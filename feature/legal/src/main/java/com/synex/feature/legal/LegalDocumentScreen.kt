package com.synex.feature.legal

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.synex.core.ui.SynexMuted
import com.synex.core.ui.SynexPaper

@Composable
fun LegalDocumentRoute(documentType: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val document = findLegalLink(documentType)
    Column(Modifier.fillMaxSize().background(SynexPaper)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Go back") }
            Text(document?.title ?: "Legal document", style = MaterialTheme.typography.titleLarge)
        }
        if (document == null) MissingDocument() else LegalWebView(document.url)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LegalWebView(url: String) {
    var loading by remember(url) { mutableStateOf(true) }
    var failed by remember(url) { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = false
                    settings.domStorageEnabled = false
                    settings.allowContentAccess = false
                    webViewClient = legalClient(
                        onLoading = { loading = it },
                        onFailure = { failed = true },
                    )
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (loading && !failed) CircularProgressIndicator(Modifier.align(Alignment.Center))
        if (failed) MissingDocument(Modifier.align(Alignment.Center))
    }
}

private fun legalClient(onLoading: (Boolean) -> Unit, onFailure: () -> Unit) = object : WebViewClient() {
    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) = onLoading(true)
    override fun onPageFinished(view: WebView?, url: String?) = onLoading(false)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean =
        request?.url?.host != "synex.app"
    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        if (request?.isForMainFrame == true) onFailure()
    }
}

@Composable
private fun MissingDocument(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("This document is unavailable right now.", color = SynexMuted)
    }
}
