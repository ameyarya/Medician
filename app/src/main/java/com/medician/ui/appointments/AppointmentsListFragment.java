package com.medician.ui.appointments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

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
import com.medician.ui.findDoctor.FindDoctorFragment;

import java.util.ArrayList;
import java.util.Iterator;

public class AppointmentsListFragment extends Fragment {

    private ListView usersList;
    private TextView noUsersText;
    private ArrayList<String> allUsers = new ArrayList<>();
    private ArrayList<String> allUsersAddress = new ArrayList<>();
    private ArrayList<String> allUserNames = new ArrayList<>();
    private int totalUsers = 0;
    private View root;
    private String loggedInUser;
    private ProgressDialog pd;
    private String type;
    private DatabaseReference db;
    private Button addApp;
    private AppointmentListAdapter appointmentListAdapter;
    private AppointmentsViewModel mViewModel;
    private TextView refresh;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityStatus.isConnected(getContext())) {
                // no connection
                pd = new ProgressDialog(getContext());
                pd.setMessage("Loading...");
                pd.show();
                usersList.setVisibility(View.GONE);
                noUsersText.setText("No Internet");
                noUsersText.setVisibility(View.VISIBLE);
                addApp.setVisibility(View.GONE);
                usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getContext(), "No Internet", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // connected
                refresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        refresh();
                    }
                });

                usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        UserDetails.appWith = allUserNames.get(position);
                        String otherName = allUserNames.get(position);
                        String fullname = allUsers.get(position);
                        Intent i = new Intent(getContext(), AppointmentDetails.class);
                        i.putExtra("appWithFull", fullname);
                        i.putExtra("otherName", otherName);
                        startActivity(i);
                    }
                });
                addApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Fragment fragment = new FindDoctorFragment();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_appointments, fragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Find A Doctor");
                        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
                        navigationView.getMenu().getItem(4).setChecked(true);
                    }
                });

                pd = new ProgressDialog(getContext());
                pd.setMessage("Loading...");
                pd.show();
                getReportsUsers();
                getAppointmentsUsers();
                if (totalUsers < 1) {
                    noUsersText.setText("No appointments found!");
                    noUsersText.setVisibility(View.VISIBLE);
                    usersList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersList.setVisibility(View.VISIBLE);
                    usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
                }
            }
            pd.dismiss();
        }
    };

    public static AppointmentsListFragment newInstance() {
        return new AppointmentsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_appointments, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        db.keepSynced(true);
        addApp = root.findViewById(R.id.addApp);
        usersList = root.findViewById(R.id.appList);
        noUsersText = root.findViewById(R.id.noAppText);
        noUsersText.setVisibility(View.GONE);
        loggedInUser = LocalSharedPreferencesData.getPreferredUserData(getContext(), "userName");
        type = LocalSharedPreferencesData.getPreferredUserData(getContext(), "type");
        refresh = root.findViewById(R.id.refreshApp);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUsersAddress();
        getReportsUsers();
        getAppointmentsUsers();
        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    private void refresh() {

        Fragment fragment = new AppointmentsListFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_appointments, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private void getReportsUsers() {
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
                                        addToList(snapshot.getValue().toString(), patient);
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
                                        addToList(snapshot.getValue().toString(), doctor);
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
//                                        addToList(snapshot.getValue().toString(), patient);
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

    private void addToList(String name, String key) {
        try {
            if (!allUsers.contains(name)) {
                allUsers.add(name);
                allUserNames.add(key);
                totalUsers++;
            }
            if (totalUsers < 1) {
                noUsersText.setVisibility(View.VISIBLE);
                usersList.setVisibility(View.GONE);
            } else {
                noUsersText.setVisibility(View.GONE);
                usersList.setVisibility(View.VISIBLE);
                appointmentListAdapter = new AppointmentListAdapter(getActivity(), allUsers, allUsersAddress);
                usersList.setAdapter(appointmentListAdapter);
            }
            pd.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
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
                                        addToList(snapshot.getValue().toString(), patient);
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
                                        addToList(snapshot.getValue().toString(), doctor);
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AppointmentsViewModel.class);
        // TODO: Use the ViewModel
    }

}