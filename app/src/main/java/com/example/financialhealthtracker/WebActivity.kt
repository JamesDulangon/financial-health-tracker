package com.example.financialhealthtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_webview.*
import android.app.AlertDialog
import android.widget.Toast
import android.webkit.WebViewClient

class WebActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://www.calcxml.com/calculators/retirement-calculator")
    }

    override fun onBackPressed() {
        if(webView!!.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}