package app.infra.integration;

import app.config.AppConfig;
import app.domain.entity.DiffFile;
import app.domain.port.DiffEngine;
import app.domain.port.DiffException;
import app.domain.value.FileChangeType;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Git diff engine implementation using ProcessBuilder to execute git commands.
 */
public class GitDiffEngine implements DiffEngine {
    
    private final String gitExecutable;
    
    public GitDiffEngine() {
        this.gitExecutable = AppConfig.getInstance().getGitExecutable();
    }
    
    @Override
    public List<DiffFile> calculateDiff(String repositoryPath, String baseBranch, String targetBranch) 
            throws DiffException {
        
        if (!isValidRepository(repositoryPath)) {
            throw new DiffException("Invalid repository path: " + repositoryPath);
        }
        
        List<String> command = List.of(
            gitExecutable,
            "-C", repositoryPath,
            "diff",
            "--numstat",
            "--summary",
            baseBranch + ".." + targetBranch
        );
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(repositoryPath));
            Process process = pb.start();
            
            List<DiffFile> diffFiles = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    DiffFile file = parseDiffLine(line);
                    if (file != null) {
                        diffFiles.add(file);
                    }
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder error = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        error.append(errorLine).append("\n");
                    }
                    throw new DiffException("Git diff failed: " + error.toString());
                }
            }
            
            return diffFiles;
            
        } catch (Exception e) {
            throw new DiffException("Failed to execute git diff", e);
        }
    }
    
    @Override
    public boolean isValidRepository(String repositoryPath) {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists() || !repoDir.isDirectory()) {
            return false;
        }
        
        File gitDir = new File(repoDir, ".git");
        return gitDir.exists() && gitDir.isDirectory();
    }
    
    @Override
    public List<String> getBranches(String repositoryPath) throws DiffException {
        if (!isValidRepository(repositoryPath)) {
            throw new DiffException("Invalid repository path: " + repositoryPath);
        }
        
        // Get both local and remote branches
        List<String> branches = new ArrayList<>();
        
        // Get local branches
        branches.addAll(getLocalBranches(repositoryPath));
        
        // Get remote branches
        branches.addAll(getRemoteBranches(repositoryPath));
        
        return branches;
    }
    
    /**
     * Get list of local branches.
     */
    private List<String> getLocalBranches(String repositoryPath) throws DiffException {
        List<String> command = List.of(
            gitExecutable,
            "-C", repositoryPath,
            "branch",
            "--list",
            "--format=%(refname:short)"
        );
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(repositoryPath));
            Process process = pb.start();
            
            List<String> branches = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    String branch = line.trim();
                    if (!branch.isEmpty()) {
                        branches.add(branch);
                    }
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    StringBuilder error = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        error.append(errorLine).append("\n");
                    }
                    throw new DiffException("Failed to list local branches: " + error.toString());
                }
            }
            
            return branches;
            
        } catch (Exception e) {
            throw new DiffException("Failed to get local branches", e);
        }
    }
    
    /**
     * Get list of remote branches (without duplicates).
     */
    private List<String> getRemoteBranches(String repositoryPath) throws DiffException {
        List<String> command = List.of(
            gitExecutable,
            "-C", repositoryPath,
            "branch",
            "-r",
            "--list",
            "--format=%(refname:short)"
        );
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(repositoryPath));
            Process process = pb.start();
            
            List<String> branches = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    String branch = line.trim();
                    // Filter out HEAD references
                    if (!branch.isEmpty() && !branch.contains("HEAD")) {
                        branches.add(branch);
                    }
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Remote branches might not exist, return empty list instead of throwing
                return new ArrayList<>();
            }
            
            return branches;
            
        } catch (Exception e) {
            // If remote listing fails, just return empty list (repo might not have remotes)
            return new ArrayList<>();
        }
    }
    
    /**
     * Parse a line from git diff --numstat --summary output.
     * Format: <added> <removed> <filename>
     * Or summary lines like: create mode 100644 <filename>
     */
    private DiffFile parseDiffLine(String line) {
        if (line.trim().isEmpty()) {
            return null;
        }
        
        // Check for summary lines (create, delete, rename, copy)
        if (line.startsWith(" create mode")) {
            String path = line.substring(line.lastIndexOf(' ') + 1);
            DiffFile file = new DiffFile(path, FileChangeType.ADDED);
            return file;
        }
        
        if (line.startsWith(" delete mode")) {
            String path = line.substring(line.lastIndexOf(' ') + 1);
            DiffFile file = new DiffFile(path, FileChangeType.DELETED);
            return file;
        }
        
        if (line.startsWith(" rename ")) {
            // Format: rename <old> => <new> (similarity%)
            int arrowPos = line.indexOf(" => ");
            if (arrowPos > 0) {
                int startPos = line.indexOf(' ', 1) + 1;
                String oldPath = line.substring(startPos, arrowPos).trim();
                int endPos = line.indexOf(" (", arrowPos);
                if (endPos < 0) endPos = line.length();
                String newPath = line.substring(arrowPos + 4, endPos).trim();
                
                DiffFile file = new DiffFile(newPath, FileChangeType.RENAMED);
                file.setOldPath(oldPath);
                return file;
            }
        }
        
        if (line.startsWith(" copy ")) {
            int arrowPos = line.indexOf(" => ");
            if (arrowPos > 0) {
                int endPos = line.indexOf(" (", arrowPos);
                if (endPos < 0) endPos = line.length();
                String newPath = line.substring(arrowPos + 4, endPos).trim();
                
                DiffFile file = new DiffFile(newPath, FileChangeType.COPIED);
                return file;
            }
        }
        
        // Parse numstat line: <added> <removed> <filename>
        String[] parts = line.split("\\s+", 3);
        if (parts.length == 3) {
            try {
                int added = parts[0].equals("-") ? 0 : Integer.parseInt(parts[0]);
                int removed = parts[1].equals("-") ? 0 : Integer.parseInt(parts[1]);
                String path = parts[2];
                
                DiffFile file = new DiffFile(path, FileChangeType.MODIFIED);
                file.setLinesAdded(added);
                file.setLinesRemoved(removed);
                return file;
            } catch (NumberFormatException e) {
                // Not a valid numstat line, skip
            }
        }
        
        return null;
    }
}
