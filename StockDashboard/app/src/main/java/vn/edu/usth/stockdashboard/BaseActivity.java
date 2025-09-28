package vn.edu.usth.stockdashboard;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ẩn action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Setup transparent system bars
        setupTransparentSystemBars();
    }

    private void setupTransparentSystemBars() {
        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // System bars trong suốt
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        // Ẩn navigation bar background nhưng giữ gesture bar


        // Điều chỉnh màu icons
    }


}