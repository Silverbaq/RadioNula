package com.radionula.radionula.core.util

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.NSURL
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.UIKit.UIApplication

/** iOS has no web-search intent, so this opens the query in Safari. */
class IosWebSearch : WebSearch {
    override fun search(query: String) {
        val encoded = (query as NSString)
            .stringByAddingPercentEncodingWithAllowedCharacters(
                NSCharacterSet.URLQueryAllowedCharacterSet
            ) ?: return
        NSURL.URLWithString("https://duckduckgo.com/?q=$encoded")
            ?.let { UIApplication.sharedApplication.openURL(it) }
    }
}
