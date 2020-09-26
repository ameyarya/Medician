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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class RegisterDoctor extends AppCompatActivity {

    private static int AUTOCOMPLETE_REQ_CODE = 100;
    EditText username, fullname, password, address;
    Button registerButton;
    String user, pass, name, addr, speciality;
    TextView login, change;
    double latitude;
    double longitude;
    Spinner spinner;

    @Override
    public void onBackPressed() {
        final AlertDialog alertDialog = new AlertDialog.Builder(RegisterDoctor.this).create();
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

    private void userTypePopUp() {
        final Intent intentD = new Intent(RegisterDoctor.this, RegisterDoctor.class);
        final Intent intentP = new Intent(RegisterDoctor.this, RegisterPatient.class);
        final AlertDialog alertDialog = new AlertDialog.Builder(RegisterDoctor.this).create();
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
        setContentView(R.layout.activity_register_doctor);

        username = (EditText) findViewById(R.id.username);
        fullname = (EditText) findViewById(R.id.fullname);
        password = (EditText) findViewById(R.id.password);
        address = (EditText) findViewById(R.id.address);

        spinner = findViewById(R.id.newDoctorSpeciality);
        List<String> arrayList = Values.specialities;
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                speciality = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Places.initialize(getApplicationContext(), "AIzaSyDV4Hv1WpxDWjSBSP2TD59-ZwYqc5eoqak");

/*        address.setFocusable(false);
        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME,
                        Place.Field.LAT_LNG);
                System.out.println(fieldList);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fieldList).build(RegisterDoctor.this);
                startActivityForResult(intent, AUTOCOMPLETE_REQ_CODE);
            }
        });*/

        Firebase.setAndroidContext(this);
        registerButton = (Button) findViewById(R.id.registerButton);
        login = (TextView) findViewById(R.id.login);
        /*login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterDoctor.this, Login.class));
            }
        });*/
        change = (TextView) findViewById(R.id.changeD);
       /* change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userTypePopUp();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = username.getText().toString();
                pass = password.getText().toString();
                name = fullname.getText().toString();
                name = Values.capitalizeString(name);
                addr = address.getText().toString();
                if (user.equals("")) {
                    username.setError("Can't be blank");
                } else if (name.equals("")) {
                    fullname.setError("Can't be blank");
                } else if (pass.equals("")) {
                    password.setError("Can't be blank");
                } else if (!user.matches("[A-Za-z0-9]+")) {
                    username.setError("Only alphabet or number allowed");
                } else if (user.length() < 5) {
                    username.setError("At least 5 characters long");
                } else if (pass.length() < 5) {
                    password.setError("At least 5 characters long");
                } else if (addr.length() == 0) {
                    address.setError("Can't be blank");
                } else {
                    final ProgressDialog pd = new ProgressDialog(RegisterDoctor.this);
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = Values.URL + "/users.json";

                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {
                            Firebase reference = new Firebase(Values.URL + "/users");

                            if (s.equals("null")) {
                                reference.child(user).child("password").setValue(pass);
                                reference.child(user).child("count").setValue(0);
                                Toast.makeText(RegisterDoctor.this, "Registration successful", Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    JSONObject obj = new JSONObject(s);

                                    if (!obj.has(user)) {
                                        reference.child(user).child("name").setValue(name);
                                        reference.child(user).child("password").setValue(pass);
                                        reference.child(user).child("type").setValue("Doctor");
                                        reference.child(user).child("speciality").setValue(speciality);
                                        reference.child(user).child("address").child("addressText")
                                                .setValue(addr);
                                        reference.child(user).child("address").child("latitude")
                                                .setValue(latitude);
                                        reference.child(user).child("address").child("longitude")
                                                .setValue(longitude);
                                        Toast.makeText(RegisterDoctor.this, "Registration successful, please login now!", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterDoctor.this, "Username already exists", Toast.LENGTH_LONG).show();
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

                    RequestQueue rQueue = Volley.newRequestQueue(RegisterDoctor.this);
                    rQueue.add(request);
                }
            }
        });*/
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
                address.setEnabled(false);
                login.setVisibility(View.GONE);
                change.setEnabled(false);
                registerButton.setEnabled(false);
                registerButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));
                username.setEnabled(false);
                fullname.setEnabled(false);
                password.setEnabled(false);

                Snackbar snackbar = Snackbar.make(address, "Looks like you do not have network. Either enable Wi-Fi or mobile data!", Snackbar.LENGTH_LONG).setAction("Action", null);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();


            }else {
                //connected
                username.setEnabled(true);
                fullname.setEnabled(true);
                password.setEnabled(true);
                address.setEnabled(true);
                login.setVisibility(View.VISIBLE);
                change.setEnabled(true);
                registerButton.setEnabled(true);
                registerButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));

                address.setFocusable(false);
                address.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME,
                                Place.Field.LAT_LNG);
                        System.out.println(fieldList);
                        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                                fieldList).build(RegisterDoctor.this);
                        startActivityForResult(intent, AUTOCOMPLETE_REQ_CODE);
                    }
                });

                login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(RegisterDoctor.this, Login.class));
                    }
                });

                change.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userTypePopUp();
                    }
                });

                registerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user = username.getText().toString();
                        pass = password.getText().toString();
                        name = fullname.getText().toString();
                        name = Values.capitalizeString(name);
                        addr = address.getText().toString();
                        if (user.equals("")) {
                            username.setError("Can't be blank");
                        } else if (pass.equals("")) {
                            password.setError("Can't be blank");
                        } else if (!user.matches("[A-Za-z0-9]+")) {
                            username.setError("Only alphabet or number allowed");
                        } else if (user.length() < 5) {
                            username.setError("At least 5 characters long");
                        } else if (pass.length() < 5) {
                            password.setError("At least 5 characters long");
                        } else if (addr.length() == 0) {
                            address.setError("Can't be blank");
                        } else {
                            final ProgressDialog pd = new ProgressDialog(RegisterDoctor.this);
                            pd.setMessage("Loading...");
                            pd.show();

                            String url = Values.URL + "/users.json";

                            StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String s) {
                                    Firebase reference = new Firebase(Values.URL + "/users");

                                    if (s.equals("null")) {
                                        reference.child(user).child("password").setValue(pass);
                                        reference.child(user).child("count").setValue(0);
                                        Toast.makeText(RegisterDoctor.this, "Registration successful", Toast.LENGTH_LONG).show();
                                    } else {
                                        try {
                                            JSONObject obj = new JSONObject(s);

                                            if (!obj.has(user)) {
                                                reference.child(user).child("name").setValue(name);
                                                reference.child(user).child("password").setValue(pass);
                                                reference.child(user).child("type").setValue("Doctor");
                                                reference.child(user).child("speciality").setValue(speciality);
                                                reference.child(user).child("address").child("addressText")
                                                        .setValue(addr);
                                                reference.child(user).child("address").child("latitude")
                                                        .setValue(latitude);
                                                reference.child(user).child("address").child("longitude")
                                                        .setValue(longitude);
                                                Toast.makeText(RegisterDoctor.this, "Registration successful, please login now!", Toast.LENGTH_LONG).show();
                                                finish();
                                            } else {
                                                Toast.makeText(RegisterDoctor.this, "Username already exists", Toast.LENGTH_LONG).show();
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

                            RequestQueue rQueue = Volley.newRequestQueue(RegisterDoctor.this);
                            rQueue.add(request);
                        }
                    }
                });

            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTOCOMPLETE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                addr = place.getAddress();
                LatLng latLng = place.getLatLng();
                System.out.println(addr);
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                address.setText(addr);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                address.setError("Invalid Address-Try again");
            } else if (resultCode == RESULT_CANCELED) {
                address.setError("Address cannot be left empty");
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}