package com.zybooks.jeanpetitims;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^" +
            "(?=.*[A-Z])" +     // // At least one Uppercase
            "(?=.*[a-z])" +     // // At least one Lowercase
            "(?=.*[0-9])" +     // At least one Numeric character
            "(?=\\S+$)" +
            "(?=.*[!@#$%^&*+=?])" +
            ".{8,}" +
            "$");

    private EditText user;
    private EditText pass;
    private EditText repass;
    private TextView weakPassword;
    private Button register;

    private InventoryDatabase mInventoryDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = (EditText) findViewById(R.id.usernameText);
        pass = (EditText) findViewById(R.id.passwordText);
        repass = (EditText) findViewById(R.id.rePasswordText);
        weakPassword = (TextView) findViewById(R.id.weakPasswordLabel);
        register = (Button) findViewById(R.id.buttonRegister);

        mInventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());

        // Set the Weak password label to invisible
        weakPassword.setVisibility(View.INVISIBLE);

        // Make the Enter button behave as the Tab button when focus is on the Username field
        user.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Instrumentation inst = new Instrumentation();
                            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
                        }
                    }).start();
                    return true;
                }
                return false;
            }
        });

        // Make the Enter button behave as the Tab button when focus is on the Password field
        pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
                return false;
            }
        });

        // Register
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = user.getText().toString();
                String password = pass.getText().toString();
                String repassword = repass.getText().toString();

                // Set the Weak password label to invisible
                weakPassword.setVisibility(View.INVISIBLE);

                if (username.equals("") || password.equals("") || repassword.equals("")) {
                    Toast.makeText(RegisterActivity.this, "All fields are required. Please complete the form.", Toast.LENGTH_LONG).show();
                }
                else if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    weakPassword.setText("Password needs to contain:\n" +
                            "  * A mininum of 8 characters in total\n" +
                            "  * At least one uppercase letter\n" +
                            "  * At least one lowercase letter\n" +
                            "  * At least one numeric character\n" +
                            "  * At least one special character");
                    weakPassword.setVisibility(View.VISIBLE);
                }
                else {
                    if (password.equals(repassword)) {
                        boolean checkUserExistResult = mInventoryDatabase.checkUserExists(username);

                        if (checkUserExistResult) {
                            Toast.makeText(RegisterActivity.this, "Username already exists. Please use the login page to login.", Toast.LENGTH_LONG).show();
                        }
                        else {
                             boolean addUserResult = mInventoryDatabase.addUser(username, password);

                             if (addUserResult) {
                                 Toast.makeText(RegisterActivity.this, "User \"" + username + "\" was created successfully.", Toast.LENGTH_LONG).show();

                                 // Redirect user to the login page
                                 Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                 startActivity(intent);

                             }
                             else {
                                 Toast.makeText(RegisterActivity.this, "Oops. An exception has occurred. Please contact app creator if issue persists.", Toast.LENGTH_SHORT).show();
                             }
                        }
                    }
                    else {
                        Toast.makeText(RegisterActivity.this, "Passwords do not match.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}