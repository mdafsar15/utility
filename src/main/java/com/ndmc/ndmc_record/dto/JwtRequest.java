package com.ndmc.ndmc_record.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Transient;

public class JwtRequest {

    private String userName;
    private String userPassword;
    @JsonIgnore
    @Transient
    private String userIp;

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
}
