package ar.edu.iua.TruckTeck.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ar.edu.iua.TruckTeck.auth.model.business.IUserBusiness;
import ar.edu.iua.TruckTeck.auth.custom.CustomAuthenticationManager;
import ar.edu.iua.TruckTeck.auth.filters.JWTAuthorizationFilter;
import ar.edu.iua.TruckTeck.controllers.Constants;

/**
 * Spring Security configuration class for the application.
 * <p>
 * This class sets up authentication and authorization rules, password encoding,
 * CORS configuration, and the security filter chain. It integrates JWT-based
 * authorization and stateless session management for REST APIs.
 * </p>
 * 
 * <p>Key configurations include:</p>
 * <ul>
 *     <li>Password encoding using {@link BCryptPasswordEncoder}</li>
 *     <li>CORS configuration allowing all origins, headers, and HTTP methods</li>
 *     <li>Custom authentication manager using {@link CustomAuthenticationManager}</li>
 *     <li>JWT authorization filter for validating incoming requests</li>
 *     <li>Stateless session management with CSRF disabled</li>
 * </ul>
 * 
 * <p><b>Author:</b> IW3 Team - Universidad Argentina</p>
 * <p><b>Version:</b> 1.0.0</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

	/*
	 * @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	 * // CORS: https://developer.mozilla.org/es/docs/Web/HTTP/CORS // CSRF:0
	 * https://developer.mozilla.org/es/docs/Glossary/CSRF
	 * http.cors(CorsConfigurer::disable);
	 * http.csrf(AbstractHttpConfigurer::disable); http.authorizeHttpRequests(auth
	 * -> auth .requestMatchers("/**").permitAll() .anyRequest().authenticated() );
	 * return http.build(); }
	 */

	/**
    * Bean that provides password encoding using BCrypt.
    *
    * @return {@link PasswordEncoder} instance using BCrypt algorithm.
    */
	@Bean
	PasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
     * Configures CORS (Cross-Origin Resource Sharing) settings for the application.
     * <p>
     * All origins, headers, and HTTP methods are allowed for all endpoints.
     * </p>
     *
     * @return {@link WebMvcConfigurer} instance with CORS configuration.
     */
	@Bean
	WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedMethods("*").allowedHeaders("*").allowedOrigins("*");
			}
		};
	}

	/** Provides business logic for user operations, used in authentication. */
	@Autowired
	private IUserBusiness userBusiness;

	/**
     * Bean that provides a custom authentication manager using {@link CustomAuthenticationManager}.
     *
     * @return {@link AuthenticationManager} instance for handling authentication requests.
     */
	@Bean
	AuthenticationManager authenticationManager() {
		return new CustomAuthenticationManager(bCryptPasswordEncoder(), userBusiness);
	}

	/**
     * Configures the main security filter chain for HTTP requests.
     * <p>
     * This method defines authorization rules, disables CSRF protection, adds
     * the JWT authorization filter, enables HTTP Basic for testing purposes,
     * and sets stateless session management.
     * </p>
     *
     * @param http {@link HttpSecurity} object to configure security for HTTP requests.
     * @return {@link SecurityFilterChain} configured for the application.
     * @throws Exception if an error occurs during configuration.
     */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// CORS: https://developer.mozilla.org/es/docs/Web/HTTP/CORS
		// CSRF: https://developer.mozilla.org/es/docs/Glossary/CSRF

		// Disable CSRF protection for stateless REST APIs
		http.csrf(AbstractHttpConfigurer::disable);
		// Define request authorization rules
		http.authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, Constants.URL_LOGIN).permitAll()
				.requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
				.requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/ui/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
				.requestMatchers("/demo/**").permitAll().anyRequest().authenticated());

		// Enable HTTP Basic authentication (optional, for testing)
		http.httpBasic(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT authorization filter to validate incoming tokens
		http.addFilter(new JWTAuthorizationFilter(authenticationManager()));
		return http.build();

	}

	/**
 	* Configura el manejo de CORS (Cross-Origin Resource Sharing) para la aplicación.
 	* <p>
 	* Este método define una política CORS personalizada especificando:
 	* <ul>
 	*   <li>Orígenes permitidos (frontend autorizado)</li>
 	*   <li>Métodos HTTP aceptados</li>
 	*   <li>Headers permitidos</li>
 	*   <li>Headers expuestos al cliente</li>
 	*   <li>Si las credenciales están permitidas o no</li>
 	* </ul>
 	* La configuración se aplica a todos los endpoints expuestos por el backend,
 	* incluyendo peticiones de WebSocket durante la fase de handshake.
 	*
 	* @return una instancia de {@link CorsConfigurationSource} con la política CORS aplicada.
 	*/
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Permite únicamente los orígenes del frontend (desarrollo local)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5500"));

        // Métodos HTTP permitidos en las solicitudes al backend
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    	// Headers permitidos que el cliente puede enviar
        config.setAllowedHeaders(List.of("*"));

        // Headers expuestos al frontend (necesario para leer el JWT desde Authorization)
        config.setExposedHeaders(List.of("Authorization"));

		// Deshabilita el envío de cookies o credenciales en la solicitud
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        //Aplica a TODOS los endpoints (incluye WS handshake)
		// Aplica la configuración CORS a todos los endpoints de la API
        source.registerCorsConfiguration("/**", config);

        return source;
    }

}