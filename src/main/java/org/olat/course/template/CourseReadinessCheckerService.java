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
package org.olat.course.template;

import java.util.List;

import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;

/**
 * Service for checking course readiness before publication
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public interface CourseReadinessCheckerService {
	
	/**
	 * Check if a course is ready for publication
	 * @param course
	 * @return List of readiness check items (empty if course is fully ready)
	 */
	List<CourseReadinessCheckItem> checkCourseReadiness(ICourse course);
	
	/**
	 * Check if a course is ready for publication (by repository entry)
	 * @param courseEntry
	 * @return List of readiness check items
	 */
	List<CourseReadinessCheckItem> checkCourseReadiness(RepositoryEntry courseEntry);
	
	/**
	 * Check if course structure is defined and valid
	 * @param course
	 * @return
	 */
	List<CourseReadinessCheckItem> checkCourseStructure(ICourse course);
	
	/**
	 * Check if required staff (coaches, owners) are assigned
	 * @param course
	 * @return
	 */
	List<CourseReadinessCheckItem> checkStaffAssignment(ICourse course);
	
	/**
	 * Check if course configuration is complete
	 * @param course
	 * @return
	 */
	List<CourseReadinessCheckItem> checkCourseConfiguration(ICourse course);
	
	/**
	 * Check if learning resources are properly linked
	 * @param course
	 * @return
	 */
	List<CourseReadinessCheckItem> checkLearningResources(ICourse course);
	
	/**
	 * Check if course elements are properly configured
	 * @param course
	 * @return
	 */
	List<CourseReadinessCheckItem> checkCourseElements(ICourse course);
	
	/**
	 * Check if there are any critical issues preventing publication
	 * @param course
	 * @return true if course can be published, false otherwise
	 */
	boolean canPublish(ICourse course);
	
	/**
	 * Get all issues by category
	 * @param course
	 * @return Map of category to list of check items
	 */
	java.util.Map<CourseReadinessCheckItem.CheckCategory, List<CourseReadinessCheckItem>> checkByCategory(ICourse course);
}
