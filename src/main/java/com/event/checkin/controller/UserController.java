package com.event.checkin.controller;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.event.checkin.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
class ControllerResponse {
	private int code;
	private String result;
	private String authUrl;
}

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
class QRCodeControllerResponse {
	private String name;
}

@Entity
@Data
@AllArgsConstructor
class QRCodeRequest {
	private @Id Long id;
	private String data;
}

@RestController
@RequestMapping("/users")
public class UserController {

	@RequestMapping(value = "/check-in", method = RequestMethod.POST)
	public ResponseEntity<QRCodeControllerResponse> checkIn(@RequestBody QRCodeRequest request) {
		if(request.getData().equals("1234567890")) {
			return ResponseEntity.accepted().body(new QRCodeControllerResponse("John Doe"));
		}else {
			return ResponseEntity.badRequest().body(new QRCodeControllerResponse(null));
		}
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<ControllerResponse> login(@RequestBody User user) {
		return ResponseEntity.accepted().body(new ControllerResponse(0, "Logged In", "http://test"));
		/*
		User _user = repository.findByEmailAndPassword(user.getEmail(), user.getPassword());
		if (_user != null)
			return ResponseEntity.accepted().body(new ControllerResponse(0, "Logged In", _user.getAuthUrl()));
		else
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ControllerResponse(-1, "Invalid Credentials", null));
		*/			
	}

}
