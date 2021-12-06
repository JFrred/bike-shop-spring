package com.example.service.impl;

import com.example.dto.RegisterRequest;
import com.example.exception.InvalidPasswordException;
import com.example.exception.UsernameTakenException;
import com.example.mapper.UserViewMapper;
import com.example.model.User;
import com.example.repository.CartRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtTokenService;
import com.example.service.MessageSender;
import com.example.validation.RegisterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageSender messageSender;
    @Mock
    private RegisterValidator registerValidator;
    @Mock
    private UserViewMapper userViewMapper;
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private AuthServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerWithValidRequest_ThenOK() {
        RegisterRequest request = new RegisterRequest();
        User user = new User();
        given(userViewMapper.mapRegisterRequestToUser(request)).willReturn(user);
        given(userRepository.save(any())).willReturn(user);
        given(jwtTokenService.generateAccessToken(any())).willReturn("test_access_token");

        service.register(request);

        verify(registerValidator).validateRequest(request);
        verify(userViewMapper).mapRegisterRequestToUser(request);
        verify(jwtTokenService).generateAccountActivationToken(user);
        verify(messageSender).sendMessage(any());
    }

    @Test
    void registerWithUsernameTaken_ThenThrowUsernameTakenException() {
        RegisterRequest request = new RegisterRequest();
        doThrow(UsernameTakenException.class).when(registerValidator).validateRequest(any());

        Exception exception = assertThrows(UsernameTakenException.class,
                () -> service.register(request));

        assertThat(exception).isInstanceOf(UsernameTakenException.class);
        verify(registerValidator).validateRequest(request);

        verify(userViewMapper, never()).mapRegisterRequestToUser(request);
        verify(jwtTokenService, never()).generateAccountActivationToken(any());
        verify(messageSender, never()).sendMessage(any());
    }

    @Test
    void registerWithInvalidPassword_ThenThrowInvalidPasswordException() {
        RegisterRequest request = new RegisterRequest();
        doThrow(InvalidPasswordException.class).when(registerValidator).validateRequest(any());

        Exception exception = assertThrows(InvalidPasswordException.class,
                () -> service.register(request));

        assertThat(exception).isInstanceOf(InvalidPasswordException.class);
        verify(registerValidator).validateRequest(request);

        verify(userViewMapper, never()).mapRegisterRequestToUser(request);
        verify(jwtTokenService, never()).generateAccountActivationToken(any());
        verify(messageSender, never()).sendMessage(any());

    }

}