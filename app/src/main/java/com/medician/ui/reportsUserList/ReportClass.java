package com.medician.ui.reportsUserList;

public class ReportClass {
    String patientName;
    String doctorName;
    String type;
    String imageID;

    public ReportClass(String patientName, String doctorName, String type, String imageID){
        this.doctorName = doctorName;
        this.patientName = patientName;
        this.type = type;
        this.imageID = imageID;
    }

    public String getPatientName(){
        return this.patientName;
    }
    public String getDoctorName(){
        return this.doctorName;
    }
    public String getType(){
        return this.type;
    }
    public String getImageID(){return this.imageID;}
}
