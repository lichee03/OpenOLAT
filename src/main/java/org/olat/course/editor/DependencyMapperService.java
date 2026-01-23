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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;

/**
 * Enhancement 2: Smart Course Element Duplication with Dependency Mapping
 * 
 * Service interface for analyzing and managing dependencies between course elements.
 * This enables intelligent duplication that preserves relationships between elements.
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public interface DependencyMapperService {
    
    /**
     * Analyze all dependencies in a course
     * @param course The course to analyze
     * @return Map of node IDs to their dependency information
     */
    Map<String, NodeDependencyInfo> analyzeDependencies(ICourse course);
    
    /**
     * Get all nodes that depend on the given node
     * @param course The course
     * @param nodeId The node ID to check
     * @return Set of node IDs that depend on this node
     */
    Set<String> getDependentNodes(ICourse course, String nodeId);
    
    /**
     * Get all nodes that the given node depends on
     * @param course The course
     * @param nodeId The node ID to check
     * @return Set of node IDs that this node depends on
     */
    Set<String> getRequiredNodes(ICourse course, String nodeId);
    
    /**
     * Check if a node can be safely deleted without breaking dependencies
     * @param course The course
     * @param nodeId The node to check
     * @return DependencyValidationResult with validation status and details
     */
    DependencyValidationResult canDeleteNode(ICourse course, String nodeId);
    
    /**
     * Get the duplication order for a set of nodes, ensuring dependencies are copied first
     * @param course The course
     * @param nodeIds The nodes to duplicate
     * @return Ordered list of node IDs for safe duplication
     */
    List<String> getDuplicationOrder(ICourse course, Set<String> nodeIds);
    
    /**
     * Duplicate a node with its dependencies
     * @param course The course
     * @param nodeId The node to duplicate
     * @param includeDependencies Whether to include required dependencies
     * @return The duplicated node
     */
    CourseNode duplicateWithDependencies(ICourse course, String nodeId, boolean includeDependencies);
    
    /**
     * Update references after duplication
     * @param course The course
     * @param oldToNewNodeMapping Map of original node IDs to new node IDs
     */
    void updateReferencesAfterDuplication(ICourse course, Map<String, String> oldToNewNodeMapping);
}
