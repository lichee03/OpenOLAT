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
package org.olat.course.member.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.member.BulkAssignmentResult;
import org.olat.course.member.BulkAssignmentResult.AssignmentError;
import org.olat.course.member.BulkStaffAssignmentService;
import org.olat.course.member.StaffAssignmentSummary;
import org.olat.course.member.StaffAssignmentValidation;
import org.olat.course.member.StaffAssignmentValidation.IssueSeverity;
import org.olat.course.member.StaffAssignmentValidation.ValidationIssue;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Enhancement 3: Bulk Staff Assignment
 * 
 * Implementation of bulk staff assignment service
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
@Service
public class BulkStaffAssignmentServiceImpl implements BulkStaffAssignmentService {
    
    private static final Logger log = Tracing.createLoggerFor(BulkStaffAssignmentServiceImpl.class);
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Override
    public BulkAssignmentResult assignStaffToCourse(RepositoryEntry course, List<Identity> identities, 
            GroupRoles role) {
        log.info("Assigning {} staff members to course {} with role {}", 
            identities.size(), course.getKey(), role);
        
        long startTime = System.currentTimeMillis();
        BulkAssignmentResult.Builder resultBuilder = BulkAssignmentResult.builder();
        
        // Validate first
        StaffAssignmentValidation validation = validateAssignment(course, identities, role);
        
        for (Identity identity : identities) {
            try {
                // Check if already assigned
                if (repositoryService.hasRole(identity, course, role.name())) {
                    resultBuilder.addSkippedIdentity(identity);
                    log.debug("Identity {} already has role {} in course {}", 
                        identity.getKey(), role, course.getKey());
                    continue;
                }
                
                // Perform assignment
                repositoryService.addRole(identity, course, role.name());
                resultBuilder.addSuccessfulIdentity(identity);
                log.debug("Assigned identity {} to course {} with role {}", 
                    identity.getKey(), course.getKey(), role);
                
            } catch (Exception e) {
                log.error("Failed to assign identity {} to course {}: {}", 
                    identity.getKey(), course.getKey(), e.getMessage());
                resultBuilder.addFailedIdentity(identity);
                resultBuilder.addError(new AssignmentError(identity, "ASSIGNMENT_FAILED", e.getMessage()));
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        resultBuilder.executionTimeMs(executionTime);
        
        BulkAssignmentResult result = resultBuilder.build();
        log.info("Bulk assignment completed: {}", result);
        
        return result;
    }
    
    @Override
    public BulkAssignmentResult assignStaffToMultipleCourses(List<RepositoryEntry> courses,
            List<Identity> identities, GroupRoles role) {
        log.info("Assigning {} staff members to {} courses with role {}", 
            identities.size(), courses.size(), role);
        
        long startTime = System.currentTimeMillis();
        BulkAssignmentResult.Builder resultBuilder = BulkAssignmentResult.builder();
        
        Set<Identity> successfulOverall = new HashSet<>();
        Set<Identity> failedOverall = new HashSet<>();
        
        for (RepositoryEntry course : courses) {
            BulkAssignmentResult courseResult = assignStaffToCourse(course, identities, role);
            
            // Track overall success/failure
            successfulOverall.addAll(courseResult.getSuccessfulIdentities());
            failedOverall.addAll(courseResult.getFailedIdentities());
            
            // Collect errors
            for (AssignmentError error : courseResult.getErrors()) {
                resultBuilder.addError(error);
            }
        }
        
        // Build aggregate result
        for (Identity identity : successfulOverall) {
            if (!failedOverall.contains(identity)) {
                resultBuilder.addSuccessfulIdentity(identity);
            }
        }
        for (Identity identity : failedOverall) {
            resultBuilder.addFailedIdentity(identity);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        resultBuilder.executionTimeMs(executionTime);
        
        return resultBuilder.build();
    }
    
    @Override
    public BulkAssignmentResult removeStaffFromCourse(RepositoryEntry course, List<Identity> identities,
            GroupRoles role) {
        log.info("Removing {} staff members from course {} with role {}", 
            identities.size(), course.getKey(), role);
        
        long startTime = System.currentTimeMillis();
        BulkAssignmentResult.Builder resultBuilder = BulkAssignmentResult.builder();
        
        for (Identity identity : identities) {
            try {
                // Check if has role
                if (!repositoryService.hasRole(identity, course, role.name())) {
                    resultBuilder.addSkippedIdentity(identity);
                    continue;
                }
                
                // Remove role
                repositoryService.removeRole(identity, course, role.name());
                resultBuilder.addSuccessfulIdentity(identity);
                
            } catch (Exception e) {
                log.error("Failed to remove identity {} from course {}: {}", 
                    identity.getKey(), course.getKey(), e.getMessage());
                resultBuilder.addFailedIdentity(identity);
                resultBuilder.addError(new AssignmentError(identity, "REMOVAL_FAILED", e.getMessage()));
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        resultBuilder.executionTimeMs(executionTime);
        
        return resultBuilder.build();
    }
    
    @Override
    public List<Identity> getStaffByRole(RepositoryEntry course, GroupRoles role) {
        return repositoryService.getMembers(course, RepositoryEntryRelationType.defaultGroup, role.name());
    }
    
    @Override
    public BulkAssignmentResult copyStaffAssignments(RepositoryEntry sourceCourse,
            RepositoryEntry targetCourse, Set<GroupRoles> roles) {
        log.info("Copying staff assignments from course {} to course {} for roles {}", 
            sourceCourse.getKey(), targetCourse.getKey(), roles);
        
        long startTime = System.currentTimeMillis();
        BulkAssignmentResult.Builder resultBuilder = BulkAssignmentResult.builder();
        
        for (GroupRoles role : roles) {
            List<Identity> staffWithRole = getStaffByRole(sourceCourse, role);
            BulkAssignmentResult roleResult = assignStaffToCourse(targetCourse, staffWithRole, role);
            
            // Aggregate results
            for (Identity identity : roleResult.getSuccessfulIdentities()) {
                resultBuilder.addSuccessfulIdentity(identity);
            }
            for (Identity identity : roleResult.getFailedIdentities()) {
                resultBuilder.addFailedIdentity(identity);
            }
            for (Identity identity : roleResult.getSkippedIdentities()) {
                resultBuilder.addSkippedIdentity(identity);
            }
            for (AssignmentError error : roleResult.getErrors()) {
                resultBuilder.addError(error);
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        resultBuilder.executionTimeMs(executionTime);
        
        return resultBuilder.build();
    }
    
    @Override
    public StaffAssignmentValidation validateAssignment(RepositoryEntry course,
            List<Identity> identities, GroupRoles role) {
        List<ValidationIssue> issues = new ArrayList<>();
        List<Identity> validIdentities = new ArrayList<>();
        List<Identity> invalidIdentities = new ArrayList<>();
        
        for (Identity identity : identities) {
            // Check if identity is valid
            if (identity == null || identity.getKey() == null) {
                issues.add(new ValidationIssue(IssueSeverity.ERROR, "INVALID_IDENTITY", 
                    "Identity is null or has no key", identity));
                invalidIdentities.add(identity);
                continue;
            }
            
            // Check if identity status is active
            if (identity.getStatus() != null && identity.getStatus() >= Identity.STATUS_DELETED) {
                issues.add(new ValidationIssue(IssueSeverity.ERROR, "IDENTITY_INACTIVE",
                    "Identity is not active", identity));
                invalidIdentities.add(identity);
                continue;
            }
            
            // Check if already has the role (warning only)
            if (repositoryService.hasRole(identity, course, role.name())) {
                issues.add(new ValidationIssue(IssueSeverity.WARNING, "ALREADY_ASSIGNED",
                    "Identity already has role " + role + " in course", identity));
            }
            
            validIdentities.add(identity);
        }
        
        if (!invalidIdentities.isEmpty()) {
            return StaffAssignmentValidation.invalid(issues, validIdentities, invalidIdentities);
        } else if (!issues.isEmpty()) {
            return StaffAssignmentValidation.withWarnings(issues, validIdentities, invalidIdentities);
        }
        
        return StaffAssignmentValidation.valid(validIdentities);
    }
    
    @Override
    public StaffAssignmentSummary getAssignmentSummary(RepositoryEntry course) {
        StaffAssignmentSummary.Builder builder = StaffAssignmentSummary.builder()
            .courseKey(course.getKey())
            .courseName(course.getDisplayname());
        
        // Count members by role
        for (GroupRoles role : GroupRoles.values()) {
            List<Identity> members = getStaffByRole(course, role);
            builder.setRoleCount(role, members.size());
        }
        
        return builder.build();
    }
}
