package com.guilhermemussi.gameplay.config;

import javax.json.Json;
import javax.json.JsonObject;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;

public class JsonTextDecoder implements Decoder.Text<JsonObject> {
    @Override
    public JsonObject decode(String s) throws DecodeException {
        return Json.createReader(new StringReader(s)).readObject();
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }

    @Override
    public void init(EndpointConfig endpointConfig) {

    }

    @Override
    public void destroy() {

    }
}
