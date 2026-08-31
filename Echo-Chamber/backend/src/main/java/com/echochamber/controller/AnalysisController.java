package com.echochamber.controller;

import com.echochamber.model.AnalysisHistoryItem;
import com.echochamber.model.AnalysisRequest;
import com.echochamber.model.AnalysisResponse;
import com.echochamber.service.AnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(@Valid @RequestBody AnalysisRequest request) {
        AnalysisResponse response = analysisService.analyzeStatement(request.getStatement());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisHistoryItem>> getHistory() {
        return ResponseEntity.ok(analysisService.getHistory());
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory() {
        analysisService.clearHistory();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}