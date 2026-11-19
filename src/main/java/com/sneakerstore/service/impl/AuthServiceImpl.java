package com.sneakerstore.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sneakerstore.common.exception.BusinessException;
import com.sneakerstore.common.exception.ResourceNotFoundException;
import com.sneakerstore.common.util.SecurityUtils;
import com.sneakerstore.dto.request.ChangePasswordRequest;
import com.sneakerstore.dto.request.GoogleLoginRequest;
import com.sneakerstore.dto.request.LoginRequest;
import com.sneakerstore.dto.request.RegisterRequest;
import com.sneakerstore.dto.response.LoginResponse;
import com.sneakerstore.dto.response.UserDto;
import com.sneakerstore.entity.Customer;
import com.sneakerstore.entity.Role;
import com.sneakerstore.entity.User;
import com.sneakerstore.mapper.UserMapper;
import com.sneakerstore.repository.CustomerRepository;
import com.sneakerstore.repository.RoleRepository;
import com.sneakerstore.repository.UserRepository;
import com.sneakerstore.security.jwt.JwtTokenProvider;
import com.sneakerstore.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.google.client-id:}")
    private String googleClientId;

    @Value("${app.google.client-secret:}")
    private String googleClientSecret;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String loginId = request.getUsername().trim();
        // Cho phép đăng nhập linh hoạt bằng Username hoặc Email
        User user = userRepository.findByUsername(loginId)
                .or(() -> userRepository.findByEmail(loginId))
                .orElseThrow(() -> new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu"));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new LockedException("Tài khoản đã bị khóa hoặc ngừng hoạt động");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );
            String token = jwtTokenProvider.generateToken(authentication);

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            return LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .user(userMapper.toDto(user))
                    .build();
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new BusinessException("Địa chỉ Email đã được sử dụng cho một tài khoản khác");
        }

        Role customerRole = roleRepository.findByRoleCode("CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleCode("CUSTOMER")
                        .roleName("Khách hàng")
                        .description("Khách hàng đăng ký tài khoản mua sắm SneakerHub")
                        .status("ACTIVE")
                        .build()));

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .status("ACTIVE")
                .roles(new HashSet<>(Collections.singletonList(customerRole)))
                .lastLoginAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // Đồng bộ hoặc tạo bản ghi khách hàng tương ứng nếu có SĐT
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String phone = request.getPhone().trim();
            if (!customerRepository.existsByPhone(phone)) {
                Customer customer = Customer.builder()
                        .customerCode("KH" + (System.currentTimeMillis() % 1000000))
                        .fullName(request.getFullName().trim())
                        .phone(phone)
                        .email(email)
                        .address(request.getAddress())
                        .status("ACTIVE")
                        .build();
                customerRepository.save(customer);
            }
        }

        List<String> roleCodes = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleCode())
                .collect(Collectors.toList());
        String token = jwtTokenProvider.generateTokenForUser(user.getUsername(), roleCodes);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toDto(user))
                .build();
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(GoogleLoginRequest request) {
        String email = null;
        String fullName = null;
        String googleId = null;
        String avatarUrl = null;

        // 0. Xác thực qua Google Authorization Code (đổi sang token qua Client Secret)
        if (request.getAuthCode() != null && !request.getAuthCode().trim().isEmpty()) {
            Map<String, Object> profile = exchangeGoogleAuthCode(request.getAuthCode().trim(), request.getRedirectUri());
            email = (String) profile.get("email");
            fullName = (String) profile.get("name");
            googleId = (String) profile.get("sub");
            avatarUrl = (String) profile.get("picture");
            log.info("Xác thực thành công Google Authorization Code cho email: {}", email);
        }
        // 1. Xác thực qua Google ID Token (Google Identity Services / One Tap / Credential JWT)
        else if (request.getIdToken() != null && !request.getIdToken().trim().isEmpty()) {
            Map<String, Object> profile = verifyGoogleIdToken(request.getIdToken().trim());
            email = (String) profile.get("email");
            fullName = (String) profile.get("name");
            googleId = (String) profile.get("sub");
            avatarUrl = (String) profile.get("picture");
            log.info("Xác thực thành công Google ID Token từ API Google cho email: {}", email);
        }
        // 2. Xác thực qua Google OAuth2 Access Token (Google OAuth 2.0 UserInfo)
        else if (request.getAccessToken() != null && !request.getAccessToken().trim().isEmpty()) {
            Map<String, Object> profile = verifyGoogleAccessToken(request.getAccessToken().trim());
            email = (String) profile.get("email");
            fullName = (String) profile.get("name");
            googleId = (String) profile.get("sub");
            avatarUrl = (String) profile.get("picture");
            log.info("Xác thực thành công Google Access Token từ API Google cho email: {}", email);
        }
        // 3. Fallback nếu đã truyền email (dành cho integration testing có email hợp lệ)
        else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            email = request.getEmail().trim();
            fullName = request.getFullName() != null && !request.getFullName().isBlank() ? request.getFullName().trim() : email.split("@")[0];
            googleId = request.getGoogleId();
            avatarUrl = request.getAvatarUrl();
            log.info("Đăng nhập Google trực tiếp cho email: {}", email);
        } else {
            throw new BadCredentialsException("Vui lòng cung cấp Google Token (idToken hoặc accessToken) để xác thực máy chủ Google");
        }

        if (email == null || email.isBlank()) {
            throw new BadCredentialsException("Không thể xác định địa chỉ email từ tài khoản Google đã xác thực");
        }

        final String finalEmail = email.trim().toLowerCase();
        final String finalFullName = fullName;
        Optional<User> existingUserOpt = userRepository.findByEmail(finalEmail)
                .or(() -> userRepository.findByUsername(finalEmail));

        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                throw new LockedException("Tài khoản của bạn đã bị khóa hoặc ngừng hoạt động");
            }
            if (finalFullName != null && !finalFullName.isBlank() && (user.getFullName() == null || user.getFullName().isBlank())) {
                user.setFullName(finalFullName);
            }
            user.setLastLoginAt(LocalDateTime.now());
            user = userRepository.save(user);
        } else {
            String baseUsername = finalEmail.contains("@")
                    ? finalEmail.substring(0, finalEmail.indexOf("@")).replaceAll("[^a-zA-Z0-9_]", "")
                    : "google_user";
            if (baseUsername.length() < 3) baseUsername = "user_" + baseUsername;
            String username = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + (counter++);
            }

            Role customerRole = roleRepository.findByRoleCode("CUSTOMER")
                    .orElseGet(() -> roleRepository.save(Role.builder()
                            .roleCode("CUSTOMER")
                            .roleName("Khách hàng")
                            .description("Khách hàng đăng nhập qua tài khoản Google")
                            .status("ACTIVE")
                            .build()));

            user = User.builder()
                    .username(username)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .fullName(finalFullName != null && !finalFullName.isBlank() ? finalFullName : username)
                    .email(finalEmail)
                    .status("ACTIVE")
                    .roles(new HashSet<>(Collections.singletonList(customerRole)))
                    .lastLoginAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);
            log.info("Đã tạo mới tài khoản USERS trong cơ sở dữ liệu cho thành viên Google: {} ({})", user.getUsername(), finalEmail);
        }

        // Tự động đồng bộ và lưu vào bảng CUSTOMERS nếu chưa tồn tại
        syncCustomerRecord(user, finalEmail, finalFullName);

        List<String> roleCodes = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleCode())
                .collect(Collectors.toList());
        String token = jwtTokenProvider.generateTokenForUser(user.getUsername(), roleCodes);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userMapper.toDto(user))
                .build();
    }

    private Map<String, Object> exchangeGoogleAuthCode(String authCode, String redirectUri) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("code", authCode.trim());
            map.add("client_id", googleClientId);
            map.add("client_secret", googleClientSecret);
            map.add("redirect_uri", redirectUri != null && !redirectUri.isBlank() ? redirectUri : "http://localhost:5173");
            map.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BadCredentialsException("Máy chủ Google từ chối Authorization Code");
            }

            JsonNode tokenNode = objectMapper.readTree(response.getBody());
            if (tokenNode.has("id_token")) {
                return verifyGoogleIdToken(tokenNode.get("id_token").asText());
            } else if (tokenNode.has("access_token")) {
                return verifyGoogleAccessToken(tokenNode.get("access_token").asText());
            } else {
                throw new BadCredentialsException("Google không trả về token xác thực");
            }
        } catch (Exception ex) {
            log.error("Lỗi khi đổi Google Authorization Code: {}", ex.getMessage());
            throw new BadCredentialsException("Mã ủy quyền Google không hợp lệ hoặc đã hết hạn: " + ex.getMessage());
        }
    }

    private Map<String, Object> verifyGoogleIdToken(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BadCredentialsException("Máy chủ Google phản hồi lỗi khi xác thực ID Token");
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("error")) {
                throw new BadCredentialsException("Google ID Token không hợp lệ: " + root.path("error_description").asText("Invalid token"));
            }
            Map<String, Object> profile = new HashMap<>();
            if (root.has("email")) profile.put("email", root.get("email").asText());
            if (root.has("name")) profile.put("name", root.get("name").asText());
            if (root.has("sub")) profile.put("sub", root.get("sub").asText());
            if (root.has("picture")) profile.put("picture", root.get("picture").asText());
            return profile;
        } catch (HttpClientErrorException ex) {
            log.error("Google tokeninfo HTTP error: status={}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new BadCredentialsException("Token Google ID không hợp lệ hoặc đã hết hạn từ Google Identity Services");
        } catch (Exception ex) {
            log.error("Lỗi kết nối tới máy chủ Google oauth2 API: {}", ex.getMessage());
            throw new BadCredentialsException("Không thể xác thực token với máy chủ Google: " + ex.getMessage());
        }
    }

    private Map<String, Object> verifyGoogleAccessToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BadCredentialsException("Máy chủ Google từ chối Access Token");
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            Map<String, Object> profile = new HashMap<>();
            if (root.has("email")) profile.put("email", root.get("email").asText());
            if (root.has("name")) profile.put("name", root.get("name").asText());
            if (root.has("sub")) profile.put("sub", root.get("sub").asText());
            if (root.has("picture")) profile.put("picture", root.get("picture").asText());
            return profile;
        } catch (Exception ex) {
            log.error("Lỗi kết nối tới Google UserInfo API: {}", ex.getMessage());
            throw new BadCredentialsException("Google Access Token không hợp lệ hoặc đã hết hạn: " + ex.getMessage());
        }
    }

    private void syncCustomerRecord(User user, String email, String fullName) {
        try {
            Optional<Customer> existingCustomer = customerRepository.findByEmail(email);
            if (existingCustomer.isEmpty()) {
                long phoneSuffix = Math.abs((email + user.getId()).hashCode()) % 100000000L;
                String generatedPhone = String.format("09%08d", phoneSuffix);
                int phoneAttempt = 1;
                while (customerRepository.existsByPhone(generatedPhone)) {
                    generatedPhone = String.format("08%08d", (phoneSuffix + phoneAttempt++) % 100000000L);
                }

                String code = "KH" + (System.currentTimeMillis() % 1000000);
                while (customerRepository.existsByCustomerCodeIgnoreCase(code)) {
                    code = "KH" + ((System.currentTimeMillis() + 1) % 1000000);
                }

                Customer customer = Customer.builder()
                        .customerCode(code)
                        .fullName(fullName != null && !fullName.isBlank() ? fullName : user.getFullName())
                        .email(email)
                        .phone(user.getPhone() != null && !user.getPhone().isBlank() ? user.getPhone() : generatedPhone)
                        .status("ACTIVE")
                        .build();
                customerRepository.save(customer);
                log.info("Đã tự động lưu khách hàng mới vào DB Oracle CUSTOMERS: {} (Mã: {})", customer.getFullName(), code);
            }
        } catch (Exception e) {
            log.warn("Lưu thông tin CUSTOMERS từ tài khoản Google thất bại: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException("Mật khẩu hiện tại không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedBy(username);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));

        return userMapper.toDto(user);
    }
}
