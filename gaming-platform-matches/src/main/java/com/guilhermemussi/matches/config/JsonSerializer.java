package com.guilhermemussi.matches.config;

import org.apache.kafka.common.serialization.Serializer;

import javax.json.JsonObject;
import java.nio.charset.Charset;

public class JsonSerializer implements Serializer<JsonObject> {

    @Override
    public byte[] serialize(String s, JsonObject jsonObject) {
        return jsonObject.toString().getBytes(Charset.forName("UTF-8"));
    }
}
