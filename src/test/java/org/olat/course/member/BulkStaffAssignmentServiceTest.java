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
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.course.member.BulkAssignmentResult.AssignmentError;
import org.olat.course.member.StaffAssignmentValidation.IssueSeverity;
import org.olat.course.member.StaffAssignmentValidation.ValidationIssue;

/**
 * Enhancement 3: Unit tests for Bulk Staff Assignment
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class BulkStaffAssignmentServiceTest {
    
    @Test
    public void testBulkAssignmentResultBuilder() {
        BulkAssignmentResult result = BulkAssignmentResult.builder()
            .successCount(5)
            .failureCount(2)
            .skippedCount(1)
            .executionTimeMs(100)
            .build();
        
        Assert.assertNotNull(result);
        Assert.assertEquals(8, result.getTotalProcessed());
        Assert.assertEquals(5, result.getSuccessCount());
        Assert.assertEquals(2, result.getFailureCount());
        Assert.assertEquals(1, result.getSkippedCount());
        Assert.assertEquals(100, result.getExecutionTimeMs());
    }
    
    @Test
    public void testBulkAssignmentResultSuccess() {
        BulkAssignmentResult result = BulkAssignmentResult.builder()
            .successCount(10)
            .failureCount(0)
            .build();
        
        Assert.assertTrue(result.isSuccess());
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(result.isPartialSuccess());
        Assert.assertEquals(100.0, result.getSuccessRate(), 0.01);
    }
    
    @Test
    public void testBulkAssignmentResultPartialSuccess() {
        BulkAssignmentResult result = BulkAssignmentResult.builder()
            .success(true)
            .successCount(8)
            .failureCount(2)
            .build();
        
        Assert.assertTrue(result.isSuccess());
        Assert.assertTrue(result.isPartialSuccess());
        Assert.assertEquals(80.0, result.getSuccessRate(), 0.01);
    }
    
    @Test
    public void testBulkAssignmentResultFailure() {
        BulkAssignmentResult result = BulkAssignmentResult.builder()
            .successCount(0)
            .failureCount(5)
            .build();
        
        Assert.assertFalse(result.isSuccess());
        Assert.assertEquals(0.0, result.getSuccessRate(), 0.01);
    }
    
    @Test
    public void testAssignmentError() {
        AssignmentError error = new AssignmentError(null, "TEST_ERROR", "Test error message");
        
        Assert.assertNull(error.getIdentity());
        Assert.assertEquals("TEST_ERROR", error.getErrorCode());
        Assert.assertEquals("Test error message", error.getErrorMessage());
    }
    
    @Test
    public void testStaffAssignmentSummaryBuilder() {
        StaffAssignmentSummary summary = StaffAssignmentSummary.builder()
            .courseKey(123L)
            .courseName("Test Course")
            .setRoleCount(GroupRoles.owner, 2)
            .setRoleCount(GroupRoles.coach, 5)
            .setRoleCount(GroupRoles.participant, 50)
            .build();
        
        Assert.assertNotNull(summary);
        Assert.assertEquals(Long.valueOf(123L), summary.getCourseKey());
        Assert.assertEquals("Test Course", summary.getCourseName());
        Assert.assertEquals(2, summary.getOwnerCount());
        Assert.assertEquals(5, summary.getCoachCount());
        Assert.assertEquals(50, summary.getParticipantCount());
        Assert.assertEquals(57, summary.getTotalStaffCount());
    }
    
    @Test
    public void testStaffAssignmentSummaryFlags() {
        StaffAssignmentSummary withOwners = StaffAssignmentSummary.builder()
            .setRoleCount(GroupRoles.owner, 1)
            .build();
        Assert.assertTrue(withOwners.hasOwners());
        Assert.assertFalse(withOwners.hasCoaches());
        
        StaffAssignmentSummary withCoaches = StaffAssignmentSummary.builder()
            .setRoleCount(GroupRoles.coach, 3)
            .build();
        Assert.assertTrue(withCoaches.hasCoaches());
        Assert.assertFalse(withCoaches.hasOwners());
    }
    
    @Test
    public void testStaffAssignmentValidationValid() {
        StaffAssignmentValidation validation = StaffAssignmentValidation.valid(new ArrayList<>());
        
        Assert.assertTrue(validation.isValid());
        Assert.assertFalse(validation.hasErrors());
        Assert.assertFalse(validation.hasWarnings());
        Assert.assertEquals(0, validation.getErrorCount());
        Assert.assertEquals(0, validation.getWarningCount());
    }
    
    @Test
    public void testStaffAssignmentValidationWithWarnings() {
        List<ValidationIssue> issues = Arrays.asList(
            new ValidationIssue(IssueSeverity.WARNING, "WARN1", "Warning 1", null),
            new ValidationIssue(IssueSeverity.WARNING, "WARN2", "Warning 2", null)
        );
        
        StaffAssignmentValidation validation = StaffAssignmentValidation.withWarnings(
            issues, new ArrayList<>(), new ArrayList<>());
        
        Assert.assertTrue(validation.isValid());
        Assert.assertTrue(validation.hasWarnings());
        Assert.assertFalse(validation.hasErrors());
        Assert.assertEquals(2, validation.getWarningCount());
    }
    
    @Test
    public void testStaffAssignmentValidationInvalid() {
        List<ValidationIssue> issues = Arrays.asList(
            new ValidationIssue(IssueSeverity.ERROR, "ERR1", "Error 1", null)
        );
        
        StaffAssignmentValidation validation = StaffAssignmentValidation.invalid(
            issues, new ArrayList<>(), new ArrayList<>());
        
        Assert.assertFalse(validation.isValid());
        Assert.assertTrue(validation.hasErrors());
        Assert.assertEquals(1, validation.getErrorCount());
    }
    
    @Test
    public void testValidationIssue() {
        ValidationIssue issue = new ValidationIssue(
            IssueSeverity.ERROR, "TEST_CODE", "Test message", null);
        
        Assert.assertEquals(IssueSeverity.ERROR, issue.getSeverity());
        Assert.assertEquals("TEST_CODE", issue.getCode());
        Assert.assertEquals("Test message", issue.getMessage());
        Assert.assertNull(issue.getAffectedIdentity());
    }
    
    @Test
    public void testIssueSeverityEnum() {
        IssueSeverity[] severities = IssueSeverity.values();
        
        Assert.assertEquals(3, severities.length);
        Assert.assertEquals(IssueSeverity.INFO, IssueSeverity.valueOf("INFO"));
        Assert.assertEquals(IssueSeverity.WARNING, IssueSeverity.valueOf("WARNING"));
        Assert.assertEquals(IssueSeverity.ERROR, IssueSeverity.valueOf("ERROR"));
    }
    
    @Test
    public void testGroupRolesEnum() {
        // Test that required roles exist
        Assert.assertNotNull(GroupRoles.owner);
        Assert.assertNotNull(GroupRoles.coach);
        Assert.assertNotNull(GroupRoles.participant);
    }
    
    @Test
    public void testBulkAssignmentResultToString() {
        BulkAssignmentResult result = BulkAssignmentResult.builder()
            .successCount(5)
            .failureCount(1)
            .skippedCount(2)
            .executionTimeMs(50)
            .build();
        
        String str = result.toString();
        Assert.assertTrue(str.contains("success="));
        Assert.assertTrue(str.contains("processed=8"));
        Assert.assertTrue(str.contains("succeeded=5"));
    }
    
    @Test
    public void testStaffAssignmentSummaryToString() {
        StaffAssignmentSummary summary = StaffAssignmentSummary.builder()
            .courseKey(100L)
            .setRoleCount(GroupRoles.owner, 1)
            .setRoleCount(GroupRoles.coach, 2)
            .build();
        
        String str = summary.toString();
        Assert.assertTrue(str.contains("courseKey=100"));
        Assert.assertTrue(str.contains("owners=1"));
        Assert.assertTrue(str.contains("coaches=2"));
    }
    
    @Test
    public void testStaffAssignmentSummaryIncrementRoleCount() {
        StaffAssignmentSummary.Builder builder = StaffAssignmentSummary.builder();
        builder.incrementRoleCount(GroupRoles.coach);
        builder.incrementRoleCount(GroupRoles.coach);
        builder.incrementRoleCount(GroupRoles.coach);
        
        StaffAssignmentSummary summary = builder.build();
        Assert.assertEquals(3, summary.getCoachCount());
    }
}
