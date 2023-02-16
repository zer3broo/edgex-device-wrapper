package com.onsite.edgexdecivewrapper.component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.onsite.edgexdecivewrapper.config.TopicConfig;
import com.onsite.edgexdecivewrapper.model.MapperConfig;
import com.onsite.edgexdecivewrapper.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MessageWrapper {

    private static final Logger log = LoggerFactory.getLogger(MessageWrapper.class);
    private TopicConfig topicConfig;

    private ObjectMapper jsonMapper = new ObjectMapper();

    private static final String DEVICE_FIELD_NAME = "name";
    private static final String RESOURCE_FIELD_NAME = "cmd";
    private static final String ERROR_LOG_MESSAGE_NO_CONFIG = "Could not find mapper config for topic: ";
    private static final String ERROR_LOG_MESSAGE_GENERIC_PART = ". Message could not mapped and is deleted";
    private static final String ERROR_LOG_MESSAGE_NO_TOPIC_POSITION_FOR_DEVICE =  "Topic position for field 'deviceName'  is not specified for topic: ";
    private static final String ERROR_LOG_MESSAGE_NO_TOPIC_POSITION_FOR_RESOURCE = "Topic position for  field 'resource'  is not specified for topic: ";
    private static final String ERROR_LOG_MESSAGE_NO_PAYLOAD_FIELD_FOR_DEVICE = "Payload filed name for  field 'deviceName'  is not specified for topic: ";
    private static final String ERROR_LOG_MESSAGE_NO_PAYLOAD_FIELD_FOR_RESOURCE= "Payload filed name for  field 'resource'  is not specified for topic: ";
    @Autowired
    public MessageWrapper(TopicConfig topicConfig) {
        this.topicConfig = topicConfig;
    }


    @ServiceActivator(inputChannel = "wrapDataChanel", outputChannel = "routingChannel")
    public String receivedMessage(Message<?> message, @Header(name = "mqtt_receivedTopic", required = true) String topic) throws JsonProcessingException {

        String deviceName;
        String resource;
        String payload = message.getPayload().toString();
        log.debug("Incoming Payload: "+payload);
        log.debug("Incoming mqtt-Topic: "+topic);

        Optional<MapperConfig> optionalMapperConfig = getConfig(topic);

        if (optionalMapperConfig.isEmpty()){
            log.error(ERROR_LOG_MESSAGE_NO_CONFIG+topic+ ERROR_LOG_MESSAGE_GENERIC_PART);
            return null;
        }

        MapperConfig mapperConfig = optionalMapperConfig.get();
        log.debug("Mapper Config: "+mapperConfig.toString());
        if (mapperConfig.isNameInTopic()) {
            if(mapperConfig.getNamePositions().isEmpty()) {
                log.error(ERROR_LOG_MESSAGE_NO_TOPIC_POSITION_FOR_DEVICE+topic+ERROR_LOG_MESSAGE_GENERIC_PART);
                return null;
            }
            deviceName = getValueFromTopic(topic, mapperConfig.getNamePositions());
        } else {
            if(mapperConfig.getResourcePositions().isEmpty()) {
                log.error(ERROR_LOG_MESSAGE_NO_PAYLOAD_FIELD_FOR_DEVICE+topic+ERROR_LOG_MESSAGE_GENERIC_PART);
                return null;
            }
            deviceName = getValueFromPayload(payload, mapperConfig.getFieldName());
        }
        if (mapperConfig.isResourceInTopic()) {
            if(mapperConfig.getResourcePositions().isEmpty()) {
                log.error(ERROR_LOG_MESSAGE_NO_TOPIC_POSITION_FOR_RESOURCE+topic+ERROR_LOG_MESSAGE_GENERIC_PART);
                return null;
            }
            resource = getValueFromTopic(topic, mapperConfig.getResourcePositions());
        } else {
            if(mapperConfig.getResourcePositions().isEmpty()) {
                log.error(ERROR_LOG_MESSAGE_NO_PAYLOAD_FIELD_FOR_RESOURCE+topic+ERROR_LOG_MESSAGE_GENERIC_PART);
                return null;
            }
            resource = getValueFromPayload(payload, mapperConfig.getResourceName());
        }

        JsonNode modifiedPayload = addInformationToJson(deviceName, resource, jsonMapper.readTree(payload));
        log.debug("New Payload: "+ modifiedPayload.toString());
        return modifiedPayload.toString();
    }

    private JsonNode addInformationToJson(String deviceName, String deviceResource , JsonNode payload){
        ObjectNode jNode = jsonMapper.createObjectNode();
        jNode.put(DEVICE_FIELD_NAME,deviceName).put(RESOURCE_FIELD_NAME,deviceResource).put(deviceResource,payload);
        return  jNode;
    }

    private Optional<MapperConfig> getConfig(String topicName) {
        if (topicConfig == null) return Optional.empty();
        if (topicConfig.getTopics() == null) return Optional.empty();

        Optional<Topic> topic = topicConfig.getTopics().stream().findFirst().filter(t -> t.getTopicName().equals(topicName));
        if (topic.isPresent()) {
            return Optional.of(topic.get().getMapperConfig());

        } else {
            List<Topic> topicWithWildcard = topicConfig.getTopics().stream().filter(t-> t.getTopicName().contains("+")).toList();
            for (Topic t : topicWithWildcard) {
                String topicRegex = t.getTopicName().replaceAll("\\+\\/","(.*?\\/)");
                topicRegex = topicRegex.replaceAll("\\+",".*");
                Pattern pattern = Pattern.compile(topicRegex);
                Matcher m = pattern.matcher(topicName);
                if(m.matches()) return Optional.of(t.getMapperConfig());
            }
        }
        return Optional.empty();
    }

    private String getValueFromTopic(String topic, List<Integer> position) {
        String[] splitTopic = topic.split("/");
        AtomicReference<String> name = new AtomicReference<>("");
        position.forEach(n->{
             name.set(name + splitTopic[n - 1]);
        });
        return name.get();
    }

    private String getValueFromPayload(String payload, String fieldName) throws JsonProcessingException {
        JsonNode payloadAsJson = jsonMapper.readTree(payload);
        List<String> nameList = payloadAsJson.findValuesAsText(fieldName);
        String name = nameList.stream().reduce("", (partialString, element) -> partialString + element);
        return name;
    }

}
