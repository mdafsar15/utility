package com.ndmc.ndmc_record.security;

import com.ndmc.ndmc_record.dto.UserDto;
import com.ndmc.ndmc_record.dto.UserRoleDto;
import com.ndmc.ndmc_record.model.UserModel;
import com.ndmc.ndmc_record.model.UserRoleModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.SecondaryTable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipal implements UserDetails {


    private UserModel userDto;

    public UserPrincipal(UserModel userDto) {
        this.userDto = userDto;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<UserRoleModel> rolesId = this.userDto.getRoles();
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
//        for(UserRoleDto role: roles){
//            authorities.addAll()
//        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return userDto.getPassword();
    }

    @Override
    public String getUsername() {
        return userDto.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
