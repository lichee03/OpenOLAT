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
package org.olat.course.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.olat.course.template.model.SelectableCourseElement;

/**
 * Enhancement 1: Unit tests for SelectableCourseElement model
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class SelectiveCopyServiceTest {
    
    @Test
    public void testSelectableCourseElementModel() {
        // Test basic element creation using actual constructor
        SelectableCourseElement element = new SelectableCourseElement("node1", "st", "Test Node");
        
        Assert.assertNotNull("Element should not be null", element);
        Assert.assertEquals("node1", element.getNodeId());
        Assert.assertEquals("Test Node", element.getShortTitle());
        Assert.assertEquals("st", element.getNodeType());
        
        // Set additional properties
        element.setLevel(0);
        element.setLongTitle("Test Description");
        Assert.assertEquals(0, element.getLevel());
        Assert.assertEquals("Test Description", element.getLongTitle());
        
        // Test selection state - default is true according to model
        Assert.assertTrue("Element should be selected by default", element.isSelected());
        element.setSelected(false);
        Assert.assertFalse("Element should not be selected after setting", element.isSelected());
        
        // Test required state
        Assert.assertFalse("Element should not be required by default", element.isRequired());
        element.setRequired(true);
        Assert.assertTrue("Element should be required after setting", element.isRequired());
    }
    
    @Test
    public void testSelectableCourseElementDependencies() {
        SelectableCourseElement element = new SelectableCourseElement("node1", "st", "Test Node");
        
        // Test dependencies
        Assert.assertNotNull("Dependencies should not be null", element.getDependencies());
        Assert.assertTrue("Dependencies should be empty initially", element.getDependencies().isEmpty());
        
        element.addDependency("node2");
        Assert.assertEquals(1, element.getDependencies().size());
        Assert.assertTrue("Should contain node2 dependency", element.getDependencies().contains("node2"));
        
        // Test duplicate prevention
        element.addDependency("node2");
        Assert.assertEquals(1, element.getDependencies().size());
    }
    
    @Test
    public void testSelectableCourseElementLearningResources() {
        SelectableCourseElement element = new SelectableCourseElement("node1", "iqtest", "Test Node");
        
        // Test learning resources - these are Strings not Long
        Assert.assertNotNull("Learning resources should not be null", element.getLearningResources());
        Assert.assertTrue("Learning resources should be empty initially", element.getLearningResources().isEmpty());
        
        element.addLearningResource("12345");
        Assert.assertEquals(1, element.getLearningResources().size());
        Assert.assertTrue("Should contain resource 12345", element.getLearningResources().contains("12345"));
    }
    
    @Test
    public void testSelectableCourseElementParentChild() {
        SelectableCourseElement parent = new SelectableCourseElement("parent", "st", "Parent Node");
        SelectableCourseElement child = new SelectableCourseElement("child", "sp", "Child Node");
        child.setLevel(1);
        
        // Test parent-child relationship
        Assert.assertNull("Parent should be null initially", child.getParent());
        parent.addChild(child);
        Assert.assertEquals("Parent should be set", parent, child.getParent());
        Assert.assertEquals(1, parent.getChildren().size());
    }
    
    @Test
    public void testSelectedIdsSetOperations() {
        // Test that the selected IDs set operations work correctly
        Set<String> selectedIds = new HashSet<>();
        
        // Add elements
        selectedIds.add("node1");
        selectedIds.add("node2");
        selectedIds.add("node3");
        
        Assert.assertEquals(3, selectedIds.size());
        Assert.assertTrue(selectedIds.contains("node1"));
        Assert.assertTrue(selectedIds.contains("node2"));
        Assert.assertTrue(selectedIds.contains("node3"));
        
        // Remove element
        selectedIds.remove("node2");
        Assert.assertEquals(2, selectedIds.size());
        Assert.assertFalse(selectedIds.contains("node2"));
        
        // Clear all
        selectedIds.clear();
        Assert.assertTrue(selectedIds.isEmpty());
    }
    
    @Test
    public void testHasMissingDependencies() {
        SelectableCourseElement element1 = new SelectableCourseElement("node1", "st", "Node 1");
        SelectableCourseElement element2 = new SelectableCourseElement("node2", "sp", "Node 2");
        element2.addDependency("node1");
        
        List<SelectableCourseElement> allElements = List.of(element1, element2);
        
        // Both selected - no missing dependencies
        Assert.assertFalse("No missing dependencies when both selected", element2.hasMissingDependencies(allElements));
        
        // Deselect dependency
        element1.setSelected(false);
        Assert.assertTrue("Should have missing dependency when node1 not selected", element2.hasMissingDependencies(allElements));
    }
    
    @Test
    public void testGetAllDescendants() {
        SelectableCourseElement root = new SelectableCourseElement("root", "st", "Root");
        SelectableCourseElement child1 = new SelectableCourseElement("child1", "sp", "Child 1");
        SelectableCourseElement child2 = new SelectableCourseElement("child2", "sp", "Child 2");
        SelectableCourseElement grandchild = new SelectableCourseElement("grandchild", "sp", "Grandchild");
        
        root.addChild(child1);
        root.addChild(child2);
        child1.addChild(grandchild);
        
        List<SelectableCourseElement> descendants = root.getAllDescendants();
        Assert.assertEquals(3, descendants.size());
        Assert.assertTrue(descendants.contains(child1));
        Assert.assertTrue(descendants.contains(child2));
        Assert.assertTrue(descendants.contains(grandchild));
    }
    
    @Test
    public void testCanBeDeselected() {
        SelectableCourseElement requiredElement = new SelectableCourseElement("root", "st", "Root");
        requiredElement.setRequired(true);
        
        SelectableCourseElement optionalElement = new SelectableCourseElement("optional", "sp", "Optional");
        optionalElement.setRequired(false);
        
        Assert.assertFalse("Required element should not be deselectable", requiredElement.canBeDeselected());
        Assert.assertTrue("Optional element should be deselectable", optionalElement.canBeDeselected());
    }
}
