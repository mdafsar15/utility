package com.ndmc.ndmc_record.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomBeanUtils {


    // function to copy content from BirthDto birthDto to BirthModel birthModel

    public static void copyBirthDetailsForUpdate(BirthDto birthDto, BirthModel birthModel) {

        birthModel.setEventPlace(birthDto.getEventPlace());
        birthModel.setPermanentAddress(birthDto.getPermanentAddress());
        birthModel.setFatherName(birthDto.getFatherName());
        birthModel.setFatherLiteracy(birthDto.getFatherLiteracy());
        birthModel.setFatherOccupation(birthDto.getFatherOccupation());
        birthModel.setFatherReligion(birthDto.getFatherReligion());
        birthModel.setMotherName(birthDto.getMotherName());
        birthModel.setMotherLiteracy(birthDto.getMotherLiteracy());
        birthModel.setMotherOccupation(birthDto.getMotherOccupation());
        birthModel.setMotherReligion(birthDto.getMotherReligion());
        birthModel.setMotherAge(birthDto.getMotherAge());
        birthModel.setNumberWeekPregnancy(birthDto.getNumberWeekPregnancy());
        birthModel.setIllegitimateBirth(birthDto.getIllegitimateBirth());
        birthModel.setBirthOrder(birthDto.getBirthOrder());
        birthModel.setDeliveryAttentionCode(birthDto.getDeliveryAttentionCode());
        birthModel.setIsVillageTown(birthDto.getIsVillageTown());
        birthModel.setNameVillageTown(birthDto.getNameVillageTown());
        birthModel.setDistrictName(birthDto.getDistrictName());
        birthModel.setStateName(birthDto.getStateName());
        birthModel.setEventPlaceFlag(birthDto.getEventPlaceFlag());
        birthModel.setIsDelhiResident(birthDto.getIsDelhiResident());
        birthModel.setIsNdmcResident(birthDto.getIsNdmcResident());
        birthModel.setAddressAtBirth(birthDto.getAddressAtBirth());
        birthModel.setMotherAgeAtMarriage(birthDto.getMotherAgeAtMarriage());
        birthModel.setMethodOfDelivery(birthDto.getMethodOfDelivery());
        birthModel.setInformantName(birthDto.getInformantName());
        birthModel.setInformantAddress(birthDto.getInformantAddress());
        birthModel.setIsOralInformant(birthDto.getIsOralInformant());
        birthModel.setActivityCode(birthDto.getActivityCode());
        birthModel.setCrNumber(CommonUtil.checkNullOrBlank(birthDto.getCrNumber()) ? "0" : birthDto.getCrNumber());
        birthModel.setLateFee(birthDto.getLateFee());
        birthModel.setFatherAdharNumber(birthDto.getFatherAdharNumber());
        birthModel.setMotherAdharNumber(birthDto.getMotherAdharNumber());
        birthModel.setOrganizationCode(birthDto.getOrganizationCode());
        birthModel.setStatus(birthDto.getStatus());
        birthModel.setContactNumber(birthDto.getContactNumber());
        birthModel.setPinCode(birthDto.getPinCode());
        birthModel.setMotherNationality(birthDto.getMotherNationality());
        birthModel.setFatherNationality(birthDto.getFatherNationality());
        birthModel.setRecordType(birthDto.getRecordType());
        birthModel.setSdmLetterNo(birthDto.getSdmLetterNo());
        birthModel.setArea(birthDto.getArea());
        //Added on 19-07-2022 by Deepak

       // birthModel.setRegistrationDatetime(birthDto.getRegistrationDate().atStartOfDay());
       // birthModel.setRegistrationDatetime(birthDto.getRegistrationDate() == null ? LocalDateTime.now() : birthDto.getRegistrationDate().atStartOfDay());

        //birthModel.setRegistrationDatetime(birthDto.getRegistrationDate().equals("") ? LocalDateTime.now() : birthDto.getRegistrationDate().atStartOfDay());


    }

    public static void copyDeathDetailsForUpdate(DeathDto deathDto, DeathModel deathModel) {
        deathModel.setDeathId(deathDto.getDeathId());

        // private String eventPlace;
        deathModel.setEventPlace(deathDto.getEventPlace());
        // private String eventDate;
        deathModel.setEventDate(deathDto.getEventDate());
        // private String genderCode;
        deathModel.setGenderCode(deathDto.getGenderCode());
        // private String name;
        deathModel.setName(deathDto.getName());
        // private String permanentAddress;
        deathModel.setPermanentAddress(deathDto.getPermanentAddress());
        // private String fatherName;
        deathModel.setFatherName(deathDto.getFatherName());
        // private String motherName;
        deathModel.setMotherName(deathDto.getMotherName());
        // private String isVillageTown;
        deathModel.setIsVillageTown(deathDto.getIsVillageTown());
        // private String nameVillageTown;
        deathModel.setNameVillageTown(deathDto.getNameVillageTown());
        // private String districtName;
        deathModel.setDistrictName(deathDto.getDistrictName());
        // private String stateName;
        deathModel.setStateName(deathDto.getStateName());
        // private String eventPlaceFlag;
        deathModel.setEventPlaceFlag(deathDto.getEventPlaceFlag());
        // private String isDelhiResident;
        deathModel.setIsDelhiResident(deathDto.getIsDelhiResident());
        // private String isNdmcResident;
        deathModel.setIsNdmcResident(deathDto.getIsNdmcResident());
        // private Integer maritalStatusCode;
        deathModel.setMaritalStatusCode(deathDto.getMaritalStatusCode());
        // private String causeOfDeath;
        deathModel.setCauseOfDeath(deathDto.getCauseOfDeath());
        // private String isPregnant;
        deathModel.setIsPregnant(deathDto.getIsPregnant());
        // private Integer yearSmoking;
        deathModel.setYearsSmoking(deathDto.getYearsSmoking());
        // private Integer yearDrinking;
        deathModel.setYearsDrinking(deathDto.getYearsDrinking());
        // private Integer yearTobacco;
        deathModel.setYearsTobacco(deathDto.getYearsTobacco());
        // private String informantName;
        deathModel.setInformantName(deathDto.getInformantName());
        // private String informantAddress;
        deathModel.setInformantAddress(deathDto.getInformantAddress());
        // private String isOralInformant;
        deathModel.setIsOralInformant(deathDto.getIsOralInformant());
        // private String isMedicalCertified;
        deathModel.setIsMedicalCertified(deathDto.getIsMedicalCertified());
        // private String deceasedOccupation;
        deathModel.setDeceasedOccupation(deathDto.getDeceasedOccupation());
        // private Integer activityCode;
        deathModel.setActivityCode(deathDto.getActivityCode());
        // private String deceasedAge;
        deathModel.setDeceasedAge(deathDto.getDeceasedAge());
        // private String deceasedReligion;
        deathModel.setDeceasedReligion(deathDto.getDeceasedReligion());
        // private Integer medicalAttentionCode;
        deathModel.setMedicalAttentionCode(deathDto.getMedicalAttentionCode());
        // private String applStatus;
        deathModel.setApplStatus(deathDto.getApplStatus());
        // private String crNumber;
        deathModel.setCrNumber(CommonUtil.checkNullOrBlank(deathDto.getCrNumber()) ? "0" : deathDto.getCrNumber());

        // private String causeOfDeathDetails;
        deathModel.setCauseOfDeathDetails(deathDto.getCauseOfDeathDetails());
        // private Float lateFee;
        deathModel.setLateFee(deathDto.getLateFee());
        // private String modifiedBy;
        deathModel.setModifiedBy(deathDto.getModifiedBy());
        // private String fatherAdharNumber;
        deathModel.setFatherAdharNumber(deathDto.getFatherAdharNumber());
        // private String motherAdharNumber;
        deathModel.setMotherAdharNumber(deathDto.getMotherAdharNumber());
        // private String deceasedAdharNumber;
        deathModel.setDeceasedAdharNumber(deathDto.getDeceasedAdharNumber());
        // private String educationCode; // API call to get education details
        deathModel.setEducationCode(deathDto.getEducationCode());
        // private String religionCode; // API
        deathModel.setReligionCode(deathDto.getReligionCode());
        // private String nationality;
        deathModel.setNationality(deathDto.getNationality());
        // private String occupationCode; // API
        deathModel.setOccupationCode(deathDto.getOccupationCode());
        // private String organizationCode;
        deathModel.setOrganizationCode(deathDto.getOrganizationCode());
        // private String status;
        deathModel.setStatus(deathDto.getStatus());
        // private String contactNumber;
      deathModel.setContactNumber(deathDto.getContactNumber());
        // private String pinCode;
    deathModel.setPinCode(deathDto.getPinCode());
        // private String motherNationality;
    deathModel.setMotherNationality(deathDto.getMotherNationality());
        // private String fatherNationality;
    deathModel.setFatherNationality(deathDto.getFatherNationality());
        // private String applicantName;
    deathModel.setApplicantAddress(deathDto.getApplicantAddress());
        // private String applicantAddress;
        deathModel.setApplicantName(deathDto.getApplicantName());

//        private String chewArecanut;
        deathModel.setChewArecanut(deathDto.getChewArecanut());
//         private String informantStreet;
        deathModel.setInformantStreet(deathDto.getInformantStreet());
//        private String informantDoorNo;
        deathModel.setInformantDoorNo(deathDto.getInformantDoorNo());
//        private String husbandWifeUID;
        deathModel.setHusbandWifeUID(deathDto.getHusbandWifeUID());
//        private String husbandWifeName;
        deathModel.setHusbandWifeName(deathDto.getHusbandWifeName());

        deathModel.setArea(deathDto.getArea());

        deathModel.setRecordType(deathDto.getRecordType());
        deathModel.setSdmLetterNo(deathDto.getSdmLetterNo());
       // deathModel.setSdmLetterImage(deathDto.getSdmLetterImage());
        deathModel.setAddressAtDeath(deathDto.getAddressAtDeath());

        deathModel.setIsUnkownCase(deathDto.getIsUnkownCase());

        // deathModel.setRegistrationNumber(deathDto.getRegistrationNumber());
         deathModel.setRegistrationDatetime(deathDto.getRegistrationDate() == null ? LocalDateTime.now() : deathDto.getRegistrationDate().atStartOfDay());

       // deathModel.setRegistrationDatetime(deathDto.getRegistrationDate().atStartOfDay());
    }


    // function to copy content from BirthDto birthDto to BirthModel birthModel

    public static void copySBirthDetailsForUpdate(SBirthDto sBirthDto, SBirthModel sBirthModel) {
        // private Long birthId;
        sBirthModel.setSbirthId(sBirthDto.getSbirthId());
        // private String eventPlace;
        sBirthModel.setEventPlace(sBirthDto.getEventPlace());
        // private String eventDate;
        sBirthModel.setEventDate(sBirthDto.getEventDate());
        // private String genderCode;
        sBirthModel.setGenderCode(sBirthDto.getGenderCode());
        // private String name;
        sBirthModel.setName(sBirthDto.getName());
        // private String permanentAddress;
        sBirthModel.setPermanentAddress(sBirthDto.getPermanentAddress());
        // private String fatherName;
        sBirthModel.setFatherName(sBirthDto.getFatherName());
        // private String fatherLiteracy;
        sBirthModel.setFatherLiteracy(sBirthDto.getFatherLiteracy());
        // private String fatherOccupation;
        sBirthModel.setFatherOccupation(sBirthDto.getFatherOccupation());
        // private String fatherReligion;
        sBirthModel.setFatherReligion(sBirthDto.getFatherReligion());
        // private String motherName;
        sBirthModel.setMotherName(sBirthDto.getMotherName());
        // private String motherLiteracy;
        sBirthModel.setMotherLiteracy(sBirthDto.getMotherLiteracy());
        // private String motherOccupation;
        sBirthModel.setMotherOccupation(sBirthDto.getMotherOccupation());
        // private String motherReligion;
        sBirthModel.setMotherReligion(sBirthDto.getMotherReligion());
        // private String motherAge;
        sBirthModel.setMotherAge(sBirthDto.getMotherAge());
        // private Float childWeight;
        sBirthModel.setChildWeight(sBirthDto.getChildWeight());
        // private String numberWeekPregnancy;
        sBirthModel.setNumberWeekPregnancy(sBirthDto.getNumberWeekPregnancy());
        // private String illegimateBirth;
        sBirthModel.setIllegitimateBirth(sBirthDto.getIllegitimateBirth());
        // private String birthOrder;
        sBirthModel.setBirthOrder(sBirthDto.getBirthOrder());
        // private String deliveryAttentionCode;
        sBirthModel.setDeliveryAttentionCode(sBirthDto.getDeliveryAttentionCode());
        // private String isVillageTown;
        sBirthModel.setIsVillageTown(sBirthDto.getIsVillageTown());
        // private String nameVillageTown;
        sBirthModel.setNameVillageTown(sBirthDto.getNameVillageTown());
        // private String districtName;
        sBirthModel.setDistrictName(sBirthDto.getDistrictName());
        // private String stateName;
        sBirthModel.setStateName(sBirthDto.getStateName());
        // private String eventPlaceFlag;
        sBirthModel.setEventPlaceFlag(sBirthDto.getEventPlaceFlag());
        // private String isDelhiResident;
        sBirthModel.setIsDelhiResident(sBirthDto.getIsDelhiResident());
        // private String isNdmcResident;
        sBirthModel.setIsNdmcResident(sBirthDto.getIsNdmcResident());
        // private String addressAtBirth;
        sBirthModel.setAddressAtBirth(sBirthDto.getAddressAtBirth());
        // private String motherAgeAtMarriage;
        sBirthModel.setMotherAgeAtMarriage(sBirthDto.getMotherAgeAtMarriage());
        // private String methodOfDelivery;
        sBirthModel.setMethodOfDelivery(sBirthDto.getMethodOfDelivery());
        // private String informantName;
        sBirthModel.setInformantName(sBirthDto.getInformantName());
        // private String informantAddress;
        sBirthModel.setInformantAddress(sBirthDto.getInformantAddress());
        // private String isOralInformant;
        sBirthModel.setIsOralInformant(sBirthDto.getIsOralInformant());
        // private Integer activityCode;
        sBirthModel.setActivityCode(sBirthDto.getActivityCode());
        // private String crNumber;
        sBirthModel.setCrNumber(CommonUtil.checkNullOrBlank(sBirthDto.getCrNumber()) ? "0" : sBirthDto.getCrNumber());
        // private Float lateFee;
        sBirthModel.setLateFee(sBirthDto.getLateFee());
        // private String fatherAdharNumber;
        sBirthModel.setFatherAdharNumber(sBirthDto.getFatherAdharNumber());
        // private String motherAdharNumber;
        sBirthModel.setMotherAdharNumber(sBirthDto.getMotherAdharNumber());
        // private String organizationCode;
        sBirthModel.setOrganizationCode(sBirthDto.getOrganizationCode());
        // private String status;
        sBirthModel.setStatus(sBirthDto.getStatus());
        // private String contactNumber;
        sBirthModel.setContactNumber(sBirthDto.getContactNumber());
        // private String pinCode;
        sBirthModel.setPinCode(sBirthDto.getPinCode());
        // private String motherNationality;
        sBirthModel.setMotherNationality(sBirthDto.getMotherNationality());
        // private String fatherNationality;
        sBirthModel.setFatherNationality(sBirthDto.getFatherNationality());

        sBirthModel.setRecordType(sBirthDto.getRecordType());
        sBirthModel.setSdmLetterNo(sBirthDto.getSdmLetterNo());
        sBirthModel.setApplicantName(sBirthDto.getApplicantName());
        sBirthModel.setApplicantAddress(sBirthDto.getApplicantAddress());
        //birthModel.setSdmLetterImage(birthDto.getSdmLetterImage());
        //sBirthModel.setRegistrationNumber(sBirthDto.getRegistrationNumber());
        sBirthModel.setRegistrationDatetime(sBirthDto.getRegistrationDate() == null ? LocalDateTime.now() : sBirthDto.getRegistrationDate().atStartOfDay());

        //sBirthModel.setRegistrationDatetime(sBirthDto.getRegistrationDate().atStartOfDay());
    }


    public static void copyChildDetails(ChildDetails childDetails, BirthModel birthModel) {

        birthModel.setName(childDetails.getChildName());
        birthModel.setChildWeight(childDetails.getChildWeight());
        birthModel.setGenderCode(childDetails.getGenderCode());
        birthModel.setEventDate(childDetails.getEventDate());
        birthModel.setRegistrationDatetime(childDetails.getRegistrationDate() == null ? LocalDateTime.now() : childDetails.getRegistrationDate().atStartOfDay());
    }

    public static void copySlaDetails(SlaDetailsModel slaDetailsModel, BirthModel birthModel) {

        birthModel.setApplicationNumber(slaDetailsModel.getApplNo());
        birthModel.setFatherName(slaDetailsModel.getFatherName());
        birthModel.setName(slaDetailsModel.getChildName());
        birthModel.setMotherAdharNumber(slaDetailsModel.getMotherAdharNumber());
        birthModel.setEventDate(slaDetailsModel.getDateOfEvent());
       // private String sex;
        birthModel.setGenderCode(slaDetailsModel.getGenderCode());
       // private String motherName;
        birthModel.setMotherName(slaDetailsModel.getMotherName());
        birthModel.setEventPlace(slaDetailsModel.getPlaceOfEvent());
       // private String applicantContact;

        birthModel.setRegistrationDatetime(slaDetailsModel.getRegistrationDatetime());

        //private String divisionCode;
        birthModel.setDivisionCode(slaDetailsModel.getDivisionCode());
       // private LocalDate applDate;
        //birthModel.date
       // private Float amount;
        // birthModel.setLateFee(slaDetailsModel.getAmount());
       // private Integer noOfCopies;

       // private String status; //UPLOADING/PENDING/APPROVED/REJECTED
       // private LocalDateTime createdAt;

        birthModel.setFatherName(slaDetailsModel.getFatherName());
        birthModel.setMotherName(slaDetailsModel.getMotherName());
        birthModel.setContactNumber(slaDetailsModel.getContactNumber());
        birthModel.setChildWeight(slaDetailsModel.getChildWeight());

        birthModel.setInformantName(slaDetailsModel.getInformantName());
        birthModel.setInformantAddress(slaDetailsModel.getInformantAddress());
        birthModel.setPermanentAddress(slaDetailsModel.getPermanentAddress());
        birthModel.setRegistrationNumber(slaDetailsModel.getRegistrationNumber());
        birthModel.setAddressAtBirth(slaDetailsModel.getAddressAtBirth());
       // birthModel.setRejectionRemark(slaDetailsModel.getRemarks());

        /*
        * Removed condition to save event place in Main table
        * */

       // if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getEventPlaceFlag())){
            birthModel.setEventPlaceFlag(slaDetailsModel.getEventPlaceFlag());
            birthModel.setEventPlace(slaDetailsModel.getEventPlace());
            birthModel.setDivisionCode(slaDetailsModel.getDivisionCode());
       // }
    }

    public static void copySlaDetailsToSbirth(SlaDetailsModel slaDetailsModel, SBirthModel birthModel) {

        birthModel.setApplicationNumber(slaDetailsModel.getApplNo());

        birthModel.setFatherName(slaDetailsModel.getFatherName());
        birthModel.setName(slaDetailsModel.getChildName());
        birthModel.setMotherAdharNumber(slaDetailsModel.getMotherAdharNumber());
        birthModel.setEventDate(slaDetailsModel.getDateOfEvent());
        birthModel.setGenderCode(slaDetailsModel.getGenderCode());
        birthModel.setMotherName(slaDetailsModel.getMotherName());
        birthModel.setEventPlace(slaDetailsModel.getPlaceOfEvent());
        birthModel.setDivisionCode(slaDetailsModel.getDivisionCode());
        //birthModel.setLateFee(slaDetailsModel.getAmount());
        birthModel.setFatherName(slaDetailsModel.getFatherName());
        birthModel.setMotherName(slaDetailsModel.getMotherName());
        birthModel.setContactNumber(slaDetailsModel.getContactNumber());
        birthModel.setChildWeight(slaDetailsModel.getChildWeight());
        birthModel.setInformantName(slaDetailsModel.getInformantName());
        birthModel.setInformantAddress(slaDetailsModel.getInformantAddress());
        birthModel.setPermanentAddress(slaDetailsModel.getPermanentAddress());
        birthModel.setRegistrationNumber(slaDetailsModel.getRegistrationNumber());


        /*Removed condition to save event place in main table after approved
        * */
        // if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getEventPlaceFlag())){


            birthModel.setEventPlaceFlag(slaDetailsModel.getEventPlaceFlag());
            birthModel.setEventPlace(slaDetailsModel.getEventPlace());
            birthModel.setDivisionCode(slaDetailsModel.getDivisionCode());

//}

        birthModel.setRegistrationDatetime(slaDetailsModel.getRegistrationDatetime());
    }

    public static void copySlaDetailsToDeathModel(SlaDetailsModel slaDetailsModel, DeathModel deathModel) {

        deathModel.setApplicationNumber(slaDetailsModel.getApplNo());
        deathModel.setFatherName(slaDetailsModel.getFatherName());
        deathModel.setName(slaDetailsModel.getDeceasedName());
        deathModel.setMotherAdharNumber(slaDetailsModel.getMotherAdharNumber());
        deathModel.setDeceasedAdharNumber(slaDetailsModel.getDeceaseAdharNumber());
        deathModel.setEventDate(slaDetailsModel.getDateOfEvent());
        deathModel.setGenderCode(slaDetailsModel.getGenderCode());
        deathModel.setMotherName(slaDetailsModel.getMotherName());
        // deathModel.setEventPlace(slaDetailsModel.getPlaceOfEvent());
        // deathModel.setDivisionCode(slaDetailsModel.getDivisionCode());
        // deathModel.setLateFee(slaDetailsModel.getAmount());
        deathModel.setMotherName(slaDetailsModel.getMotherName());
        deathModel.setFatherName(slaDetailsModel.getFatherName());
        deathModel.setHusbandWifeName(slaDetailsModel.getHusbandWifeName());
        deathModel.setHusbandWifeUID(slaDetailsModel.getHusbandWifeUID());
        deathModel.setGenderCode(slaDetailsModel.getGenderCode());
        deathModel.setContactNumber(slaDetailsModel.getContactNumber());
        deathModel.setInformantName(slaDetailsModel.getInformantName());
        deathModel.setInformantAddress(slaDetailsModel.getInformantAddress());
        deathModel.setInformantDoorNo(slaDetailsModel.getInformantDoorNo());
        deathModel.setPermanentAddress(slaDetailsModel.getPermanentAddress());
        deathModel.setRegistrationNumber(slaDetailsModel.getRegistrationNumber());
        deathModel.setDeceasedAge(slaDetailsModel.getDeceasedAge());
        deathModel.setAddressAtDeath(slaDetailsModel.getDeceaseAtAddress());

        /*Removed condition to save event place in main table after approv or reject the record*/

      //  if(!CommonUtil.checkNullOrBlank(slaDetailsModel.getEventPlaceFlag())){

            deathModel.setEventPlaceFlag(slaDetailsModel.getEventPlaceFlag());
            deathModel.setEventPlace(slaDetailsModel.getEventPlace());
            deathModel.setDivisionCode(slaDetailsModel.getDivisionCode());
      //  }

        deathModel.setRegistrationDatetime(slaDetailsModel.getRegistrationDatetime());
    }

    public static void copyUserDtoToUserModel(UserDto userDto, UserModel userModel) {

        userModel.setEmail(userDto.getEmail());
        //userModel.setPassword(userDto.getPassword());
        userModel.setStatus(userDto.getStatus());
        userModel.setOrganizationId(userDto.getOrganizationId());
        userModel.setFirstName(userDto.getFirstName());
        userModel.setLastName(userDto.getLastName());
        userModel.setEmployeeCode(userDto.getEmployeeCode());
        userModel.setDesignation(userDto.getDesignation());
        userModel.setContactNo(userDto.getContactNo());
        userModel.setValidityStart(userDto.getValidityStart());
        userModel.setValidityEnd(userDto.getValidityEnd());

    }


    public static void copyCurrentUserDetailsToUserModel(UserModel currentUser, UserModel userModel) {

        userModel.setRoles(currentUser.getRoles());
        userModel.setUserName(currentUser.getUserName());
        userModel.setEmail(currentUser.getEmail());
        userModel.setStatus(currentUser.getStatus());
        userModel.setOrganizationId(currentUser.getOrganizationId());
        userModel.setFirstName(currentUser.getFirstName());
        userModel.setLastName(currentUser.getLastName());
        userModel.setEmployeeCode(currentUser.getEmployeeCode());
        userModel.setDesignation(currentUser.getDesignation());
        userModel.setContactNo(currentUser.getContactNo());
        userModel.setValidityStart(currentUser.getValidityStart());
        userModel.setValidityEnd(currentUser.getValidityEnd());
        userModel.setCreatedBy(currentUser.getCreatedBy());
        userModel.setCreatedAt(currentUser.getCreatedAt());
    }


    public static void copyBirthCorrectionToSlaDetailsModel(BirthCorrectionDto birthCorrectionDto, SlaDetailsModel slaDetailsModel) {
        slaDetailsModel.setChildName(birthCorrectionDto.getChildName());
        slaDetailsModel.setChildWeight(birthCorrectionDto.getChildWeight());
        slaDetailsModel.setApplicantName(birthCorrectionDto.getApplicantName());
        slaDetailsModel.setApplicantContact(birthCorrectionDto.getApplicantContact());
        slaDetailsModel.setApplicantEmailId(birthCorrectionDto.getApplicantEmailId());
        slaDetailsModel.setApplicantAddress(birthCorrectionDto.getApplicantAddress());
        slaDetailsModel.setApplNo(birthCorrectionDto.getApplNo());
        slaDetailsModel.setTransactionType(birthCorrectionDto.getTransactionType());
        slaDetailsModel.setRecordType(birthCorrectionDto.getRecordType());
        slaDetailsModel.setRegistrationDatetime(birthCorrectionDto.getRegistrationDatetime());
        slaDetailsModel.setGenderCode(birthCorrectionDto.getGenderCode());
        slaDetailsModel.setFatherAdharNumber(birthCorrectionDto.getFatherAdharNumber());
        slaDetailsModel.setMotherAdharNumber(birthCorrectionDto.getMotherAdharNumber());
        slaDetailsModel.setFatherName(birthCorrectionDto.getFatherName());
        slaDetailsModel.setMotherName(birthCorrectionDto.getMotherName());
        slaDetailsModel.setPermanentAddress(birthCorrectionDto.getPermanentAddress());
        slaDetailsModel.setAddressAtBirth(birthCorrectionDto.getAddressAtBirth());
        slaDetailsModel.setInformantName(birthCorrectionDto.getInformantName());
        slaDetailsModel.setInformantAddress(birthCorrectionDto.getInformantAddress());
        slaDetailsModel.setContactNumber(birthCorrectionDto.getContactNumber());
        slaDetailsModel.setDateOfEvent(birthCorrectionDto.getDateOfEvent());
        slaDetailsModel.setVerfiedUIDMobile(birthCorrectionDto.getVerfiedUIDMobile());
        slaDetailsModel.setVerifiedUIDName(birthCorrectionDto.getVerifiedUIDName());
        slaDetailsModel.setVerifiedUIDNo(birthCorrectionDto.getVerifiedUIDNo());
        slaDetailsModel.setUploadedFile(birthCorrectionDto.getUploadedFile());
        slaDetailsModel.setCorrectionFields(birthCorrectionDto.getCorrectionFields());
    }
}
