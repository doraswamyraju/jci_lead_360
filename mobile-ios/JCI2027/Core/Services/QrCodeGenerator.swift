import CoreGraphics
import Foundation
import CoreImage
import CoreImage.CIFilterBuiltins
import UIKit

class QrCodeGenerator {
    static func buildExchangeUrl(leaderId: String = "usr_pres_01", baseUrl: String = "https://lead.sriddha.com") -> String {
        return "\(baseUrl)/card/\(leaderId)"
    }

    static func generateQrImage(from string: String, size: CGSize = CGSize(width: 480, height: 480)) -> UIImage? {
        let filter = CIFilter.qrCodeGenerator()
        guard let data = string.data(using: .utf8) else { return nil }
        filter.setValue(data, forKey: "inputMessage")
        filter.setValue("M", forKey: "inputCorrectionLevel")

        guard let outputImage = filter.outputImage else { return nil }
        let scaleX = size.width / outputImage.extent.size.width
        let scaleY = size.height / outputImage.extent.size.height
        let transformedImage = outputImage.transformed(by: CGAffineTransform(scaleX: scaleX, y: scaleY))

        let context = CIContext()
        guard let cgImage = context.createCGImage(transformedImage, from: transformedImage.extent) else { return nil }
        return UIImage(cgImage: cgImage)
    }
}

