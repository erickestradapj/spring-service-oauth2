package com.dev.spring.app.oauth.services;

import com.dev.spring.app.commons.users.models.entity.User;
import com.dev.spring.app.oauth.clients.UserFeignClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService, UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        try {
            User user = userFeignClient.findByUsername(username);

            List<GrantedAuthority> authorities = user.getRoles()
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .peek(simpleGrantedAuthority -> logger.info("ROLE: " + simpleGrantedAuthority.getAuthority()))
                    .collect(Collectors.toList());

            logger.info("USER AUTHENTICATED: " + username);

            return new org.springframework.security.core.userdetails.User(user.getUsername()
                    , user.getPassword()
                    , user.getEnabled()
                    , true
                    , true
                    , true
                    , authorities);


        } catch (FeignException e) {
            String error = " ======> Error Login - USERNAME: " + username;
            logger.error(error);
            throw new UsernameNotFoundException(" ======> Error Login - USERNAME: " + username);
        }
    }

    @Override
    public User findByUsername(String username) {
        return userFeignClient.findByUsername(username);
    }

    @Override
    public User update(User user, Long id) {
        return userFeignClient.update(user, id);
    }
}
