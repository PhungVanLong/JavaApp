package vn.edu.usth.stockdashboard.data.model;

import com.google.gson.annotations.SerializedName;

public class StockApiResponse {
    @SerializedName("data")
    private StockData data;

    @SerializedName("success")
    private boolean success;

    @SerializedName("symbol")
    private String symbol;

    public StockData getData() { return data; }
    public boolean isSuccess() { return success; }
    public String getSymbol() { return symbol; }
}