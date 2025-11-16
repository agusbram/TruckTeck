package ar.edu.iua.TruckTeck.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ar.edu.iua.TruckTeck.auth.model.User;


public class BaseRestController {
	protected User getUserLogged() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = (User) auth.getPrincipal();
		return user;
	}

}
