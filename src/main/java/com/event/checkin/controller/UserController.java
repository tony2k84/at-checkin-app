package com.event.checkin.controller;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.event.checkin.model.QrCodeInfo;
import com.event.checkin.model.Role;
import com.event.checkin.model.RoleName;
import com.event.checkin.model.User;
import com.event.checkin.repository.RoleRepository;
import com.event.checkin.repository.UserRepository;
import com.event.checkin.security.JwtTokenProvider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
class CheckInRequest {
	private String data;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginRequest {
	private String username;
	private String password;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginResponse {
	private String token;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class ValidationResponse {
	private String name;
}

@RestController
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
    AuthenticationManager authenticationManager;
	@Autowired
    JwtTokenProvider tokenProvider;
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Secured("ROLE_USER")
	@RequestMapping(value = "/check-in", method = RequestMethod.POST)
	public ResponseEntity<?> checkIn(@RequestBody CheckInRequest request, Authentication authentication) {
		
		logger.debug("@checkIn");
		User _user = userRepository.findByUsername(authentication.getName()).get();
        QrCodeInfo qrCodeInfo = _user.getQrCodeInfo();
		
		RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", "Bearer 89f85d76-daaa-4977-8c29-5b11e57c9e35");
		headers.add("Authorization", "Bearer "+qrCodeInfo.getValidationToken());
        
		HttpEntity<CheckInRequest> requestUpdate = new HttpEntity<CheckInRequest>(request, headers);
		
		ResponseEntity<ValidationResponse> res = null;
		try {
			logger.debug("calling 3rd party checkin uri now..");

			res = restTemplate.exchange(qrCodeInfo.getValidationUri(),
        		HttpMethod.POST,
        		requestUpdate, 
        		ValidationResponse.class);
			
			logger.debug("status code"+res.getStatusCode());

	        if(res.getStatusCode() != HttpStatus.OK) {
	        	return ResponseEntity.badRequest().body("cant validate the qrcode");
	        }
		}catch(Exception e) {
			return ResponseEntity.badRequest().body("cant validate the qrcode");
		}
		return ResponseEntity.ok().body(res.getBody());
	}
	
	@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<String> register(@RequestBody User user) {
		if(userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("username is already registered");
        }
		Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
				.orElseThrow(
		        () -> new IllegalArgumentException("unable to map role"));
        user.setRoles(Collections.singleton(userRole));
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.getQrCodeInfo().setUser(user);
		userRepository.save(user);
		return ResponseEntity.ok().body("user registered");
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		request.getUsername(),
                		request.getPassword()
                )
        );
		
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        // generate token for QrCode via login
        User _user = userRepository.findByUsername(request.getUsername()).get();
        QrCodeInfo qrCodeInfo = _user.getQrCodeInfo();
        
        RestTemplate restTemplate = new RestTemplate();
        
        ResponseEntity<LoginResponse> res = restTemplate.postForEntity(
        		qrCodeInfo.getLoginUri(),
        		new LoginRequest(
        				qrCodeInfo.getLoginUser(), 
        				qrCodeInfo.getLoginPassword()
        			), 
        			LoginResponse.class);
        
        if(res.getStatusCode() == HttpStatus.OK) {
        	_user.getQrCodeInfo().setValidationToken(res.getBody().getToken());
        	userRepository.save(_user);
        }else {
        	return ResponseEntity.badRequest().body(new LoginResponse(null));
        }
        return ResponseEntity.ok(new LoginResponse(jwt));
	}
	@RequestMapping(value = "/admin/login", method = RequestMethod.POST)
	public ResponseEntity<LoginResponse> adminLogin(@RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                		request.getUsername(),
                		request.getPassword()
                )
        );
		
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new LoginResponse(jwt));
	}
}
