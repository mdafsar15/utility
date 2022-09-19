package com.ndmc.ndmc_record.serviceImpl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.config.KeycloakConstants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.*;
import com.ndmc.ndmc_record.repository.AuthRepository;
import com.ndmc.ndmc_record.repository.OrganizationRepository;
import com.ndmc.ndmc_record.repository.ResetTokenRepository;
import com.ndmc.ndmc_record.repository.UserRoleRepository;
import com.ndmc.ndmc_record.service.AuthService;
import com.ndmc.ndmc_record.service.KeycloakApiService;
import com.ndmc.ndmc_record.utils.CommonUtil;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.json.JSONString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import com.ndmc.ndmc_record.utils.CustomBeanUtils;
import com.ndmc.ndmc_record.utils.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Qualifier("userDetailService")
public class AuthServiceImpl implements AuthService {

    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    //1 Hour Token Expiration time
    private static final int TOKEN_VALIDITY = 3600 * 1;
    //@Autowired
    //private PasswordEncoder passwordEncoder;
    @Autowired
    AuthRepository authRepository;
    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthServiceImpl authService;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    ResetTokenRepository resetTokenRepository;

    @Autowired

    private KeycloakApiService keycloakService;

    @Override
    public ApiResponse saveUserRecords(UserDto userDto, HttpServletRequest request) {
        UserModel userModel = new UserModel();
        ApiResponse res = new ApiResponse();

        logger.info("==== userDTO===="+userDto);
        String username = authService.getUserIdFromRequest(request);
        int userCounter = 0;
            if(Constants.USER_TYPE_CFC.equalsIgnoreCase(userDto.getUserType())){

                logger.info("==== userDto.getUserType() ===="+userDto.getUserType());
                if(userDto.getEmployeeCode().isEmpty()){
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.EMPCODE_MANDATORY);
                }else{
                    if(authRepository.existsByEmployeeCode(userDto.getEmployeeCode())){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.EMPCODE_NOT_AVAILABLE);
                    }
                    else if(authRepository.existsByContactNo(userDto.getContactNo())){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.MOBILENO_NOT_AVAILABLE);
                    }
                    else if(authRepository.existsByEmail(userDto.getEmail())){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.EMAIL_NOT_AVAILABLE);
                    }
                    else if(authRepository.existsByUserName(userDto.getFirstName()+userDto.getLastName())){

                        String resUserName = createUniqueUserName(userDto.getFirstName()+userDto.getLastName());
                        logger.info("=============User Id with 01 =============="+resUserName);
                        userModel.setUserName(resUserName);
                        UserModel userResponse = insertUserDeails(userModel, username, userDto, res);
                    }
                    else{
                        userModel.setUserName((userDto.getFirstName()+userDto.getLastName()).toLowerCase());
                       // userModel.setUserName(userDto.getEmployeeCode());
                        UserModel userResponse = insertUserDeails(userModel, username, userDto, res);

                    }


                }

            }
            else if(Constants.USER_TYPE_HOSPITAL.equalsIgnoreCase(userDto.getUserType())){
                logger.info("==== user type is HOSPITAL ===="+userDto.getUserType());

                if(userDto.getContactNo().isEmpty()){
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.MOBILENO_MANDATORY);
                }else{
                    if(authRepository.existsByContactNo(userDto.getContactNo())){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.MOBILENO_NOT_AVAILABLE);
                    }
                    else if(authRepository.existsByEmail(userDto.getEmail())){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.EMAIL_NOT_AVAILABLE);
                    }
                    else if(authRepository.existsByUserName(userDto.getFirstName()+userDto.getLastName())){

                        String resUserName = createUniqueUserName(userDto.getFirstName()+userDto.getLastName());
                        logger.info("=============User Id with 01 =============="+resUserName);
                        userModel.setUserName(resUserName);
                        UserModel userResponse = insertUserDeails(userModel, username, userDto, res);

                    }
                    else{
                        userModel.setUserName((userDto.getFirstName()+userDto.getLastName()).toLowerCase());
                       // userModel.setUserName(userDto.getContactNo());
                        insertUserDeails(userModel, username, userDto, res);
                    }


                }
            }

           return res;

        }

    private String createUniqueUserName(String username) {

       // UserModel userDetails = authRepository.findByUserNameStartsWith(username);
        UserModel userDetails = authRepository.findByUserNameLike(username);
        String newUserName = "";
        String rawUserName = userDetails.getUserName();
        int indexOfZero = rawUserName.indexOf("0");
        String lastChars = rawUserName.substring(rawUserName.length() - 1);
        logger.info("===== lastChars ====="+lastChars);
       // String substring = rawUserName.length() > 2 ? rawUserName.substring(rawUserName.length() - 2) : rawUserName;
        logger.info("=============User Id with 01 =============="+userDetails);
        String subString = "";
        if (indexOfZero != -1)
        {
            subString= rawUserName.substring(0 , indexOfZero); //this will give abc
        }
        else{
            subString = rawUserName;
        }
        if(isNumeric(lastChars)){
            int counter = Integer.parseInt(lastChars);
            logger.info("===== counter ====="+counter);
            counter++;
            newUserName = subString+"0"+counter;
            logger.info("===== newUserName ====="+newUserName);
        }
        else{
            logger.info("===== LASTCHARS NOT NUMBER ====="+lastChars);
            int initialCounter =1;
            newUserName = subString+"0"+initialCounter;
            logger.info("===== newUserName ====="+newUserName);
        }
        return newUserName;

    }

    private String createUniqueUserNameByName(String username) {

        // UserModel userDetails = authRepository.findByUserNameStartsWith(username);
        UserModel userDetails = authRepository.findByUserNameLike(username);
        String newUserName = "";
        String rawUserName = userDetails.getUserName();
        int indexOfZero = rawUserName.indexOf("0");
        String lastChars = rawUserName.substring(rawUserName.length() - 1);
        logger.info("===== lastChars ====="+lastChars);
        // String substring = rawUserName.length() > 2 ? rawUserName.substring(rawUserName.length() - 2) : rawUserName;
        logger.info("=============User Id with 01 =============="+userDetails);
        String subString = "";
        if (indexOfZero != -1)
        {
            subString= rawUserName.substring(0 , indexOfZero); //this will give abc
        }
        else{
            subString = rawUserName;
        }
        if(isNumeric(lastChars)){
            int counter = Integer.parseInt(lastChars);
            logger.info("===== counter ====="+counter);
            counter++;
            newUserName = subString+"0"+counter;
            logger.info("===== newUserName ====="+newUserName);
        }
        else{
            logger.info("===== LASTCHARS NOT NUMBER ====="+lastChars);
            int initialCounter =1;
            newUserName = subString+"0"+initialCounter;
            logger.info("===== newUserName ====="+newUserName);
        }
        return newUserName;

    }

    public boolean isNumeric(String string) {
        int intValue;

      //  System.out.println(String.format("Parsing string: \"%s\"", string));

        if(string == null || string.equals("")) {
           // System.out.println("String cannot be parsed, it is null or empty.");
            return false;
        }

        try {
            intValue = Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            logger.info("Input String cannot be parsed to Integer.");
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
    }


    private UserModel insertUserDeails(UserModel userModel, String username, UserDto userDto, ApiResponse res) {
        LocalDateTime now = LocalDateTime.now();

        CustomBeanUtils.copyUserDtoToUserModel(userDto, userModel);
        userModel.setCreatedAt(now);
        userModel.setModifiedAt(now);
        userModel.setCreatedBy(username);
        userModel.setModifiedBy(username);
        String password = generateRandomPassword(8);
        //String password = Constants.DEFAULT_PASSWORD;
        String encodedPassword = passwordEncoder.encode(password);
        userModel.setPassword(encodedPassword);
        //Long userId = userDto.getRoles().stream().map(r -> r.getRoleId());
        Set<UserRoleModel> roleList = userRoleRepository.findByRoleIdIn(userDto.getRoles());

        // roles.add(role.get());
        userModel.setRoles(roleList);
        userModel.setStatus(Constants.USER_DEFAULT_STATUS);
       // userModel.setUserType(userDto.getUserType());
        //userModel.setOrganizationCode(userDto.getOrganizationCode());
        logger.info("Organization id "+userDto.getOrganizationId());
        if( (userDto.getOrganizationId().isEmpty()) || (userDto.getOrganizationId() == null)) {
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.ORG_ID_MANDATORY);
        }
        else{
            userModel = authRepository.save(userModel);

// Keycloak Token Service - call for User Creation

            if(userModel != null) {

                try {
                    String token = keycloakService.adminToken(KeycloakConstants.TOKEN);
                    ApiResponse KeycloakUserCreation = keycloakService.createUser(userModel, password, token);
                    if(KeycloakUserCreation.getData().toString().equalsIgnoreCase(KeycloakConstants.USER_CREATION_CODE))
                    {
                        logger.info("User Created ====== " +KeycloakUserCreation.getData() + "-------" + KeycloakUserCreation.getMsg());
                        String UserIDForRole = keycloakService.getUsernameForRole(userModel, token);
                        List<JSONObject> ROLE_DETAIL =  keycloakService.getRoleDetail(userModel, token);
                        logger.info("LIST----- " +ROLE_DETAIL);
                        for(int i=0; i<ROLE_DETAIL.size(); i++)
                        {
                            JSONObject data =(JSONObject) ROLE_DETAIL.get(i);
                            logger.info("Data --- " +data);
                            ApiResponse RoleApi = keycloakService.assignRoleToUser(UserIDForRole, data.get("id").toString(), data.get("name").toString(), token);
                            logger.info("Role Assigned ======= " +RoleApi.getMsg() + RoleApi.getData());
                        }
                    }
                    else if(KeycloakUserCreation.getData().toString().equalsIgnoreCase(KeycloakConstants.UserCreationConflict)) {
                        authRepository.deleteById(userModel.getUserId());
                        throw new BadCredentialsException(" " +KeycloakUserCreation.getMsg());

                    }

                    else if(KeycloakUserCreation.getData().toString().equalsIgnoreCase(KeycloakConstants.KeycloakUnauthorized)) {
                        authRepository.deleteById(userModel.getUserId());
                        throw new BadCredentialsException(" " +KeycloakUserCreation.getMsg());

                    }
                    else if(KeycloakUserCreation.getData().toString().equalsIgnoreCase(KeycloakConstants.KeycloakCodeBadRequest)) {
                        authRepository.deleteById(userModel.getUserId());
                        throw new BadCredentialsException(" " +KeycloakUserCreation.getMsg());

                    }

                    else {

                        authRepository.deleteById(userModel.getUserId());

                        throw new NullPointerException(" ERROR " +HttpStatus.INTERNAL_SERVER_ERROR);

                    }

                } catch (Exception e) {

                    logger.info("The User not Created: " + e);

                }

            }
                /* Code to send user credential to his/her mobile number
                 * Code by Deepak
                 * 29-04-2022
                 * */
                if(userModel != null && !CommonUtil.checkNullOrBlank(userModel.getContactNo())){
                    CommonUtil commonUtil = new CommonUtil();

                    logger.info("  === User addition response ====="+userModel);
                    try{
                    commonUtil.sendTextMessage(userModel.getFirstName(), userModel.getContactNo(), "", Constants.USER_CREATION, Constants.USER_CREATION, userModel.getUserName(), password, "", "", "");
                    }catch (Exception e){
                        logger.info("===SMS EXCEPTION =="+e);
                    }
                    }
                res.setStatus(HttpStatus.OK);
                res.setMsg(Constants.USER_ADDED );
                res.setData(userModel);
            }
        return userModel;
    }


    @Override
    public ApiResponse updateUserRecords(UserDto userDto, HttpServletRequest request) {
       // UserModel userModel = new UserModel();
        ApiResponse res = new ApiResponse();

        logger.info("==== USER ID  ==== " + userDto.getUserId());
        if (userDto.getUserId() != null) {
            Optional<UserModel> existedUser = authRepository.findById(userDto.getUserId());
            UserModel userModel = existedUser.get();
            if (existedUser.isPresent()) {
                logger.info("==== userDTO====" + userDto);
                String username = authService.getUserIdFromRequest(request);

                if(Constants.USER_TYPE_CFC.equalsIgnoreCase(userDto.getUserType())){

                    logger.info("==== userDto.getUserType() ===="+userDto.getUserType());
                    if(userDto.getEmployeeCode().isEmpty()){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.EMPCODE_MANDATORY);
                    }else{
                        if(authRepository.existsByEmployeeCode(userDto.getEmployeeCode())
                                && !existedUser.get().getEmployeeCode().equalsIgnoreCase(userDto.getEmployeeCode())){
                            res.setStatus(HttpStatus.BAD_REQUEST);
                            res.setMsg(Constants.EMPCODE_NOT_AVAILABLE);
                        }
                        else if(authRepository.existsByContactNo(userDto.getContactNo())
                                && !existedUser.get().getContactNo().equalsIgnoreCase(userDto.getContactNo())){
                            res.setStatus(HttpStatus.BAD_REQUEST);
                            res.setMsg(Constants.MOBILENO_NOT_AVAILABLE);
                        }
                        else{

                           // userModel.setUserName(userDto.getEmployeeCode());
                            updateUserDeails(userModel, username, userDto, res);
                        }


                    }

                }
                else if(Constants.USER_TYPE_HOSPITAL.equalsIgnoreCase(userDto.getUserType())){
                    logger.info("==== user type is HOSPITAL ===="+userDto.getUserType());

                    if(userDto.getContactNo().isEmpty()){
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(Constants.MOBILENO_MANDATORY);
                    }else{
                        if(authRepository.existsByContactNo(userDto.getContactNo())
                                && !existedUser.get().getContactNo().equalsIgnoreCase(userDto.getContactNo())){
                            res.setStatus(HttpStatus.BAD_REQUEST);
                            res.setMsg(Constants.MOBILENO_NOT_AVAILABLE);
                        }
                        else{

                            //userModel.setUserName(userDto.getContactNo());
                            updateUserDeails(userModel, username, userDto, res);
                        }


                    }
                }
            }
            else{
                res.setStatus(HttpStatus.NOT_FOUND);
                res.setMsg(Constants.RECORD_NOT_FOUND);
            }
        }

        else{
            logger.info("==== USER ID not available ==== "+ userDto.getUserId());
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.USERID_NOT_AVAILABLE);
        }


        return res;
    }

    private void updateUserDeails(UserModel userModel, String username, UserDto userDto, ApiResponse res) {
        CustomBeanUtils.copyUserDtoToUserModel(userDto, userModel);
        LocalDateTime now = LocalDateTime.now();
        userModel.setUserId(userDto.getUserId());
        userModel.setModifiedAt(now);
        userModel.setModifiedBy(username);

//        if(!userDto.getPassword().isEmpty() || userDto.getPassword() != null) {
//            String encodedPassword = passwordEncoder.encode(userDto.getPassword());
//            userModel.setPassword(encodedPassword);
//        }else{
//            userModel.setPassword(userModel.getPassword());
//        }

        logger.info("Existing password is =="+userModel);
       // userModel.setPassword(userModel.getPassword());
        //Long userId = userDto.getRoles().stream().map(r -> r.getRoleId());
        Set<UserRoleModel> roleList = userRoleRepository.findByRoleIdIn(userDto.getRoles());

        // roles.add(role.get());
        userModel.setRoles(roleList);
        userModel.setStatus(Constants.USER_DEFAULT_STATUS);
       // userModel.setUserType(userDto.getUserType());
      //  userModel.setOrganizationCode(userDto.getOrganizationCode());
        userModel = authRepository.save(userModel);

        // Keycloak API - Update user CAll

        try {

            Optional<UserModel> existedUser = authRepository.findById(userDto.getUserId());

            UserModel uModel = existedUser.get();

            logger.info(uModel.getUserName() +" ---USER FETCH DETAIL DBs--- "+ uModel.getUserType());

            String token = keycloakService.adminToken(KeycloakConstants.TOKEN);

            ApiResponse KeycloakUserUpdate = keycloakService.updateUser(uModel, token);
            logger.info("User Updated Successfully " +KeycloakUserUpdate.getData().toString()+" MSG == "+KeycloakUserUpdate.getMsg().toString());
            if(KeycloakUserUpdate.getData().toString().equalsIgnoreCase(KeycloakConstants.USER_UPDATION_CODE)) {



                logger.info("User Updated Successfully " +KeycloakUserUpdate);

                String UserIDForRole = keycloakService.getUsernameForRole(uModel, token);



                //Keycloak Roles associated with Users

                List<JSONObject> USER_MAPPED_ROLES = keycloakService.userMappedRoles(uModel, token);

                logger.info("GET ------ userMappedRoles ROLES----  " +USER_MAPPED_ROLES);

                for(int i=0; i<USER_MAPPED_ROLES.size(); i++) {

                    JSONObject data =(JSONObject) USER_MAPPED_ROLES.get(i);

                    ApiResponse DeleteMappedRoles = keycloakService.deleteAssignedRoles(UserIDForRole, data.get("id").toString(), data.get("name").toString(), token);

                    logger.info("DELETE ASSIGN ROLES ----  " +DeleteMappedRoles.getData() + DeleteMappedRoles.getMsg());

                }

                List<JSONObject> ROLE_DETAIL =  keycloakService.getRoleDetail(userModel, token);

                logger.info("LIST----- " +ROLE_DETAIL);



                for(int i=0; i<ROLE_DETAIL.size(); i++) {

                    JSONObject data =(JSONObject) ROLE_DETAIL.get(i);

                    logger.info("ROLE OBJECT ---- " +data.get("id") + " ------- " + data.get("name"));

                    ApiResponse RoleApi = keycloakService.assignRoleToUser(UserIDForRole, data.get("id").toString(), data.get("name").toString(), token);

                    logger.info("Role Assigned ======= " +RoleApi.getMsg() + RoleApi.getData());

                }

            }

            else {

                throw new NullPointerException(KeycloakUserUpdate.getMsg().toString() +HttpStatus.INTERNAL_SERVER_ERROR);

            }

        } catch (Exception e) {

            logger.info("User not Updated: " +e);

        }

        res.setStatus(HttpStatus.OK);
        res.setMsg(Constants.USER_UPDATED);
        res.setData(userModel.getUserName());
    }

    @Override
    public ApiResponse getUserList() {
        ApiResponse res = new ApiResponse();

       List<UserModel> users = authRepository.findAll();
       UserModel userModel = new UserModel();



       if(users.isEmpty()){
           res.setStatus(HttpStatus.NOT_FOUND);
           res.setMsg(Constants.RECORD_NOT_FOUND);
       }
       else {

           List<OrganizationModel> organizationModels = organizationRepository.findAll();
           Map<Long, OrganizationModel>  orgMap = new HashMap<>();

          // logger.info("==Size of organizationModels==="+organizationModels.size());
           for(OrganizationModel orgModel:organizationModels){
               orgMap.put(orgModel.getOrganizationId(), orgModel);

           }


           for(UserModel user:users ){
             if(user.getOrganizationId() != null){

                // Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(user.getOrganizationId()));
                 OrganizationModel organizationModel = orgMap.get(Long.parseLong(user.getOrganizationId()));
                // logger.info("==== OrganizationModel===="+organizationModel);
                 if(organizationModel != null) {
                     user.setOrganizationCode(organizationModel.getOrganisationCode());
                     user.setOrganizationName(organizationModel.getOrganizationName());
                 }else{
                     logger.warn( "==== user list with organizationModelOp  is not Present == "+user.getOrganizationId());
                 }
             }
           }
           res.setStatus(HttpStatus.OK);
       // Use stream api for descending

        List<UserModel> descendingList = users.stream().sorted((o1, o2) -> o2.getUserId().compareTo(o1.getUserId())).collect(Collectors.toList());
           res.setData(descendingList);
       }
        return res;
    }

    public String generateRandomPassword(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                +"lmnopqrstuvwxyz!@#$%&";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public String getUserIdFromRequest(HttpServletRequest request) {
        JwtUtil jwtUtil = new JwtUtil();
        String userName = jwtUtil.getUsernameFromRequest(request);
        logger.info("==== getUsernameFromRequest==="+userName);
        UserModel userDetails =  authRepository.findByUserName(userName);
        logger.info("==== userDetails==="+userDetails);
        String userId =  userDetails.getUserId().toString();
        return userId;
    }

    public Long getOrganizationIdFromUserId(String userName) {
        logger.info("==== getOrganizationIdFromUserId==="+userName);
        Optional<UserModel> userDetails =  authRepository.findById(Long.parseLong(userName));
        logger.info("==== userDetails==="+userDetails);
        Long organizationId =0L;
        if(userDetails.isPresent()){
            organizationId = Long.parseLong(userDetails.get().getOrganizationId());
        }

        return organizationId;
    }

    @Override
    public ApiResponse getUserRoles() {
        ApiResponse res = new ApiResponse();
        List<UserRoleModel> roles = userRoleRepository.findAll();
        if(roles.isEmpty()){
            res.setStatus(HttpStatus.NOT_FOUND);
            res.setMsg(Constants.RECORD_NOT_FOUND);
        }
        else {
            res.setStatus(HttpStatus.OK);
            res.setData(roles);
        }
        return res;
    }

    @Override
    public ApiResponse getUserRolesByType(String userType) {
        ApiResponse res = new ApiResponse();
        List<UserRoleModel> roles = userRoleRepository.findByType(userType.toUpperCase());
        if(roles.isEmpty()){
            res.setStatus(HttpStatus.NOT_FOUND);
            res.setMsg(Constants.RECORD_NOT_FOUND);
        }
        else {
            res.setStatus(HttpStatus.OK);
            res.setData(roles);
        }
        return res;
    }

    @Override
    public UserModel findByUserName(String userName) {
        ApiResponse res = new ApiResponse();
        UserModel user = authRepository.findByUserName(userName);
        String orgId = user.getOrganizationId();
        Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(orgId));
        OrganizationModel organizationModel = organizationModelOp.get();
        user.setOrganizationCode(organizationModel.getOrganisationCode());
        user.setOrganizationName(organizationModel.getOrganizationName());
        user.setOrganizationAddress(organizationModel.getOrganizationAddress());


        return user;
    }

    @Override
    public ApiResponse getFilteredUser(UserFilterDto userFilter, HttpServletRequest request) {

        ApiResponse res = new ApiResponse();
        List<OrganizationModel> organizationModels = organizationRepository.findAll();
        Map<Long, OrganizationModel>  orgMap = new HashMap<>();

        List<UserModel> users = authRepository.findAll(new Specification<UserModel>() {
            @Override
            public Predicate toPredicate(Root<UserModel> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                if (!CommonUtil.checkNullOrBlank(userFilter.getFirstName())) {
                    // FirstName
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_FNAME),
                            userFilter.getFirstName())));
                }
                if (!CommonUtil.checkNullOrBlank(userFilter.getLastName())) {
                    // LastName
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_LNAME),
                            userFilter.getLastName())));
                }
                if (!CommonUtil.checkNullOrBlank(userFilter.getOrgId())) {
                    // Organization Id
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_ORGID),
                            userFilter.getOrgId())));
                }
                if (!CommonUtil.checkNullOrBlank(userFilter.getMobileNo())) {
                    // Mobile Number
                    predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(Constants.USER_MOBILENO),
                            userFilter.getMobileNo())));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        });



        // logger.info("==Size of organizationModels==="+organizationModels.size());
        for(OrganizationModel orgModel:organizationModels){
            orgMap.put(orgModel.getOrganizationId(), orgModel);

        }

        for(UserModel user:users ){
            if(user.getOrganizationId() != null){

                // Optional<OrganizationModel> organizationModelOp = organizationRepository.findById(Long.parseLong(user.getOrganizationId()));
                OrganizationModel organizationModel = orgMap.get(Long.parseLong(user.getOrganizationId()));
                // logger.info("==== OrganizationModel===="+organizationModel);
                if(organizationModel != null) {
                    user.setOrganizationCode(organizationModel.getOrganisationCode());
                    user.setOrganizationName(organizationModel.getOrganizationName());
                }else{
                    logger.warn( "==== user list with organizationModelOp  is not Present == "+user.getOrganizationId());
                }
            }
        }
        res.setStatus(HttpStatus.OK);
        // Use stream api for descending

        List<UserModel> descendingList = users.stream().sorted((o1, o2) -> o2.getUserId().compareTo(o1.getUserId())).collect(Collectors.toList());
        res.setData(descendingList);
        return res;
    }

    @Override
    public ApiResponse resetPassword(PasswordChangeDto passwordResetDto, HttpServletRequest request) {

        ApiResponse res = new ApiResponse();
        UserModel userModel = new UserModel();
        String userId = authService.getUserIdFromRequest(request);
        Optional<UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));
        UserModel currentUser = currentUserOp.get();
        logger.info("==== current user is===="+currentUser);
        String currentPassword = currentUser.getPassword();
        if(currentUser != null){
            logger.info("==== current Password is===="+currentPassword);
            if(passwordResetDto.getCurrentPassword().isEmpty()){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.CURRENT_PWD_BLANK);
            }
            else if((passwordResetDto.getNewPassword().isEmpty())){
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.NEW_PWD_BLANK);
            }
            boolean result = passwordEncoder.matches(passwordResetDto.getCurrentPassword(), currentPassword);
            logger.info("==== current Password matched ===="+result);
            if(result){


                    CustomBeanUtils.copyCurrentUserDetailsToUserModel(currentUser, userModel);
                    userModel.setUserId(currentUser.getUserId());
                    String encodedPassword = passwordEncoder.encode(passwordResetDto.getNewPassword());
                    userModel.setPassword(encodedPassword);
                    userModel.setModifiedBy(currentUser.getUserId().toString());
                    userModel.setModifiedAt(LocalDateTime.now());

                    UserModel savedData = authRepository.save(userModel);

                if(savedData != null) {
                    try {
                        String token = keycloakService.adminToken(KeycloakConstants.TOKEN);
                        ApiResponse resetKeycloakPassword = keycloakService.keycloakresetPassword(savedData, token, passwordResetDto.getNewPassword());
                        logger.info(resetKeycloakPassword.getMsg() + resetKeycloakPassword.getData());
                        ApiResponse smsResponse = CommonUtil.sendMessage(passwordResetDto.getNewPassword(), userModel, currentUser.getContactNo(), Constants.MSG_TYPE_CHANGE_PWD);
                    }catch (Exception e){
                        logger.info(e.getMessage());
                    }

                }

                /* Code to send user password to his/her mobile number
                 * Code by Deepak
                 * 29-04-2022
                 * */
                if(savedData != null && !CommonUtil.checkNullOrBlank(userModel.getContactNo())){
                    CommonUtil commonUtil = new CommonUtil();

                    logger.info("  === User addition response ====="+userModel);
                    try{
                    commonUtil.sendTextMessage(userModel.getFirstName(), userModel.getContactNo(), "", Constants.FORGOT_PASSWORD, Constants.FORGOT_PASSWORD, userModel.getUserName(), passwordResetDto.getNewPassword(), "", "", "");
                    }catch (Exception e){
                        logger.info("===SMS EXCEPTION =="+e);
                    }
                    }

                    res.setStatus(HttpStatus.OK);
                    res.setMsg(Constants.PASSWORD_UPDATED);


            }
            else{
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.PASSWORD_NOT_MATCHED);
            }
        }
        else{
           res.setStatus(HttpStatus.BAD_REQUEST);
           res.setMsg(Constants.RECORD_NOT_FOUND);
        }

        return res;
    }

    @Override
    public ApiResponse forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        ApiResponse res = new ApiResponse();
        String regex = "^[0-9]+$";
        Pattern pattern = Pattern.compile(regex);

        if(forgotPasswordDto.getMobileNoOrEmail().isEmpty()){
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.MANDATORY_FIELD);
        }
        else {
            Matcher insertedData = pattern.matcher(forgotPasswordDto.getMobileNoOrEmail());
            if(insertedData.matches()){
                UserModel userDetails = authRepository.findByMobileNo(forgotPasswordDto.getMobileNoOrEmail());
                res.setStatus(HttpStatus.OK);
                if(userDetails != null){

                    String mobileNumber = forgotPasswordDto.getMobileNoOrEmail();
                    String otp = CommonUtil.getRandomNumberString();
                    ApiResponse tokenResponse = createPasswordOtpForUser(userDetails, otp, mobileNumber);
                    logger.info(" OTP TOKEN RESPONSE =="+tokenResponse);
                    if(tokenResponse.getStatus().equals(HttpStatus.OK)){
                        res.setStatus(HttpStatus.OK);
                        res.setMsg(tokenResponse.getMsg());
                    }
                    else{
                        res.setStatus(HttpStatus.BAD_REQUEST);
                        res.setMsg(tokenResponse.getMsg());
                    }

                }
                else{
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.MOBILE_IS_REGISTERED);
                }

            }
            else{

               // sendEmai(forgotPasswordDto.getMobileNoOrEmail());

                UserModel userDetails = authRepository.findByEmail(forgotPasswordDto.getMobileNoOrEmail());
               logger.info("====== user details is "+userDetails);
                String token = UUID.randomUUID().toString();
                if(userDetails != null) {
                    ApiResponse tokenResponse = createPasswordResetTokenForUser(userDetails, token);
                    logger.info("==== tokenResponse " + tokenResponse);
                    if(tokenResponse !=null){
                        res.setStatus(tokenResponse.getStatus());
                        res.setMsg(tokenResponse.getMsg());
                    }
                }
                else{
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.EMAIL_NOT_REGISTERED);
                }
            }
        }
        return res;
    }

    @Override
    public ApiResponse resentForgotPassword(ForgotPasswordDto forgotPasswordDto) {
        ApiResponse res = new ApiResponse();
        Integer count = 1;
        String regex = "^[0-9]+$";
        Pattern pattern = Pattern.compile(regex);


        if(forgotPasswordDto.getMobileNoOrEmail().isEmpty()){
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.MANDATORY_FIELD);
        }
        else {

            Matcher insertedData = pattern.matcher(forgotPasswordDto.getMobileNoOrEmail());
            if(insertedData.matches()){

                UserModel userDetails = authRepository.findByMobileNo(forgotPasswordDto.getMobileNoOrEmail());
                res.setStatus(HttpStatus.OK);
                if(userDetails != null){

                    String mobileNumber = forgotPasswordDto.getMobileNoOrEmail();
                    ResetTokenModel otpDetails = findActiveOtpFromUserId(userDetails.getUserId());
                    if(otpDetails != null ){

                       // ApiResponse smsResponse = sendSms(otpDetails.getOtp(), userDetails, mobileNumber);
                       // ApiResponse otpCountResponse = updateOtpCount(otpDetails);
                        try {
                            ApiResponse smsResponse = CommonUtil.sendMessage(otpDetails.getOtp(), userDetails, mobileNumber, Constants.MSG_TYPE_FORGOT_PWD);
                            if(smsResponse != null) {
                                if (smsResponse.getStatus().equals(HttpStatus.OK)) {
                                    res.setStatus(HttpStatus.OK);
                                    res.setMsg(smsResponse.getMsg());
                                } else {
                                    res.setStatus(HttpStatus.BAD_REQUEST);
                                    res.setMsg(smsResponse.getMsg());
                                }
                            }

                        }catch (Exception e){
                            logger.info(e.getMessage());
                        }

                    }else{

                        String otp = CommonUtil.getRandomNumberString();
                        ApiResponse tokenResponse = createPasswordOtpForUser(userDetails, otp, mobileNumber);

                        if(tokenResponse.getStatus().equals(HttpStatus.OK)){
                            res.setStatus(HttpStatus.OK);
                            res.setMsg(tokenResponse.getMsg());
                        }
                        else{
                            res.setStatus(HttpStatus.BAD_REQUEST);
                            res.setMsg(tokenResponse.getMsg());
                        }
                    }



                }
                else{
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.MOBILE_IS_REGISTERED);
                }

            }
            else{

                // sendEmai(forgotPasswordDto.getMobileNoOrEmail());

                UserModel userDetails = authRepository.findByEmail(forgotPasswordDto.getMobileNoOrEmail());
                logger.info("====== user details is "+userDetails);
                String token = UUID.randomUUID().toString();
                if(userDetails != null) {
                    ApiResponse tokenResponse = createPasswordResetTokenForUser(userDetails, token);
                    logger.info("==== tokenResponse " + tokenResponse);
                    if(tokenResponse !=null){
                        res.setStatus(tokenResponse.getStatus());
                        res.setMsg(tokenResponse.getMsg());
                    }
                }
                else{
                    res.setStatus(HttpStatus.BAD_REQUEST);
                    res.setMsg(Constants.EMAIL_NOT_REGISTERED);
                }
            }
        }
        return res;
    }

    @Override
    public List<UserModel> findApproverUserDetailsUserId(String userId) {

       Optional <UserModel> currentUserOp = authRepository.findById(Long.parseLong(userId));
       UserModel currentUser = currentUserOp.get();

       List<UserModel> approverUsers = authRepository.findUserByOrganizationId(currentUser.getOrganizationId(), Constants.ROLEID_APPROVER, Constants.ROLEID_CFCAPPROVER);
        return approverUsers;
    }

    @Override
    public ApiResponse getUserListByOrg(String orgId, HttpServletRequest request) {
        ApiResponse response = new ApiResponse();
        List<UserModel> users = authRepository.findByOrganizationId(orgId);
        if(users !=null) {
            response.setStatus(HttpStatus.OK);
            response.setData(users);
        }else{
            response.setMsg(Constants.INTERNAL_SERVER_ERROR);
            response.setStatus(HttpStatus.BAD_REQUEST);
        }
        return response;
    }

    private ApiResponse updateOtpCount(ResetTokenModel otpDetails) {
        ApiResponse res = new ApiResponse();
        ResetTokenModel tokenModel = new ResetTokenModel();
        int count = otpDetails.getCount();
        tokenModel.setCount(count+1);
        tokenModel.setToken(otpDetails.getToken());
        tokenModel.setOtp(otpDetails.getOtp());
        tokenModel.setExpiryDate(otpDetails.getExpiryDate());
        tokenModel.setId(otpDetails.getId());
        ResetTokenModel updateResponse = resetTokenRepository.save(tokenModel);
        if(updateResponse != null){
            res.setStatus(HttpStatus.OK);
            res.setMsg(Constants.RESET_COUNT_UPDATED);
        }
        return res;
    }


    private ResetTokenModel findActiveOtpFromUserId(Long userId) {
        ResetTokenModel resetTokenModel = resetTokenRepository.findByUserId(userId, LocalDateTime.now());
        logger.info("===== Query from Resend token model response ===="+resetTokenModel);
        return resetTokenModel;
    }

    private ApiResponse createPasswordOtpForUser(UserModel userDetails, String otp, String mobileNumber) {
        ResetTokenModel resetToken = new ResetTokenModel();
        ApiResponse res = new ApiResponse();
        // Date tokenExpiry = new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000);
        LocalDateTime tokenExpiry = LocalDateTime.now().plusMinutes(5);
        resetToken.setUser(userDetails);
        resetToken.setOtp(otp);
        resetToken.setCount(Constants.DEFAULT_RESET_COUNT);
        resetToken.setExpiryDate(tokenExpiry);
        // ResetTokenModel myToken = new ResetTokenModel(token, userDetails);
        ResetTokenModel resetTokenModel = resetTokenRepository.save(resetToken);
       // ApiResponse response = sendSms(otp, userDetails, mobileNumber);
        try {
            ApiResponse response = CommonUtil.sendMessage(otp, userDetails, mobileNumber, Constants.MSG_TYPE_OTP);
           logger.info(" SEND MESSAGE RESPONSE ==="+response);
            if(resetTokenModel != null) {
                logger.info("resetTokenModel"+resetTokenModel);
                logger.info("Email verify response"+response);
                res.setStatus(HttpStatus.OK);
                res.setMsg(response.getMsg());
            }
        }catch (Exception e){
            logger.info(e.getMessage());
        }

        return res;
    }


    @Override
    public ApiResponse verifyToken(String token) {
        final ResetTokenModel passToken = resetTokenRepository.findByToken(token);

        ApiResponse response = new ApiResponse();
        if(passToken != null) {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime expiryTime = passToken.getExpiryDate();
            long expiryLeftSeconds = ChronoUnit.SECONDS.between(currentTime, expiryTime);

            logger.info("==== expiryLeftSeconds=====" + expiryLeftSeconds);
            if (expiryLeftSeconds > 0) {

                response.setStatus(HttpStatus.OK);
                response.setData(passToken.getToken());
                response.setMsg(Constants.TOKEN_VALID);
            }
            else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.TOKEN_EXPIRED);
            }
        }else{
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setMsg(Constants.TOKEN_INVALID);
        }
        return response;
    }

    @Override
    public ApiResponse changePassword(PasswordResetDto passwordChangeDto) {

        UserModel userModel = new UserModel();
        ApiResponse res = new ApiResponse();
        ResetTokenModel tokenDetails = resetTokenRepository.findByToken(passwordChangeDto.getToken());
        logger.info("=== current token is ==== "+tokenDetails);
        if(tokenDetails != null){

            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime expiryTime = tokenDetails.getExpiryDate();
            long expiryLeftSeconds = ChronoUnit.SECONDS.between(currentTime, expiryTime);
            logger.info("==== expiryLeftSeconds=====" + expiryLeftSeconds);

            if (expiryLeftSeconds > 0) {

                Optional<UserModel> currentUserOp = authRepository.findById(tokenDetails.getId());
                UserModel currentUser = currentUserOp.get();

                CustomBeanUtils.copyCurrentUserDetailsToUserModel(currentUser, userModel);
                userModel.setUserId(currentUser.getUserId());
                String encodedPassword = passwordEncoder.encode(passwordChangeDto.getNewPassword());
                userModel.setPassword(encodedPassword);
                userModel.setModifiedBy(currentUser.getUserId().toString());
                userModel.setModifiedAt(LocalDateTime.now());

                UserModel savedData = authRepository.save(userModel);
                if(savedData != null) {
                    try {
                        ApiResponse smsResponse = CommonUtil.sendMessage(passwordChangeDto.getNewPassword(), userModel, currentUser.getContactNo(), Constants.MSG_TYPE_CHANGE_PWD);
                    }
                    catch (Exception e){
                        logger.info(e.getMessage());
                    }
                }
                res.setStatus(HttpStatus.OK);
                res.setMsg(Constants.PASSWORD_UPDATED);
            }
            else{
                res.setStatus(HttpStatus.BAD_REQUEST);
                res.setMsg(Constants.TOKEN_EXPIRED);
            }

        }
        else{
            res.setStatus(HttpStatus.BAD_REQUEST);
            res.setMsg(Constants.TOKEN_INVALID);
        }
        return res;
    }

    @Override
    public ApiResponse verifyOtp(String otp) {

        JSONObject obj = new JSONObject(otp);
        String otpVal  = obj.getString("otp");
        ResetTokenModel otpDetails = resetTokenRepository.findByOtp(otpVal);

        logger.info("====== OTP Details ====="+otpDetails+" OTP is "+otpVal);
        ApiResponse response = new ApiResponse();
        if(otpDetails != null) {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime expiryTime = otpDetails.getExpiryDate();
            long expiryLeftSeconds = ChronoUnit.SECONDS.between(currentTime, expiryTime);

            logger.info("==== expiryLeftSeconds=====" + expiryLeftSeconds);
            if (expiryLeftSeconds > 0) {

                UserModel userModel = new UserModel();
                Optional<UserModel> userDetailsOp = authRepository.findById(otpDetails.getUser().getUserId());
                UserModel userDetails = userDetailsOp.get();

                CustomBeanUtils.copyCurrentUserDetailsToUserModel(userDetails, userModel);
                userModel.setUserId(userDetails.getUserId());
                String plainPassword = generateRandomPassword(8);
                String encodedPassword = passwordEncoder.encode(plainPassword);
                userModel.setPassword(encodedPassword);
                userModel.setModifiedBy(userDetails.getUserId().toString());
                userModel.setModifiedAt(LocalDateTime.now());

                UserModel savedData = authRepository.save(userModel);

                response.setStatus(HttpStatus.OK);
                response.setMsg(Constants.PASSWORD_UPDATED);

                logger.info("======== contact number is ===="+userDetails.getContactNo());

                String mobileNumber = userDetails.getContactNo();
                try {

                    String token = keycloakService.adminToken(KeycloakConstants.TOKEN);
                    ApiResponse resetKeycloakPassword = keycloakService.keycloakresetPassword(savedData, token, plainPassword);
                    logger.info(resetKeycloakPassword.getMsg() + resetKeycloakPassword.getData());

                    CommonUtil commonUtil = new CommonUtil();
                   // String name, String contactNumber, String applNo, String recordType, String templateType, String userName, String password, String reviewUrl
                   // ApiResponse msgResponse = CommonUtil.sendMessage(plainPassword, userDetails, mobileNumber, Constants.MSG_TYPE_FORGOT_PWD);
                    ApiResponse msgResponse = commonUtil.sendTextMessageForOtp(userDetails.getFirstName(), mobileNumber, "", "", Constants.FORGOT_PASSWORD, userDetails.getUserName(), plainPassword, "");

                    if(msgResponse != null){
                        response.setStatus(HttpStatus.OK);
                        response.setMsg(msgResponse.getMsg());
                    }
                }catch (Exception e){
                    logger.info(e.getMessage());
                }

            }
            else{
                response.setStatus(HttpStatus.BAD_REQUEST);
                response.setMsg(Constants.OTP_EXPIRED);
            }
        }else{
            response.setStatus(HttpStatus.BAD_REQUEST);
            response.setMsg(Constants.OTP_INVALID);
        }
        return response;
    }

    private ApiResponse createPasswordResetTokenForUser(UserModel userDetails, String token) {
        ResetTokenModel resetToken = new ResetTokenModel();
        ApiResponse res = new ApiResponse();
       // Date tokenExpiry = new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000);
        LocalDateTime tokenExpiry = LocalDateTime.now().plusMinutes(5);
        resetToken.setUser(userDetails);
        resetToken.setToken(token);
        resetToken.setExpiryDate(tokenExpiry);
       // ResetTokenModel myToken = new ResetTokenModel(token, userDetails);
        ResetTokenModel resetTokenModel = resetTokenRepository.save(resetToken);
        ApiResponse response = emailResetLink(token, userDetails);

        if(resetTokenModel != null) {
            logger.info("resetTokenModel"+resetTokenModel);
            logger.info("Email verify response"+response);
            res.setStatus(HttpStatus.OK);
            res.setMsg(response.getMsg());
        }
        return res;
    }

    private ApiResponse emailResetLink(String token, UserModel userDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        ApiResponse res = new ApiResponse();
        String url = Constants.PASSWORD_RESET_URL+token;
        String messageContent = url;
        message.setFrom(Constants.MAIL_FROM);
        message.setTo(userDetails.getEmail().trim().toLowerCase());
        message.setSubject(Constants.FORGOT_PWD_SUBJECT);
        message.setText(messageContent);
        javaMailSender.send(message);

        res.setStatus(HttpStatus.OK);
        res.setMsg(Constants.RESET_LINK_SENT + " " + userDetails.getEmail().trim().toLowerCase());
        // res.setMsg("==== Input value is is emailId "+insertedData);
        return res;
    }

    public String getChannelName(Long bndId, String userId, String channelName, String recordType) {
        String channel = "";

        if(!CommonUtil.checkNullOrBlank(channelName)){
            channel += channelName;
        }else{

        }
        return null;
    }

//    public static void main(String[] args){
//        String password = "Ndmc@1234";
//        PasswordEncoder passwordEncoder1 = new BCryptPasswordEncoder();
//        String encodedPassword = passwordEncoder1.encode(password);
//        System.out.println("encodedPassword ====>>>>>" +encodedPassword);
//    }

}


