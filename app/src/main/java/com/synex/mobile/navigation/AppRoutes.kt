package com.synex.mobile.navigation

object AppRoutes {
    const val LEGAL = "legal"
    const val LEGAL_DOCUMENT = "legal/{documentType}"
    const val POSITION = "position/{contractId}"
    const val FUNDING = "funding"
    const val AUTOMATION = "automation"
    const val LEGACY_HISTORY = "legacy-history"

    fun legalDocument(documentType: String) = "legal/$documentType"
    fun position(contractId: Long) = "position/$contractId"
}
