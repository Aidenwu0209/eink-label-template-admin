package com.aiden.einklabel.admin.mqtt;

import java.util.Map;

public record MqttCommand(String topic, Map<String, Object> payload) {
}
