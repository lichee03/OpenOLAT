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
import java.util.Collection;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of SelectiveTemplateInstantiationService
 * Allows creating courses from templates with selective element/resource choosing
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
@Service
public class SelectiveTemplateInstantiationServiceImpl implements SelectiveTemplateInstantiationService {
	
	private static final Logger log = Tracing.createLogger(SelectiveTemplateInstantiationServiceImpl.class);
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Autowired
	private CourseElementDuplicationService duplicationService;

	@Override
	public List<TemplateElement> getTemplateElements(RepositoryEntry templateEntry) {
		log.info("Getting template elements from: {}", templateEntry.getDisplayname());
		
		List<TemplateElement> elements = new ArrayList<>();
		
		try {
			ICourse course = CourseFactory.loadCourse(templateEntry);
			CourseNode rootNode = course.getCourseEnvironment().getRunStructureManager().getRootNode();
			
			// Recursively extract all elements
			extractElements(rootNode, elements, null, 0);
			
			log.info("Extracted {} elements from template", elements.size());
		} catch (Exception e) {
			log.error("Error extracting template elements: " + e.getMessage(), e);
		}
		
		return elements;
	}

	@Override
	public List<TemplateResource> getTemplateResources(RepositoryEntry templateEntry) {
		log.info("Getting template resources from: {}", templateEntry.getDisplayname());
		
		List<TemplateResource> resources = new ArrayList<>();
		
		try {
			ICourse course = CourseFactory.loadCourse(templateEntry);
			CourseNode rootNode = course.getCourseEnvironment().getRunStructureManager().getRootNode();
			
			// Recursively extract linked resources
			extractResources(rootNode, resources);
			
			log.info("Extracted {} resources from template", resources.size());
		} catch (Exception e) {
			log.error("Error extracting template resources: " + e.getMessage(), e);
		}
		
		return resources;
	}

