package com.winnyking.wincalendar.tasker.htmlviewer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import com.winnyking.wincalendar.databinding.ActivityWebViewBinding

class WebViewActivity : Activity() {

    companion object {
        const val ACTION_FINISH = "com.winnyking.wincalendar.action.FINISH"
        const val EXTRA_VARIABLE_VALUES = "com.winnyking.wincalendar.extra.VARIABLE_VALUES"
    }

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()

        val html = intent.getStringExtra("html")

        binding.webView.addJavascriptInterface(WebAppInterface(this), "Android")

        binding.webView.loadDataWithBaseURL(null, html ?: "", "text/html", "UTF-8", null)
    }

    inner class WebAppInterface(private val activity: Activity) {
        @JavascriptInterface
        fun closeWebView(values: String) {
            val intent = Intent(ACTION_FINISH).apply {
                putExtra(EXTRA_VARIABLE_VALUES, values)
            }
            sendBroadcast(intent)
            activity.finish()
        }
    }
}
