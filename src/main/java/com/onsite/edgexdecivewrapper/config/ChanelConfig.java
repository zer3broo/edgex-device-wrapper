package com.onsite.edgexdecivewrapper.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.router.PayloadTypeRouter;

@Configuration
public class ChanelConfig {

    @ServiceActivator(inputChannel = "routingChannel")
    @Bean
    public PayloadTypeRouter router(){
        PayloadTypeRouter router = new PayloadTypeRouter();
        router.setChannelMapping("null", "errorChannel");
        router.setChannelMapping(String.class.getName(), "mqttOutboundChannel");
        return router;
    }

}