	@Override
	public RepositoryEntry createCourseWithSelectiveElements(
			RepositoryEntry templateEntry, String courseName, String courseDescription,
			Set<String> selectedNodeIds, Set<String> selectedResourceIds,
			boolean deepCopyResources, Identity creator) {
		
		log.info("Creating course from template with {} selected elements and {} resources",
			selectedNodeIds.size(), selectedResourceIds.size());
		
		try {
			// Validate selection
			SelectionValidationResult validation = validateSelection(templateEntry, selectedNodeIds);
			if (!validation.isValid()) {
				log.error("Selection validation failed: {}", validation.getErrors());
				return null;
			}
			
			// Create new course
			RepositoryEntry newEntry = createNewCourse(courseName, courseDescription, creator);
			if (newEntry == null) {
				return null;
			}
			
			// Copy selected elements
			ICourse templateCourse = CourseFactory.loadCourse(templateEntry);
			ICourse newCourse = CourseFactory.loadCourse(newEntry);
			
			CourseNode templateRoot = templateCourse.getCourseEnvironment()
				.getRunStructureManager().getRootNode();
			CourseNode newRoot = newCourse.getCourseEnvironment()
				.getRunStructureManager().getRootNode();
			
			// Duplicate selected elements
			for (String nodeId : selectedNodeIds) {
				CourseNode sourceNode = templateCourse.getCourseEnvironment()
					.getRunStructureManager().getNodeById(nodeId);
				
				if (sourceNode != null && !sourceNode.equals(templateRoot)) {
					// Find appropriate parent in new course
					CourseNode newParent = findMatchingParent(newCourse, sourceNode, 
						templateCourse, selectedNodeIds);
					
					if (newParent == null) {
						newParent = newRoot; // Use root if no matching parent
					}
					
					DuplicationResult result = duplicationService.duplicateElementTree(
						newCourse, sourceNode, newParent, true, true, creator);
					
					if (!result.isSuccess()) {
						log.warn("Failed to duplicate element: {}", nodeId);
					}
				}
			}
			
			// Copy selected resources if needed
			if (deepCopyResources && !selectedResourceIds.isEmpty()) {
				copyResources(templateEntry, newEntry, selectedResourceIds);
			}
			
			log.info("Successfully created course from template: {}", newEntry.getKey());
			return newEntry;
		} catch (Exception e) {
			log.error("Error creating course from template: " + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public SelectionPreview previewSelection(RepositoryEntry templateEntry,
			Set<String> selectedNodeIds, Set<String> selectedResourceIds) {
		
		log.info("Generating preview for selection");
		
		SelectionPreview preview = new SelectionPreview();
		preview.setTotalElementsSelected(selectedNodeIds.size());
		preview.setTotalResourcesSelected(selectedResourceIds.size());
		
		try {
			ICourse course = CourseFactory.loadCourse(templateEntry);
			
			// Add selected node names
			for (String nodeId : selectedNodeIds) {
				CourseNode node = course.getCourseEnvironment().getRunStructureManager()
					.getNodeById(nodeId);
				if (node != null) {
					preview.addSelectedNodeName(node.getShortTitle());
				}
			}
			
			// Add selected resource names and calculate size
			for (String resourceId : selectedResourceIds) {
				// In real implementation, would look up actual resource names
				preview.addSelectedResourceName("Resource-" + resourceId);
			}
			
			// Estimate total size
			long estimatedSize = estimateResourceSize(templateEntry, selectedResourceIds);
			preview.setEstimatedSize(estimatedSize);
			
			log.debug("Preview generated: {}", preview);
		} catch (Exception e) {
			log.error("Error generating preview: " + e.getMessage(), e);
		}
		
		return preview;
	}

	@Override
	public List<String> getElementDependencies(RepositoryEntry templateEntry, String nodeId) {
		log.debug("Getting dependencies for node: {}", nodeId);
		
		List<String> dependencies = new ArrayList<>();
		
		try {
			ICourse course = CourseFactory.loadCourse(templateEntry);
			CourseNode node = course.getCourseEnvironment().getRunStructureManager()
				.getNodeById(nodeId);
			
			if (node != null) {
				// Add parent
				CourseNode parent = (CourseNode) node.getParent();
				if (parent != null && !parent.equals(
					course.getCourseEnvironment().getRunStructureManager().getRootNode())) {
					dependencies.add(parent.getIdent());
				}
				
				// Scan for references to this node
				CourseNode rootNode = course.getCourseEnvironment().getRunStructureManager().getRootNode();
				findNodeReferences(rootNode, nodeId, dependencies);
			}
		} catch (Exception e) {
			log.error("Error getting dependencies: " + e.getMessage(), e);
		}
		
		return dependencies;
	}

	@Override
	public SelectionValidationResult validateSelection(RepositoryEntry templateEntry,
			Set<String> selectedNodeIds) {
		
		log.debug("Validating selection of {} nodes", selectedNodeIds.size());
		
		SelectionValidationResult result = new SelectionValidationResult();
		
		try {
			ICourse course = CourseFactory.loadCourse(templateEntry);
			
			for (String nodeId : selectedNodeIds) {
				CourseNode node = course.getCourseEnvironment().getRunStructureManager()
					.getNodeById(nodeId);
				
				if (node == null) {
					result.addError("Node not found: " + nodeId);
					continue;
				}
				
				// Check for dependencies
				List<String> deps = getElementDependencies(templateEntry, nodeId);
				for (String depId : deps) {
					if (!selectedNodeIds.contains(depId) && 
						!isRootNode(course, depId)) {
						result.addWarning("Element " + nodeId + " depends on " + depId 
							+ " which is not selected");
						result.addSuggestedAutoInclusion(depId);
					}
				}
			}
			
		} catch (Exception e) {
			log.error("Error validating selection: " + e.getMessage(), e);
			result.addError("Validation error: " + e.getMessage());
		}
		
		return result;
	}

	@Override
	public long estimateResourceSize(RepositoryEntry templateEntry,
			Set<String> selectedResourceIds) {
		
		log.debug("Estimating size for {} resources", selectedResourceIds.size());
		
		long totalSize = 0;
		
		try {
			// In a real implementation, would calculate actual resource sizes
			// For now, return placeholder
			totalSize = selectedResourceIds.size() * 1024 * 1024; // 1 MB per resource estimate
		} catch (Exception e) {
			log.error("Error estimating resource size: " + e.getMessage(), e);
		}
		
		return totalSize;
	}
	
	/**
	 * Recursively extract all course elements from tree
	 */
	private void extractElements(CourseNode node, List<TemplateElement> elements,
			String parentId, int level) {
		
		if (node == null) {
			return;
		}
		
		TemplateElement element = new TemplateElement(
			node.getIdent(),
			node.getShortTitle(),
			node.getType()
		);
		element.setLevel(level);
		element.setParentNodeId(parentId);
		element.setDescription(node.getDescription());
		element.setHasChildren(node.getChildCount() > 0);
		
		elements.add(element);
		
		// Process children
		for (int i = 0; i < node.getChildCount(); i++) {
			CourseNode child = (CourseNode) node.getChildAt(i);
			if (child != null) {
				extractElements(child, elements, node.getIdent(), level + 1);
			}
		}
	}
	
	/**
	 * Recursively extract linked resources
	 */
	private void extractResources(CourseNode node, List<TemplateResource> resources) {
		if (node == null) {
			return;
		}
		
		// Check if node has linked resources
		// This is simplified - in real implementation would inspect ModuleConfiguration
		if (hasLinkedResource(node)) {
			TemplateResource resource = new TemplateResource(
				node.getIdent() + "-res",
				"Resource for " + node.getShortTitle(),
				"linked"
			);
			resource.setLinkedNodeId(node.getIdent());
			resources.add(resource);
		}
		
		// Process children
		for (int i = 0; i < node.getChildCount(); i++) {
			CourseNode child = (CourseNode) node.getChildAt(i);
			if (child != null) {
				extractResources(child, resources);
			}
		}
	}
	
	/**
	 * Check if node has linked resources
	 */
	private boolean hasLinkedResource(CourseNode node) {
		// Simplified implementation
		String type = node.getType();
		return type.equals("scorm") || type.equals("wiki") || type.equals("blog") ||
			type.equals("forum") || type.equals("document");
	}
	
	/**
	 * Find matching parent in new course structure
	 */
	private CourseNode findMatchingParent(ICourse newCourse, CourseNode sourceNode,
			ICourse templateCourse, Set<String> selectedNodeIds) {
		
		CourseNode parent = (CourseNode) sourceNode.getParent();
		if (parent == null) {
			return null;
		}
		
		// If parent is selected, find its duplicate in new course
		if (selectedNodeIds.contains(parent.getIdent())) {
			// In real implementation, would look up the duplicated parent
			// For now, return root
		}
		
		return null;
	}
	
	/**
	 * Create new course
	 */
	private RepositoryEntry createNewCourse(String name, String description, Identity creator) {
		try {
			// This would use CourseFactory or similar to create a new course
			// Simplified placeholder
			log.info("Creating new course: {}", name);
			return null;
		} catch (Exception e) {
			log.error("Error creating new course: " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * Copy resources from template to new course
	 */
	private void copyResources(RepositoryEntry templateEntry, RepositoryEntry newEntry,
			Set<String> selectedResourceIds) {
		
		log.info("Copying {} resources", selectedResourceIds.size());
		
		try {
			// Implementation would copy actual resources
			// This is a placeholder
		} catch (Exception e) {
			log.error("Error copying resources: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Find node references
	 */
	private void findNodeReferences(CourseNode node, String targetNodeId,
			List<String> references) {
		
		if (node == null) {
			return;
		}
		
		// Check if this node references the target
		// In real implementation would inspect ModuleConfiguration
		
		// Check children
		for (int i = 0; i < node.getChildCount(); i++) {
			CourseNode child = (CourseNode) node.getChildAt(i);
			if (child != null) {
				findNodeReferences(child, targetNodeId, references);
			}
		}
	}
	
	/**
	 * Check if node is root
	 */
	private boolean isRootNode(ICourse course, String nodeId) {
		CourseNode rootNode = course.getCourseEnvironment().getRunStructureManager().getRootNode();
		return rootNode != null && rootNode.getIdent().equals(nodeId);
	}
}
