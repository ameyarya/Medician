package com.medician.ui.covid19;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.medician.ConnectivityStatus;
import com.medician.R;
import com.medician.UserDetails;
import com.medician.ui.appointments.AppointmentListAdapter;
import com.medician.ui.reminders.RemindersFragment;
import com.medician.ui.reminders.RemindersListAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class CovidFragment extends Fragment {

    private final String baseURLStr = "https://coronavirus-19-api.herokuapp.com/";
    private String selectedOption = null;
    private TextView displayOutput;
    private CovidViewModel covidViewModel;
    private Button send;
    private ProgressBar progressBar;
    private Handler handler = new Handler();
    private Spinner spinner;
    private TextView noInternet;
    private View root;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityStatus.isConnected(getContext())) {
                // no connection
                noInternet.setText("No Internet");
                noInternet.setVisibility(View.VISIBLE);
                spinner.setVisibility(View.GONE);
                send.setVisibility(View.GONE);

                displayOutput = root.findViewById(R.id.country);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.cases);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.todayCases);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.deaths);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.todayDeaths);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.recovered);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.active);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.critical);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.casesPerOneMillion);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.deathsPerOneMillion);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.totalTests);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.testsPerOneMillion);
                displayOutput.setVisibility(View.GONE);

                displayOutput = root.findViewById(R.id.countryHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.casesHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.todayCasesHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.deathsHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.todayDeathsHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.recoveredHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.activeHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.criticalHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.casesPerOneMillionHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.deathsPerOneMillionHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.totalTestsHeading);
                displayOutput.setVisibility(View.GONE);
                displayOutput = root.findViewById(R.id.testsPerOneMillionHeading);
                displayOutput.setVisibility(View.GONE);

            } else {
                // connected
                noInternet.setVisibility(View.GONE);
                spinner.setVisibility(View.VISIBLE);
                send.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.country);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.cases);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.todayCases);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.deaths);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.todayDeaths);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.recovered);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.active);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.critical);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.casesPerOneMillion);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.deathsPerOneMillion);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.totalTests);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.testsPerOneMillion);
                displayOutput.setVisibility(View.VISIBLE);

                displayOutput = root.findViewById(R.id.countryHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.casesHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.todayCasesHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.deathsHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.todayDeathsHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.recoveredHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.activeHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.criticalHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.casesPerOneMillionHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.deathsPerOneMillionHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.totalTestsHeading);
                displayOutput.setVisibility(View.VISIBLE);
                displayOutput = root.findViewById(R.id.testsPerOneMillionHeading);
                displayOutput.setVisibility(View.VISIBLE);
                try {
                    covidCases("World", root);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        try {
            covidCases("World", root);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        getContext().registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        covidViewModel =
                ViewModelProviders.of(this).get(CovidViewModel.class);
        root = inflater.inflate(R.layout.fragment_covid19, container, false);
        progressBar = root.findViewById(R.id.covidApiProgressBar);
        send = root.findViewById(R.id.IDsendButton);
        noInternet = root.findViewById(R.id.noInternet);
        spinner = root.findViewById(R.id.spinner);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getCovidCases(root);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<>();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (country.trim().length() > 0 && !countries.contains(country)) {
                countries.add(country);
            }
        }
        countries.remove("United States");
        countries.add("USA");
        Collections.sort(countries);
        countries.add(0, "World");
        ArrayAdapter<String> countryAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, countries);

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(countryAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                selectedOption = parentView.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //todo
            }

        });

        return root;
    }

    private void getCovidCases(View v) throws MalformedURLException {
        progressBar.setVisibility(View.VISIBLE);
        covidCases(selectedOption, v);
        progressBar.setVisibility(View.GONE);
    }

    private void covidCases(String country, final View root) throws MalformedURLException {
        String updatedURLStr = "";
        if (country.equals("World")) {
            updatedURLStr = baseURLStr + "all";
            displayOutput = root.findViewById(R.id.country);
            displayOutput.setText("World");
            displayOutput = root.findViewById(R.id.todayCases);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.todayDeaths);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.active);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.critical);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.casesPerOneMillion);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.deathsPerOneMillion);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.totalTests);
            displayOutput.setText("N/A");
            displayOutput = root.findViewById(R.id.testsPerOneMillion);
            displayOutput.setText("N/A");
        } else {
            updatedURLStr = baseURLStr + "countries/" + country;
        }
        final URL countryURL = new URL(updatedURLStr);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
               handler.post(new Runnable() {
                   @Override
                   public void run() {
                       progressBar.setVisibility(View.VISIBLE);
                   }
               });
                try {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    HttpsURLConnection myConnection = (HttpsURLConnection) countryURL.openConnection();
                    if (myConnection.getResponseCode() == 200) {
                        InputStream responseBody = myConnection.getInputStream();
                        InputStreamReader responseBodyReader =
                                new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                        jsonReader.beginObject();
                        while (jsonReader.hasNext()) {
                            String key = jsonReader.nextName();
                            switch (key) {
                                case "country":
                                    displayOutput = root.findViewById(R.id.country);
                                    displayOutput.setText(jsonReader.nextString());
                                    break;
                                case "cases":
                                    displayOutput = root.findViewById(R.id.cases);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "todayCases":
                                    displayOutput = root.findViewById(R.id.todayCases);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "deaths":
                                    displayOutput = root.findViewById(R.id.deaths);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "todayDeaths":
                                    displayOutput = root.findViewById(R.id.todayDeaths);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "recovered":
                                    displayOutput = root.findViewById(R.id.recovered);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "active":
                                    displayOutput = root.findViewById(R.id.active);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "critical":
                                    displayOutput = root.findViewById(R.id.critical);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "casesPerOneMillion":
                                    displayOutput = root.findViewById(R.id.casesPerOneMillion);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "deathsPerOneMillion":
                                    displayOutput = root.findViewById(R.id.deathsPerOneMillion);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "totalTests":
                                    displayOutput = root.findViewById(R.id.totalTests);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                case "testsPerOneMillion":
                                    displayOutput = root.findViewById(R.id.testsPerOneMillion);
                                    displayOutput.setText(String.valueOf(jsonReader.nextInt()));
                                    break;
                                default:
                                    jsonReader.skipValue();
                            }
                        }
                        jsonReader.close();
                        myConnection.disconnect();

                    } else {

                        //todo
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        });
    }

}