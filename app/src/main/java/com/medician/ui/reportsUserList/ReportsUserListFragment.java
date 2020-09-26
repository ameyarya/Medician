package com.medician.ui.reportsUserList;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.medician.ConnectivityStatus;
import com.medician.LocalSharedPreferencesData;
import com.medician.R;
import com.medician.ui.addReport.addReportActivity;

import java.util.ArrayList;
import java.util.Iterator;

public class ReportsUserListFragment extends Fragment {

    private ReportsUserListViewModel mViewModel;
    ListView usersList;
    TextView noUsersText;
    ArrayList<String> allUsers = new ArrayList<>();
    ArrayList<String> allUserNames = new ArrayList<>();
    int totalUsers = 0;
    View root;
    String loggedInUser;
    ProgressDialog pd;
    String type;
    DatabaseReference db;

    public static ReportsUserListFragment newInstance() {
        return new ReportsUserListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root =  inflater.inflate(R.layout.reports_user_list_fragment, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        db.keepSynced(true);
        usersList = root.findViewById(R.id.userList);
        noUsersText = root.findViewById(R.id.noUsers);
        noUsersText.setVisibility(View.GONE);
        loggedInUser = LocalSharedPreferencesData.getPreferredUserData(getContext(),"userName");
        type = LocalSharedPreferencesData.getPreferredUserData(getContext(), "type");
/*        pd = new ProgressDialog(getContext());
        pd.setMessage("Loading...");
        pd.show();
        getReportsUsers();
        getAppointmentsUsers();

        if (totalUsers < 1) {
            noUsersText.setVisibility(View.VISIBLE);
            usersList.setVisibility(View.GONE);
        } else {
            noUsersText.setVisibility(View.GONE);
            usersList.setVisibility(View.VISIBLE);
            usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
        }
        pd.dismiss();*/


        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String otherName = allUserNames.get(position);
                Intent i = new Intent(getContext(), ReportsForUser.class);
                i.putExtra("otherName", otherName);
                startActivity(i);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!ConnectivityStatus.isConnected(getContext())){
                // no connection
                pd = new ProgressDialog(getContext());
                pd.setMessage("Loading...");
                pd.show();
                if (totalUsers < 1) {
                    noUsersText.setText("No Network Found!");
                    noUsersText.setVisibility(View.VISIBLE);
                    usersList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersList.setVisibility(View.VISIBLE);
                    usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
                }
                pd.dismiss();

            }else {
                // connected
                pd = new ProgressDialog(getContext());
                pd.setMessage("Loading...");
                pd.show();
                getReportsUsers();
                getAppointmentsUsers();
                if (totalUsers < 1) {
                    if(type.equals("Doctor")){
                        noUsersText.setText("No connected patients found!");
                    }
                    else{
                        noUsersText.setText("No connected Doctors found!");
                    }
                    noUsersText.setVisibility(View.VISIBLE);
                    usersList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersList.setVisibility(View.VISIBLE);
                    usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
                }
                pd.dismiss();
            }
        }
    };


    public void getReportsUsers(){
        db.child("reports").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String name = snapshot.getKey();
                final String doctor = name.substring(0,name.indexOf("_"));
                final String patient = name.substring(name.indexOf("_")+1);
                if(type.equals("Doctor")){
                    if(doctor.equals(loggedInUser)) {
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
                }
                else {
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

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                if (totalUsers < 1) {
                    noUsersText.setVisibility(View.VISIBLE);
                    usersList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersList.setVisibility(View.VISIBLE);
                    usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
                }
                pd.dismiss();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
/*        db.child("reports").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    Iterator it = snapshot.getChildren().iterator();
                    Object val;
                    while(it.hasNext()){
                        val = it.next();
                        String name = ((com.google.firebase.database.DataSnapshot) val).getKey();
                        final String doctor = name.substring(0,name.indexOf("_"));
                        final String patient = name.substring(name.indexOf("_")+1);
                        if(type.equals("Doctor")){
                            if(doctor.equals(loggedInUser)) {
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
                        }
                        else{
                            if(patient.equals(loggedInUser)) {
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
                }
                else{
                    System.out.println("----------------------ENTER ELSE------------------");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    public void addToList(String name, String key){
        if(!allUsers.contains(name)){
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
            usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
        }
        pd.dismiss();
    }

    public void getAppointmentsUsers(){
        db.child("appointments").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String name = snapshot.getKey();
                final String doctor = name.substring(0,name.indexOf("_"));
                final String patient = name.substring(name.indexOf("_")+1);
                if(type.equals("Doctor")){
                    if(doctor.equals(loggedInUser)) {
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
                }
                else{
                    if(patient.equals(loggedInUser)) {
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

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (totalUsers < 1) {
                    noUsersText.setVisibility(View.VISIBLE);
                    usersList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersList.setVisibility(View.VISIBLE);
                    usersList.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, allUsers));
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
       /* db.child("appointments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    Iterator it = snapshot.getChildren().iterator();
                    Object val;
                    while(it.hasNext()){
                        val = it.next();
                        String name = ((com.google.firebase.database.DataSnapshot) val).getKey();
                        final String doctor = name.substring(0,name.indexOf("_"));
                        final String patient = name.substring(name.indexOf("_")+1);
                        if(type.equals("Doctor")){
                            if(doctor.equals(loggedInUser)) {
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
                        }
                        else{
                            if(patient.equals(loggedInUser)) {
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
                }
                else{
                    System.out.println("----------------------ENTER ELSE------------------");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ReportsUserListViewModel.class);
        // TODO: Use the ViewModel
    }

}