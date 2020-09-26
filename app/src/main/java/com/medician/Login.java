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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {
    TextView register, loginScreen;
    EditText username, password;
    Button loginButton;
    String user, pass;

    @Override
    public void onBackPressed() {
        final AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
        alertDialog.setTitle("Exit Application");
        alertDialog.setMessage("Do you really wanna exit the application?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void userTypePopUp(View view){
        final Intent intentD = new Intent(Login.this, RegisterDoctor.class);
        final Intent intentP = new Intent(Login.this, RegisterPatient.class);
        final AlertDialog alertDialog = new AlertDialog.Builder(Login.this).create();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        String savedSessionUserName = LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "userName");
        if (savedSessionUserName.length() != 0) {
            Intent userDashboardIntent = new Intent(Login.this, Dashboard.class);
            userDashboardIntent.putExtra("userName", user);
            startActivity(userDashboardIntent);
        } else {
            register = (TextView) findViewById(R.id.register);
            username = (EditText) findViewById(R.id.username);
            password = (EditText) findViewById(R.id.password);
            loginButton = (Button) findViewById(R.id.loginButton);
            loginScreen = findViewById(R.id.loginScreen);

            /*register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userTypePopUp(v);
                }
            });

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = username.getText().toString();
                    pass = password.getText().toString();
                    if (user.equals("")) {
                        username.setError("Can't be blank");
                    } else if (pass.equals("")) {
                        password.setError("Can't be blank");
                    } else {
                        String url = Values.URL + "/users.json";
                        final ProgressDialog pd = new ProgressDialog(Login.this);
                        pd.setMessage("Loading...");
                        pd.show();

                        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String s) {
                                if (s.equals("null")) {
                                    Toast.makeText(Login.this, "User not found", Toast.LENGTH_LONG).show();
                                } else {
                                    try {
                                        JSONObject obj = new JSONObject(s);

                                        if (!obj.has(user)) {
                                            Toast.makeText(Login.this,
                                                    "User not found", Toast.LENGTH_LONG).show();
                                        } else if (obj.getJSONObject(user).getString("password").equals(pass)) {

                                            UserDetails.username = user;
                                            UserDetails.password = pass;
                                            UserDetails.type = obj.getJSONObject(user).getString("type");
                                            UserDetails.fullName = obj.getJSONObject(user).getString("name");
                                            UserDetails.address = obj.getJSONObject(user)
                                                    .getJSONObject("address")
                                                    .getString("addressText");


                                            UserDetails.speciality = obj.getJSONObject(user)
                                                    .getString("speciality");
                                            System.out.println("139 :: "+UserDetails.speciality);
                                            if(obj.getJSONObject(user).has("profilePicturePath"))  {
                                                System.out.println("here here here 136 Login");
                                                UserDetails.profilePicPath = obj.getJSONObject(user)
                                                        .getString("profilePicturePath");
                                            }

                                            LocalSharedPreferencesData.savePreferredUserData(
                                                    getApplicationContext(), UserDetails.username,
                                                    UserDetails.fullName, UserDetails.type,
                                                    UserDetails.address, UserDetails.profilePicPath, UserDetails.speciality);
                                            Toast.makeText(Login.this, "Logged in as: " + LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), Toast.LENGTH_LONG).show();

                                            Intent userDashboardIntent = new Intent(Login.this, Dashboard.class);
                                            userDashboardIntent.putExtra("userName", user);
                                            startActivity(userDashboardIntent);

                                        } else {
                                            Toast.makeText(Login.this, "Incorrect password", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                pd.dismiss();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                System.out.println("" + volleyError);
                                pd.dismiss();
                            }
                        });

                        RequestQueue rQueue = Volley.newRequestQueue(Login.this);
                        rQueue.add(request);
                    }

                }
            });*/
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

                register.setVisibility(View.GONE);

                loginButton.setEnabled(false);
                loginButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));
                username.setEnabled(false);
                password.setEnabled(false);
                Snackbar snackbar = Snackbar.make(loginButton, "Looks like you do not have network. Either enable Wi-Fi or mobile data!", Snackbar.LENGTH_LONG).setAction("Action", null);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();

            }else {
                loginButton.setEnabled(true);
                loginButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));
                username.setEnabled(true);
                password.setEnabled(true);
                register.setVisibility(View.VISIBLE);
                // connected
                register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        userTypePopUp(v);
                    }
                });

                loginButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user = username.getText().toString();
                        pass = password.getText().toString();
                        if (user.equals("")) {
                            username.setError("Can't be blank");
                        } else if (pass.equals("")) {
                            password.setError("Can't be blank");
                        } else {
                            String url = Values.URL + "/users.json";
                            final ProgressDialog pd = new ProgressDialog(Login.this);
                            pd.setMessage("Loading...");
                            pd.show();

                            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    if (s.equals("null")) {
                                        Toast.makeText(Login.this, "User not found", Toast.LENGTH_LONG).show();
                                    } else {
                                        try {
                                            JSONObject obj = new JSONObject(s);

                                            if (!obj.has(user)) {
                                                Toast.makeText(Login.this,
                                                        "User not found", Toast.LENGTH_LONG).show();
                                            } else if (obj.getJSONObject(user).getString("password").equals(pass)) {

                                                UserDetails.username = user;
                                                UserDetails.password = pass;
                                                UserDetails.type = obj.getJSONObject(user).getString("type");
                                                UserDetails.fullName = obj.getJSONObject(user).getString("name");
                                                UserDetails.address = obj.getJSONObject(user)
                                                        .getJSONObject("address")
                                                        .getString("addressText");


                                                UserDetails.speciality = obj.getJSONObject(user)
                                                        .getString("speciality");
                                                System.out.println("139 :: "+UserDetails.speciality);
                                                if(obj.getJSONObject(user).has("profilePicturePath"))  {
                                                    System.out.println("here here here 136 Login");
                                                    UserDetails.profilePicPath = obj.getJSONObject(user)
                                                            .getString("profilePicturePath");
                                                }

                                                LocalSharedPreferencesData.savePreferredUserData(
                                                        getApplicationContext(), UserDetails.username,
                                                        UserDetails.fullName, UserDetails.type,
                                                        UserDetails.address, UserDetails.profilePicPath, UserDetails.speciality);
                                                Toast.makeText(Login.this, "Logged in as: " + LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), Toast.LENGTH_LONG).show();

                                                Intent userDashboardIntent = new Intent(Login.this, Dashboard.class);
                                                userDashboardIntent.putExtra("userName", user);
                                                startActivity(userDashboardIntent);

                                            } else {
                                                Toast.makeText(Login.this, "Incorrect password", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    pd.dismiss();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    System.out.println("" + volleyError);
                                    pd.dismiss();
                                }
                            });

                            RequestQueue rQueue = Volley.newRequestQueue(Login.this);
                            rQueue.add(request);
                        }

                    }
                });
            }
        }
    };
}
