package com.medician.ui.reportsUserList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import static java.security.AccessController.getContext;

public class ReportsForUser extends AppCompatActivity {
    String patientName;
    private ListView reports;
    FloatingActionButton addReport;
    private ArrayAdapter<ReportClass> adapter;
    private ArrayList<ReportClass> reportArrayList;
    String loggedInUser;
    ProgressDialog pd;
    TextView noReports;
    String type;
    DatabaseReference db;
    View parentLayout;
    AlertDialog.Builder builder;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_for_user);

        parentLayout = findViewById(R.id.reportsForUser);
        db = FirebaseDatabase.getInstance().getReference();
        db.keepSynced(true);
        reportArrayList = new ArrayList<>();
        adapter = new ReportsAdapter();
        reports = findViewById(R.id.reportList);
        loggedInUser = LocalSharedPreferencesData.getPreferredUserData(this,"userName");
        patientName = getIntent().getStringExtra("otherName");
        addReport = findViewById(R.id.addReport);
        noReports = findViewById(R.id.noReports);
        noReports.setVisibility(View.GONE);
        reports.setVisibility(View.GONE);
        type = LocalSharedPreferencesData.getPreferredUserData(this, "type");

        if(type.equals("Patient")){
            addReport.setVisibility(View.GONE);
        }
