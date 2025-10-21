package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.SolidFill;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChartActivity extends BaseActivity {

    private static final String TAG = "ChartActivity";
    private static final int MSG_SHOW_CHART = 1;
    private static final int MSG_SHOW_ERROR = 2;
    private static final int MSG_SHOW_LOADING = 3;

    private AnyChartView anyChartView;
    private TextView titleText;
    private Button btnRefresh;

    private String stockSymbol;
    private OkHttpClient client;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        anyChartView = findViewById(R.id.any_chart_view);
        titleText = findViewById(R.id.titleText);
        btnRefresh = findViewById(R.id.btn_refresh);

        // Khởi tạo Handler với Main Looper
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                if (msg.what == MSG_SHOW_CHART) {
                    Bundle data = msg.getData();
                    List<DataEntry> seriesData = (List<DataEntry>) data.getSerializable("seriesData");
                    double lastClose = data.getDouble("lastClose");
                    showChart(seriesData, lastClose);

                } else if (msg.what == MSG_SHOW_ERROR) {
                    String errorMsg = msg.getData().getString("error");
                    showErrorChart(errorMsg);
                    Toast.makeText(ChartActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                } else if (msg.what == MSG_SHOW_LOADING) {
                    showLoadingChart();
                }
            }
        };

        client = new OkHttpClient();

        // Lấy mã chứng chỉ từ Intent
        stockSymbol = getIntent().getStringExtra("STOCK_SYMBOL");
        if (stockSymbol == null || stockSymbol.isEmpty()) {
            stockSymbol = "VNI";
        }

        Log.d(TAG, "Stock Symbol: " + stockSymbol);

        showLoadingChart();

        btnRefresh.setOnClickListener(v -> {
            Log.d(TAG, "Refresh button clicked");
            fetchStockData();
        });

        fetchStockData();
    }

    private void showLoadingChart() {
        Cartesian loadingChart = AnyChart.line();
        loadingChart.background().fill("#0e1117");
        loadingChart.title("Loading stock data for " + stockSymbol + "...");
        anyChartView.setChart(loadingChart);
    }

    private void fetchStockData() {
        String url = "https://vn-stock-api-bsjj.onrender.com/api/stock/"
                + stockSymbol.toLowerCase() + "/history";

        Log.d(TAG, " Fetching data from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, " Request failed: " + e.getMessage());

                Message msg = Message.obtain();
                msg.what = MSG_SHOW_ERROR;
                Bundle data = new Bundle();
                data.putString("error", "Connection Error: " + e.getMessage());
                msg.setData(data);
                uiHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, " API Error: " + response.code());

                    Message msg = Message.obtain();
                    msg.what = MSG_SHOW_ERROR;
                    Bundle data = new Bundle();
                    data.putString("error", "API Error: " + response.code());
                    msg.setData(data);
                    uiHandler.sendMessage(msg);
                    response.close();
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    Log.d(TAG, " Response received: " + jsonData.substring(0, Math.min(200, jsonData.length())));

                    JSONObject jsonObject = new JSONObject(jsonData);

                    if (!jsonObject.has("data")) {
                        Log.e(TAG, " No 'data' field in response");
                        sendErrorMessage("No data available");
                        return;
                    }

                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    if (dataArray.length() == 0) {
                        Log.e(TAG, " Data array is empty");
                        sendErrorMessage("Data is empty");
                        return;
                    }

                    List<DataEntry> seriesData = new ArrayList<>();
                    double lastClose = 0;

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);

                        try {
                            String time = item.getString("time");
                            if (time.length() >= 10) {
                                time = time.substring(5, 10).replace("-", " ");
                                String[] parts = time.split(" ");
                                if (parts.length == 2) {
                                    String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                                    int month = Integer.parseInt(parts[0]) - 1;
                                    time = parts[1] + " " + (month >= 0 && month < 12 ? months[month] : "");
                                }
                            }

                            double close = item.getDouble("close");
                            lastClose = close;

                            seriesData.add(new ValueDataEntry(time, close));
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing item " + i + ": " + e.getMessage());
                        }
                    }

                    if (seriesData.isEmpty()) {
                        Log.e(TAG, " No valid data entries parsed");
                        sendErrorMessage("No valid data");
                        return;
                    }

                    Log.d(TAG, "Successfully parsed " + seriesData.size() + " data points");

                    // Gửi message tới Handler để update UI
                    Message msg = Message.obtain();
                    msg.what = MSG_SHOW_CHART;
                    Bundle msgData = new Bundle();
                    msgData.putSerializable("seriesData", new ArrayList<>(seriesData));
                    msgData.putDouble("lastClose", lastClose);
                    msg.setData(msgData);
                    uiHandler.sendMessage(msg);

                } catch (JSONException e) {
                    Log.e(TAG, " JSON Parse Error: " + e.getMessage());
                    sendErrorMessage("JSON Parse Error");
                } finally {
                    response.close();
                }
            }
        });
    }

    private void sendErrorMessage(String error) {
        Message msg = Message.obtain();
        msg.what = MSG_SHOW_ERROR;
        Bundle data = new Bundle();
        data.putString("error", error);
        msg.setData(data);
        uiHandler.sendMessage(msg);
    }

    private void showErrorChart(String message) {
        Cartesian errorChart = AnyChart.line();
        errorChart.background().fill("#0e1117");
        errorChart.title("Error: " + message);
        anyChartView.setChart(errorChart);
        titleText.setText("Error loading chart");
    }

    private void showChart(List<DataEntry> seriesData, double currentPrice) {
        try {
            APIlib.getInstance().setActiveAnyChartView(anyChartView);

            Cartesian cartesian = AnyChart.line();

            cartesian.background().fill("#0E1117");
            cartesian.animation(true);
            cartesian.title(stockSymbol + " – $" + String.format("%.2f", currentPrice));

            cartesian.crosshair().enabled(true);
            cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
            cartesian.xAxis(0).labels().fontColor("#bdc3c7");
            cartesian.yAxis(0).labels().format("${%Value}").fontColor("#bdc3c7");
            cartesian.legend(false);

            cartesian.line(seriesData)
                    .name("Close Price")
                    .color("#00C853")
                    .tooltip()
                    .titleFormat("Date: {%x}")
                    .format("Close: ${%value}");

            cartesian.area(seriesData)
                    .fill(new SolidFill("rgba(0, 200, 83, 0.2)", 1))
                    .stroke("none");

            anyChartView.setChart(cartesian);
            titleText.setText(stockSymbol + " – $" + String.format("%.2f", currentPrice));

            Log.d(TAG, "Chart rendered successfully");

        } catch (Exception e) {
            Log.e(TAG, " Error rendering chart: " + e.getMessage());
            Toast.makeText(this, "Chart Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Clear chart
        if (anyChartView != null) {
            anyChartView.setChart(null);
        }

        // Stop handler
        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }

        // Stop API calls
        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }
}