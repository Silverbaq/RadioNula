package com.radionula.radionula.fragments;


import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.radionula.radionula.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommentsFragment extends Fragment {

    WebView myWebView;

    public static String APP_KEY = "330582393726734";
    public static String BASE_DOMAIN = "http://radionula.com";


    private WebView webView,childView =null;
    private LinearLayout parentLayout;

    public CommentsFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comments, container, false);


        parentLayout =(LinearLayout)view.findViewById(R.id.parentLayout);


        webView = new WebView(getActivity());
        webView.setLayoutParams(getLayoutParams());

        webView.setWebViewClient(new FaceBookClient());
        webView.setWebChromeClient(new MyChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setSupportMultipleWindows(true);
        //webView.getSettings().setSupportZoom(true);
        //webView.getSettings().setBuiltInZoomControls(true);

        if (Build.VERSION.SDK_INT >= 21) {
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }

        parentLayout.addView(webView);



/*
        myWebView = (WebView) view.findViewById(R.id.fragment_comments_webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);



        myWebView.setWebViewClient(new WebViewClient());

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
*/
        webView.loadDataWithBaseURL(BASE_DOMAIN,
                "<html>" +
                        "<head></head>" +
                        "<body>" +
                        "<div id=\"fb-root\"></div><div id=\"fb-root\"></div>" +
                        "<script>" +
                        "(function(d, s, id) {var js, fjs = d.getElementsByTagName(s)[0];if (d.getElementById(id)) return;js = d.createElement(s); js.id = id;js.src = \"http://connect.facebook.net/en_US/all.js#xfbml=1&appId=" + APP_KEY + "\";fjs.parentNode.insertBefore(js, fjs);}" +
                        "(document, 'script', 'facebook-jssdk'));" +
                        "</script>" +
                        "<div class=\"fb-comments\" data-href=\"" + BASE_DOMAIN + "\" data-width=\"470\" data-order-by=\"reverse_time\" data-colorscheme=\"light\"></div> " +
                        " " +
                        " </body>" +
                        "</html>", "text/html", null, null);

        return view;
    }

    public boolean webViewGoBack() {
        if(childView != null && parentLayout.getChildCount()==2){
            childView.stopLoading();
            parentLayout.removeViewAt(parentLayout.getChildCount()-1);
            if(webView.getVisibility() == View.GONE)
                webView.setVisibility(View.VISIBLE);
            return true;
        }else{
            return false;
        }
    }


    private LinearLayout.LayoutParams getLayoutParams(){
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);
    }


    final class MyChromeClient extends WebChromeClient{
        @Override
        public boolean onCreateWindow(WebView view, boolean dialog,
                                      boolean userGesture, Message resultMsg) {
            childView = new WebView(getActivity());
            childView.getSettings().setJavaScriptEnabled(true);
            //childView.getSettings().setSupportZoom(true);
            //childView.getSettings().setBuiltInZoomControls(false);
            childView.setWebViewClient(new FaceBookClient());
            childView.setWebChromeClient(this);
            childView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT));


            parentLayout.addView(childView);


            childView.requestFocus();
            webView.setVisibility(View.GONE);

          /*I think this is the main part which handles all the log in session*/
            WebView.WebViewTransport transport =(WebView.WebViewTransport)resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }


        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            getActivity().setProgress(newProgress * 100);
        }

        @Override
        public void onCloseWindow(WebView window) {
            parentLayout.removeViewAt(parentLayout.getChildCount()-1);
            childView =null;
            webView.setVisibility(View.VISIBLE);
            webView.requestFocus();
        }
    }

    private class FaceBookClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("REQUEST URL", url);
            return false;
        }
    }


}
