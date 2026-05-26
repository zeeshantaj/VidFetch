package com.example.vidfetch.WorkManager;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

public class DownladVideoWorker extends Worker {

    String KEY_VIDEO_URL;

    public DownladVideoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams, @NonNull String KEY_VIDEO_URL) {
        super(context, workerParams);
        this.KEY_VIDEO_URL = KEY_VIDEO_URL;
    }

    @NonNull
    @Override
    public Result doWork() {
        String videoUrl = getInputData().getString(KEY_VIDEO_URL);

        if (videoUrl == null || videoUrl.isEmpty()) {
            return Result.failure();
        }

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {

            URL url = new URL(videoUrl);

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Result.failure();
            }

            inputStream = connection.getInputStream();

            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
            );

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            String randomId = UUID.randomUUID().toString();
            File outputFile = new File(downloadsDir, randomId+"video.mp4");

            outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();

            return Result.success();

        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();

        } finally {

            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (Exception ignored) {}

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
