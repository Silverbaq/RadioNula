package com.radionula.radionula.comments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.radionula.radionula.databinding.FragmentCommentsBinding
import com.radionula.radionula.radio.ChannelPresenter
import org.koin.android.ext.android.inject

/**
 * Remark42 comments for the channel that is currently tuned in.
 *
 * radionula.com dropped the Facebook comments plugin this screen used to embed -
 * the Remark42 instance only offers telegram and anonymous login - so this loads
 * the same widget the website does, with the same host, site id and thread URL,
 * to keep the app and the site on one conversation.
 */
class CommentsFragment : Fragment() {

    private val channelPresenter: ChannelPresenter by inject()

    private var binding: FragmentCommentsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCommentsBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = binding?.commentsWebView ?: return

        // Remark42 is a JavaScript widget and keeps its preferences in localStorage.
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        // The comments render inside an iframe served from the Remark42 host, so
        // its login cookie is third-party as far as the page is concerned.
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        // Leaving popups off means target=_blank links load here instead of being
        // swallowed, and shouldOverrideUrlLoading can hand them to the browser.
        webView.settings.setSupportMultipleWindows(false)
        webView.setBackgroundColor(Color.TRANSPARENT)
        webView.webViewClient = ExternalLinkClient()

        webView.loadDataWithBaseURL(SITE_URL, embedHtml(), "text/html", "utf-8", null)
    }

    override fun onDestroyView() {
        binding?.commentsWebView?.let { webView ->
            webView.stopLoading()
            (webView.parent as? ViewGroup)?.removeView(webView)
            webView.destroy()
        }
        binding = null
        super.onDestroyView()
    }

    private fun embedHtml(): String {
        val channel = channelPresenter.currentChannel
        return """
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
    }

    private inner class ExternalLinkClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val host = request.url.host ?: return false
            if (host == SITE_HOST || host.endsWith(".$SITE_HOST")) return false

            // Telegram login, and anything else off-site, belongs in a real browser.
            return openExternally(request.url)
        }
    }

    private fun openExternally(uri: Uri): Boolean = try {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

    private companion object {
        const val SITE_HOST = "radionula.com"
        const val SITE_URL = "https://radionula.com/"
        const val REMARK42_HOST = "https://comments.radionula.com"
        const val REMARK42_SITE_ID = "radionula-prod"
    }
}
