package com.ndmc.ndmc_record.domain;


import com.ndmc.ndmc_record.enums.ApprovalEnum;
import com.ndmc.ndmc_record.enums.DataFlagEnum;
import com.ndmc.ndmc_record.enums.GenderEnum;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@ToString
@Entity
@Table(name = "child_details")
public class ChildDetails {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UUID",strategy = GenerationType.SEQUENCE)

    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "child_id", updatable = false, nullable = false,length=36)
    private UUID childId;
    @Column(name = "registration_number",length=50)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "req_status",nullable=false)
    private ApprovalEnum req_status;
    @Enumerated(EnumType.STRING)
    @Column(name = "gender",nullable=false)
    private GenderEnum gender;

    @Column(name = "date_of_birth",length=20)
    private String dateOfBirth;

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    @Column(name = "childName",length=20)
    private String childName;


    @Column(name = "date_of_time",length=20)
    private String dateOfTime;

    @Column(name = "birth_city_name",length=20)
    private String birthCityName;

    @Column(name = "birth_country_name",length=20)
    private String birthCountryName;

    @Column(name = "mother_name",length=20)
    private String motherName;

    @Column(name = "father_name",length=20)
    private String fatherName;

    @Column(name = "address",length=20)
    private String address;

    @Column(name = "city",length=20)
    private String city;

    @Column(name = "country",length=20)
    private String country;

    @Column(name = "state",length=20)
    private String state;

    @Column(name = "postal_code",length=20)
    private String postalCode;

    @Column(name = "religion",length=20)
    private String religion;

    @Column(name = "gurdian_aadhar_no",length=20)
    private String gurdianAadharNo;

    @Column(name = "father_aadhar_no",length=20)
    private String fatherAadharNo;

    @Column(name = "mother_aadhar_no",length=20)
    private String motherAadharNo;

    @Column(name = "child_aadhar_no",length=20)
    private String childAadharNo;

    public String getGurdianAadharNo() {
        return gurdianAadharNo;
    }

    public ApprovalEnum getReq_status() {
        return req_status;
    }

    public void setReq_status(ApprovalEnum req_status) {
        this.req_status = req_status;
    }



    public void setGurdianAadharNo(String gurdianAadharNo) {
        this.gurdianAadharNo = gurdianAadharNo;
    }

    public String getFatherAadharNo() {
        return fatherAadharNo;
    }

    public void setFatherAadharNo(String fatherAadharNo) {
        this.fatherAadharNo = fatherAadharNo;
    }

    public String getMotherAadharNo() {
        return motherAadharNo;
    }

    public void setMotherAadharNo(String motherAadharNo) {
        this.motherAadharNo = motherAadharNo;
    }

    public String getChildAadharNo() {
        return childAadharNo;
    }

    public void setChildAadharNo(String childAadharNo) {
        this.childAadharNo = childAadharNo;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public UUID getChildId() {
        return childId;
    }

    public ChildDetails() {
        super();
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public GenderEnum getGender() {
        return gender;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDateOfTime() {
        return dateOfTime;
    }

    public void setDateOfTime(String dateOfTime) {
        this.dateOfTime = dateOfTime;
    }

    public String getBirthCityName() {
        return birthCityName;
    }

    public void setBirthCityName(String birthCityName) {
        this.birthCityName = birthCityName;
    }

    public String getBirthCountryName() {
        return birthCountryName;
    }

    public void setBirthCountryName(String birthCountryName) {
        this.birthCountryName = birthCountryName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public ChildDetails(UUID childId, String registrationNumber, ApprovalEnum req_status, GenderEnum gender, String dateOfBirth, String childName, String dateOfTime, String birthCityName, String birthCountryName, String motherName, String fatherName, String address, String city, String country, String state, String postalCode, String religion, String gurdianAadharNo, String fatherAadharNo, String motherAadharNo, String childAadharNo, DataFlagEnum flag, String createdBy, String modifiedBy, Date createdDate, Date modifiedDate) {
        this.childId = childId;
        this.registrationNumber = registrationNumber;
        this.req_status = req_status;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.childName = childName;
        this.dateOfTime = dateOfTime;
        this.birthCityName = birthCityName;
        this.birthCountryName = birthCountryName;
        this.motherName = motherName;
        this.fatherName = fatherName;
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.postalCode = postalCode;
        this.religion = religion;
        this.gurdianAadharNo = gurdianAadharNo;
        this.fatherAadharNo = fatherAadharNo;
        this.motherAadharNo = motherAadharNo;
        this.childAadharNo = childAadharNo;
        this.flag = flag;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }


    public DataFlagEnum getFlag() {
        return flag;
    }

    public void setFlag(DataFlagEnum flag) {
        this.flag = flag;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "flag",nullable=false)
    private DataFlagEnum flag;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public void setChildId(UUID childId) {
        this.childId = childId;
    }

    @Column(name = "created_by",length=50)
    private String createdBy;

    @Column(name = "modified_by",length=50)
    private String modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date",length=50,updatable = false)
    private Date createdDate;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modified_date",length=50)
    private Date modifiedDate;


}
