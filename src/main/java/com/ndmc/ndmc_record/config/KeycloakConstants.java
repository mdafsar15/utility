package com.ndmc.ndmc_record.config;

import okhttp3.MediaType;

public class KeycloakConstants {

    // Config
    public static final String KEYCLOAK_SECRET = "8d3f00e3-290b-49c9-afba-ef715bc8c872";
    public static final String CLIENT_NAME = "ndmc-bnd";
    public static final String GRANT_TYPE = "password";
    public static final MediaType APPLICATION_JSON = MediaType.parse("application/json");
    public static final String CLIENT_ID = "27d4e5ef-b221-4ddd-a5bc-b87f0c30fe9f";

    //HEADERS
    public static final String Content_Type = "application/x-www-form-urlencoded";

    // Keycloak - Token Generation API
    public static final String TOKEN = "http://172.16.200.166:8080/auth/realms/realmBnd/protocol/openid-connect/token";
    public static final String USER_CREATION_STATUS = "Created";
    public static final String USER_CREATION_CODE = "201";
    public static final String USER_UPDATION_CODE = "204";

    // Keycloak - Base API
    public static final String BASE_API = "http://172.16.200.166:8080/auth/admin/realms/realmBnd";
    public static final String USER = "/users";
    public static final String USER_UPDATE = "/users/";
    public static final String USERNAME_ID = "/users/?username=";
    public static final String BASE_ROLE = "http://172.16.200.166:8080/auth/admin/realms/realmBnd";
    public static final String CLIENT_ROLE = "/clients/"+CLIENT_ID+"/";
    public static final String ROLE = "roles";
    public static final String GET_ROLE_DETAILS = "http://testndmc.com:8085/auth/admin/realms/hospital";
    public static final String ASSIGN_ROLE_TO_USER = "http://172.16.200.166:8080/auth/admin/realms/realmBnd/users/";
    public static final String ROLE_MAPPING = "/role-mappings/clients/"+CLIENT_ID;
    public static final String RESET_PASSWORD = "/reset-password";
    public static final String USER_ASSIGN_ROLE_DETAILS = "/role-mappings/clients/"+CLIENT_ID;

    public static final String KeycloakFALSE ="0";
    public static final String KeycloakOK = "OK";
    public static final String KeycloakCode = "200";
    public static final String KeycloakUnauthorized ="401";
    public static final String KeycloakBadRequest = "Bad Request";
    public static final String KeycloakCodeBadRequest = "400";
    public static final String KeycloakCodeNotFound = "404";
    public static final String UserCreationConflict = "409";

}

