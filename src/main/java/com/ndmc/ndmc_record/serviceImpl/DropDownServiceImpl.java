package com.ndmc.ndmc_record.serviceImpl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.exception.NotFoundException;
import com.ndmc.ndmc_record.service.DropDownService;

@Service
@Transactional
public class DropDownServiceImpl implements DropDownService {

	private final Logger logger = LoggerFactory.getLogger(DropDownServiceImpl.class);

	@Autowired
	private GenderDropDownRepository genderDropDownRepository;

	@Autowired
	private StateDropDownRepository stateDropDownRepository;

	@Autowired
	private OccupationRepository occupationRepository;

	@Autowired
	private ReligionsRepository religionsRepository;

	@Autowired
	private LiteracyRepository literacyRepository;

	@Autowired
	private DeliveryMethodRepository deliveryMethodRepository;

	@Autowired
	private DeliveryAttentionsRepository deliveryAttentionsRepository;

	@Autowired
	private MedicalAttentionsRepository medicalAttentionsRepository;

	@Autowired
	private ActivityCodesRepository activityCodesRepository;

	@Autowired
	private MaritalStatusRepository maritalStatusRepository;

	@Autowired
	private LocationsRepository locationsRepository;

	@Autowired
	private CauseofDeathRepository causeofDeathRepository;

	@Autowired
	OrganizationRepository organizationRepository;

	@Override
	@Cacheable("Gender")
	public ResponseEntity<?> getAllGender() {
		return new ResponseEntity<>(genderDropDownRepository.findByDeleteFlag("N"), HttpStatus.OK);
	}

	@Override
	@Cacheable("PinCode")
	public ResponseEntity<?> getByPinCode(String pincode) {

		List<StateMasterModel> pincodeList = stateDropDownRepository.findByPincode(pincode);
		if (pincodeList.isEmpty())
			throw new NotFoundException();

		return new ResponseEntity<>(pincodeList, HttpStatus.OK);
	}


