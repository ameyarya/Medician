package com.medician.ui.findDoctor;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.medician.ConnectivityStatus;
import com.medician.ExpandCollapseAnimation;
import com.medician.R;
import com.medician.Values;
import com.medician.UserDetails;
import com.medician.ui.appointments.Appointment;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FindDoctorFragment extends Fragment {
    private MapView mMapView;
    // TODO: Rename and change types of parameters
    private Spinner specialityDropdown;
    private Spinner distanceDropdown;
    private GoogleMap googleMap;
    private Doctor selectedDoctor = new Doctor();
    private ImageView selectedDoctorImageView;
    private TextView doctorDetailsTextView;
    private LinearLayout doctorDetailsSection;
    private ProgressBar progressBar;
    private LatLng currentUserPosition = new LatLng(0, 0);
    private JSONObject usersJSONObj;
    private String specialitySelected = "General Surgery";
    private Location currentUserLocation;
    private LinearLayout doctorDetailsLayout;
    private Button createAppointmentButton;
    private Map<String, ImageView> starsMap;
    private Bitmap bmp;
    private String exceptionString;
    private ProgressBar doctorPicLoadingProgressBar;
    private ImageButton hideDoctorDetailsSectionButton;
    private TextView noNetworkMessageTextView;
    private Handler handler = new Handler();
    private View rootView;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!ConnectivityStatus.isConnected(getContext())) {
                noNetworkMessageTextView.setVisibility(View.VISIBLE);
                doctorDetailsLayout.setVisibility(View.GONE);
                mMapView.setVisibility(View.GONE);
            }
            else {
                if(mMapView.getVisibility() == View.GONE) {
                    mMapView.setVisibility(View.VISIBLE);
                    noNetworkMessageTextView.setVisibility(View.VISIBLE);
                    doctorDetailsLayout.setVisibility(View.VISIBLE);
                    updateMap();
                    if(usersJSONObj ==  null) {
                        Toast.makeText(getContext(), "Online again! You may need to " +
                                "go back to Dashboard and navigate here again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FindDoctorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FindDoctorFragment newInstance(String param1, String param2) {
        FindDoctorFragment fragment = new FindDoctorFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exceptionString = "";
        String url = Values.URL + "/users.json";
        final StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    usersJSONObj = new JSONObject(s);
                } catch (JSONException e) {
                    Toast.makeText(getContext(), "User data fetched is corrupted! " +
                                    "Please log out and login and try again",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("VolleyError" + volleyError);
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_find_doctor, container, false);

        // Inflate the layout for this fragment
        specialityDropdown = rootView.findViewById(R.id.specialityDropdown);
        distanceDropdown = rootView.findViewById(R.id.distanceDropdown);
        mMapView = rootView.findViewById(R.id.mapView);
        noNetworkMessageTextView = rootView.findViewById(R.id.noNetworkMessageTextView);
        doctorDetailsTextView = rootView.findViewById(R.id.doctorDetailsTextView);
        doctorDetailsTextView.setText(getString(R.string.doctorDetailsTextFormat,
                selectedDoctor.getName(), selectedDoctor.getSpeciality(),
                selectedDoctor.getAddress(), selectedDoctor.getDistance()));

        specialityDropdown = rootView.findViewById(R.id.specialityDropdown);
        distanceDropdown = rootView.findViewById(R.id.distanceDropdown);
        doctorDetailsLayout = rootView.findViewById(R.id.doctorDetailsLayout);
        doctorDetailsSection = rootView.findViewById(R.id.doctorDetailsSection);
        selectedDoctorImageView = rootView.findViewById(R.id.doctorProfilePic);
        doctorPicLoadingProgressBar = rootView.findViewById(R.id.doctorPicLoadingProgressBar);
        hideDoctorDetailsSectionButton = rootView.findViewById(R.id.hideDoctorDetailsSectionButton);
        hideDoctorDetailsSectionButton = rootView.findViewById(R.id.hideDoctorDetailsSectionButton);
        hideDoctorDetailsSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExpandCollapseAnimation a = new ExpandCollapseAnimation(doctorDetailsSection, 1000, ExpandCollapseAnimation.COLLAPSE);
                a.setHeight(doctorDetailsSection.getHeight());
                doctorDetailsSection.startAnimation(a);

            }
        });
        starsMap = new HashMap<>();
        starsMap.put("star1", (ImageView) rootView.findViewById(R.id.star1));
        starsMap.put("star2", (ImageView) rootView.findViewById(R.id.star2));
        starsMap.put("star3", (ImageView) rootView.findViewById(R.id.star3));
        starsMap.put("star4", (ImageView) rootView.findViewById(R.id.star4));
        starsMap.put("star5", (ImageView) rootView.findViewById(R.id.star5));

        createAppointmentButton = rootView.findViewById(R.id.createAppointmentButton);
        createAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAppointment();
            }
        });
        List<String> specialities = Values.specialities;
        ArrayAdapter<String> specialityListAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, specialities);
        specialityDropdown.setAdapter(specialityListAdapter);
        specialityDropdown.setSelection(0);

        List<String> distances = new ArrayList(Arrays.asList("Within 5 miles", "Within 10 miles",
                "Within 15 miles"));
        ArrayAdapter<String> distanceListAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item, distances);
        distanceDropdown.setAdapter(distanceListAdapter);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateMap();

