package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChartActivity extends BaseActivity {

    private AnyChartView anyChartView;
    private Cartesian lineChart;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView titleText;
    private Button btnRefresh;

    private static final String STOCK_SYMBOL = "VFS";
    private static final String API_KEY = "PKOZ5VBIZQD1NS3DS2VG";
    private static final String API_SECRET = "CwxMO4Iw8Z1dUdZHfRFu51DwrdwZlQvE8drkUfaU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        anyChartView = findViewById(R.id.any_chart_view);
        titleText = findViewById(R.id.titleText);
        btnRefresh = findViewById(R.id.btn_refresh);

        lineChart = AnyChart.line();
        showLoadingChart();

        btnRefresh.setOnClickListener(v -> fetchStockData());
        fetchStockData();
    }

    private void showLoadingChart() {
        lineChart.animation(true);
        lineChart.background().fill("#0e1117");
        lineChart.title("Loading stock data...");
        anyChartView.setChart(lineChart);
    }

    private void fetchStockData() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://data.alpaca.markets/v2/stocks/VFS/bars?timeframe=1Day&start=2025-09-10";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", API_KEY)
                .addHeader("APCA-API-SECRET-KEY", API_SECRET)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("API_ERROR", "Invalid response");
                    return;
                }

                String jsonData = response.body().string();
                Log.d("API_RESPONSE", jsonData);

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

                List<DataEntry> seriesData = new ArrayList<>();
                double lastPrice = 0;

                if (jsonObject.has("bars")) {
                    JsonArray bars = jsonObject.getAsJsonArray("bars");

                    for (int i = 0; i < bars.size(); i++) {
                        JsonObject bar = bars.get(i).getAsJsonObject();
                        String date = bar.get("t").getAsString().substring(0, 10);
                        double closePrice = bar.get("c").getAsDouble();
                        lastPrice = closePrice;
                        seriesData.add(new ValueDataEntry(date, closePrice));
                    }
                }

                double finalLastPrice = lastPrice;
                mainHandler.post(() -> showChart(seriesData, finalLastPrice));
            }
        });
    }

    private void showChart(List<DataEntry> entries, double currentPrice) {
        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);
        cartesian.background().fill("#0e1117");
        cartesian.title("Stock Price " + STOCK_SYMBOL + " – Current: " + currentPrice + " USD");

        cartesian.rangeMarker(0)
                .from(currentPrice)
                .to(currentPrice)
                .fill("rgba(231, 76, 60, 0.15)");

        cartesian.xAxis(0).title("Date").labels().fontColor("#bdc3c7");
        cartesian.yAxis(0).title("Close Price (USD)").labels().fontColor("#bdc3c7");
        cartesian.legend().enabled(false);

        cartesian.line(entries).name("Price").color("#1abc9c");

        anyChartView.setChart(cartesian);
        titleText.setText("VFS Stock Chart – " + currentPrice + " USD");
    }
}
