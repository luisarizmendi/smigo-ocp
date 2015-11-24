package org.smigo.user.springsocial;

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

import org.smigo.user.AuthenticatedUser;
import org.smigo.user.UserBean;
import org.smigo.user.UserDao;
import org.smigo.user.UserHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class AddUserConnectionSignUp implements ConnectionSignUp {

    @Autowired
    private UserHandler userHandler;
    @Autowired
    private UserDao userDao;

    @Override
    public String execute(Connection<?> connection) {
        final String email = connection.fetchUserProfile().getEmail();
        final AuthenticatedUser user = getUserDetails(email);

        final UserBean userBean = userDao.getUser(user.getUsername());
        userBean.setEmail(email);
        userBean.setDisplayName(connection.fetchUserProfile().getName());
        userHandler.updateUser(user.getId(), userBean);

        return String.valueOf(user.getId());
    }

    private AuthenticatedUser getUserDetails(String email) {
        final List<UserDetails> userByEmail = userDao.getUserByEmail(email);
        if (userByEmail.isEmpty()) {
            return userHandler.createUser();
        }
        return (AuthenticatedUser) userByEmail.iterator().next();
    }
}
