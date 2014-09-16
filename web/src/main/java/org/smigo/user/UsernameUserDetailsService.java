package org.smigo.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsernameUserDetailsService implements UserDetailsService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final List<UserDetails> userDetails = userDao.getUserDetails(username);
        if (userDetails.isEmpty()) {
            throw new UsernameNotFoundException("User not found:" + username);
        }
        return userDetails.get(0);
    }
}
