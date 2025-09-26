package vn.edu.usth.stockdashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import vn.edu.usth.stockdashboard.data.DatabaseHelper;

public class UserAccountFragment extends Fragment {

    private TextView username;
    private Button logout;
    private DatabaseHelper databaseHelper;
    private String currentUsername;

    public UserAccountFragment() {
    }//để trống

//tạo fragment user
    public static UserAccountFragment newInstance(String username) {
        UserAccountFragment fragment = new UserAccountFragment();
        Bundle args = new Bundle();
        args.putString("USERNAME", username);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUsername = getArguments().getString("USERNAME");
        }
        databaseHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_user_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo views
        username = view.findViewById(R.id.UsernameText);
        logout = view.findViewById(R.id.logoutButton);

        // Hiển thị username
        if (currentUsername != null && !currentUsername.isEmpty()) {
            username.setText("Welcome " + currentUsername);
        } else {
            username.setText("Welcome User");
        }

        // Xử lý sự kiện logout
        logout.setOnClickListener(v -> showLogoutConfirmation());
    }


// sự kiện logout

    private void showLogoutConfirmation() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }


    //Thực hiện logout
    private void performLogout() {
        if (getContext() == null) return;

        try {
            // Hiển thị thông báo logout
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Tạo Intent để quay về LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Khởi động LoginActivity
            startActivity(intent);

            // Đóng activity hiện tại
            if (getActivity() != null) {
                getActivity().finish();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(), "Error during logout: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}