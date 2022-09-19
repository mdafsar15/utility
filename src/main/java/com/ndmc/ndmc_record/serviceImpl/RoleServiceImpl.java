package com.ndmc.ndmc_record.serviceImpl;

import com.ndmc.ndmc_record.config.KeycloakConstants;
import com.ndmc.ndmc_record.dto.ApiResponse;
import com.ndmc.ndmc_record.dto.UserRoleDto;
import com.ndmc.ndmc_record.model.UserRoleModel;
import com.ndmc.ndmc_record.repository.UserRoleRepository;
import com.ndmc.ndmc_record.service.KeycloakApiService;
import com.ndmc.ndmc_record.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.transaction.Transactional;

@Service
public class RoleServiceImpl implements RoleService {

    private final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private UserRoleRepository roleRepository;
    @Autowired
    KeycloakApiService keycloakService;
    @Override
    @Transactional
    public UserRoleDto saveUserRole(UserRoleDto roleDto) {
        UserRoleModel roleModel = new UserRoleModel();


            BeanUtils.copyProperties(roleDto, roleModel);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            //setRegistrationDatetime(now);

            roleModel.setRoleName(roleDto.getRoleName().toUpperCase(Locale.ROOT));
            roleModel.setCreatedAt(now);
            roleModel.setModifiedAt(now);
            roleModel.setCreatedBy("Deepak Patel");
            roleModel.setModifiedBy("Deepak Patel");
            //  userModel.setPassword();
           BeanUtils.copyProperties(roleDto, roleModel);
            roleModel = roleRepository.save(roleModel);

        // Keycloak ADD-ROLE to Realm

        try {
            String token = keycloakService.adminToken(KeycloakConstants.TOKEN);
            ApiResponse KeycloakAddRole = keycloakService.addRole(roleDto, token);
            if(KeycloakAddRole.getData().toString().equalsIgnoreCase(KeycloakConstants.USER_CREATION_CODE)) {
                logger.info("Role created Successfully " +KeycloakAddRole);
            }else {

                throw new NullPointerException(KeycloakAddRole.getMsg().toString() + HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {

            logger.info("Role is not created : " +e);

        }
        return roleDto;
    }
}
