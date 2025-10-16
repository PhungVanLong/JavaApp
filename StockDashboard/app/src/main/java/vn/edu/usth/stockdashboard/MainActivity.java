package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;

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

    // Keep references to your fragments
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

        if (savedInstanceState == null) {
            // Add all fragments initially, dashboard is visible by default
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, dashboardFragment, "1") // Dashboard visible
                    .add(R.id.fragment_container, cryptoFragment, "2").hide(cryptoFragment)
                    .add(R.id.fragment_container, portfolioFragment, "3").hide(portfolioFragment)
                    .add(R.id.fragment_container, accountFragment, "4").hide(accountFragment)
                    .commit();
            activeFragment = dashboardFragment;
        }

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                loadFragment(dashboardFragment);
            } else if (itemId == R.id.nav_crypto) {
                loadFragment(cryptoFragment);
            } else if (itemId == R.id.nav_portfolio) {
                loadFragment(portfolioFragment);
            } else if (itemId == R.id.nav_account) {
                loadFragment(accountFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        if (activeFragment != fragment) {
            Log.d("FRAGMENT_DEBUG", "Switching to: " + fragment.getClass().getSimpleName());
            fragmentManager.beginTransaction()
                    .hide(activeFragment)
                    .show(fragment)
                    .commit();
            activeFragment = fragment;
        }
    }

}