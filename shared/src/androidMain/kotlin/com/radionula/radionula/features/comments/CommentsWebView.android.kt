package com.radionula.radionula.features.comments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun CommentsWebView(html: String, modifier: Modifier) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            // Remark42 is a JavaScript widget and keeps its preferences in localStorage.
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            // The comments render inside an iframe served from the Remark42 host,
            // so its login cookie is third-party as far as the page is concerned.
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            // Leaving popups off means target=_blank links load here instead of
            // being swallowed, and shouldOverrideUrlLoading can hand them to the browser.
            settings.setSupportMultipleWindows(false)
            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = ExternalLinkClient(context::startActivity)
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
        update = { it.loadDataWithBaseURL(SITE_URL, html, "text/html", "utf-8", null) },
    )
}

private class ExternalLinkClient(private val startActivity: (Intent) -> Unit) : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val host = request.url.host ?: return false
        if (host == SITE_HOST || host.endsWith(".$SITE_HOST")) return false

        // Telegram login, and anything else off-site, belongs in a real browser.
        return openExternally(request.url)
    }

    private fun openExternally(uri: Uri): Boolean = try {
        startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
