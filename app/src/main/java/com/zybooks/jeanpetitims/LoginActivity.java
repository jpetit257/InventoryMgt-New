package com.zybooks.jeanpetitims;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText user;
    private EditText pass;
    private TextView wrongUserPass;
    private Button login;

    private InventoryDatabase mInventoryDatabase;
    private int counter;
    private static final int REQUEST_SEND_MESSAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mInventoryDatabase = InventoryDatabase.getInstance(getApplicationContext());

        // If no user exists in the DB, redirect to Register
        boolean isUserExist = mInventoryDatabase.isUsersExist();

        if (!isUserExist) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }

        user = (EditText) findViewById(R.id.usernameText);
        pass = (EditText) findViewById(R.id.passwordText);
        login = (Button) findViewById(R.id.buttonLogin);
        wrongUserPass = (TextView) findViewById(R.id.failedLoginLabel);

        counter = 3;

        wrongUserPass.setVisibility(View.INVISIBLE);

        // Login when Login button is pressed
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(user.getText().toString(), pass.getText().toString());
            }
        });



        // Make the Enter button behave as the Tab button when focus is on the username field
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

        // Login when Enter button on the keyboard is pressed on the password field
        pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER && counter > 0) {
                    validate(user.getText().toString(), pass.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // Check if permission has still not been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            // if permission is not granted, then check if the user has already denied permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

            }
            else {
                // a pop up will appear asking for required permission
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SEND_SMS }, REQUEST_SEND_MESSAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Will check the requestCode
        switch (requestCode) {
            case REQUEST_SEND_MESSAGE: {

                // Check whether the length of grantResults is greater than 0 and is equal to PERMISSION_GRANTED
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted!
                }
                else {
                    // Permission denied!
                }
                return;
            }
        }
    }

    private void validate (String username, String password) {
        boolean validateResult = mInventoryDatabase.checkUserAndPassword(username, password);

        if (validateResult) {
            Intent intent = new Intent(LoginActivity.this, CategoryActivity.class);
            startActivity(intent);
        } else {
            counter--;

            if (counter == 2) {
                wrongUserPass.setText("Invalid login attempt.\n" + String.valueOf(counter) + " attempts remaining.");
                wrongUserPass.setVisibility(View.VISIBLE);
            }
            else if (counter == 1) {
                wrongUserPass.setText("Invalid login attempt.\n" + String.valueOf(counter) + " attempt remaining.");
            }
            else {
                wrongUserPass.setText("You have been locked out. Please try again later.");
                login.setEnabled(false);
            }
        }
    }

    private void sendSMS () {
        SmsManager smsManager =   SmsManager.getDefault();
        smsManager.sendTextMessage("555-521-5554", null, "Inventory is low", null, null);

        // Confirm that the message was sent
        Toast.makeText(this, "SMS message was sent successfully.", Toast.LENGTH_LONG).show();
    }
}