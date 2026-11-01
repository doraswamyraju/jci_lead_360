package com.jci2027.lead360.engine

/**
 * Fuzzy & Exact Deduplication algorithms adapted from CardSnap
 */
object CardSnapDeduplicator {

    fun normalizedSimilarity(a: String, b: String): Double {
        val aStr = a.trim().lowercase()
        val bStr = b.trim().lowercase()
        if (aStr == bStr) return 1.0
        if (aStr.isEmpty() || bStr.isEmpty()) return 0.0
        val maxLen = maxOf(aStr.length, bStr.length)
        if (maxLen == 0) return 1.0
        return 1.0 - levenshteinDistance(aStr, bStr).toDouble() / maxLen
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }

    /**
     * Checks if new candidate contact matches an existing contact by Phone, Email, or Name similarity
     */
    fun checkDuplicate(
        candidateName: String,
        candidatePhone: String,
        candidateEmail: String,
        candidateCompany: String,
        existingList: List<Triple<String, String, String>> // list of Triple(name, phone, email)
    ): Pair<Boolean, String?> {
        val cleanCandidatePhone = candidatePhone.filter { it.isDigit() }
        val cleanCandidateEmail = candidateEmail.trim().lowercase()

        for ((exName, exPhone, exEmail) in existingList) {
            val cleanExPhone = exPhone.filter { it.isDigit() }
            val cleanExEmail = exEmail.trim().lowercase()

            // Exact phone match (at least 8 digits)
            if (cleanCandidatePhone.length >= 8 && cleanExPhone.length >= 8 && cleanCandidatePhone.takeLast(10) == cleanExPhone.takeLast(10)) {
                return Pair(true, "Exact match with existing phone number ($exName)")
            }

            // Exact email match
            if (cleanCandidateEmail.isNotBlank() && cleanCandidateEmail == cleanExEmail) {
                return Pair(true, "Exact match with existing email ($exName)")
            }

            // Fuzzy Name similarity (> 82%)
            val sim = normalizedSimilarity(candidateName, exName)
            if (sim >= 0.82) {
                return Pair(true, "High name similarity (${(sim * 100).toInt()}%) with $exName")
            }
        }

        return Pair(false, null)
    }
}

