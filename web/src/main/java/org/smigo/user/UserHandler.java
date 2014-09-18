package org.smigo.user;

import kga.PlantData;
import org.smigo.config.Props;
import org.smigo.persitance.DatabaseResource;
import org.smigo.plants.PlantDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class UserHandler {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private UserSession userSession;
    @Autowired
    private DatabaseResource databaseResource;
    @Autowired
    protected AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MailSender javaMailSender;
    @Autowired
    private PersistentTokenRepository tokenRepository;
    @Autowired
    private PlantDao plantDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private Props props;

    private final Map<String, String> resetMap = new ConcurrentHashMap<String, String>();

    public void createUser(RegisterFormBean user, String identityUrl) {
        final int userId = createUser(user);
        userDao.addOpenId(userId, identityUrl);
    }

    public int createUser(RegisterFormBean user) {
        long decideTime = System.currentTimeMillis() - request.getSession().getCreationTime();
        final String rawPassword = user.getPassword();
        final String encoded = rawPassword.isEmpty() ? "" : passwordEncoder.encode(rawPassword);
        final int userId = userDao.addUser(user, encoded, decideTime);

        //save plants
        List<PlantData> plants = userSession.getPlants();
        if (!plants.isEmpty()) {
            plantDao.addPlants(plants, userId);
        }
        return userId;
    }

    public void authenticateUser(String username, String password) {
        //set authentication
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

        token.setDetails(new WebAuthenticationDetails(request));
        Authentication authenticatedUser = authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
    }

    public void updatePassword(String username, String newPassword) {
        final String encodedPassword = passwordEncoder.encode(newPassword);
        databaseResource.updatePassword(username, encodedPassword);
        tokenRepository.removeUserTokens(username);
    }

    public void sendResetPasswordEmail(String email) {
        final String id = UUID.randomUUID().toString();
        resetMap.put(id, email);

        final TimerTask removeTask = new TimerTask() {
            @Override
            public void run() {
                resetMap.remove(id);
            }
        };

        new Timer().schedule(removeTask, TimeUnit.MINUTES.toMillis(15));

        final SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Smigo reset password");
        simpleMailMessage.setText("Click link to reset password. " + props.getResetUrl() + id);
        javaMailSender.send(simpleMailMessage);
    }

    public void authenticateUser(String loginKey) {
        final String email = resetMap.get(loginKey);
        resetMap.remove(loginKey);
        final UserBean user = userDao.getUserByEmail(email);
        final String rawTempPassword = UUID.randomUUID().toString();
        final String encodedTempPassword = passwordEncoder.encode(rawTempPassword);
        //TODO: Replace ugly hack for authenticating user with proper implementation.
        databaseResource.updatePassword(user.getUsername(), encodedTempPassword);
        authenticateUser(user.getUsername(), rawTempPassword);
        databaseResource.updatePassword(user.getUsername(), "");
    }

    public void acceptTermsOfService(AuthenticatedUser principal) {
        UserBean user = userDao.getUser(principal.getUsername());
        user.setTermsofservice(true);
        userDao.updateUser(principal.getId(), user);
    }

    public UserBean getUser(AuthenticatedUser user) {
        if (user == null) {
            return null;
        }
        return userDao.getUser(user.getUsername());
    }

    public AuthenticatedUser getCurrentUser() {//@AuthenticationPrincipal
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof AuthenticatedUser) {
            return (AuthenticatedUser) principal;
        }
        return null;
    }
}
