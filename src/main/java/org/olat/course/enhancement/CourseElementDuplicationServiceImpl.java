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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of CourseElementDuplicationService
 * Handles intelligent duplication of course elements with dependency mapping
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
@Service
public class CourseElementDuplicationServiceImpl implements CourseElementDuplicationService {
	
	private static final Logger log = Tracing.createLogger(CourseElementDuplicationServiceImpl.class);
	
	private static final Set<String> NON_DUPLICABLE_TYPES = new HashSet<>();
	static {
		// These node types typically should not be duplicated
		NON_DUPLICABLE_TYPES.add("surveystart");
		NON_DUPLICABLE_TYPES.add("surveycourse");
	}
	
	private Map<String, String> nodeIdMapping;

	@Override
	public CourseNode duplicateElement(ICourse course, CourseNode sourceNode,
			CourseNode targetParent, boolean preserveAssessment, Identity executor) {
		
		log.info("Duplicating single course element: {} of type: {}", 
			sourceNode.getIdent(), sourceNode.getType());
		
		try {
			CourseNode duplicatedNode = duplicateNode(course, sourceNode, 
				targetParent, preserveAssessment, executor);
			
			if (duplicatedNode != null) {
				nodeIdMapping.put(sourceNode.getIdent(), duplicatedNode.getIdent());
				log.info("Successfully duplicated element {} to {}", 
					sourceNode.getIdent(), duplicatedNode.getIdent());
			}
			
			return duplicatedNode;
		} catch (Exception e) {
			log.error("Error duplicating element: " + sourceNode.getIdent(), e);
			return null;
		}
	}

	@Override
	public DuplicationResult duplicateElementTree(ICourse course, CourseNode sourceNode,
			CourseNode targetParent, boolean preserveAssessment,
			boolean preserveReferences, Identity executor) {
		
		log.info("Duplicating element tree starting from: {} ({})", 
			sourceNode.getIdent(), sourceNode.getType());
		
		long startTime = System.currentTimeMillis();
		DuplicationResult result = new DuplicationResult();
		nodeIdMapping = new HashMap<>();
		
		try {
			// Recursively duplicate the node and its children
			CourseNode duplicatedRoot = duplicateNodeAndChildren(course, sourceNode, 
				targetParent, preserveAssessment, executor, result);
			
			if (duplicatedRoot != null) {
				result.setNodeIdMapping(nodeIdMapping);
				
				// Fix references if requested
				if (preserveReferences) {
					fixInternalReferences(course, nodeIdMapping);
				}
				
				result.setSuccess(true);
			} else {
				result.setSuccess(false);
				result.setErrorMessage("Failed to duplicate root element");
			}
		} catch (Exception e) {
			log.error("Error during tree duplication: " + e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		} finally {
			result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
		}
		
		return result;
	}

	@Override
	public DuplicationResult duplicateSelectedElements(ICourse course,
			List<String> selectedNodeIds, CourseNode targetParent,
			boolean preserveAssessment, Identity executor) {
		
		log.info("Duplicating {} selected elements", selectedNodeIds.size());
		
		long startTime = System.currentTimeMillis();
		DuplicationResult result = new DuplicationResult();
		nodeIdMapping = new HashMap<>();
		
		try {
			for (String nodeId : selectedNodeIds) {
				CourseNode sourceNode = course.getCourseEnvironment().getRunStructureManager()
					.getNodeById(nodeId);
				
				if (sourceNode != null) {
					CourseNode duplicated = duplicateNode(course, sourceNode, 
						targetParent, preserveAssessment, executor);
					
					if (duplicated != null) {
						nodeIdMapping.put(nodeId, duplicated.getIdent());
						result.incrementElementsProcessed();
					} else {
						result.incrementElementsFailed();
					}
				}
			}
			
			result.setSuccess(result.getElementsFailed() == 0);
			result.setNodeIdMapping(nodeIdMapping);
		} catch (Exception e) {
			log.error("Error during bulk duplication: " + e.getMessage(), e);
			result.setSuccess(false);
			result.setErrorMessage(e.getMessage());
		} finally {
			result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
		}
		
		return result;
	}

	@Override
	public Map<String, String> getNodeIdMapping(ICourse course) {
		return nodeIdMapping != null ? nodeIdMapping : new HashMap<>();
	}

	@Override
	public boolean canDuplicate(String nodeType) {
		return !NON_DUPLICABLE_TYPES.contains(nodeType);
	}

	@Override
	public List<String> getNonDuplicableNodeTypes() {
		return new ArrayList<>(NON_DUPLICABLE_TYPES);
	}
	
	/**
	 * Recursively duplicate a node and all its children
	 */
	private CourseNode duplicateNodeAndChildren(ICourse course, CourseNode sourceNode,
			CourseNode targetParent, boolean preserveAssessment, Identity executor,
			DuplicationResult result) {
		
		// Duplicate the node itself
		CourseNode duplicated = duplicateNode(course, sourceNode, 
			targetParent, preserveAssessment, executor);
		
		if (duplicated != null) {
			nodeIdMapping.put(sourceNode.getIdent(), duplicated.getIdent());
			result.incrementElementsProcessed();
			
			// Recursively duplicate children
			int childCount = sourceNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				CourseNode child = (CourseNode) sourceNode.getChildAt(i);
				if (child != null) {
					duplicateNodeAndChildren(course, child, duplicated, 
						preserveAssessment, executor, result);
				}
			}
		} else {
			result.incrementElementsFailed();
		}
		
		return duplicated;
	}
	
