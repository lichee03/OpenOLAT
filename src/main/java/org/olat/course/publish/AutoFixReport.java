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
import java.util.List;

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Report of auto-fix operations performed on course issues
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class AutoFixReport {
    
    private final Long courseId;
    private final int totalAttempted;
    private final int successCount;
    private final int failureCount;
    private final List<FixResult> fixResults;
    private final long executionTimeMs;
    
    private AutoFixReport(Builder builder) {
        this.courseId = builder.courseId;
        this.totalAttempted = builder.totalAttempted;
        this.successCount = builder.successCount;
        this.failureCount = builder.failureCount;
        this.fixResults = Collections.unmodifiableList(new ArrayList<>(builder.fixResults));
        this.executionTimeMs = builder.executionTimeMs;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public int getTotalAttempted() {
        return totalAttempted;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    public List<FixResult> getFixResults() {
        return fixResults;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public boolean hasFailures() {
        return failureCount > 0;
    }
    
    public boolean isFullySuccessful() {
        return failureCount == 0 && totalAttempted > 0;
    }
    
    public double getSuccessRate() {
        if (totalAttempted == 0) {
            return 100.0;
        }
        return (successCount * 100.0) / totalAttempted;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static AutoFixReport empty(Long courseId) {
        return builder()
            .courseId(courseId)
            .totalAttempted(0)
            .successCount(0)
            .failureCount(0)
            .build();
    }
    
    /**
     * Result of an individual fix operation
     */
    public static class FixResult {
        private final ReadinessIssue originalIssue;
        private final boolean success;
        private final String action;
        private final String errorMessage;
        
        private FixResult(ReadinessIssue originalIssue, boolean success, String action, String errorMessage) {
            this.originalIssue = originalIssue;
            this.success = success;
            this.action = action;
            this.errorMessage = errorMessage;
        }
        
        public static FixResult success(ReadinessIssue issue, String action) {
            return new FixResult(issue, true, action, null);
        }
        
        public static FixResult failure(ReadinessIssue issue, String action, String errorMessage) {
            return new FixResult(issue, false, action, errorMessage);
        }
        
        public ReadinessIssue getOriginalIssue() {
            return originalIssue;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getAction() {
            return action;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String toString() {
            if (success) {
                return "Fixed: " + originalIssue.getType() + " - " + action;
            } else {
                return "Failed: " + originalIssue.getType() + " - " + errorMessage;
            }
        }
    }
    
    public static class Builder {
        private Long courseId;
        private int totalAttempted;
        private int successCount;
        private int failureCount;
        private List<FixResult> fixResults = new ArrayList<>();
        private long executionTimeMs;
        
        public Builder courseId(Long courseId) {
            this.courseId = courseId;
            return this;
        }
        
        public Builder totalAttempted(int totalAttempted) {
            this.totalAttempted = totalAttempted;
            return this;
        }
        
        public Builder successCount(int successCount) {
            this.successCount = successCount;
            return this;
        }
        
        public Builder failureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }
        
        public Builder addFixResult(FixResult result) {
            this.fixResults.add(result);
            if (result.isSuccess()) {
                this.successCount++;
            } else {
                this.failureCount++;
            }
            this.totalAttempted++;
            return this;
        }
        
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public AutoFixReport build() {
            return new AutoFixReport(this);
        }
    }
}
