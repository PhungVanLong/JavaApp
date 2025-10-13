package vn.edu.usth.stockdashboard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import vn.edu.usth.stockdashboard.data.sse.StockData;
import vn.edu.usth.stockdashboard.data.sse.StockSymbolData;


public class StockItem {
    // All necessary fields are declared here
    private final String symbol;
    private String time;
    private double price;
    private double change;
    private double percentChange;
    private long volume;
    private double open;
    private double high;
    private double low;

    // Constructor for creating initial placeholders
    public StockItem(String symbol) {
        this.symbol = symbol;
        this.time = "--:--:--";
    }

    // The single, correct way to create a fully populated StockItem
    public static StockItem fromStockSymbolData(StockSymbolData symbolData) {
        StockItem item = new StockItem(symbolData.getSymbol());
        StockData data = symbolData.getData();
        if (data != null) {
            item.price = data.getClose();
            item.open = data.getOpen();
            item.high = data.getHigh();
            item.low = data.getLow();
            item.change = data.getChange();
            item.percentChange = data.getPercentChange();
            item.volume = data.getVolume();
            item.time = formatTime(data.getTime());
        }
        return item;
    }

    // The single, correct time formatting method for this project
    private static String formatTime(String gmtTime) {
        if (gmtTime == null || gmtTime.isEmpty()) return "--:--:--";
        try {
            // Input format: "Mon, 13 Oct 2025 00:00:00 GMT"
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(gmtTime);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "--:--:--";
        }
    }

    // Getters for all fields
    public String getSymbol() {
        return symbol;
    }

    public String getTime() {
        return time;
    }

    public double getPrice() {
        return price;
    }

    public double getChange() {
        return change;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public long getVolume() {
        return volume;
    }

    public double getOpen() {
        return open;
    }
}