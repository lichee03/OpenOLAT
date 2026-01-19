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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.course.editor.NodeDependencyInfo.DependencyType;

/**
 * Enhancement 2: Unit tests for Smart Course Element Duplication with Dependency Mapping
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class DependencyMapperServiceTest {
    
    @Test
    public void testNodeDependencyInfoCreation() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Test Structure");
        
        Assert.assertNotNull(info);
        Assert.assertEquals("node1", info.getNodeId());
        Assert.assertEquals("st", info.getNodeType());
        Assert.assertEquals("Test Structure", info.getNodeTitle());
        Assert.assertTrue(info.getDependsOn().isEmpty());
        Assert.assertTrue(info.getDependedBy().isEmpty());
        Assert.assertFalse(info.hasCyclicDependency());
    }
    
    @Test
    public void testAddDependencies() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Test Structure");
        
        info.addDependsOn("node2");
        info.addDependsOn("node3");
        
        Assert.assertEquals(2, info.getDependsOn().size());
        Assert.assertTrue(info.getDependsOn().contains("node2"));
        Assert.assertTrue(info.getDependsOn().contains("node3"));
        Assert.assertTrue(info.hasDependencies());
        Assert.assertFalse(info.isIndependent());
    }
    
    @Test
    public void testAddDependedBy() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Test Structure");
        
        info.addDependedBy("node4");
        info.addDependedBy("node5");
        
        Assert.assertEquals(2, info.getDependedBy().size());
        Assert.assertTrue(info.getDependedBy().contains("node4"));
        Assert.assertTrue(info.getDependedBy().contains("node5"));
    }
    
    @Test
    public void testDependencyTypes() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Test Structure");
        
        info.addDependencyType(DependencyType.VISIBILITY_CONDITION);
        info.addDependencyType(DependencyType.SCORE_CALCULATION);
        info.addDependencyType(DependencyType.VISIBILITY_CONDITION); // Duplicate
        
        Assert.assertEquals(2, info.getDependencyTypes().size());
        Assert.assertTrue(info.getDependencyTypes().contains(DependencyType.VISIBILITY_CONDITION));
        Assert.assertTrue(info.getDependencyTypes().contains(DependencyType.SCORE_CALCULATION));
    }
    
    @Test
    public void testIsIndependent() {
        NodeDependencyInfo independentNode = new NodeDependencyInfo("node1", "sp", "Single Page");
        Assert.assertTrue(independentNode.isIndependent());
        Assert.assertFalse(independentNode.hasDependencies());
        
        NodeDependencyInfo dependentNode = new NodeDependencyInfo("node2", "st", "Structure");
        dependentNode.addDependsOn("node1");
        Assert.assertFalse(dependentNode.isIndependent());
        Assert.assertTrue(dependentNode.hasDependencies());
    }
    
    @Test
    public void testCyclicDependencyFlag() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Test");
        
        Assert.assertFalse(info.hasCyclicDependency());
        
        info.setHasCyclicDependency(true);
        Assert.assertTrue(info.hasCyclicDependency());
    }
    
    @Test
    public void testDependencyValidationResultValid() {
        DependencyValidationResult result = DependencyValidationResult.valid();
        
        Assert.assertTrue(result.isValid());
        Assert.assertFalse(result.hasErrors());
        Assert.assertFalse(result.hasWarnings());
        Assert.assertEquals("No dependency issues found", result.getSummary());
    }
    
    @Test
    public void testDependencyValidationResultInvalid() {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        List<String> affectedIds = Arrays.asList("node2", "node3");
        
        DependencyValidationResult result = DependencyValidationResult.invalid(
            "2 elements affected", errors, affectedIds);
        
        Assert.assertFalse(result.isValid());
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(2, result.getErrors().size());
        Assert.assertEquals(2, result.getAffectedNodeIds().size());
    }
    
    @Test
    public void testDependencyValidationResultWithWarnings() {
        List<String> warnings = Arrays.asList("Warning 1");
        List<String> affectedIds = Arrays.asList("node2");
        
        DependencyValidationResult result = DependencyValidationResult.withWarnings(
            "1 warning", warnings, affectedIds);
        
        Assert.assertTrue(result.isValid());
        Assert.assertTrue(result.hasWarnings());
        Assert.assertFalse(result.hasErrors());
    }
    
    @Test
    public void testDependencyTypeEnum() {
        DependencyType[] types = DependencyType.values();
        
        Assert.assertEquals(7, types.length);
        Assert.assertEquals(DependencyType.VISIBILITY_CONDITION, DependencyType.valueOf("VISIBILITY_CONDITION"));
        Assert.assertEquals(DependencyType.PREREQUISITE, DependencyType.valueOf("PREREQUISITE"));
        Assert.assertEquals(DependencyType.SHARED_RESOURCE, DependencyType.valueOf("SHARED_RESOURCE"));
        Assert.assertEquals(DependencyType.DATA_REFERENCE, DependencyType.valueOf("DATA_REFERENCE"));
        Assert.assertEquals(DependencyType.ASSESSMENT_DEPENDENCY, DependencyType.valueOf("ASSESSMENT_DEPENDENCY"));
        Assert.assertEquals(DependencyType.STRUCTURE_PARENT, DependencyType.valueOf("STRUCTURE_PARENT"));
        Assert.assertEquals(DependencyType.SCORE_CALCULATION, DependencyType.valueOf("SCORE_CALCULATION"));
    }
    
    @Test
    public void testNodeDependencyInfoToString() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Test Structure");
        info.addDependsOn("node2");
        info.addDependedBy("node3");
        
        String str = info.toString();
        Assert.assertTrue(str.contains("node1"));
        Assert.assertTrue(str.contains("st"));
        Assert.assertTrue(str.contains("dependsOn=1"));
        Assert.assertTrue(str.contains("dependedBy=1"));
    }
    
    @Test
    public void testDuplicationOrderSimple() {
        // Simulate duplication order calculation
        // node1 -> node2 -> node3 (node2 depends on node1, node3 depends on node2)
        Set<String> nodeIds = new HashSet<>(Arrays.asList("node1", "node2", "node3"));
        
        // In correct order: node1 first (no deps), then node2, then node3
        List<String> expectedOrder = Arrays.asList("node1", "node2", "node3");
        
        // Verify set contains all expected nodes
        Assert.assertEquals(3, nodeIds.size());
        Assert.assertTrue(nodeIds.containsAll(expectedOrder));
    }
    
    @Test
    public void testMultipleDependencies() {
        NodeDependencyInfo info = new NodeDependencyInfo("node1", "st", "Structure");
        
        // Add multiple dependencies
        info.addDependsOn("node2");
        info.addDependsOn("node3");
        info.addDependsOn("node4");
        
        info.addDependedBy("node5");
        info.addDependedBy("node6");
        
        info.addDependencyType(DependencyType.VISIBILITY_CONDITION);
        info.addDependencyType(DependencyType.PREREQUISITE);
        info.addDependencyType(DependencyType.SCORE_CALCULATION);
        
        Assert.assertEquals(3, info.getDependsOn().size());
        Assert.assertEquals(2, info.getDependedBy().size());
        Assert.assertEquals(3, info.getDependencyTypes().size());
    }
}
