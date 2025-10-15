package vn.edu.usth.stockdashboard.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.edu.usth.stockdashboard.data.sse.StockData;

public class SharedStockViewModel extends ViewModel {

    private final MutableLiveData<Map<String, StockData>> stockLiveData = new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<String>> userSymbolsLiveData = new MutableLiveData<>();

    public LiveData<Map<String, StockData>> getStockLiveData() {
        return stockLiveData;
    }

    public void updateStockData(Map<String, StockData> newData) {
        Map<String, StockData> current = stockLiveData.getValue();
        if (current == null) current = new HashMap<>();
        current.putAll(newData);
        stockLiveData.setValue(current);
    }

    // ====== Danh sách cổ phiếu người dùng sở hữu ======

    public LiveData<List<String>> getUserSymbols() {
        return userSymbolsLiveData;
    }

    public void setUserSymbols(List<String> symbols) {
        userSymbolsLiveData.setValue(symbols);
    }
}
