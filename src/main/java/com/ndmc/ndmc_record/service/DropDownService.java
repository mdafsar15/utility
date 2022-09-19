package com.ndmc.ndmc_record.service;

import org.springframework.http.ResponseEntity;

public interface DropDownService {

	ResponseEntity<?> getAllGender();

	ResponseEntity<?> getByPinCode(String pinCode);
	
	ResponseEntity<?> getDetailsByStateMaster();
	
	ResponseEntity<?> getDetailsByStateToAreaMaster(String stateName);

	ResponseEntity<?> getDetailsByAreaToDistrictMaster(String stateName, String area);

	ResponseEntity<?> getDetailsByPincodeMaster(String stateName, String area, String districtName);

	ResponseEntity<?> getOccupation();

	ResponseEntity<?> getLiteracy();

	ResponseEntity<?> getReligions();

	ResponseEntity<?> getDeliveryMethod();

	ResponseEntity<?> getDeliveryAttentions();

	ResponseEntity<?> getMedicalAttentions();

	ResponseEntity<?> getActivityCodes();

	ResponseEntity<?> getMaritalStatus();

	ResponseEntity<?> getLocations();

	ResponseEntity<?> getCauseofDeath(String causeDesc);

	ResponseEntity<?> getAllCauseofDeath();

    ResponseEntity<?> getHospitals();

    ResponseEntity<?> getCfcs();

    ResponseEntity<?> getOrganizations();

	ResponseEntity<?> getDetailsByStateToDistrictMaster(String stateName);

	ResponseEntity<?> getDetailsByDistrictToAreaMaster(String stateName, String districtName);
}
