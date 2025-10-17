package vn.edu.usth.stockdashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.data.model.CandleData;
import vn.edu.usth.stockdashboard.data.sse.service.CryptoHistoryApi;
import vn.edu.usth.stockdashboard.view.CustomMarkerView;

import java.text.SimpleDateFormat;
import java.util.*;

public class CryptoDetailActivity extends AppCompatActivity {
    private LineChart lineChart;
    private TextView tvCryptoName, tvCurrentPrice, tvPriceChange, tvLastUpdate;
    private TextView tvDayRange, tvYearRange;
    private Button btn1Day, btn1Week, btn1Month, btn1Year, btn5Years, btnAll;
    private ImageView btnBack;

    private String symbol;
    private String cryptoName;
    private double currentPrice;
    private double priceChange;
    private double changePercent;

    private CryptoHistoryApi api;
    private Button selectedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_detail);

        // Get data from intent
        symbol = getIntent().getStringExtra("symbol");
        cryptoName = getIntent().getStringExtra("name");
        currentPrice = getIntent().getDoubleExtra("price", 0);
        priceChange = getIntent().getDoubleExtra("priceChange", 0);
        changePercent = getIntent().getDoubleExtra("changePercent", 0);

        initViews();
        setupRetrofit();
        setupChart();
        updatePriceInfo();
        setupTimeframeButtons();

        // Load default timeframe (5 years)
        loadChartData("1h", 30);
        selectButton(btn5Years);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvCryptoName = findViewById(R.id.tvCryptoName);
        tvCurrentPrice = findViewById(R.id.tvCurrentPrice);
        tvPriceChange = findViewById(R.id.tvPriceChange);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        tvDayRange = findViewById(R.id.tvDayRange);
        tvYearRange = findViewById(R.id.tvYearRange);
        lineChart = findViewById(R.id.lineChart);

        btn1Day = findViewById(R.id.btn1Day);
        btn1Week = findViewById(R.id.btn1Week);
        btn1Month = findViewById(R.id.btn1Month);
        btn1Year = findViewById(R.id.btn1Year);
        btn5Years = findViewById(R.id.btn5Years);
        btnAll = findViewById(R.id.btnAll);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api-crypto-d8ke.onrender.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(CryptoHistoryApi.class);
    }


    private void updatePriceInfo() {
        tvCryptoName.setText(cryptoName != null ? cryptoName : symbol.toUpperCase());
        tvCurrentPrice.setText(String.format(Locale.US, "%.1f", currentPrice));

        String changeText = String.format(Locale.US, "%.1f (%.2f%%)",
                priceChange, changePercent);
        tvPriceChange.setText(changeText);
        tvPriceChange.setTextColor(changePercent >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#FF4444"));

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss - 'Thời Gian Thực'", Locale.getDefault());
        tvLastUpdate.setText(sdf.format(new Date()));
    }

    private void setupChart() {
        // Chart appearance
        lineChart.setBackgroundColor(Color.parseColor("#1A1A1A"));
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawBorders(false);
        lineChart.setHighlightPerTapEnabled(true);
        lineChart.setHighlightPerDragEnabled(true);


        // ✅ Add custom marker view
        CustomMarkerView markerView = new CustomMarkerView(this, R.layout.marker_view);
        markerView.setChartView(lineChart);
        lineChart.setMarker(markerView);

        // X-Axis
        XAxis xAxis = lineChart.getXAxis();

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#888888"));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            private SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.US);

            @Override
            public String getFormattedValue(float value) {
                return sdf.format(new Date((long) value));
            }
        });

        // Y-Axis (left)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#888888"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#2A2A2A"));
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);

        // Y-Axis (right)
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
    }

    private void setupTimeframeButtons() {
        btn1Day.setOnClickListener(v -> {
            loadChartData("1m", 1440);
            selectButton(btn1Day);
        });

        btn1Week.setOnClickListener(v -> {
            loadChartData("15m", 672);
            selectButton(btn1Week);
        });

        btn1Month.setOnClickListener(v -> {
            loadChartData("1h", 720);
            selectButton(btn1Month);
        });

        btn1Year.setOnClickListener(v -> {
            loadChartData("1d", 365);
            selectButton(btn1Year);
        });

        btn5Years.setOnClickListener(v -> {
            loadChartData("1h", 30);
            selectButton(btn5Years);
        });

        btnAll.setOnClickListener(v -> {
            loadChartData("1w", 520);
            selectButton(btnAll);
        });
    }

    private void selectButton(Button button) {
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(
                    getResources().getColorStateList(android.R.color.darker_gray, null));
        }

        button.setBackgroundTintList(
                getResources().getColorStateList(android.R.color.holo_blue_dark, null));
        selectedButton = button;
    }

    private void loadChartData(String interval, int limit) {
        Call<List<CandleData>> call = api.getHistory(symbol, interval, limit);

        call.enqueue(new Callback<List<CandleData>>() {
            @Override
            public void onResponse(Call<List<CandleData>> call, Response<List<CandleData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CandleData> data = response.body();
                    updateChart(data);
                    updateRangeInfo(data);
                }
            }

            @Override
            public void onFailure(Call<List<CandleData>> call, Throwable t) {
                Log.e("CryptoDetail", "Failed to load chart data", t);
                Toast.makeText(CryptoDetailActivity.this,
                        "Không thể tải dữ liệu biểu đồ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChart(List<CandleData> dataList) {
        if (dataList == null || dataList.isEmpty()) return;

        // 1️⃣ Tạo danh sách Entry với index làm trục X
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            entries.add(new Entry(i, (float) dataList.get(i).getClose()));
        }

        // 2️⃣ Tạo dataset
        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setColor(Color.parseColor("#4A90E2"));
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);

        // Highlight styling
        dataSet.setHighLightColor(Color.parseColor("#FFFFFF"));
        dataSet.setHighlightLineWidth(1.5f);
        dataSet.setDrawHorizontalHighlightIndicator(true);
        dataSet.setDrawVerticalHighlightIndicator(true);

        // Fill gradient
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#4A90E2"));
        dataSet.setFillAlpha(50);

        // 3️⃣ Gán dữ liệu vào chart
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 4️⃣ Cấu hình lại trục X để fit theo limit
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // mỗi tick là 1 điểm
        xAxis.setLabelCount(6, true); // số lượng nhãn hiển thị (tuỳ chỉnh)
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
            @Override
            public String getFormattedValue(float value) {
                int i = (int) value;
                if (i >= 0 && i < dataList.size()) {
                    long time = dataList.get(i).getTime(); // timestamp trả về từ API
                    return sdf.format(new Date(time));
                }
                return "";
            }
        });

        // 5️⃣ Fit trục đúng phạm vi dữ liệu (bắt đầu = 0, kết thúc = limit - 1)
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(dataList.size() - 1);

        // 6️⃣ Hiệu ứng hiển thị
        lineChart.animateX(1000);
        lineChart.animateY(800);
        lineChart.invalidate();
    }


    private void updateRangeInfo(List<CandleData> dataList) {
        if (dataList.isEmpty()) return;

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (CandleData data : dataList) {
            if (data.getLow() < min) min = data.getLow();
            if (data.getHigh() > max) max = data.getHigh();
        }

        tvDayRange.setText(String.format(Locale.US, "%.1f - %.1f", min, max));

        // For year range, you would need to fetch 52 weeks of data
        // For now, showing same as day range
        tvYearRange.setText(String.format(Locale.US, "%.1f - %.1f", min * 0.8, max * 1.2));
    }
}