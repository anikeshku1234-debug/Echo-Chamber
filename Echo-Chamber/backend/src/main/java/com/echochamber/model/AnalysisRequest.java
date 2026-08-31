package com.echochamber.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnalysisRequest {

    @NotBlank(message = "Statement must not be blank")
    @Size(min = 3, max = 1000, message = "Statement must be between 3 and 1000 characters")
    private String statement;

    public AnalysisRequest() {}

    public AnalysisRequest(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }
}