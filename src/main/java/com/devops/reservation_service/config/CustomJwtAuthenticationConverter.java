package com.devops.reservation_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;


@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final KeycloakRolesMapper keycloakRolesMapper;

    @Autowired
    public CustomJwtAuthenticationConverter(KeycloakRolesMapper keycloakRolesMapper) {
        this.keycloakRolesMapper = keycloakRolesMapper;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return keycloakRolesMapper.mapAuthorities(jwt);
    }
}
