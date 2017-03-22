package net.whistlingfish.harmony.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;

import static net.whistlingfish.harmony.Jackson.OBJECT_MAPPER;

public abstract class IrCommand extends OAStanza {
    public IrCommand(String mimeType) {
        super(mimeType);
    }

    public String generateAction(int deviceId, String button) {
        try {
            return OBJECT_MAPPER.writeValueAsString(ImmutableMap.<String, Object> builder() //
                    .put("type", "IRCommand")
                    .put("deviceId", Integer.valueOf(deviceId).toString())
                    .put("command", button)
                    .build()).replaceAll(":", "::");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
