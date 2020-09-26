package com.medician.ui.reminders;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.medician.ConnectivityStatus;
import com.medician.LocalSharedPreferencesData;
import com.medician.R;
import com.medician.UserDetails;
import com.medician.Values;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.ALARM_SERVICE;

public class RemindersFragment extends Fragment {

    private ListView remList;
    private TextView noRemText;
    private int totalRem = 0;
    private ProgressDialog pd;
    private Button addRem;
    private String remName;
    private RemindersListAdapter remindersListAdapter;
    private List<String> remIdArray = new ArrayList<>();
    private List<String> nameArray = new ArrayList<>();
    private List<String> dateArray = new ArrayList<>();
    private Firebase reference;
    private RemindersViewModel remindersViewModel;
    private Dialog dialog;
    private TextView refresh;
    private View root;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityStatus.isConnected(getContext())) {
                // no connection
                noRemText.setText("No Internet");
                noRemText.setVisibility(View.VISIBLE);
                remList.setVisibility(View.GONE);
                addRem.setVisibility(View.GONE);
            } else {
                // connected
                if (totalRem < 1) {
                    noRemText.setVisibility(View.VISIBLE);
                    remList.setVisibility(View.GONE);
                } else {
                    noRemText.setVisibility(View.GONE);
                    remList.setVisibility(View.VISIBLE);
                    remindersListAdapter = new RemindersListAdapter(getActivity(), nameArray, dateArray);
                    remList.setAdapter(remindersListAdapter);
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        reminders(root);
        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        remindersViewModel =
                ViewModelProviders.of(this).get(RemindersViewModel.class);
        root = inflater.inflate(R.layout.fragment_reminders, container, false);

        Firebase.setAndroidContext(getContext());
        UserDetails.username = LocalSharedPreferencesData.getPreferredUserData(getContext(), "userName");
        remList = (ListView) root.findViewById(R.id.remindersList);
        noRemText = (TextView) root.findViewById(R.id.noRemText);
        addRem = root.findViewById(R.id.addReminder);
        refresh = root.findViewById(R.id.refreshRem);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        return root;
    }

    private void reminders(View root) {
        String url = Values.URL + "/reminders.json";

        final StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("VolleyError" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);

        remList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final String remId = remIdArray.get(position);
                final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle(nameArray.get(position));
                alertDialog.setMessage("Do you want to delete this reminder?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        Firebase.setAndroidContext(getContext());
                        reference = new Firebase(Values.URL + "/reminders/" + remId);
                        reference.removeValue();
                        refresh();
                        Toast.makeText(getContext(), "Reminder deleted!", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        addRem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addReminder();
            }
        });
    }

    public void addReminder() {

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.floating_popup);

        final TextView textView = dialog.findViewById(R.id.date);
        Button select, add;
        select = dialog.findViewById(R.id.selectDate);
        add = dialog.findViewById(R.id.addButton);
        final EditText message = dialog.findViewById(R.id.message);


        final Calendar newCalender = Calendar.getInstance();
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                newDate.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                Calendar tem = Calendar.getInstance();
                                Log.w("TIME", System.currentTimeMillis() + "");
                                if (newDate.getTimeInMillis() - tem.getTimeInMillis() > 0)
                                    textView.setText(newDate.getTime().toString());
                                else
                                    Toast.makeText(getContext(), "Invalid time", Toast.LENGTH_SHORT).show();

                            }
                        }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true);
                        time.show();

                    }
                }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();

            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!textView.getText().toString().trim().equals("") && !message.getText().toString().trim().equals("")) {
                    Reminders reminders = new Reminders();
                    reminders.setMessage(message.getText().toString().trim());
                    final Date remind = new Date(textView.getText().toString().trim());
                    reminders.setRemindDate(remind);
                    remName = UserDetails.username + "_" + message.getText().toString().trim();

                    final ProgressDialog pd = new ProgressDialog(getContext());
                    pd.setMessage("Loading...");
                    pd.show();

                    String url = Values.URL + "/reminders.json";
                    StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String s) {

                            Firebase reference = new Firebase(Values.URL + "/reminders");
                            try {

                                reference.child(remName).child("name").setValue(message.getText().toString().trim());
                                reference.child(remName).child("date").setValue(remind.toString().trim());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            refresh();
                            pd.dismiss();
                        }

                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            System.out.println("volleyError" + volleyError);
                            pd.dismiss();
                        }
                    });

                    RequestQueue rQueue = Volley.newRequestQueue(getContext());
                    rQueue.add(request);

                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
                    calendar.setTime(remind);
                    calendar.set(Calendar.SECOND, 0);
                    Intent intent = new Intent(getContext(), NotifierAlarm.class);
                    intent.putExtra("Message", reminders.getMessage());
                    intent.putExtra("RemindDate", reminders.getRemindDate().toString());
                    PendingIntent intent1 = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);
                    Toast.makeText(getContext(), "Reminder for " + message.getText().toString().trim() + " added!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(getContext(), "Reminder name and date can not be blank!", Toast.LENGTH_LONG).show();
                }
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void refresh() {

        Fragment fragment = new RemindersFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_reminders, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    private void doOnSuccess(String s) {
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();
            String key = "";

            while (i.hasNext()) {
                key = i.next().toString();
                if (key.contains(UserDetails.username)) {
                    remIdArray.add(key);
                    String title = "" + (key.split("_")[1]);
                    nameArray.add(title);
                    dateArray.add(obj.getJSONObject(key).getString("date"));
                    totalRem++;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (totalRem < 1) {
            noRemText.setVisibility(View.VISIBLE);
            remList.setVisibility(View.GONE);
        } else {
            noRemText.setVisibility(View.GONE);
            remList.setVisibility(View.VISIBLE);
            remindersListAdapter = new RemindersListAdapter(getActivity(), nameArray, dateArray);
            remList.setAdapter(remindersListAdapter);
        }
    }
}