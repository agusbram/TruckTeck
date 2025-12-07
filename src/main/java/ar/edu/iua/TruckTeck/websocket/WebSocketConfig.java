package ar.edu.iua.TruckTeck.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Donde tus clientes van a escuchar mensajes
        config.enableSimpleBroker("/topic");

        // Prefijo obligatorio para enviar mensajes desde el front al backend
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override   
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")    // URL de conexión
                .setAllowedOriginPatterns("*") // CORS para WebSocket
                .withSockJS(); // Habilita SockJS (fallback)
    }
}