/*        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                // googleMap.setMyLocationEnabled(true);
                // For dropping a marker at a point on the Map
                progressBar = rootView.findViewById(R.id.progress_bar_find_doctor);
                progressBar.setVisibility(View.VISIBLE);
                String url = Values.URL + "/users.json";
                final StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            usersJSONObj = new JSONObject(s);
                            // Store the camera position near to user's location
                            // Mark the doctors of selected speciality
                            Iterator i = usersJSONObj.keys();
                            LatLng doctorPosition;
                            while(i.hasNext()) {
                                String key = (String) i.next();
                                if(usersJSONObj.get(key) instanceof JSONObject) {
                                    JSONObject userObj = new JSONObject(usersJSONObj
                                            .get(key).toString());
                                    System.out.println("UserObject : " + userObj.toString());
                                    if (userObj.getString("type").equals("Doctor")
                                            && userObj.getString("speciality").equals(specialitySelected)) {
                                        doctorPosition = new LatLng(userObj.getJSONObject("address")
                                                .getDouble("latitude"), userObj.getJSONObject("address")
                                                .getDouble("longitude"));
                                        googleMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                                .position(doctorPosition)
                                                .title("Dr" + userObj.get("name"))
                                                .snippet(userObj
                                                        .getJSONObject("address")
                                                        .getString("addressText")));
                                    }
                                }
                            }
                            JSONObject address = usersJSONObj.getJSONObject(UserDetails.username)
                                    .getJSONObject("address");
                            System.out.println("167"+address);
                            currentUserPosition =  new LatLng(address.getDouble("latitude"),
                                    address.getDouble("longitude"));

                            // For zooming automatically to the location of the marker
                            int distance = Integer.parseInt(distanceDropdown
                                    .getSelectedItem().toString()
                                    .split(" ")[1]);
                            double iMeter = distance * 1609.34;
                            Circle circle  = googleMap.addCircle(new CircleOptions()
                                    .center(currentUserPosition)
                                    .radius(iMeter) // Converting Miles into Meters...
                                    .strokeColor(Color.RED)
                                    .strokeWidth(5));
                            circle.isVisible();
                            float currentZoomLevel = getZoomLevel(circle);
                            float animateZoom = currentZoomLevel + 5;
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, animateZoom));
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), 2000, null);
                            System.out.println("186" + currentUserPosition.latitude);
                            Marker currentUserMarker = googleMap.addMarker(new MarkerOptions()
                                    .position(currentUserPosition)
                                    .title("You are here")
                                    .snippet("This is your current location"));
                            currentUserMarker.setTag(null);
                            currentUserLocation = new Location(UserDetails.username);
                            currentUserLocation.setLatitude(address.getDouble("latitude"));
                            currentUserLocation.setLongitude(address.getDouble("longitude"));
                            progressBar.setVisibility(View.GONE);
                        }
                        catch (JSONException e) {
                            Toast.makeText(getContext(), "User data fetched is corrupted! " +
                                    "Please log out and login and try again : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        System.out.println("VolleyError" + volleyError);
                    }
                });
                RequestQueue rQueue = Volley.newRequestQueue(getContext());
                rQueue.add(request);

            }
        });*/
        distanceDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                zoomToSelectedDistance();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        specialityDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    if(usersJSONObj != null) {
                        if (specialityDropdown != null) {
                            markDoctors();
                        }
                    }

                } catch (JSONException e) {
                    Toast.makeText(getContext(), "User data fetched is corrupted! " +
                                    "Please log out and login and try again",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        return rootView;
    }

    private void createNewAppointment() {
        final Appointment newAppointment = new Appointment();
        newAppointment.setPatientID(UserDetails.username);
        newAppointment.setPatientFullName(UserDetails.fullName);
        newAppointment.setDoctorID(selectedDoctor.getUserID());
        newAppointment.setDoctorFullName(selectedDoctor.getName());
        final Dialog appointmentDialog = new Dialog(getContext());
        appointmentDialog.setContentView(R.layout.floating_popup);
        final EditText issueDescription = appointmentDialog.findViewById(R.id.message);
        Button select = appointmentDialog.findViewById(R.id.selectDate);
        final TextView selectedDateTextView = appointmentDialog.findViewById(R.id.date);
        final ProgressBar pb = appointmentDialog.findViewById(R.id.progress_bar_dialog_box);
        final Calendar newCalender = Calendar.getInstance();
        final Button add = appointmentDialog.findViewById(R.id.addButton);
        final TextView heading = appointmentDialog.findViewById(R.id.popUpHeading);
        heading.setText(("Booking appointment with\n Dr. " + selectedDoctor.getName()).toUpperCase());
        heading.setVisibility(View.VISIBLE);

        issueDescription.setHint("Briefly explain your problem");
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();

                        CustomTimePickerDialog time = new CustomTimePickerDialog(getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                boolean isSlotBooked = false;
                                if(hourOfDay < 10 || hourOfDay > 17) {

                                    Toast.makeText(getContext(), "Appointment hours can be between 10AM - 5PM",
                                            Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    try {
                                        newDate.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                        newDate.set(Calendar.MILLISECOND, 0);
                                        newDate.set(Calendar.SECOND, 0);


                                        if (newDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() <= 0) {
                                            add.setEnabled(false);
                                            Toast.makeText(getContext(), "You have selected a past date or time",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            if (usersJSONObj.getJSONObject(newAppointment.getDoctorID())
                                                    .has("appointments")) {
                                               JSONObject doctorAppointmentsTimes = usersJSONObj.getJSONObject(newAppointment.getDoctorID())
                                                       .getJSONObject("appointments");
                                               Iterator i = doctorAppointmentsTimes.keys();
                                               while (i.hasNext()) {
                                                   String time =(String) i.next();

                                                   long time1 = Long.parseLong(time);
                                                   Calendar ithDateTime = Calendar.getInstance();
                                                   ithDateTime.setTimeInMillis(time1);
                                                   ithDateTime.set(Calendar.SECOND, 0);
                                                   ithDateTime.set(Calendar.MILLISECOND, 0);
                                                   if(ithDateTime.equals(newDate)) {
                                                       add.setEnabled(false);
                                                       Toast.makeText(getContext(), "The doctor is already booked" +
                                                                       " during this time and date!\n" +
                                                                       "Please selected some other Date/Time",
                                                               Toast.LENGTH_SHORT).show();
                                                       isSlotBooked = true;
                                                       break;
                                                   }

                                                }
                                            }
                                            if (!isSlotBooked) {
                                                add.setEnabled(true);
                                                Log.w("TIME", System.currentTimeMillis() + "");
                                                selectedDateTextView.setText(newDate.getTime().toString());
                                                newAppointment.setAppointmentTimeInMillis(newDate.getTimeInMillis());
                                                Toast.makeText(getContext(), " Time Selected", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } catch (JSONException e) {
                                        Toast.makeText(getContext(), "Doctor appointment data cannot be fetched!" +
                                                        "Please try again in some time",
                                                Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                }

                            }
                        },newTime.get(Calendar.HOUR_OF_DAY),newTime.get(Calendar.MINUTE),true);
                        time.setTitle("Select a 30 minutes appointment slot between 10 AM - 5PM");
                        time.show();

                    }
                },newCalender.get(Calendar.YEAR),newCalender.get(Calendar.MONTH),newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();

            }
        });

        add.setEnabled(false);
        add.setText("Schedule Appointment");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!issueDescription.getText().toString().trim().equals("")) {
                    newAppointment.setIllnessDescription(issueDescription.getText().toString());
                    add.setVisibility(View.GONE);
                    pb.setVisibility(View.VISIBLE);
                    final String appointment_id = newAppointment.getDoctorID() +
                            "_" + newAppointment.getPatientID();
                    String url = Values.URL + "/appointments.json";
                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject allAppointments = new JSONObject(response);

                                if (allAppointments.has(appointment_id) &&
                                        allAppointments.getJSONObject(appointment_id)
                                                .has(Long.toString(newAppointment.getAppointmentTimeInMillis()))) {
                                    Toast.makeText(getContext(), "This Date and time is already booked." +
                                                    "\nSelect some other date/time",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Firebase reference = new Firebase(Values.URL + "/appointments");
                                    reference.child(appointment_id)
                                            .child(Long.toString(newAppointment.getAppointmentTimeInMillis()))
                                            .setValue(newAppointment);

                                    reference = new Firebase(Values.URL + "/users");
                                    reference.child(newAppointment.getPatientID())
                                            .child("appointments")
                                            .child(Long.toString(newAppointment.getAppointmentTimeInMillis()))
                                            .setValue("Booked");
                                    reference.child(newAppointment.getDoctorID())
                                            .child("appointments")
                                            .child(Long.toString(newAppointment.getAppointmentTimeInMillis()))
                                            .setValue("Booked");
                                    Toast.makeText(getContext(), "Appointment booked with Dr." + selectedDoctor.getName()
                                                    + " on " + selectedDateTextView.getText(),
                                            Toast.LENGTH_LONG).show();
                                }
                                appointmentDialog.dismiss();
                            } catch (JSONException e) {
                                Toast.makeText(getContext(), "The data received is corrupted! " +
                                                "Please retry after sometime",
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("volleyError" + volleyError);
                            pb.setVisibility(View.GONE);
                            add.setVisibility(View.VISIBLE);
                        }
                    });
                    RequestQueue rQueue = Volley.newRequestQueue(getContext());
                    rQueue.add(request);

                }
                else {
                    Toast.makeText(getContext(), "Please enter your health issue briefly!", Toast.LENGTH_LONG).show();
                }
            }});

        appointmentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        appointmentDialog.show();
    }

    private void markDoctors() throws JSONException {
        googleMap.clear();
        //System.out.println("mark doctors called!!! userJSONOBJ = " + usersJSONObj);
        specialitySelected = specialityDropdown.getSelectedItem().toString();
        // Store the camera position near to user's location
        // Mark the doctors of selected speciality
        Iterator i = usersJSONObj.keys();
        LatLng doctorPosition;
        while(i.hasNext()) {
            String key = (String) i.next();
            if(usersJSONObj.get(key) instanceof JSONObject) {
                JSONObject userObj = new JSONObject(usersJSONObj
                        .get(key).toString());
                if (userObj.getString("type").equals("Doctor")
                        && userObj.getString("speciality").equals(specialitySelected)) {
                    doctorPosition = new LatLng(userObj.getJSONObject("address")
                            .getDouble("latitude"), userObj.getJSONObject("address")
                            .getDouble("longitude"));

                    Marker doctorMarker = googleMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .position(doctorPosition)
                            .title("Dr. " + userObj.get("name"))
                            .snippet(userObj
                                    .getJSONObject("address")
                                    .getString("addressText")));
                    userObj.put("userID", key);
                    doctorMarker.setTag(userObj);
                }
            }
        }

        JSONObject address = usersJSONObj.getJSONObject(UserDetails.username)
                .getJSONObject("address");
        currentUserPosition =  new LatLng(address.getDouble("latitude"),
                address.getDouble("longitude"));
        // Zoom to selected distance
        zoomToSelectedDistance();
        // For zooming automatically to the location of the marker
        CameraPosition cameraPosition = new CameraPosition.Builder().target(currentUserPosition).build();
        int distance = Integer.parseInt(distanceDropdown
                .getSelectedItem().toString()
                .split(" ")[1]);
        double iMeter = distance * 1609.34;
        Circle circle  = googleMap.addCircle(new CircleOptions()
                .center(currentUserPosition)
                .radius(iMeter) // Converting Miles into Meters...
                .strokeColor(Color.RED)
                .strokeWidth(5));
        circle.isVisible();
        float currentZoomLevel = getZoomLevel(circle);
        float animateZoom = currentZoomLevel + 5;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, animateZoom));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), 2000, null);
        googleMap.addMarker(new MarkerOptions()
                .position(currentUserPosition)
                .title("You are here")
                .snippet("This is your current location"));

    }

    private void updateMap() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                // googleMap.setMyLocationEnabled(true);
                // For dropping a marker at a point on the Map
                progressBar = rootView.findViewById(R.id.progress_bar_find_doctor);
                progressBar.setVisibility(View.VISIBLE);
                String url = Values.URL + "/users.json";
                final StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        try {
                            usersJSONObj = new JSONObject(s);
                            // Store the camera position near to user's location
                            // Mark the doctors of selected speciality
                            Iterator i = usersJSONObj.keys();
                            LatLng doctorPosition;
                            while(i.hasNext()) {
                                String key = (String) i.next();
                                if(usersJSONObj.get(key) instanceof JSONObject) {
                                    JSONObject userObj = new JSONObject(usersJSONObj
                                            .get(key).toString());
                                    System.out.println("UserObject : " + userObj.toString());
                                    if (userObj.getString("type").equals("Doctor")
                                            && userObj.getString("speciality").equals(specialitySelected)) {
                                        doctorPosition = new LatLng(userObj.getJSONObject("address")
                                                .getDouble("latitude"), userObj.getJSONObject("address")
                                                .getDouble("longitude"));
                                        googleMap.addMarker(new MarkerOptions()
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                                .position(doctorPosition)
                                                .title("Dr" + userObj.get("name"))
                                                .snippet(userObj
                                                        .getJSONObject("address")
                                                        .getString("addressText")));
                                    }
                                }
                            }
                            JSONObject address = usersJSONObj.getJSONObject(UserDetails.username)
                                    .getJSONObject("address");
                            System.out.println("167"+address);
                            currentUserPosition =  new LatLng(address.getDouble("latitude"),
                                    address.getDouble("longitude"));

                            // For zooming automatically to the location of the marker
                            int distance = Integer.parseInt(distanceDropdown
                                    .getSelectedItem().toString()
                                    .split(" ")[1]);
                            double iMeter = distance * 1609.34;
                            Circle circle  = googleMap.addCircle(new CircleOptions()
                                    .center(currentUserPosition)
                                    .radius(iMeter) // Converting Miles into Meters...
                                    .strokeColor(Color.RED)
                                    .strokeWidth(5));
                            circle.isVisible();
                            float currentZoomLevel = getZoomLevel(circle);
                            float animateZoom = currentZoomLevel + 5;
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, animateZoom));
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), 2000, null);
                            System.out.println("186" + currentUserPosition.latitude);
                            Marker currentUserMarker = googleMap.addMarker(new MarkerOptions()
                                    .position(currentUserPosition)
                                    .title("You are here")
                                    .snippet("This is your current location"));
                            currentUserMarker.setTag(null);
                            currentUserLocation = new Location(UserDetails.username);
                            currentUserLocation.setLatitude(address.getDouble("latitude"));
                            currentUserLocation.setLongitude(address.getDouble("longitude"));
                            progressBar.setVisibility(View.GONE);
                        }
                        catch (JSONException e) {
                            Toast.makeText(getContext(), "User data fetched is corrupted! " +
                                            "Please log out and login and try again : " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        System.out.println("VolleyError" + volleyError);
                    }
                });
                RequestQueue rQueue = Volley.newRequestQueue(getContext());
                rQueue.add(request);

            }
        });
    }
    private void zoomToSelectedDistance() {
        System.out.println("Zoom to selected distance called");
        int distance = Integer.parseInt(distanceDropdown
                .getSelectedItem().toString()
                .split(" ")[1]);
        double iMeter = distance * 1609.34;
        Circle circle  = googleMap.addCircle(new CircleOptions()
                .center(currentUserPosition)
                .radius(iMeter) // Converting Miles into Meters...
                .strokeColor(Color.RED)
                .strokeWidth(5));
        circle.isVisible();
        float currentZoomLevel = getZoomLevel(circle);
        float animateZoom = currentZoomLevel + 5;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserPosition, animateZoom));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(currentZoomLevel), 2000, null);
        Marker currLocationMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentUserPosition)
                .title("You are here")
                .snippet("This is your current location"));
        currLocationMarker.setTag(null);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    if(marker.getTag() == null) {
                        return true;
                    }
                    System.out.println("marker tag = " + marker.getTag());
                    JSONObject markedDoctorSelectedJSON =
                            (JSONObject) marker.getTag();
                    selectedDoctor.setUserID(markedDoctorSelectedJSON.getString("userID"));
                    selectedDoctor.setName(markedDoctorSelectedJSON.getString("name"));

                    Location doctorsLocation = new Location(selectedDoctor.getName());
                    doctorsLocation.setLatitude(markedDoctorSelectedJSON.
                            getJSONObject("address").getDouble("latitude"));
                    doctorsLocation.setLongitude(markedDoctorSelectedJSON.
                            getJSONObject("address").getDouble("longitude"));
                    selectedDoctor.setDistance(currentUserLocation.distanceTo(doctorsLocation)
                            * 0.000621371f);
                    selectedDoctor.setSpeciality(markedDoctorSelectedJSON
                            .getString("speciality"));
                    //selectedDoctor.setRating(markedDoctorSelectedJSON.getInt("rating"));

                    selectedDoctor.setAddress(markedDoctorSelectedJSON.getJSONObject("address")
                            .getString("addressText"));
                    doctorDetailsTextView.setText(getString(R.string.doctorDetailsTextFormat,
                            selectedDoctor.getName(), selectedDoctor.getSpeciality(),
                            selectedDoctor.getAddress(), selectedDoctor.getDistance()));
                    for(int i=1; i <= selectedDoctor.getRating(); i++) {
                        starsMap.get("star"+i).setVisibility(View.VISIBLE);
                    }
                    if(markedDoctorSelectedJSON.has("profilePicturePath")) {
                        doctorPicLoadingProgressBar.setVisibility(View.VISIBLE);
                        selectedDoctorImageView.setVisibility(View.GONE);
                        selectedDoctor.setProfilePictureUrl(markedDoctorSelectedJSON.
                                getString("profilePicturePath"));

                        //Set current profile picture

                        FetchCurrentProfilePictureRunnable fetchProfilePictureRunnable =
                                new FetchCurrentProfilePictureRunnable();
                        Thread fetchPicThread = new Thread(fetchProfilePictureRunnable);
                        fetchPicThread.start();
                        try {
                            fetchPicThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if(!exceptionString.isEmpty()) {
                            Toast.makeText(getContext(), exceptionString, Toast.LENGTH_LONG).show();
                        }
                        else {
                            selectedDoctorImageView.setImageBitmap(bmp);
                        }
                        doctorPicLoadingProgressBar.setVisibility(View.GONE);
                        selectedDoctorImageView.setVisibility(View.VISIBLE);

                    }
                    else {
                        selectedDoctorImageView.setImageResource(
                                R.drawable.ic_user_default_foreground);
                    }

                    doctorDetailsSection.setVisibility(View.VISIBLE);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "User data fetched is corrupted! " +
                                    "Please log out and login and try again",
                            Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }
    class FetchCurrentProfilePictureRunnable implements Runnable {
        @Override
        public void run() {

            exceptionString = "";
            URL url;
            try {
                url = new URL(selectedDoctor.getProfilePictureUrl());
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

        private Bitmap getCroppedBitmap(Bitmap bitmap) {
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
            //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
            //return _bmp;
            return output;
        }
    }

    private float getZoomLevel(Circle circle) {
        float zoomLevel=0;
        if (circle != null){
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel =(int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel +.5f;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}