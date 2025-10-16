package vn.edu.usth.stockdashboard.data.model;

public class CryptoItem {
    private String symbol;
    private double price;
    private double open;
    private double changePercent;
    private String time;

    public CryptoItem(String symbol, double price, double open, double changePercent, String time) {
        this.symbol = symbol;
        this.price = price;
        this.open = open;
        this.changePercent = changePercent;
        this.time = time;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getOpen() {
        return open;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public String getTime() {
        return time;
    }

    public boolean isPriceUp() {
        return changePercent > 0;
    }

    public boolean isPriceDown() {
        return changePercent < 0;
    }
}