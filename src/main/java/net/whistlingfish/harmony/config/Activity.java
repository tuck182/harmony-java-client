package net.whistlingfish.harmony.config;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Activity {
    private String label;
    private String suggestedDisplay;
    private Integer id;
    private String activityTypeDisplayName;
    private List<ControlGroup> controlGroup = newArrayList();
    private Integer activityOrder;
    @JsonProperty("isTuningDefault")
    private boolean tuningDefault;
    private Map<String, Fixit> fixit = newHashMap();
    private String type;
    private String icon;
    private String baseImageUri;
    private Status status = Status.UNKNOWN;
    
    public enum Status {
    	HUB_IS_OFF,
    	ACTIVITY_IS_STARTING,
    	ACTIVITY_IS_STARTED,
    	HUB_IS_TURNING_OFF,
    	UNKNOWN
    }
    
    public Status getStatus() {
    	return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSuggestedDisplay() {
        return suggestedDisplay;
    }

    public void setSuggestedDisplay(String suggestedDisplay) {
        this.suggestedDisplay = suggestedDisplay;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getActivityTypeDisplayName() {
        return activityTypeDisplayName;
    }

    public void setActivityTypeDisplayName(String activityTypeDisplayName) {
        this.activityTypeDisplayName = activityTypeDisplayName;
    }

    public List<ControlGroup> getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(List<ControlGroup> controlGroup) {
        this.controlGroup = controlGroup;
    }

    public Integer getActivityOrder() {
        return activityOrder;
    }

    public void setActivityOrder(Integer activityOrder) {
        this.activityOrder = activityOrder;
    }

    public boolean isTuningDefault() {
        return tuningDefault;
    }

    public void setTuningDefault(boolean tuningDefault) {
        this.tuningDefault = tuningDefault;
    }

    public Map<String, Fixit> getFixit() {
        return fixit;
    }

    public void setFixit(Map<String, Fixit> fixit) {
        this.fixit = fixit;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBaseImageUri() {
        return baseImageUri;
    }

    public void setBaseImageUri(String baseImageUri) {
        this.baseImageUri = baseImageUri;
    }

	@Override
	public String toString() {
		return format("Activity[%d]:%s", getId(), getLabel());
	}
}
