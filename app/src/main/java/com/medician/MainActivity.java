package com.medician;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.medician.ui.addReport.addReportActivity;
import com.medician.ui.reportsUserList.ReportClass;
import com.medician.ui.reportsUserList.ReportsForUser;

public class MainActivity extends AppCompatActivity {
    Button loginBtn, registerBtn;
    TextView parentLayout, noNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String savedSessionUserName = LocalSharedPreferencesData.getPreferredUserData(MainActivity.this,"userName");
        if(savedSessionUserName.length() != 0) {
            Intent userDashboardIntent = new Intent(MainActivity.this, Dashboard.class);
            startActivity(userDashboardIntent);
        }
        else {
            setContentView(R.layout.activity_main);
            loginBtn = findViewById(R.id.loginButton);
            registerBtn = findViewById(R.id.registerButton);
            parentLayout = findViewById(R.id.welcomeMsg);
            noNetwork = findViewById(R.id.noNetwork);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getApplicationContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getApplicationContext().unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("RestrictedApi")
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!ConnectivityStatus.isConnected(getApplicationContext())){
                // no connection
                noNetwork.setVisibility(View.VISIBLE);
                loginBtn.setEnabled(false);
                registerBtn.setEnabled(false);
                loginBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));
                registerBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));

                Snackbar snackbar = Snackbar.make(loginBtn, "Looks like you do not have network. Either enable Wi-Fi or mobile data!", Snackbar.LENGTH_LONG).setAction("Action", null);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();

            }else {
                // connected

                noNetwork.setVisibility(View.GONE);
                loginBtn.setEnabled(true);
                registerBtn.setEnabled(true);
                loginBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));
                registerBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));
                loginBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        login();
                    }
                });

                registerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        register();
                    }
                });

            }
        }
    };

    public void login() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void register() {
        final Intent intentD = new Intent(this, RegisterDoctor.class);
        final Intent intentP = new Intent(this, RegisterPatient.class);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Registration user type!");
        alertDialog.setMessage("Please select the user type you want to register as!");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Doctor", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                startActivity(intentD);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Patient", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(intentP);
            }
        });
        alertDialog.show();
    }

}