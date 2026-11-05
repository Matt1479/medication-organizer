package com.matt1479.medicationorganizer.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    public Long getId() {
        JwtAuthenticationToken authenticationToken =
            (JwtAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        return Long.valueOf(authenticationToken.getToken().getSubject());
    }

}
