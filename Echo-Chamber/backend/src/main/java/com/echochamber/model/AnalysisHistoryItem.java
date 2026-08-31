package com.echochamber.model;

import java.time.LocalDate;

public class AnalysisHistoryItem {
    private String id;
    private String statement;
    private int score;
    private LocalDate createdAt;
    private AnalysisResponse response;

    public AnalysisHistoryItem() {}

    public AnalysisHistoryItem(String id, String statement, int score, LocalDate createdAt, AnalysisResponse response) {
        this.id = id;
        this.statement = statement;
        this.score = score;
        this.createdAt = createdAt;
        this.response = response;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStatement() { return statement; }
    public void setStatement(String statement) { this.statement = statement; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public AnalysisResponse getResponse() { return response; }
    public void setResponse(AnalysisResponse response) { this.response = response; }
}