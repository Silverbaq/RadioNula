package com.radionula.fragments;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.radionula.radionula.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment {

    WebView myWebView;

    public static String APP_KEY = "330582393726734";
    public static String BASE_DOMAIN = "http://radionula.com";


    public CommentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_comments, container, false);

        myWebView = (WebView) view.findViewById(R.id.fragment_comments_webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadDataWithBaseURL(BASE_DOMAIN,
                "<html><head></head><body><div id=\"fb-root\"></div><div id=\"fb-root\"></div><script>(function(d, s, id) {var js, fjs = d.getElementsByTagName(s)[0];if (d.getElementById(id)) return;js = d.createElement(s); js.id = id;js.src = \"http://connect.facebook.net/en_US/all.js#xfbml=1&appId=" + APP_KEY + "\";fjs.parentNode.insertBefore(js, fjs);}(document, 'script', 'facebook-jssdk'));</script><div class=\"fb-comments\" data-href=\""
                        + BASE_DOMAIN + "\" data-width=\"470\" data-order-by=\"reverse_time\"></div> </body></html>", "text/html", null, null);
        myWebView.setWebViewClient(new WebViewClientActivity());


        return view;
    }

    public class WebViewClientActivity extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            System.out.println("onPageStarted: " + url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView webView, String url) {
            System.out.println("onPageFinished: " + url);

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

}
