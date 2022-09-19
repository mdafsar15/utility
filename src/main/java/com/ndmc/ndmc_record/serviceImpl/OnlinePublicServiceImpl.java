package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.*;
import com.ndmc.ndmc_record.service.AuthService;
import com.ndmc.ndmc_record.service.OnlinePublicService;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.CustomBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OnlinePublicServiceImpl implements OnlinePublicService {

    private final Logger logger = LoggerFactory.getLogger(OnlinePublicServiceImpl.class);

    @Autowired
    BirthRepository birthRepository;

    @Autowired
    SBirthRepository sBirthRepository;

    @Autowired
    DeathRepository deathRepository;

    @Autowired
    CitizenBirthRepository citizenBirthRepository;

    @Autowired
    CitizenDeathRepository citizenDeathRepository;

    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    CitizenSBirthRepository citizenSBirthRepository;

    @Autowired
    AuthServiceImpl authService;

    @Autowired
    AuthRepository authRepository;


    @Override
    public ApiResponse onlineSearchBirthEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws DateTimeParseException,Exception{
        logger.info("Calling onlineSearchBirthEnquiry ====== " + LocalDateTime.now());

        ApiResponse apiResponse = new ApiResponse();
        printRequestDto.setRecordType(Constants.RECORD_TYPE_BIRTH);
        List<BirthModel> birth = null ;
        birth = getBirthListForReport(printRequestDto);
        logger.info("====== PrintRequestDto======"+printRequestDto);
       // logger.info("====== Response======"+birth);
        if(birth.isEmpty()){
            apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
            apiResponse.setStatus(HttpStatus.NOT_FOUND);
            return apiResponse;
        }
        // logger.info("===onlineSearchBirthEnquiry=== birth======"+birth.toString());
        apiResponse.setMsg(Constants.GET_RECORDS_MESSAGE);
        apiResponse.setStatus(HttpStatus.OK);
        apiResponse.setData(birth);
        return apiResponse;
    }

    @Override
    public ApiResponse onlineSearchInclusionEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request)throws DateTimeParseException,Exception {
        logger.info("Calling onlineSearchInclusionEnquiry ====== " + LocalDateTime.now());

        ApiResponse apiResponse = new ApiResponse();
        List<BirthModel> birth = new ArrayList<>();
        printRequestDto.setRecordType(Constants.RECORD_NAME_INCLUSION);
        List<BirthModel> birthList = getBirthListForReport(printRequestDto);
       // logger.info("===== SEARCH RESPONSE 1 ==" + birthList);
        if(birthList != null &&  !birthList.isEmpty()) {
             birth = birthList.stream().filter(r -> (r.getName() == null || r.getName().isEmpty())).collect(Collectors.toList());
           //  logger.info("===== SEARCH RESPONSE ==" + birth);
            if (birth != null && !birth.isEmpty()) {

                apiResponse.setMsg(Constants.GET_RECORDS_MESSAGE);
                apiResponse.setStatus(HttpStatus.OK);
                apiResponse.setData(birth);

            } else {
                apiResponse.setMsg(Constants.NAME_ALREADY_INCLUDED_MESSAGE);
                apiResponse.setStatus(HttpStatus.BAD_REQUEST);
            }
        }else{

            apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
            apiResponse.setStatus(HttpStatus.NOT_FOUND);
        }



        return apiResponse;
    }

    @Override
    public ApiResponse onlineSearchStillBirthEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws DateTimeParseException,Exception {
        logger.info("Calling onlineSearchStillBirthEnquiry ====== " + LocalDateTime.now());

        ApiResponse apiResponse = new ApiResponse();
        printRequestDto.setRecordType(Constants.RECORD_TYPE_BIRTH);
        List<SBirthModel> sBirth = null ;

        sBirth = getStillBirthListForReport(printRequestDto);

        if(!sBirth.isEmpty()){
            apiResponse.setMsg(Constants.GET_RECORDS_MESSAGE);
            apiResponse.setStatus(HttpStatus.OK);
            apiResponse.setData(sBirth);
        }else{
            apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
            apiResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return apiResponse;
    }
    @Override
    public ApiResponse onlineSearchDeathEnquiry(OnlinePrintRequestDto printRequestDto, HttpServletRequest request) throws DateTimeParseException,Exception {
        logger.info("Calling onlineSearchDeathEnquiry ====== " + LocalDateTime.now());

        ApiResponse apiResponse = new ApiResponse();
        printRequestDto.setRecordType(Constants.RECORD_TYPE_BIRTH);
        List<DeathModel> death = null ;
        death = getDeathListForReport(printRequestDto) ;

        if(!death.isEmpty()) {
            apiResponse.setMsg(Constants.GET_RECORDS_MESSAGE);
            apiResponse.setStatus(HttpStatus.OK);
            apiResponse.setData(death);
        }else{
            apiResponse.setMsg(Constants.RECORD_NOT_FOUND);
            apiResponse.setStatus(HttpStatus.NOT_FOUND);
        }
        return apiResponse;
    }

    @Override
    public ApiResponse createLinkForSelfRegister(SelfRegistrationLinkDto selfRegistrationLinkDto, HttpServletRequest request) {

        ApiResponse response = new ApiResponse();
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = "";
        String password = "";
        if (principal instanceof UserDetails) {
             username = ((UserDetails)principal).getUsername();
             password = ((UserDetails)principal).getPassword();
             response.setMsg(password);
        } else {
             username = principal.toString();
        }
        return response;
    }

    @Override
    public ApiResponse reviewRecordsByApplNo(String recordType, String applNo) {

        ApiResponse response = new ApiResponse();
        if(recordType.equalsIgnoreCase(Constants.RECORD_TYPE_BIRTH)){
           Optional<BirthModel> birthModelOp = birthRepository.findByApplicationNumber(applNo);
           if(birthModelOp.isPresent()){
               BirthModel birthModel = birthModelOp.get();
               if(birthModel.getFatherOccupation() != null)
               birthModel.setFatherOccupation(CommonUtil.getOccupationValueById(birthModel.getFatherOccupation()));
               if(birthModel.getMotherOccupation() != null)
               birthModel.setMotherOccupation(CommonUtil.getOccupationValueById(birthModel.getMotherOccupation()));
               if(birthModel.getMotherReligion() != null)
               birthModel.setMotherReligion(CommonUtil.getReligionValueById(birthModel.getMotherReligion()));
               if(birthModel.getFatherReligion() != null)
               birthModel.setFatherReligion(CommonUtil.getReligionValueById(birthModel.getFatherReligion()));
               if(birthModel.getFatherLiteracy() != null)
               birthModel.setFatherLiteracy(CommonUtil.getLiteracyValueById(birthModel.getFatherLiteracy()));
               if(birthModel.getMotherLiteracy() != null)
               birthModel.setMotherLiteracy(CommonUtil.getLiteracyValueById(birthModel.getMotherLiteracy()));
               response.setStatus(HttpStatus.OK);
               response.setData(birthModel);
           }else{
               response.setStatus(HttpStatus.BAD_REQUEST);
               response.setMsg(Constants.RECORD_NOT_FOUND);
           }
        }

       else if(recordType.equalsIgnoreCase(Constants.RECORD_TYPE_DEATH)){
            Optional<DeathModel> deathModelOp = deathRepository.findByApplicationNumber(applNo);
            if(deathModelOp.isPresent()){
                DeathModel deathModel = deathModelOp.get();
                deathModel.setDeceasedOccupation(CommonUtil.getOccupationValueById(deathModel.getOccupationCode()));
                if(deathModel.getReligionCode() != null)
                deathModel.setDeceasedReligion(CommonUtil.getReligionValueById(deathModel.getReligionCode()));
                if(deathModel.getMedicalAttentionCode() != null)
                deathModel.setMedicalAttentionCodeDesc(CommonUtil.getMedicalAttentionByCode(deathModel.getMedicalAttentionCode().toString()));
               if(deathModel.getMaritalStatusCode() !=null)
                deathModel.setMaritalStatusCodeDesc(CommonUtil.getMaritalStatusById(deathModel.getMaritalStatusCode().toString()));
               if(deathModel.getEducationCode() != null)
               deathModel.setEducationCode(CommonUtil.getLiteracyValueById(deathModel.getEducationCode()));

                response.setStatus(HttpStatus.OK);
                response.setData(deathModel);
            }else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.RECORD_NOT_FOUND);
            }
        }
        else if(recordType.equalsIgnoreCase(Constants.RECORD_TYPE_STILLBIRTH)){
            Optional<SBirthModel> sbirthModelOp = sBirthRepository.findByApplicationNumber(applNo);
            if(sbirthModelOp.isPresent()){
                SBirthModel sbirthModel = sbirthModelOp.get();
                if(sbirthModel.getFatherOccupation() != null)
                sbirthModel.setFatherOccupation(CommonUtil.getOccupationValueById(sbirthModel.getFatherOccupation()));
                if(sbirthModel.getMotherOccupation() != null)
                sbirthModel.setMotherOccupation(CommonUtil.getOccupationValueById(sbirthModel.getMotherOccupation()));
                if(sbirthModel.getMotherReligion() != null)
                sbirthModel.setMotherReligion(CommonUtil.getReligionValueById(sbirthModel.getMotherReligion()));
                if(sbirthModel.getFatherReligion() != null)
                sbirthModel.setFatherReligion(CommonUtil.getReligionValueById(sbirthModel.getFatherReligion()));
                if(sbirthModel.getFatherLiteracy() != null)
                sbirthModel.setFatherLiteracy(CommonUtil.getLiteracyValueById(sbirthModel.getFatherLiteracy()));
                if(sbirthModel.getMotherLiteracy() != null)
                sbirthModel.setMotherLiteracy(CommonUtil.getLiteracyValueById(sbirthModel.getMotherLiteracy()));
                response.setStatus(HttpStatus.OK);
                response.setData(sbirthModel);
            }else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.RECORD_NOT_FOUND);
            }
        }

        return response;
    }

    @Override
    public ApiResponse citizenBirthRegisterEvent(CitizenBirthDto citizenBirthDto, String orgId) {
        ApiResponse response = new ApiResponse();
        byte[] decodedBytes = Base64.getDecoder().decode(orgId);
        String decodedOrgId = new String(decodedBytes);

        List<ChildDetails> responseList = new ArrayList<ChildDetails>();




        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(decodedOrgId));
        if(organizationModelOp.isPresent()){
            CitizenBirthModel citizenBirthModel = new CitizenBirthModel();
            BeanUtils.copyProperties(citizenBirthDto, citizenBirthModel);
            for (ChildDetails childDetails : citizenBirthDto.getChildDetails()) {
                BeanUtils.copyProperties(childDetails, citizenBirthModel);
            }

            String uniqueString = CommonUtil.getAlphaNumericString(10, citizenBirthModel.getOrganizationCode());

            citizenBirthModel.setCreatedAt(LocalDateTime.now());
            OrganizationModel organizationModel = organizationModelOp.get();
            citizenBirthModel.setOrganizationId(organizationModel.getOrganizationId());
            citizenBirthModel.setOrganizationCode(organizationModel.getOrganisationCode());
            citizenBirthModel.setStatus(Constants.RECORD_STATUS_PENDING);
            citizenBirthModel.setType(Constants.CITIZEN_BIRTH);

            citizenBirthModel.setTrackingNo(uniqueString);
            citizenBirthModel.setRegistrationDatetime(LocalDateTime.now());


            citizenBirthModel = citizenBirthRepository.save(citizenBirthModel);
            if(citizenBirthModel != null){
                response.setStatus(HttpStatus.OK);
                response.setMsg(Constants.BIRTH_SUCCESS_MESSAGE);
                response.setData(citizenBirthModel);
                CommonUtil commonUtil = new CommonUtil();
                try{
                    commonUtil.sendTextMessage(citizenBirthModel.getMotherName(), citizenBirthModel.getContactNumber(),
                            "", Constants.RECORD_TYPE_BIRTH, Constants.CITIZEN_TRACKING_MSG,
                            "", "", "", citizenBirthModel.getOrganizationCode(), citizenBirthModel.getTrackingNo());
                }catch (Exception e){
                    logger.info("===SMS EXCEPTION =="+e);
                }
            }else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.INTERNAL_SERVER_ERROR);
            }

        }
        else{
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setMsg(Constants.RECORD_NOT_FOUND);
        }

        return response;
    }

    @Override
    public ApiResponse citizenDeathRegisterEvent(DeathDto deathDto, String orgId) {
        ApiResponse response = new ApiResponse();
        byte[] decodedBytes = Base64.getDecoder().decode(orgId);
        String decodedOrgId = new String(decodedBytes);

        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(decodedOrgId));
        if(organizationModelOp.isPresent()){
            CitizenDeathModel citizenDeathModel = new CitizenDeathModel();
            BeanUtils.copyProperties(deathDto, citizenDeathModel);
            String uniqueString = CommonUtil.getAlphaNumericString(10, citizenDeathModel.getOrganizationCode());

            citizenDeathModel.setCreatedAt(LocalDateTime.now());
            OrganizationModel organizationModel = organizationModelOp.get();
            citizenDeathModel.setOrganizationId(organizationModel.getOrganizationId());
            citizenDeathModel.setOrganizationCode(organizationModel.getOrganisationCode());
            citizenDeathModel.setStatus(Constants.RECORD_STATUS_PENDING);
            citizenDeathModel.setType(Constants.CITIZEN_DEATH);

            citizenDeathModel.setTrackingNo(uniqueString);
            citizenDeathModel.setRegistrationDatetime(LocalDateTime.now());

            citizenDeathModel = citizenDeathRepository.save(citizenDeathModel);
            if(citizenDeathModel != null){
                response.setStatus(HttpStatus.OK);
                response.setMsg(Constants.DEATH_SUCCESS_MESSAGE);
                response.setData(citizenDeathModel);
                CommonUtil commonUtil = new CommonUtil();
                try{
                    commonUtil.sendTextMessage(citizenDeathModel.getMotherName(), citizenDeathModel.getContactNumber(), "", Constants.RECORD_TYPE_DEATH,
                            Constants.CITIZEN_TRACKING_MSG, "", "", "", citizenDeathModel.getOrganizationCode(), citizenDeathModel.getTrackingNo());
                }catch (Exception e){
                    logger.info("===SMS EXCEPTION =="+e);
                }
            }else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.INTERNAL_SERVER_ERROR);
            }

        }
        else{
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setMsg(Constants.RECORD_NOT_FOUND);
        }

        return response;
    }

    @Override
    public ApiResponse citizenSBirthRegisterEvent(SBirthDto citizenBirthDto, String orgId) {
        ApiResponse response = new ApiResponse();
        byte[] decodedBytes = Base64.getDecoder().decode(orgId);
        String decodedOrgId = new String(decodedBytes);

        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(decodedOrgId));
        if(organizationModelOp.isPresent()){
            CitizenSBirthModel citizenBirthModel = new CitizenSBirthModel();
            BeanUtils.copyProperties(citizenBirthDto, citizenBirthModel);

            String uniqueString = CommonUtil.getAlphaNumericString(10, citizenBirthModel.getOrganizationCode());

            citizenBirthModel.setCreatedAt(LocalDateTime.now());
            OrganizationModel organizationModel = organizationModelOp.get();
            citizenBirthModel.setOrganizationId(organizationModel.getOrganizationId());
            citizenBirthModel.setOrganizationCode(organizationModel.getOrganisationCode());
            citizenBirthModel.setStatus(Constants.RECORD_STATUS_PENDING);
            citizenBirthModel.setType(Constants.CITIZEN_SBIRTH);
            citizenBirthModel.setTrackingNo(uniqueString);
            citizenBirthModel.setRegistrationDatetime(LocalDateTime.now());
            citizenBirthModel = citizenSBirthRepository.save(citizenBirthModel);
            if(citizenBirthModel != null){
                response.setStatus(HttpStatus.OK);
                response.setMsg(Constants.SBIRTH_SUCCESS_MESSAGE);
                response.setData(citizenBirthModel);
                CommonUtil commonUtil = new CommonUtil();
                try{
                    commonUtil.sendTextMessage(citizenBirthModel.getMotherName(), citizenBirthModel.getContactNumber(),
                            "", Constants.RECORD_TYPE_SBIRTH, Constants.CITIZEN_TRACKING_MSG, "",
                            "", "", citizenBirthModel.getOrganizationCode(), citizenBirthModel.getTrackingNo());
                }catch (Exception e){
                    logger.info("===SMS EXCEPTION =="+e);
                }
            }else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.INTERNAL_SERVER_ERROR);
            }

        }
        else{
            response.setStatus(HttpStatus.NOT_FOUND);
            response.setMsg(Constants.RECORD_NOT_FOUND);
        }

        return response;
    }

    @Override
    public ApiResponse getCitizenRecordsByType(String type, HttpServletRequest request) {
        ApiResponse response = new ApiResponse();
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> userModelOp = authRepository.findById(Long.parseLong(userId));
        if(userModelOp.isPresent()){
            UserModel userModel = userModelOp.get();

            if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(type)){
                List<CitizenBirthModel> citizenBirthModels = citizenBirthRepository.findByOrganizationIdAndStatus(Long.parseLong(userModel.getOrganizationId()), Constants.RECORD_STATUS_PENDING);
                if(citizenBirthModels != null){
                    response.setStatus(HttpStatus.OK);
                    response.setData(citizenBirthModels);
                }
            }
            else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(type)){
                List<CitizenDeathModel> citizenDeathModels = citizenDeathRepository.findByOrganizationIdAndStatus(Long.parseLong(userModel.getOrganizationId()), Constants.RECORD_STATUS_PENDING);
                if(citizenDeathModels != null){
                    response.setStatus(HttpStatus.OK);
                    response.setData(citizenDeathModels);
                }
            }
            else if(Constants.RECORD_TYPE_STILLBIRTH.equalsIgnoreCase(type)){
                List<CitizenSBirthModel> citizenSBirthModels = citizenSBirthRepository.findByOrganizationIdAndStatus(Long.parseLong(userModel.getOrganizationId()), Constants.RECORD_STATUS_PENDING);
                if(citizenSBirthModels != null){
                    response.setStatus(HttpStatus.OK);
                    response.setData(citizenSBirthModels);
                }
            }
        }
        return response;
    }

    @Override
    public ApiResponse viewCitizenRecordsByTypeAndId(String type, String tempId, HttpServletRequest request) {
        ApiResponse response = new ApiResponse();
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> userModelOp = authRepository.findById(Long.parseLong(userId));
        if(userModelOp.isPresent()){
            UserModel userModel = userModelOp.get();

            if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(type)){
                CitizenBirthModel citizenBirthModel = citizenBirthRepository.findByBirthIdTempAndStatusAndOrganizationId(Long.parseLong(tempId), Constants.RECORD_STATUS_PENDING, Long.parseLong(userModel.getOrganizationId()));
                if(citizenBirthModel != null){
                    response.setStatus(HttpStatus.OK);
                    response.setData(citizenBirthModel);
                }
            }
            else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(type)){
                CitizenDeathModel citizenDeathModel = citizenDeathRepository.findByDeathIdTempAndStatusAndOrganizationId(Long.parseLong(tempId), Constants.RECORD_STATUS_PENDING, Long.parseLong(userModel.getOrganizationId()));
                if(citizenDeathModel != null){
                    response.setStatus(HttpStatus.OK);
                    response.setData(citizenDeathModel);
                }
            }
            else if(Constants.RECORD_TYPE_STILLBIRTH.equalsIgnoreCase(type)){
               CitizenSBirthModel citizenSBirthModel = citizenSBirthRepository.findBySbirthIdTempAndStatusAndOrganizationId(Long.parseLong(tempId), Constants.RECORD_STATUS_PENDING, Long.parseLong(userModel.getOrganizationId()));
                if(citizenSBirthModel != null){
                    response.setStatus(HttpStatus.OK);
                    response.setData(citizenSBirthModel);
                }
            }
        }
        return response;
    }

    @Override
    public ApiResponse getFilteredData(String type, CFCFilterDto cfcFilterDto, HttpServletRequest request) {

        ApiResponse response = new ApiResponse();
        String userName = authService.getUserIdFromRequest(request);
        // String userName = jwtUtil.getUsernameFromRequest(request);

        if(Constants.RECORD_TYPE_BIRTH.equalsIgnoreCase(type)) {
            List<CitizenBirthModel> birthRecords = filterBirthRecords(cfcFilterDto, type, userName);
            response.setStatus(HttpStatus.OK);
            response.setData(birthRecords);
        }else if(Constants.RECORD_TYPE_DEATH.equalsIgnoreCase(type)){
            List<CitizenDeathModel> deathRecords = filterDeathRecords(cfcFilterDto, type, userName);
            response.setStatus(HttpStatus.OK);
            response.setData(deathRecords);
        }
        else if(Constants.RECORD_TYPE_STILLBIRTH.equalsIgnoreCase(type)){
            List<CitizenSBirthModel> sBirthRecords = filterSbirthRecords(cfcFilterDto, type, userName);
            response.setStatus(HttpStatus.OK);
            response.setData(sBirthRecords);
        }

        return response;
    }

    private List<CitizenBirthModel> filterBirthRecords(CFCFilterDto filterDto1, String filterType, String userName){
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        return citizenBirthRepository.findAll(new Specification<CitizenBirthModel>() {
            @Override
            public Predicate toPredicate(Root<CitizenBirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    CFCFilterDto filterDto = filterDto1;
                    boolean isJoinCorrection = false;
                    boolean isJoinInclusion = false;
                    if (filterDto == null) {
                        filterDto = new CFCFilterDto();
                        filterDto.setRegEndDate(LocalDate.now());
                        filterDto.setRegStartDate(LocalDate.now().minusDays(Constants.DEFAULT_DAYS));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                        // registrationNumber
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                                filterDto.getRegistrationNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getTrackingNo())) {
                        // trackingNo
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.TRACKING_NUMBER), filterDto.getTrackingNo())));

                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getMobileNumber())) {
                        // contactNumber
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getMobileNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                        // motherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName() + "%")));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                        // fatherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName() + "%")));
                    }


                    // Search with Event Place flag

                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlaceFlag())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE_FLAG), filterDto.getEventPlaceFlag())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                    }

                    //Division Code

                    if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), filterDto.getDivisionCode())));
                    }

                    // Filter by Gender
                    if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), filterDto.getGenderCode())));
                    }
                    // Filter by SPOUSE NAME
                    if (!CommonUtil.checkNullOrBlank(filterDto.getHusbandWifeName())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.HUSBAND_WIFE_NAME), filterDto.getHusbandWifeName())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate() + "")) {
                        // eventDate
                        CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00, 00, 00), filterDto.getEventEndDate().atTime(23, 59, 59));

                        predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                                filterDto.getEventStartDate().atTime(00, 00, 00), filterDto.getEventEndDate().atTime(23, 59, 59))));
                    }
                    // status
                    if(!CommonUtil.checkNullOrBlank(filterDto.getStatus())){
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.STATUS), filterDto.getStatus())));
                    }

                    query.orderBy(criteriaBuilder.desc(root.get("registrationDatetime")));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }catch(Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }
        });
    }
    private List<CitizenDeathModel> filterDeathRecords(CFCFilterDto filterDto1, String filterType, String userName){
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        return citizenDeathRepository.findAll(new Specification<CitizenDeathModel>() {
            @Override
            public Predicate toPredicate(Root<CitizenDeathModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    CFCFilterDto filterDto = filterDto1;
                    boolean isJoinCorrection = false;
                    boolean isJoinInclusion = false;
                    if (filterDto == null) {
                        filterDto = new CFCFilterDto();
                        filterDto.setRegEndDate(LocalDate.now());
                        filterDto.setRegStartDate(LocalDate.now().minusDays(Constants.DEFAULT_DAYS));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                        // registrationNumber
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                                filterDto.getRegistrationNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getTrackingNo())) {
                        // trackingNo
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.TRACKING_NUMBER), filterDto.getTrackingNo())));

                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getMobileNumber())) {
                        // contactNumber
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getMobileNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                        // motherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName() + "%")));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                        // fatherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName() + "%")));
                    }


                    // Search with Event Place flag

                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlaceFlag())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE_FLAG), filterDto.getEventPlaceFlag())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                    }

                    //Division Code

                    if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), filterDto.getDivisionCode())));
                    }

                    // Filter by Gender
                    if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), filterDto.getGenderCode())));
                    }
                    // Filter by SPOUSE NAME
                    if (!CommonUtil.checkNullOrBlank(filterDto.getHusbandWifeName())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.HUSBAND_WIFE_NAME), filterDto.getHusbandWifeName())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate() + "")) {
                        // eventDate
                        CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00, 00, 00), filterDto.getEventEndDate().atTime(23, 59, 59));

                        predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                                filterDto.getEventStartDate().atTime(00, 00, 00), filterDto.getEventEndDate().atTime(23, 59, 59))));
                    }
                    // status
                    if(!CommonUtil.checkNullOrBlank(filterDto.getStatus())){
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.STATUS), filterDto.getStatus())));
                    }

                    query.orderBy(criteriaBuilder.desc(root.get("registrationDatetime")));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }catch(Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }
        });
    }
    private List<CitizenSBirthModel> filterSbirthRecords(CFCFilterDto filterDto1, String filterType, String userName){
        Long organizationId = authService.getOrganizationIdFromUserId(userName);
        return citizenSBirthRepository.findAll(new Specification<CitizenSBirthModel>() {
            @Override
            public Predicate toPredicate(Root<CitizenSBirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                try {
                    List<Predicate> predicates = new ArrayList<>();
                    // logger.info("filterDto1:" + filterDto1);
                    CFCFilterDto filterDto = filterDto1;
                    boolean isJoinCorrection = false;
                    boolean isJoinInclusion = false;
                    if (filterDto == null) {
                        filterDto = new CFCFilterDto();
                        filterDto.setRegEndDate(LocalDate.now());
                        filterDto.setRegStartDate(LocalDate.now().minusDays(Constants.DEFAULT_DAYS));
                    }

                    // logger.info("filterDto:" + filterDto);
//                    predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
//                            Constants.RECORD_STATUS_DRAFT)));
//                    predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
//                            Constants.RECORD_STATUS_REJECTED)));
//
//                    predicates.add(criteriaBuilder.and(criteriaBuilder.notEqual(root.get(Constants.STATUS),
//                            Constants.RECORD_STATUS_PENDING)));

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegistrationNumber())) {
                        // registrationNumber
                        predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                                filterDto.getRegistrationNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getTrackingNo())) {
                        // trackingNo
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.TRACKING_NUMBER), filterDto.getTrackingNo())));

                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getMobileNumber())) {
                        // contactNumber
                        predicates.add(criteriaBuilder.and(
                                criteriaBuilder.equal(root.get(Constants.CONTACT_NUMBER), filterDto.getMobileNumber())));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getMotherName())) {
                        // motherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), filterDto.getMotherName() + "%")));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getFatherName())) {
                        // fatherName
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), filterDto.getFatherName() + "%")));
                    }


                    // Search with Event Place flag

                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlaceFlag())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE_FLAG), filterDto.getEventPlaceFlag())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventPlace())) {
                        // eventPlace
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.EVENT_PLACE), filterDto.getEventPlace())));
                    }

                    //Division Code

                    if (!CommonUtil.checkNullOrBlank(filterDto.getDivisionCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), filterDto.getDivisionCode())));
                    }

                    // Filter by Gender
                    if (!CommonUtil.checkNullOrBlank(filterDto.getGenderCode())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), filterDto.getGenderCode())));
                    }
                    // Filter by SPOUSE NAME
                    if (!CommonUtil.checkNullOrBlank(filterDto.getHusbandWifeName())) {
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.HUSBAND_WIFE_NAME), filterDto.getHusbandWifeName())));
                    }

                    if (!CommonUtil.checkNullOrBlank(filterDto.getRegStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getRegEndDate() + "")) {
                        // registrationDatetime
                        CommonUtil.betweenDates(filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59));
                        predicates
                                .add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.REGISTRATION_DATE_TIME),
                                        filterDto.getRegStartDate().atTime(00, 00, 00), filterDto.getRegEndDate().atTime(23, 59, 59))));
                    }
                    if (!CommonUtil.checkNullOrBlank(filterDto.getEventStartDate() + "") && !CommonUtil.checkNullOrBlank(filterDto.getEventEndDate() + "")) {
                        // eventDate
                        CommonUtil.betweenDates(filterDto.getEventStartDate().atTime(00, 00, 00), filterDto.getEventEndDate().atTime(23, 59, 59));

                        predicates.add(criteriaBuilder.and(criteriaBuilder.between(root.get(Constants.EVENT_DATE),
                                filterDto.getEventStartDate().atTime(00, 00, 00), filterDto.getEventEndDate().atTime(23, 59, 59))));
                    }
                    // status

                    if(!CommonUtil.checkNullOrBlank(filterDto.getStatus())){
                        predicates.add(criteriaBuilder
                                .and(criteriaBuilder.equal(root.get(Constants.STATUS), filterDto.getStatus())));
                    }
