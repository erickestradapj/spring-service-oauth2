package com.dev.spring.app.oauth.clients;

import com.dev.spring.app.commons.users.models.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "service-users")
public interface UserFeignClient {

    @GetMapping("/users/search/search-username")
    User findByUsername(@RequestParam String username);

    @PutMapping("/users/{id}")
    User update(@RequestBody User user, @PathVariable Long id);
}
