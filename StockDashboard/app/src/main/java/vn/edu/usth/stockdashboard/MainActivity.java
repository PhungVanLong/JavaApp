package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.usth.stockdashboard.fragments.CryptoFragment;
import vn.edu.usth.stockdashboard.fragments.DashboardFragment;
import vn.edu.usth.stockdashboard.fragments.PortfolioFragment;
import vn.edu.usth.stockdashboard.fragments.UserAccountFragment;

public class MainActivity extends BaseActivity {

    private String currentUsername;
    private FragmentManager fragmentManager;

    private TextView headerTitle;
    // gọi fragment
    final Fragment dashboardFragment = new DashboardFragment();
    final Fragment portfolioFragment = new PortfolioFragment();
    final Fragment cryptoFragment = new CryptoFragment();
    Fragment accountFragment;
    Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setNavigationBarColor(getResources().getColor(R.color.black));

        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "User";
        }

        accountFragment = UserAccountFragment.newInstance(currentUsername);
        fragmentManager = getSupportFragmentManager();

        headerTitle = findViewById(R.id.header_title);

        if (savedInstanceState == null) {
            // Add all fragments initially, dashboard is visible by default
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, dashboardFragment, "1") // Dashboard visible
                    .add(R.id.fragment_container, cryptoFragment, "2").hide(cryptoFragment)
                    .add(R.id.fragment_container, portfolioFragment, "3").hide(portfolioFragment)
                    .add(R.id.fragment_container, accountFragment, "4").hide(accountFragment)
                    .commit();
            activeFragment = dashboardFragment;
            updateHeaderTitle("Dashboard");
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                loadFragment(dashboardFragment);
                updateHeaderTitle("Dashboard");
            } else if (itemId == R.id.nav_crypto) {
                loadFragment(cryptoFragment);

                updateHeaderTitle("Crypto");
            } else if (itemId == R.id.nav_portfolio) {
                loadFragment(portfolioFragment);
                updateHeaderTitle("Portfolio");
            } else if (itemId == R.id.nav_account) {
                loadFragment(accountFragment);
                updateHeaderTitle("Account");
            }
            return true;
        });
    }
    private void updateHeaderTitle(String title) {
        if (headerTitle != null) {
            headerTitle.setText(title);
        }
    }
    private void loadFragment(Fragment fragment) {
        if (activeFragment != fragment) {
            fragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(fragment)
                    .commit();
            activeFragment = fragment;
        }
    }
}