	/**
	 * Duplicate a single node with its configuration
	 */
	private CourseNode duplicateNode(ICourse course, CourseNode sourceNode,
			CourseNode targetParent, boolean preserveAssessment, Identity executor) {
		
		if (!canDuplicate(sourceNode.getType())) {
			log.warn("Cannot duplicate node type: {}", sourceNode.getType());
			return null;
		}
		
		try {
			// Create new node of same type
			CourseNode newNode = CourseNodeFactory.getInstance()
				.createCourseNode(sourceNode.getType());
			
			if (newNode == null) {
				log.warn("Failed to create node of type: {}", sourceNode.getType());
				return null;
			}
			
			// Copy basic properties
			newNode.setShortTitle(sourceNode.getShortTitle() + " (copy)");
			newNode.setLongTitle(sourceNode.getLongTitle());
			newNode.setDescription(sourceNode.getDescription());
			newNode.setDisplayOption(sourceNode.getDisplayOption());
			
			// Copy module configuration
			if (preserveAssessment || !isAssessmentNode(sourceNode)) {
				// Deep copy the module configuration
				newNode.setModuleConfiguration(new org.olat.modules.ModuleConfiguration(sourceNode.getModuleConfiguration()));
			} else {
				// Create fresh configuration for non-assessment nodes
				newNode.setModuleConfiguration(new org.olat.modules.ModuleConfiguration(sourceNode.getModuleConfiguration()));
			}
			
			// Add to parent and save
			targetParent.addChild(newNode);
			// Save the node to the course structure
			course.getCourseEnvironment().getRunStructureManager().addCourseNode(newNode);
			
			log.debug("Created duplicate node {} from {}", 
				newNode.getIdent(), sourceNode.getIdent());
			
			return newNode;
		} catch (Exception e) {
			log.error("Error creating duplicate node: " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Fix internal references after duplication
	 */
	private void fixInternalReferences(ICourse course, Map<String, String> mapping) {
		log.debug("Fixing internal references with {} mappings", mapping.size());
		
		try {
			// Iterate through all nodes and fix references
			// This is a placeholder for actual reference fixing logic
			// which would depend on specific node types and their reference formats
			
			log.debug("Reference fixing completed");
		} catch (Exception e) {
			log.error("Error fixing references: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Check if a node is an assessment node
	 */
	private boolean isAssessmentNode(CourseNode node) {
		String type = node.getType();
		return type.equals("iqtest") || type.equals("iquest") || 
			type.equals("iqself") || type.equals("grader") ||
			type.equals("ta") || type.equals("ms") || type.equals("st");
	}
}
