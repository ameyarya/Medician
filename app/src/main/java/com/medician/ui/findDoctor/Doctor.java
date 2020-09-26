package com.medician.ui.findDoctor;

import java.util.Random;

public class Doctor {
    private String userID;
    private String name;
    private String speciality;
    private int rating;
    private String address;
    private float distance;
    private String profilePictureUrl;

    public Doctor() {
        this.userID = "sample_ID";
        this.name = "Dr. XYZ";
        this.speciality = "Pediatrician";
        this.rating = new Random().nextInt(5)+1;
        this.address = "121 Huntington St, Boston";
        this.distance = 1.5f;
        profilePictureUrl = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
