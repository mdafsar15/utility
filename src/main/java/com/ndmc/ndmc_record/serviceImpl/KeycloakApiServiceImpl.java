package com.ndmc.ndmc_record.serviceImpl;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.ndmc.ndmc_record.config.KeycloakConstants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.JwtRequest;
import com.ndmc.ndmc_record.dto.UserDto;
import com.ndmc.ndmc_record.dto.UserRoleDto;
import com.ndmc.ndmc_record.model.KeycloakModel;
import com.ndmc.ndmc_record.model.UserModel;
import com.ndmc.ndmc_record.repository.KeycloakAdminUserRepository;
import com.ndmc.ndmc_record.service.KeycloakApiService;
import com.ndmc.ndmc_record.utils.KeycloakAdminEncryptionKey;
import okhttp3.*;


@Service
public class KeycloakApiServiceImpl implements KeycloakApiService {

    private final Logger logger = LoggerFactory.getLogger(KeycloakApiServiceImpl.class);

    @Autowired
    public KeycloakAdminUserRepository keycloakAdminRepository;

    @Autowired
    public KeycloakAdminEncryptionKey keycloakAdminDecrypt;

    // Admin Auth Token
    public String adminToken(String url) throws IOException, Exception {

        // Fetching Keycloak Admin Password from Database
        Optional <KeycloakModel> credential = keycloakAdminRepository.findById(1L);
        KeycloakModel credentials = credential.get();
        String actualCred = credentials.getPassword();
        String Decrypt = keycloakAdminDecrypt.decrypt(actualCred);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Content-Type", KeycloakConstants.Content_Type)
                .add("client_id",KeycloakConstants.CLIENT_NAME)
                .add("username",credentials.getUsername())
                .add("password",Decrypt)
                .add("grant_type",KeycloakConstants.GRANT_TYPE)
                .add("client_secret",KeycloakConstants.KEYCLOAK_SECRET )
                .build();
        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", KeycloakConstants.Content_Type)
                .build();
        try(Response response = client.newCall(request).execute()) {
            String ActulResponse = response.body().string();
            JSONObject object = new JSONObject(ActulResponse);
            String token = (String) object.get("access_token");
            logger.info("Inner Token == " + token);
            return token;
        }

    }

    //Auth Token
    @Override
    public ApiResponse authToken(String url, JwtRequest jwtRequest) throws IOException {
        ApiResponse apiResponse = new ApiResponse();
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Content-Type", KeycloakConstants.Content_Type)
                .add("client_id",KeycloakConstants.CLIENT_NAME)
                .add("username",jwtRequest.getUserName())
                .add("password",jwtRequest.getUserPassword())
                .add("grant_type",KeycloakConstants.GRANT_TYPE)
                .add("client_secret",KeycloakConstants.KEYCLOAK_SECRET )
                .build();
        Request request = new Request.Builder().url(url)
                .post(body)
                .addHeader("Content-Type", KeycloakConstants.Content_Type)
                .build();
        Response response = client.newCall(request).execute();
        String ActulResponse = response.body().string();
        String ee =response.code()+"";
        logger.info("ACTUAL DATA -- " +ee);
        apiResponse.setMsg(ee);
        apiResponse.setData(ActulResponse);
        return apiResponse;
    }


    public ApiResponse createUser(UserModel userModel, String password, String token) throws IOException{
        ApiResponse apiresponse = new ApiResponse();
        OkHttpClient client = new OkHttpClient();
        logger.info("==KEYCLOAK PASSWORD REQUEST =="+password);
        String DETAILS = "{\"enabled\":true,\"username\":\""+userModel.getUserName()+"\",\"email\":\""+userModel.getEmail()+"\",\"firstName\":\""+userModel.getFirstName()+"\",\"lastName\":\""+userModel.getLastName()+"\",\"attributes\":{\"contactNo\":\""+userModel.getContactNo()+"\",\"Created-At\":\""+userModel.getCreatedAt()+"\",\"Created-By\":\""+userModel.getCreatedBy()+"\",\"Modified-At\":\""+userModel.getModifiedAt()+"\",\"Modified-By\":\""+userModel.getModifiedBy()+"\",\"Validity-Start\":\""+userModel.getValidityStart()+"\",\"Validity-End\":\""+userModel.getValidityEnd()+"\",\"status\":\""+userModel.getStatus()+"\",\"designation\":\""+userModel.getDesignation()+"\",\"employeeCode\":\""+userModel.getEmployeeCode()+"\",\"organizationID\":\""+userModel.getOrganizationId()+"\"},\"credentials\":[{\"type\":\"password\",\"value\":\""+password+"\",\"temporary\":false}]}";
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(KeycloakConstants.APPLICATION_JSON,DETAILS);
        Request request = new Request.Builder().url(KeycloakConstants.BASE_API+KeycloakConstants.USER)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try(Response response = client.newCall(request).execute()) {
            logger.info(" Password    : " +password + "   Checking user token " +token);
            apiresponse.setStatus(HttpStatus.OK);
            apiresponse.setData(response.code());
            apiresponse.setMsg(response.message());
            return apiresponse;
        }

    }

