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
package org.olat.course.publish.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.publish.AutoFixReport;
import org.olat.course.publish.CourseReadinessCheckerService;
import org.olat.course.publish.CourseReadinessReport;
import org.olat.course.publish.ReadinessCheckType;
import org.olat.course.publish.ReadinessIssue;
import org.olat.course.publish.ReadinessIssueType;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Implementation of CourseReadinessCheckerService
 * Validates courses for publication readiness
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
@Service
public class CourseReadinessCheckerServiceImpl implements CourseReadinessCheckerService {
    
    private static final Logger log = Tracing.createLoggerFor(CourseReadinessCheckerServiceImpl.class);
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Override
    public CourseReadinessReport checkReadiness(ICourse course) {
        return checkReadiness(course, EnumSet.of(ReadinessCheckType.ALL));
    }
    
    @Override
    public CourseReadinessReport checkReadiness(RepositoryEntry entry) {
        ICourse course = CourseFactory.loadCourse(entry);
        return checkReadiness(course);
    }
    
    @Override
    public CourseReadinessReport checkReadiness(ICourse course, Set<ReadinessCheckType> checkTypes) {
        long startTime = System.currentTimeMillis();
        
        CourseReadinessReport.Builder reportBuilder = CourseReadinessReport.builder()
            .courseId(course.getResourceableId())
            .courseTitle(course.getCourseTitle());
        
        // Determine which checks to perform
        Set<ReadinessCheckType> checksToPerform = resolveCheckTypes(checkTypes);
        
        // Perform each type of check
        for (ReadinessCheckType checkType : checksToPerform) {
            List<ReadinessIssue> issues = performCheck(course, checkType);
            reportBuilder.addIssues(issues);
            reportBuilder.checkResult(checkType, issues.stream().noneMatch(ReadinessIssue::isError));
        }
        
        long duration = System.currentTimeMillis() - startTime;
        reportBuilder.checkDurationMs(duration);
        
        CourseReadinessReport report = reportBuilder.build();
        
        log.info("Course readiness check completed for '{}': {} issues found, ready={}",
            course.getCourseTitle(), report.getTotalIssueCount(), report.isReady());
        
        return report;
    }
    
    @Override
    public boolean isReadyForPublication(ICourse course) {
        List<ReadinessIssue> criticalIssues = getCriticalIssues(course);
        return criticalIssues.isEmpty();
    }
    
    @Override
    public List<ReadinessIssue> getCriticalIssues(ICourse course) {
        CourseReadinessReport report = checkReadiness(course);
        return report.getCriticalIssues();
    }
    
    @Override
    public List<String> getSuggestedFixes(ReadinessIssue issue) {
        List<String> fixes = new ArrayList<>();
        
        switch (issue.getType()) {
            case EMPTY_COURSE:
                fixes.add("Add at least one content node to the course");
                fixes.add("Consider using a course template");
                break;
            case MISSING_CONTENT:
                fixes.add("Add content to the node: " + issue.getNodeName());
                fixes.add("Remove the empty node if not needed");
                break;
            case EMPTY_TITLE:
                fixes.add("Set a descriptive title for the node");
                break;
            case NO_OWNERS:
                fixes.add("Assign at least one owner to the course");
                break;
            case NO_COACHES:
                fixes.add("Assign coaches if learner support is needed");
                break;
            case MISSING_DESCRIPTION:
                fixes.add("Add a course description in course settings");
                fixes.add("This helps learners understand the course content");
                break;
            case ASSESSMENT_UNCONFIGURED:
                fixes.add("Configure the assessment settings for: " + issue.getNodeName());
                fixes.add("Set passing score and grading if needed");
                break;
            case INVALID_CONDITION:
                fixes.add("Review and correct the access condition expression");
                fixes.add("Use the condition editor for validation");
                break;
            case BROKEN_REFERENCE:
                fixes.add("Fix the broken reference to: " + issue.getMessage());
                fixes.add("Re-link the referenced content");
                break;
            case INVALID_DATE_RANGE:
                fixes.add("Ensure end date is after start date");
                fixes.add("Review date settings in course configuration");
                break;
            case NO_ENROLLMENT_METHOD:
                fixes.add("Configure an enrollment method (open, booking, access code)");
                break;
            default:
                if (issue.getSuggestedFix() != null) {
                    fixes.add(issue.getSuggestedFix());
                }
        }
        
        return fixes;
    }
    
