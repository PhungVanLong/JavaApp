package vn.edu.usth.stockdashboard;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.edu.usth.stockdashboard.fragments.DashboardFragment;
import vn.edu.usth.stockdashboard.fragments.PortfolioFragment;
import vn.edu.usth.stockdashboard.fragments.UserAccountFragment;

public class MainActivity extends BaseActivity {

    private String currentUsername;
    private FragmentManager fragmentManager;
    // Keep references to your fragments
    final Fragment dashboardFragment = new DashboardFragment();
    final Fragment portfolioFragment = new PortfolioFragment();
    Fragment accountFragment; // Will be initialized with username
    Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "User";
        }

        // Initialize the account fragment with username
        accountFragment = UserAccountFragment.newInstance(currentUsername);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // Add all fragments initially and hide them
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, accountFragment, "3").hide(accountFragment)
                    .add(R.id.fragment_container, portfolioFragment, "2").hide(portfolioFragment)
                    .add(R.id.fragment_container, dashboardFragment, "1").commit(); // Add dashboard last, it will be visible
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
            } else if (itemId == R.id.nav_portfolio) {
                loadFragment(portfolioFragment);
            } else if (itemId == R.id.nav_account) {
                loadFragment(accountFragment);
            }
            return true;
        });
    }

    // This method now uses show/hide instead of replace
    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }
}