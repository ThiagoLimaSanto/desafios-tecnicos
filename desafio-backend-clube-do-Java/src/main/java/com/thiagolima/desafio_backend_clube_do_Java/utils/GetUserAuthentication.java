package com.thiagolima.desafio_backend_clube_do_Java.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.thiagolima.desafio_backend_clube_do_Java.model.User;

public class GetUserAuthentication {

    public static User getUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
