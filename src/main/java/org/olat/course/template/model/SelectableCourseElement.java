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
package org.olat.course.template.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhancement 1: Selective Template Element Chooser
 * Model class representing a course element that can be selected for copying
 * 
 * Initial date: January 18, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class SelectableCourseElement {
    
    private String nodeId;
    private String nodeType;
    private String shortTitle;
    private String longTitle;
    private String description;
    private boolean selected;
    private boolean hasChildren;
    private boolean required; // If true, element must be included (e.g., root node)
    private int level; // Hierarchy level in the course structure
    private List<String> dependencies; // List of node IDs this element depends on
    private List<String> learningResources; // Associated learning resources
    private SelectableCourseElement parent;
    private List<SelectableCourseElement> children;
    
    public SelectableCourseElement() {
        this.selected = true; // Default to selected
        this.dependencies = new ArrayList<>();
        this.learningResources = new ArrayList<>();
        this.children = new ArrayList<>();
    }
    
    public SelectableCourseElement(String nodeId, String nodeType, String shortTitle) {
        this();
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.shortTitle = shortTitle;
    }
    
    // Getters and Setters
    
    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    
    public String getShortTitle() {
        return shortTitle;
    }
    
    public void setShortTitle(String shortTitle) {
        this.shortTitle = shortTitle;
    }
    
    public String getLongTitle() {
        return longTitle;
    }
    
    public void setLongTitle(String longTitle) {
        this.longTitle = longTitle;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    public boolean isHasChildren() {
        return hasChildren;
    }
    
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
    
    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public List<String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public void addDependency(String nodeId) {
        if (!this.dependencies.contains(nodeId)) {
            this.dependencies.add(nodeId);
        }
    }
    
    public List<String> getLearningResources() {
        return learningResources;
    }
    
    public void setLearningResources(List<String> learningResources) {
        this.learningResources = learningResources;
    }
    
    public void addLearningResource(String resourceId) {
        if (!this.learningResources.contains(resourceId)) {
            this.learningResources.add(resourceId);
        }
    }
    
    public SelectableCourseElement getParent() {
        return parent;
    }
    
    public void setParent(SelectableCourseElement parent) {
        this.parent = parent;
    }
    
    public List<SelectableCourseElement> getChildren() {
        return children;
    }
    
    public void setChildren(List<SelectableCourseElement> children) {
        this.children = children;
    }
    
    public void addChild(SelectableCourseElement child) {
        this.children.add(child);
        child.setParent(this);
    }
    
    /**
     * Check if this element can be deselected based on dependencies
     */
    public boolean canBeDeselected() {
        return !required;
    }
    
    /**
     * Get all descendant nodes
     */
    public List<SelectableCourseElement> getAllDescendants() {
        List<SelectableCourseElement> descendants = new ArrayList<>();
        for (SelectableCourseElement child : children) {
            descendants.add(child);
            descendants.addAll(child.getAllDescendants());
        }
        return descendants;
    }
    
    /**
     * Check if any dependencies are not selected
     */
    public boolean hasMissingDependencies(List<SelectableCourseElement> allElements) {
        for (String depId : dependencies) {
            boolean found = false;
            for (SelectableCourseElement element : allElements) {
                if (element.getNodeId().equals(depId) && element.isSelected()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "SelectableCourseElement{" +
                "nodeId='" + nodeId + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", shortTitle='" + shortTitle + '\'' +
                ", selected=" + selected +
                ", required=" + required +
                ", level=" + level +
                '}';
    }
}
