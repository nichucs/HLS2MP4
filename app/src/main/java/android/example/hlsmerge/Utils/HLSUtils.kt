package android.example.hlsmerge.Utils

import java.net.MalformedURLException
import java.net.URL

class HLSUtils {


    companion object {
         fun getBaseUrl(url: URL): String {
            val urlString = url.toString()
            var index = urlString.lastIndexOf('/')
            return urlString.substring(0, ++index)
        }

        fun updateUrlForSubPlaylist(subUrl: String, mainUrl: String): URL? {
            var aUrl: URL? = null
            val newUrl: String = if (!subUrl.startsWith("http")) {
                getBaseUrl(URL(mainUrl)) + subUrl
            } else {
                subUrl
            }
            try {
                aUrl = URL(newUrl)
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
            return aUrl
        }

    }
}