//                    Predicate predicateApproved = criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_APPROVED);
//
//                    Predicate approvedPredicate = criteriaBuilder.and(
//                            criteriaBuilder.equal(root.get(Constants.STATUS), Constants.RECORD_STATUS_APPROVED));
////


                    query.orderBy(criteriaBuilder.desc(root.get("registrationDatetime")));
                    logger.info("QUERY BULDER QUERY ==== "+query);
                    return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
                }catch(Exception e) {
                    logger.error("Exception {}... ", e);
                    throw e;
                }
            }
        });
    }


    public List<BirthModel> getBirthListForReport(OnlinePrintRequestDto printRequestDto) throws DateTimeParseException,Exception {
        logger.info("Calling getBirthListForReport ====== " + LocalDateTime.now());
        return birthRepository.findAll(new Specification<BirthModel>() {
            @Override
            public Predicate toPredicate(Root<BirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                boolean isFilter =false;
                List<String> status = new ArrayList<String>();
                status.add(Constants.RECORD_STATUS_DRAFT);
                status.add(Constants.RECORD_STATUS_PENDING);
                status.add(Constants.RECORD_STATUS_REJECTED);

                predicates.add(criteriaBuilder.and(root.get(Constants.STATUS).in(status).not()));

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            printRequestDto.getRegistrationNumber())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getApplNo())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), printRequestDto.getApplNo()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), printRequestDto.getApplNo())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), printRequestDto.getMotherName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), printRequestDto.getFatherName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getDivisionCode())) {
                    // DivisionCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), printRequestDto.getDivisionCode())));
                    isFilter =true;
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getOrganizationCode())) {
                    // OrganizationCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE), printRequestDto.getOrganizationCode())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getGenderCode())) {
                    // GenderCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), printRequestDto.getGenderCode())));
                }
                if(Constants.RECORD_TYPE_BIRTH.equals(printRequestDto.getRecordType())) {

                  //  predicates.add(criteriaBuilder.and(root.get(Constants.CHILD_NAME).isNotNull()));

                }
                else if(Constants.RECORD_NAME_INCLUSION.equals(printRequestDto.getRecordType())) {
                    //   predicates.add(criteriaBuilder.and(root.get(Constants.CHILD_NAME).isNull()));
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), printRequestDto.getName()+"%")));
                    isFilter =true;
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getDateOfEvent()+"")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Constants.EVENT_DATE),
                            printRequestDto.getDateOfEvent().atTime(00, 00, 00)));

                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(Constants.EVENT_DATE),
                            printRequestDto.getDateOfEvent().atTime(23, 59, 59)));

                    isFilter =true;
                }
                if(!isFilter){
                    throw new IllegalArgumentException("Invalid Request");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }




    public List<SBirthModel> getStillBirthListForReport(OnlinePrintRequestDto printRequestDto) throws DateTimeParseException,Exception{
        logger.info("Calling getStillBirthListForReport ====== " + LocalDateTime.now());
        return sBirthRepository.findAll(new Specification<SBirthModel>() {
            @Override
            public Predicate toPredicate(Root<SBirthModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                boolean isFilter =false;
                List<String> status = new ArrayList<String>();
                status.add(Constants.RECORD_STATUS_DRAFT);
                status.add(Constants.RECORD_STATUS_PENDING);
                status.add(Constants.RECORD_STATUS_REJECTED);

                predicates.add(criteriaBuilder.and(root.get(Constants.STATUS).in(status).not()));

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            printRequestDto.getRegistrationNumber())));
                    isFilter =true;
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getApplNo())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), printRequestDto.getApplNo()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), printRequestDto.getApplNo())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), printRequestDto.getMotherName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), printRequestDto.getFatherName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getDivisionCode())) {
                    // DivisionCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), printRequestDto.getDivisionCode())));
                    isFilter =true;
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getOrganizationCode())) {
                    // OrganizationCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE), printRequestDto.getOrganizationCode())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getGenderCode())) {
                    // GenderCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), printRequestDto.getGenderCode())));
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getHusbandWifeName())) {
                    // HusbandWifeName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.HUSBAND_WIFE_NAME), printRequestDto.getHusbandWifeName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), printRequestDto.getName()+"%")));
                    isFilter =true;
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getDateOfEvent()+"")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Constants.EVENT_DATE),
                            printRequestDto.getDateOfEvent().atTime(00, 00, 00)));

                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(Constants.EVENT_DATE),
                            printRequestDto.getDateOfEvent().atTime(23, 59, 59)));
                    isFilter =true;
                }
                if(!isFilter){
                    throw new IllegalArgumentException("Invalid Request");
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }
    public List<DeathModel> getDeathListForReport(OnlinePrintRequestDto printRequestDto) throws DateTimeParseException,Exception{
        logger.info("Calling getDeathListForReport ====== " + LocalDateTime.now());
        return deathRepository.findAll(new Specification<DeathModel>() {
            @Override
            public Predicate toPredicate(Root<DeathModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                boolean isFilter =false;

                List<String> status = new ArrayList<String>();
                status.add(Constants.RECORD_STATUS_DRAFT);
                status.add(Constants.RECORD_STATUS_PENDING);
                status.add(Constants.RECORD_STATUS_REJECTED);

                predicates.add(criteriaBuilder.and(root.get(Constants.STATUS).in(status).not()));

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getRegistrationNumber())) {
                    // registrationNumber
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.REGISTRATION_NUMBER),
                            printRequestDto.getRegistrationNumber())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getApplNo())) {
                    // applicationNumber
                    predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get(Constants.APPLICATION_NUMBER), printRequestDto.getApplNo()),
                            criteriaBuilder.equal(root.get(Constants.ORIGINAL_APPLICATION_NUMBER), printRequestDto.getApplNo())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getMotherName())) {
                    // motherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.MOTHER_NAME), printRequestDto.getMotherName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getFatherName())) {
                    // fatherName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.FATHER_NAME), printRequestDto.getFatherName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getDivisionCode())) {
                    // DivisionCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.DIVISION_CODE), printRequestDto.getDivisionCode())));
                    isFilter =true;
                }

                if (!CommonUtil.checkNullOrBlank(printRequestDto.getOrganizationCode())) {
                    // OrganizationCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.ORGANIZATION_CODE), printRequestDto.getOrganizationCode())));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getGenderCode())) {
                    // GenderCode
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.equal(root.get(Constants.GENDER_CODE), printRequestDto.getGenderCode())));

                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getHusbandWifeName())) {
                    // HusbandWifeName
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.HUSBAND_WIFE_NAME), printRequestDto.getHusbandWifeName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getName())) {
                    // name
                    predicates.add(criteriaBuilder
                            .and(criteriaBuilder.like(root.get(Constants.CHILD_NAME), printRequestDto.getName()+"%")));
                    isFilter =true;
                }
                if (!CommonUtil.checkNullOrBlank(printRequestDto.getDateOfEvent()+"")) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(Constants.EVENT_DATE),
                            printRequestDto.getDateOfEvent().atTime(00, 00, 00)));

                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(Constants.EVENT_DATE),
                            printRequestDto.getDateOfEvent().atTime(23, 59, 59)));
                    isFilter =true;
                }
                if(!isFilter){
                    throw new IllegalArgumentException("Invalid Request");
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });
    }
}
