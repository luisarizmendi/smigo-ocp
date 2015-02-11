package org.smigo.user;

/*
 * #%L
 * Smigo
 * %%
 * Copyright (C) 2015 Christian Nilsson
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.smigo.log.VisitLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

class OpenIdUserDetailsService implements AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

    public static final long NOT_SO_RANDOM_POINT_IN_TIME = 1411140042351l;
    @Autowired
    private UserHandler userHandler;
    @Autowired
    private LocaleResolver localeResolver;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserDetails(OpenIDAuthenticationToken token) throws UsernameNotFoundException {
        final List<UserDetails> userDetails = userDao.getUserDetails(token);
        if (userDetails.isEmpty()) {
            request.setAttribute(VisitLogger.NOTE_ATTRIBUTE, "createdUserFromOpenid");
            final RegisterFormBean newUser = new RegisterFormBean();
            newUser.setUsername("user" + String.valueOf(System.currentTimeMillis() - NOT_SO_RANDOM_POINT_IN_TIME));
            final Locale locale = localeResolver.resolveLocale(request);
            userHandler.createUser(newUser, token.getIdentityUrl(), locale);
            return userDao.getUserDetails(newUser.getUsername()).get(0);
        }
        return userDetails.get(0);
    }
}
