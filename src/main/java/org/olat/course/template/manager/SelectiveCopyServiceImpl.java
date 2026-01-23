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
package org.olat.course.template.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.template.SelectiveCopyService;
import org.olat.course.template.model.SelectableCourseElement;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * Enhancement 1: Selective Template Element Chooser
 * Implementation of selective course element copying service
 * 
 * Initial date: January 18, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
@Service
public class SelectiveCopyServiceImpl implements SelectiveCopyService {
    
    private static final Logger log = Tracing.createLoggerFor(SelectiveCopyServiceImpl.class);
    
    @Override
    public List<SelectableCourseElement> extractSelectableElements(ICourse course) {
        log.debug("Extracting selectable elements from course: {}", course.getResourceableId());
        
        List<SelectableCourseElement> elements = new ArrayList<>();
        CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
        CourseEditorTreeNode rootNode = (CourseEditorTreeNode) editorTreeModel.getRootNode();
        
        if (rootNode != null) {
            // Process root node and all children
            processNode(rootNode, null, 0, elements);
        }
        
        // Analyze and set dependencies
        analyzeDependencies(elements, course);
        
        log.debug("Extracted {} selectable elements", elements.size());
        return elements;
    }
    
    private void processNode(CourseEditorTreeNode node, SelectableCourseElement parent, 
            int level, List<SelectableCourseElement> allElements) {
        
        CourseNode courseNode = node.getCourseNode();
        if (courseNode == null) {
            return;
        }
        
        SelectableCourseElement element = new SelectableCourseElement();
        element.setNodeId(courseNode.getIdent());
        element.setNodeType(courseNode.getType());
        element.setShortTitle(courseNode.getShortTitle());
        element.setLongTitle(courseNode.getLongTitle());
        element.setLevel(level);
        element.setHasChildren(node.getChildCount() > 0);
        
        // Root node is always required
        if (level == 0) {
            element.setRequired(true);
        }
        
        // Analyze learning resources
        analyzeLearningResources(courseNode, element);
        
        if (parent != null) {
            parent.addChild(element);
        }
        
        allElements.add(element);
        
        // Process children
        for (int i = 0; i < node.getChildCount(); i++) {
            INode childNode = node.getChildAt(i);
            if (childNode instanceof CourseEditorTreeNode) {
                processNode((CourseEditorTreeNode) childNode, element, level + 1, allElements);
            }
        }
    }
    
    private void analyzeLearningResources(CourseNode courseNode, SelectableCourseElement element) {
        // Check for specific node types with learning resources
        if (courseNode instanceof SPCourseNode) {
            // Single page - has HTML or document resources
            element.addLearningResource("HTML_CONTENT");
        } else if (courseNode instanceof BCCourseNode) {
            // Folder - contains files
            element.addLearningResource("FOLDER_CONTENT");
        } else if (courseNode instanceof IQTESTCourseNode) {
            // Test - references test resource
            IQTESTCourseNode testNode = (IQTESTCourseNode) courseNode;
            element.addLearningResource("TEST_RESOURCE");
        } else if (courseNode instanceof GTACourseNode) {
            // Group task - has task definitions and solutions
            element.addLearningResource("TASK_DEFINITIONS");
        } else if (courseNode instanceof WikiCourseNode || courseNode instanceof BlogCourseNode) {
            // Wiki/Blog - external resource
            element.addLearningResource("EXTERNAL_RESOURCE");
        }
    }
    
    private void analyzeDependencies(List<SelectableCourseElement> elements, ICourse course) {
        // Analyze structural dependencies
        for (SelectableCourseElement element : elements) {
            // If element has parent, it depends on parent
            if (element.getParent() != null) {
                element.addDependency(element.getParent().getNodeId());
            }
            
            // Structure nodes depend on having at least one child
            if ("st".equals(element.getNodeType()) && element.isHasChildren()) {
                for (SelectableCourseElement child : element.getChildren()) {
                    element.addDependency(child.getNodeId());
                }
            }
        }
    }
    
    @Override
    public ICourse copySelectedElements(ICourse sourceCourse, RepositoryEntry targetEntry,
            Set<String> selectedElements, boolean copyLearningResources, Identity creator) {
        
        log.info("Copying selected course elements from {} to {}", 
                sourceCourse.getResourceableId(), targetEntry.getKey());
        
        // Create base course structure
        ICourse targetCourse = CourseFactory.loadCourse(targetEntry);
        CourseEditorTreeModel sourceTreeModel = sourceCourse.getEditorTreeModel();
        CourseEditorTreeModel targetTreeModel = targetCourse.getEditorTreeModel();
        
        // Copy root node (always required)
        CourseEditorTreeNode sourceRoot = (CourseEditorTreeNode) sourceTreeModel.getRootNode();
        CourseEditorTreeNode targetRoot = (CourseEditorTreeNode) targetTreeModel.getRootNode();
        
        // Copy metadata from source to target root
        copyNodeMetadata(sourceRoot.getCourseNode(), targetRoot.getCourseNode());
        
        // Recursively copy selected nodes
        copyNodesRecursively(sourceRoot, targetRoot, selectedElements, copyLearningResources, sourceCourse, targetCourse);
        
        // Save the course
        CourseFactory.saveCourse(targetEntry.getOlatResource().getResourceableId());
        
        log.info("Successfully copied {} elements", selectedElements.size());
        return targetCourse;
    }
    
