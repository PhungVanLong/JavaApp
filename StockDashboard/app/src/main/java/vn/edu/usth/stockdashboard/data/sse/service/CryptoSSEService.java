package vn.edu.usth.stockdashboard.data.sse.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.content.pm.ServiceInfo;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class CryptoSSEService extends Service {
    private static final String TAG = "CryptoSSEService";
    private static final String CHANNEL_ID = "crypto_sse_channel";
    private static final int NOTIF_ID = 1;
    private Thread sseThread;
    private volatile boolean isRunning = false;
    private OkHttpClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundImmediately();
        Log.d(TAG, "✅ Service created");

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
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIF_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIF_ID, notification);
        }

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
        if (intent == null) return START_STICKY;
        String symbols = intent.getStringExtra("symbols");

        // SSE connection chạy ở background thread
        stopSSEThread();

        // Khởi động thread mới
        isRunning = true;
        sseThread = new Thread(() -> startSSE(symbols));
        sseThread.start();

        return START_STICKY;
    }

    private void startSSE(String symbols) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL("https://your-sse-endpoint.com/stream?symbols=" + symbols);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(0); // Infinite read timeout for SSE
            connection.connect();
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "✅ SSE Connected! Response code: " + responseCode);

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder dataBuilder = new StringBuilder();
            int eventCount = 0;

            while (isRunning && (line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    dataBuilder.append(line.substring(5).trim());
                } else if (line.isEmpty() && dataBuilder.length() > 0) {
                    // ✅ PARSE JSON VÀ BROADCAST
                    try {
                        String jsonString = dataBuilder.toString();
                        JSONObject json = new JSONObject(jsonString);

                        String symbol = json.getString("symbol");
                        double price = json.getDouble("price");
                        double open = json.optDouble("open", price);
                        double changePercent = json.optDouble("change_percent", 0.0);
                        long timestamp = json.optLong("timestamp", System.currentTimeMillis() / 1000);

                        // ✅ BROADCAST DATA
                        Intent broadcastIntent = new Intent("CRYPTO_UPDATE");
                        broadcastIntent.putExtra("symbol", symbol);
                        broadcastIntent.putExtra("price", price);
                        broadcastIntent.putExtra("open", open);
                        broadcastIntent.putExtra("change_percent", changePercent);
                        broadcastIntent.putExtra("timestamp", timestamp);

                        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                        eventCount++;
                        if (eventCount % 20 == 0) {
                            Log.d(TAG, "📊 Event #" + eventCount + " | " + symbol + " = $" + price);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error parsing JSON: " + dataBuilder.toString(), e);
                    }

                    dataBuilder.setLength(0);
                }
            }

            Log.d(TAG, "⚠️ SSE loop ended");

        } catch (Exception e) {
            if (isRunning) {
                Log.e(TAG, "❌ SSE error", e);
            }
        } finally {
            try {
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception ignored) {}

            Log.d(TAG, "🔌 SSE connection closed");
        }
    }

    private void stopSSEThread() {
        isRunning = false;
        if (sseThread != null) {
            sseThread.interrupt();
            try {
                sseThread.join(1000); // Chờ tối đa 1s
            } catch (InterruptedException e) {
                Log.w(TAG, "Thread interrupted while stopping");
            }
            sseThread = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSSEThread();
        Log.d(TAG, "💥 Service destroyed");
    }
}