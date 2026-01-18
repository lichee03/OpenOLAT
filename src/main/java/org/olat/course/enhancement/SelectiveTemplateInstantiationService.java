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
package org.olat.course.enhancement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * Service for selective template instantiation with element and resource chooser
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public interface SelectiveTemplateInstantiationService {
	
	/**
	 * Get all elements from a template course for selection
	 * @param templateEntry
	 * @return List of TemplateElement with hierarchy information
	 */
	List<TemplateElement> getTemplateElements(RepositoryEntry templateEntry);
	
	/**
	 * Get all learning resources linked in template
	 * @param templateEntry
	 * @return List of TemplateResource
	 */
	List<TemplateResource> getTemplateResources(RepositoryEntry templateEntry);
	
	/**
	 * Create course from template with selective element and resource inclusion
	 * @param templateEntry
	 * @param courseName
	 * @param courseDescription
	 * @param selectedNodeIds - which course elements to include
	 * @param selectedResourceIds - which learning resources to copy
	 * @param deepCopyResources - true to deep copy, false to link
	 * @param creator
	 * @return New RepositoryEntry
	 */
	RepositoryEntry createCourseWithSelectiveElements(
		RepositoryEntry templateEntry,
		String courseName,
		String courseDescription,
		Set<String> selectedNodeIds,
		Set<String> selectedResourceIds,
		boolean deepCopyResources,
		Identity creator);
	
	/**
	 * Preview what will be copied based on selection
	 * @param templateEntry
	 * @param selectedNodeIds
	 * @param selectedResourceIds
	 * @return SelectionPreview with summary
	 */
	SelectionPreview previewSelection(RepositoryEntry templateEntry,
		Set<String> selectedNodeIds, Set<String> selectedResourceIds);
	
	/**
	 * Get element dependencies (parents, children, references)
	 * @param templateEntry
	 * @param nodeId
	 * @return List of dependent node IDs
	 */
	List<String> getElementDependencies(RepositoryEntry templateEntry, String nodeId);
	
	/**
	 * Validate if selected elements form a valid hierarchy
	 * @param templateEntry
	 * @param selectedNodeIds
	 * @return ValidationResult with errors/warnings
	 */
	SelectionValidationResult validateSelection(RepositoryEntry templateEntry,
		Set<String> selectedNodeIds);
	
	/**
	 * Get size estimate for selected resources
	 * @param templateEntry
	 * @param selectedResourceIds
	 * @return Size in bytes
	 */
	long estimateResourceSize(RepositoryEntry templateEntry,
		Set<String> selectedResourceIds);
}
