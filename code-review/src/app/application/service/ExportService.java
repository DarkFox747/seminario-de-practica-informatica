package app.application.service;

import app.application.dto.AnalysisResultDTO;
import app.application.dto.FindingSummaryDTO;
import app.domain.value.Severity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Service for exporting analysis results to CSV/PDF.
 */
public class ExportService {
    
    private final HistoryQueryService historyService;
    
    public ExportService(HistoryQueryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Export analysis results to CSV.
     */
    public File exportToCSV(Long analysisRunId, String outputPath) throws Exception {
        AnalysisResultDTO result = historyService.getRunDetails(analysisRunId);
        List<FindingSummaryDTO> findings = historyService.getFindings(analysisRunId);
        
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("Analysis Run ID,Status,Started At,Completed At,Duration (ms),Total Files,Total Findings\n");
        
        // Summary row
        csv.append(String.format("%d,%s,%s,%s,%d,%d,%d\n",
            result.getAnalysisRunId(),
            result.getStatus(),
            result.getStartedAt(),
            result.getCompletedAt(),
            result.getDurationMs() != null ? result.getDurationMs() : 0,
            result.getTotalFiles() != null ? result.getTotalFiles() : 0,
            result.getTotalFindings() != null ? result.getTotalFindings() : 0
        ));
        
        csv.append("\n");
        
        // Findings header
        csv.append("File,Line,Severity,Category,Rule ID,Message,Suggestion\n");
        
        // Findings rows
        for (FindingSummaryDTO finding : findings) {
            csv.append(String.format("\"%s\",%d,%s,%s,%s,\"%s\",\"%s\"\n",
                escapeCSV(finding.getFilePath()),
                finding.getLineNumber() != null ? finding.getLineNumber() : 0,
                finding.getSeverity(),
                finding.getCategory(),
                finding.getRuleId(),
                escapeCSV(finding.getMessage()),
                escapeCSV(finding.getSuggestion())
            ));
        }
        
        // Write to file
        Path path = Path.of(outputPath);
        Files.writeString(path, csv.toString());
        
        return path.toFile();
    }
    
    /**
     * Export findings summary to CSV (severity breakdown).
     */
    public File exportSummaryToCSV(Long analysisRunId, String outputPath) throws Exception {
        AnalysisResultDTO result = historyService.getRunDetails(analysisRunId);
        
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("Severity,Count\n");
        
        // Rows
        csv.append(String.format("CRITICAL,%d\n", result.getCriticalCount() != null ? result.getCriticalCount() : 0));
        csv.append(String.format("HIGH,%d\n", result.getHighCount() != null ? result.getHighCount() : 0));
        csv.append(String.format("MEDIUM,%d\n", result.getMediumCount() != null ? result.getMediumCount() : 0));
        csv.append(String.format("LOW,%d\n", result.getLowCount() != null ? result.getLowCount() : 0));
        csv.append(String.format("INFO,%d\n", result.getInfoCount() != null ? result.getInfoCount() : 0));
        csv.append(String.format("TOTAL,%d\n", result.getTotalFindings() != null ? result.getTotalFindings() : 0));
        
        // Write to file
        Path path = Path.of(outputPath);
        Files.writeString(path, csv.toString());
        
        return path.toFile();
    }
    
    /**
     * Export to simple text report (lightweight alternative to PDF).
     */
    public File exportToTextReport(Long analysisRunId, String outputPath) throws Exception {
        AnalysisResultDTO result = historyService.getRunDetails(analysisRunId);
        List<FindingSummaryDTO> findings = historyService.getFindings(analysisRunId);
        
        StringBuilder report = new StringBuilder();
        
        // Header
        report.append("=".repeat(80)).append("\n");
        report.append("CODE REVIEW ANALYSIS REPORT\n");
        report.append("=".repeat(80)).append("\n\n");
        
        // Summary
        report.append("Analysis Run ID: ").append(result.getAnalysisRunId()).append("\n");
        report.append("Status: ").append(result.getStatus()).append("\n");
        report.append("Started: ").append(result.getStartedAt()).append("\n");
        report.append("Completed: ").append(result.getCompletedAt()).append("\n");
        report.append("Duration: ").append(result.getDurationMs() != null ? result.getDurationMs() + " ms" : "N/A").append("\n");
        report.append("Total Files: ").append(result.getTotalFiles() != null ? result.getTotalFiles() : 0).append("\n");
        report.append("Total Findings: ").append(result.getTotalFindings() != null ? result.getTotalFindings() : 0).append("\n\n");
        
        // Severity breakdown
        report.append("-".repeat(80)).append("\n");
        report.append("SEVERITY BREAKDOWN\n");
        report.append("-".repeat(80)).append("\n");
        report.append("CRITICAL: ").append(result.getCriticalCount() != null ? result.getCriticalCount() : 0).append("\n");
        report.append("HIGH:     ").append(result.getHighCount() != null ? result.getHighCount() : 0).append("\n");
        report.append("MEDIUM:   ").append(result.getMediumCount() != null ? result.getMediumCount() : 0).append("\n");
        report.append("LOW:      ").append(result.getLowCount() != null ? result.getLowCount() : 0).append("\n");
        report.append("INFO:     ").append(result.getInfoCount() != null ? result.getInfoCount() : 0).append("\n\n");
        
        // Findings by severity
        for (Severity severity : Severity.values()) {
            List<FindingSummaryDTO> severityFindings = findings.stream()
                .filter(f -> f.getSeverity() == severity)
                .toList();
            
            if (!severityFindings.isEmpty()) {
                report.append("-".repeat(80)).append("\n");
                report.append(severity).append(" FINDINGS (").append(severityFindings.size()).append(")\n");
                report.append("-".repeat(80)).append("\n");
                
                for (FindingSummaryDTO finding : severityFindings) {
                    report.append("\nFile: ").append(finding.getFilePath()).append("\n");
                    report.append("Line: ").append(finding.getLineNumber()).append("\n");
                    report.append("Rule: ").append(finding.getRuleId()).append(" (").append(finding.getCategory()).append(")\n");
                    report.append("Message: ").append(finding.getMessage()).append("\n");
                    if (finding.getSuggestion() != null) {
                        report.append("Suggestion: ").append(finding.getSuggestion()).append("\n");
                    }
                }
                report.append("\n");
            }
        }
        
        report.append("=".repeat(80)).append("\n");
        report.append("END OF REPORT\n");
        report.append("=".repeat(80)).append("\n");
        
        // Write to file
        Path path = Path.of(outputPath);
        Files.writeString(path, report.toString());
        
        return path.toFile();
    }
    
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes
        return value.replace("\"", "\"\"");
    }
}
