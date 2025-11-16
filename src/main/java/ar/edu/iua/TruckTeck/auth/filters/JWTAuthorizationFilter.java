package ar.edu.iua.TruckTeck.auth.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import ar.edu.iua.TruckTeck.auth.model.Role;
import ar.edu.iua.TruckTeck.auth.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro de autorización basado en JWT que extiende {@link BasicAuthenticationFilter}.
 * <p>
 * Este filtro intercepta las solicitudes HTTP entrantes y verifica la validez de los tokens
 * JWT (JSON Web Token) enviados mediante el encabezado {@code Authorization} o el parámetro
 * {@code authtoken}. En caso de ser válido, establece la autenticación en el contexto de seguridad
 * de Spring.
 * </p>
 * 
 * <p>Se utiliza principalmente para proteger los recursos del backend y garantizar
 * que solo los usuarios autenticados puedan acceder a ellos.</p>
 *
 * <p><b>Autor:</b> Equipo IW3 - Universidad Argentina</p>
 * <p><b>Versión:</b> 1.0.0</p>
 */
@Slf4j
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    /**
     * Constructor del filtro de autorización JWT.
     *
     * @param authenticationManager Gestor de autenticación utilizado por el sistema.
     */
	public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

    /**
     * Método principal del filtro que intercepta cada solicitud HTTP.
     * <p>
     * Este método analiza la cabecera {@code Authorization} o el parámetro {@code authtoken}
     * para extraer el token JWT. Si se encuentra un token válido, se crea una instancia de
     * {@link UsernamePasswordAuthenticationToken} y se establece en el contexto de seguridad.
     * </p>
     *
     * @param req   Objeto {@link HttpServletRequest} de la solicitud entrante.
     * @param res   Objeto {@link HttpServletResponse} de la respuesta.
     * @param chain Cadena de filtros que continúa la ejecución del request.
     * @throws IOException      Si ocurre un error de entrada/salida.
     * @throws ServletException Si ocurre un error al procesar la solicitud.
     */
	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		String header = req.getHeader(AuthConstants.AUTH_HEADER_NAME);
		String param = req.getParameter(AuthConstants.AUTH_PARAM_NAME);
		boolean byHeader = !(header == null || !header.startsWith(AuthConstants.TOKEN_PREFIX));
		boolean byParam = !(param == null || param.trim().length() < 10);
		
		// Si no se envía o es correcto el inicio de la cabecera o bien no se envía un
		// parámetro correcto, se continúa con el resto de los filtros
		if (!byHeader && !byParam) {
			chain.doFilter(req, res);
			return;
		}
		// Le damos prioridad al header.
		UsernamePasswordAuthenticationToken authentication = getAuthentication(req, byHeader);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);

	}

    /**
     * Extrae y valida el token JWT, generando una autenticación válida para Spring Security.
     * <p>
     * Si el token es válido, se obtiene la información del usuario (nombre, roles, correo electrónico, etc.)
     * y se crea una instancia de {@link UsernamePasswordAuthenticationToken}.
     * </p>
     *
     * @param request  Solicitud HTTP actual.
     * @param byHeader Indica si el token fue recibido por cabecera (true) o por parámetro (false).
     * @return Una instancia válida de {@link UsernamePasswordAuthenticationToken} si el token es correcto,
     *         o {@code null} si la validación falla.
     */
	// Extraer el token JWT de la cabecera y lo intenta validar
	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, boolean byHeader) {
		// Recordar que el header inicia con alguna cadena, por ejemplo: 'Bearer '
		String token = byHeader
				? request.getHeader(AuthConstants.AUTH_HEADER_NAME).replace(AuthConstants.TOKEN_PREFIX, "")
				: request.getParameter(AuthConstants.AUTH_PARAM_NAME);

		if (token != null) {
			// Parseamos el token usando la librería
			DecodedJWT jwt=null;
			try {
                // Verificar la validez del token con la clave secreta
				jwt = JWT.require(Algorithm.HMAC512(AuthConstants.SECRET.getBytes())).build().verify(token);
				log.trace("Token recibido por '{}'", byHeader ? "header" : "query param");
				log.trace("Usuario logueado: " + jwt.getSubject());
				log.trace("Roles: " + jwt.getClaim("roles"));
				log.trace("Custom JWT Version: " + jwt.getClaim("version").asString());
				
				
				Set<Role> roles=new HashSet<Role>();
				
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
                
                // Construcción de los roles y autoridades
				@SuppressWarnings("unchecked")
				List<String> rolesStr = (List<String>) jwt.getClaim("roles").as(List.class);
				authorities = rolesStr.stream().map(role -> new SimpleGrantedAuthority(role))
						.collect(Collectors.toList());
				roles=rolesStr.stream().map(role-> new Role(role,0,role)).collect(Collectors.toSet());

                // Reconstrucción del usuario autenticado
				String username = jwt.getSubject();

				if (username != null) {
					User user = new User();
					user.setIdUser(jwt.getClaim("internalId").asLong());
					user.setUsername(username);
					user.setRoles(roles);
					user.setEmail(jwt.getClaim("email").asString());
					return new UsernamePasswordAuthenticationToken(user, null, authorities);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			
			
			return null;
		}
		return null;
	}

}