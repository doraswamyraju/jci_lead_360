import SwiftUI

/**
 * Single Unified iOS Login View
 * Directs user via RoleRouter to respective dashboard
 */
struct LoginView: View {
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String? = nil
    
    var onLoginSuccess: (String, String, String, String) -> Void

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(hex: "0F172A"), Color(hex: "0284C7")],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                VStack(spacing: 6) {
                    Text("JCI LEAD360")
                        .font(.system(size: 28, weight: .black))
                        .foregroundColor(.white)
                    
                    Text("Digital Support for JCI Leaders 2027")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(Color.white.opacity(0.8))
                }
                .padding(.bottom, 12)

                VStack(spacing: 16) {
                    TextField("Email Address or Phone", text: $email)
                        .padding()
                        .background(Color.white)
                        .cornerRadius(10)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)

                    SecureField("Password", text: $password)
                        .padding()
                        .background(Color.white)
                        .cornerRadius(10)

                    if let errorMessage = errorMessage {
                        Text(errorMessage)
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.red)
                    }

                    Button(action: handleLogin) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                        } else {
                            Text("Sign In to Command Hub")
                                .font(.system(size: 15, weight: .bold))
                                .foregroundColor(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 50)
                                .background(Color(hex: "0097D8"))
                                .cornerRadius(10)
                        }
                    }
                    .disabled(isLoading)
                }
                .padding(24)
                .background(Color.white.opacity(0.95))
                .cornerRadius(16)
                .shadow(color: Color.black.opacity(0.2), radius: 15, x: 0, y: 10)
            }
            .padding(.horizontal, 24)
        }
    }

    private func handleLogin() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter email and password"
            return
        }
        isLoading = true
        let role = email.contains("admin") ? "SUPER_ADMIN" : "PRESIDENT"
        onLoginSuccess("mock_jwt_token", role, "JC Leader", "JCI Tirupati Innovations")
    }
}

extension Color {
    init(hex: String) {
        let scanner = Scanner(string: hex)
        var rgbValue: UInt64 = 0
        scanner.scanHexInt64(&rgbValue)
        let r = Double((rgbValue & 0xFF0000) >> 16) / 255.0
        let g = Double((rgbValue & 0x00FF00) >> 8) / 255.0
        let b = Double(rgbValue & 0x0000FF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}
