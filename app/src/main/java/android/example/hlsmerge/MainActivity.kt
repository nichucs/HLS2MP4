package android.example.hlsmerge

import android.example.hlsmerge.crypto.PlaylistDownloader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.security.Key
import java.util.*

class MainActivity : AppCompatActivity(), PlaylistDownloader.DownloadListener {
    private lateinit var progressText: TextView
    private lateinit var progressMsg: String
    private lateinit var textBox: EditText
    private lateinit var fab: FloatingActionButton
    private lateinit var cbNewDownloader: CheckBox

    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel =
            ViewModelProvider(this)[MainActivityViewModel::class.java]
        initViews()


        fab.setOnClickListener {
            var playlistUrl = "http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8"
            if (!TextUtils.isEmpty(textBox.text.toString())) playlistUrl =
                textBox.text.toString()
            if (cbNewDownloader.isChecked)
                viewModel.downloadFile(playlistUrl)
            else
                existingDownloader(playlistUrl)
        }

        viewModel.progress.observe(this) {
            updateProgress(it)
        }
    }

    private fun initViews() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        fab = findViewById<View>(R.id.fab) as FloatingActionButton
        progressText = findViewById<View>(R.id.progress) as TextView
        textBox = findViewById<View>(R.id.txt_url) as EditText
        cbNewDownloader = findViewById(R.id.cb_new_downloader)
    }

    private fun existingDownloader(playlistUrl: String) {
        val downloader =
            PlaylistDownloader(playlistUrl, this);
        downloader.download(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/outputHLS" + Random().nextInt() + ".mp4");
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (!BuildConfig.DEBUG) return super.onKeyUp(keyCode, event)
        var url = ""
        when (keyCode) {
            KeyEvent.KEYCODE_1 -> url =
                "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
            KeyEvent.KEYCODE_2 -> url =
                "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"
            KeyEvent.KEYCODE_3 -> url =
                "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.mp4/.m3u8"
            KeyEvent.KEYCODE_4 -> url =
                "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"
            KeyEvent.KEYCODE_5 -> url =
                "https://moctobpltc-i.akamaihd.net/hls/live/571329/eight/playlist.m3u8"
            KeyEvent.KEYCODE_6 -> {
                //this url returns a m3u8 file and when you visit the maximum resolution link,
                //which contains another m3u8 file
                //That file contains tag #EXT-X-PLAYLIST-TYPE:VDO & #EXT-X-INDEPENDENT-SEGMENTS
                //Which I though would make them different from providing link to same resources

                //So I need to change my strategy as to look for #EXT-X-BYTE-RANGE
                //if a file contains that, it means all the URL should point to same resource



                url =
                    "https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_stereo_subs.m3u8"
            }
            KeyEvent.KEYCODE_7 -> url =
                "https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_multichannel_subs.m3u8"
            KeyEvent.KEYCODE_8 -> url =
                "https://d3rlna7iyyu8wu.cloudfront.net/skip_armstrong/skip_armstrong_multi_language_subs.m3u8"
            KeyEvent.KEYCODE_9 -> url =
                "http://amssamples.streaming.mediaservices.windows.net/91492735-c523-432b-ba01-faba6c2206a2/AzureMediaServicesPromo.ism/manifest(format=m3u8-aapl)"
            KeyEvent.KEYCODE_Q -> url =
                "https://ottverse.com/free-hls-m3u8-test-urls/#:~:text=http%3A//amssamples.streaming.mediaservices.windows.net/69fbaeba%2D8e92%2D4740%2Daedc%2Dce09ae945073/AzurePromo.ism/manifest(format%3Dm3u8%2Daapl)"
            KeyEvent.KEYCODE_W -> url =
                "https://ottverse.com/free-hls-m3u8-test-urls/#:~:text=http%3A//amssamples.streaming.mediaservices.windows.net/634cd01c%2D6822%2D4630%2D8444%2D8dd6279f94c6/CaminandesLlamaDrama4K.ism/manifest(format%3Dm3u8%2Daapl)"
        }
        textBox.setText(url)
        fab.performClick()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onProgressUpdate(progress: Int) {
        updateProgress(progress.toString())
    }

    private fun updateProgress(progress: String) {
        var text = progressText!!.text.toString()
        if (!text.contains("~Progress:")) {
            progressText!!.append("\n~Progress:$progress%")
        } else {
            text = text.substring(0, text.lastIndexOf("~Progress:"))
            text += "~Progress:$progress%"
            progressText!!.text = text
        }
    }

    override fun onStartDownload(url: String) {
        progressMsg = "\nDownloading from $url"
        progressText!!.text = progressMsg
    }
}