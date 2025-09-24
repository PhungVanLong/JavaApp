package vn.edu.usth.stockdashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import vn.edu.usth.stockdashboard.data.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {
    EditText username, password;
    Button backLoginButton, registerButton;
    DatabaseHelper databaseHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        username = findViewById(R.id.usernameText);
        password = findViewById(R.id.passwordText);
        backLoginButton = findViewById(R.id.backLoginButton);
        registerButton = findViewById(R.id.registerButton);
        databaseHelper = new DatabaseHelper(this);
        backLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                String user = username.getText().toString().trim();
                String pass = password.getText().toString().trim();
                if (user.isEmpty() || pass.isEmpty())
                {
                    Toast.makeText(RegisterActivity.this, "Please ENTER your USERNAME and PASSWORD!!!!!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(databaseHelper.insertData(user,pass))
                    {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("USERNAME", user);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this, "Username already existed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }
}