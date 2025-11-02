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

        val htmlContent = intent.getStringExtra("html_content")
        val cssContent = intent.getStringExtra("css_content")
        val jsContent = intent.getStringExtra("js_content")

        binding.webView.addJavascriptInterface(WebAppInterface(this), "Android")

        val fullHtml = "<html><head><style>$cssContent</style></head><body>$htmlContent<script>$jsContent</script></body></html>"
        binding.webView.loadData(fullHtml, "text/html", "UTF-8")
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
