package ar.edu.iua.TruckTeck.auth.custom;

import java.util.Collection;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import ar.edu.iua.TruckTeck.model.business.exceptions.BusinessException;
import ar.edu.iua.TruckTeck.model.business.exceptions.NotFoundException;

import ar.edu.iua.TruckTeck.auth.model.business.IUserBusiness;
import ar.edu.iua.TruckTeck.auth.model.User;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementación personalizada de {@link AuthenticationManager} que gestiona el proceso
 * de autenticación de usuarios dentro del sistema.
 * <p>
 * Este componente utiliza un servicio de negocio {@link IUserBusiness} para cargar los datos del usuario
 * y un {@link PasswordEncoder} para validar las credenciales ingresadas contra las almacenadas.
 * </p>
 *
 * <p>Durante el proceso de autenticación se evalúan distintos estados del usuario
 * (cuenta bloqueada, deshabilitada, expirada, etc.), y se lanzan las excepciones correspondientes
 * según el caso.</p>
 *
 * <p><b>Autor:</b> Equipo IW3 - Universidad Argentina</p>
 * <p><b>Versión:</b> 1.0.0</p>
 */
@Slf4j
public class CustomAuthenticationManager implements AuthenticationManager {
    /** Servicio de negocio encargado de la gestión de usuarios. */
	private IUserBusiness userBusiness;

    /** Codificador de contraseñas utilizado para comparar las credenciales. */
	private PasswordEncoder pEncoder;

    /**
     * Constructor de la clase {@code CustomAuthenticationManager}.
     *
     * @param pEncoder      Codificador de contraseñas para la validación.
     * @param userBusiness  Servicio de negocio para la carga de usuarios.
     */
	public CustomAuthenticationManager(PasswordEncoder pEncoder, IUserBusiness userBusiness) {
		this.pEncoder = pEncoder;
		this.userBusiness = userBusiness;
	}


    /**
     * Autentica a un usuario basándose en sus credenciales.
     * <p>
     * Este método carga el usuario desde la base de datos y verifica tanto su estado
     * (activo, bloqueado, expirado, etc.) como la validez de la contraseña proporcionada.
     * </p>
     *
     * @param authentication Objeto {@link Authentication} que contiene el nombre de usuario y la contraseña.
     * @return Un objeto {@link UsernamePasswordAuthenticationToken} en caso de autenticación exitosa.
     * @throws AuthenticationException Si las credenciales son inválidas o si el usuario presenta restricciones
     *                                 (bloqueado, expirado, deshabilitado, etc.).
     */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		User user = null;

		try {
			user = userBusiness.load(username);
		} catch (NotFoundException e) {
			throw new BadCredentialsException(e.getMessage());
		} catch (BusinessException e) {
			log.error(e.getMessage(), e);
			throw new AuthenticationServiceException(e.getMessage());
		}
		String validation = user.validate();
		if (validation.equals(User.VALIDATION_ACCOUNT_EXPIRED))
			throw new AccountExpiredException(User.VALIDATION_ACCOUNT_EXPIRED);
		if (validation.equals(User.VALIDATION_CREDENTIALS_EXPIRED))
			throw new CredentialsExpiredException(User.VALIDATION_CREDENTIALS_EXPIRED);
		if (validation.equals(User.VALIDATION_DISABLED))
			throw new DisabledException(User.VALIDATION_DISABLED);
		if (validation.equals(User.VALIDATION_LOCKED))
			throw new LockedException(User.VALIDATION_LOCKED);
		if (!pEncoder.matches(password, user.getPassword()))
			throw new BadCredentialsException("Invalid password");
		return new UsernamePasswordAuthenticationToken(user, null,user.getAuthorities());

		
	}

    /**
     * Crea una instancia anónima de {@link Authentication} con las credenciales
     * proporcionadas (nombre de usuario y contraseña).
     * <p>
     * Este método se utiliza como envoltorio auxiliar para generar el objeto
     * de autenticación sin necesidad de una implementación externa.
     * </p>
     *
     * @param name Nombre de usuario.
     * @param pass Contraseña del usuario.
     * @return Una nueva instancia de {@link Authentication} con los datos básicos del usuario.
     */
	@SuppressWarnings("serial")
	public Authentication authWrap(String name, String pass) {
		return new Authentication() {
			@Override
			public String getName() {
				return name;
			}
			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			}
			@Override
			public boolean isAuthenticated() {
				return false;
			}
			@Override
			public Object getPrincipal() {
				return null;
			}
			@Override
			public Object getDetails() {
				return null;
			}
			@Override
			public Object getCredentials() {
				return pass;
			}
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return null;
			}
		};
	}

}
