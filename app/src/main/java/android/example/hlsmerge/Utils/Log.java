package android.example.hlsmerge.Utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by nizamcs on 21/3/17.
 */
public class Log {
    public static void log(String msg) {
        android.util.Log.d("Nzm",""+msg);
    }


    public static void log(String msg, Boolean writeToFiles) {
        writeToFile(msg + "\n");
        android.util.Log.d("Nzm", "" + msg);

    }

    @TargetApi(Build.VERSION_CODES.O)
    public static void writeToFile(String content) {
        try {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            String fileName = "file_details.txt";
            File file = new File(filePath + "/" + fileName);
            if(file.exists()) file.delete();
            file.createNewFile();
            java.nio.file.Files.write(Paths.get(filePath, fileName), content.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
