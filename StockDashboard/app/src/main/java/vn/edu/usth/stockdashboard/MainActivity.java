package vn.edu.usth.stockdashboard;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private String currentUsername;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lấy username từ Intent
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "User";
        }

        // Khởi tạo FragmentManager
        fragmentManager = getSupportFragmentManager();

        // Khởi tạo BottomNavigationView
        setupBottomNavigation();

        // Hiển thị fragment mặc định (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }

    /**
     * Thiết lập Bottom Navigation
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_stocks) {
                    selectedFragment = new StocksFragment();
                } else if (itemId == R.id.nav_portfolio) {
                    selectedFragment = new PortfolioFragment();
                } else if (itemId == R.id.nav_account) {
                    selectedFragment = UserAccountFragment.newInstance(currentUsername);
                }

                if (selectedFragment != null) {
                    return loadFragment(selectedFragment);
                }
                return false;
            }
        });

        // Set default selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);
    }

    /**
     * Load fragment vào container
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
            return true;
        }
        return false;

    /*
     * Xử lý nút Back
     */
    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            finishAffinity();
        }
    }

}