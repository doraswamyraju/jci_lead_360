import UIKit
import Vision

public struct ParsedCardData {
    public var name: String = ""
    public var designation: String = ""
    public var company: String = ""
    public var phone: String = ""
    public var email: String = ""
    public var website: String = ""
    public var address: String = ""
    public var notes: String = ""
    public var rawText: String = ""
    public var confidence: String = "HIGH"

    public init(
        name: String = "",
        designation: String = "",
        company: String = "",
        phone: String = "",
        email: String = "",
        website: String = "",
        address: String = "",
        notes: String = "",
        rawText: String = "",
        confidence: String = "HIGH"
    ) {
        self.name = name
        self.designation = designation
        self.company = company
        self.phone = phone
        self.email = email
        self.website = website
        self.address = address
        self.notes = notes
        self.rawText = rawText
        self.confidence = confidence
    }
}

public final class BusinessCardOcrEngine {
    public static let shared = BusinessCardOcrEngine()

    private let emailRegex = try? NSRegularExpression(pattern: "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private let phoneRegex = try? NSRegularExpression(pattern: #"(?:\+?\d{1,3}[-.\s]?)?(?:\(?\d{2,4}\)?[-.\s]?)?\d{3,5}[-.\s]?\d{3,5}(?:[-.\s]?\d{2,4})*"#)
    private let websiteRegex = try? NSRegularExpression(pattern: #"(?:https?://)?(?:www\.)?[a-zA-Z0-9][a-zA-Z0-9-]+\.[a-zA-Z]{2,}(?:/[^\s]*)?"#)

    private let companyKeywords = [
        "ltd", "limited", "pvt", "inc", "corp", "corporation", "llc", "technologies",
        "tech", "solutions", "group", "associates", "consulting", "enterprises",
        "industries", "holdings", "systems", "innovations", "logistics", "clinic",
        "clinics", "hospital", "services", "infra", "builders", "realtors"
    ]

    private let titleKeywords = [
        "president", "vice president", "secretary", "treasurer", "director", "ceo", "coo", "cto", "cfo",
        "founder", "co-founder", "chairman", "manager", "general manager", "consultant",
        "specialist", "advocate", "doctor", "dr.", "senator", "jc", "hgf", "partner", "principal", "head"
    ]

    private let addressKeywords = [
        "road", "rd", "street", "st", "lane", "avenue", "ave", "nagar", "colony", "layout",
        "sector", "floor", "building", "complex", "plaza", "block", "opp", "near", "highway"
    ]

    private init() {}

    /// Performs native on-device Vision text recognition on the given UIImage
    public func recognizeText(from image: UIImage) async throws -> String {
        guard let cgImage = image.cgImage else {
            throw NSError(domain: "BusinessCardOcrEngine", code: -1, userInfo: [NSLocalizedDescriptionKey: "Invalid image"])
        }

        return try await withCheckedThrowingContinuation { continuation in
            let request = VNRecognizeTextRequest { request, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }

                guard let observations = request.results as? [VNRecognizedTextObservation] else {
                    continuation.resume(returning: "")
                    return
                }

                let lines = observations.compactMap { $0.topCandidates(1).first?.string }
                continuation.resume(returning: lines.joined(separator: "\n"))
            }

            request.recognitionLevel = .accurate
            request.usesLanguageCorrection = true

            let handler = VNImageRequestHandler(cgImage: cgImage, orientation: CGImagePropertyOrientation(image.imageOrientation), options: [:])
            do {
                try handler.perform([request])
            } catch {
                continuation.resume(throwing: error)
            }
        }
    }

    /// Parses raw OCR output into structured contact card fields
    public func parse(rawText: String) -> ParsedCardData {
        var parsed = ParsedCardData(rawText: rawText)
        let lines = rawText
            .components(separatedBy: .newlines)
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }

        guard !lines.isEmpty else { return parsed }

        var usedLineIndices = Set<Int>()

        // 1. Extract Email
        for (i, line) in lines.enumerated() {
            if let regex = emailRegex,
               let match = regex.firstMatch(in: line, range: NSRange(line.startIndex..., in: line)),
               let range = Range(match.range, in: line) {
                parsed.email = String(line[range]).lowercased()
                usedLineIndices.insert(i)
                break
            }
        }

        // 2. Extract Website
        for (i, line) in lines.enumerated() {
            guard !usedLineIndices.contains(i) else { continue }
            if let regex = websiteRegex,
               let match = regex.firstMatch(in: line, range: NSRange(line.startIndex..., in: line)),
               let range = Range(match.range, in: line) {
                let candidate = String(line[range]).lowercased()
                if !candidate.contains("@") {
                    parsed.website = candidate
                    usedLineIndices.insert(i)
                    break
                }
            }
        }

        // 3. Extract Phone
        for (i, line) in lines.enumerated() {
            guard !usedLineIndices.contains(i) else { continue }
            if let regex = phoneRegex,
               let match = regex.firstMatch(in: line, range: NSRange(line.startIndex..., in: line)),
               let range = Range(match.range, in: line) {
                let digits = line.filter { $0.isNumber }
                if digits.count >= 7 {
                    parsed.phone = String(line[range]).trimmingCharacters(in: .whitespaces)
                    usedLineIndices.insert(i)
                    break
                }
            }
        }

        // 4. Extract Designation / Title
        for (i, line) in lines.enumerated() {
            guard !usedLineIndices.contains(i) else { continue }
            let lower = line.lowercased()
            if titleKeywords.contains(where: { lower.contains($0) }) {
                parsed.designation = line
                usedLineIndices.insert(i)
                break
            }
        }

        // 5. Extract Company
        for (i, line) in lines.enumerated() {
            guard !usedLineIndices.contains(i) else { continue }
            let lower = line.lowercased()
            if companyKeywords.contains(where: { lower.contains($0) }) {
                parsed.company = line
                usedLineIndices.insert(i)
                break
            }
        }

        // 6. Extract Address
        var addressLines: [String] = []
        for (i, line) in lines.enumerated() {
            guard !usedLineIndices.contains(i) else { continue }
            let lower = line.lowercased()
            if addressKeywords.contains(where: { lower.contains($0) }) || line.filter({ $0.isNumber }).count >= 5 {
                addressLines.append(line)
                usedLineIndices.insert(i)
            }
        }
        if !addressLines.isEmpty {
            parsed.address = addressLines.joined(separator: ", ")
        }

        // 7. Extract Name (First available high-confidence person line near top)
        for (i, line) in lines.enumerated() {
            guard !usedLineIndices.contains(i) else { continue }
            let words = line.components(separatedBy: .whitespaces).filter { !$0.isEmpty }
            if (2...4).contains(words.count) && line.rangeOfCharacter(from: .decimalDigits) == nil {
                parsed.name = line
                usedLineIndices.insert(i)
                break
            }
        }

        // Fallback for Name if not yet identified
        if parsed.name.isEmpty {
            for (i, line) in lines.enumerated() {
                guard !usedLineIndices.contains(i) else { continue }
                if line.count > 2 && line.rangeOfCharacter(from: .decimalDigits) == nil {
                    parsed.name = line
                    usedLineIndices.insert(i)
                    break
                }
            }
        }

        // Confidence calculation
        var filledCount = 0
        if !parsed.name.isEmpty { filledCount += 1 }
        if !parsed.phone.isEmpty { filledCount += 1 }
        if !parsed.email.isEmpty { filledCount += 1 }
        if !parsed.company.isEmpty { filledCount += 1 }
        if !parsed.designation.isEmpty { filledCount += 1 }

        parsed.confidence = filledCount >= 3 ? "HIGH" : (filledCount >= 2 ? "MEDIUM" : "LOW")
        return parsed
    }
}

private extension CGImagePropertyOrientation {
    init(_ uiOrientation: UIImage.Orientation) {
        switch uiOrientation {
        case .up: self = .up
        case .upMirrored: self = .upMirrored
        case .down: self = .down
        case .downMirrored: self = .downMirrored
        case .left: self = .left
        case .leftMirrored: self = .leftMirrored
        case .right: self = .right
        case .rightMirrored: self = .rightMirrored
        @unknown default: self = .up
        }
    }
}
