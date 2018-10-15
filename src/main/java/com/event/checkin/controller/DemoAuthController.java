package com.event.checkin.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/demo")
public class DemoAuthController {

	private static final Logger logger = LoggerFactory.getLogger(DemoAuthController.class);
	
	@RequestMapping(value = "/check-in", method = RequestMethod.POST)
	public ResponseEntity<ValidationResponse> checkIn(@RequestBody CheckInRequest request) {
		logger.debug("returning dummy checkin success");
		return ResponseEntity.ok().body(new ValidationResponse("John Doe"));
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<String> login() {
		logger.debug("returning dummy token");
        return ResponseEntity.ok(UUID.randomUUID().toString());
	}
}
