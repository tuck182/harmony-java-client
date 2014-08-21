package net.whistlingfish.harmony;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;

public class Jackson {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper() //
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(READ_ENUMS_USING_TO_STRING);
}
