package com.event.checkin.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class User {
	private @Id @GeneratedValue Long id;
	private String email;
	private String password;
	private String authUrl;
}
