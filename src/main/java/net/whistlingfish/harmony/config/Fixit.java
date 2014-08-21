package net.whistlingfish.harmony.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Fixit {
    private String id;

    @JsonProperty("Power")
    private PowerState power;

    @JsonProperty("Input")
    private String input;

    @JsonProperty("isAlwaysOn")
    private boolean alwaysOn;

    @JsonProperty("isRelativePower")
    private boolean relativePower;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PowerState getPower() {
        return power;
    }

    public void setPower(PowerState power) {
        this.power = power;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public boolean isAlwaysOn() {
        return alwaysOn;
    }

    public void setAlwaysOn(boolean alwaysOn) {
        this.alwaysOn = alwaysOn;
    }

    public boolean isRelativePower() {
        return relativePower;
    }

    public void setRelativePower(boolean relativePower) {
        this.relativePower = relativePower;
    }
}
