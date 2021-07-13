package com.dose.dose.controls;

public abstract class ControlSetting {
    private String value;
    private int id;

    public ControlSetting(String value, int id) {
        this.value = value;
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public int getId() {
        return id;
    }
}
