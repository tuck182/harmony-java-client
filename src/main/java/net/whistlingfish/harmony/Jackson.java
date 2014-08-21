package net.whistlingfish.harmony;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class Jackson {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper() //
            .disable(FAIL_ON_UNKNOWN_PROPERTIES);
}
