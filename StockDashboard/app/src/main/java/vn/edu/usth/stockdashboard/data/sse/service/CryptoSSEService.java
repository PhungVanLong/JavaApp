package vn.edu.usth.stockdashboard.data.sse.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CryptoSSEService extends Service {
    private static final String TAG = "CryptoSSEService";
    private OkHttpClient client;
    private Call call;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String symbols = intent != null ? intent.getStringExtra("symbols") :
                "btcusdt,ethusdt,bnbusdt,adausdt,xrpusdt,solusdt,dotusdt,avxusdt,ltcusdt,linkusdt,maticusdt,uniusdt,atomusdt,trxusdt,aptusdt,filusdt,nearusdt,icpusdt,vetusdt";

        String sseUrl = "https://crypto-server-xqv5.onrender.com/events?symbols=" + symbols;
        Log.d(TAG, "Connecting to: " + sseUrl);

        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(sseUrl)
                .build();

        call = client.newCall(request);

        new Thread(() -> {
            try (Response response = call.execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    var source = response.body().source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line != null && line.startsWith("data:")) {
                            String json = line.substring(5).trim();
                            JSONObject obj = new JSONObject(json);

                            Intent update = new Intent("CRYPTO_UPDATE");
                            update.putExtra("symbol", obj.getString("symbol"));
                            update.putExtra("price", obj.getDouble("price"));
                            update.putExtra("timestamp", obj.getLong("timestamp"));
                            sendBroadcast(update);
                        }
                    }
                } else {
                    Log.e(TAG, "SSE connection failed: " + response);
                }
            } catch (Exception e) {
                Log.e(TAG, "SSE error", e);
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null && !call.isCanceled()) call.cancel();
        Log.d(TAG, "CryptoSSEService stopped");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
