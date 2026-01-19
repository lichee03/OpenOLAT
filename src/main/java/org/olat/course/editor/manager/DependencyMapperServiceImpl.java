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
package org.olat.course.editor.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.editor.DependencyMapperService;
import org.olat.course.editor.DependencyValidationResult;
import org.olat.course.editor.NodeDependencyInfo;
import org.olat.course.editor.NodeDependencyInfo.DependencyType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.springframework.stereotype.Service;

/**
 * Enhancement 2: Smart Course Element Duplication with Dependency Mapping
 * 
 * Implementation of DependencyMapperService that analyzes course structure
 * and manages dependencies between course elements.
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
@Service
public class DependencyMapperServiceImpl implements DependencyMapperService {
    
    private static final Logger log = Tracing.createLoggerFor(DependencyMapperServiceImpl.class);
    
    @Override
    public Map<String, NodeDependencyInfo> analyzeDependencies(ICourse course) {
        log.debug("Analyzing dependencies for course: {}", course.getResourceableId());
        
        Map<String, NodeDependencyInfo> dependencyMap = new HashMap<>();
        CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
        CourseEditorTreeNode rootNode = (CourseEditorTreeNode) editorTreeModel.getRootNode();
        
        if (rootNode == null) {
            return dependencyMap;
        }
        
        // First pass: collect all nodes
        List<CourseNode> allNodes = new ArrayList<>();
        TreeVisitor visitor = new TreeVisitor(node -> {
            if (node instanceof CourseEditorTreeNode) {
                CourseNode courseNode = ((CourseEditorTreeNode) node).getCourseNode();
                allNodes.add(courseNode);
                
                NodeDependencyInfo info = new NodeDependencyInfo(
                    courseNode.getIdent(),
                    courseNode.getType(),
                    courseNode.getShortTitle()
                );
                dependencyMap.put(courseNode.getIdent(), info);
            }
        }, rootNode, true);
        visitor.visitAll();
        
        // Second pass: analyze dependencies
        for (CourseNode node : allNodes) {
            analyzeNodeDependencies(node, dependencyMap, allNodes);
        }
        
        // Third pass: detect cyclic dependencies
        detectCyclicDependencies(dependencyMap);
        
        log.debug("Found {} nodes with {} total dependencies", 
            dependencyMap.size(), 
            dependencyMap.values().stream().mapToInt(d -> d.getDependsOn().size()).sum());
        
        return dependencyMap;
    }
    
    private void analyzeNodeDependencies(CourseNode node, Map<String, NodeDependencyInfo> dependencyMap,
            List<CourseNode> allNodes) {
        NodeDependencyInfo nodeInfo = dependencyMap.get(node.getIdent());
        if (nodeInfo == null) return;
        
        // Check visibility/access conditions
        analyzeConditionDependencies(node, nodeInfo, dependencyMap);
        
        // Check score calculations (for ST nodes)
        if (node instanceof STCourseNode) {
            analyzeScoreDependencies((STCourseNode) node, nodeInfo, dependencyMap);
        }
        
        // Check assessment dependencies
        analyzeAssessmentDependencies(node, nodeInfo, dependencyMap, allNodes);
        
        // Check shared resource references
        analyzeResourceDependencies(node, nodeInfo, dependencyMap, allNodes);
    }
    
    private void analyzeConditionDependencies(CourseNode node, NodeDependencyInfo nodeInfo,
            Map<String, NodeDependencyInfo> dependencyMap) {
        // Check preConditionAccess
        Condition accessCondition = node.getPreConditionAccess();
        if (accessCondition != null && accessCondition.getConditionExpression() != null) {
            extractNodeReferences(accessCondition.getConditionExpression(), nodeInfo, 
                dependencyMap, DependencyType.VISIBILITY_CONDITION);
        }
        
        // Check preConditionVisibility
        Condition visibilityCondition = node.getPreConditionVisibility();
        if (visibilityCondition != null && visibilityCondition.getConditionExpression() != null) {
            extractNodeReferences(visibilityCondition.getConditionExpression(), nodeInfo,
                dependencyMap, DependencyType.VISIBILITY_CONDITION);
        }
    }
    
    private void extractNodeReferences(String expression, NodeDependencyInfo nodeInfo,
            Map<String, NodeDependencyInfo> dependencyMap, DependencyType type) {
        // Parse expressions like: getAttempts("87654321") > 0 or getPassed("12345678")
        // Pattern matches node IDs in quotes within function calls
        if (expression == null || expression.isEmpty()) return;
        
        // Look for patterns like "nodeId" in the expression
        int idx = 0;
        while ((idx = expression.indexOf("\"", idx)) != -1) {
            int endIdx = expression.indexOf("\"", idx + 1);
            if (endIdx > idx + 1) {
                String potentialNodeId = expression.substring(idx + 1, endIdx);
                // Check if this is a valid node ID (typically numeric)
                if (potentialNodeId.matches("\\d+") && dependencyMap.containsKey(potentialNodeId)) {
                    nodeInfo.addDependsOn(potentialNodeId);
                    nodeInfo.addDependencyType(type);
                    
                    // Update reverse dependency
                    NodeDependencyInfo targetInfo = dependencyMap.get(potentialNodeId);
                    if (targetInfo != null) {
                        targetInfo.addDependedBy(nodeInfo.getNodeId());
                    }
                }
            }
            idx = endIdx + 1;
            if (idx >= expression.length()) break;
        }
    }
    
    private void analyzeScoreDependencies(STCourseNode stNode, NodeDependencyInfo nodeInfo,
            Map<String, NodeDependencyInfo> dependencyMap) {
        ModuleConfiguration config = stNode.getModuleConfiguration();
        
        // Check if score calculation is enabled
        String scoreCalculatorExpr = config.getStringValue("scoreCalculatorExpression");
        if (scoreCalculatorExpr != null && !scoreCalculatorExpr.isEmpty()) {
            extractNodeReferences(scoreCalculatorExpr, nodeInfo, dependencyMap, 
                DependencyType.SCORE_CALCULATION);
        }
        
        // Check passed calculation
        String passedCalculatorExpr = config.getStringValue("passedCalculatorExpression");
        if (passedCalculatorExpr != null && !passedCalculatorExpr.isEmpty()) {
            extractNodeReferences(passedCalculatorExpr, nodeInfo, dependencyMap,
                DependencyType.SCORE_CALCULATION);
        }
    }
    
    private void analyzeAssessmentDependencies(CourseNode node, NodeDependencyInfo nodeInfo,
            Map<String, NodeDependencyInfo> dependencyMap, List<CourseNode> allNodes) {
        // MS nodes may reference other assessment nodes
        if (node instanceof MSCourseNode) {
            ModuleConfiguration config = node.getModuleConfiguration();
            String referencedNodeId = config.getStringValue("referencedNode");
            if (referencedNodeId != null && dependencyMap.containsKey(referencedNodeId)) {
                nodeInfo.addDependsOn(referencedNodeId);
                nodeInfo.addDependencyType(DependencyType.ASSESSMENT_DEPENDENCY);
                
                NodeDependencyInfo targetInfo = dependencyMap.get(referencedNodeId);
                if (targetInfo != null) {
                    targetInfo.addDependedBy(nodeInfo.getNodeId());
                }
            }
        }
    }
    
    private void analyzeResourceDependencies(CourseNode node, NodeDependencyInfo nodeInfo,
            Map<String, NodeDependencyInfo> dependencyMap, List<CourseNode> allNodes) {
        // Check if nodes share the same softkey/resource reference
        ModuleConfiguration config = node.getModuleConfiguration();
        String softkey = config.getStringValue("softkey");
        
        if (softkey != null && !softkey.isEmpty()) {
            for (CourseNode otherNode : allNodes) {
                if (!otherNode.getIdent().equals(node.getIdent())) {
                    String otherSoftkey = otherNode.getModuleConfiguration().getStringValue("softkey");
                    if (softkey.equals(otherSoftkey)) {
                        nodeInfo.addDependsOn(otherNode.getIdent());
                        nodeInfo.addDependencyType(DependencyType.SHARED_RESOURCE);
                    }
                }
            }
        }
    }
    
    private void detectCyclicDependencies(Map<String, NodeDependencyInfo> dependencyMap) {
        for (NodeDependencyInfo info : dependencyMap.values()) {
            if (hasCycle(info.getNodeId(), dependencyMap, new HashSet<>())) {
                info.setHasCyclicDependency(true);
                log.warn("Cyclic dependency detected for node: {}", info.getNodeId());
            }
        }
    }
    
    private boolean hasCycle(String nodeId, Map<String, NodeDependencyInfo> dependencyMap, Set<String> visited) {
        if (visited.contains(nodeId)) {
            return true;
        }
        
        visited.add(nodeId);
        NodeDependencyInfo info = dependencyMap.get(nodeId);
        if (info != null) {
            for (String depId : info.getDependsOn()) {
                if (hasCycle(depId, dependencyMap, new HashSet<>(visited))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Set<String> getDependentNodes(ICourse course, String nodeId) {
        Map<String, NodeDependencyInfo> depMap = analyzeDependencies(course);
        NodeDependencyInfo info = depMap.get(nodeId);
        return info != null ? info.getDependedBy() : new HashSet<>();
    }
    
    @Override
    public Set<String> getRequiredNodes(ICourse course, String nodeId) {
        Map<String, NodeDependencyInfo> depMap = analyzeDependencies(course);
        NodeDependencyInfo info = depMap.get(nodeId);
        return info != null ? info.getDependsOn() : new HashSet<>();
    }
    
    @Override
    public DependencyValidationResult canDeleteNode(ICourse course, String nodeId) {
        Map<String, NodeDependencyInfo> depMap = analyzeDependencies(course);
        NodeDependencyInfo info = depMap.get(nodeId);
        
        if (info == null) {
            return DependencyValidationResult.valid();
        }
        
        Set<String> dependentNodes = info.getDependedBy();
        if (dependentNodes.isEmpty()) {
            return DependencyValidationResult.valid();
        }
        
        List<String> errors = new ArrayList<>();
        List<String> affectedIds = new ArrayList<>(dependentNodes);
        
        for (String depId : dependentNodes) {
            NodeDependencyInfo depInfo = depMap.get(depId);
            String nodeName = depInfo != null ? depInfo.getNodeTitle() : depId;
            errors.add("Node '" + nodeName + "' depends on this element");
        }
        
        String summary = dependentNodes.size() + " element(s) depend on this node and will be affected";
        return DependencyValidationResult.invalid(summary, errors, affectedIds);
    }
    
    @Override
    public List<String> getDuplicationOrder(ICourse course, Set<String> nodeIds) {
        Map<String, NodeDependencyInfo> depMap = analyzeDependencies(course);
        List<String> orderedList = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        // Topological sort using Kahn's algorithm
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> inDegree = new HashMap<>();
        
        // Initialize in-degrees for nodes in our set
        for (String nodeId : nodeIds) {
            int degree = 0;
            NodeDependencyInfo info = depMap.get(nodeId);
            if (info != null) {
                for (String dep : info.getDependsOn()) {
                    if (nodeIds.contains(dep)) {
                        degree++;
                    }
                }
            }
            inDegree.put(nodeId, degree);
            if (degree == 0) {
                queue.add(nodeId);
            }
        }
        
        // Process queue
        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            orderedList.add(nodeId);
            processed.add(nodeId);
            
            // Reduce in-degree for dependent nodes
            NodeDependencyInfo info = depMap.get(nodeId);
            if (info != null) {
                for (String dependentId : info.getDependedBy()) {
                    if (nodeIds.contains(dependentId) && !processed.contains(dependentId)) {
                        int newDegree = inDegree.get(dependentId) - 1;
                        inDegree.put(dependentId, newDegree);
                        if (newDegree == 0) {
                            queue.add(dependentId);
                        }
                    }
                }
            }
        }
        
        // Add any remaining nodes (might have cycles)
        for (String nodeId : nodeIds) {
            if (!processed.contains(nodeId)) {
                orderedList.add(nodeId);
            }
        }
        
        return orderedList;
    }
    
    @Override
    public CourseNode duplicateWithDependencies(ICourse course, String nodeId, boolean includeDependencies) {
        // This would integrate with existing course editor duplication logic
        // For now, return null as this requires deep integration with CourseEditorController
        log.info("Duplicate with dependencies requested for node: {}, includeDependencies: {}", 
            nodeId, includeDependencies);
        return null;
    }
    
    @Override
    public void updateReferencesAfterDuplication(ICourse course, Map<String, String> oldToNewNodeMapping) {
        log.debug("Updating {} node references after duplication", oldToNewNodeMapping.size());
        
        CourseEditorTreeModel editorTreeModel = course.getEditorTreeModel();
        CourseEditorTreeNode rootNode = (CourseEditorTreeNode) editorTreeModel.getRootNode();
        
        if (rootNode == null) return;
        
        // Visit all nodes and update references
        TreeVisitor visitor = new TreeVisitor(node -> {
            if (node instanceof CourseEditorTreeNode) {
                CourseNode courseNode = ((CourseEditorTreeNode) node).getCourseNode();
                updateNodeReferences(courseNode, oldToNewNodeMapping);
            }
        }, rootNode, true);
        visitor.visitAll();
    }
    
    private void updateNodeReferences(CourseNode node, Map<String, String> oldToNewMapping) {
        // Update condition expressions
        updateConditionReferences(node.getPreConditionAccess(), oldToNewMapping);
        updateConditionReferences(node.getPreConditionVisibility(), oldToNewMapping);
        
        // Update module configuration references
        ModuleConfiguration config = node.getModuleConfiguration();
        for (Map.Entry<String, String> entry : oldToNewMapping.entrySet()) {
            String oldId = entry.getKey();
            String newId = entry.getValue();
            
            // Update any string values that contain the old ID
            // Note: ModuleConfiguration stores values in a Map, we check common config keys
            String[] commonKeys = {"softkey", "assessedNodeIdent", "referencedNodeIdent", 
                                   "condition", "prerequisite", "nodeIdent"};
            for (String key : commonKeys) {
                Object value = config.get(key);
                if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.contains(oldId)) {
                        config.set(key, strValue.replace(oldId, newId));
                    }
                }
            }
        }
    }
    
    private void updateConditionReferences(Condition condition, Map<String, String> oldToNewMapping) {
        if (condition == null || condition.getConditionExpression() == null) return;
        
        String expression = condition.getConditionExpression();
        for (Map.Entry<String, String> entry : oldToNewMapping.entrySet()) {
            expression = expression.replace("\"" + entry.getKey() + "\"", 
                "\"" + entry.getValue() + "\"");
        }
        condition.setConditionExpression(expression);
    }
}
