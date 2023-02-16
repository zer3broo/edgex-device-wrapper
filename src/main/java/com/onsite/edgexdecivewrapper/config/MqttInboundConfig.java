package com.onsite.edgexdecivewrapper.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.util.List;

@Configuration
@EnableIntegration
public class MqttInboundConfig {

    private final Logger log = LoggerFactory.getLogger(MqttInboundConfig.class);

    @Value("${mqtt-connection.inbound.broker.host}")
    private String host;

    @Value("${mqtt-connection.inbound.broker.topics}")
    private String[] topics;

    @Value("${mqtt-connection.inbound.broker.clientId}")
    private String clientId;

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageChannel wrapDataChanel (){return new DirectChannel();}
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(host, clientId,
                        topics);
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }


    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel", outputChannel = "wrapDataChanel")
    public MessageHandler stringDataHandler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String payload = message.getPayload().toString();
                System.out.println(payload);
                XmlMapper xmlMapper = new XmlMapper();
                ObjectMapper jsonMapper = new ObjectMapper();
                JsonNode node;
                try {
                    node = jsonMapper.readTree(payload);
                }  catch (JsonMappingException e) {
                    try {
                        node = xmlMapper.readTree(payload);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                } catch (JsonProcessingException e) {
                    try {
                        node = xmlMapper.readTree(payload);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    throw new RuntimeException(e);
                }
                Message<?> newMessage = MessageBuilder.withPayload(node.toString()).copyHeadersIfAbsent(message.getHeaders()).build();
                wrapDataChanel().send(newMessage);
            }
        };

    }
}
