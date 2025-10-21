package vn.edu.usth.stockdashboard;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.edu.usth.stockdashboard.view.CustomMarkerView;

public class CryptoDetailActivity extends BaseActivity {

    private TextView cryptoNameTextView, currentPriceTextView, priceChangeTextView, lastUpdateTextView, dayRangeTextView, yearRangeTextView;
    private ImageView backButton;
    private LineChart priceHistoryChart;

    private Button oneWeekButton, oneMonthButton, oneYearButton, fiveYearsButton, allTimeButton;
    private Button currentSelectedButton = null;

    private String symbol; // Ví dụ: "btcusdt"
    private String cryptoFullName; // Ví dụ: "BITCOIN" (dùng làm giá trị dự phòng)

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Thread sseThread;
    private volatile boolean isRunning = true; // Dùng 'volatile' để đảm bảo tính toàn vẹn của biến giữa các luồng
    private double lastPrice = -1.0; // Lưu giá cuối cùng để so sánh

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_detail);

        symbol = getIntent().getStringExtra("symbol");
        cryptoFullName = getIntent().getStringExtra("name");

        initializeViews();

        setupInitialUI();

        setupTimeframeButtons();

        highlightSelectedButton(oneMonthButton);
        fetchAndRenderChartData(symbol, "4h", 30);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (sseThread == null || !sseThread.isAlive()) {
            isRunning = true;
            startRealtimePriceUpdates(symbol);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        if (sseThread != null) {
            sseThread.interrupt();
            sseThread = null;
        }
    }


    private void initializeViews() {
        cryptoNameTextView = findViewById(R.id.tvCryptoName);
        currentPriceTextView = findViewById(R.id.tvCurrentPrice);
        priceChangeTextView = findViewById(R.id.tvPriceChange);
        lastUpdateTextView = findViewById(R.id.tvLastUpdate);
        dayRangeTextView = findViewById(R.id.tvDayRange);
        yearRangeTextView = findViewById(R.id.tvYearRange);
        backButton = findViewById(R.id.btnBack);
        priceHistoryChart = findViewById(R.id.lineChart);

        oneWeekButton = findViewById(R.id.btn1Week);
        oneMonthButton = findViewById(R.id.btn1Month);
        oneYearButton = findViewById(R.id.btn1Year);
        fiveYearsButton = findViewById(R.id.btn5Years);
        allTimeButton = findViewById(R.id.btnAll);
    }


    private void setupInitialUI() {
        cryptoNameTextView.setText(mapSymbolToFullName(symbol));
        backButton.setOnClickListener(v -> finish());
    }


    private void setupTimeframeButtons() {
        oneWeekButton.setOnClickListener(v -> handleTimeframeSelection(oneWeekButton, "4h", 7));
        oneMonthButton.setOnClickListener(v -> handleTimeframeSelection(oneMonthButton, "4h", 30));
        oneYearButton.setOnClickListener(v -> handleTimeframeSelection(oneYearButton, "1d", 365));
        fiveYearsButton.setOnClickListener(v -> handleTimeframeSelection(fiveYearsButton, "1w", 5 * 365));
        allTimeButton.setOnClickListener(v -> handleTimeframeSelection(allTimeButton, "1w", 10 * 365)); // Giả định "All" là 10 năm
    }


    private void handleTimeframeSelection(Button clickedButton, String interval, int days) {
        highlightSelectedButton(clickedButton);
        fetchAndRenderChartData(symbol, interval, days);
    }


    private void highlightSelectedButton(Button selectedButton) {
        if (currentSelectedButton != null) {
        }
        currentSelectedButton = selectedButton;
    }


    private String mapSymbolToFullName(String symbol) {
        if (symbol == null) return "Unknown";
        switch (symbol.toLowerCase()) {
            case "btcusdt": return "Bitcoin";
            case "ethusdt": return "Ethereum";
            case "bnbusdt": return "BNB";
            case "adausdt": return "Cardano";
            case "xrpusdt": return "XRP";
            case "solusdt": return "Solana";
            case "dotusdt": return "Polkadot";
            case "avxusdt": return "Avalanche";
            case "ltcusdt": return "Litecoin";
            case "linkusdt": return "Chainlink";
            case "maticusdt": return "Polygon";
            case "uniusdt": return "Uniswap";
            case "atomusdt": return "Cosmos";
            case "trxusdt": return "TRON";
            case "aptusdt": return "Aptos";
            case "filusdt": return "Filecoin";
            case "nearusdt": return "NEAR Protocol";
            case "icpusdt": return "Internet Computer";
            case "vetusdt": return "VeChain";
            default:
                return cryptoFullName != null ? cryptoFullName : symbol.replace("usdt", "").toUpperCase();
        }
    }



    private void fetchAndRenderChartData(String symbol, String interval, int days) {
        new Thread(() -> {
            try {
                long currentTimeMillis = System.currentTimeMillis();
                long startTimeMillis = currentTimeMillis - (days * 24L * 60 * 60 * 1000);

                String urlString = "https://api-crypto-58oa.onrender.com/history?symbol=" +
                        symbol + "&interval=" + interval + "&start=" + startTimeMillis + "&end=" + currentTimeMillis;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();
                connection.disconnect();

                JSONArray jsonArray = new JSONArray(jsonBuilder.toString());
                List<Entry> chartEntries = new ArrayList<>();

                double minPrice = Double.MAX_VALUE;
                double maxPrice = Double.MIN_VALUE;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    long time = jsonObject.getLong("time");
                    float closePrice = (float) jsonObject.getDouble("close");

                    chartEntries.add(new Entry(time, closePrice));
                    minPrice = Math.min(minPrice, closePrice);
                    maxPrice = Math.max(maxPrice, closePrice);
                }

                double finalMinPrice = minPrice;
                double finalMaxPrice = maxPrice;
                handler.post(() -> {
                    renderChart(chartEntries, interval);
                    dayRangeTextView.setText(String.format(Locale.US, "$%.2f - $%.2f", finalMinPrice, finalMaxPrice));
                    yearRangeTextView.setText(String.format(Locale.US, "$%.2f - $%.2f", finalMinPrice * 0.8, finalMaxPrice * 1.2));
                });

            } catch (Exception e) {
                Log.e("ChartDataError", "Lỗi khi tải dữ liệu biểu đồ: ", e);
            }
        }).start();
    }

    private void renderChart(List<Entry> entries, String interval) {
        LineDataSet dataSet = new LineDataSet(entries, "Giá " + symbol.toUpperCase());
        dataSet.setColor(Color.parseColor("#4A90E2"));
        dataSet.setLineWidth(1f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        priceHistoryChart.setData(lineData);

        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view);
        priceHistoryChart.setMarker(markerView);

        XAxis xAxis = priceHistoryChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setLabelRotationAngle(-30);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat dateFormat;
            {
                switch (interval) {
                    case "1h":
                    case "4h":
                        dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                        break;
                    case "1d":
                    case "1w":
                    default:
                        dateFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                        break;
                }
            }
            @Override
            public String getFormattedValue(float value) {
                return dateFormat.format(new Date((long) value));
            }
        });

        YAxis leftAxis = priceHistoryChart.getAxisLeft();
        leftAxis.setTextColor(Color.LTGRAY);
        leftAxis.setDrawGridLines(true);
        priceHistoryChart.getAxisRight().setEnabled(false);

        priceHistoryChart.getDescription().setEnabled(false);
        priceHistoryChart.getLegend().setEnabled(false);
        priceHistoryChart.invalidate();
    }


    private void startRealtimePriceUpdates(String symbol) {
        String urlString = "https://crypto-server-xqv5.onrender.com/events?symbols=" + symbol;
        sseThread = new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urlString);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "text/event-stream");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(0);
                connection.connect();

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder dataBuilder = new StringBuilder();

                while (isRunning && (line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        dataBuilder.append(line.substring(5).trim());
                    } else if (line.isEmpty() && dataBuilder.length() > 0) {
                        try {
                            JSONObject json = new JSONObject(dataBuilder.toString());
                            double price = json.getDouble("price");
                            double changePercent = json.getDouble("change_percent");
                            long timestamp = json.getLong("timestamp") * 1000;
                            handler.post(() -> updatePriceUI(price, changePercent, timestamp));
                        } catch (Exception e) {
                            Log.e("SSE_ParseError", "Lỗi phân tích JSON: " + e.getMessage());
                        }
                        dataBuilder.setLength(0);
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    Log.e("SSE_ConnectionError", "Lỗi kết nối SSE: ", e);
                }
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException ignored) {}
            }
        });
        sseThread.start();
    }



    private void updatePriceUI(double price, double changePercent, long timestamp) {
        if (lastPrice > 0) {
            if (price > lastPrice) {
                flashTextView(currentPriceTextView, Color.WHITE, Color.parseColor("#4CAF50")); // Xanh
            } else if (price < lastPrice) {
                flashTextView(currentPriceTextView, Color.WHITE, Color.parseColor("#F44336")); // Đỏ
            }
        }
        lastPrice = price;

        currentPriceTextView.setText(String.format(Locale.US, "$%.3f", price));
        priceChangeTextView.setText(String.format(Locale.US, "%.3f%%", changePercent));

        int priceChangeColor = changePercent >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336");
        priceChangeTextView.setTextColor(priceChangeColor);

        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        lastUpdateTextView.setText("Update: " + time);
    }

    private void flashTextView(TextView textView, int startColor, int endColor) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), endColor, startColor);
        animator.setDuration(500);
        animator.addUpdateListener(animation -> textView.setTextColor((int) animation.getAnimatedValue()));
        animator.start();
    }
}