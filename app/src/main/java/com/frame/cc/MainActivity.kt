package com.frame.cc

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.Window
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var weburl = "https://app.51cao7.com/"
    private var errorPage = "file:///android_asset/error.html";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout)
        // Get the system color (colorPrimary) programmatically
//        val systemColor = getSystemColor(this, androidx.appcompat.R.attr.colorPrimary)
        setStatusBarColor(R.color.black)
        swipeRefreshLayout = findViewById(R.id.webSwipe)
        webView = findViewById(R.id.webView)

        // Show dialog if no internet

        // Handle the case where there's no internet connection
        // Create a dialog
        val builder = AlertDialog.Builder(this)

        // Set the dialog title and message
        builder.setTitle("Connection")
        builder.setMessage("No internet connection")

        // Set a positive button
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle the button click (if needed)
            dialog.dismiss() // Close the dialog
        }
        builder.setNeutralButton("Setting") { dialog, which ->
            val dialogIntent = Intent(Settings.ACTION_SETTINGS)
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(dialogIntent)
        }
        // Create and show the dialog
        val dialog = builder.create()
        var toast = Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT)

        // Check for internet connectivity before loading a URL
        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        webView.webViewClient = WebViewClient()
        // this will enable the javascript settings, it can also allow xss vulnerabilities
        webView.settings.javaScriptEnabled = true
        // if you want to enable zoom feature
        webView.settings.setSupportZoom(true)
        webView.webViewClient = MyWebViewClient()

        if (isNetworkAvailable()) {
            // this will load the url of the website
            webView.loadUrl(weburl)
        } else {
            dialog.show()
            toast.show()
            webView.loadUrl(errorPage);
        }
        swipeRefreshLayout.setOnRefreshListener{
            webView.reload();
        }
    }
    private fun setStatusBarColor(@ColorRes colorResId: Int) {
        // Check if the device is running Android 5.0 Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Get the Window object of the activity
            val window: Window = window

            // Set the status bar color using a color resource
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, colorResId)
        }
    }

    private fun getSystemColor(context: Context, colorAttr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }

    private inner class MyWebViewClient: WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            Log.i("onPageStarted", url.toString())
        }
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            swipeRefreshLayout.isRefreshing = false
        }
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return super.shouldOverrideUrlLoading(view, request)
        }
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError
        ) {
            // Check for ERR_NAME_NOT_RESOLVED
            if (error.errorCode == ERROR_HOST_LOOKUP) {
                webView.loadUrl(errorPage);
            }
            if(isNetworkAvailable()){
                webView.loadUrl(weburl);
                webView.canGoBack();
            }
        }
        override fun onReceivedHttpError(
            view: WebView?,
            request: WebResourceRequest?,
            errorResponse: WebResourceResponse?
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (errorResponse != null) {
                webView.loadUrl(errorPage);
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // if you press Back button this code will work
    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (webView.canGoBack()) {
            webView.goBack()
            // if your webview cannot go back
            // it will exit the application
        }else {
            super.onBackPressedDispatcher
            val builder = AlertDialog.Builder(this)

            // Set the dialog title and message
            builder.setTitle("Alert")
            builder.setIcon(R.drawable.baseline_exit_to_app_24)
            builder.setMessage("你确定要离开？")

            // Set a positive button
            builder.setPositiveButton("Yes") { dialog, which ->
                dialog.dismiss() // Close the dialog
                finish();
            }
            builder.setNeutralButton("No") { dialog, which ->
                dialog.dismiss();
            }
            // Create and show the dialog
            val dialog = builder.create()
            dialog.show()
        }
    }

}