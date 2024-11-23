// package com.emsi.blog.auth;

// import com.emsi.blog.config.JwtService;
// import com.emsi.blog.user.Role;
// import com.emsi.blog.user.User;
// import com.emsi.blog.user.UserRepository;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;

// class AuthenticationServiceTest {

//     @Mock
//     private UserRepository userRepository;

//     @Mock
//     private PasswordEncoder passwordEncoder;

//     @Mock
//     private JwtService jwtService;

//     @Mock
//     private AuthenticationManager authenticationManager;

//     @InjectMocks
//     private AuthenticationService authenticationService;

//     @BeforeEach
//     void setUp() {
//         MockitoAnnotations.openMocks(this);
//     }

//     @Test
//     void testRegister() {
//         RegisterRequest request = new RegisterRequest("John", "Doe", "john.doe@example.com", "password");
//         User user = User.builder()
//                 .firstName("John")
//                 .lastName("Doe")
//                 .email("john.doe@example.com")
//                 .password("encodedPassword")
//                 .role(Role.USER)
//                 .build();

//         when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
//         when(userRepository.save(any(User.class))).thenReturn(user);
//         when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

//         AuthenticationResponse response = authenticationService.register(request);

//         assertEquals("jwtToken", response.getToken());
//     }

//     @Test
//     void testAuthenticate() {
//         AuthenticationRequest request = new AuthenticationRequest("john.doe@example.com", "password");
//         User user = User.builder()
//                 .firstName("John")
//                 .lastName("Doe")
//                 .email("john.doe@example.com")
//                 .password("encodedPassword")
//                 .role(Role.USER)
//                 .build();

//         when(authenticationManager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
//         when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

//         AuthenticationResponse response = authenticationService.authenticate(request);

//         assertEquals("jwtToken", response.getToken());
//     }
// }