// StockItem.java - Thêm constructor từ StockApiResponse
package vn.edu.usth.stockdashboard;


import vn.edu.usth.stockdashboard.data.model.StockApiResponse;
import vn.edu.usth.stockdashboard.data.model.StockData;

public class StockItem {
    private String symbol;
    private String time;
    private double price;
    private double change;
    private double percentChange;
    private long volume;
    private double open;
    private double high;
    private double low;

    // Constructor cũ (giữ lại để không bị lỗi)
    public StockItem(String symbol, String time, double price, double change, long volume) {
        this.symbol = symbol;
        this.time = time;
        this.price = price;
        this.change = change;
        this.volume = volume;
    }

    // Constructor mới từ API response
    public static StockItem fromApiResponse(StockApiResponse response) {
        StockItem item = new StockItem();
        StockData data = response.getData();

        item.symbol = response.getSymbol();
        item.price = data.getClose();
        item.open = data.getOpen();
        item.high = data.getHigh();
        item.low = data.getLow();
        item.change = data.getChange();
        item.percentChange = data.getPercentChange();
        item.volume = data.getVolume();
        item.time = formatTime(data.getTime());

        return item;
    }

    private StockItem() {
        // Private constructor for factory method
    }

    private static String formatTime(String time) {
        try {
            // Convert "Mon, 06 Oct 2025 00:00:00 GMT" to "12:00:00 AM"
            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH);
            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.ENGLISH);

            java.util.Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (Exception e) {
            return time; // Trả về time gốc nếu parse lỗi
        }
    }

    // Getters
    public String getSymbol() { return symbol; }
    public String getTime() { return time; }
    public double getPrice() { return price; }
    public double getChange() { return change; }
    public double getPercentChange() { return percentChange; }
    public long getVolume() { return volume; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
}