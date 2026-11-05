package com.matt1479.medicationorganizer.users;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {
    
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean isEnabled;
    private final Set<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.isEnabled = user.isEnabled();
        this.authorities = user.getRoles()
            .stream()
            .map((role) -> new SimpleGrantedAuthority("ROLE_" + role.name()))
            .collect(Collectors.toUnmodifiableSet());
    }

    public Long getId() {
        return id;
    }

    @Override 
    public String getUsername() {
        return email;
    }

    @Override 
    public String getPassword() {
        return passwordHash;
    }

    @Override 
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override 
    public boolean isEnabled() {
        return this.isEnabled;
    }

}
