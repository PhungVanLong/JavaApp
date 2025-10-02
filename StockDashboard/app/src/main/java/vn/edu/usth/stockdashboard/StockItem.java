package vn.edu.usth.stockdashboard;

public class StockItem {
    private String symbol;      // Mã cổ phiếu (vd: AAPL)
    private String time;        // Thời gian (vd: 12:12:00 AM)
    private double price;       // Giá
    private double change;      // Thay đổi (+/-)
    private int volume;         // Khối lượng giao dịch

    public StockItem(String symbol, String time, double price, double change, int volume) {
        this.symbol = symbol;
        this.time = time;
        this.price = price;
        this.change = change;
        this.volume = volume;
    }

    // --- Getter & Setter ---
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }

    public int getVolume() { return volume; }
    public void setVolume(int volume) { this.volume = volume; }
}
