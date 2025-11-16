package ar.edu.iua.TruckTeck.auth.controller;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import ar.edu.iua.TruckTeck.auth.model.User;
import ar.edu.iua.TruckTeck.auth.custom.CustomAuthenticationManager;
import ar.edu.iua.TruckTeck.auth.filters.AuthConstants;
import ar.edu.iua.TruckTeck.controllers.BaseRestController;
import ar.edu.iua.TruckTeck.controllers.Constants;
import ar.edu.iua.TruckTeck.util.IStandardResponseBusiness;
//import ar.edu.iw3.auth.event.UserEvent;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controlador REST encargado de gestionar la autenticación de usuarios y el manejo
 * de tokens JWT dentro del sistema.
 * <p>
 * Proporciona endpoints para iniciar sesión y para encriptar contraseñas,
 * utilizando los mecanismos de autenticación de Spring Security.
 * </p>
 * 
 * <p><b>Autor:</b> Equipo IW3 - Universidad Argentina</p>
 */
@RestController
public class AuthRestController extends BaseRestController {
    /** 
     * Manejador de autenticación proporcionado por Spring Security.
     */
	@Autowired
	private AuthenticationManager authManager;

    /** 
     * Componente de negocio encargado de generar respuestas estándar.
     */
	@Autowired
	private IStandardResponseBusiness response;
	
    /** 
     * Publicador de eventos de la aplicación (para disparar eventos de usuario, etc.).
     */
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;


    /**
     * Endpoint para autenticar un usuario y devolver un token JWT.
     * <p>
     * Este método valida las credenciales del usuario utilizando el
     * {@link AuthenticationManager}. En caso de éxito, genera un token JWT
     * con la información del usuario (nombre, roles, email, etc.).
     * </p>
     * 
     * @param username Nombre de usuario proporcionado.
     * @param password Contraseña del usuario.
     * @param request  Objeto {@link HttpServletRequest} con información de la petición.
     * @return {@link ResponseEntity} que contiene:
     *         <ul>
     *             <li>El token JWT en caso de autenticación exitosa (HTTP 200).</li>
     *             <li>Un mensaje de error en caso de credenciales inválidas (HTTP 401).</li>
     *             <li>Un mensaje de error interno si ocurre una excepción (HTTP 500).</li>
     *         </ul>
     */
	@PostMapping(value = Constants.URL_LOGIN, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> loginExternalOnlyToken(@RequestParam String username, @RequestParam String password, HttpServletRequest request) {
		Authentication auth = null;
		try {
			auth = authManager.authenticate(((CustomAuthenticationManager) authManager).authWrap(username, password));
		} catch (AuthenticationServiceException e0) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e0, e0.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (AuthenticationException e) {
			return new ResponseEntity<>(response.build(HttpStatus.UNAUTHORIZED, e, e.getMessage()),
					HttpStatus.UNAUTHORIZED);
		}

		User user = (User) auth.getPrincipal();

        // Creación del token JWT con la información del usuario
		String token = JWT.create().withSubject(user.getUsername())
				.withClaim("internalId", user.getIdUser())
				.withClaim("roles", new ArrayList<String>(user.getAuthoritiesStr())).withClaim("email", user.getEmail())
				.withClaim("version", "1.0.0")
				.withExpiresAt(new Date(System.currentTimeMillis() + AuthConstants.EXPIRATION_TIME))
				.sign(Algorithm.HMAC512(AuthConstants.SECRET.getBytes()));

        // Publicación opcional de evento de login
		//applicationEventPublisher.publishEvent(new UserEvent(user, request, UserEvent.TypeEvent.LOGIN));

		return new ResponseEntity<String>(token, HttpStatus.OK);
	}

    /**
     * Codificador de contraseñas utilizado para aplicar hashing seguro (por ejemplo, BCrypt).
     */
	@Autowired
	private PasswordEncoder pEncoder;

    /**
     * Endpoint de demostración para encriptar una contraseña.
     * <p>
     * Este método permite verificar el funcionamiento del codificador de contraseñas
     * configurado en el sistema. No debe utilizarse en producción.
     * </p>
     * 
     * @param password Contraseña en texto plano a encriptar.
     * @return {@link ResponseEntity} que contiene:
     *         <ul>
     *             <li>La contraseña encriptada (HTTP 200).</li>
     *             <li>Un mensaje de error en caso de excepción (HTTP 500).</li>
     *         </ul>
     */
	@GetMapping(value = "/demo/encodepass", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> encodepass(@RequestParam String password) {
		try {
			return new ResponseEntity<String>(pEncoder.encode(password), HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(response.build(HttpStatus.INTERNAL_SERVER_ERROR, e, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}