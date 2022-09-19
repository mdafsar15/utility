package com.ndmc.ndmc_record.service;

import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.UserModel;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AuthService {
    public ApiResponse saveUserRecords(UserDto userDto, HttpServletRequest request);
    public ApiResponse updateUserRecords(UserDto userDto, HttpServletRequest request);

    public ApiResponse getUserList();
    public String getUserIdFromRequest(HttpServletRequest request);

   public ApiResponse getUserRoles();

    public ApiResponse getUserRolesByType(String userType);
    public UserModel findByUserName(String userName);


    ApiResponse getFilteredUser(UserFilterDto userFilter, HttpServletRequest request);

    public ApiResponse resetPassword(PasswordChangeDto passwordResetDto, HttpServletRequest request);

   public ApiResponse forgotPassword(ForgotPasswordDto forgotPasswordDto);

    public ApiResponse verifyToken(String token);

    ApiResponse changePassword(PasswordResetDto passwordChangeDto);

    ApiResponse verifyOtp(String otp);

    ApiResponse resentForgotPassword(ForgotPasswordDto forgotPasswordDto);

    List<UserModel> findApproverUserDetailsUserId(String userId);

    ApiResponse getUserListByOrg(String orgId, HttpServletRequest request);
}
