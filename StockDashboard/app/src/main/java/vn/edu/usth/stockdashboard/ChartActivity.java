package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.enums.Anchor;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.SolidFill;
import com.anychart.graphics.vector.text.HAlign;

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

    private AnyChartView anyChartView;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView titleText;
    private Button btnRefresh;

    private static final String STOCK_SYMBOL = "VNI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        anyChartView = findViewById(R.id.any_chart_view);
        titleText = findViewById(R.id.titleText);
        btnRefresh = findViewById(R.id.btn_refresh);

        showLoadingChart();

        btnRefresh.setOnClickListener(v -> fetchStockData());
        fetchStockData();
    }

    private void showLoadingChart() {
        Cartesian loadingChart = AnyChart.line();
        loadingChart.background().fill("#0e1117");
        loadingChart.title("Loading stock data...");
        anyChartView.setChart(loadingChart);
    }

    private void fetchStockData() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://vn-stock-api-bsjj.onrender.com/api/stock/vni/history";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("API_RESPONSE", "Request failed: " + response.code());
                    return;
                }

                String jsonData = response.body().string();
                Log.d("API_RESPONSE", jsonData);

                try {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray dataArray = jsonObject.getJSONArray("data");

                    List<DataEntry> seriesData = new ArrayList<>();
                    double lastClose = 0;

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject item = dataArray.getJSONObject(i);

                        String time = item.getString("time").substring(5, 16); // ví dụ "08 Sep 2025"
                        double close = item.getDouble("close");
                        lastClose = close;

                        seriesData.add(new ValueDataEntry(time, close));
                    }

                    double finalLastClose = lastClose;
                    runOnUiThread(() -> showChart(seriesData, finalLastClose));

                } catch (JSONException e) {
                    Log.e("API_RESPONSE", "JSON Parse Error: " + e.getMessage());
                }
            }
        });
    }

    private void showChart(List<DataEntry> seriesData, double currentPrice) {
        APIlib.getInstance().setActiveAnyChartView(anyChartView);

        Cartesian cartesian = AnyChart.line();

        cartesian.background().fill("#0E1117");
        cartesian.animation(true);
        cartesian.title("VNI Index – " + currentPrice + " USD");

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
                .format("Close: {%value}");

        cartesian.area(seriesData)
                .fill(new SolidFill("rgba(0, 200, 83, 0.2)", 1))
                .stroke("none");

        anyChartView.setChart(cartesian);
        titleText.setText("VNI Stock – " + currentPrice + " USD");
    }
}
