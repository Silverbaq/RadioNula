package com.radionula.radionula.ui.comments

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import com.radionula.radionula.R
import com.radionula.radionula.radio.ChannelPresenter
import com.radionula.radionula.ui.theme.FadedBlack

/**
 * Remark42 comments for the channel that is currently tuned in.
 *
 * radionula.com dropped the Facebook comments plugin this screen used to embed -
 * the Remark42 instance only offers telegram and anonymous login - so this loads
 * the same widget the website does, with the same host, site id and thread URL,
 * to keep the app and the site on one conversation.
 *
 * Compose has no WebView, so this stays a View. The artwork and scrim around it
 * are what fragment_comments.xml drew.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CommentsScreen(channel: ChannelPresenter.Channel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val html = remember(channel) { embedHtml(channel) }

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

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // The artwork alone is too busy to read text over, so it sits behind a
        // scrim. The WebView stays transparent so both show through.
        Box(Modifier.fillMaxSize().background(FadedBlack))
        AndroidView(
            factory = { webView },
            modifier = Modifier.fillMaxSize(),
            update = { it.loadDataWithBaseURL(SITE_URL, html, "text/html", "utf-8", null) },
        )
    }
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

private fun embedHtml(channel: ChannelPresenter.Channel): String = """
    <!doctype html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <style>html, body { margin: 0; padding: 8px; background: transparent; }</style>
    </head>
    <body>
      <div id="remark42"></div>
      <script>
        var remark_config = {
          host: "$REMARK42_HOST",
          site_id: "$REMARK42_SITE_ID",
          url: "${channel.commentsUrl}",
          page_title: "Radio NULA - ${channel.displayName}",
          theme: "dark",
          locale: "en",
          components: ["embed"],
          max_shown_comments: 10
        };
      </script>
      <script src="$REMARK42_HOST/web/embed.js"></script>
    </body>
    </html>
""".trimIndent()

private const val SITE_HOST = "radionula.com"
private const val SITE_URL = "https://radionula.com/"
private const val REMARK42_HOST = "https://comments.radionula.com"
private const val REMARK42_SITE_ID = "radionula-prod"
