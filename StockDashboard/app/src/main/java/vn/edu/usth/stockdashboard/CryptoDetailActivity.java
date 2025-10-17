package vn.edu.usth.stockdashboard;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.json.*;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

import vn.edu.usth.stockdashboard.view.CustomMarkerView;

public class CryptoDetailActivity extends AppCompatActivity {

    private TextView tvCryptoName, tvCurrentPrice, tvPriceChange, tvLastUpdate, tvDayRange, tvYearRange;
    private ImageView btnBack;
    private LineChart lineChart;
    private String symbol, cryptoName;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Thread sseThread;
    private volatile boolean isRunning = true;
    private double lastPrice = -1;

    // Các nút timeframe
    private Button btn1Week, btn1Month, btn1Year, btn5Years, btnAll;
    private Button currentSelectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_detail);

        symbol = getIntent().getStringExtra("symbol");
        cryptoName = getIntent().getStringExtra("name");

        tvCryptoName = findViewById(R.id.tvCryptoName);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvPriceChange = findViewById(R.id.tvPriceChange);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        tvDayRange = findViewById(R.id.tvDayRange);
        tvYearRange = findViewById(R.id.tvYearRange);
        btnBack = findViewById(R.id.btnBack);
        lineChart = findViewById(R.id.lineChart);

        // Gán button
        btn1Week = findViewById(R.id.btn1Week);
        btn1Month = findViewById(R.id.btn1Month);
        btn1Year = findViewById(R.id.btn1Year);
        btn5Years = findViewById(R.id.btn5Years);

        btnAll = findViewById(R.id.btnAll);

        tvCryptoName.setText(cryptoName);
        btnBack.setOnClickListener(v -> finish());

        // Timeframe (đã bỏ nút 1 day)
        setupButton(btn1Week, "4h", 7);
        setupButton(btn1Month, "4h", 30);
        setupButton(btn1Year, "4h", 365);
        setupButton(btn5Years, "1w", 5 * 365);
        setupButton(btnAll, "1w", 10 * 365);

        // Mặc định chọn 1 tháng
        highlightSelectedButton(btn1Month);
        loadChartData(symbol, "4h", 30);
        startSSE(symbol);
    }

    private void setupButton(Button button, String interval, int days) {
        button.setOnClickListener(v -> {
            loadChartData(symbol, interval, days);
        });
    }

    private void highlightSelectedButton(Button selectedButton) {

        currentSelectedButton = selectedButton;
    }

    // ======================
    // 1️⃣ LẤY DỮ LIỆU CHART
    // ======================
    private void loadChartData(String symbol, String interval, int days) {
        new Thread(() -> {
            try {
                long now = System.currentTimeMillis();
                long start = now - days * 24L * 60 * 60 * 1000;

                String urlStr = "https://api-crypto-58oa.onrender.com/history?symbol=" +
                        symbol + "&interval=" + interval + "&start=" + start + "&end=" + now;

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) jsonBuilder.append(line);
                reader.close();

                JSONArray arr = new JSONArray(jsonBuilder.toString());
                List<Entry> entries = new ArrayList<>();
                List<Long> times = new ArrayList<>();

                double minPrice = Double.MAX_VALUE;
                double maxPrice = Double.MIN_VALUE;

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    long time = o.getLong("time");
                    float close = (float) o.getDouble("close");
                    entries.add(new Entry(time, close)); // Dùng timestamp
                    times.add(time);
                    minPrice = Math.min(minPrice, close);
                    maxPrice = Math.max(maxPrice, close);
                }

                double finalMinPrice = minPrice;
                double finalMaxPrice = maxPrice;
                handler.post(() -> renderChart(entries, times, finalMinPrice, finalMaxPrice, interval));

            } catch (Exception e) {
                Log.e("Chart", "Error loading chart: ", e);
            }
        }).start();
    }

    private void renderChart(List<Entry> entries, List<Long> times, double minPrice, double maxPrice, String interval) {
        LineDataSet dataSet = new LineDataSet(entries, "Giá " + symbol.toUpperCase());
        dataSet.setColor(Color.parseColor("#4A90E2"));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Marker View
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view);
        lineChart.setMarker(markerView);

        // X-Axis (theo timestamp)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setLabelRotationAngle(-30);

        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf;

            {
                switch (interval) {
                    case "1h":
                        sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        break;
                    case "4h":
                        sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
                        break;
                    case "1d":
                    case "1w":
                    default:
                        sdf = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                        break;
                }
            }

            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });

        // Y-Axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.LTGRAY);
        leftAxis.setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);

        // Chart style
        lineChart.getDescription().setText("");
        lineChart.invalidate();

        tvDayRange.setText(String.format(Locale.US, "$%.2f - $%.2f", minPrice, maxPrice));
        tvYearRange.setText(String.format(Locale.US, "$%.2f - $%.2f", minPrice * 0.5, maxPrice * 1.2));
    }

    // ======================
    // 2️⃣ SSE GIÁ REALTIME
    // ======================
    private void startSSE(String symbol) {
        String urlStr = "https://crypto-server-xqv5.onrender.com/events?symbols=" + symbol;
        sseThread = new Thread(() -> {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "text/event-stream");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(0);
                conn.connect();

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder dataBuilder = new StringBuilder();

                while (isRunning && (line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        dataBuilder.append(line.substring(5).trim());
                    } else if (line.isEmpty()) {
                        if (dataBuilder.length() > 0) {
                            try {
                                JSONObject json = new JSONObject(dataBuilder.toString());
                                double price = json.getDouble("price");
                                double changePercent = json.getDouble("change_percent");
                                long timestamp = json.getLong("timestamp") * 1000;
                                handler.post(() -> updateUI(price, changePercent, timestamp));
                            } catch (Exception e) {
                                Log.e("SSE", "Parse error: " + e.getMessage());
                            }
                            dataBuilder.setLength(0);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("SSE", "Error: ", e);
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (conn != null) conn.disconnect();
                } catch (IOException ignored) {}
            }
        });
        sseThread.start();
    }

    // ======================
    // 3️⃣ CẬP NHẬT UI GIÁ
    // ======================
    private void updateUI(double price, double changePercent, long timestamp) {
        if (lastPrice > 0) {
            if (price > lastPrice) flash(tvCurrentPrice, Color.WHITE, Color.parseColor("#4CAF50"));
            else if (price < lastPrice) flash(tvCurrentPrice, Color.WHITE, Color.parseColor("#F44336"));
        }
        lastPrice = price;

        tvCurrentPrice.setText(String.format(Locale.US, "$%.4f", price));
        tvPriceChange.setText(String.format(Locale.US, "%.3f%%", changePercent));
        tvPriceChange.setTextColor(changePercent >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(timestamp));
        tvLastUpdate.setText("Cập nhật: " + time);
    }

    private void flash(TextView tv, int from, int to) {
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), to, from);
        animator.setDuration(400);
        animator.addUpdateListener(a -> tv.setTextColor((int) a.getAnimatedValue()));
        animator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (sseThread != null && sseThread.isAlive()) {
            sseThread.interrupt();
        }
    }
}
