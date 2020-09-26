package com.medician.ui.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.firebase.client.Firebase;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.medician.ConnectivityStatus;
import com.medician.Dashboard;
import com.medician.LocalSharedPreferencesData;
import com.medician.R;
import com.medician.UserDetails;
import com.medician.Values;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class ProfileFragment extends Fragment {
    private Button editProfilePictureButton;
    private static int AUTOCOMPLETE_REQ_CODE = 100;
    private static final int  SELECT_IMAGE_REQ_CODE=260;
    public Bitmap bmp;
    private String exceptionString;
    private ProgressBar progressBar;
    private Button uploadProfilePictureButton;
    private Button removeProfilePictureButton;
    private ImageView currentProfilePic;
    private StorageReference storageRef;
    private Firebase reference;
    private Uri filePath;
    private EditText updateNameEditText;
    private Button updateButton;

    private EditText updateAddressEditText;
    private double latitude;
    private double longitude;
    private String newAddress = UserDetails.address;
    private String newName = UserDetails.fullName;
    private String newPassword = UserDetails.password;
    private String newSpeciality = UserDetails.speciality;
    private String newProfilePicPath = UserDetails.profilePicPath;

    private EditText updatePasswordEditText;

    private Spinner updateSpecialityDropdown;

    private TextView noNetworkMessageTextView;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!ConnectivityStatus.isConnected(getContext())) {
                editProfilePictureButton.setEnabled(false);
                editProfilePictureButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));
                removeProfilePictureButton.setEnabled(false);
                removeProfilePictureButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));
                updateButton.setEnabled(false);
                updateButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip_gray, null));
                noNetworkMessageTextView.setVisibility(View.VISIBLE);

            }
            else {
                editProfilePictureButton.setEnabled(true);
                editProfilePictureButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));
                removeProfilePictureButton.setEnabled(true);
                removeProfilePictureButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));
                updateButton.setEnabled(true);
                updateButton.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.rounded_corner_tip, null));
                noNetworkMessageTextView.setVisibility(View.GONE);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storageRef = FirebaseStorage.getInstance().getReference();
        reference = new Firebase(Values.URL + "/users");
        Places.initialize(getContext(), "AIzaSyDV4Hv1WpxDWjSBSP2TD59-ZwYqc5eoqak");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // No Network message
        noNetworkMessageTextView = rootView.findViewById(R.id.profileNoNetworkMessageTextView);
        // Name
        updateNameEditText = rootView.findViewById(R.id.updateNameEditText);
        updateNameEditText.setText(UserDetails.fullName);
        updateNameEditText.isSelected();

        // Address
        updateAddressEditText = rootView.findViewById(R.id.updateAddressEditText);
        updateAddressEditText.setText(UserDetails.address);
        editAddress();

        // Profile Picture
        currentProfilePic = rootView.findViewById(R.id.currentProfilePic);
        progressBar = rootView.findViewById(R.id.update_profile_progress_bar);
        editProfilePictureButton = rootView.findViewById(R.id.editProfilePictureButton);
        removeProfilePictureButton = rootView.findViewById(R.id.removeProfilePictureButton);
        uploadProfilePictureButton = rootView.findViewById(R.id.uploadProfilePictureButton);
        updateButton = rootView.findViewById(R.id.updateProfileButton);
        updatePasswordEditText = rootView.findViewById(R.id.updatePasswordEditText);

        // Speciality
        updateSpecialityDropdown = rootView.findViewById(R.id.updateSpecialitySpinner);

        //Set current profile picture
        if(!UserDetails.profilePicPath.isEmpty()) {
            FetchCurrentProfilePictureRunnable fetchProfilePictureRunnable =
                    new FetchCurrentProfilePictureRunnable();
            Thread fetchPicThread = new Thread(fetchProfilePictureRunnable);
            fetchPicThread.start();
            try {
                fetchPicThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!exceptionString.isEmpty()) {
                Toast.makeText(getContext(), exceptionString, Toast.LENGTH_LONG).show();
            } else {
                currentProfilePic.setImageBitmap(bmp);
            }
        }
        editProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openChoosePictureDialog();
            }
        });

        uploadProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPicture();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(updateNameEditText.getText().toString().equals(""))
                    updateNameEditText.setError("Can not be blank!");
                newName = updateNameEditText.getText().toString();
                newAddress = updateAddressEditText.getText().toString();
                newPassword = updatePasswordEditText.getText().toString();
                if(UserDetails.type.equals("Doctor")) {
                    newSpeciality = updateSpecialityDropdown.getSelectedItem().toString();
                }
                progressBar.setVisibility(View.VISIBLE);
                UpdateProfileInfoRunnable updateProfileInfoRunnable =
                        new UpdateProfileInfoRunnable();
                Thread updateProfileThread = new Thread(updateProfileInfoRunnable);
                updateProfileThread.start();
                try {
                    updateProfileThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UserDetails.profileUpdated = true;
                refreshDashboard();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Updated the profile",Toast.LENGTH_LONG).show();

            }
        });

        // Remove Profile Picture
        removeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RemoveProfilePicturePathRunnable removeProfilePicturePathRunnable =
                        new RemoveProfilePicturePathRunnable();
                Thread removeProfilePicturePathThread =
                        new Thread(removeProfilePicturePathRunnable);
                removeProfilePicturePathThread.start();
                try {
                    removeProfilePicturePathThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                newProfilePicPath = "";
                UserDetails.profileUpdated = true;
                progressBar.setVisibility(View.GONE);
                currentProfilePic.setVisibility(View.VISIBLE);
                currentProfilePic.setImageResource(R.drawable.ic_user_default_foreground);
                //ImageView iv = getActivity().findViewById(R.id.loggedInUserImageView);
                //iv.setImageResource(R.drawable.ic_user_default_foreground);
                refreshPicture();
            }
        });

        // Populate the Specialities
        if(UserDetails.type.equals("Doctor")) {
            List<String> specialities = Values.specialities;
            int currentIndex = specialities.indexOf(UserDetails.speciality);
            System.out.println("currI :: " + currentIndex);
            System.out.println("specialities :: " + specialities.toString());
            System.out.println("current_speciality :: " + UserDetails.speciality);
            ArrayAdapter<String> specialityListAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item, specialities);
            updateSpecialityDropdown.setAdapter(specialityListAdapter);
            updateSpecialityDropdown.setSelection(currentIndex);
            updateSpecialityDropdown.setVisibility(View.VISIBLE);
        }
        return rootView;
    }

    private void editAddress() {
        updateAddressEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME,
                        Place.Field.LAT_LNG);
                System.out.println(fieldList);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                        fieldList).build(getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQ_CODE);
            }
        });
    }

    private void uploadPicture() {
        progressBar.setVisibility(View.VISIBLE);
        uploadProfilePictureButton.setVisibility(View.GONE);
        currentProfilePic.setVisibility(View.GONE);
        final StorageReference profilePicturesRef = storageRef.child("profile_pictures/"
                + UserDetails.username + ".jpg");
        UploadTask uploadTask = profilePicturesRef.putFile(filePath);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {

            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return profilePicturesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri == null) {
                        return;
                    }
                    else {
                        newProfilePicPath = downloadUri.toString();
                        UserDetails.profilePicPath = downloadUri.toString();

                        Toast.makeText(getContext(),
                                "Profile picture updated",
                                Toast.LENGTH_LONG).show();
                        System.out.println(downloadUri);
                        UpdateProfilePicturePathRunnable updateProfilePicturePathRunnable =
                                new UpdateProfilePicturePathRunnable(UserDetails.profilePicPath);
                        Thread updateProfilePicturePathThread =
                                new Thread(updateProfilePicturePathRunnable);
                        updateProfilePicturePathThread.start();
                        try {
                            updateProfilePicturePathThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        refreshPicture();
                        UserDetails.profileUpdated = true;
                        progressBar.setVisibility(View.GONE);
                        currentProfilePic.setVisibility(View.VISIBLE);
                        editProfilePictureButton.setVisibility(View.VISIBLE);
                        removeProfilePictureButton.setVisibility(View.VISIBLE);
                        refreshDashboard();
                    }
                }
            }
        });
    }

    private void openChoosePictureDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select an Image"),
                SELECT_IMAGE_REQ_CODE);
    }

    public void refreshPicture() {
        ImageView iv = getActivity().findViewById(R.id.loggedInUserImageView);
        currentProfilePic.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        UserDetails.profilePicPath = newProfilePicPath;
        currentProfilePic.setVisibility(View.GONE);
        LocalSharedPreferencesData.savePreferredUserData(
                getActivity().getApplicationContext(), UserDetails.username,
                UserDetails.fullName, UserDetails.type,
                UserDetails.address, UserDetails.profilePicPath, UserDetails.speciality);
        UserDetails.profileUpdated = false;
        if(!UserDetails.profilePicPath.isEmpty()) {
            FetchCurrentProfilePictureRunnable fetchProfilePictureRunnable =
                    new FetchCurrentProfilePictureRunnable();
            Thread fetchPicThread = new Thread(fetchProfilePictureRunnable);
            fetchPicThread.start();
            try {
                fetchPicThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!exceptionString.isEmpty()) {
                Toast.makeText(getContext(), exceptionString, Toast.LENGTH_LONG).show();
            } else {
                currentProfilePic.setImageBitmap(bmp);
                Dashboard d = (Dashboard) getActivity();
                iv.setImageBitmap(bmp);
            }
        }
        else {
            iv.getLayoutParams().height = (int) getResources()
                    .getDimension(R.dimen.profile_pic_size);
            iv.getLayoutParams().width = (int) getResources()
                    .getDimension(R.dimen.profile_pic_default_size);
            iv.setImageResource(R.mipmap.ic_user_default_round);
        }
        currentProfilePic.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
    public void refreshDashboard() {
        if (UserDetails.profileUpdated) {
            UserDetails.fullName = newName;
            UserDetails.address = newAddress;
            UserDetails.password = newPassword;
            UserDetails.speciality = newSpeciality;

            Toast.makeText(getContext(),"Updating the Profile", Toast.LENGTH_LONG);
            currentProfilePic.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            LocalSharedPreferencesData.savePreferredUserData(
                    getActivity().getApplicationContext(), UserDetails.username,
                    UserDetails.fullName, UserDetails.type,
                    UserDetails.address, UserDetails.profilePicPath, UserDetails.speciality);
            UserDetails.profileUpdated = false;
            TextView tv = getActivity().findViewById(R.id.loggedInUserNameTextView);

            tv.setText(newName);


            progressBar.setVisibility(View.GONE);
            currentProfilePic.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(),"Profile Info Updated", Toast.LENGTH_LONG).show();
        }
    }

    class FetchCurrentProfilePictureRunnable implements Runnable {
        @Override
        public void run() {
            exceptionString ="";
            URL url;
            try {
                url = new URL(UserDetails.profilePicPath);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                bmp = getCroppedBitmap(bmp);
            } catch (MalformedURLException e) {
                exceptionString = "Bad URL : Unable to load Profile Picture.";
                e.printStackTrace();
            } catch (IOException e) {
                exceptionString = "IO Failed : Unable to load Profile Picture.";
                e.printStackTrace();
            }
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    class UpdateProfileInfoRunnable implements Runnable {
        @Override
        public void run() {
            if(!newName.isEmpty() && !newName.equals(UserDetails.fullName)) {
                reference.child(UserDetails.username).child("name")
                        .setValue(newName);
                UserDetails.profileUpdated = true;
            }
            if(!newAddress.isEmpty() && !newAddress.equals(UserDetails.address)) {
                reference.child(UserDetails.username).child("address").child("addressText")
                        .setValue(newAddress);
                reference.child(UserDetails.username).child("address").child("latitude")
                        .setValue(latitude);
                reference.child(UserDetails.username).child("address").child("longitude")
                        .setValue(longitude);
                UserDetails.profileUpdated = true;
            }
            if(!newPassword.isEmpty() && !newAddress.equals(UserDetails.password)) {
                reference.child(UserDetails.username).child("password")
                        .setValue(newPassword);
            }
            if(!newSpeciality.isEmpty() && !newSpeciality.equals(UserDetails.speciality)) {
                reference.child(UserDetails.username).child("speciality")
                        .setValue(newSpeciality);
            }
        }
    }

    class RemoveProfilePicturePathRunnable implements Runnable {
        @Override
        public void run() {
            reference.child(UserDetails.username).child("profilePicturePath").removeValue();
        }
    }

    class UpdateProfilePicturePathRunnable implements Runnable {
        private String newPath;
        UpdateProfilePicturePathRunnable(String newPath) {
            this.newPath = newPath;
        }
        @Override
        public void run() {
            reference.child(UserDetails.username).child("profilePicturePath")
                    .setValue(newPath);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQ_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                newAddress = place.getAddress();
                LatLng latLng = place.getLatLng();
                System.out.println(newAddress);
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                updateAddressEditText.setText(newAddress);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                updateAddressEditText.setError("Invalid Address-Try again");
            } else if (resultCode == Activity.RESULT_CANCELED) {
                updateAddressEditText.setError("Address not selected");
            }
            return;
        }
        else if (requestCode == SELECT_IMAGE_REQ_CODE && resultCode == Activity.RESULT_OK
                && data !=null && data.getData()!=null) {
            try {
                filePath = data.getData();
                InputStream imageInputStream = getActivity().getContentResolver().openInputStream(filePath);
                Drawable drawableNewPic = Drawable.createFromStream(imageInputStream,
                        filePath.toString());
                bmp = ((BitmapDrawable) drawableNewPic).getBitmap();
                bmp = getCroppedBitmap(bmp);
                currentProfilePic.setImageBitmap(bmp);
                editProfilePictureButton.setVisibility(View.GONE);
                removeProfilePictureButton.setVisibility(View.GONE);
                uploadProfilePictureButton.setGravity(Gravity.CENTER);
                uploadProfilePictureButton.setVisibility(View.VISIBLE);



            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), "Image is corrupt or cannot be accessed! " +
                                "Try with some different image",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }
}