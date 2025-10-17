package vn.edu.usth.stockdashboard.data.model;

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

    private double investedValue; // Giá trị đã đầu tư
    private double currentValue;  // Giá trị hiện tại của khoản đầu tư
    private double quantity; // Số lượng cổ phiếu nắm giữ


    // Constructor for creating initial placeholders
    public StockItem(String symbol) {
        this.symbol = symbol;
        this.time = "--:--:--";
        this.quantity = 0;
    }

    public StockItem(String symbol, double investedValue, double currentValue) {
        this(symbol); // Khởi tạo các trường market data mặc định
        this.investedValue = investedValue;
        this.currentValue = currentValue;
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
    // Add this new method to update an existing item from the new StockData model
    public void updateFromData(StockData data) {
        if (data == null) return;
        this.price = data.getClose();
        this.open = data.getOpen();
        this.high = data.getHigh();
        this.low = data.getLow();
        this.change = data.getChange();
        this.percentChange = data.getPercentChange();
        this.volume = data.getVolume();
        this.time = formatTime(data.getTime());
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
    public double getInvestedValue() {
        return investedValue;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public double getQuantity() {
        return quantity;
    }
    // Setters để cập nhật Portfolio Data
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setInvestedValue(double investedValue) {
        this.investedValue = investedValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }
    public void setPrice(double price) {this.price = price;}
}
