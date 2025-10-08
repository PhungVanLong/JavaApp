package vn.edu.usth.stockdashboard.data.model;

// StockData.java - Data bên trong
import com.google.gson.annotations.SerializedName;

public class StockData {
    @SerializedName("close")
    private double close;

    @SerializedName("high")
    private double high;

    @SerializedName("low")
    private double low;

    @SerializedName("open")
    private double open;

    @SerializedName("time")
    private String time;

    @SerializedName("volume")
    private long volume;

    // Getters
    public double getClose() { return close; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getOpen() { return open; }
    public String getTime() { return time; }
    public long getVolume() { return volume; }

    // Tính toán change và percentChange
    public double getChange() {
        return close - open;
    }

    public double getPercentChange() {
        if (open == 0) return 0;
        return ((close - open) / open) * 100;
    }
}