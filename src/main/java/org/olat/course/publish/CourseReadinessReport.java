/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.publish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Complete readiness report for a course containing all issues found
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class CourseReadinessReport {
    
    private final Long courseId;
    private final String courseTitle;
    private final boolean ready;
    private final List<ReadinessIssue> issues;
    private final Map<ReadinessCheckType, Boolean> checkResults;
    private final long checkDurationMs;
    private final long timestamp;
    
    private CourseReadinessReport(Builder builder) {
        this.courseId = builder.courseId;
        this.courseTitle = builder.courseTitle;
        this.issues = Collections.unmodifiableList(new ArrayList<>(builder.issues));
        this.checkResults = Collections.unmodifiableMap(new EnumMap<>(builder.checkResults));
        this.checkDurationMs = builder.checkDurationMs;
        this.timestamp = builder.timestamp;
        this.ready = calculateReady();
    }
    
    private boolean calculateReady() {
        return issues.stream().noneMatch(ReadinessIssue::isError);
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public String getCourseTitle() {
        return courseTitle;
    }
    
    public boolean isReady() {
        return ready;
    }
    
    public List<ReadinessIssue> getIssues() {
        return issues;
    }
    
    public Map<ReadinessCheckType, Boolean> getCheckResults() {
        return checkResults;
    }
    
    public long getCheckDurationMs() {
        return checkDurationMs;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public List<ReadinessIssue> getCriticalIssues() {
        return issues.stream()
            .filter(ReadinessIssue::isCritical)
            .collect(Collectors.toList());
    }
    
    public List<ReadinessIssue> getErrors() {
        return issues.stream()
            .filter(ReadinessIssue::isError)
            .collect(Collectors.toList());
    }
    
    public List<ReadinessIssue> getWarnings() {
        return issues.stream()
            .filter(i -> i.getSeverity() == ReadinessIssue.Severity.WARNING)
            .collect(Collectors.toList());
    }
    
    public List<ReadinessIssue> getAutoFixableIssues() {
        return issues.stream()
            .filter(ReadinessIssue::isAutoFixable)
            .collect(Collectors.toList());
    }
    
    public List<ReadinessIssue> getIssuesByCategory(ReadinessCheckType category) {
        return issues.stream()
            .filter(i -> i.getCheckCategory() == category)
            .collect(Collectors.toList());
    }
    
    public int getTotalIssueCount() {
        return issues.size();
    }
    
    public int getCriticalCount() {
        return (int) issues.stream().filter(ReadinessIssue::isCritical).count();
    }
    
    public int getErrorCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == ReadinessIssue.Severity.ERROR).count();
    }
    
    public int getWarningCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == ReadinessIssue.Severity.WARNING).count();
    }
    
    public double getReadinessScore() {
        if (issues.isEmpty()) {
            return 100.0;
        }
        
        int totalWeight = 0;
        int deductions = 0;
        
        for (ReadinessIssue issue : issues) {
            int weight;
            switch (issue.getSeverity()) {
                case CRITICAL:
                    weight = 25;
                    break;
                case ERROR:
                    weight = 15;
                    break;
                case WARNING:
                    weight = 5;
                    break;
                default:
                    weight = 1;
            }
            deductions += weight;
            totalWeight += weight;
        }
        
        double score = Math.max(0, 100.0 - deductions);
        return Math.round(score * 10.0) / 10.0;
    }
    
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Course Readiness Report: ").append(courseTitle).append("\n");
        sb.append("Status: ").append(ready ? "READY" : "NOT READY").append("\n");
        sb.append("Score: ").append(getReadinessScore()).append("%\n");
        sb.append("Issues: ").append(getTotalIssueCount());
        sb.append(" (Critical: ").append(getCriticalCount());
        sb.append(", Errors: ").append(getErrorCount());
        sb.append(", Warnings: ").append(getWarningCount()).append(")\n");
        return sb.toString();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long courseId;
        private String courseTitle;
        private List<ReadinessIssue> issues = new ArrayList<>();
        private Map<ReadinessCheckType, Boolean> checkResults = new EnumMap<>(ReadinessCheckType.class);
        private long checkDurationMs;
        private long timestamp = System.currentTimeMillis();
        
        public Builder courseId(Long courseId) {
            this.courseId = courseId;
            return this;
        }
        
        public Builder courseTitle(String courseTitle) {
            this.courseTitle = courseTitle;
            return this;
        }
        
        public Builder addIssue(ReadinessIssue issue) {
            this.issues.add(issue);
            return this;
        }
        
        public Builder addIssues(List<ReadinessIssue> issues) {
            this.issues.addAll(issues);
            return this;
        }
        
        public Builder checkResult(ReadinessCheckType type, boolean passed) {
            this.checkResults.put(type, passed);
            return this;
        }
        
        public Builder checkDurationMs(long checkDurationMs) {
            this.checkDurationMs = checkDurationMs;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public CourseReadinessReport build() {
            return new CourseReadinessReport(this);
        }
    }
}
