package org.smigo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smigo.entities.User;
import org.smigo.persitance.DatabaseResource;
import org.smigo.persitance.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Component
public class AuthenticationListener implements AuthenticationSuccessHandler, LogoutSuccessHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserSession userSession;
    @Autowired
    private DatabaseResource databaseResource;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("Login successful, user: " + authentication.getName());
        userSession.reloadSpecies();
        userSession.reloadGarden();

        final User principal = (User) authentication.getPrincipal();
        final Map<String, String> translation = databaseResource.getTranslation(principal.getId());
        userSession.getTranslation().putAll(translation);

        response.sendRedirect(request.getContextPath() + "/garden");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        log.info("Logout successful, user: " + authentication.getName());
        request.getSession().invalidate();
        response.sendRedirect(request.getContextPath() + "/hastalavista");
    }

}