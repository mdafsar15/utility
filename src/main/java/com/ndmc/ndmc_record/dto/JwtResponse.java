package com.ndmc.ndmc_record.dto;

import com.ndmc.ndmc_record.model.UserModel;

public class JwtResponse {

    private UserModel user;
    private String jwtToken;

    public JwtResponse(UserModel user, String jwtToken) {
        this.user = user;
        this.jwtToken = jwtToken;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }
}
