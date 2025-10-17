package vn.edu.usth.stockdashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedStockViewModel extends ViewModel {
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
}
