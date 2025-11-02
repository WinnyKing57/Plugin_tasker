package com.winnyking.wincalendar.tasker.htmlviewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.winnyking.wincalendar.R;

public class WebViewActivity extends Activity {

    public static final String ACTION_FINISH = "com.winnyking.wincalendar.action.FINISH";
    public static final String EXTRA_VARIABLE_VALUES = "com.winnyking.wincalendar.extra.VARIABLE_VALUES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        Intent intent = getIntent();
        String htmlContent = intent.getStringExtra("html_content");
        String cssContent = intent.getStringExtra("css_content");
        String jsContent = intent.getStringExtra("js_content");
        String taskerVariables = intent.getStringExtra("tasker_variables");

        webView.addJavascriptInterface(new WebAppInterface(this, taskerVariables), "Android");

        String fullHtml = "<html><head><style>" + cssContent + "</style></head><body>" + htmlContent + "<script>" + jsContent + "</script></body></html>";
        webView.loadData(fullHtml, "text/html", "UTF-8");
    }

    public class WebAppInterface {
        Activity mActivity;
        String mTaskerVariables;

        WebAppInterface(Activity activity, String taskerVariables) {
            mActivity = activity;
            mTaskerVariables = taskerVariables;
        }

        @JavascriptInterface
        public void closeWebView(String values) {
            Intent intent = new Intent(ACTION_FINISH);
            intent.putExtra(EXTRA_VARIABLE_VALUES, values);
            sendBroadcast(intent);
            mActivity.finish();
        }
    }
}
