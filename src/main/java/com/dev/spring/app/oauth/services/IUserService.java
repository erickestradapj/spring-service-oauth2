package com.dev.spring.app.oauth.services;

import com.dev.spring.app.commons.users.models.entity.User;

public interface IUserService {

    User findByUsername(String username);

    User update(User user, Long id);
}
