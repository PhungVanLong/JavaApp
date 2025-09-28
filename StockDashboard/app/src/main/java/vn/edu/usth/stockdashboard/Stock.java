package vn.edu.usth.stockdashboard;

public class Stock {
    private String ticker;
    private String price;
    private String invested;
    private String current;
    private String pnl;

    public Stock(String ticker, String price, String invested, String current, String pnl) {
        this.ticker = ticker;
        this.price = price;
        this.invested = invested;
        this.current = current;
        this.pnl = pnl;
    }

    public String getTicker() { return ticker; }
    public String getPrice() { return price; }
    public String getInvested() { return invested; }
    public String getCurrent() { return current; }
    public String getPnl() { return pnl; }
}
