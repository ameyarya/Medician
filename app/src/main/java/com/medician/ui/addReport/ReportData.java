package com.medician.ui.addReport;


public class ReportData {

    String doctor_id;
    String patient_id;
    String type;
    String imageId;

    public String getDoctorName() {
        return doctor_id;
    }

    public void setDoctorName(String doctorName) {
        this.doctor_id = doctorName;
    }


    public String getPatientName() {
        return patient_id;
    }

    public void setPatientName(String patientName) {
        this.patient_id = patientName;
    }

    public String getReportType() {
        return type;
    }

    public void setReportType(String reportType) {
        this.type = reportType;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public ReportData(){

    }



}
