package vn.edu.usth.stockdashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import vn.edu.usth.stockdashboard.data.model.StockItem;

public class SharedStockViewModel extends ViewModel {

    // --- Portfolio update flag ---
    private final MutableLiveData<Boolean> portfolioUpdated = new MutableLiveData<>(false);

    public LiveData<Boolean> getPortfolioUpdated() {
        return portfolioUpdated;
    }

    public void notifyPortfolioUpdated() {
        portfolioUpdated.setValue(true);
    }

    public void resetFlag() {
        portfolioUpdated.setValue(false);
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
