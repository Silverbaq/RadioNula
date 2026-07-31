package com.radionula.radionula.features.comments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun CommentsWebView(html: String, modifier: Modifier) {
    val state = rememberWebViewStateWithHTMLData(data = html, baseUrl = SITE_URL)
    val navigator = rememberWebViewNavigator(requestInterceptor = ExternalLinkInterceptor)

    DisposableEffect(state) {
        state.webSettings.apply {
            isJavaScriptEnabled = true
            // Transparent, so the artwork and scrim behind the view show through -
            // opaque = false is what actually lets a WKWebView be see-through.
            backgroundColor = Color.Transparent
            iOSWebSettings.opaque = false
            iOSWebSettings.backgroundColor = Color.Transparent
        }
        onDispose { }
    }

    WebView(state = state, navigator = navigator, modifier = modifier)
}

/** Telegram login, and anything else off-site, belongs in Safari. */
private object ExternalLinkInterceptor : RequestInterceptor {
    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResult {
        val host = NSURL(string = request.url).host
        val onSite = host == null || host == SITE_HOST || host.endsWith(".$SITE_HOST")
        if (onSite) return WebRequestInterceptResult.Allow

        NSURL.URLWithString(request.url)?.let { UIApplication.sharedApplication.openURL(it) }
        return WebRequestInterceptResult.Reject
    }
}
