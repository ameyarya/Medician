package com.medician.ui.home;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medician.ConnectivityStatus;
import com.medician.LocalSharedPreferencesData;
import com.medician.R;
import com.medician.UserDetails;
import com.medician.Values;
import com.medician.ui.appointments.AppointmentListAdapter;
import com.medician.ui.appointments.AppointmentsListFragment;
import com.medician.ui.findDoctor.FindDoctorFragment;
import com.medician.ui.reminders.RemindersFragment;
import com.medician.ui.reminders.RemindersListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeFragment extends Fragment {

    private int totalApp = 0;
    private List<String> nameArrayApp = new ArrayList<>();
    private int totalRem = 0;
    private List<String> nameArrayRem = new ArrayList<>();
    private List<String> dateArrayRem = new ArrayList<>();
    private List<String> allUsersAddress = new ArrayList<>();
    private HomeViewModel homeViewModel;
    private TextView noAppTextHome;
    private TextView noRemTextHome;
    private TextView textGreet;
    private ListView appointmentList;
    private ListView reminderList;
    private RemindersListAdapter remindersListAdapter;
    private AppointmentListAdapter appointmentListAdapter;
    private Button addNewApp;
    private Button appShowMore;
    private Button addNewRem;
    private Button remShowMore;
    private TextView tip;
    private DatabaseReference db;
    private String type;
    private String loggedInUser;
    private String fullTip = "";
    private View root;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityStatus.isConnected(getContext())) {
                // no connection

                //appointment
                noAppTextHome.setText("No Internet");
                noAppTextHome.setVisibility(View.VISIBLE);
                appointmentList.setVisibility(View.GONE);
                appShowMore.setVisibility(View.GONE);
                addNewApp.setVisibility(View.GONE);

                //reminders
                noRemTextHome.setText("No Internet");
                noRemTextHome.setVisibility(View.VISIBLE);
                reminderList.setVisibility(View.GONE);
                remShowMore.setVisibility(View.GONE);
                addNewRem.setVisibility(View.GONE);

                //health tip
                tip.setText("No Internet");
                tip.setVisibility(View.VISIBLE);

            } else {
                // connected

                //appointment
                if (totalApp < 1) {
                    noAppTextHome.setVisibility(View.VISIBLE);
                    appointmentList.setVisibility(View.GONE);
                    appShowMore.setVisibility(View.GONE);
                    if (UserDetails.type.equalsIgnoreCase("Patient"))
                        addNewApp.setVisibility(View.VISIBLE);
                    else
                        addNewApp.setVisibility(View.GONE);
                } else {
                    if (UserDetails.type.equalsIgnoreCase("Patient"))
                        addNewApp.setVisibility(View.VISIBLE);
                    else
                        addNewApp.setVisibility(View.GONE);
                    appShowMore.setVisibility(View.VISIBLE);
                    noAppTextHome.setVisibility(View.GONE);
                    appointmentList.setVisibility(View.VISIBLE);
                    appointmentListAdapter = new AppointmentListAdapter(getActivity(), nameArrayApp, allUsersAddress);
                    appointmentList.setAdapter(appointmentListAdapter);
                }

                //reminders
                if (totalRem < 1) {
                    noRemTextHome.setVisibility(View.VISIBLE);
                    reminderList.setVisibility(View.GONE);
                    remShowMore.setVisibility(View.GONE);
                } else {
                    addNewRem.setVisibility(View.VISIBLE);
                    remShowMore.setVisibility(View.VISIBLE);
                    noRemTextHome.setVisibility(View.GONE);
                    reminderList.setVisibility(View.VISIBLE);
                    remindersListAdapter = new RemindersListAdapter(getActivity(), nameArrayRem, dateArrayRem);
                    reminderList.setAdapter(remindersListAdapter);
                }

                //health tip
                tipOfTheDay(root);
            }
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        db.keepSynced(true);
        Firebase.setAndroidContext(getContext());
        UserDetails.fullName = LocalSharedPreferencesData.getPreferredUserData(getContext(), "fullName");
        UserDetails.username = LocalSharedPreferencesData.getPreferredUserData(getContext(), "userName");
        UserDetails.type = LocalSharedPreferencesData.getPreferredUserData(getContext(), "type");
        UserDetails.address = LocalSharedPreferencesData.getPreferredUserData(getContext(), "address");
        textGreet = root.findViewById(R.id.text_greet);
        textGreet.setText("Hello, " + UserDetails.fullName.split(" ")[0]);
        type = LocalSharedPreferencesData.getPreferredUserData(getContext(), "type");
        loggedInUser = LocalSharedPreferencesData.getPreferredUserData(getContext(), "userName");

        appointmentList = root.findViewById(R.id.appointmentList);
        noAppTextHome = root.findViewById(R.id.noAppTextHome);
        addNewApp = root.findViewById(R.id.btnAptAddNew);
        appShowMore = root.findViewById(R.id.btnAptShowMore);

        reminderList = root.findViewById(R.id.remindersList);
        noRemTextHome = root.findViewById(R.id.noRemTextHome);
        addNewRem = root.findViewById(R.id.btnRemAddNew);
        remShowMore = root.findViewById(R.id.btnRemShowMore);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        appointments(root);
        getUsersAddress();
        getAppointmentsUsers();

        reminders(root);
        tipOfTheDay(root);

        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    private void tipOfTheDay(View root) {

        tip = root.findViewById(R.id.tipOfTheDay);
        String urlTip = Values.URL + "/tips.json";

        final StringRequest requestTip = new StringRequest(Request.Method.GET, urlTip, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject obj = new JSONObject(s);
                    Iterator i = obj.keys();
                    String key = "";
                    String random = getRandomAlphabet(obj.length());
                    while (i.hasNext()) {
                        key = i.next().toString();
                        if (key.equals(random)) {
                            tip.setText(obj.getJSONObject(key).getString("tip"));
                            fullTip = obj.getJSONObject(key).getString("full");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("VolleyError" + volleyError);
            }
        });

        tip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog tipFullDialog = new AlertDialog.Builder(getContext()).create();
                tipFullDialog.setTitle("Health Tip");
                tipFullDialog.setMessage(fullTip);
                tipFullDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        tipFullDialog.dismiss();
                    }
                });
                tipFullDialog.show();
            }
        });

        RequestQueue rQueueTip = Volley.newRequestQueue(getContext());
        rQueueTip.add(requestTip);
    }

    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    private String getRandomAlphabet(int max) {
        int maxA = max + 97;
        int x = getRandomNumber(97, maxA);
        return Character.toString((char) x);
    }

    private void appointments(View root) {
        addNewApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new FindDoctorFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_home, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Find A Doctor");
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(4).setChecked(true);
            }
        });

        appShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new AppointmentsListFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_home, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Appointments");
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(2).setChecked(true);
            }
        });
    }

    private void reminders(View root) {
        addNewRem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new RemindersFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_home, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Reminders");
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(3).setChecked(true);
            }
        });

        remShowMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new RemindersFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_home, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Reminders");
                NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(3).setChecked(true);
            }
        });

        String urlRem = Values.URL + "/reminders.json";

        final StringRequest requestRem = new StringRequest(Request.Method.GET, urlRem, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccessRem(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("VolleyError" + volleyError);
            }
        });

        RequestQueue rQueueRem = Volley.newRequestQueue(getContext());
        rQueueRem.add(requestRem);
    }

    private void doOnSuccessApp() {
        if (totalApp < 1) {
            noAppTextHome.setVisibility(View.VISIBLE);
            appointmentList.setVisibility(View.GONE);
            appShowMore.setVisibility(View.GONE);
        } else {
            appShowMore.setVisibility(View.VISIBLE);
            noAppTextHome.setVisibility(View.GONE);
            appointmentList.setVisibility(View.VISIBLE);
            appointmentListAdapter = new AppointmentListAdapter(getActivity(), nameArrayApp, allUsersAddress);
            appointmentList.setAdapter(appointmentListAdapter);
        }
    }

    private void getAppointmentsUsers() {
        db.child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Iterator it = snapshot.getChildren().iterator();
                    Object val;
                    while (it.hasNext()) {
                        val = it.next();
                        String name = ((DataSnapshot) val).getKey();
                        final String doctor = name.substring(0, name.indexOf("_"));
                        final String patient = name.substring(name.indexOf("_") + 1);
                        if (type.equals("Doctor")) {
                            if (doctor.equals(loggedInUser)) {
                                db.child("users").child(patient).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        nameArrayApp.add(snapshot.getValue().toString());
                                        totalApp++;
                                        doOnSuccessApp();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        } else {
                            if (patient.equals(loggedInUser)) {
                                db.child("users").child(doctor).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        nameArrayApp.add(snapshot.getValue().toString());
                                        totalApp++;
                                        doOnSuccessApp();

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                    }
                } else {
                    Toast.makeText(getContext(), "DB error occurred, please restart the app!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUsersAddress() {
        db.child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Iterator it = snapshot.getChildren().iterator();
                    Object val;
                    while (it.hasNext()) {
                        val = it.next();
                        String name = ((DataSnapshot) val).getKey();
                        final String doctor = name.substring(0, name.indexOf("_"));
                        final String patient = name.substring(name.indexOf("_") + 1);
                        if (type.equals("Doctor")) {
                            if (doctor.equals(loggedInUser)) {
                                db.child("users").child(patient).child("address").child("addressText").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        allUsersAddress.add(snapshot.getValue().toString());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        } else {
                            if (patient.equals(loggedInUser)) {
                                db.child("users").child(doctor).child("address").child("addressText").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        allUsersAddress.add(snapshot.getValue().toString());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }

                    }
                } else {
                    Toast.makeText(getContext(), "DB error occurred, please restart the app!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void doOnSuccessRem(String s) {
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();
            String key = "";

            while (i.hasNext()) {
                key = i.next().toString();
                if (key.contains(UserDetails.username)) {
                    String title = "" + (key.split("_")[1]);
                    nameArrayRem.add(title);
                    dateArrayRem.add(obj.getJSONObject(key).getString("date"));
                    totalRem++;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (totalRem < 1) {
            noRemTextHome.setVisibility(View.VISIBLE);
            reminderList.setVisibility(View.GONE);
            remShowMore.setVisibility(View.GONE);
        } else {
            addNewRem.setVisibility(View.VISIBLE);
            remShowMore.setVisibility(View.VISIBLE);
            noRemTextHome.setVisibility(View.GONE);
            reminderList.setVisibility(View.VISIBLE);
            remindersListAdapter = new RemindersListAdapter(getActivity(), nameArrayRem, dateArrayRem);
            reminderList.setAdapter(remindersListAdapter);
        }

    }
}