package com.medician;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.medician.chat.ChatList;
import com.medician.ui.profile.ProfileFragment;
import com.medician.ui.reminders.RemindersFragment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class Dashboard extends AppCompatActivity {

    public Bitmap bmp;
    private AppBarConfiguration mAppBarConfiguration;
    private TextView userNameTextView;
    private ImageView loggedInUserImageView;
    private String exceptionString;
    private TextView profileEditButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        exceptionString = "";
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, ChatList.class));
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        UserDetails.fullName = LocalSharedPreferencesData.getPreferredUserData(this, "fullName");
        UserDetails.profilePicPath = LocalSharedPreferencesData.getPreferredUserData(this, "profilePicPath");
        UserDetails.type = LocalSharedPreferencesData.getPreferredUserData(this, "type");
        UserDetails.address = LocalSharedPreferencesData.getPreferredUserData(this, "address");
        UserDetails.speciality = LocalSharedPreferencesData.getPreferredUserData(this, "speciality");

        if (UserDetails.type.equals("Doctor")) {
            navigationView.getMenu().findItem(R.id.nav_find_a_doctor).setVisible(false);
        }
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_appointments, R.id.nav_reminders, R.id.nav_find_a_doctor,
                R.id.nav_reports, R.id.nav_covid, R.id.nav_contact_us,R.id.nav_log_out,R.id.nav_profile)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        NavigationUI.setupWithNavController(navigationView, navController);
        userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.loggedInUserNameTextView);
        userNameTextView.setText(UserDetails.fullName);
        loggedInUserImageView = navigationView.getHeaderView(0).findViewById(R.id.loggedInUserImageView);
//        profileEditButton = navigationView.getHeaderView(0).findViewById(R.id.profileEditButton);
//        profileEditButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intentProfile = new Intent(Dashboard.this, Profile.class);
//                startActivity(intentProfile);
//            }
//        });
        bmp = null;

        if (!UserDetails.profilePicPath.isEmpty()) {
            FetchProfilePictureRunnable fetchProfilePictureRunnable =
                    new FetchProfilePictureRunnable();
            Thread fetchPicThread = new Thread(fetchProfilePictureRunnable);
            fetchPicThread.start();
            try {
                fetchPicThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!exceptionString.isEmpty()) {
                Toast.makeText(this, exceptionString, Toast.LENGTH_LONG).show();
            } else {
                bmp = getCroppedBitmap(bmp);
                loggedInUserImageView.getLayoutParams().height = (int) getResources()
                        .getDimension(R.dimen.profile_pic_size);
                loggedInUserImageView.getLayoutParams().width = (int) getResources().getDimension(R.dimen.profile_pic_size);
                loggedInUserImageView.requestLayout();
                loggedInUserImageView.setImageBitmap(bmp);
            }
        }

//        profile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intentProfile = new Intent(Dashboard.this, Profile.class);
//                startActivity(intentProfile);
//            }
//        });

//        final NavigationView logout = findViewById(R.id.nav_logout);
//        logout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                logout();
//            }
//

    }

    @Override
    public void onBackPressed() {
        final AlertDialog alertDialog = new AlertDialog.Builder(Dashboard.this).create();
        alertDialog.setTitle("Exit Application");
        alertDialog.setMessage("Do you really wanna exit the application?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                Intent a = new Intent(Intent.ACTION_MAIN);
                a.addCategory(Intent.CATEGORY_HOME);
                a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(a);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_refresh:
////                Intent intentProfile = new Intent(Dashboard.this, Dashboard.class);
////                startActivity(intentProfile);
//                recreate();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    private void logout() {
        LocalSharedPreferencesData.clearData(Dashboard.this);
        UserDetails.profilePicPath = "";
        Intent intentLogout = new Intent(Dashboard.this, Login.class);
        startActivity(intentLogout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        startService(new Intent(this, NotificationService.class));
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (UserDetails.profileUpdated) {
            LocalSharedPreferencesData.savePreferredUserData(
                    getApplicationContext(), UserDetails.username,
                    UserDetails.fullName, UserDetails.type,
                    UserDetails.address, UserDetails.profilePicPath, UserDetails.speciality);
            System.out.println("Here Here Here " + UserDetails.profileUpdated);
            UserDetails.profileUpdated = false;
            Intent intent = new Intent(Dashboard.this, MainActivity.class);
            startActivity(intent);
            finish();
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
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    class FetchProfilePictureRunnable implements Runnable {
        @Override
        public void run() {
            exceptionString = "";
            URL url;
            try {
                url = new URL(UserDetails.profilePicPath);
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (MalformedURLException e) {
                exceptionString = "Bad URL : Unable to load Profile Picture.";

                e.printStackTrace();
            } catch (IOException e) {
                exceptionString = "IO Failed : Unable to load Profile Picture.";
            }
        }
    }
}