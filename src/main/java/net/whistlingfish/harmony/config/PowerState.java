package net.whistlingfish.harmony.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PowerState {
    ON("On"), OFF("Off");

    private final String description;

    private PowerState(String description) {
        this.description = description;
    }

    @JsonValue
    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static PowerState forValue(String v) {
        if (v == null)
            throw new IllegalArgumentException();
        if (v.toLowerCase().equals("on"))
            return ON;
        if (v.toLowerCase().equals("off"))
            return OFF;
        throw new IllegalArgumentException();
    }
}
