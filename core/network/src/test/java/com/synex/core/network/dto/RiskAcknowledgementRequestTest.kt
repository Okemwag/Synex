package com.synex.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskAcknowledgementRequestTest {
    @Test
    fun `accepted is always included in the request body`() {
        val payload = Json.parseToJsonElement(
            Json.encodeToString(
                RiskAcknowledgementRequest(
                    accepted = true,
                    disclosureVersion = "synex-risk-disclosure-v1",
                ),
            ),
        ).jsonObject

        assertEquals(true, payload.getValue("accepted").jsonPrimitive.boolean)
        assertEquals(
            "synex-risk-disclosure-v1",
            payload.getValue("disclosure_version").jsonPrimitive.content,
        )
    }
}
