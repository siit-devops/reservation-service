package com.devops.reservation_service.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class KeycloakRolesMapper {
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";


    public Collection<GrantedAuthority> mapAuthorities(Jwt jwt) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        try {
            String rolesStr = jwt.getClaimAsMap(REALM_ACCESS_CLAIM).get(ROLES_CLAIM).toString();
            String role = rolesStr.substring(rolesStr.indexOf("[") + 1, rolesStr.lastIndexOf("]"));
            mappedAuthorities.add(new SimpleGrantedAuthority(role));

            return mappedAuthorities;
        } catch (NullPointerException e) {
            return new HashSet<>();
        }
    }
}
