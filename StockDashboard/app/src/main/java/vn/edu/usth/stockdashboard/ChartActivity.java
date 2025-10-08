package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        anyChartView = findViewById(R.id.any_chart_view);
        lineChart = AnyChart.line();

        fetchStockData();
    }

    private void fetchStockData() {
        OkHttpClient client = new OkHttpClient();

        String url = "https://data.alpaca.markets/v2/stocks/VFS/bars?timeframe=1Day&start=2025-09-10";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("APCA-API-KEY-ID", "PKOZ5VBIZQD1NS3DS2VG")
                .addHeader("APCA-API-SECRET-KEY", "CwxMO4Iw8Z1dUdZHfRFu51DwrdwZlQvE8drkUfaU")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    Log.d("API_RESPONSE", jsonData);

                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

                    List<DataEntry> seriesData = new ArrayList<>();

                    if (jsonObject.has("bars")) {
                        JsonArray bars = jsonObject.getAsJsonArray("bars");

                        for (int i = 0; i < bars.size(); i++) {
                            JsonObject bar = bars.get(i).getAsJsonObject();
                            String date = bar.get("t").getAsString();
                            double closePrice = bar.get("c").getAsDouble();

                            seriesData.add(new ValueDataEntry(date.substring(0, 10), closePrice));
                        }
                    }

                    mainHandler.post(() -> {
                        lineChart.data(seriesData);
                        anyChartView.setChart(lineChart);
                    });
                }
            }
        });
    }
}
