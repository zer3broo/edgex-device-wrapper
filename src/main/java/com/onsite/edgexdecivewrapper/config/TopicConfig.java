package com.onsite.edgexdecivewrapper.config;


import com.onsite.edgexdecivewrapper.model.Topic;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "mqtt-topics-config")
@EnableConfigurationProperties
@Getter
@Setter
public class TopicConfig {
    private String name;
    private boolean test;
    private List<Topic> topics;
}
