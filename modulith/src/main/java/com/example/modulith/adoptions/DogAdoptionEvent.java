package com.example.modulith.adoptions;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.GenericHandler;
import org.springframework.integration.dsl.DirectChannelSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.modulith.events.Externalized;

@Externalized(MyIntegrationFlow.CN)
public record DogAdoptionEvent(int dogId) {
}

@Configuration
class MyIntegrationFlow {

    static final String CN = "adoptionsOutboundChannelName" ;

    @Bean
    IntegrationFlow integrationFlow(@Qualifier(CN) MessageChannel inboundChannel) {
        return IntegrationFlow
                .from(inboundChannel)
                .handle((GenericHandler<DogAdoptionEvent>) (payload, headers) -> {
                    System.out.println("got a message: " + payload);
                    headers.forEach((k, v) -> System.out.println(k + '=' + v));
                    return null;
                })
                .get();
    }

    @Bean(name = CN)
    DirectChannelSpec outboundChannelName() {
        return MessageChannels.direct();
    }
}