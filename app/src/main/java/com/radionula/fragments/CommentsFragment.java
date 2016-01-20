package com.radionula.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
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
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        //myWebView.setWebViewClient(new WebViewClient());


        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
               // myWebView.removeAllViews();

                WebView newView = new WebView(getContext());
                newView.setWebViewClient(new WebViewClient());

                // Create dynamically a new view
                newView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                myWebView.addView(newView);

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(newView);
                resultMsg.sendToTarget();
                return true;
            }


        });

        myWebView.loadDataWithBaseURL(BASE_DOMAIN,
                "<html><head></head><body><div id=\"fb-root\"></div><div id=\"fb-root\"></div><script>(function(d, s, id) {var js, fjs = d.getElementsByTagName(s)[0];if (d.getElementById(id)) return;js = d.createElement(s); js.id = id;js.src = \"http://connect.facebook.net/en_US/all.js#xfbml=1&appId=" + APP_KEY + "\";fjs.parentNode.insertBefore(js, fjs);}(document, 'script', 'facebook-jssdk'));</script><div class=\"fb-comments\" data-href=\""
                        + BASE_DOMAIN + "\" data-width=\"470\" data-order-by=\"reverse_time\"></div> </body></html>", "text/html", null, null);

        return view;
    }

    public boolean webViewGoBack(){
        if (myWebView.canGoBack())
        {
            myWebView.goBack();
            return true;
        }
        return false;
    }


}
