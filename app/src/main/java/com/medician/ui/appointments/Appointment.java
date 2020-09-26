package com.medician.ui.appointments;


public class Appointment {
    private String doctorID;
    private String doctorFullName;
    private String patientID;
    private String patientFullName;
    private long appointmentTimeInMillis;
    private String illnessDescription;
    private String dateAndTime;

    public Appointment(){}

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public Appointment(String illnessDescription, String dateAndTime){
        this.illnessDescription = illnessDescription;
        this.dateAndTime = dateAndTime;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(String doctorID) {
        this.doctorID = doctorID;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public long getAppointmentTimeInMillis() {
        return appointmentTimeInMillis;
    }

    public void setAppointmentTimeInMillis(long appointmentTimeInMillis) {
        this.appointmentTimeInMillis = appointmentTimeInMillis;
    }

    public void setIllnessDescription(String illnessDescription) {
        this.illnessDescription = illnessDescription;
    }

    public String getIllnessDescription() {
        return illnessDescription;
    }

    public String getDoctorFullName() {
        return doctorFullName;
    }

    public void setDoctorFullName(String doctorFullName) {
        this.doctorFullName = doctorFullName;
    }

    public String getPatientFullName() {
        return patientFullName;
    }

    public void setPatientFullName(String patientFullName) {
        this.patientFullName = patientFullName;
    }
}
