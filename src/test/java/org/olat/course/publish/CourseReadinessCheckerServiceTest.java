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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Unit tests for course readiness checker models
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class CourseReadinessCheckerServiceTest {
    
    // ========== ReadinessIssueType Tests ==========
    
    @Test
    public void testReadinessIssueTypeValues() {
        // Verify enum values exist
        assertNotNull(ReadinessIssueType.EMPTY_COURSE);
        assertNotNull(ReadinessIssueType.MISSING_CONTENT);
        assertNotNull(ReadinessIssueType.EMPTY_TITLE);
        assertNotNull(ReadinessIssueType.NO_OWNERS);
        assertNotNull(ReadinessIssueType.ASSESSMENT_UNCONFIGURED);
    }
    
    @Test
    public void testReadinessIssueTypeDescription() {
        assertEquals("Course has no content nodes", 
            ReadinessIssueType.EMPTY_COURSE.getDescription());
        assertEquals("Node has no valid parent", 
            ReadinessIssueType.ORPHAN_NODE.getDescription());
    }
    
    @Test
    public void testReadinessIssueTypeCritical() {
        assertTrue(ReadinessIssueType.EMPTY_COURSE.isCritical());
        assertTrue(ReadinessIssueType.MISSING_RESOURCE.isCritical());
        assertTrue(ReadinessIssueType.NO_OWNERS.isCritical());
        assertFalse(ReadinessIssueType.MISSING_DESCRIPTION.isCritical());
    }
    
    @Test
    public void testReadinessIssueTypeAutoFixable() {
        assertTrue(ReadinessIssueType.EMPTY_TITLE.isAutoFixable());
        assertTrue(ReadinessIssueType.MISSING_DESCRIPTION.isAutoFixable());
        assertFalse(ReadinessIssueType.EMPTY_COURSE.isAutoFixable());
    }
    
    // ========== ReadinessCheckType Tests ==========
    
    @Test
    public void testReadinessCheckTypeValues() {
        assertNotNull(ReadinessCheckType.STRUCTURE);
        assertNotNull(ReadinessCheckType.CONTENT);
        assertNotNull(ReadinessCheckType.RESOURCES);
        assertNotNull(ReadinessCheckType.ASSESSMENTS);
        assertNotNull(ReadinessCheckType.STAFF);
        assertNotNull(ReadinessCheckType.ALL);
    }
    
    // ========== ReadinessIssue Tests ==========
    
    @Test
    public void testReadinessIssueBuilder() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .severity(ReadinessIssue.Severity.WARNING)
            .message("Test node has no title")
            .nodeId("12345")
            .nodeName("Test Node")
            .build();
        
        assertEquals(ReadinessIssueType.EMPTY_TITLE, issue.getType());
        assertEquals(ReadinessIssue.Severity.WARNING, issue.getSeverity());
        assertEquals("Test node has no title", issue.getMessage());
        assertEquals("12345", issue.getNodeId());
        assertEquals("Test Node", issue.getNodeName());
    }
    
    @Test
    public void testReadinessIssueCriticalFactory() {
        ReadinessIssue issue = ReadinessIssue
            .critical(ReadinessIssueType.EMPTY_COURSE, "Course is empty")
            .build();
        
        assertEquals(ReadinessIssue.Severity.CRITICAL, issue.getSeverity());
        assertTrue(issue.isCritical());
        assertTrue(issue.isError());
    }
    
    @Test
    public void testReadinessIssueErrorFactory() {
        ReadinessIssue issue = ReadinessIssue
            .error(ReadinessIssueType.ASSESSMENT_UNCONFIGURED, "Test not configured")
            .build();
        
        assertEquals(ReadinessIssue.Severity.ERROR, issue.getSeverity());
        assertFalse(issue.isCritical());
        assertTrue(issue.isError());
    }
    
    @Test
    public void testReadinessIssueWarningFactory() {
        ReadinessIssue issue = ReadinessIssue
            .warning(ReadinessIssueType.MISSING_DESCRIPTION, "No description")
            .build();
        
        assertEquals(ReadinessIssue.Severity.WARNING, issue.getSeverity());
        assertFalse(issue.isCritical());
        assertFalse(issue.isError());
    }
    
    @Test
    public void testReadinessIssueInfoFactory() {
        ReadinessIssue issue = ReadinessIssue
            .info(ReadinessIssueType.CUSTOM, "Informational message")
            .build();
        
        assertEquals(ReadinessIssue.Severity.INFO, issue.getSeverity());
    }
    
    @Test
    public void testReadinessIssueAutoFixable() {
        ReadinessIssue fixable = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .autoFixable(true)
            .build();
        
        ReadinessIssue notFixable = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_COURSE)
            .autoFixable(false)
            .build();
        
        assertTrue(fixable.isAutoFixable());
        assertFalse(notFixable.isAutoFixable());
    }
    
    @Test
    public void testReadinessIssueDefaultMessage() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_COURSE)
            .build();
        
        // Should use type description as default message
        assertEquals("Course has no content nodes", issue.getMessage());
    }
    
    @Test
    public void testReadinessIssueSuggestedFix() {
        String fix = "Add content to the course";
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_COURSE)
            .suggestedFix(fix)
            .build();
        
        assertEquals(fix, issue.getSuggestedFix());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testReadinessIssueBuilderRequiresType() {
        ReadinessIssue.builder()
            .message("Test message")
            .build();
    }
    
    // ========== CourseReadinessReport Tests ==========
    
    @Test
    public void testCourseReadinessReportBuilder() {
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .build();
        
        assertEquals(Long.valueOf(123L), report.getCourseId());
        assertEquals("Test Course", report.getCourseTitle());
        assertTrue(report.isReady()); // No issues = ready
    }
    
    @Test
    public void testCourseReadinessReportWithIssues() {
        ReadinessIssue warning = ReadinessIssue
            .warning(ReadinessIssueType.MISSING_DESCRIPTION, "No description")
            .build();
        
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .addIssue(warning)
            .build();
        
        assertTrue(report.isReady()); // Warnings don't block readiness
        assertEquals(1, report.getTotalIssueCount());
        assertEquals(1, report.getWarningCount());
    }
    
    @Test
    public void testCourseReadinessReportNotReady() {
        ReadinessIssue error = ReadinessIssue
            .error(ReadinessIssueType.ASSESSMENT_UNCONFIGURED, "Not configured")
            .build();
        
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .addIssue(error)
            .build();
        
        assertFalse(report.isReady()); // Errors block readiness
        assertEquals(1, report.getErrorCount());
    }
    
    @Test
    public void testCourseReadinessReportCriticalIssues() {
        ReadinessIssue critical = ReadinessIssue
            .critical(ReadinessIssueType.EMPTY_COURSE, "Empty course")
            .build();
        ReadinessIssue warning = ReadinessIssue
            .warning(ReadinessIssueType.MISSING_DESCRIPTION, "No description")
            .build();
        
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .addIssue(critical)
            .addIssue(warning)
            .build();
        
        assertEquals(1, report.getCriticalCount());
        assertEquals(1, report.getCriticalIssues().size());
        assertEquals(ReadinessIssueType.EMPTY_COURSE, 
            report.getCriticalIssues().get(0).getType());
    }
    
    @Test
    public void testCourseReadinessReportAutoFixable() {
        ReadinessIssue fixable = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .autoFixable(true)
            .build();
        ReadinessIssue notFixable = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_COURSE)
            .autoFixable(false)
            .build();
        
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .addIssue(fixable)
            .addIssue(notFixable)
            .build();
        
        assertEquals(1, report.getAutoFixableIssues().size());
    }
    
    @Test
    public void testCourseReadinessReportScore() {
        // No issues = 100%
        CourseReadinessReport perfect = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Perfect Course")
            .build();
        assertEquals(100.0, perfect.getReadinessScore(), 0.01);
        
        // Issues reduce score
        CourseReadinessReport withIssues = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .addIssue(ReadinessIssue.warning(ReadinessIssueType.MISSING_DESCRIPTION, "test").build())
            .build();
        assertTrue(withIssues.getReadinessScore() < 100.0);
    }
    
    @Test
    public void testCourseReadinessReportCheckResults() {
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .checkResult(ReadinessCheckType.STRUCTURE, true)
            .checkResult(ReadinessCheckType.CONTENT, false)
            .build();
        
        assertTrue(report.getCheckResults().get(ReadinessCheckType.STRUCTURE));
        assertFalse(report.getCheckResults().get(ReadinessCheckType.CONTENT));
    }
    
    @Test
    public void testCourseReadinessReportIssuesByCategory() {
        ReadinessIssue structureIssue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_COURSE)
            .checkCategory(ReadinessCheckType.STRUCTURE)
            .build();
        ReadinessIssue contentIssue = ReadinessIssue.builder()
            .type(ReadinessIssueType.MISSING_CONTENT)
            .checkCategory(ReadinessCheckType.CONTENT)
            .build();
        
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .addIssue(structureIssue)
            .addIssue(contentIssue)
            .build();
        
        assertEquals(1, report.getIssuesByCategory(ReadinessCheckType.STRUCTURE).size());
        assertEquals(1, report.getIssuesByCategory(ReadinessCheckType.CONTENT).size());
        assertEquals(0, report.getIssuesByCategory(ReadinessCheckType.STAFF).size());
    }
    
    @Test
    public void testCourseReadinessReportSummary() {
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .build();
        
        String summary = report.getSummary();
        assertTrue(summary.contains("Test Course"));
        assertTrue(summary.contains("READY"));
    }
    
    @Test
    public void testCourseReadinessReportDuration() {
        CourseReadinessReport report = CourseReadinessReport.builder()
            .courseId(123L)
            .courseTitle("Test Course")
            .checkDurationMs(150L)
            .build();
        
        assertEquals(150L, report.getCheckDurationMs());
    }
    
    // ========== AutoFixReport Tests ==========
    
    @Test
    public void testAutoFixReportBuilder() {
        AutoFixReport report = AutoFixReport.builder()
            .courseId(123L)
            .totalAttempted(3)
            .successCount(2)
            .failureCount(1)
            .executionTimeMs(50L)
            .build();
        
        assertEquals(Long.valueOf(123L), report.getCourseId());
        assertEquals(3, report.getTotalAttempted());
        assertEquals(2, report.getSuccessCount());
        assertEquals(1, report.getFailureCount());
    }
    
    @Test
    public void testAutoFixReportEmpty() {
        AutoFixReport report = AutoFixReport.empty(123L);
        
        assertEquals(Long.valueOf(123L), report.getCourseId());
        assertEquals(0, report.getTotalAttempted());
        assertFalse(report.hasFailures());
    }
    
    @Test
    public void testAutoFixReportAddResults() {
        ReadinessIssue issue1 = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .build();
        ReadinessIssue issue2 = ReadinessIssue.builder()
            .type(ReadinessIssueType.MISSING_DESCRIPTION)
            .build();
        
        AutoFixReport report = AutoFixReport.builder()
            .courseId(123L)
            .addFixResult(AutoFixReport.FixResult.success(issue1, "Fixed title"))
            .addFixResult(AutoFixReport.FixResult.failure(issue2, "Attempted fix", "Failed"))
            .executionTimeMs(100L)
            .build();
        
        assertEquals(2, report.getTotalAttempted());
        assertEquals(1, report.getSuccessCount());
        assertEquals(1, report.getFailureCount());
        assertTrue(report.hasFailures());
        assertFalse(report.isFullySuccessful());
    }
    
    @Test
    public void testAutoFixReportFullySuccessful() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .build();
        
        AutoFixReport report = AutoFixReport.builder()
            .courseId(123L)
            .addFixResult(AutoFixReport.FixResult.success(issue, "Fixed title"))
            .build();
        
        assertTrue(report.isFullySuccessful());
        assertFalse(report.hasFailures());
    }
    
    @Test
    public void testAutoFixReportSuccessRate() {
        ReadinessIssue issue1 = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .build();
        ReadinessIssue issue2 = ReadinessIssue.builder()
            .type(ReadinessIssueType.MISSING_DESCRIPTION)
            .build();
        
        AutoFixReport report = AutoFixReport.builder()
            .courseId(123L)
            .addFixResult(AutoFixReport.FixResult.success(issue1, "Fixed"))
            .addFixResult(AutoFixReport.FixResult.failure(issue2, "Fix", "Error"))
            .build();
        
        assertEquals(50.0, report.getSuccessRate(), 0.01);
    }
    
    @Test
    public void testAutoFixResultSuccess() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .build();
        
        AutoFixReport.FixResult result = AutoFixReport.FixResult.success(issue, "Set default title");
        
        assertTrue(result.isSuccess());
        assertEquals(issue, result.getOriginalIssue());
        assertEquals("Set default title", result.getAction());
        assertNull(result.getErrorMessage());
    }
    
    @Test
    public void testAutoFixResultFailure() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_COURSE)
            .build();
        
        AutoFixReport.FixResult result = AutoFixReport.FixResult.failure(
            issue, "Auto-fix attempted", "Cannot fix empty course automatically");
        
        assertFalse(result.isSuccess());
        assertEquals(issue, result.getOriginalIssue());
        assertEquals("Auto-fix attempted", result.getAction());
        assertEquals("Cannot fix empty course automatically", result.getErrorMessage());
    }
    
    @Test
    public void testAutoFixResultToString() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .build();
        
        AutoFixReport.FixResult success = AutoFixReport.FixResult.success(issue, "Fixed title");
        assertTrue(success.toString().contains("Fixed"));
        
        AutoFixReport.FixResult failure = AutoFixReport.FixResult.failure(issue, "Fix", "Error occurred");
        assertTrue(failure.toString().contains("Failed"));
    }
    
    // ========== ReadinessIssue Severity Tests ==========
    
    @Test
    public void testReadinessIssueSeverityValues() {
        assertNotNull(ReadinessIssue.Severity.INFO);
        assertNotNull(ReadinessIssue.Severity.WARNING);
        assertNotNull(ReadinessIssue.Severity.ERROR);
        assertNotNull(ReadinessIssue.Severity.CRITICAL);
    }
    
    // ========== ReadinessIssue ToString Tests ==========
    
    @Test
    public void testReadinessIssueToString() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.EMPTY_TITLE)
            .severity(ReadinessIssue.Severity.WARNING)
            .message("Test message")
            .nodeName("Test Node")
            .build();
        
        String str = issue.toString();
        assertTrue(str.contains("WARNING"));
        assertTrue(str.contains("EMPTY_TITLE"));
        assertTrue(str.contains("Test message"));
        assertTrue(str.contains("Test Node"));
    }
    
    @Test
    public void testReadinessIssueToStringWithoutNode() {
        ReadinessIssue issue = ReadinessIssue.builder()
            .type(ReadinessIssueType.NO_OWNERS)
            .severity(ReadinessIssue.Severity.ERROR)
            .message("No owners")
            .build();
        
        String str = issue.toString();
        assertTrue(str.contains("ERROR"));
        assertFalse(str.contains("Node:"));
    }
}
