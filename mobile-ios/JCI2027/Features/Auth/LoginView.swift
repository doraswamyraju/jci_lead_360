import SwiftUI

struct LoginView: View {
    @State private var email = ""
    @State private var password = ""
    @State private var isPasswordVisible = false
    @State private var rememberMe = true
    @State private var isLoading = false
    @State private var errorMessage: String? = nil

    var onLoginSuccess: (String, String, String, String, String, String?) -> Void

    var body: some View {
        ZStack {
            JciGradients.hero
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 28) {
                    Spacer(minLength: 40)

                    // Brand Logo & Header
                    VStack(spacing: 12) {
                        Image(systemName: "shield.fill")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 56, height: 56)
                            .foregroundStyle(JciGradients.shield)

                        Text("JCI LEAD360")
                            .font(.system(size: 32, weight: .black, design: .rounded))
                            .foregroundColor(.white)

                        Text("Official Command Suite for JCI 2027 Leaders")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.white.opacity(0.8))
                    }

                    // Login Card
                    VStack(alignment: .leading, spacing: 18) {
                        Text("Sign In to Portal")
                            .font(.system(size: 20, weight: .bold))
                            .foregroundColor(.jciTextDark)

                        // Email Field
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Email or Username")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(.jciTextMuted)
                            
                            HStack {
                                Image(systemName: "envelope.fill")
                                    .foregroundColor(.jciSkyBlue)
                                TextField("Enter official email", text: $email)
                                    .autocapitalization(.none)
                                    .keyboardType(.emailAddress)
                            }
                            .padding()
                            .background(Color.jciBackgroundLight)
                            .cornerRadius(12)
                        }

                        // Password Field
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Password")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(.jciTextMuted)
                            
                            HStack {
                                Image(systemName: "lock.fill")
                                    .foregroundColor(.jciSkyBlue)
                                if isPasswordVisible {
                                    TextField("Enter password", text: $password)
                                } else {
                                    SecureField("Enter password", text: $password)
                                }
                                Button(action: { isPasswordVisible.toggle() }) {
                                    Image(systemName: isPasswordVisible ? "eye.slash.fill" : "eye.fill")
                                        .foregroundColor(.jciTextMuted)
                                }
                            }
                            .padding()
                            .background(Color.jciBackgroundLight)
                            .cornerRadius(12)
                        }

                        // Remember Me
                        Toggle(isOn: $rememberMe) {
                            Text("Remember login details")
                                .font(.system(size: 13, weight: .medium))
                                .foregroundColor(.jciTextDark)
                        }
                        .tint(.jciSkyBlue)

                        // Error Banner
                        if let error = errorMessage {
                            Text(error)
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(.red)
                                .padding(10)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color.red.opacity(0.1))
                                .cornerRadius(8)
                        }

                        // Submit Button
                        Button(action: handleLogin) {
                            if isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 52)
                            } else {
                                Text("Sign In")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 52)
                            }
                        }
                        .background(JciGradients.button)
                        .cornerRadius(14)
                        .disabled(isLoading)
                        .shadow(color: Color.jciRoyalBlue.opacity(0.3), radius: 8, x: 0, y: 4)
                    }
                    .padding(26)
                    .background(Color.white)
                    .cornerRadius(24)
                    .shadow(color: Color.black.opacity(0.25), radius: 20, x: 0, y: 10)

                    Spacer(minLength: 40)
                }
                .padding(.horizontal, 20)
            }
        }
    }

    private func handleLogin() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter both email and password"
            return
        }

        isLoading = true
        errorMessage = nil

        Task {
            do {
                let response = try await ApiService.shared.login(email: email.trimmingCharacters(in: .whitespacesAndNewlines), password: password)
                await MainActor.run {
                    isLoading = false
                    if let token = response.token, let user = response.user {
                        let role = user.role ?? "PRESIDENT"
                        let name = user.name ?? "JC Leader"
                        let chapter = user.chapterName ?? "JCI Chapter"
                        let userId = user.id ?? "usr_pres_01"
                        let photoUrl = user.photoUrl
                        if let photoUrl = photoUrl {
                            UserDefaults.standard.set(photoUrl, forKey: "auth_photo_url")
                        }
                        onLoginSuccess(token, role, name, chapter, userId, photoUrl)
                    } else {
                        errorMessage = response.message ?? "Authentication failed"
                    }
                }
            } catch {
                await MainActor.run {
                    isLoading = false
                    errorMessage = "Network error: \(error.localizedDescription)"
                }
            }
        }
    }
}

