package com.ndmc.ndmc_record.controller;


import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.dto.*;
import com.ndmc.ndmc_record.model.UserModel;
import com.ndmc.ndmc_record.repository.AuthRepository;
import com.ndmc.ndmc_record.repository.UserRoleRepository;
import com.ndmc.ndmc_record.service.AuthService;
import com.ndmc.ndmc_record.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/v1/user")
//@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private RoleService roleService;
    @PostMapping("/add")
    @PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
    public ApiResponse userRegistration(@RequestBody UserDto userDto, HttpServletRequest request){
       return  authService.saveUserRecords(userDto, request);
    }

    @PutMapping("/update")
    @PreAuthorize("hasRole('" + Constants.ROLE_ADMIN + "')")
    public ApiResponse userUpdattion(@RequestBody UserDto userDto, HttpServletRequest request){
        return authService.updateUserRecords(userDto, request);

    }

    @PostMapping("/add-role")
    public ResponseEntity<?> addUserRole(@RequestBody UserRoleDto roleDto){

        if(userRoleRepository.existsByRoleName(roleDto.getRoleName())){
            return new ResponseEntity<>("Role already existed", HttpStatus.BAD_REQUEST);
        }

            roleService.saveUserRole(roleDto);
            return new ResponseEntity<>("Role Added Successfully", HttpStatus.ACCEPTED);

    }


    @GetMapping("/list")
    public ApiResponse getUserList(){
        return authService.getUserList();
    }

    @GetMapping("/roles")
    public ApiResponse getUserRole(){
        return authService.getUserRoles();
    }

    @GetMapping("/roles/{userType}")
    public ApiResponse getUserRoleByType(@PathVariable ("userType") String userType){
        return authService.getUserRolesByType(userType);
    }

    @PostMapping("/filter")
    // @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "')")
    public ApiResponse getFilteredUserRecords(@Nullable @RequestBody UserFilterDto userFilter, HttpServletRequest request) throws Exception {
        return authService.getFilteredUser(userFilter,  request);
    }

    @PutMapping("password/reset")
    // @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
            + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_CREATOR
            + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse resetPassword(@Nullable @RequestBody PasswordChangeDto passwordResetDto, HttpServletRequest request) throws Exception {
        return authService.resetPassword(passwordResetDto,  request);
    }

    @PostMapping("password/forgot")
    // @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR', 'APPROVER')")
//    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
//            + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_CREATOR
//            + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse forgotPassword(@Nullable @RequestBody ForgotPasswordDto forgotPasswordDto) throws Exception {
        return authService.forgotPassword(forgotPasswordDto);
    }

    @PostMapping("password/forgot/resend")
    public ApiResponse resentForgotPassword(@Nullable @RequestBody ForgotPasswordDto forgotPasswordDto) throws Exception {
        return authService.resentForgotPassword(forgotPasswordDto);
    }
    @GetMapping("password/verify")
    public ApiResponse verifyToken(@RequestParam(value = "token", required = false) String token) throws Exception {
        return authService.verifyToken(token);
    }
    @PostMapping("verify/otp")
    public ApiResponse verifyOtp(@RequestBody String otp)  throws Exception {
        return authService.verifyOtp(otp);
    }


    @PutMapping("password/change")
    public ApiResponse changePassword(@Nullable @RequestBody PasswordResetDto passwordChangeDto) throws Exception {
        return authService.changePassword(passwordChangeDto);
    }

    @GetMapping("list/{orgId}")
//    @PreAuthorize("hasAnyRole('" + Constants.ROLE_ADMIN + "','" + Constants.ROLE_APPROVER + "', '"
//            + Constants.ROLE_CFC_APPROVER + "', '" + Constants.ROLE_CREATOR + "', '" + Constants.ROLE_CFC_CREATOR
//            + "', '" + Constants.ROLE_CFC_REGISTRAR + "')")
    public ApiResponse getUserListByOrg(@PathVariable(value="orgId", required = false) String orgId, HttpServletRequest request)  throws Exception {
        return authService.getUserListByOrg(orgId, request);
    }
}
