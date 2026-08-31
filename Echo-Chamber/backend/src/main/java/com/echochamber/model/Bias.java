package com.echochamber.model;

public class Bias {
    private String name;
    private String explanation;
    private String severity; // High, Medium, Low

    public Bias() {}

    public Bias(String name, String explanation, String severity) {
        this.name = name;
        this.explanation = explanation;
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}