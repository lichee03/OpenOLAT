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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.ICourse;
import org.olat.course.template.SelectiveCopyService;
import org.olat.course.template.SelectiveCopyService.ValidationResult;
import org.olat.course.template.model.SelectableCourseElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Enhancement 1: Selective Template Element Chooser
 * UI Controller for selecting course elements to copy
 * 
 * Initial date: January 18, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class SelectiveElementChooserController extends StepFormBasicController {
    
    private static final String[] KEYS_YES = new String[] { "yes" };
    
    private FlexiTableElement elementsTable;
    private SelectableElementTableModel tableModel;
    private FormLink selectAllLink;
    private FormLink deselectAllLink;
    private FormLink validateSelectionLink;
    private MultipleSelectionElement copyResourcesCheckbox;
    
    private final ICourse sourceCourse;
    private List<SelectableCourseElement> allElements;
    private Set<String> selectedElementIds;
    
    @Autowired
    private SelectiveCopyService selectiveCopyService;
    
    public SelectiveElementChooserController(UserRequest ureq, WindowControl wControl, Form rootForm, 
            StepsRunContext runContext, ICourse sourceCourse) {
        super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
        
        this.sourceCourse = sourceCourse;
        this.selectedElementIds = new HashSet<>();
        
        initForm(ureq);
    }
    
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("selective.element.chooser.title");
        setFormDescription("selective.element.chooser.description");
        
        // Extract selectable elements from source course
        allElements = selectiveCopyService.extractSelectableElements(sourceCourse);
        
        // Initially select all elements
        for (SelectableCourseElement element : allElements) {
            selectedElementIds.add(element.getNodeId());
        }
        
        // Create quick action links
        selectAllLink = uifactory.addFormLink("selectAll", "selective.element.chooser.select.all", null, 
                formLayout, Link.BUTTON);
        selectAllLink.setIconLeftCSS("o_icon o_icon_check_on");
        
        deselectAllLink = uifactory.addFormLink("deselectAll", "selective.element.chooser.deselect.all", null, 
                formLayout, Link.BUTTON);
        deselectAllLink.setIconLeftCSS("o_icon o_icon_check_off");
        
        // Create table columns FIRST
        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectableElementTableModel.ElementCols.title));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectableElementTableModel.ElementCols.type));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectableElementTableModel.ElementCols.level));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectableElementTableModel.ElementCols.resources));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectableElementTableModel.ElementCols.dependencies));
        
        // Create table model with columnsModel
        tableModel = new SelectableElementTableModel(columnsModel, allElements, selectedElementIds, getTranslator());
        elementsTable = uifactory.addTableElement(getWindowControl(), "elementsTable", tableModel, 
                getTranslator(), formLayout);
        elementsTable.setCustomizeColumns(true);
        elementsTable.setMultiSelect(true);
        elementsTable.setSelectAllEnable(true);
        
        // Copy resources checkbox
        String[] values = new String[] { translate("selective.element.chooser.copy.resources") };
        copyResourcesCheckbox = uifactory.addCheckboxesVertical("copyResources", "selective.element.chooser.copy.resources.label", 
                formLayout, KEYS_YES, values, 1);
        copyResourcesCheckbox.select(KEYS_YES[0], true);
        
        // Validate selection button
        validateSelectionLink = uifactory.addFormLink("validateSelection", "selective.element.chooser.validate", null, 
                formLayout, Link.BUTTON);
        validateSelectionLink.setIconLeftCSS("o_icon o_icon_preview");
    }
    
    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == selectAllLink) {
            selectAll();
        } else if (source == deselectAllLink) {
            deselectAll();
        } else if (source == validateSelectionLink) {
            validateSelection();
        } else if (source == elementsTable) {
            if (event.getCommand().equals("select")) {
                updateSelection();
            }
        }
        
        super.formInnerEvent(ureq, source, event);
    }
    
    private void selectAll() {
        selectedElementIds.clear();
        for (SelectableCourseElement element : allElements) {
            selectedElementIds.add(element.getNodeId());
            element.setSelected(true);
        }
        elementsTable.reset();
        showInfo("selective.element.chooser.all.selected");
    }
    
    private void deselectAll() {
        // Keep root node selected (it's required)
        selectedElementIds.clear();
        for (SelectableCourseElement element : allElements) {
            if (element.getLevel() == 0) {
                selectedElementIds.add(element.getNodeId());
                element.setSelected(true);
            } else {
                element.setSelected(false);
            }
        }
        elementsTable.reset();
        showInfo("selective.element.chooser.all.deselected");
    }
    
    private void updateSelection() {
        // Get selected rows from table
        Set<Integer> selectedRows = elementsTable.getMultiSelectedIndex();
        selectedElementIds.clear();
        
        for (int i = 0; i < allElements.size(); i++) {
            SelectableCourseElement element = allElements.get(i);
            boolean isSelected = selectedRows.contains(i);
            element.setSelected(isSelected);
            if (isSelected) {
                selectedElementIds.add(element.getNodeId());
            }
        }
    }
    
    private void validateSelection() {
        updateSelection();
        
        // Auto-resolve dependencies
        Set<String> resolvedSelection = selectiveCopyService.resolveAndAutoSelectDependencies(
                allElements, selectedElementIds);
        
        // Update if dependencies were added
        if (resolvedSelection.size() > selectedElementIds.size()) {
            selectedElementIds = resolvedSelection;
            for (SelectableCourseElement element : allElements) {
                element.setSelected(selectedElementIds.contains(element.getNodeId()));
            }
            elementsTable.reset();
            showInfo("selective.element.chooser.dependencies.added", 
                    new String[] { String.valueOf(resolvedSelection.size() - selectedElementIds.size()) });
        }
        
        // Validate
        ValidationResult result = selectiveCopyService.validateSelection(allElements, selectedElementIds);
        
        if (result.isValid()) {
            if (result.hasWarnings()) {
                StringBuilder warnings = new StringBuilder();
                for (String warning : result.getWarnings()) {
                    warnings.append(warning).append("<br/>");
                }
                showWarning("selective.element.chooser.validation.warnings", warnings.toString());
            } else {
                showInfo("selective.element.chooser.validation.success");
            }
        } else {
            StringBuilder errors = new StringBuilder();
            for (String error : result.getErrors()) {
                errors.append(error).append("<br/>");
            }
            showError("selective.element.chooser.validation.errors", errors.toString());
        }
    }
    
    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);
        
        updateSelection();
        
        // Validate selection
        ValidationResult result = selectiveCopyService.validateSelection(allElements, selectedElementIds);
        
        if (!result.isValid()) {
            for (String error : result.getErrors()) {
                showError(error);
            }
            allOk = false;
        }
        
        return allOk;
    }
    
    @Override
    protected void formOK(UserRequest ureq) {
        updateSelection();
        
        // Store selection in run context
        StepsRunContext context = getRunContext();
        context.put("selectedElementIds", selectedElementIds);
        context.put("copyLearningResources", copyResourcesCheckbox.isSelected(0));
        context.put("allElements", allElements);
        
        fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
    }
    
    public Set<String> getSelectedElementIds() {
        return selectedElementIds;
    }
    
    public boolean isCopyLearningResources() {
        return copyResourcesCheckbox.isSelected(0);
    }
}
