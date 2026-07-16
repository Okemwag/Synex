package com.synex.mobile.navigation

object AppRoutes {
    const val LEGAL = "legal"
    const val LEGAL_DOCUMENT = "legal/{documentType}"

    fun legalDocument(documentType: String) = "legal/$documentType"
}
