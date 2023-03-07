# Mqtt-Device Mapper for EdgeX Device Service

This artefact can be used, if a device provides data over multilevel topics that do not 
match the device/resource configuration of an EdgeX mqtt devices. 

## Use Case
Imaging using one Device Service in EdgeX with several devices and the same topic for incoming data.
What can you do if your device publish the deviceName/deviceId over the topic name and don't specify it
in the data section? You could use the multilevel topic function provided by EdgeX, but the
problem here is, if your topics looks like deviceId/resource1/resource1.1 and deviceId/resource2 you cant 
use wildcards except the '#' but that's not a good solution at all. 
For this use case you can use this mapper, witch put the information provide via the topic
into the data section and publish it to a generic incoming data topic of the device Service.

## How to Use 

The whole mapper can be configured over the application.yml file.

#### 1. Configure mqtt-connections
In this section you need to configure the connection to the broker for incoming and 
```yml
mqtt-connection:
   inbound:
     broker:
       clientId: ClientId
       host: tcp://localhost:1883
       topics: topic1/to/subscribe/#, topic2/sub, topic3/to/sub
   outbound:
     broker:
       clientId: Client
       host: tcp://localhost:1883
       topic: topic/to/publish
       username: username
       password: password
```
#### 2. Configure mqtt-connections
In this section you need to specify how to compute the data, that arrive at a topic
```yml
mqtt-topics-config:
  topics:
    - topicName: topic1/to/subscribe/+/+/+
      mapperConfig:
        nameInTopic: true
        resourceInTopic: true
        namePositions:
          - 2
        resourcePositions:
          - 3
          - 4
        fieldName:
        resourceName:
    - topicName: topic2/sub
      mapperConfig:
        nameInTopic: false
        resourceInTopic: true
        namePositions:
        resourcePositions:
          - 2
        fieldName: nameOfDeviceFieldInJsonData
        resourceName:
    - topicName: topic2/sub
      mapperConfig:
        nameInTopic: false
        resourceInTopic: false
        namePositions:
        resourcePositions:
        fieldName: nameOfDeviceFieldInJsonData
        resourceName: nameOfResourceFieldInJsonData
```

#### 3. Explanation of filed names for mqtt-topics-config

**topicName:**<br>
specify the name/type of topic for a specific mapping structure. Use 
wildcards, if topics have the same structure for topic name and date.
Imaging, having this two topics: device1/temp/tool1, device2/temp/tool12 with the same 
datastructures. Instead of creating an entry for each topic, create an entry with a 
wildcard e.g. +/temp/+ . <br>
**Only '+' is allowed as a wildcard not '#'**.<br>

**nameInTopic:**<br>
Boolean value that indicates if deviceName is part of the topic name or part to the data.<br>

**resourceInTopic:**<br>
Boolean value that indicates if resource is part of the topic name or part to the data.<br>

**namePositions/resourcePositions:**<br>
List of Integer values, that indicates witch parts of the MQTT-topic describes the 
device/resource name. Start count with one. **Is required if nameInTopic/resourceInTopic is
true**<br>

**fieldName/resourceName:**<br>
Name of JSON-Field witch contains device/resource name. **Is required if nameInTopic/resourceInTopic is
false**<br>
