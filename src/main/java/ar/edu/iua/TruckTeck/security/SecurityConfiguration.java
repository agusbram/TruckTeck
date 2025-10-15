package ar.edu.iua.TruckTeck.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de la aplicación.
 * <p>
 * Esta clase define la configuración de seguridad para la API utilizando Spring Security.
 * Se habilitan los mecanismos de seguridad a nivel de método y se configura el filtro de seguridad.
 * </p>
 * <p>
 * Características principales:
 * <ul>
 *   <li>CORS deshabilitado.</li>
 *   <li>CSRF deshabilitado.</li>
 *   <li>Permite todas las solicitudes HTTP ("/**").</li>
 *   <li>Habilita seguridad a nivel de método mediante {@link EnableMethodSecurity}.</li>
 * </ul>
 * </p>
 * <p>
 * Referencias:
 * <ul>
 *   <li>CORS: <a href="https://developer.mozilla.org/es/docs/Web/HTTP/CORS">MDN CORS</a></li>
 *   <li>CSRF: <a href="https://developer.mozilla.org/es/docs/Glossary/CSRF">MDN CSRF</a></li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {
    /**
     * Define el filtro de seguridad de Spring Security.
     * <p>
     * Configura CORS y CSRF como deshabilitados y permite todas las solicitudes HTTP.
     * </p>
     *
     * @param http Objeto {@link HttpSecurity} utilizado para configurar la seguridad web.
     * @return {@link SecurityFilterChain} construido según la configuración especificada.
     * @throws Exception Si ocurre un error durante la construcción del filtro de seguridad.
     */
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CORS: https://developer.mozilla.org/es/docs/Web/HTTP/CORS
        // CSRF: https://developer.mozilla.org/es/docs/Glossary/CSRF
        http.cors(CorsConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/**").permitAll().anyRequest().authenticated());
        return http.build();
    }
}