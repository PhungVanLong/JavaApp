package vn.edu.usth.stockdashboard.data.sse;// Represents the object for each symbol, e.g., the "VNI" object

import com.google.gson.annotations.SerializedName;


public class StockSymbolData {
    @SerializedName("data")
    private StockData data;
    @SerializedName("success")
    private boolean success;
    @SerializedName("symbol")
    private String symbol;

    // Getters
    public StockData getData() { return data; }
    public boolean isSuccess() { return success; }
    public String getSymbol() { return symbol; }
}