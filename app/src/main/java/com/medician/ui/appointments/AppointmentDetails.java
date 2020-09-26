package com.medician.ui.appointments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.medician.LocalSharedPreferencesData;
import com.medician.R;
import com.medician.UserDetails;
import com.medician.Values;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class AppointmentDetails extends AppCompatActivity {

    String patientName;
    String doctorName;
    FloatingActionButton addReport;
    String loggedInUser;
    ProgressDialog pd;
    TextView noReports;
    String type;
    DatabaseReference db;
    View parentLayout;
    AlertDialog.Builder builder;
    private ListView reports;
    private ArrayAdapter<Appointment> adapter;
    private ArrayList<Appointment> reportArrayList;
    //private ConnectionDetector detector;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

//        setTitle("Appointment Details for " + getIntent().getStringExtra("otherName"));
        parentLayout = findViewById(R.id.reportsForUser);
        db = FirebaseDatabase.getInstance().getReference();
        db.keepSynced(true);
        reportArrayList = new ArrayList<>();
        adapter = new AppointmentAdapter();
        reports = findViewById(R.id.reportList);
        loggedInUser = LocalSharedPreferencesData.getPreferredUserData(this, "userName");
        patientName = getIntent().getStringExtra("otherName");
        doctorName = UserDetails.username;
        addReport = findViewById(R.id.addReport);
        noReports = findViewById(R.id.noReports);
        noReports.setVisibility(View.GONE);
        reports.setVisibility(View.GONE);
        type = LocalSharedPreferencesData.getPreferredUserData(this, "type");

        reports.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (type.equals("Doctor")) {
                    builder =
                            new AlertDialog.Builder(AppointmentDetails.this).
                                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Firebase.setAndroidContext(AppointmentDetails.this);
                                            Firebase reference = new Firebase(Values.URL + "/appointments/" + doctorName + "_" + patientName + "/" + reportArrayList.get(position).getDateAndTime());
                                            reference.removeValue();
                                            reference = new Firebase(Values.URL + "/users/" + doctorName + "/appointments/" + reportArrayList.get(position).getDateAndTime());
                                            reference.removeValue();
                                            reference = new Firebase(Values.URL + "/users/" + patientName + "/appointments/" + reportArrayList.get(position).getDateAndTime());
                                            reference.removeValue();
                                            recreate();
                                            Toast.makeText(AppointmentDetails.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setTitle("Do you want to cancel this appointment?");

                } else {
                    builder =
                            new AlertDialog.Builder(AppointmentDetails.this).
                                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Firebase.setAndroidContext(AppointmentDetails.this);
                                            Firebase reference = new Firebase(Values.URL + "/appointments/" + patientName + "_" + doctorName + "/" + reportArrayList.get(position).getDateAndTime());
                                            reference.removeValue();
                                            reference = new Firebase(Values.URL + "/users/" + doctorName + "/appointments/" + reportArrayList.get(position).getDateAndTime());
                                            reference.removeValue();
                                            reference = new Firebase(Values.URL + "/users/" + patientName + "/appointments/" + reportArrayList.get(position).getDateAndTime());
                                            reference.removeValue();
                                            recreate();
                                            Toast.makeText(AppointmentDetails.this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                                        }
                                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setTitle("Do you want to cancel this appointment?");
                }
                builder.create().show();
            }
        });
        // check Internet
