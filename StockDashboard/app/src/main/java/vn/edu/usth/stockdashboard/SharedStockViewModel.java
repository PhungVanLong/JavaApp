package vn.edu.usth.stockdashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import vn.edu.usth.stockdashboard.StockItem;

public class SharedStockViewModel extends ViewModel {

    private final MutableLiveData<List<StockItem>> stockListLiveData = new MutableLiveData<>();

    public void setStockList(List<StockItem> stockList) {
        stockListLiveData.postValue(stockList);
    }

    public LiveData<List<StockItem>> getStockList() {
        return stockListLiveData;
    }
}
