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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Enhancement 2: Smart Course Element Duplication with Dependency Mapping
 * 
 * Contains dependency information for a single course node
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class NodeDependencyInfo {
    
    private final String nodeId;
    private final String nodeType;
    private final String nodeTitle;
    private final Set<String> dependsOn; // Nodes this node requires
    private final Set<String> dependedBy; // Nodes that require this node
    private final List<DependencyType> dependencyTypes;
    private boolean hasCyclicDependency;
    
    public NodeDependencyInfo(String nodeId, String nodeType, String nodeTitle) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.nodeTitle = nodeTitle;
        this.dependsOn = new HashSet<>();
        this.dependedBy = new HashSet<>();
        this.dependencyTypes = new ArrayList<>();
        this.hasCyclicDependency = false;
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public String getNodeTitle() {
        return nodeTitle;
    }
    
    public Set<String> getDependsOn() {
        return dependsOn;
    }
    
    public void addDependsOn(String nodeId) {
        this.dependsOn.add(nodeId);
    }
    
    public Set<String> getDependedBy() {
        return dependedBy;
    }
    
    public void addDependedBy(String nodeId) {
        this.dependedBy.add(nodeId);
    }
    
    public List<DependencyType> getDependencyTypes() {
        return dependencyTypes;
    }
    
    public void addDependencyType(DependencyType type) {
        if (!this.dependencyTypes.contains(type)) {
            this.dependencyTypes.add(type);
        }
    }
    
    public boolean hasCyclicDependency() {
        return hasCyclicDependency;
    }
    
    public void setHasCyclicDependency(boolean hasCyclicDependency) {
        this.hasCyclicDependency = hasCyclicDependency;
    }
    
    public boolean hasDependencies() {
        return !dependsOn.isEmpty() || !dependedBy.isEmpty();
    }
    
    public boolean isIndependent() {
        return dependsOn.isEmpty() && dependedBy.isEmpty();
    }
    
    /**
     * Types of dependencies between course elements
     */
    public enum DependencyType {
        VISIBILITY_CONDITION,    // Node visibility depends on another node's completion
        PREREQUISITE,            // Node requires another node to be completed first
        SHARED_RESOURCE,         // Nodes share the same learning resource
        DATA_REFERENCE,          // Node references data from another node
        ASSESSMENT_DEPENDENCY,   // Assessment that includes results from other nodes
        STRUCTURE_PARENT,        // Hierarchical parent-child relationship
        SCORE_CALCULATION        // Score calculated from other nodes
    }
    
    @Override
    public String toString() {
        return "NodeDependencyInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", nodeType='" + nodeType + '\'' +
                ", dependsOn=" + dependsOn.size() +
                ", dependedBy=" + dependedBy.size() +
                '}';
    }
}
