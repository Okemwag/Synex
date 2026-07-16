package com.synex.feature.legal

data class LegalLink(
    val type: String,
    val title: String,
    val summary: String,
    val url: String,
)

internal val legalLinks = listOf(
    LegalLink("privacy", "Privacy Notice", "How we use and protect your personal information.", "https://synex.app/legal/privacy"),
    LegalLink("terms", "Terms of Use", "The agreement for using Synex and connected accounts.", "https://synex.app/legal/terms"),
    LegalLink("risk", "Trading Risks", "Important risks to understand before you trade.", "https://synex.app/legal/risk"),
    LegalLink("platform-disclosure", "How Synex Works", "The roles of Synex, Deriv, and our service partners.", "https://synex.app/legal/platform-disclosure"),
    LegalLink("order-execution", "How Orders Work", "What happens after you confirm a trade.", "https://synex.app/legal/order-execution"),
    LegalLink("financial-crime", "Keeping Accounts Safe", "How we prevent fraud and account misuse.", "https://synex.app/legal/financial-crime"),
    LegalLink("cookies", "Cookie Notice", "How the Synex website remembers your preferences.", "https://synex.app/legal/cookies"),
    LegalLink("complaints", "Making a Complaint", "How to raise a concern and what happens next.", "https://synex.app/legal/complaints"),
    LegalLink("data-rights", "Your Data Choices", "How to view, correct, download, or delete your information.", "https://synex.app/legal/data-rights"),
)

internal fun findLegalLink(type: String) = legalLinks.firstOrNull { it.type == type }
