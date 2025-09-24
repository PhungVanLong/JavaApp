package vn.edu.usth.stockdashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import vn.edu.usth.stockdashboard.data.DatabaseHelper;
public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    Button loginButton, registerButton;
    DatabaseHelper databaseHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
// setupz
        username = findViewById(R.id.usernameText);
        password = findViewById(R.id.passwordText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        databaseHelper = new DatabaseHelper(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                if (user.isEmpty() || pass.isEmpty())
                {
                    Toast.makeText(LoginActivity.this, "Please ENTER your USERNAME and PASSWORD!!!!!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    String loggedInUserName = databaseHelper.checkLogin(user, pass);
                    if (loggedInUserName!=null)
                    {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USERNAME", loggedInUserName);
                        startActivity(intent);
                        finish();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this, "Invalid Username", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }
}