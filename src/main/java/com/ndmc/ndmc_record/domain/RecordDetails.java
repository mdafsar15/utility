package com.ndmc.ndmc_record.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;
import java.util.Date;

@Validated
public class RecordDetails {

    @JsonProperty("applicationNumber")
    private String applicationNumber = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("divisionCode")
    private String divisionCode = null;

    @JsonProperty("dateOfEvent")
    private String dateOfEvent = null;

    @JsonProperty("activityCode")
    private String activityCode = null;

    @JsonProperty("dateOfRegistration")
    private String dateOfRegistration = null;

    @JsonProperty("placeOfEvent")
    private String placeOfEvent = null;

    @JsonProperty("gender")
    private String gender = null;

    @JsonProperty("fatherName")
    private String fatherName = null;

    @JsonProperty("fatherLiteracy")
    private String fatherLiteracy = null;

    @JsonProperty("fatherOccupation")
    private String fatherOccupation = null;

    @JsonProperty("fatherNationality")
    private String fatherNationality = null;

    @JsonProperty("fatherReligion")
    private String fatherReligion = null;

    @JsonProperty("motherName")
    private String motherName = null;

    @JsonProperty("motherNationality")
    private String motherNationality = null;

    @JsonProperty("permanentAddress")
    private String permanentAddress = null;

    @JsonProperty("createdBy")
    private String createdBy = null;

    @JsonProperty("createdAt")
    private Date createdAt = null;

    @JsonProperty("modifiedAt")
    private Date modifiedAt = null;

    @JsonProperty("modifiedBy")
    private String modifiedBy = null;

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public String getDateOfEvent() {
        return dateOfEvent;
    }

    public void setDateOfEvent(String dateOfEvent) {
        this.dateOfEvent = dateOfEvent;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getPlaceOfEvent() {
        return placeOfEvent;
    }

    public void setPlaceOfEvent(String placeOfEvent) {
        this.placeOfEvent = placeOfEvent;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getFatherLiteracy() {
        return fatherLiteracy;
    }

    public void setFatherLiteracy(String fatherLiteracy) {
        this.fatherLiteracy = fatherLiteracy;
    }

    public String getFatherOccupation() {
        return fatherOccupation;
    }

    public void setFatherOccupation(String fatherOccupation) {
        this.fatherOccupation = fatherOccupation;
    }

    public String getFatherNationality() {
        return fatherNationality;
    }

    public void setFatherNationality(String fatherNationality) {
        this.fatherNationality = fatherNationality;
    }

    public String getFatherReligion() {
        return fatherReligion;
    }

    public void setFatherReligion(String fatherReligion) {
        this.fatherReligion = fatherReligion;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherNationality() {
        return motherNationality;
    }

    public void setMotherNationality(String motherNationality) {
        this.motherNationality = motherNationality;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
