package com.dev.spring.app.oauth.security.event;

import com.dev.spring.app.commons.users.models.entity.User;
import com.dev.spring.app.oauth.services.IUserService;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessErrorHandler implements AuthenticationEventPublisher {

    private Logger logger = LoggerFactory.getLogger(AuthenticationSuccessErrorHandler.class);

    @Autowired
    private IUserService userService;

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {

        if (authentication.getDetails() instanceof WebAuthenticationDetails) {
            return;
        }

        UserDetails user = (UserDetails) authentication.getPrincipal();
        String message = "Success Login: " + user.getUsername();
        System.out.println(message);
        logger.info(message);

        User u = userService.findByUsername(authentication.getName());

        if (u.getAttempts() != null && u.getAttempts() > 0) {
            u.setAttempts(0);
            userService.update(u, u.getId());
        }
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException e, Authentication authentication) {
        String message = "Error Login: " + e.getMessage();
        logger.error(message);
        System.out.println(message);

        try {
            StringBuilder errors = new StringBuilder();
            errors.append(message);

            User user = userService.findByUsername(authentication.getName());

            if (user.getAttempts() == null) {
                user.setAttempts(0);
            }

            logger.info("===> Attempts - currently: " + user.getAttempts());
            user.setAttempts(user.getAttempts() + 1);
            logger.info("===> Attempts - after: " + user.getAttempts());

            errors.append("Attempts Login: ").append(user.getAttempts());

            if (user.getAttempts() >= 3) {
                String errorMax = String.format("User %s  disabled ", user.getUsername());

                logger.error(errorMax);
                errors.append(" - ").append(errorMax);
                user.setEnabled(false);
            }

            userService.update(user, user.getId());

        } catch (FeignException exception) {
            logger.error(String.format("User %s doesn't exist: ", authentication.getName()));
        }
    }
}
