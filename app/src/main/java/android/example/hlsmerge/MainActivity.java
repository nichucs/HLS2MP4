package android.example.hlsmerge;

import android.example.hlsmerge.crypto.PlaylistDownloader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements PlaylistDownloader.DownloadListener {

    TextView progressText;
    String progressMsg;
    EditText textBox;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        progressText = (TextView) findViewById(R.id.progress);
        textBox = (EditText) findViewById(R.id.txt_url);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textBox.clearFocus();
            }
        }, 500);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String playlistUrl = "http://184.72.239.149/vod/smil:BigBuckBunny.smil/playlist.m3u8";
                    if (!TextUtils.isEmpty(textBox.getText().toString()))
                        playlistUrl = textBox.getText().toString();
                    PlaylistDownloader downloader =
                            new PlaylistDownloader(playlistUrl, MainActivity.this);
                    downloader.download(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/outputHLS" + new Random().nextInt() + ".mp4");
                } catch (java.io.IOException e) {
                    Snackbar.make(view, "Url/path not correct", Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!BuildConfig.DEBUG) return super.onKeyUp(keyCode, event);
        String url = "";
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                url = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8";
                break;
            case KeyEvent.KEYCODE_2:
                url = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8";
                break;
        }

        textBox.setText(url);
        fab.performClick();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressUpdate(int progress) {
        String text = progressText.getText().toString();
        if (!text.contains("~Progress:")) {
            progressText.append("\n~Progress:" + progress + "%");
        } else {
            text = text.substring(0, text.lastIndexOf("~Progress:"));
            text += "~Progress:" + progress + "%";
            progressText.setText(text);
        }
    }

    @Override
    public void onStartDownload(String url) {
        progressMsg = "\nDownloading from " + url;
        progressText.setText(progressMsg);
    }
}
