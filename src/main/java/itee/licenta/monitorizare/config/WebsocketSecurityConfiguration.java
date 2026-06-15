package itee.licenta.monitorizare.config;

import itee.licenta.monitorizare.security.AuthoritiesConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebsocketSecurityConfiguration extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .nullDestMatcher()
            .authenticated()
            .simpDestMatchers("/topic/tracker")
            .hasAuthority(AuthoritiesConstants.ADMIN)
            .simpDestMatchers("/topic/medical-data/**")
            .authenticated()
            .simpDestMatchers("/topic/notifications/**")
            .authenticated()
            .simpDestMatchers("/topic/dashboard-update")
            .authenticated()
            .simpDestMatchers("/topic/**")
            .authenticated()
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE)
            .denyAll()
            .anyMessage()
            .denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