	@Override
	@Cacheable("Occupation")
	public ResponseEntity<?> getOccupation() {
		List<OccupationsModel> list = occupationRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();

		list.sort(Comparator.comparing(OccupationsModel::getOccupationDesc).thenComparing(OccupationsModel::getOccupationDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("Literacy")
	public ResponseEntity<?> getLiteracy() {
		List<LiteracyModel> list = literacyRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(LiteracyModel::getLiteracyType).thenComparing(LiteracyModel::getLiteracyType));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("Religions")
	public ResponseEntity<?> getReligions() {
		List<ReligionsModel> list = religionsRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(ReligionsModel::getReligionName).thenComparing(ReligionsModel::getReligionName));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("DeliveryMethod")
	public ResponseEntity<?> getDeliveryMethod() {
		List<DeliveryMethodModel> list = deliveryMethodRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(DeliveryMethodModel::getDeliveryDesc).thenComparing(DeliveryMethodModel::getDeliveryDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("DeliveryAttentions")
	public ResponseEntity<?> getDeliveryAttentions() {
		List<DeliveryAttentionsModel> list = deliveryAttentionsRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(DeliveryAttentionsModel::getDeliveryAttentionsDesc).thenComparing(DeliveryAttentionsModel::getDeliveryAttentionsDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("MedicalAttentions")
	public ResponseEntity<?> getMedicalAttentions() {
		List<MedicalAttentionsModel> list = medicalAttentionsRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(MedicalAttentionsModel::getMedicalAttentionsDesc).thenComparing(MedicalAttentionsModel::getMedicalAttentionsDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("ActivityCodes")
	public ResponseEntity<?> getActivityCodes() {
		List<ActivityCodesModel> list = activityCodesRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(ActivityCodesModel::getActivityDesc).thenComparing(ActivityCodesModel::getActivityDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("MaritalStatus")
	public ResponseEntity<?> getMaritalStatus() {
		List<MaritalStatusModel> list = maritalStatusRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(MaritalStatusModel::getMaritalStatusDesc).thenComparing(MaritalStatusModel::getMaritalStatusDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("Locations")
	public ResponseEntity<?> getLocations() {
		List<LocationsModel> list = locationsRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(LocationsModel::getLocationDesc).thenComparing(LocationsModel::getLocationDesc));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("CauseofDeath")
	public ResponseEntity<?> getCauseofDeath(String causeDesc) {
		List<CauseofDeathModel> list = causeofDeathRepository.findByDeleteFlagAndCauseDescStartingWithIgnoreCase("N", causeDesc);
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(CauseofDeathModel::getCauseCode).thenComparing(CauseofDeathModel::getCauseCode));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("AllCauseofDeath")
	public ResponseEntity<?> getAllCauseofDeath() {
		List<CauseofDeathModel> list = causeofDeathRepository.findByDeleteFlag("N");
		if (list.isEmpty())
			throw new NotFoundException();
		list.sort(Comparator.comparing(CauseofDeathModel::getCauseCode).thenComparing(CauseofDeathModel::getCauseCode));
		return new ResponseEntity<>(list, HttpStatus.OK);
	}

	@Override
	@Cacheable("Hospitals")
	public ResponseEntity<?> getHospitals() {

		List<OrganizationModel> organizationModelList = organizationRepository.findByOrganizationTypeAndStatus(Constants.HOSPITAL, Constants.RECORD_STATUS_ACTIVE);
		if (organizationModelList.isEmpty())
			throw new NotFoundException();

		organizationModelList.sort(Comparator.comparing(OrganizationModel::getOrganizationName).thenComparing(OrganizationModel::getOrganizationName));
		return new ResponseEntity<>(organizationModelList, HttpStatus.OK);
	}

	@Override
	@Cacheable("Cfcs")
	public ResponseEntity<?> getCfcs() {
		List<OrganizationModel> organizationModelList = organizationRepository.findByOrganizationTypeAndStatus(Constants.CFC, Constants.RECORD_STATUS_ACTIVE);
		if (organizationModelList.isEmpty())
			throw new NotFoundException();
		organizationModelList.sort(Comparator.comparing(OrganizationModel::getOrganizationName).thenComparing(OrganizationModel::getOrganizationName));

		return new ResponseEntity<>(organizationModelList, HttpStatus.OK);

	}

	@Override
	@Cacheable("DetailsByPincode")
	public ResponseEntity<?> getDetailsByPincodeMaster(String stateName, String area, String districtName) {

		if (stateName != null && area != null && districtName != null) {
			List<String> distinctState = stateDropDownRepository
					.findByStateNameAndAreaAndDistrictNameAndDeleteFlagAndPincodeNotNull(stateName, area, districtName,"N").stream()
					.map(StateMasterModel::getPincode).sorted().distinct().collect(Collectors.toList());
			return new ResponseEntity<>(distinctState, HttpStatus.OK);

		} else {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg("Please select correct fields");
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}

	}
	@Override
	@Cacheable("State")
	public ResponseEntity<?> getDetailsByStateMaster() {
			List<String> distinctState = stateDropDownRepository.findByDeleteFlagAndStateNameNotNull("N").stream()
					.map(StateMasterModel::getStateName).sorted().distinct().collect(Collectors.toList());
			return new ResponseEntity<>(distinctState, HttpStatus.OK);	
	}

	@Override
	@Cacheable("StateToArea")
	public ResponseEntity<?> getDetailsByStateToAreaMaster(String stateName) {
		if (stateName != null ) {
			List<String> distinctState = stateDropDownRepository.findByStateNameAndDeleteFlagAndAreaNotNull(stateName,"N").stream()
					.map(StateMasterModel::getArea).sorted().distinct().collect(Collectors.toList());
			return new ResponseEntity<>(distinctState, HttpStatus.OK);
		}else {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg("Please select correct fields");
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}
	}
	@Override
	@Cacheable("StateToDistrict")
	public ResponseEntity<?> getDetailsByStateToDistrictMaster(String stateName) {
		if (stateName != null ) {
			List<String> distinctState = stateDropDownRepository.findByStateNameAndDeleteFlagAndDistrictNameNotNull(stateName,"N").stream()
					.map(StateMasterModel::getDistrictName).sorted().distinct().collect(Collectors.toList());
			if (distinctState.isEmpty())
				throw new NotFoundException();
			return new ResponseEntity<>(distinctState, HttpStatus.OK);
		}else {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg("Please select correct fields");
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}
	}


	@Override
	@Cacheable("AreaToDistrict")
	public ResponseEntity<?> getDetailsByAreaToDistrictMaster(String stateName, String area) {
		if (stateName != null && area != null) {
			List<String> distinctState = stateDropDownRepository
					.findByStateNameAndAreaAndDeleteFlagAndDistrictNameNotNull(stateName, area, "N").stream()
					.map(StateMasterModel::getDistrictName).sorted().distinct().collect(Collectors.toList());
			if (distinctState.isEmpty())
				throw new NotFoundException();
			return new ResponseEntity<>(distinctState, HttpStatus.OK);
		}else {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg("Please select correct fields");
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Cacheable("DistrictToArea")
	public ResponseEntity<?> getDetailsByDistrictToAreaMaster(String stateName, String districtName) {
		if (stateName != null && districtName != null) {
			List<String> distinctState = stateDropDownRepository
					.findByStateNameAndDistrictNameAndDeleteFlagAndAreaNotNull(stateName, districtName, "N").stream()
					.map(StateMasterModel::getArea).sorted().distinct().collect(Collectors.toList());
			if (distinctState.isEmpty())
				throw new NotFoundException();
			return new ResponseEntity<>(distinctState, HttpStatus.OK);
		}else {
			ApiResponse apiResponse = new ApiResponse();
			apiResponse.setMsg("Please select correct fields");
			apiResponse.setStatus(HttpStatus.BAD_REQUEST);
			return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	@Cacheable("Organizations")
	public ResponseEntity<?> getOrganizations() {
		List<OrganizationModel> organizationModelList = organizationRepository.findByStatus(Constants.RECORD_STATUS_ACTIVE);
		if (organizationModelList.isEmpty())
			throw new NotFoundException();

		return new ResponseEntity<>(organizationModelList, HttpStatus.OK);
	}
}
