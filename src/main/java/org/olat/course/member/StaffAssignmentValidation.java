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
 * Validation result for staff assignment operations
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class StaffAssignmentValidation {
    
    private final boolean valid;
    private final List<ValidationIssue> issues;
    private final List<Identity> validIdentities;
    private final List<Identity> invalidIdentities;
    
    private StaffAssignmentValidation(boolean valid, List<ValidationIssue> issues,
            List<Identity> validIdentities, List<Identity> invalidIdentities) {
        this.valid = valid;
        this.issues = issues;
        this.validIdentities = validIdentities;
        this.invalidIdentities = invalidIdentities;
    }
    
    public static StaffAssignmentValidation valid(List<Identity> identities) {
        return new StaffAssignmentValidation(true, new ArrayList<>(), 
                new ArrayList<>(identities), new ArrayList<>());
    }
    
    public static StaffAssignmentValidation invalid(List<ValidationIssue> issues,
            List<Identity> validIdentities, List<Identity> invalidIdentities) {
        return new StaffAssignmentValidation(false, issues, validIdentities, invalidIdentities);
    }
    
    public static StaffAssignmentValidation withWarnings(List<ValidationIssue> issues,
            List<Identity> validIdentities, List<Identity> invalidIdentities) {
        return new StaffAssignmentValidation(true, issues, validIdentities, invalidIdentities);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<ValidationIssue> getIssues() {
        return issues;
    }
    
    public List<Identity> getValidIdentities() {
        return validIdentities;
    }
    
    public List<Identity> getInvalidIdentities() {
        return invalidIdentities;
    }
    
    public boolean hasWarnings() {
        return issues.stream().anyMatch(i -> i.getSeverity() == IssueSeverity.WARNING);
    }
    
    public boolean hasErrors() {
        return issues.stream().anyMatch(i -> i.getSeverity() == IssueSeverity.ERROR);
    }
    
    public int getWarningCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == IssueSeverity.WARNING).count();
    }
    
    public int getErrorCount() {
        return (int) issues.stream().filter(i -> i.getSeverity() == IssueSeverity.ERROR).count();
    }
    
    /**
     * A single validation issue
     */
    public static class ValidationIssue {
        private final IssueSeverity severity;
        private final String code;
        private final String message;
        private final Identity affectedIdentity;
        
        public ValidationIssue(IssueSeverity severity, String code, String message, Identity identity) {
            this.severity = severity;
            this.code = code;
            this.message = message;
            this.affectedIdentity = identity;
        }
        
        public IssueSeverity getSeverity() {
            return severity;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Identity getAffectedIdentity() {
            return affectedIdentity;
        }
    }
    
    public enum IssueSeverity {
        INFO,
        WARNING,
        ERROR
    }
}
