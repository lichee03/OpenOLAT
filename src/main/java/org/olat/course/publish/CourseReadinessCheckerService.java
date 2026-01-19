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

import java.util.List;
import java.util.Set;

import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Service interface for validating course readiness before publication.
 * Provides comprehensive checks for course structure, content, and configuration.
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public interface CourseReadinessCheckerService {
    
    /**
     * Perform a full readiness check on a course
     * @param course The course to check
     * @return Complete readiness report
     */
    CourseReadinessReport checkReadiness(ICourse course);
    
    /**
     * Perform a full readiness check using repository entry
     * @param entry The repository entry
     * @return Complete readiness report
     */
    CourseReadinessReport checkReadiness(RepositoryEntry entry);
    
    /**
     * Check specific aspects of course readiness
     * @param course The course to check
     * @param checkTypes Which types of checks to perform
     * @return Readiness report for specified checks
     */
    CourseReadinessReport checkReadiness(ICourse course, Set<ReadinessCheckType> checkTypes);
    
    /**
     * Quick check if course is ready for publication
     * @param course The course
     * @return true if ready, false if critical issues exist
     */
    boolean isReadyForPublication(ICourse course);
    
    /**
     * Get only critical issues that block publication
     * @param course The course
     * @return List of critical issues
     */
    List<ReadinessIssue> getCriticalIssues(ICourse course);
    
    /**
     * Get suggested fixes for readiness issues
     * @param issue The issue to get fixes for
     * @return List of suggested fixes
     */
    List<String> getSuggestedFixes(ReadinessIssue issue);
    
    /**
     * Auto-fix certain types of issues if possible
     * @param course The course
     * @param issueTypes Types of issues to auto-fix
     * @return Report of fixes applied
     */
    AutoFixReport autoFix(ICourse course, Set<ReadinessIssueType> issueTypes);
}
