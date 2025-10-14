package vn.edu.usth.stockdashboard.data.sse.service;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import vn.edu.usth.stockdashboard.data.sse.StockData;

public class StockSseService {
    private static final String TAG = "StockSseService";

    // Enum to manage connection state (Solves lifecycle race condition)
    private enum State { IDLE, CONNECTING, CONNECTED }
    private volatile State currentState = State.IDLE;

    private final OkHttpClient client;
    private EventSource eventSource;
    private final String BASE_SSE_URL = "https://sse-stock-iqeg.onrender.com/stream-prices?symbols=";
    private final List<String> symbols;
    private final Gson gson = new Gson();
    private SseUpdateListener listener;

    public interface SseUpdateListener {
        void onStockUpdate(Map<String, StockData> stockDataMap);
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
            if (currentState != State.IDLE) return;
            currentState = State.CONNECTING;
        }
        String fullUrl = BASE_SSE_URL + String.join(",", symbols);
        Request request = new Request.Builder().url(fullUrl).build();
        eventSource = EventSources.createFactory(client).newEventSource(request, sseListener);
    }

    public void disconnect() {
        synchronized (this) {
            if (currentState == State.IDLE) return;
            if (eventSource != null) eventSource.cancel();
            eventSource = null;
            currentState = State.IDLE;
        }
    }

    private final EventSourceListener sseListener = new EventSourceListener() {
        @Override
        public void onOpen(@NonNull EventSource eventSource, @NonNull Response response) {
            currentState = State.CONNECTED;
            if (listener != null) listener.onOpen();
        }

        @Override
        public void onEvent(@NonNull EventSource eventSource, @Nullable String id, @Nullable String type, @NonNull String data) {
            Log.d(TAG, "RAW SSE DATA RECEIVED: " + data);

            // *** ROBUST PARSING LOGIC TO PREVENT CRASHES ***
            try {
                // Try to parse the expected data format
                Type mapType = new TypeToken<Map<String, StockData>>() {}.getType();
                Map<String, StockData> stockDataMap = gson.fromJson(data, mapType);

                if (listener != null && stockDataMap != null && !stockDataMap.isEmpty()) {
                    listener.onStockUpdate(stockDataMap);
                }
            } catch (JsonSyntaxException e) {
                // If standard parsing fails, check if it's a server error message
                try {
                    Type errorType = new TypeToken<Map<String, String>>() {}.getType();
                    Map<String, String> errorMap = gson.fromJson(data, errorType);
                    if (errorMap != null && errorMap.containsKey("error")) {
                        String errorMessage = errorMap.get("error");
                        Log.e(TAG, "Server returned an error: " + errorMessage);
                        // Send the server's error message to the UI
                        if (listener != null) listener.onFailure(errorMessage);
                    } else {
                        // It's a different kind of parsing error
                        Log.e(TAG, "Unhandled JSON syntax error: " + data, e);
                    }
                } catch (JsonSyntaxException e2) {
                    // The data is not a valid JSON at all
                    Log.e(TAG, "Received non-JSON data: " + data, e2);
                }
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
            // Gracefully handle the "Canceled" exception from lifecycle events
            if (!"Canceled".equalsIgnoreCase(error)) {
                if (listener != null) listener.onFailure(error);
            }
        }
    };
}