package com.ndmc.ndmc_record.service;

import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.JwtRequest;
import com.ndmc.ndmc_record.dto.UserDto;
import com.ndmc.ndmc_record.dto.UserRoleDto;
import com.ndmc.ndmc_record.model.UserModel;

public interface KeycloakApiService {

    public String adminToken(String url) throws IOException, Exception;
    public ApiResponse authToken(String url, JwtRequest jwtRequest) throws IOException;
    public ApiResponse createUser(UserModel userModel, String password, String token) throws IOException;
    public List<JSONObject> userMappedRoles(UserModel userModel, String token) throws IOException;
    public ApiResponse deleteAssignedRoles(String UserIDForRole, String id, String name, String token) throws IOException;
    public ApiResponse updateUser(UserModel userModel, String token) throws IOException;
    public ApiResponse addRole(UserRoleDto roleDto, String token) throws IOException;
    public String getUsernameForRole(UserModel userModel, String token) throws IOException;
    public List<JSONObject> getRoleDetail(UserModel userModel, String token) throws IOException;
    public ApiResponse assignRoleToUser(String UserIDForRole, String id, String name, String token) throws IOException;
    public ApiResponse keycloakresetPassword(UserModel userModel, String token, String newPassword) throws IOException;
    public ApiResponse searchByUserDetails(UserModel userModel, String token) throws IOException;

}
