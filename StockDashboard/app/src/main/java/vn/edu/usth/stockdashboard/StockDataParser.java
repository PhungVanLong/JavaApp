package vn.edu.usth.stockdashboard;

import android.util.Log;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StockDataParser {

    private static final String TAG = "StockDataParser";

    public static class Result {
        public List<DataEntry> seriesData;
        public double lastClose;
    }

    public Result parse(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);

        if (!jsonObject.has("data")) throw new JSONException("Missing 'data' field");

        JSONArray dataArray = jsonObject.getJSONArray("data");
        List<DataEntry> seriesData = new ArrayList<>();
        double lastClose = 0;

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject item = dataArray.getJSONObject(i);
            try {
                String time = item.getString("time");
                double close = item.getDouble("close");
                lastClose = close;
                time = formatDate(time);
                seriesData.add(new ValueDataEntry(time, close));
            } catch (Exception e) {
                Log.e(TAG, "Error parsing item " + i + ": " + e.getMessage());
            }
        }

        if (seriesData.isEmpty()) throw new JSONException("No valid data entries");

        Result result = new Result();
        result.seriesData = seriesData;
        result.lastClose = lastClose;
        return result;
    }

    private String formatDate(String time) {
        if (time.length() >= 10) {
            time = time.substring(5, 10).replace("-", " ");
            String[] parts = time.split(" ");
            if (parts.length == 2) {
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                int month = Integer.parseInt(parts[0]) - 1;
                return parts[1] + " " + (month >= 0 && month < 12 ? months[month] : "");
            }
        }
        return time;
    }
}

