package com.company.eams.security;

import com.company.eams.entity.User;
import com.company.eams.entity.enums.RoleType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;

    @JsonIgnore
    private final String password;

    private final RoleType role;
    private final Set<String> permissions;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> permissionCodes = new HashSet<>();

        if (user.getRole() != null) {
            // Add Spring Security standard Role authority with "ROLE_" prefix
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().name()));

            // Add individual granular permissions (e.g. USER_CREATE, ASSET_ASSIGN)
            if (user.getRole().getPermissions() != null) {
                permissionCodes = user.getRole().getPermissions().stream()
                        .map(p -> p.getCode())
                        .collect(Collectors.toSet());

                permissionCodes.forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));
            }
        }

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .role(user.getRole() != null ? user.getRole().getName() : null)
                .permissions(permissionCodes)
                .active(Boolean.TRUE.equals(user.getIsActive()))
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
