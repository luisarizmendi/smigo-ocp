package org.smigo;

import org.smigo.entities.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CurrentUser {

    public boolean isAuthenticated() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public Integer getId() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    public User getUser() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    public Locale getLocale() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getLocale();
    }
}