/*        if (detector.isInternetAvailable())
        {

        }
        else
        {
            this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }*/
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.appointment_details, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_refresh_app:
////                Intent intentProfile = new Intent(AppointmentDetailsNew.this, AppointmentDetailsNew.class);
////                startActivity(intentProfile);
////                finish();
//                recreate();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    public void getData() {
        reportArrayList = new ArrayList<>();
        String childName;
        if (type.equals("Doctor")) {
            childName = loggedInUser + "_" + patientName;
        } else {
            childName = patientName + "_" + loggedInUser;
        }
        db.child("appointments").child(childName).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.getValue() != null) {
                    Iterator it = snapshot.getChildren().iterator();
                    Object val;
                    while (it.hasNext()) {
                        val = it.next();
                        if (type.equals("Doctor")) {
                            String patientName = ((com.google.firebase.database.DataSnapshot) val).child("patientID").getValue().toString().trim();
                            final String illness = ((com.google.firebase.database.DataSnapshot) val).child("illnessDescription").getValue().toString().trim();
                            final String date = ((com.google.firebase.database.DataSnapshot) val).child("appointmentTimeInMillis").getValue().toString().trim();
                            db.child("users").child(patientName).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    addToList(illness, date);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            //reportArrayList.add(new Appointment(((com.google.firebase.database.DataSnapshot) val).child("patientName").getValue().toString().trim(), ((com.google.firebase.database.DataSnapshot) val).child("doctorName").getValue().toString().trim(), ((com.google.firebase.database.DataSnapshot) val).child("reportType").getValue().toString().trim(), ((com.google.firebase.database.DataSnapshot) val).child("imageId").getValue().toString().trim()));
                        } else {
                            String doctorName = ((com.google.firebase.database.DataSnapshot) val).child("doctorID").getValue().toString().trim();
                            final String illness = ((com.google.firebase.database.DataSnapshot) val).child("illnessDescription").getValue().toString().trim();
                            final String date = ((com.google.firebase.database.DataSnapshot) val).child("appointmentTimeInMillis").getValue().toString().trim();
                            db.child("users").child(doctorName).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    addToList(snapshot.getValue().toString(), LocalSharedPreferencesData.getPreferredUserData(getApplicationContext(), "fullName"), reportType, imageID);
                                    addToList(illness, date);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //reportArrayList.add(new Appointment(((com.google.firebase.database.DataSnapshot) val).child("doctorName").getValue().toString().trim(),((com.google.firebase.database.DataSnapshot) val).child("patientName").getValue().toString().trim(),((com.google.firebase.database.DataSnapshot) val).child("reportType").getValue().toString().trim(),((com.google.firebase.database.DataSnapshot) val).child("imageId").getValue().toString().trim()));
                        }
                    }
                } else {
                    System.out.println("----------------------ENTER ELSE------------------");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addToList(String illness, String date) {
        reportArrayList.add(new Appointment(illness, date));
        if (reportArrayList.size() < 1) {
            noReports.setVisibility(View.VISIBLE);
            reports.setVisibility(View.GONE);
        } else {
            noReports.setVisibility(View.GONE);
            reports.setAdapter(new AppointmentAdapter());
            reports.setVisibility(View.VISIBLE);
        }
        pd.dismiss();
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onResume() {
        super.onResume();
        pd = new ProgressDialog(AppointmentDetails.this);
        pd.setMessage("Loading...");
        pd.show();
        getData();
        if (reportArrayList.size() < 1) {
            noReports.setVisibility(View.VISIBLE);
            reports.setVisibility(View.GONE);
        } else {
            noReports.setVisibility(View.GONE);
            reports.setAdapter(new AppointmentAdapter());
            reports.setVisibility(View.VISIBLE);
        }
        pd.dismiss();
    }

    private void refreshAppointmentDetails() {
        Intent intent = new Intent(getApplicationContext(), AppointmentDetails.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        //ReportsForUser.this.unregisterReceiver(mConnReceiver);
    }

/*    private BroadcastReceiver mConnReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
            boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

            if (currentNetworkInfo.isConnected())
            {
                finish();
                startActivity(getIntent());
            }
            else
            {
                Snackbar snackbar = Snackbar.make(parentLayout, "Looks like you do not have network. We cannot load images with no network. Please check your connectivity.", Snackbar.LENGTH_LONG).setAction("Action", null);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
                showDialog();
            }
        }
    };*/

    public void setImage(final ImageView v, String imageId) {
        FirebaseApp.initializeApp(AppointmentDetails.this);
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
                    Toast.makeText(AppointmentDetails.this, "Could not load image. Check connectivity and try again", Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("We need network connectivity to download reports.")
                .setCancelable(false)
                .setPositiveButton("Connect to Network", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class AppointmentAdapter extends ArrayAdapter<Appointment> {
        public AppointmentAdapter() {
            super(AppointmentDetails.this, R.layout.appointment_listview_row, reportArrayList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.appointment_listview_row, parent, false);
            }

            Appointment currURL = reportArrayList.get(position);
            TextView illness = convertView.findViewById(R.id.illness);
            TextView date = convertView.findViewById(R.id.date);

            illness.setText(currURL.getIllnessDescription());
            String dateString = DateFormat.format("MMM dd,yyyy @ hh:mm", new Date(Long.parseLong(currURL.getDateAndTime()))).toString();
            date.setText(dateString);

            return convertView;
        }
    }

/*    public class ConnectionDetector
    {
        private Context _context;

        public ConnectionDetector(Context context)
        {
            this._context = context;
        }

        public boolean isInternetAvailable()
        {
            ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null)
            {
                NetworkInfo netInfo = connectivity.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
                return false;
            }
            return false;
        }
    }*/
}