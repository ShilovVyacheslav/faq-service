package io.knowledgebase.demo.service.jwt.impl;

import io.knowledgebase.demo.entity.User;
import io.knowledgebase.demo.exception.AuthException;
import io.knowledgebase.demo.exception.UserException;
import io.knowledgebase.demo.repository.UserRepository;
import io.knowledgebase.demo.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
@RequiredArgsConstructor
public class SecurityUserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UserException, AuthException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User '%s' not found", username)));

        if (!user.getActive()) {
            throw AuthException.accessDenied();
        }

        log.debug("User details loaded successfully for: {}", username);

        return SecurityUser.builder().user(user).build();
    }

}
