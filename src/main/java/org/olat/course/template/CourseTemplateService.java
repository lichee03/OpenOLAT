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

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * Service for managing course templates
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public interface CourseTemplateService {
	
	/**
	 * Create a new course template from an existing course
	 * @param sourceEntry
	 * @param name
	 * @param description
	 * @param identity
	 * @return
	 */
	CourseTemplate createTemplate(RepositoryEntry sourceEntry, String name, String description, Identity identity);
	
	/**
	 * Update template metadata
	 * @param template
	 * @param name
	 * @param description
	 * @return
	 */
	CourseTemplate updateTemplate(CourseTemplate template, String name, String description);
	
	/**
	 * Update template configuration options
	 * @param template
	 * @param includeStructure
	 * @param includeCourseElements
	 * @param includeLearningResources
	 * @param includeCourseSettings
	 * @param includeStaffAssignments
	 * @return
	 */
	CourseTemplate updateTemplateConfig(CourseTemplate template, 
		boolean includeStructure, boolean includeCourseElements, boolean includeLearningResources,
		boolean includeCourseSettings, boolean includeStaffAssignments);
	
	/**
	 * Get template by key
	 * @param templateKey
	 * @return
	 */
	CourseTemplate getTemplate(Long templateKey);
	
	/**
	 * Get all active templates
	 * @return
	 */
	List<CourseTemplate> getActiveTemplates();
	
	/**
	 * Get templates for a specific source course
	 * @param sourceEntryRef
	 * @return
	 */
	List<CourseTemplate> getTemplatesFor(RepositoryEntryRef sourceEntryRef);
	
	/**
	 * Get templates created by a specific user
	 * @param identity
	 * @return
	 */
	List<CourseTemplate> getTemplatesByCreator(Identity identity);
	
	/**
	 * Activate or deactivate a template
	 * @param template
	 * @param active
	 * @return
	 */
	CourseTemplate setTemplateActive(CourseTemplate template, boolean active);
	
	/**
	 * Delete a template
	 * @param template
	 */
	void deleteTemplate(CourseTemplate template);
	
	/**
	 * Create a new course from a template
	 * @param template
	 * @param newCourseName
	 * @param newCourseDescription
	 * @param creator
	 * @return The new RepositoryEntry
	 */
	RepositoryEntry createCourseFromTemplate(CourseTemplate template, String newCourseName, 
		String newCourseDescription, Identity creator);
}
