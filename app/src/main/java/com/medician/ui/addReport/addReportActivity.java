package com.medician.ui.addReport;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.FirebaseDatabase;
import com.medician.ConnectivityStatus;
import com.medician.LocalSharedPreferencesData;
import com.medician.R;
import com.medician.Values;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class addReportActivity extends AppCompatActivity {

    public static int requestCode = 10;
    StorageReference mStorage;
    Button takePic;
    TextView doctorName, patientName;
    EditText reportType;
    DatabaseReference dbReff;
    Firebase reportsFB;
    ImageView uploadedImage;
    ReportData data;
    public Uri imageURL ;
    static final int REQUEST_TAKE_PHOTO = 20;
    static final int REQUEST_CHOOSE_PHOTO = 10;
    String currentPhotoPath;
    ProgressDialog pd;
    FloatingActionButton deletePic, uploadBtn;
    Spinner reportTypeSpinner;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);
        FirebaseApp.initializeApp(this);
        doctorName = findViewById(R.id.doctorName);
        patientName = findViewById(R.id.patientName);
        //reportType = findViewById(R.id.reportType);
        reportTypeSpinner = findViewById(R.id.reportType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.reportTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(adapter);

        //doctorName.setText(LocalSharedPreferencesData.getPreferredUserData(this, "userName"));
        String patientID = getIntent().getStringExtra("patientName");

        db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(patientID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientName.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        db.child("users").child(LocalSharedPreferencesData.getPreferredUserData(this, "userName")).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorName.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        data = new ReportData();
        mStorage = FirebaseStorage.getInstance().getReference("Images");

        dbReff = FirebaseDatabase.getInstance().getReference().child("reports").child(LocalSharedPreferencesData.getPreferredUserData(this, "userName").trim()+"_"+patientID.trim());
        uploadedImage = findViewById(R.id.uploadedImage);
        uploadBtn = findViewById(R.id.upload);
        takePic = findViewById(R.id.takePic);
        deletePic = findViewById(R.id.deleteBtn);

        //When a user wants to choose an image from the gallery.
        uploadedImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                chooseFile();
            }
        });


        takePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED)
                {
                    ActivityCompat.requestPermissions(addReportActivity.this, new String[] {Manifest.permission.CAMERA}, requestCode);
                }
                else {
                    openCamera();
                }
            }
        });

        deletePic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(imageURL == null){
                    Toast.makeText(addReportActivity.this, "No image to delete", Toast.LENGTH_LONG).show();
                }
                else {
                    imageURL = null;
                    uploadedImage.setImageResource(R.drawable.ic_menu_camera);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
                //Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void chooseFile(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CHOOSE_PHOTO);
    }

    public void uploadFile(){
        String imageID = System.currentTimeMillis()+"." +getExtension(imageURL);
        data.setDoctorName(LocalSharedPreferencesData.getPreferredUserData(this, "userName"));
        data.setPatientName(getIntent().getStringExtra("patientName").trim());
        data.setReportType(reportTypeSpinner.getSelectedItem().toString());
        data.setImageId(imageID);
        //reportsFB.push().setValue(data);
        dbReff.push().setValue(data);
        StorageReference ref = mStorage.child(imageID);
        ref.putFile(imageURL)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Toast.makeText(addReportActivity.this,"Report uploaded successfully", Toast.LENGTH_LONG).show();
                        imageURL = null;
                        uploadedImage.setImageResource(R.drawable.ic_menu_camera);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(addReportActivity.this,"Something went wrong. Please try again!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openCamera(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(addReportActivity.this.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                imageURL = FileProvider.getUriForFile(addReportActivity.this,
                        "com.medician.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURL);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = addReportActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public String getExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    public void onResume() {
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
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!ConnectivityStatus.isConnected(getApplicationContext())){
                // no connection
        /*When the image is chosen or captured and ready to be uploaded, this function is called.
        If there is no image captured, it throws an error.
        Else it calls the uploadFile function.
        */
                uploadBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        Snackbar snackbar = Snackbar.make(doctorName, "Looks like you do not have network. Either enable Wi-Fi or mobile data!", Snackbar.LENGTH_LONG).setAction("Action", null);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        snackbar.setActionTextColor(Color.WHITE);
                        snackbar.show();
                    }
                });

            }else {
                // connected
        /*When the image is chosen or captured and ready to be uploaded, this function is called.
        If there is no image captured, it throws an error.
        Else it calls the uploadFile function.
        */
                uploadBtn.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if(imageURL == null){
                            Toast.makeText(addReportActivity.this, "Choose or capture an image of the report", Toast.LENGTH_LONG).show();
                        }
                        else{
                            pd = new ProgressDialog(addReportActivity.this);
                            pd.setMessage("Uploading...");
                            pd.show();
                            uploadFile();
                        }
                    }
                });
            }
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == REQUEST_CHOOSE_PHOTO && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageURL = data.getData();
            uploadedImage.setImageURI(imageURL);
        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap bitmapImg = BitmapFactory.decodeFile(currentPhotoPath);
            uploadedImage.setImageBitmap(bitmapImg);
        }
    }
}