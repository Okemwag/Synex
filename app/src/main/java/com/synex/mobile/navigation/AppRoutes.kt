package com.synex.mobile.navigation

object AppRoutes {
    const val LEGAL = "legal"
    const val LEGAL_DOCUMENT = "legal/{documentType}"
    const val POSITION = "position/{contractId}"

    fun legalDocument(documentType: String) = "legal/$documentType"
    fun position(contractId: Long) = "position/$contractId"
}
