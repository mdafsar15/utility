package com.ndmc.ndmc_record.service;


import com.ndmc.ndmc_record.config.Constants;
import com.ndmc.ndmc_record.config.KeycloakConstants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.JwtRequest;
import com.ndmc.ndmc_record.dto.JwtResponse;
import com.ndmc.ndmc_record.model.ApplicationAccessControlModel;
import com.ndmc.ndmc_record.model.UserModel;
import com.ndmc.ndmc_record.repository.ApplicationAccessRepository;
import com.ndmc.ndmc_record.repository.AuthRepository;
import com.ndmc.ndmc_record.serviceImpl.AuthServiceImpl;
import com.ndmc.ndmc_record.serviceImpl.KeycloakApiServiceImpl;
import com.ndmc.ndmc_record.utils.CommonUtil;
import com.ndmc.ndmc_record.utils.JwtUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class JwtService implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(JwtService.class);
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    AuthServiceImpl authService;

    @Autowired
    KeycloakApiService keycloakService;

    @Autowired
    ApplicationAccessRepository applicationAccessRepository;

    public JwtResponse createJwtToken(JwtRequest jwtRequest) throws Exception {
        String userName = jwtRequest.getUserName();
        String userPassword = jwtRequest.getUserPassword();
        // logger.info("==== user name is === "+userName+"    and Password is ===="+userPassword);

        // keycloak Authentication call

        Optional<ApplicationAccessControlModel> applicationAccessControlModelOp = applicationAccessRepository.findById(1L);
        ApplicationAccessControlModel applicationAccessControlModel = applicationAccessControlModelOp.get();
        if(Constants.YES.equalsIgnoreCase(applicationAccessControlModel.getKeycloakAuth())) {
            if (!jwtRequest.getUserName().isEmpty() && !jwtRequest.getUserPassword().isEmpty()) {
                keycloakAuthenticate(jwtRequest);
            }
        }

        authenticate(userName, userPassword);
        boolean validatedIp = false;
        String newGeneratedToken = "";

        UserModel user = authService.findByUserName(userName);
        validatedIp = validateWithIpAddress(jwtRequest.getUserIp(), user);
        if (validatedIp) {

            UserDetails userDetails = loadUserByUsername(userName);
            newGeneratedToken = jwtUtil.generateToken(userDetails);
        } else {
            throw new UsernameNotFoundException("IP IS NOT VALIDATED: " + validatedIp);
        }

        keycloakUserCreation(user, userPassword);
        return new JwtResponse(user, newGeneratedToken);
    }

    private void keycloakAuthenticate(JwtRequest jwtRequest) {
        ApiResponse Response;
        try {

            Response = keycloakService.authToken(KeycloakConstants.TOKEN, jwtRequest);
            logger.info("GET DATA -- " +Response.getData());
            logger.info("GET MSG -- " +Response.getMsg() );
            if(Response.getMsg().equalsIgnoreCase(KeycloakConstants.KeycloakCode)) {
                String ActulResponse = Response.getData().toString();
                JSONObject object = new JSONObject(ActulResponse);
                String token = (String) object.get("access_token");
                logger.info("Inner Token == " + token);
            }else if(Response.getMsg().equalsIgnoreCase(KeycloakConstants.KeycloakUnauthorized)) {
                throw new BadCredentialsException(""+Response.getData());
            }else if(Response.getMsg().equalsIgnoreCase(KeycloakConstants.KeycloakCodeBadRequest)) {
                throw new BadCredentialsException("INVALID_CONFIGURATION_CLIENT_DETAILS" +Response.getData());
            }else if(Response.getMsg().equalsIgnoreCase(KeycloakConstants.KeycloakCodeNotFound)) {
                throw new BadCredentialsException("" +Response.getData());
            }
            else {throw new NullPointerException(" ERROR " +HttpStatus.INTERNAL_SERVER_ERROR);}
        } catch (IOException e) {
            throw new NullPointerException(e.getMessage() + HttpStatus.INTERNAL_SERVER_ERROR);

        }



    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // UserModel user = userDao.findById(username).get();
        UserModel user = authRepository.findByUserName(username);

        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                    user.getUserName(),
                    user.getPassword(),
                    getAuthority(user)
            );
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    private Boolean validateWithIpAddress(String requestIp, UserModel user) throws Exception {

        /**
         * Title: IP address Authentication
         * Description: To restrict CFC User with particular IP Address
         * Date: 12-05-2022
         * Author: Deepak Patel
         **/

        if (user != null) {

            if (!CommonUtil.checkNullOrBlank(user.getIpAddress())) {

                String[] existingIps = user.getIpAddress().split("#");
                boolean isIpExisted = false;
                for (String ip : existingIps) {
                    logger.info("===EXISTING IP===[" + ip + "]   USER IP [" + requestIp + "]" + user.getIpAddress().equals(ip.trim()));
                    if (requestIp.equals(ip.trim())) {
                        isIpExisted = true;
                        break;
                    }
                }
                return isIpExisted;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }


    private Set getAuthority(UserModel user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        });
        return authorities;
    }

    private void authenticate(String userName, String userPassword) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, userPassword));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    private void keycloakUserCreation(UserModel user, String userPassword) throws Exception {

        try {
            String token = keycloakService.adminToken(KeycloakConstants.TOKEN);
            ApiResponse keycloakQuery = keycloakService.searchByUserDetails(user, token);
            logger.info("----->>>>>> " + keycloakQuery.getMsg());
            if (keycloakQuery.getMsg().equals(KeycloakConstants.KeycloakFALSE)) {
                ApiResponse KeycloakUserCreation = keycloakService.createUser(user, userPassword, token);
                if (KeycloakUserCreation.getData().toString().equalsIgnoreCase(KeycloakConstants.USER_CREATION_CODE)) {
                    logger.info("User Created ====== " + KeycloakUserCreation.getData() + "-------" + KeycloakUserCreation.getMsg());
                    String UserIDForRole = keycloakService.getUsernameForRole(user, token);
                    List<JSONObject> ROLE_DETAIL = keycloakService.getRoleDetail(user, token);
                    logger.info("LIST----- " + ROLE_DETAIL);
                    for (int i = 0; i < ROLE_DETAIL.size(); i++) {
                        JSONObject data = (JSONObject) ROLE_DETAIL.get(i);
                        logger.info("Data --- " + data);
                        ApiResponse RoleApi = keycloakService.assignRoleToUser(UserIDForRole, data.get("id").toString(), data.get("name").toString(), token);
                        logger.info("Role Assigned ======= " + RoleApi.getMsg() + RoleApi.getData());
                    }

                }
                else {
                    throw new NullPointerException(KeycloakUserCreation.getMsg().toString() + HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        catch (Exception e) {
            logger.info("The User not Created: " + e);
        }

    }

}