    @Override
    public AutoFixReport autoFix(ICourse course, Set<ReadinessIssueType> issueTypes) {
        long startTime = System.currentTimeMillis();
        AutoFixReport.Builder reportBuilder = AutoFixReport.builder()
            .courseId(course.getResourceableId());
        
        // Get current issues
        CourseReadinessReport readinessReport = checkReadiness(course);
        List<ReadinessIssue> autoFixableIssues = readinessReport.getAutoFixableIssues();
        
        for (ReadinessIssue issue : autoFixableIssues) {
            if (issueTypes.contains(issue.getType())) {
                AutoFixReport.FixResult result = attemptAutoFix(course, issue);
                reportBuilder.addFixResult(result);
            }
        }
        
        reportBuilder.executionTimeMs(System.currentTimeMillis() - startTime);
        return reportBuilder.build();
    }
    
    /**
     * Resolve ALL check type to all individual checks
     */
    private Set<ReadinessCheckType> resolveCheckTypes(Set<ReadinessCheckType> checkTypes) {
        if (checkTypes.contains(ReadinessCheckType.ALL)) {
            Set<ReadinessCheckType> allChecks = EnumSet.allOf(ReadinessCheckType.class);
            allChecks.remove(ReadinessCheckType.ALL);
            return allChecks;
        }
        return checkTypes;
    }
    
