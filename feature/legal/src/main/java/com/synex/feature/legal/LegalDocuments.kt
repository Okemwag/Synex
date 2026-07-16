package com.synex.feature.legal

data class LegalLink(
    val title: String,
    val summary: String,
    val version: String,
    val url: String,
)

internal val legalLinks = listOf(
    LegalLink("Privacy Notice", "How Synex processes and protects personal data.", "v1 draft", "https://synex.app/legal/privacy"),
    LegalLink("Platform Terms", "Rules for using Synex and connected accounts.", "v1 draft", "https://synex.app/legal/terms"),
    LegalLink("Trading Risk Disclosure", "Material product, execution, and technology risks.", "v1", "https://synex.app/legal/risk"),
    LegalLink("Platform and Deriv Disclosure", "The responsibilities of Synex, Deriv, and payment providers.", "v1 draft", "https://synex.app/legal/platform-disclosure"),
    LegalLink("Order Transmission", "How confirmed instructions move from Synex to Deriv.", "v1 draft", "https://synex.app/legal/order-execution"),
    LegalLink("Financial Crime and AML", "Identity, sanctions, fraud, and suspicious-activity controls.", "v1 draft", "https://synex.app/legal/financial-crime"),
    LegalLink("Cookie Notice", "Website cookies and local-storage practices.", "v1 draft", "https://synex.app/legal/cookies"),
    LegalLink("Complaints Procedure", "How to report and escalate a complaint.", "v1 draft", "https://synex.app/legal/complaints"),
    LegalLink("Data Rights", "Access, correction, export, deletion, and account closure.", "v1 draft", "https://synex.app/legal/data-rights"),
)
