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
package org.olat.course.member;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * Enhancement 3: Bulk Staff Assignment
 * 
 * Result of a bulk staff assignment operation
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class BulkAssignmentResult {
    
    private final boolean success;
    private final int totalProcessed;
    private final int successCount;
    private final int failureCount;
    private final int skippedCount;
    private final List<AssignmentError> errors;
    private final List<Identity> successfulIdentities;
    private final List<Identity> failedIdentities;
    private final List<Identity> skippedIdentities;
    private final long executionTimeMs;
    
    private BulkAssignmentResult(Builder builder) {
        this.success = builder.success;
        this.totalProcessed = builder.totalProcessed;
        this.successCount = builder.successCount;
        this.failureCount = builder.failureCount;
        this.skippedCount = builder.skippedCount;
        this.errors = builder.errors;
        this.successfulIdentities = builder.successfulIdentities;
        this.failedIdentities = builder.failedIdentities;
        this.skippedIdentities = builder.skippedIdentities;
        this.executionTimeMs = builder.executionTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public int getTotalProcessed() {
        return totalProcessed;
    }
    
    public int getSuccessCount() {
        return successCount;
    }
    
    public int getFailureCount() {
        return failureCount;
    }
    
    public int getSkippedCount() {
        return skippedCount;
    }
    
    public List<AssignmentError> getErrors() {
        return errors;
    }
    
    public List<Identity> getSuccessfulIdentities() {
        return successfulIdentities;
    }
    
    public List<Identity> getFailedIdentities() {
        return failedIdentities;
    }
    
    public List<Identity> getSkippedIdentities() {
        return skippedIdentities;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean isPartialSuccess() {
        return success && failureCount > 0;
    }
    
    public double getSuccessRate() {
        if (totalProcessed == 0) return 100.0;
        return (double) successCount / totalProcessed * 100.0;
    }
    
    @Override
    public String toString() {
        return "BulkAssignmentResult{" +
                "success=" + success +
                ", processed=" + totalProcessed +
                ", succeeded=" + successCount +
                ", failed=" + failureCount +
                ", skipped=" + skippedCount +
                ", timeMs=" + executionTimeMs +
                '}';
    }
    
    /**
     * Error details for failed assignments
     */
    public static class AssignmentError {
        private final Identity identity;
        private final String errorCode;
        private final String errorMessage;
        
        public AssignmentError(Identity identity, String errorCode, String errorMessage) {
            this.identity = identity;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
        
        public Identity getIdentity() {
            return identity;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    public static class Builder {
        private boolean success = true;
        private int totalProcessed = 0;
        private int successCount = 0;
        private int failureCount = 0;
        private int skippedCount = 0;
        private List<AssignmentError> errors = new ArrayList<>();
        private List<Identity> successfulIdentities = new ArrayList<>();
        private List<Identity> failedIdentities = new ArrayList<>();
        private List<Identity> skippedIdentities = new ArrayList<>();
        private long executionTimeMs = 0;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder totalProcessed(int count) {
            this.totalProcessed = count;
            return this;
        }
        
        public Builder successCount(int count) {
            this.successCount = count;
            return this;
        }
        
        public Builder failureCount(int count) {
            this.failureCount = count;
            return this;
        }
        
        public Builder skippedCount(int count) {
            this.skippedCount = count;
            return this;
        }
        
        public Builder addError(AssignmentError error) {
            this.errors.add(error);
            return this;
        }
        
        public Builder addSuccessfulIdentity(Identity identity) {
            this.successfulIdentities.add(identity);
            this.successCount++;
            return this;
        }
        
        public Builder addFailedIdentity(Identity identity) {
            this.failedIdentities.add(identity);
            this.failureCount++;
            return this;
        }
        
        public Builder addSkippedIdentity(Identity identity) {
            this.skippedIdentities.add(identity);
            this.skippedCount++;
            return this;
        }
        
        public Builder executionTimeMs(long timeMs) {
            this.executionTimeMs = timeMs;
            return this;
        }
        
        public BulkAssignmentResult build() {
            this.totalProcessed = successCount + failureCount + skippedCount;
            if (failureCount > 0 && successCount == 0) {
                this.success = false;
            }
            return new BulkAssignmentResult(this);
        }
    }
}
