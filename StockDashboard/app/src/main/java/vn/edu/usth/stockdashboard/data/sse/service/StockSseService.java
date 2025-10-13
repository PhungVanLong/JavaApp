package vn.edu.usth.stockdashboard.data.sse.service;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import vn.edu.usth.stockdashboard.data.sse.StockSymbolData;

public class StockSseService {
    private static final String TAG = "StockSseService";

    // Enum to manage connection state
    private enum State { IDLE, CONNECTING, CONNECTED }
    private volatile State currentState = State.IDLE;

    private final OkHttpClient client;
    private EventSource eventSource;
    private final String BASE_SSE_URL = "https://sse-stock-iqeg.onrender.com/api/stock/stream?symbols=";
    private final List<String> symbols;
    private final Gson gson = new Gson();
    private SseUpdateListener listener;

    public interface SseUpdateListener {
        void onStockUpdate(List<StockSymbolData> stockList);
        void onOpen();
        void onClose();
        void onFailure(String error);
    }

    public StockSseService(List<String> symbols, SseUpdateListener listener) {
        this.symbols = symbols;
        this.listener = listener;
        this.client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void connect() {
        synchronized (this) {
            if (currentState != State.IDLE) {
                Log.d(TAG, "Connect called but already in state: " + currentState);
                return;
            }
            currentState = State.CONNECTING;
        }
        String fullUrl = BASE_SSE_URL + String.join(",", symbols);
        Request request = new Request.Builder().url(fullUrl).build();
        eventSource = EventSources.createFactory(client).newEventSource(request, sseListener);
        Log.d(TAG, "SSE Connecting...");
    }

    public void disconnect() {
        synchronized (this) {
            if (currentState == State.IDLE) {
                Log.d(TAG, "Disconnect called but already idle.");
                return;
            }
            if (eventSource != null) {
                eventSource.cancel();
                eventSource = null;
            }
            currentState = State.IDLE;
        }
        Log.d(TAG, "SSE Disconnected.");
    }

    private final EventSourceListener sseListener = new EventSourceListener() {
        @Override
        public void onOpen(@NonNull EventSource eventSource, @NonNull Response response) {
            currentState = State.CONNECTED;
            if (listener != null) listener.onOpen();
        }

        @Override
        public void onEvent(@NonNull EventSource eventSource, @Nullable String id, @Nullable String type, @NonNull String data) {
            Log.d(TAG, "RAW SSE DATA RECEIVED: " + data); // Log to see the data
            try {
                Type topLevelType = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> topLevelMap = gson.fromJson(data, topLevelType);
                Map<String, Object> innerDataMap = (Map<String, Object>) topLevelMap.get("data");
                if (innerDataMap == null) return;
                List<StockSymbolData> stockList = new ArrayList<>();
                for (Object stockJson : innerDataMap.values()) {
                    String stockJsonString = gson.toJson(stockJson);
                    StockSymbolData stockSymbol = gson.fromJson(stockJsonString, StockSymbolData.class);
                    stockList.add(stockSymbol);
                }
                if (listener != null && !stockList.isEmpty()) {
                    listener.onStockUpdate(stockList);
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "Error parsing batch SSE data: " + data, e);
            }
        }

        @Override
        public void onClosed(@NonNull EventSource eventSource) {
            currentState = State.IDLE;
            if (listener != null) listener.onClose();
        }

        @Override
        public void onFailure(@NonNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
            currentState = State.IDLE;
            String error = (t != null) ? t.getMessage() : "Unknown SSE error";
            if (!"Canceled".equalsIgnoreCase(error)) {
                Log.e(TAG, "SSE Connection Failure!", t);
                if (listener != null) listener.onFailure(error);
            }
        }
    };
}