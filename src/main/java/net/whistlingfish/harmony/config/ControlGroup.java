package net.whistlingfish.harmony.config;

import java.util.List;

public class ControlGroup {
    private String name;
    private List<Function> function;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Function> getFunction() {
        return function;
    }

    public void setFunction(List<Function> function) {
        this.function = function;
    }
}