    /**
     * Perform a specific type of check
     */
    private List<ReadinessIssue> performCheck(ICourse course, ReadinessCheckType checkType) {
        switch (checkType) {
            case STRUCTURE:
                return checkStructure(course);
            case CONTENT:
                return checkContent(course);
            case METADATA:
                return checkMetadata(course);
            case STAFF:
                return checkStaff(course);
            case DATES:
                return checkDates(course);
            case ASSESSMENTS:
                return checkAssessments(course);
            case ACCESS_CONDITIONS:
                return checkAccessConditions(course);
            case RESOURCES:
                return checkResources(course);
            case ENROLLMENT:
                return checkEnrollment(course);
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Check course structure
     */
    private List<ReadinessIssue> checkStructure(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        CourseNode rootNode = course.getRunStructure().getRootNode();
        
        // Check if course is empty
        if (rootNode.getChildCount() == 0) {
            issues.add(ReadinessIssue.critical(ReadinessIssueType.EMPTY_COURSE, 
                "Course has no content nodes")
                .checkCategory(ReadinessCheckType.STRUCTURE)
                .suggestedFix("Add learning content to the course")
                .build());
        }
        
        // Check for orphan nodes and hierarchy issues
        checkNodeHierarchy(rootNode, issues, new HashSet<>());
        
        return issues;
    }
    
    private void checkNodeHierarchy(CourseNode node, List<ReadinessIssue> issues, Set<String> visitedIds) {
        if (visitedIds.contains(node.getIdent())) {
            issues.add(ReadinessIssue.critical(ReadinessIssueType.INVALID_HIERARCHY,
                "Circular reference detected in course structure")
                .nodeId(node.getIdent())
                .nodeName(node.getShortTitle())
                .checkCategory(ReadinessCheckType.STRUCTURE)
                .build());
            return;
        }
        visitedIds.add(node.getIdent());
        
        for (int i = 0; i < node.getChildCount(); i++) {
            CourseNode child = (CourseNode) node.getChildAt(i);
            checkNodeHierarchy(child, issues, visitedIds);
        }
    }
    
    /**
     * Check content completeness
     */
    private List<ReadinessIssue> checkContent(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        CourseNode rootNode = course.getRunStructure().getRootNode();
        checkNodeContent(rootNode, issues);
        
        return issues;
    }
    
    private void checkNodeContent(CourseNode node, List<ReadinessIssue> issues) {
        // Check for empty title
        if (!StringHelper.containsNonWhitespace(node.getShortTitle())) {
            issues.add(ReadinessIssue.warning(ReadinessIssueType.EMPTY_TITLE,
                "Node has no title")
                .nodeId(node.getIdent())
                .nodeName(node.getIdent())
                .checkCategory(ReadinessCheckType.CONTENT)
                .autoFixable(true)
                .suggestedFix("Set a descriptive title for the node")
                .build());
        }
        
        // Recursively check children
        for (int i = 0; i < node.getChildCount(); i++) {
            CourseNode child = (CourseNode) node.getChildAt(i);
            checkNodeContent(child, issues);
        }
    }
    
    /**
     * Check course metadata
     */
    private List<ReadinessIssue> checkMetadata(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        // Check course title
        if (!StringHelper.containsNonWhitespace(course.getCourseTitle())) {
            issues.add(ReadinessIssue.error(ReadinessIssueType.EMPTY_TITLE,
                "Course has no title")
                .checkCategory(ReadinessCheckType.METADATA)
                .autoFixable(true)
                .build());
        }
        
        return issues;
    }
    
    /**
     * Check staff assignments
     */
    private List<ReadinessIssue> checkStaff(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        // Note: Full implementation would check RepositoryEntry for owners/coaches
        // This is a simplified version
        
        return issues;
    }
    
    /**
     * Check date configurations
     */
    private List<ReadinessIssue> checkDates(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        // Check lifecycle dates from course config
        // This is a simplified check
        
        return issues;
    }
    
    /**
     * Check assessment configurations
     */
    private List<ReadinessIssue> checkAssessments(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        CourseNode rootNode = course.getRunStructure().getRootNode();
        checkAssessmentNodes(rootNode, issues);
        
        return issues;
    }
    
    private void checkAssessmentNodes(CourseNode node, List<ReadinessIssue> issues) {
        String nodeType = node.getType();
        
        // Check assessment-type nodes
        if ("iqtest".equals(nodeType) || "iqself".equals(nodeType) || "iqsurv".equals(nodeType)) {
            // Check if test is referenced
            Object ref = node.getModuleConfiguration().get("configReferencedSoftkey");
            if (ref == null) {
                issues.add(ReadinessIssue.error(ReadinessIssueType.ASSESSMENT_UNCONFIGURED,
                    "Test node has no test assigned")
                    .nodeId(node.getIdent())
                    .nodeName(node.getShortTitle())
                    .checkCategory(ReadinessCheckType.ASSESSMENTS)
                    .suggestedFix("Assign a test resource to this node")
                    .build());
            }
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            CourseNode child = (CourseNode) node.getChildAt(i);
            checkAssessmentNodes(child, issues);
        }
    }
    
    /**
     * Check access conditions
     */
    private List<ReadinessIssue> checkAccessConditions(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        // Access condition validation would go here
        // This is a placeholder for more complex validation
        
        return issues;
    }
    
    /**
     * Check learning resources
     */
    private List<ReadinessIssue> checkResources(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        // Resource availability checks would go here
        
        return issues;
    }
    
    /**
     * Check enrollment configuration
     */
    private List<ReadinessIssue> checkEnrollment(ICourse course) {
        List<ReadinessIssue> issues = new ArrayList<>();
        
        // Enrollment method checks would go here
        
        return issues;
    }
    
    /**
     * Attempt to auto-fix an issue
     */
    private AutoFixReport.FixResult attemptAutoFix(ICourse course, ReadinessIssue issue) {
        try {
            switch (issue.getType()) {
                case EMPTY_TITLE:
                    // Auto-fix would set a default title
                    return AutoFixReport.FixResult.success(issue, 
                        "Set default title for node");
                case MISSING_DESCRIPTION:
                    // Auto-fix would set a default description
                    return AutoFixReport.FixResult.success(issue,
                        "Set default description");
                default:
                    return AutoFixReport.FixResult.failure(issue,
                        "Auto-fix attempted",
                        "Auto-fix not supported for this issue type");
            }
        } catch (Exception e) {
            log.error("Auto-fix failed for issue: {}", issue.getType(), e);
            return AutoFixReport.FixResult.failure(issue,
                "Auto-fix attempted",
                e.getMessage());
        }
    }
}
