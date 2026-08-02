package com.radionula.radionula.features.comments

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.radionula.radionula.core.ui.theme.FadedBlack
import com.radionula.radionula.core.util.ChannelPresenter
import com.radionula.radionula.resources.Res
import com.radionula.radionula.resources.background
import org.jetbrains.compose.resources.painterResource

/**
 * Remark42 comments for the channel that is currently tuned in.
 *
 * radionula.com dropped the Facebook comments plugin this screen used to embed -
 * the Remark42 instance only offers telegram and anonymous login - so this loads
 * the same widget the website does, with the same host, site id and thread URL,
 * to keep the app and the site on one conversation.
 *
 * The artwork and scrim are what fragment_comments.xml drew. Only the web view
 * itself is per-platform: see [CommentsWebView].
 */
@Composable
fun CommentsScreen(channel: ChannelPresenter.Channel, modifier: Modifier = Modifier) {
    val html = remember(channel) { embedHtml(channel) }

    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // The artwork alone is too busy to read text over, so it sits behind a
        // scrim. The web view stays transparent so both show through.
        Box(Modifier.fillMaxSize().background(FadedBlack))
        CommentsWebView(html = html, modifier = Modifier.fillMaxSize())
    }
}

/**
 * A transparent web view showing [html], with off-site links handed to the
 * system browser - telegram login is off-site and belongs in a real browser.
 *
 * Not shared: the Android side has to call `setAcceptThirdPartyCookies` on the
 * WebView instance, because the comments render in an iframe served from the
 * Remark42 host and its login cookie is third-party as far as the page is
 * concerned. No cross-platform WebView wrapper exposes the instance, and
 * without that call login silently fails on Android.
 */
@Composable
expect fun CommentsWebView(html: String, modifier: Modifier = Modifier)

internal fun embedHtml(channel: ChannelPresenter.Channel): String = """
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

internal const val SITE_HOST = "radionula.com"
internal const val SITE_URL = "https://radionula.com/"
private const val REMARK42_HOST = "https://comments.radionula.com"
private const val REMARK42_SITE_ID = "radionula-prod"
