package issuetracker.service;

import issuetracker.dto.LoginRequest;
import issuetracker.dto.LoginResponse;
import issuetracker.dto.RegisterRequest;
import issuetracker.dto.UserResponse;
import issuetracker.model.Role;
import issuetracker.model.User;
import issuetracker.repository.RoleRepository;
import issuetracker.repository.UserRepository;
import issuetracker.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> SELF_REGISTER_ROLES = Set.of("STUDENT", "STAFF");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String roleName = (req.role() == null || req.role().isBlank())
                ? "STUDENT"
                : req.role().trim().toUpperCase();

        if (!SELF_REGISTER_ROLES.contains(roleName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Role must be STUDENT or STAFF");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Role not configured: " + roleName));

        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setAuthProvider("LOCAL");
        user.setRole(role);

        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getFirstName(), saved.getLastName(),
                saved.getEmail(), saved.getRole().getName());
    }

    public LoginResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .filter(User::isActive)
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        UserResponse profile = new UserResponse(user.getId(), user.getFirstName(),
                user.getLastName(), user.getEmail(), user.getRole().getName());
        return new LoginResponse(jwtService.generate(user), "Bearer",
                jwtService.expiresInSeconds(), profile);
    }
}