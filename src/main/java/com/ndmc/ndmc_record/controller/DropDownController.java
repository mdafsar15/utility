package com.ndmc.ndmc_record.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.service.DropDownService;

@RestController
@RequestMapping("api/v1/dropdown")
//@CrossOrigin(origins = "*")
public class DropDownController {
	private final Logger logger = LoggerFactory.getLogger(DropDownController.class);

	@Autowired
	private DropDownService dropDownService;

	@GetMapping("/get/details/by/{pinCode}")
	public ResponseEntity<?> getByPinCode(@PathVariable String pinCode) {
		logger.info("Calling for service getByPinCode{}");
		return dropDownService.getByPinCode(pinCode);
	}

	@GetMapping("/get/detail/by/{type}/master")
	public ResponseEntity<?> getDetailByMaster(
			@PathVariable String type,
			@RequestParam (value = "stateName", required = false) String stateName,
			@RequestParam(value = "area", required = false) String area,
			@RequestParam(value = "districtName", required = false) String districtName) {
		
		logger.info("Calling for service getDetailByMaster{}  " + type);
		switch (type) {
		case "state":
			if (stateName == null && area == null && districtName == null)
				return dropDownService.getDetailsByStateMaster();
			else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please don't pass stateName, area and districtName parameter");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
			}
		case "area":
		case "state-to-area":
			if (stateName != null && area == null && districtName == null)
				return dropDownService.getDetailsByStateToAreaMaster(stateName);
			else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please pass in stateName and don't pass area and districtName parameter");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
			}
		case "district":
		case "area-to-district":
			if (stateName != null && area != null && districtName == null)
				return dropDownService.getDetailsByAreaToDistrictMaster(stateName, area);
			else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please pass in stateName and area and don't pass districtName parameter");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
			}
		case "state-to-district":
			if (stateName != null && area == null && districtName == null)
				return dropDownService.getDetailsByStateToDistrictMaster(stateName);
			else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please pass in stateName and don't pass area and districtName parameter");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
			}
		case "district-to-area":
			if (stateName != null && area == null && districtName != null)
				return dropDownService.getDetailsByDistrictToAreaMaster(stateName, districtName);
			else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please pass in stateName and area and don't pass districtName parameter");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
			}
		case "pincode":
			if (stateName != null && area != null && districtName != null)
				return dropDownService.getDetailsByPincodeMaster(stateName, area, districtName);
			else {
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please pass in stateName and area and don't pass districtName parameter");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
			}
		default:
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg("Please entered correct type");
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}
		
	}

	@GetMapping("/get/master/{type}")
	public ResponseEntity<?> getMaster(@PathVariable String type) {
		logger.info("Calling for service getMaster{}  " + type);
		switch (type) {
			case "gender":
				return dropDownService.getAllGender();
			case "occupation":
				return dropDownService.getOccupation();
			case "literacy":
				return dropDownService.getLiteracy();
			case "religions":
				return dropDownService.getReligions();
			case "deliveryMethod":
				return dropDownService.getDeliveryMethod();
			case "deliveryAttentions":
				return dropDownService.getDeliveryAttentions();
			case "medicalAttentions":
				return dropDownService.getMedicalAttentions();
			case "activityCodes":
				return dropDownService.getActivityCodes();
			case "maritalStatus":
				return dropDownService.getMaritalStatus();
			case "locations":
				return dropDownService.getLocations();
			default:
				ApiResponse apiResponse = new ApiResponse();
				apiResponse.setMsg("Please entered correct type");
				apiResponse.setStatus(HttpStatus.BAD_REQUEST);
				return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/get/detail/by/causeofDeath/master")
	public ResponseEntity<?> getCauseofDeath(@RequestParam(value = "causeDesc", defaultValue = "") String causeDesc) {
		logger.info("Calling for service getDetailsByStateMaster{}");
		return dropDownService.getCauseofDeath(causeDesc);
	}

	@GetMapping("/get/detail/by/allCauseofDeath/master")
	public ResponseEntity<?> getAllCauseofDeath() {
		logger.info("Calling for service getDetailsByStateMaster{}");
		return dropDownService.getAllCauseofDeath();
	}

	@GetMapping("/get/{orgType}")
	public ResponseEntity<?> getHospitalList(@PathVariable(name = "orgType") String orgType) {
		logger.info("Calling for service " + orgType);
		if(orgType.equalsIgnoreCase(Constants.ORGANIZATION_TYPE_HOSPITALS)) {
			return dropDownService.getHospitals();
		} else if(orgType.equalsIgnoreCase(Constants.ORGANIZATION_TYPE_CFCS)) {
			return dropDownService.getCfcs();
		} else if(orgType.equalsIgnoreCase(Constants.ORGANIZATION_TYPE_ORGANIZATIONS)) {
			return dropDownService.getOrganizations();
		} else {
			return new ResponseEntity<>("Invalid URL", HttpStatus.BAD_REQUEST);
		}
		
	}

	// @GetMapping("/get/cfcs")
	// public ResponseEntity<?> getCFCList() {
	// 	logger.info("Calling for service getCfcs{}");
	// 	return dropDownService.getCfcs();
	// }

}
