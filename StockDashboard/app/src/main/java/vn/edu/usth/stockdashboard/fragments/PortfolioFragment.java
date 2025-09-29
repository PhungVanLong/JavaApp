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
        // Thêm logic cho Portfolio fragment ở đây

//=======
        recyclerView = view.findViewById(R.id.recyclerView);

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);

        recyclerView.setLayoutManager(layoutManager);

        // Gắn adapter
        adapter = new PortfolioAdapter(stockList);
        recyclerView.setAdapter(adapter);

        return view;
//>>>>>>> main:StockDashboard/app/src/main/java/vn/edu/usth/stockdashboard/PortfolioFragment.java
    }
}
