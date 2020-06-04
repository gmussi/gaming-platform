package com.guilhermemussi.matches.config;

import org.apache.kafka.common.serialization.Deserializer;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.ByteArrayInputStream;

public class JsonDeserializer implements Deserializer<JsonObject> {
    @Override
    public JsonObject deserialize(String s, byte[] bytes) {
        return Json.createReader(new ByteArrayInputStream(bytes)).readObject();
    }
}
