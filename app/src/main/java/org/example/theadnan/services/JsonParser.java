package org.example.theadnan.services;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParser {
    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T parse(String json, Class<T> cls) throws Exception {
        return mapper.readValue(json, cls);
    }
}
