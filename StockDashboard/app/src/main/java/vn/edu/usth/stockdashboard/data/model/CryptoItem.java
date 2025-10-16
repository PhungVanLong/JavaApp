package vn.edu.usth.stockdashboard.data.model;

public class CryptoItem {
    private String symbol;
    private double price;
    private String time;

    public CryptoItem(String symbol, double price, String time) {
        this.symbol = symbol;
        this.price = price;
        this.time = time;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public String getTime() {
        return time;
    }
}
