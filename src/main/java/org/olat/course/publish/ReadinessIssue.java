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

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Represents an individual readiness issue found during course validation
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class ReadinessIssue {
    
    public enum Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
    
    private final ReadinessIssueType type;
    private final ReadinessCheckType checkCategory;
    private final Severity severity;
    private final String message;
    private final String nodeId;
    private final String nodeName;
    private final String suggestedFix;
    private final boolean autoFixable;
    
    private ReadinessIssue(Builder builder) {
        this.type = builder.type;
        this.checkCategory = builder.checkCategory;
        this.severity = builder.severity;
        this.message = builder.message;
        this.nodeId = builder.nodeId;
        this.nodeName = builder.nodeName;
        this.suggestedFix = builder.suggestedFix;
        this.autoFixable = builder.autoFixable;
    }
    
    public ReadinessIssueType getType() {
        return type;
    }
    
    public ReadinessCheckType getCheckCategory() {
        return checkCategory;
    }
    
    public Severity getSeverity() {
        return severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public String getSuggestedFix() {
        return suggestedFix;
    }
    
    public boolean isAutoFixable() {
        return autoFixable;
    }
    
    public boolean isCritical() {
        return severity == Severity.CRITICAL;
    }
    
    public boolean isError() {
        return severity == Severity.ERROR || severity == Severity.CRITICAL;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder critical(ReadinessIssueType type, String message) {
        return new Builder()
            .type(type)
            .severity(Severity.CRITICAL)
            .message(message);
    }
    
    public static Builder error(ReadinessIssueType type, String message) {
        return new Builder()
            .type(type)
            .severity(Severity.ERROR)
            .message(message);
    }
    
    public static Builder warning(ReadinessIssueType type, String message) {
        return new Builder()
            .type(type)
            .severity(Severity.WARNING)
            .message(message);
    }
    
    public static Builder info(ReadinessIssueType type, String message) {
        return new Builder()
            .type(type)
            .severity(Severity.INFO)
            .message(message);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("] ");
        sb.append(type).append(": ").append(message);
        if (nodeName != null) {
            sb.append(" (Node: ").append(nodeName).append(")");
        }
        return sb.toString();
    }
    
    public static class Builder {
        private ReadinessIssueType type;
        private ReadinessCheckType checkCategory;
        private Severity severity = Severity.WARNING;
        private String message;
        private String nodeId;
        private String nodeName;
        private String suggestedFix;
        private boolean autoFixable = false;
        
        public Builder type(ReadinessIssueType type) {
            this.type = type;
            return this;
        }
        
        public Builder checkCategory(ReadinessCheckType checkCategory) {
            this.checkCategory = checkCategory;
            return this;
        }
        
        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder nodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }
        
        public Builder nodeName(String nodeName) {
            this.nodeName = nodeName;
            return this;
        }
        
        public Builder suggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }
        
        public Builder autoFixable(boolean autoFixable) {
            this.autoFixable = autoFixable;
            return this;
        }
        
        public ReadinessIssue build() {
            if (type == null) {
                throw new IllegalStateException("Issue type is required");
            }
            if (message == null || message.isEmpty()) {
                message = type.getDescription();
            }
            return new ReadinessIssue(this);
        }
    }
}
