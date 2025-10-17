package vn.edu.usth.stockdashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class SharedStockViewModel extends ViewModel {

    // --- Portfolio update flag ---
    private final MutableLiveData<Boolean> portfolioUpdated = new MutableLiveData<>(false);

    private final MutableLiveData<List<StockItem>> stockListLiveData = new MutableLiveData<>();

    public void setStockList(List<StockItem> stockList) {
        stockListLiveData.postValue(stockList);
    }

    public LiveData<List<StockItem>> getStockList() {
        return stockListLiveData;
    }

    // --- Dashboard live stocks ---
    private final MutableLiveData<List<StockItem>> dashboardStocks = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<StockItem>> getDashboardStocks() {
        return dashboardStocks;
    }
    public void setDashboardStocks(List<StockItem> list) {
        dashboardStocks.setValue(list);
    }

    // --- Crypto live stocks ---
    private final MutableLiveData<List<StockItem>> cryptoStocks = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<StockItem>> getCryptoStocks() {
        return cryptoStocks;
    }
    public void setCryptoStocks(List<StockItem> list) {
        cryptoStocks.setValue(list);
    }
}
