package vn.edu.usth.stockdashboard.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import vn.edu.usth.stockdashboard.R;

public class ChartFragment extends Fragment {

    private static final String TAG = "ChartFragment";
    private static final int MSG_SHOW_CHART = 1;
    private static final int MSG_SHOW_ERROR = 2;
    private static final int MSG_SHOW_LOADING = 3;
    private static final String ARG_STOCK_SYMBOL = "stock_symbol";

    private LineChart lineChart;
    private TextView titleText;
    private TextView loadingText;
    private ChipGroup chipGroup;

    private String stockSymbol;
    // CHANGED: Thay đổi giá trị mặc định ở đây
    private String currentRange = "1D";
    private OkHttpClient client;
    private Handler uiHandler;
    private List<String> xAxisLabels;

    public static ChartFragment newInstance(String stockSymbol) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STOCK_SYMBOL, stockSymbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockSymbol = getArguments().getString(ARG_STOCK_SYMBOL, "VNI");
        } else {
            stockSymbol = "VNI";
        }
        client = new OkHttpClient();
        xAxisLabels = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChart = view.findViewById(R.id.line_chart);
        titleText = view.findViewById(R.id.titleText);
        chipGroup = view.findViewById(R.id.chipGroup);

        setupHandler();
        setupChart();
        setupChipGroupListener();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Lần gọi đầu tiên này sẽ tự động fetch dữ liệu intraday do currentRange = "1D"
        fetchStockData();
    }

    private void setupChipGroupListener() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) return;
            if (checkedId == R.id.chip1D) {
                currentRange = "1D";
            } else if (checkedId == R.id.chip1M) {
                currentRange = "1M";
            } else if (checkedId == R.id.chip3M) {
                currentRange = "3M";
            } else if (checkedId == R.id.chip6M) {
                currentRange = "6M";
            }
            Log.d(TAG, "Time range changed to: " + currentRange);
            fetchStockData();
        });
    }

    private void setupHandler() {
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                if (msg.what == MSG_SHOW_CHART) {
                    Bundle data = msg.getData();
                    ArrayList<Entry> entries = (ArrayList<Entry>) data.getSerializable("entries");
                    ArrayList<String> labels = (ArrayList<String>) data.getSerializable("labels");
                    double lastClose = data.getDouble("lastClose");
                    showChart(entries, labels, lastClose);

                } else if (msg.what == MSG_SHOW_ERROR) {
                    String errorMsg = msg.getData().getString("error");
                    showError(errorMsg);

                } else if (msg.what == MSG_SHOW_LOADING) {
                    showLoading();
                }
            }
        };
    }

    private void setupChart() {
        lineChart.setBackgroundColor(Color.parseColor("#0E1117"));
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.parseColor("#BDC3C7"));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < xAxisLabels.size()) {
                    return xAxisLabels.get(index);
                }
                return "";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#BDC3C7"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#2C3E50"));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return "$" + String.format("%.2f", value);
            }
        });

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
    }

    private void showLoading() {
        if (loadingText != null && titleText != null && lineChart != null) {
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("Loading " + stockSymbol + " data...");
            titleText.setText(stockSymbol);
            lineChart.clear();
            lineChart.invalidate();
        }
    }

    private void showError(String errorMsg) {
        if (loadingText != null && titleText != null) {
            loadingText.setVisibility(View.VISIBLE);
            loadingText.setText("Error: " + errorMsg);
            titleText.setText("Error");
            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void fetchStockData() {
        showLoading();

        String baseUrl = "https://vn-stock-api-bsjj.onrender.com/api/stock/" + stockSymbol.toLowerCase();
        String url;

        if ("1D".equals(currentRange)) {
            url = baseUrl + "/intraday";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar calendar = Calendar.getInstance();
            String endDate = sdf.format(calendar.getTime());

            switch (currentRange) {
                case "1M":
                    calendar.add(Calendar.MONTH, -1);
                    break;
                case "3M":
                    calendar.add(Calendar.MONTH, -3);
                    break;
                case "6M":
                default:
                    calendar.add(Calendar.MONTH, -6);
                    break;
            }
            String startDate = sdf.format(calendar.getTime());
            url = baseUrl + "/history?start=" + startDate + "&end=" + endDate;
        }

        Log.d(TAG, "📡 Fetching data from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "❌ Request failed: " + e.getMessage());
                sendErrorMessage("Connection Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "❌ API Error: " + response.code());
                    sendErrorMessage("API Error: " + response.code());
                    response.close();
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    if (!jsonObject.has("data")) {
                        sendErrorMessage("No data available");
                        return;
                    }

                    JSONArray dataArray = jsonObject.getJSONArray("data");
                    if (dataArray.length() == 0) {
                        sendErrorMessage("Data is empty");
                        return;
                    }

                    ArrayList<Entry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();
                    double lastClose = 0;

                    if ("1D".equals(currentRange)) {
                        TreeMap<String, Float> aggregatedData = new TreeMap<>();

                        SimpleDateFormat gmtFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
                        SimpleDateFormat localFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        localFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject item = dataArray.getJSONObject(i);
                            try {
                                String timeStr = item.getString("time");
                                Date date = gmtFormat.parse(timeStr);
                                String localTimeKey = localFormat.format(date);

                                float price = (float) item.getDouble("price");
                                lastClose = price;

                                aggregatedData.put(localTimeKey, price);
                            } catch (JSONException | ParseException e) {
                                Log.e(TAG, "Error parsing intraday item " + i + ": " + e.getMessage());
                            }
                        }

                        int index = 0;
                        for (String timeKey : aggregatedData.keySet()) {
                            labels.add(timeKey);
                            entries.add(new Entry(index++, aggregatedData.get(timeKey)));
                        }

                    } else {
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

                                entries.add(new Entry(i, (float) close));
                                labels.add(time);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing history item " + i + ": " + e.getMessage());
                            }
                        }
                    }

                    if (entries.isEmpty()) {
                        sendErrorMessage("No valid data");
                        return;
                    }

                    Message msg = Message.obtain();
                    msg.what = MSG_SHOW_CHART;
                    Bundle msgData = new Bundle();
                    msgData.putSerializable("entries", entries);
                    msgData.putSerializable("labels", labels);
                    msgData.putDouble("lastClose", lastClose);
                    msg.setData(msgData);
                    uiHandler.sendMessage(msg);

                } catch (JSONException e) {
                    Log.e(TAG, "❌ JSON Parse Error: " + e.getMessage());
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

    private void showChart(List<Entry> entries, List<String> labels, double currentPrice) {
        try {
            if (loadingText != null) {
                loadingText.setVisibility(View.GONE);
            }

            xAxisLabels.clear();
            xAxisLabels.addAll(labels);

            LineDataSet dataSet = new LineDataSet(entries, "Close Price");
            dataSet.setColor(Color.parseColor("#FFFFFFFF"));
            dataSet.setDrawCircles(false);
            dataSet.setLineWidth(2f);
            dataSet.setValueTextSize(0f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(Color.parseColor("#FFFFFFFF"));
            dataSet.setFillAlpha(50);
            dataSet.setMode(LineDataSet.Mode.LINEAR);

            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);
            lineChart.invalidate();

            if (titleText != null) {
                titleText.setText(stockSymbol + " – $" + String.format("%.2f", currentPrice));
            }

            Log.d(TAG, "✅ Chart rendered successfully");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error rendering chart: " + e.getMessage());
            Toast.makeText(getContext(), "Chart Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (lineChart != null) {
            lineChart.clear();
        }

        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }

        if (client != null) {
            client.dispatcher().cancelAll();
        }
    }
}