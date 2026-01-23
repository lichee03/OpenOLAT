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

import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * Enhancement 3: Bulk Staff Assignment
 * 
 * Service interface for bulk assignment of staff (tutors, coaches, owners)
 * to courses and course elements.
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public interface BulkStaffAssignmentService {
    
    /**
     * Assign multiple staff members to a course with specified role
     * @param course The course repository entry
     * @param identities The staff members to assign
     * @param role The role to assign (coach, owner, etc.)
     * @return Result of the bulk operation
     */
    BulkAssignmentResult assignStaffToCourse(RepositoryEntry course, List<Identity> identities, GroupRoles role);
    
    /**
     * Assign staff to multiple courses at once
     * @param courses The courses to assign to
     * @param identities The staff members to assign
     * @param role The role to assign
     * @return Result of the bulk operation
     */
    BulkAssignmentResult assignStaffToMultipleCourses(List<RepositoryEntry> courses, 
            List<Identity> identities, GroupRoles role);
    
    /**
     * Remove staff from a course
     * @param course The course repository entry
     * @param identities The staff members to remove
     * @param role The role to remove
     * @return Result of the bulk operation
     */
    BulkAssignmentResult removeStaffFromCourse(RepositoryEntry course, List<Identity> identities, GroupRoles role);
    
    /**
     * Get all staff members with a specific role in a course
     * @param course The course
     * @param role The role to filter by
     * @return List of identities with the role
     */
    List<Identity> getStaffByRole(RepositoryEntry course, GroupRoles role);
    
    /**
     * Copy staff assignments from one course to another
     * @param sourceCourse The source course
     * @param targetCourse The target course
     * @param roles Which roles to copy
     * @return Result of the copy operation
     */
    BulkAssignmentResult copyStaffAssignments(RepositoryEntry sourceCourse, 
            RepositoryEntry targetCourse, Set<GroupRoles> roles);
    
    /**
     * Validate staff assignment before execution
     * @param course The course
     * @param identities The staff to assign
     * @param role The role
     * @return Validation result with any issues
     */
    StaffAssignmentValidation validateAssignment(RepositoryEntry course, 
            List<Identity> identities, GroupRoles role);
    
    /**
     * Get assignment summary for a course
     * @param course The course
     * @return Summary of current staff assignments
     */
    StaffAssignmentSummary getAssignmentSummary(RepositoryEntry course);
}