/*        if(reportArrayList.size() < 1){
            noReports.setVisibility(View.VISIBLE);
            reports.setVisibility(View.GONE);
        }
        else{
            noReports.setVisibility(View.GONE);
            reports.setAdapter(new ReportsAdapter());
            reports.setVisibility(View.VISIBLE);
        }*/
        getApplication().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }


    public void getData(){
        reportArrayList = new ArrayList<>();
        String childName;
        if(type.equals("Doctor")) {
            childName = loggedInUser + "_" + patientName;
        }
        else{
            childName = patientName + "_" + loggedInUser;
        }
        db.child("reports").child(childName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(type.equals("Doctor")) {
                    String patientName = snapshot.child("patientName").getValue().toString().trim();
                    final String reportType = snapshot.child("reportType").getValue().toString().trim();
                    final String imageID = snapshot.child("imageId").getValue().toString().trim();
                    db.child("users").child(patientName).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            addToList(snapshot.getValue().toString(),LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), reportType, imageID );
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else{
                    String doctorName = snapshot.child("doctorName").getValue().toString().trim();
                    final String reportType = snapshot.child("reportType").getValue().toString().trim();
                    final String imageID = snapshot.child("imageId").getValue().toString().trim();
                    db.child("users").child(doctorName).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            addToList(snapshot.getValue().toString(),LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), reportType, imageID );
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
/*        db.child("reports").child(childName).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    Iterator it = snapshot.getChildren().iterator();
                    Object val;
                    while(it.hasNext()){
                        val = it.next();
                        if(type.equals("Doctor")) {
                            String patientName = ((com.google.firebase.database.DataSnapshot) val).child("patientName").getValue().toString().trim();
                            final String reportType = ((com.google.firebase.database.DataSnapshot) val).child("reportType").getValue().toString().trim();
                            final String imageID = ((com.google.firebase.database.DataSnapshot) val).child("imageId").getValue().toString().trim();
                            db.child("users").child(patientName).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    addToList(snapshot.getValue().toString(),LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), reportType, imageID );
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else{
                            String doctorName = ((com.google.firebase.database.DataSnapshot) val).child("doctorName").getValue().toString().trim();
                            final String reportType = ((com.google.firebase.database.DataSnapshot) val).child("reportType").getValue().toString().trim();
                            final String imageID = ((com.google.firebase.database.DataSnapshot) val).child("imageId").getValue().toString().trim();
                            db.child("users").child(doctorName).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    addToList(snapshot.getValue().toString(),LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), reportType, imageID );
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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

    public void addToList(String name, String loggedInUserName, String reportType, String imageId){
        reportArrayList.add(new ReportClass(name, loggedInUserName, reportType, imageId));
        if(reportArrayList.size() < 1){
            noReports.setVisibility(View.VISIBLE);
            reports.setVisibility(View.GONE);
        }
        else{
            noReports.setVisibility(View.GONE);
            reports.setAdapter(new ReportsAdapter());
            reports.setVisibility(View.VISIBLE);
        }
        if(pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onResume() {
        super.onResume();
        getData();
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
                pd = new ProgressDialog(ReportsForUser.this);
                pd.setMessage("Loading...");
                pd.show();
                if(reportArrayList.size() < 1){
                    noReports.setVisibility(View.VISIBLE);
                    reports.setVisibility(View.GONE);
                    noReports.setText("Looks like there is no network connectivity. Either enable Wi-Fi or mobile data!");
                }
                else{
                    noReports.setVisibility(View.GONE);
                    reports.setAdapter(new ReportsAdapter());
                    reports.setVisibility(View.VISIBLE);
                }
                addReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Snackbar snackbar = Snackbar.make(parentLayout, "Looks like you do not have network. Either enable Wi-Fi or mobile data!", Snackbar.LENGTH_LONG).setAction("Action", null);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.show();
                    }
                });

                reports.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                   @Override
                                                   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                       Snackbar snackbar = Snackbar.make(parentLayout, "Looks like you do not have network. Either enable Wi-Fi or mobile data!", Snackbar.LENGTH_LONG).setAction("Action", null);
                                                       View snackbarView = snackbar.getView();
                                                       snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                                       snackbar.setActionTextColor(Color.WHITE);
                                                       snackbar.show();
                                                   }
                                               });
                pd.dismiss();

            }else {
                // connected
                pd = new ProgressDialog(ReportsForUser.this);
                pd.setMessage("Loading...");
                pd.show();
                if(reportArrayList.size() < 1){
                    noReports.setVisibility(View.VISIBLE);
                    reports.setVisibility(View.GONE);
                }
                else{
                    noReports.setVisibility(View.GONE);
                    reports.setAdapter(new ReportsAdapter());
                    reports.setVisibility(View.VISIBLE);
                }
                pd.dismiss();
                addReport.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(ReportsForUser.this, addReportActivity.class);
                        i.putExtra("patientName", patientName);
                        startActivity(i);
                    }
                });
                reports.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ReportClass selectedImage = reportArrayList.get(position);
                        String imageId = selectedImage.getImageID();
                        ImageView image = new ImageView(ReportsForUser.this);
                        setImage(image, imageId);

                        if(type.equals("Doctor")) {
                            builder =
                                    new AlertDialog.Builder(ReportsForUser.this).
                                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).setTitle(selectedImage.getType() + " Details for " + selectedImage.getPatientName()).
                                            setView(image);
                        }
                        else {
                            builder =
                                    new AlertDialog.Builder(ReportsForUser.this).
                                            setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).setTitle(selectedImage.getType() + " Details for " + selectedImage.getDoctorName()).
                                            setView(image);
                        }
                        builder.create().show();
                    }
                });
            }
        }
    };

    public void setImage(final ImageView v, String imageId){
            FirebaseApp.initializeApp(ReportsForUser.this);
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://medician-neu.appspot.com/Images/").child(imageId);
            try {
                final File file = File.createTempFile("image", "jpg");
                ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        v.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ReportsForUser.this, "Could not load image. Check connectivity and try again", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private class ReportsAdapter extends ArrayAdapter<ReportClass> {
        public ReportsAdapter(){
            super(ReportsForUser.this, R.layout.patient_reports_listview_row, reportArrayList);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = getLayoutInflater().inflate(R.layout.patient_reports_listview_row, parent, false);
            }

            ReportClass currURL = reportArrayList.get(position);
            TextView patientName= convertView.findViewById(R.id.patientName);
            TextView doctorName = convertView.findViewById(R.id.doctorName);
            TextView type = convertView.findViewById(R.id.reportType);
            final ImageView image = convertView.findViewById(R.id.reportThumbnail);

            String imageId = currURL.getImageID();
            setImage(image, imageId);
            patientName.setText(currURL.getPatientName());
            doctorName.setText(currURL.getDoctorName());
            type.setText(currURL.getType());

            return convertView;
        }
    }
}