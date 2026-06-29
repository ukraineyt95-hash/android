package com.zaneschepke.wireguardautotunnel.ui.screens.tunnels.warp

import android.annotation.SuppressLint
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dokar.sonner.ToastType
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.LocalNavController
import com.zaneschepke.wireguardautotunnel.util.StringValue
import com.zaneschepke.wireguardautotunnel.viewmodel.SharedAppViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinActivityViewModel
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WarpImportScreen(sharedViewModel: SharedAppViewModel = koinActivityViewModel()) {
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    fun importConfig(content: String) {
        sharedViewModel.importFromClipboard(content)
        navController.pop()
    }

    fun fetchAndImport(downloadUrl: String, cookies: String?) {
        scope.launch(Dispatchers.IO) {
            try {
                if (downloadUrl.startsWith("data:")) {
                    val encoded = downloadUrl.substringAfter(",")
                    val bytes = Base64.decode(encoded, Base64.DEFAULT)
                    val content = String(bytes, Charsets.UTF_8)
                    withContext(Dispatchers.Main) { importConfig(content) }
                } else {
                    val connection = URL(downloadUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    if (!cookies.isNullOrEmpty()) {
                        connection.setRequestProperty("Cookie", cookies)
                    }
                    connection.connect()
                    val content = connection.inputStream.bufferedReader().readText()
                    withContext(Dispatchers.Main) { importConfig(content) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    sharedViewModel.showSnackMessage(
                        StringValue.StringResource(R.string.error_download_failed),
                        ToastType.Error,
                    )
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            isLoading = false
                        }
                    }
                    setDownloadListener(
                        DownloadListener { url, _, _, mimetype, _ ->
                            val cookies = CookieManager.getInstance().getCookie(url)
                            if (
                                url.endsWith(".conf") ||
                                    mimetype.contains("octet-stream") ||
                                    mimetype.contains("text/plain") ||
                                    url.startsWith("data:")
                            ) {
                                fetchAndImport(url, cookies)
                            } else {
                                fetchAndImport(url, cookies)
                            }
                        }
                    )
                    loadUrl("https://warpgen.net/")
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
