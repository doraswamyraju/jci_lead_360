import SwiftUI
import UIKit

/// Replicates the Luxury Instant Exchange QR Code Modal Dialog from MainActivity.kt (lines 183-284)
/// Provides complete reciprocal contact exchange capabilities with push notification integration:
/// - Real-time QR Code pointing to live web card
/// - Copy direct card URL with instant visual & haptic feedback
/// - Native iOS ShareSheet integration (WhatsApp, iMessage, AirDrop)
/// - Save high-res QR code image to Photo Library
/// - Preview live visitor web exchange portal in Safari
/// - Instant Test Push Alert trigger to verify reciprocal exchange notifications
/// - Direct routing to full card scanner & Rolodex
struct ExchangeQrDialogView: View {
    let userName: String
    let chapterName: String
    let userId: String
    var onDismiss: () -> Void
    var onOpenFullScanner: () -> Void

    @ObservedObject private var pushManager = PushNotificationManager.shared
    @State private var copiedToClipboard = false
    @State private var savedToPhotos = false
    @State private var showTestNotificationSuccess = false

    var exchangeUrl: String {
        QrCodeGenerator.buildExchangeUrl(leaderId: userId)
    }

    var body: some View {
        ZStack {
            // Backdrop
            Color.black.opacity(0.6)
                .ignoresSafeArea()
                .onTapGesture {
                    onDismiss()
                }

            VStack(spacing: 14) {
                // MARK: - Title Row
                HStack(spacing: 10) {
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [Color(hex: "0097D8"), Color(hex: "002F6C")],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 36, height: 36)

                        Image(systemName: "qrcode")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                    }

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Exchange Contacts")
                            .font(.system(size: 17, weight: .heavy))
                            .foregroundColor(Color(hex: "0F172A"))

                        Text(chapterName.isEmpty ? "President Digital Card" : "President • \(chapterName)")
                            .font(.system(size: 11, weight: .medium))
                            .foregroundColor(Color(hex: "64748B"))
                            .lineLimit(1)
                    }

                    Spacer()

                    Button(action: onDismiss) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 22))
                            .foregroundColor(Color(hex: "94A3B8"))
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 20)
                .padding(.top, 18)

                // MARK: - Live Push Exchange Status Banner
                HStack(spacing: 6) {
                    Circle()
                        .fill(Color(hex: "10B981"))
                        .frame(width: 7, height: 7)

                    Text("LIVE PUSH EXCHANGE ACTIVE")
                        .font(.system(size: 10, weight: .black))
                        .foregroundColor(Color(hex: "047857"))

                    if pushManager.totalExchangesCount > 0 {
                        Text("• \(pushManager.totalExchangesCount) EXCHANGED")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(Color(hex: "065F46"))
                    }
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(Color(hex: "D1FAE5"))
                .cornerRadius(12)

                // MARK: - QR Code Surface
                VStack(spacing: 8) {
                    if let qrImage = QrCodeGenerator.generateQrImage(from: exchangeUrl, size: CGSize(width: 480, height: 480)) {
                        Image(uiImage: qrImage)
                            .resizable()
                            .interpolation(.none)
                            .scaledToFit()
                            .frame(width: 195, height: 195)
                            .padding(12)
                            .background(Color.white)
                            .cornerRadius(16)
                            .overlay(
                                RoundedRectangle(cornerRadius: 16)
                                    .stroke(Color(hex: "E2E8F0"), lineWidth: 1.5)
                            )
                            .shadow(color: Color.black.opacity(0.08), radius: 8, x: 0, y: 4)
                    } else {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: Color(hex: "0097D8")))
                            .frame(width: 195, height: 195)
                    }

                    Text("Scan with any camera to instantly exchange contacts with \(userName)")
                        .font(.system(size: 11.5))
                        .foregroundColor(Color(hex: "475569"))
                        .multilineTextAlignment(.center)
                        .lineSpacing(2)
                        .padding(.horizontal, 16)
                }

                // MARK: - Share, Copy & Save Action Bar
                HStack(spacing: 8) {
                    // Copy Link Button
                    Button(action: {
                        UIPasteboard.general.string = exchangeUrl
                        let generator = UIImpactFeedbackGenerator(style: .medium)
                        generator.impactOccurred()
                        withAnimation { copiedToClipboard = true }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                            withAnimation { copiedToClipboard = false }
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: copiedToClipboard ? "checkmark" : "doc.on.doc")
                                .font(.system(size: 11, weight: .bold))
                            Text(copiedToClipboard ? "Copied!" : "Copy Link")
                                .font(.system(size: 11, weight: .semibold))
                        }
                        .foregroundColor(copiedToClipboard ? Color(hex: "047857") : Color.jciRoyalBlue)
                        .frame(maxWidth: .infinity)
                        .frame(height: 34)
                        .background(copiedToClipboard ? Color(hex: "D1FAE5") : Color.jciRoyalBlue.opacity(0.08))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)

                    // Share Link Button (Native iOS Share Sheet)
                    if let shareUrl = URL(string: exchangeUrl) {
                        ShareLink(
                            item: shareUrl,
                            subject: Text("JCI Digital Business Card"),
                            message: Text("Exchange contacts with \(userName) (President, \(chapterName)): \(exchangeUrl)")
                        ) {
                            HStack(spacing: 4) {
                                Image(systemName: "square.and.arrow.up")
                                    .font(.system(size: 11, weight: .bold))
                                Text("Share")
                                    .font(.system(size: 11, weight: .semibold))
                            }
                            .foregroundColor(Color(hex: "0097D8"))
                            .frame(maxWidth: .infinity)
                            .frame(height: 34)
                            .background(Color(hex: "0097D8").opacity(0.08))
                            .cornerRadius(8)
                        }
                    }

                    // Save QR to Photos Button
                    Button(action: {
                        saveQrToPhotos()
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: savedToPhotos ? "checkmark" : "arrow.down.to.line.compact")
                                .font(.system(size: 11, weight: .bold))
                            Text(savedToPhotos ? "Saved!" : "Save QR")
                                .font(.system(size: 11, weight: .semibold))
                        }
                        .foregroundColor(savedToPhotos ? Color(hex: "047857") : Color(hex: "475569"))
                        .frame(maxWidth: .infinity)
                        .frame(height: 34)
                        .background(savedToPhotos ? Color(hex: "D1FAE5") : Color(hex: "F1F5F9"))
                        .cornerRadius(8)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 20)

                // MARK: - Testing & Safari Preview Row
                HStack(spacing: 12) {
                    // Preview Live Web Card in Safari
                    if let webUrl = URL(string: exchangeUrl) {
                        Link(destination: webUrl) {
                            HStack(spacing: 4) {
                                Image(systemName: "safari")
                                    .font(.system(size: 11))
                                Text("Preview Web Card")
                                    .font(.system(size: 11, weight: .semibold))
                            }
                            .foregroundColor(Color.jciSkyBlue)
                        }
                    }

                    Text("•")
                        .foregroundColor(Color(hex: "CBD5E1"))
                        .font(.system(size: 10))

                    // Test Reciprocal Push Alert Button
                    Button(action: {
                        let generator = UINotificationFeedbackGenerator()
                        generator.notificationOccurred(.success)
                        pushManager.triggerTestAlert()
                        withAnimation { showTestNotificationSuccess = true }
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                            withAnimation { showTestNotificationSuccess = false }
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "bell.badge.fill")
                                .font(.system(size: 11))
                            Text(showTestNotificationSuccess ? "Alert Fired! 🔔" : "Test Push Alert")
                                .font(.system(size: 11, weight: .semibold))
                        }
                        .foregroundColor(showTestNotificationSuccess ? Color(hex: "047857") : Color(hex: "D97706"))
                    }
                    .buttonStyle(.plain)
                }

                Divider()
                    .padding(.horizontal, 20)

                // MARK: - Primary Action Buttons
                HStack(spacing: 12) {
                    Button(action: onDismiss) {
                        Text("Close")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(Color(hex: "0F172A"))
                            .frame(maxWidth: .infinity)
                            .frame(height: 44)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color(hex: "CBD5E1"), lineWidth: 1)
                            )
                    }
                    .buttonStyle(.plain)

                    Button(action: onOpenFullScanner) {
                        HStack(spacing: 6) {
                            Image(systemName: "qrcode.viewfinder")
                                .font(.system(size: 14, weight: .bold))
                            Text("Open Full Scanner")
                                .font(.system(size: 14, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 44)
                        .background(Color(hex: "0097D8"))
                        .cornerRadius(10)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 18)
            }
            .frame(maxWidth: 340)
            .background(Color.white)
            .cornerRadius(22)
            .shadow(color: Color.black.opacity(0.25), radius: 20, x: 0, y: 10)
        }
    }

    private func saveQrToPhotos() {
        guard let qrImage = QrCodeGenerator.generateQrImage(from: exchangeUrl, size: CGSize(width: 600, height: 600)) else {
            return
        }

        let saver = QrImageSaver {
            DispatchQueue.main.async {
                let generator = UINotificationFeedbackGenerator()
                generator.notificationOccurred(.success)
                withAnimation { savedToPhotos = true }
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
                    withAnimation { savedToPhotos = false }
                }
            }
        }
        saver.writeToPhotoAlbum(image: qrImage)
    }
}

// MARK: - Helper class for Photo Album export
class QrImageSaver: NSObject {
    private let onComplete: () -> Void

    init(onComplete: @escaping () -> Void) {
        self.onComplete = onComplete
        super.init()
    }

    func writeToPhotoAlbum(image: UIImage) {
        UIImageWriteToSavedPhotosAlbum(image, self, #selector(saveCompleted), nil)
    }

    @objc func saveCompleted(_ image: UIImage, didFinishSavingWithError error: Error?, contextInfo: UnsafeRawPointer) {
        onComplete()
    }
}
