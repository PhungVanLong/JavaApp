/// file này về sau đổi thành user account interface

package vn.edu.usth.stockdashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import vn.edu.usth.stockdashboard.data.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    TextView username;
    DatabaseHelper databaseHelper;
    Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById(R.id.UsernameText);
        logout = findViewById(R.id.logoutButton);
        databaseHelper = new DatabaseHelper(this);

        // Lấy username
        String user = getIntent().getStringExtra("USERNAME");
        if (user != null && !user.isEmpty()) {
            username.setText("Welcome " + user);
        } else {
            username.setText("Welcome User");
        }


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });
    }


     // Hiển thị dialog xác nhận logout
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    doLogout();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }


    private void doLogout() {
        try {
            // Hiển thị thông báo
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Quay về LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);

            // Clear task
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }
        // bery nesessary
        catch (Exception e) {
            Toast.makeText(this, "Error during logout: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


}