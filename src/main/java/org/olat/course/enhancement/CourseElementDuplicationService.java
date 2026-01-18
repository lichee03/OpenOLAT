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

import java.util.List;
import java.util.Map;

import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.core.id.Identity;

/**
 * Service for smart duplication of course elements with dependency mapping
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public interface CourseElementDuplicationService {
	
	/**
	 * Duplicate a single course element with all configurations
	 * @param course
	 * @param sourceNode
	 * @param targetParent
	 * @param preserveAssessment
	 * @param executor
	 * @return
	 */
	CourseNode duplicateElement(ICourse course, CourseNode sourceNode, 
		CourseNode targetParent, boolean preserveAssessment, Identity executor);
	
	/**
	 * Duplicate course element tree with all children and dependencies
	 * @param course
	 * @param sourceNode
	 * @param targetParent
	 * @param preserveAssessment - whether to keep assessment configurations
	 * @param preserveReferences - whether to fix internal references
	 * @param executor
	 * @return
	 */
	DuplicationResult duplicateElementTree(ICourse course, CourseNode sourceNode,
		CourseNode targetParent, boolean preserveAssessment, 
		boolean preserveReferences, Identity executor);
	
	/**
	 * Duplicate multiple selected elements
	 * @param course
	 * @param selectedNodeIds - which nodes to duplicate
	 * @param targetParent
	 * @param preserveAssessment
	 * @param executor
	 * @return
	 */
	DuplicationResult duplicateSelectedElements(ICourse course, 
		List<String> selectedNodeIds, CourseNode targetParent,
		boolean preserveAssessment, Identity executor);
	
	/**
	 * Get mapping of old node IDs to new node IDs after duplication
	 * @param course
	 * @return Map of sourceNodeId -> duplicatedNodeId
	 */
	Map<String, String> getNodeIdMapping(ICourse course);
	
	/**
	 * Check if element is eligible for duplication
	 * @param nodeType
	 * @return
	 */
	boolean canDuplicate(String nodeType);
	
	/**
	 * Get list of node types that cannot be duplicated
	 * @return
	 */
	List<String> getNonDuplicableNodeTypes();
}
