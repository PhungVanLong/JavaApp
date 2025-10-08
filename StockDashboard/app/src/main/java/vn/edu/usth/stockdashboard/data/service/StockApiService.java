package vn.edu.usth.stockdashboard.data.service;

// StockApiService.java
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import vn.edu.usth.stockdashboard.data.model.StockApiResponse;

public interface StockApiService {
    @GET("api/stock/{symbol}/price")
    Call<StockApiResponse> getStockPrice(@Path("symbol") String symbol);
}