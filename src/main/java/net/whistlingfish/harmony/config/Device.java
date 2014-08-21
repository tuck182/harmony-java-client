package net.whistlingfish.harmony.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.google.common.collect.Lists.newArrayList;

public class Device {
    private String id;

    private String label;

    private String type;

    @JsonProperty("Transport")
    private int transport;

    private String suggestedDisplay;

    private String deviceTypeDisplayName;

    @JsonProperty("Capabilities")
    private List<Integer> capabilities = newArrayList();

    @JsonProperty("DongleRFID")
    private int dongleRFID;

    private List<ControlGroup> controlGroup = newArrayList();

    @JsonProperty("ControlPort")
    private String controlPort;

    @JsonProperty("IsKeyboardAssociated")
    private boolean keyboardAssociated;

    private String model;

    private String deviceProfileUri;

    private String manufacturer;

    private String icon;

    @JsonProperty("isManualPower")
    private boolean manualPower;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTransport() {
        return transport;
    }

    public void setTransport(int transport) {
        this.transport = transport;
    }

    public String getSuggestedDisplay() {
        return suggestedDisplay;
    }

    public void setSuggestedDisplay(String suggestedDisplay) {
        this.suggestedDisplay = suggestedDisplay;
    }

    public String getDeviceTypeDisplayName() {
        return deviceTypeDisplayName;
    }

    public void setDeviceTypeDisplayName(String deviceTypeDisplayName) {
        this.deviceTypeDisplayName = deviceTypeDisplayName;
    }

    public List<Integer> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Integer> capabilities) {
        this.capabilities = capabilities;
    }

    public int getDongleRFID() {
        return dongleRFID;
    }

    public void setDongleRFID(int dongleRFID) {
        this.dongleRFID = dongleRFID;
    }

    public List<ControlGroup> getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(List<ControlGroup> controlGroup) {
        this.controlGroup = controlGroup;
    }

    public String getControlPort() {
        return controlPort;
    }

    public void setControlPort(String controlPort) {
        this.controlPort = controlPort;
    }

    public boolean isKeyboardAssociated() {
        return keyboardAssociated;
    }

    public void setKeyboardAssociated(boolean keyboardAssociated) {
        this.keyboardAssociated = keyboardAssociated;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getDeviceProfileUri() {
        return deviceProfileUri;
    }

    public void setDeviceProfileUri(String deviceProfileUri) {
        this.deviceProfileUri = deviceProfileUri;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isManualPower() {
        return manualPower;
    }

    public void setManualPower(boolean manualPower) {
        this.manualPower = manualPower;
    }
}
