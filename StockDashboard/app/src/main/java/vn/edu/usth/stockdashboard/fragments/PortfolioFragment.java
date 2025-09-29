package vn.edu.usth.stockdashboard.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;

import java.util.ArrayList;
import java.util.List;

import vn.edu.usth.stockdashboard.PortfolioAdapter;
import vn.edu.usth.stockdashboard.R;
import vn.edu.usth.stockdashboard.Stock;

public class PortfolioFragment extends Fragment {

    private RecyclerView recyclerView;
    private PortfolioAdapter adapter;
    private List<Stock> stockList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        stockList = new ArrayList<>();
        stockList.add(new Stock("AAPL", "175.3 USD", "1000", "1200", "+200"));
        stockList.add(new Stock("GOOG", "2800 USD", "5000", "4800", "-200"));
        stockList.add(new Stock("TSLA", "750 USD", "2000", "2500", "+500"));
        stockList.add(new Stock("MSFT", "330.5 USD", "3000", "3100", "+100"));
        stockList.add(new Stock("AMZN", "134.2 USD", "4200", "4150", "-50"));
        stockList.add(new Stock("META", "305.6 USD", "2800", "3000", "+200"));
        stockList.add(new Stock("NVDA", "455.9 USD", "1500", "1700", "+200"));
        stockList.add(new Stock("VFS", "7.8 USD", "1500", "1400", "-100"));
        stockList.add(new Stock("VNI", "1245.6", "8000", "8050", "+50"));
        stockList.add(new Stock("USTH", "92.4 USD", "2200", "2000", "-200"));
        stockList.add(new Stock("BID", "42.3 USD", "3100", "3150", "+50"));
        stockList.add(new Stock("CTG", "32.1 USD", "2800", "2700", "-100"));
        stockList.add(new Stock("VCB", "88.5 USD", "1900", "2000", "+100"));
        stockList.add(new Stock("HPG", "21.6 USD", "2500", "2600", "+100"));
        stockList.add(new Stock("FPT", "89.7 USD", "3000", "3050", "+50"));
        stockList.add(new Stock("MWG", "45.2 USD", "1500", "1480", "-20"));
        stockList.add(new Stock("PNJ", "95.1 USD", "1200", "1220", "+20"));
        stockList.add(new Stock("VIC", "58.4 USD", "2100", "2050", "-50"));
        stockList.add(new Stock("SSI", "28.9 USD", "3200", "3300", "+100"));
        stockList.add(new Stock("VHM", "71.3 USD", "2700", "2650", "-50"));

        // Thêm logic cho Portfolio fragment ở đây

        recyclerView = view.findViewById(R.id.recyclerView);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);

        recyclerView.setLayoutManager(layoutManager);

        // Gắn adapter
        adapter = new PortfolioAdapter(stockList);
        recyclerView.setAdapter(adapter);

        return view;

    }
}
