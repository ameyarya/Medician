package com.medician;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class LocalSharedPreferencesData {

    private static final String LOGGED_IN_USERNAME = "userNameKey";
    private static final String LOGGED_IN_FULLNAME = "fullNameKey";
    private static final String LOGGED_IN_TYPE = "typeKey";
    private static final String LOGGED_IN_ADDRESS = "addressKey";
    private static final String LOGGED_IN_PROFILE_PIC_PATH = "profilePicKey";
    private static final String LOGGED_IN_DOCTOR_SPECIALITY = "";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void savePreferredUserData(Context context, String userName, String fullName,
                                             String type, String address, String profilePicPath,
                                             String doctorSpeciality) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(LOGGED_IN_USERNAME, userName);
        editor.putString(LOGGED_IN_FULLNAME, fullName);
        editor.putString(LOGGED_IN_TYPE, type);
        editor.putString(LOGGED_IN_ADDRESS, address);
        editor.putString(LOGGED_IN_PROFILE_PIC_PATH, profilePicPath);
        editor.putString(LOGGED_IN_DOCTOR_SPECIALITY, doctorSpeciality);
        editor.commit();
    }

    public static String getPreferredUserData(Context context, String typeOfData) {
        String returnValue = "";
        switch (typeOfData) {
            case "userName":
                returnValue = getSharedPreferences(context).getString(LOGGED_IN_USERNAME, "");
                break;
            case "fullName":
                returnValue = getSharedPreferences(context).getString(LOGGED_IN_FULLNAME, "");
                break;
            case "type":
                returnValue = getSharedPreferences(context).getString(LOGGED_IN_TYPE, "");
                break;
            case "address":
                returnValue = getSharedPreferences(context).getString(LOGGED_IN_ADDRESS, "");
                break;
            case "profilePicPath":
                returnValue = getSharedPreferences(context).getString(LOGGED_IN_PROFILE_PIC_PATH, "");
                break;
            case "speciality":
                returnValue = getSharedPreferences(context).getString(LOGGED_IN_DOCTOR_SPECIALITY, "");
        }
        return returnValue;
    }

    public static void clearData(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

}
