package vn.edu.usth.stockdashboard.data.sse.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import vn.edu.usth.stockdashboard.data.model.CandleData;
import java.util.List;

public interface CryptoHistoryApi {
    @GET("history")
    Call<List<CandleData>> getHistory(
            @Query("symbol") String symbol,
            @Query("interval") String interval,
            @Query("limit") int limit
    );
}