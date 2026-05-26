package com.locmns.security;

import com.locmns.model.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Getter
public class AppUserDetails implements UserDetails {

    // Encapsule AppUser sans le modifier
    protected AppUser user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Le role vient du profil : ROLE_GESTIONNAIRE, ROLE_COLLABORATEUR, etc.
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getProfil().getType().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
