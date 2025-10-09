package vn.edu.usth.stockdashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import vn.edu.usth.stockdashboard.fragments.DashboardFragment;
import vn.edu.usth.stockdashboard.fragments.PortfolioFragment;
import vn.edu.usth.stockdashboard.fragments.UserAccountFragment;

public class MainActivity extends BaseActivity {

    private String currentUsername;
    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Window window = getWindow();
        window.setNavigationBarColor(getResources().getColor(R.color.black));

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


//     Tạo lập Bottom Navigation

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    selectedFragment = new DashboardFragment();
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

    }


}