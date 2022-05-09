package com.nsss.pizzamsmsatoppingbackend.service;

import com.nsss.pizzamsmsatoppingbackend.model.User;
import com.nsss.pizzamsmsatoppingbackend.request.GetUserRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    RestTemplate restTemplate;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        GetUserRequest getUserRequest = new GetUserRequest();

        getUserRequest.setUsername(username);

        User user = restTemplate.postForEntity("http://USER-SERVICE/auth/user/users", getUserRequest, User.class).getBody();

        return UserDetailsImpl.build(user);
    }
}
