package com.nsss.pizzamsmsacrustbackend.service;

import com.nsss.pizzamsmsacrustbackend.model.User;
import com.nsss.pizzamsmsacrustbackend.request.GetUserRequest;

// import com.nsss.pizzamsmsacrustbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    // @Autowired
    // UserRepository userRepository;

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
