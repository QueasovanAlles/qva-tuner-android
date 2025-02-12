package com.qva.qvatuner

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            //domStorageEnabled = true
            //allowContentAccess = true
            //setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)
            //defaultTextEncodingName = "utf-8"
            //setMediaPlaybackRequiresUserGesture(false)
            //setDomStorageEnabled(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                val uri = request.url
                if (uri.path?.endsWith(".js") == true) {
                    try {
                        val lastSegment = uri.lastPathSegment!!
                        val input = assets.open("www/${lastSegment}")
                        return WebResourceResponse("application/javascript", "UTF-8", input)
                    } catch (e: Exception) {
                        Log.d("WebView", "Error loading: ${uri}, ${e.message}")
                    }
                }
                if (uri.path?.endsWith(".png") == true) {
                    try {
                        val input = assets.open("www/${uri.path!!.removePrefix("/")}")
                        return WebResourceResponse("image/png", "UTF-8", input)
                    } catch (e: Exception) {
                        Log.d("WebView", "Error loading image: ${uri}")
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }

        // WebChromeClient for permissions
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: android.webkit.PermissionRequest) {
                request.grant(request.resources)
            }
        }

        WebView.setWebContentsDebuggingEnabled(true)

        requestAudioPermissions()
        Log.d("Permissions", "Microphone permission: ${checkMicrophonePermission()}")

        webView.loadUrl("file:///android_asset/www/index.html")

    }

    private fun checkMicrophonePermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermissions() {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                123
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, initialize audio
            webView.reload()
        }
    }
}