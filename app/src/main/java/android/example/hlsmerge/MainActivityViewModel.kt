package android.example.hlsmerge

import android.app.Application
import android.example.hlsmerge.Utils.HLSUtils
import android.example.hlsmerge.Utils.Log
import android.example.hlsmerge.crypto.Crypto
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Downloader
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

private const val BANDWIDTH = "BANDWIDTH"
private const val EXT_X_KEY = "#EXT-X-KEY"
private const val EXT_X_PLAYLIST_TYPE = "#EXT-X-PLAYLIST-TYPE:VOD"
private const val EXT_X_BYTE_RANGE = "#EXT-X-BYTE-RANGE"
private const val EXT_X_INDEPENDENT_SEGMENTS = "#EXT-X-INDEPENDENT-SEGMENTS"
private const val TAG = "MainActivityViewModel"

class MainActivityViewModel(private val app: Application) : AndroidViewModel(app) {

    private val _progressLiveData = MutableLiveData<String>()
    val progress: LiveData<String> = _progressLiveData;

    val s = androidx.lifecycle.viewmodel.viewModelFactory { }
    fun downloadFile(address: String) =
        viewModelScope.launch(IO) {
            val url = URL(address)
            val outputFileAddress =
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path}/outputHLS${Random().nextInt()}.mp4"
            val outputFile = File(outputFileAddress)

            _progressLiveData.postValue("Getting master play list...")
            val maximumResolutionPlayList = getMasterPlaylist(mainUrl = url, subUrl = url)
            val crypto = Crypto(HLSUtils.getBaseUrl(url), null)
            android.util.Log.i(TAG, "downloadFile: downloading: ${maximumResolutionPlayList.first}")
//            var containsPlaylistTypeVOD = false
//            var containsIndependentSegments = false


            //it means that each of the segment basically points to the same resource
            //and you can download in chunks by reading the #EXT-X-BYTE-RANGE and providing the range in
            //in http-header or you can provide your own range as well
            var isPointingToSameFile = false
            var urlPointingToSingleResource = ""

            maximumResolutionPlayList.second.forEach { item ->
//                if (item.startsWith(EXT_X_PLAYLIST_TYPE)) {
//                    containsPlaylistTypeVOD = true
//                }
//
//                if (item.startsWith(EXT_X_INDEPENDENT_SEGMENTS)) {
//                    containsIndependentSegments = true
//                }

                //This algo can be further improved by checking if the all the sub file point to same file

                if (item.contains(EXT_X_BYTE_RANGE)) isPointingToSameFile = true

                if (isPointingToSameFile) {

                    if (!item.startsWith("#")) {
                        urlPointingToSingleResource =
                            if (item.startsWith("http")) item else {
                                HLSUtils.getBaseUrl(maximumResolutionPlayList.first ?: url) + item
                            }
                    }

                    isPointingToSameFile = true
                    return@forEach
                }
            }


            if (isPointingToSameFile) {
                //Download using HTTPUrlConnection
                try {
                    val connection =
                        URL(urlPointingToSingleResource).openConnection() as HttpURLConnection
                    connection.connect()

                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val fileLength = connection.contentLength
                        val outputStream = FileOutputStream(outputFile)
                        val inputStream = connection.inputStream
                        val buffer = ByteArray(4096)
                        var totalBytesRead = 0L
                        var bytesRead: Int
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            val progress = (totalBytesRead * 100L) / fileLength
                            _progressLiveData.postValue("$progress")
                        }

                        inputStream.close()
                        outputStream.close()
                        connection.disconnect()

                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else

                maximumResolutionPlayList.second.forEachIndexed { index, item ->
                    val line = item.trim()
                    android.util.Log.i(TAG, "downloadFile: LINE: $line")
                    if (line.startsWith(EXT_X_KEY)) {
                        crypto.updateKeyString(line)
                    } else if (line.isNotEmpty() && !line.startsWith("#")) {
                        val segmentUrl = if (!line.startsWith("http")) {
                            val baseUrl =
                                HLSUtils.getBaseUrl(maximumResolutionPlayList.first ?: url)
                            android.util.Log.i(TAG, "downloadFile: base URL: $baseUrl")
                            URL(baseUrl + line)
                        } else URL(line)

                        downloadFile(segmentUrl, outputFile, crypto)
                        val progress = index * 100 / maximumResolutionPlayList.second.size
                        _progressLiveData.postValue("$progress")

                    }
                }


        }

    private fun downloadFile(url: URL, outputFile: File, crypto: Crypto) {
        val buffer = ByteArray(512)
        android.util.Log.i(TAG, "downloadFile: downloading: $url")
        try {
            val inputStream = if (crypto.hasKey()) {
                crypto.wrapInputStream(url.openStream())
            } else url.openStream()

            if (!outputFile.exists()) outputFile.createNewFile()
            val outputStream = FileOutputStream(outputFile, true)

            var read: Int?
            while ((inputStream.read(buffer).also { read = it } >= 0)) {
                read?.let { outputStream.write(buffer, 0, it) }
            }
            inputStream.close()
            outputStream.close()


        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    private fun getMasterPlaylist(
        mainUrl: URL?, //So that it can be passed to recursion to make new subURL
        subUrl: URL?,
        playlist: MutableList<String> = mutableListOf(),
    ): Pair<URL?, List<String>> {


        if (subUrl == null) throw java.lang.Exception("Sub URL can not be null")

        var isMaster = false
        var maxRate = 0L
        var maxRateIndex = 0
        try {
            val reader = BufferedReader(InputStreamReader(subUrl.openStream()))
            var line: String? = ""
            var index = 0;

            while (reader.readLine().also { line = it } != null) {
                line?.let { l ->

                    playlist.add(l)
                    Log.log(l, true)

                    if (l.contains(BANDWIDTH))
                        isMaster = true

                    if (isMaster && l.contains(BANDWIDTH)) {
                        try {
                            val pos = l.lastIndexOf("$BANDWIDTH=") + 10
                            var end = l.indexOf(",", pos)
                            if (end < 0 || end < pos) end = l.length - 1
                            val bandwidth = l.subSequence(pos, end).toString().toLong()

                            maxRate = Math.max(bandwidth, maxRate)

                            if (bandwidth == maxRate) maxRateIndex = index + 1

                        } catch (e: java.lang.NumberFormatException) {
                            e.printStackTrace()
                        }
                    }

                    index++

                }
            }

            reader.close()

        } catch (e: Exception) {
            _progressLiveData.postValue("ERROR: ${e.message}")
            e.printStackTrace()
        }



        return if (isMaster) {
            val newMainUrl = HLSUtils.updateUrlForSubPlaylist(
                playlist[maxRateIndex],
                mainUrl.toString()
            )
            getMasterPlaylist(
                mainUrl = newMainUrl,
                subUrl = newMainUrl,
                playlist = arrayListOf()

            )
        } else Pair(first = mainUrl, playlist)

    }


}