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
 * Types of readiness issues that can be detected
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public enum ReadinessIssueType {
    
    // Structure issues
    EMPTY_COURSE("Course has no content nodes"),
    ORPHAN_NODE("Node has no valid parent"),
    INVALID_HIERARCHY("Invalid node hierarchy"),
    
    // Content issues
    MISSING_CONTENT("Course node has no content"),
    EMPTY_TITLE("Course node has no title"),
    BROKEN_REFERENCE("Referenced content not found"),
    
    // Resource issues
    MISSING_RESOURCE("Learning resource not found"),
    RESOURCE_UNAVAILABLE("Learning resource is unavailable"),
    
    // Access issues
    INVALID_CONDITION("Access condition expression is invalid"),
    CIRCULAR_DEPENDENCY("Circular dependency in access conditions"),
    UNREACHABLE_NODE("Node cannot be accessed"),
    
    // Assessment issues
    ASSESSMENT_UNCONFIGURED("Assessment node not configured"),
    INVALID_SCORE_RANGE("Invalid score range configured"),
    MISSING_GRADING("Grading not configured"),
    
    // Staff issues
    NO_OWNERS("Course has no owners"),
    NO_COACHES("Course has no coaches assigned"),
    
    // Enrollment issues
    NO_ENROLLMENT_METHOD("No enrollment method configured"),
    ENROLLMENT_CLOSED("Enrollment dates are in the past"),
    
    // Metadata issues
    MISSING_DESCRIPTION("Course has no description"),
    MISSING_OBJECTIVES("Learning objectives not defined"),
    
    // Date issues
    INVALID_DATE_RANGE("Invalid date range"),
    PAST_START_DATE("Start date is in the past"),
    
    // Certificate issues
    CERTIFICATE_UNCONFIGURED("Certificate template not configured"),
    
    // Link issues
    BROKEN_LINK("External link is broken"),
    
    // Media issues
    MISSING_MEDIA("Media file not found"),
    
    // Other
    CUSTOM("Custom validation issue");
    
    private final String description;
    
    ReadinessIssueType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isCritical() {
        return this == EMPTY_COURSE 
            || this == MISSING_RESOURCE 
            || this == NO_OWNERS
            || this == ASSESSMENT_UNCONFIGURED
            || this == BROKEN_REFERENCE;
    }
    
    public boolean isAutoFixable() {
        return this == EMPTY_TITLE 
            || this == MISSING_DESCRIPTION;
    }
}
