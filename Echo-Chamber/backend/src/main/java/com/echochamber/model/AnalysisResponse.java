package com.echochamber.model;

import java.util.List;

public class AnalysisResponse {
    private int score;
    private List<String> positivePoints;
    private List<String> negativePoints;
    private String alternativePerspective;
    private List<Bias> biases;
    private String balancedConclusion;

    public AnalysisResponse() {}

    public AnalysisResponse(int score, List<String> positivePoints, List<String> negativePoints,
                            String alternativePerspective, List<Bias> biases, String balancedConclusion) {
        this.score = score;
        this.positivePoints = positivePoints;
        this.negativePoints = negativePoints;
        this.alternativePerspective = alternativePerspective;
        this.biases = biases;
        this.balancedConclusion = balancedConclusion;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getPositivePoints() {
        return positivePoints;
    }

    public void setPositivePoints(List<String> positivePoints) {
        this.positivePoints = positivePoints;
    }

    public List<String> getNegativePoints() {
        return negativePoints;
    }

    public void setNegativePoints(List<String> negativePoints) {
        this.negativePoints = negativePoints;
    }

    public String getAlternativePerspective() {
        return alternativePerspective;
    }

    public void setAlternativePerspective(String alternativePerspective) {
        this.alternativePerspective = alternativePerspective;
    }

    public List<Bias> getBiases() {
        return biases;
    }

    public void setBiases(List<Bias> biases) {
        this.biases = biases;
    }

    public String getBalancedConclusion() {
        return balancedConclusion;
    }

    public void setBalancedConclusion(String balancedConclusion) {
        this.balancedConclusion = balancedConclusion;
    }
}