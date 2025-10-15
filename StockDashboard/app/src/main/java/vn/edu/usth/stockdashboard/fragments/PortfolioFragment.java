import androidx.lifecycle.ViewModelProvider;
import vn.edu.usth.stockdashboard.viewmodel.SharedStockViewModel;
import vn.edu.usth.stockdashboard.data.sse.StockData;

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Thiết lập RecyclerView
    recyclerView = view.findViewById(R.id.recyclerView_portfolio);
    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    portfolioAdapter = new PortfolioAdapter(portfolioList);
    recyclerView.setAdapter(portfolioAdapter);

    // ⚡ Kết nối ViewModel chia sẻ dữ liệu
    SharedStockViewModel sharedViewModel =
            new ViewModelProvider(requireActivity()).get(SharedStockViewModel.class);

    sharedViewModel.getStockLiveData().observe(getViewLifecycleOwner(), stockMap -> {
        if (stockMap != null && !stockMap.isEmpty()) {
            int changed = 0;
            for (int i = 0; i < portfolioList.size(); i++) {
                PortfolioItem item = portfolioList.get(i);
                StockData updated = stockMap.get(item.getSymbol());
                if (updated != null) {
                    item.setPrice(updated.getClose());
                    item.setChange(updated.getChangePercent());
                    changed++;
                }
            }
            if (changed > 0) {
                portfolioAdapter.notifyDataSetChanged();
                Log.d("PortfolioFragment", "💹 Updated " + changed + " items from Dashboard");
            }
        }
    });
}
