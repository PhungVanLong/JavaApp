package vn.edu.usth.stockdashboard.data.sse.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class CryptoSSEService extends Service {

    private static final String CHANNEL_ID = "crypto_sse_channel";
    private static final int NOTIF_ID = 1;
    private OkHttpClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundImmediately();

        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    private void startForegroundImmediately() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Crypto Dashboard")
                .setContentText("Receiving real-time crypto prices")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(NOTIF_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Crypto Price Updates",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Receiving real-time crypto prices");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String symbols = intent.getStringExtra("symbols");

        // SSE connection chạy ở background thread
        new Thread(() -> startSSE(symbols)).start();

        return START_STICKY;
    }

    private void startSSE(String symbols) {
        try {
            URL url = new URL("https://your-sse-endpoint.com/stream?symbols=" + symbols);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    Log.d("CryptoSSEService", "Received: " + data);
                    // TODO: broadcast hoặc cập nhật UI qua LiveData/ViewModel
                }
            }

            reader.close();
            connection.disconnect();
        } catch (Exception e) {
            Log.e("CryptoSSEService", "SSE error", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
