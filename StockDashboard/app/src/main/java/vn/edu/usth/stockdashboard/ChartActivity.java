package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.SolidFill;

import org.json.JSONException;

import java.io.IOException;
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
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String stockSymbol;
    private OkHttpClient client;
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        initViews();
        setupHandler();

        client = new OkHttpClient();
        stockSymbol = getIntent().getStringExtra("STOCK_SYMBOL");
        if (stockSymbol == null || stockSymbol.isEmpty()) stockSymbol = "VNI";

        Log.d(TAG, "Stock Symbol: " + stockSymbol);

        showLoadingState();
        fetchStockData();

        btnRefresh.setOnClickListener(v -> {
            showLoadingState();
            fetchStockData();
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchStockData();
        });
    }

    private void initViews() {
        anyChartView = findViewById(R.id.any_chart_view);
        titleText = findViewById(R.id.titleText);
        btnRefresh = findViewById(R.id.btn_refresh);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
    }

    private void setupHandler() {
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case MSG_SHOW_CHART:
                        Bundle data = msg.getData();
                        List<DataEntry> seriesData =
                                (List<DataEntry>) data.getSerializable("seriesData");
                        double lastClose = data.getDouble("lastClose");
                        showChart(seriesData, lastClose);
                        hideLoadingState();
                        break;

                    case MSG_SHOW_ERROR:
                        String errorMsg = msg.getData().getString("error");
                        showErrorChart(errorMsg);
                        hideLoadingState();
                        Toast.makeText(ChartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        break;

                    case MSG_SHOW_LOADING:
                        showLoadingState();
                        break;
                }
            }
        };
    }

    private void fetchStockData() {
        String url = "https://vn-stock-api-bsjj.onrender.com/api/stock/"
                + stockSymbol.toLowerCase() + "/history";

        Log.d(TAG, "📡 Fetching data from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sendErrorMessage("Connection Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
                    throws IOException {
                if (!response.isSuccessful()) {
                    sendErrorMessage("API Error: " + response.code());
                    response.close();
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    StockDataParser parser = new StockDataParser();
                    StockDataParser.Result result = parser.parse(jsonData);

                    Message msg = Message.obtain();
                    msg.what = MSG_SHOW_CHART;
                    Bundle msgData = new Bundle();
                    msgData.putSerializable("seriesData",
                            (java.io.Serializable) result.seriesData);
                    msgData.putDouble("lastClose", result.lastClose);
                    msg.setData(msgData);
                    uiHandler.sendMessage(msg);

                } catch (JSONException e) {
                    sendErrorMessage("JSON Parse Error: " + e.getMessage());
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

    private void showChart(List<DataEntry> seriesData, double currentPrice) {
        try {
            APIlib.getInstance().setActiveAnyChartView(anyChartView);
            Cartesian cartesian = AnyChart.line();

            cartesian.background().fill("#0E1117");
            cartesian.animation(true);
            cartesian.title(stockSymbol + " – $" + String.format("%.2f", currentPrice));
            cartesian.crosshair().enabled(true);
            cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

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

            Log.d(TAG, "✅ Chart rendered successfully");
        } catch (Exception e) {
            sendErrorMessage("Chart Render Error: " + e.getMessage());
        }
    }

    private void showErrorChart(String message) {
        Cartesian errorChart = AnyChart.line();
        errorChart.background().fill("#0E1117");
        errorChart.title("Error: " + message);
        anyChartView.setChart(errorChart);
        titleText.setText("Error loading chart");
    }

    private void showLoadingState() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        anyChartView.setVisibility(android.view.View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void hideLoadingState() {
        progressBar.setVisibility(android.view.View.GONE);
        anyChartView.setVisibility(android.view.View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (anyChartView != null) anyChartView.setChart(null);
        if (uiHandler != null) uiHandler.removeCallbacksAndMessages(null);
        if (client != null) client.dispatcher().cancelAll();
    }
}
