package vn.edu.usth.stockdashboard;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setStatusBarToMatchBackground();
    }
    private void setStatusBarToMatchBackground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View rootView = findViewById(android.R.id.content);
            Drawable background = rootView.getBackground();

            if (background instanceof ColorDrawable) {
                int backgroundColor = ((ColorDrawable) background).getColor();
                getWindow().setStatusBarColor(backgroundColor);
            }
        }
    }
}