    public ApiResponse updateUser(UserModel userModel, String token) throws IOException{

        ApiResponse apiresponse = new ApiResponse();
        String userID = getUserbyUsername(userModel, token);
        OkHttpClient client = new OkHttpClient();
        String UPDATED_DETAILS = "{\"firstName\": \""+userModel.getFirstName()+"\",\"lastName\": \""+userModel.getLastName()+"\",\"email\": \""+userModel.getEmail()+"\",\"attributes\": {\"designation\": [\""+userModel.getDesignation()+"\"],\"Modified-At\": [\""+userModel.getModifiedAt()+"\"],\"Modified-By\": [\""+userModel.getModifiedBy()+"\"],\"contactNo\":\""+userModel.getContactNo()+"\",\"Created-At\":\""+userModel.getCreatedAt()+"\",\"Created-By\":\""+userModel.getCreatedBy()+"\",\"Validity-Start\":\""+userModel.getValidityStart()+"\",\"Validity-End\":\""+userModel.getValidityEnd()+"\",\"status\":\""+userModel.getStatus()+"\",\"employeeCode\":\""+userModel.getEmployeeCode()+"\",\"organizationID\": [\""+userModel.getOrganizationId()+"\"]}}";
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(KeycloakConstants.APPLICATION_JSON,UPDATED_DETAILS);
        Request request = new Request.Builder().url(KeycloakConstants.BASE_API+KeycloakConstants.USER_UPDATE+userID)
                .put(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try(Response response = client.newCall(request).execute()){

            logger.info("User Update Token === "+token);
            apiresponse.setStatus(HttpStatus.OK);
            apiresponse.setData(response.code());
            apiresponse.setMsg(response.message());
            return apiresponse;
        }

    }
    public String getUserbyUsername(UserModel userModel, String token) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_API+KeycloakConstants.USERNAME_ID+userModel.getUserName()+"")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try(Response response = client.newCall(request).execute()){
            String Aresponse =  response.body().string();
            logger.info(Aresponse);
            JSONArray newobj = new JSONArray(Aresponse);
            logger.info("New Object" + newobj);
            JSONObject object = (JSONObject) newobj.get(0);
            String id = (String) object.get("id");
            logger.info("New Object with id" + id);
            String user = (String) object.get("id");
            return user;

        }
    }

    public ApiResponse addRole(UserRoleDto roleDto, String token) throws IOException {
        ApiResponse apiresponse = new ApiResponse();
        OkHttpClient client = new OkHttpClient();
        String ADD_REALM_ROLE = "{\"name\": \""+roleDto.getRoleName()+"\",\"composite\": false,\"clientRole\": true,\"containerId\": \""+KeycloakConstants.CLIENT_ID+"\",\"attributes\": {\"type\":[\""+roleDto.getType()+"\"],\"roleLabel\":[\""+roleDto.getRoleLabel()+"\"]},\"description\": \""+roleDto.getRoleLabel()+"\"}";
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(KeycloakConstants.APPLICATION_JSON,ADD_REALM_ROLE);
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_ROLE+KeycloakConstants.CLIENT_ROLE+KeycloakConstants.ROLE)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try(Response response = client.newCall(request).execute()){
            logger.info("Role Update Token === "+token);
            apiresponse.setStatus(HttpStatus.OK);
            apiresponse.setData(response.code());
            apiresponse.setMsg(response.message());
            return apiresponse;
        }

    }
    @Override
    public String getUsernameForRole(UserModel userModel, String token) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_API+KeycloakConstants.USERNAME_ID+userModel.getUserName()+"")
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try(Response response = client.newCall(request).execute()){
            String Aresponse =  response.body().string();
            logger.info(Aresponse);
            JSONArray newobj = new JSONArray(Aresponse);
            logger.info("New Object" + newobj);
            JSONObject object = (JSONObject) newobj.get(0);
            String id = (String) object.get("id");
            logger.info("New Object with id" + id);
            String user = (String) object.get("id");
            return user;
        }
    }

    @Override
    public List<JSONObject> getRoleDetail(UserModel userModel, String token) throws IOException {
        List<JSONObject> list = new ArrayList<JSONObject>();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        List<Object> newList = userModel.getRoles().stream().map(x -> x.getRoleName()).collect(Collectors.toList());
        System.out.println("USER MODEL ROLES ------ " +userModel.getRoles());  //[APPROVER]
        JSONArray newobj = new JSONArray(newList);
        logger.info("JSON ARRAY --- " + newobj);
//	  	String cdl = CDL.rowToString(newobj);
        logger.info("New Object" + newobj.get(0));
        for(int i=0; i<newobj.length(); i++) {
            Request request = new Request.Builder()
                    .url(KeycloakConstants.BASE_API+KeycloakConstants.CLIENT_ROLE+KeycloakConstants.ROLE+"/"+newobj.get(i))
                    .method("GET", null)
                    .addHeader("Authorization", "Bearer "+token)
                    .build();
            try(Response response = client.newCall(request).execute()){
                String ActulResponse = response.body().string();
                JSONObject data = new JSONObject(ActulResponse);
                list.add(data);
                logger.info("RESPONSE ------ getRoleDetail "  +data);

            }
        }
        return list;



    }

    @Override
    public ApiResponse assignRoleToUser(String UserIDForRole, String id, String name, String token) throws IOException {
        ApiResponse apiresponse = new ApiResponse();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String RoleBody = "[{\"id\": \""+id+"\",\"name\": \""+name+"\",\"composite\": false,\"clientRole\": true,\n\"containerId\": \"27d4e5ef-b221-4ddd-a5bc-b87f0c30fe9f\"}]";
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(KeycloakConstants.APPLICATION_JSON,RoleBody);
        Request request = new Request.Builder()
                .url(KeycloakConstants.ASSIGN_ROLE_TO_USER+UserIDForRole+KeycloakConstants.ROLE_MAPPING)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        Response response = client.newCall(request).execute();
        apiresponse.setData(response.code());
        apiresponse.setMsg(response.message());
        return apiresponse;
    }

    @Override
    public ApiResponse keycloakresetPassword(UserModel userModel, String token, String newPassword) throws IOException {
        ApiResponse apiresponse = new ApiResponse();
        String userID = getUserbyUsername(userModel, token);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String Password = "{\"type\":\"password\",\"value\":\""+newPassword+"\",\"temporary\":false\n}";
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(KeycloakConstants.APPLICATION_JSON,Password);
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_API+KeycloakConstants.USER+"/"+userID+KeycloakConstants.RESET_PASSWORD)
                .method("PUT", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        Response response = client.newCall(request).execute();
        apiresponse.setData(response.code());
        apiresponse.setMsg(response.message());
        return apiresponse;
    }

    @Override
    public List<JSONObject> userMappedRoles(UserModel userModel, String token) throws IOException {
        String userID = getUserbyUsername(userModel, token);
        List<JSONObject> list = new ArrayList<JSONObject>();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_API+KeycloakConstants.USER_UPDATE+userID+KeycloakConstants.USER_ASSIGN_ROLE_DETAILS)
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        Response response = client.newCall(request).execute();
        String ActulResponse = response.body().string();
        JSONArray data = new JSONArray(ActulResponse);
        list = fetchRoleDetails(data);
        logger.info("LISTTTTTT--------- " +list);
        return list;
    }

    private List<JSONObject> fetchRoleDetails(JSONArray data) {
        List<JSONObject> list = new ArrayList<>();
        if(data != null) {
            for(int i=0; i<data.length(); i++) {
                JSONObject fetchData = data.getJSONObject(i);
                JSONObject lastResponse = new JSONObject();
                String id = fetchData.getString("id");
                String name = fetchData.getString("name");
                lastResponse.put("id", id);
                lastResponse.put("name", name);
                list.add(lastResponse);
            }
        }

        return list;
    }

    @Override
    public ApiResponse deleteAssignedRoles(String UserIDForRole, String id, String name, String token) throws IOException {
        ApiResponse apiresponse = new ApiResponse();
        logger.info("DELETE USER ID ---- " +UserIDForRole);
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        String DeleteRole = "[{\"id\": \""+id+"\",\"name\": \""+name+"\",\"composite\": false,\"clientRole\": true,\"containerId\": \"27d4e5ef-b221-4ddd-a5bc-b87f0c30fe9f\"}]";
        @SuppressWarnings("deprecation")
        RequestBody body = RequestBody.create(KeycloakConstants.APPLICATION_JSON,DeleteRole);
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_API+KeycloakConstants.USER_UPDATE+UserIDForRole+KeycloakConstants.USER_ASSIGN_ROLE_DETAILS)
                .method("DELETE", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        Response response = client.newCall(request).execute();
        apiresponse.setData(response.code());
        apiresponse.setMsg(response.message());
        return apiresponse;

    }

    @Override
    public ApiResponse searchByUserDetails(UserModel userModel, String token) throws IOException {
        ApiResponse api = new ApiResponse();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(KeycloakConstants.BASE_API+KeycloakConstants.USER+
                        "/?username="+userModel.getUserName())
                .method("GET", null)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer "+token)
                .build();
        try(Response response = client.newCall(request).execute()){
            String Aresponse =  response.body().string();
            JSONArray newobj = new JSONArray(Aresponse);
            String A = String.valueOf(newobj.length());
            api.setMsg(A);
            return api;
        }




    }



}
