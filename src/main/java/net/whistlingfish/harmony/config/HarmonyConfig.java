package net.whistlingfish.harmony.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.whistlingfish.harmony.Jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class HarmonyConfig {
    @JsonProperty("activity")
    private List<Activity> activities = newArrayList();

    @JsonProperty("device")
    private List<Device> devices = newArrayList();

    private Map<String, String> content = newHashMap();

    private Global global;

    public static HarmonyConfig parse(String config) {
        try {
            return Jackson.OBJECT_MAPPER.readValue(config, HarmonyConfig.class);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing config from json", e);
        }
    }

    public String toJson() {
        try {
            return Jackson.OBJECT_MAPPER.writer(new DefaultPrettyPrinter()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing config to json", e);
        }
    }

    public Map<Integer, String> getDeviceLabels() {
        Map<Integer, String> results = new HashMap<>();
        for (Device device : devices) {
            results.put(device.getId(), device.getLabel());
        }
        return results;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activity) {
        this.activities = activity;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> device) {
        this.devices = device;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public void setContent(Map<String, String> content) {
        this.content = content;
    }

    public Global getGlobal() {
        return global;
    }

    public void setGlobal(Global global) {
        this.global = global;
    }

    public Activity getActivityById(int result) {
        for (Activity activity : activities) {
            if (activity.getId() == result)
                return activity;
        }
        return null;
    }

    public Activity getActivityByName(String label) {
        for (Activity activity : activities) {
            if (activity.getLabel().equals(label))
                return activity;
        }
        return null;
    }

    public Device getDeviceByName(String label) {
        for (Device device : devices) {
            if (device.getLabel().equals(label))
                return device;
        }
        return null;
    }
}