    private void copyNodesRecursively(CourseEditorTreeNode sourceNode, CourseEditorTreeNode targetParent,
            Set<String> selectedElements, boolean copyResources, ICourse sourceCourse, ICourse targetCourse) {
        
        for (int i = 0; i < sourceNode.getChildCount(); i++) {
            INode child = sourceNode.getChildAt(i);
            if (!(child instanceof CourseEditorTreeNode)) {
                continue;
            }
            
            CourseEditorTreeNode sourceChild = (CourseEditorTreeNode) child;
            CourseNode sourceCourseNode = sourceChild.getCourseNode();
            
            if (sourceCourseNode == null) {
                continue;
            }
            
            // Check if this node is selected
            if (selectedElements.contains(sourceCourseNode.getIdent())) {
                // Create a copy of the node
                CourseNode copiedNode = (CourseNode) sourceCourseNode.createInstanceForCopy(true, sourceCourse, null);
                
                // Add to target tree
                CourseEditorTreeNode newTreeNode = targetCourse.getEditorTreeModel().insertCourseNodeAt(
                        copiedNode, targetParent.getCourseNode(), targetParent.getChildCount());
                
                // Copy learning resources if requested
                if (copyResources) {
                    copyNodeResources(sourceCourseNode, copiedNode, sourceCourse, targetCourse);
                }
                
                // Recursively process children
                copyNodesRecursively(sourceChild, newTreeNode, selectedElements, copyResources, sourceCourse, targetCourse);
            }
        }
    }
    
    private void copyNodeMetadata(CourseNode source, CourseNode target) {
        if (source != null && target != null) {
            target.setShortTitle(source.getShortTitle());
            target.setLongTitle(source.getLongTitle());
            // Note: getLearningObjectives() may not exist in all versions
            // Commenting out for compatibility
            // target.setLearningObjectives(source.getLearningObjectives());
            target.setDisplayOption(source.getDisplayOption());
        }
    }
    
    private void copyNodeResources(CourseNode source, CourseNode target, ICourse sourceCourse, ICourse targetCourse) {
        // This is a placeholder - actual implementation would handle specific resource types
        log.debug("Copying resources from {} to {}", source.getIdent(), target.getIdent());
        
        // Different node types require different resource copying logic
        // This would be implemented based on the specific node type
    }
    
    @Override
    public ValidationResult validateSelection(List<SelectableCourseElement> allElements, Set<String> selectedNodeIds) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check if root is selected
        boolean rootSelected = false;
        for (SelectableCourseElement element : allElements) {
            if (element.getLevel() == 0 && selectedNodeIds.contains(element.getNodeId())) {
                rootSelected = true;
                break;
            }
        }
        
        if (!rootSelected) {
            errors.add("Root course node must be selected");
        }
        
        // Check for missing dependencies
        for (String selectedId : selectedNodeIds) {
            SelectableCourseElement element = findElement(allElements, selectedId);
            if (element != null) {
                for (String depId : element.getDependencies()) {
                    if (!selectedNodeIds.contains(depId)) {
                        // Check if it's a parent dependency
                        SelectableCourseElement depElement = findElement(allElements, depId);
                        if (depElement != null && depElement.getLevel() < element.getLevel()) {
                            errors.add("Element '" + element.getShortTitle() + 
                                    "' requires parent '" + depElement.getShortTitle() + "' to be selected");
                        } else {
                            warnings.add("Element '" + element.getShortTitle() + 
                                    "' has dependency on '" + (depElement != null ? depElement.getShortTitle() : depId) + 
                                    "' which is not selected");
                        }
                    }
                }
            }
        }
        
        // Check for orphaned children
        for (String selectedId : selectedNodeIds) {
            SelectableCourseElement element = findElement(allElements, selectedId);
            if (element != null && element.getParent() != null) {
                if (!selectedNodeIds.contains(element.getParent().getNodeId())) {
                    errors.add("Element '" + element.getShortTitle() + 
                            "' cannot be selected without its parent");
                }
            }
        }
        
        boolean valid = errors.isEmpty();
        return new ValidationResult(valid, errors, warnings);
    }
    
    @Override
    public Set<String> resolveAndAutoSelectDependencies(List<SelectableCourseElement> allElements, 
            Set<String> currentSelection) {
        
        Set<String> resolvedSelection = new HashSet<>(currentSelection);
        boolean changed = true;
        
        // Iteratively add dependencies until no changes
        while (changed) {
            changed = false;
            Set<String> toAdd = new HashSet<>();
            
            for (String selectedId : resolvedSelection) {
                SelectableCourseElement element = findElement(allElements, selectedId);
                if (element != null) {
                    // Add all parent dependencies
                    for (String depId : element.getDependencies()) {
                        SelectableCourseElement depElement = findElement(allElements, depId);
                        if (depElement != null && depElement.getLevel() < element.getLevel()) {
                            if (!resolvedSelection.contains(depId)) {
                                toAdd.add(depId);
                                changed = true;
                            }
                        }
                    }
                }
            }
            
            resolvedSelection.addAll(toAdd);
        }
        
        return resolvedSelection;
    }
    
    private SelectableCourseElement findElement(List<SelectableCourseElement> elements, String nodeId) {
        for (SelectableCourseElement element : elements) {
            if (element.getNodeId().equals(nodeId)) {
                return element;
            }
        }
        return null;
    }
}
