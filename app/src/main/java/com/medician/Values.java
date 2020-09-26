package com.medician;

import java.util.ArrayList;
import java.util.Arrays;

public class Values {

    public final static String URL = "https://medician-neu.firebaseio.com";
    public final static ArrayList<String> specialities = new ArrayList(Arrays.asList("General Surgery", "Geriatrics",
            "Gynecologic Oncology", "Hematology/Oncology", "Hepatobiliary", "Hospitalist",
            "Infectious Disease", "Internal Medicine", "Interventional Radiology",
            "Medical Genetics", "Neonatology", "Nephrology", "Neuroradiology",
            "Neurology", "Neurosurgery", "Nuclear Medicine"));

    public static String capitalizeString(String str) {
        if (str != null && !str.equals(""))
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        return str;
    }

}
