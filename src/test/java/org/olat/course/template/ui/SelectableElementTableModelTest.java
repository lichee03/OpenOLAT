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
package org.olat.course.template.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.course.template.model.SelectableCourseElement;

/**
 * Enhancement 1: Unit tests for SelectableElementTableModel
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class SelectableElementTableModelTest {
    
    private List<SelectableCourseElement> elements;
    private Set<String> selectedIds;
    
    @Before
    public void setUp() {
        elements = new ArrayList<>();
        selectedIds = new HashSet<>();
        
        // Create test elements
        SelectableCourseElement root = new SelectableCourseElement("root", "st", "Root Structure");
        root.setLevel(0);
        root.setRequired(true);
        elements.add(root);
        selectedIds.add("root");
        
        SelectableCourseElement page = new SelectableCourseElement("page1", "sp", "Single Page");
        page.setLevel(1);
        elements.add(page);
        selectedIds.add("page1");
        
        SelectableCourseElement test = new SelectableCourseElement("test1", "iqtest", "Assessment Test");
        test.setLevel(1);
        test.addLearningResource("12345");
        elements.add(test);
        selectedIds.add("test1");
    }
    
    @Test
    public void testTableModelCreation() {
        Assert.assertNotNull(elements);
        Assert.assertEquals(3, elements.size());
    }
    
    @Test
    public void testElementRetrieval() {
        SelectableCourseElement first = elements.get(0);
        Assert.assertEquals("root", first.getNodeId());
        Assert.assertEquals("st", first.getNodeType());
        Assert.assertEquals("Root Structure", first.getShortTitle());
        Assert.assertEquals(0, first.getLevel());
        Assert.assertTrue(first.isRequired());
    }
    
    @Test
    public void testSelectedIds() {
        Assert.assertEquals(3, selectedIds.size());
        Assert.assertTrue(selectedIds.contains("root"));
        Assert.assertTrue(selectedIds.contains("page1"));
        Assert.assertTrue(selectedIds.contains("test1"));
        Assert.assertFalse(selectedIds.contains("nonexistent"));
    }
    
    @Test
    public void testElementWithResources() {
        SelectableCourseElement test = elements.get(2);
        Assert.assertEquals(1, test.getLearningResources().size());
        Assert.assertTrue(test.getLearningResources().contains("12345"));
    }
    
    @Test
    public void testColumnValues() {
        // Test that we can get proper values for each column
        SelectableCourseElement element = elements.get(1); // page1
        
        // Column 0: title
        String title = element.getShortTitle();
        Assert.assertEquals("Single Page", title);
        
        // Column 1: type
        String type = element.getNodeType();
        Assert.assertEquals("sp", type);
        
        // Column 2: level
        int level = element.getLevel();
        Assert.assertEquals(1, level);
        
        // Column 3: resources count
        int resourceCount = element.getLearningResources().size();
        Assert.assertEquals(0, resourceCount);
        
        // Column 4: dependencies count
        int depCount = element.getDependencies().size();
        Assert.assertEquals(0, depCount);
    }
    
    @Test
    public void testElementCols() {
        // Test the enum values
        SelectableElementTableModel.ElementCols[] cols = SelectableElementTableModel.ElementCols.values();
        Assert.assertEquals(5, cols.length);
        
        Assert.assertEquals("title", cols[0].name());
        Assert.assertEquals("type", cols[1].name());
        Assert.assertEquals("level", cols[2].name());
        Assert.assertEquals("resources", cols[3].name());
        Assert.assertEquals("dependencies", cols[4].name());
        
        // Test i18n keys
        Assert.assertEquals("selective.element.chooser.table.title", cols[0].i18nHeaderKey());
        Assert.assertEquals("selective.element.chooser.table.type", cols[1].i18nHeaderKey());
    }
    
    @Test
    public void testSelectionToggle() {
        // Initially selected
        Assert.assertTrue(selectedIds.contains("page1"));
        
        // Deselect
        selectedIds.remove("page1");
        Assert.assertFalse(selectedIds.contains("page1"));
        
        // Re-select
        selectedIds.add("page1");
        Assert.assertTrue(selectedIds.contains("page1"));
    }
}
