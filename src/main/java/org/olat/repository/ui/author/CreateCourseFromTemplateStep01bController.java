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
package org.olat.repository.ui.author;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.CoreSpringFactory;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.template.model.SelectableCourseElement;
import org.olat.course.template.ui.SelectableElementTableModel;
import org.olat.course.template.ui.SelectableElementTableModel.ElementCols;
import org.olat.course.template.SelectiveCopyService;
import org.olat.repository.RepositoryEntry;

/**
 * Enhancement 1: Selective Element Chooser Controller
 * 
 * Displays a hierarchical table of all course elements from the selected template
 * and allows users to choose which elements to include in their new course.
 * 
 * Initial date: 2026-01-18<br>
 *
 * @author Enhancement Team
 */
public class CreateCourseFromTemplateStep01bController extends StepFormBasicController {
	
	private SelectiveCopyService selectiveCopyService;
	
	private CreateCourseFromTemplateContext context;
	private final StepsRunContext runContext;
	private FlexiTableElement tableEl;
	private SelectableElementTableModel tableModel;
	private FormLink selectAllLink;
	private FormLink deselectAllLink;
	
	private List<SelectableCourseElement> allElements;
	
	public CreateCourseFromTemplateStep01bController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, CreateCourseFromTemplateContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		this.runContext = runContext;
		this.selectiveCopyService = CoreSpringFactory.getImpl(SelectiveCopyService.class);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.step.select.elements");
		setFormDescription("wizard.step.select.elements.description");
		
		// Get context from run context (set by previous step)
		if (runContext.get(CreateCourseFromTemplateContext.KEY) instanceof CreateCourseFromTemplateContext ctx) {
			this.context = ctx;
		}
		
		// Load course elements from the selected template
		RepositoryEntry templateEntry = context != null ? context.getTemplateRepositoryEntry() : null;
		if (templateEntry != null) {
			ICourse templateCourse = CourseFactory.loadCourse(templateEntry);
			allElements = selectiveCopyService.extractSelectableElements(templateCourse);
			
			// Create select all / deselect all links
			selectAllLink = uifactory.addFormLink("selectAll", "select.all", null, formLayout, Link.LINK);
			deselectAllLink = uifactory.addFormLink("deselectAll", "deselect.all", null, formLayout, Link.LINK);
			
			// Create table model with selected IDs from context
			Set<String> selectedIds = context.getSelectedElementIds();
			if (selectedIds == null) {
				selectedIds = new HashSet<>();
				// Select all by default
				for (SelectableCourseElement element : allElements) {
					selectedIds.add(element.getNodeId());
				}
				context.setSelectedElementIds(selectedIds);
			}
			
			// Create table columns FIRST
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.title));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.type));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.level));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.resources));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.dependencies));
			
			// Create table model with columnsModel
			tableModel = new SelectableElementTableModel(columnsModel, allElements, selectedIds, getTranslator());
			
			tableEl = uifactory.addTableElement(getWindowControl(), "elements", tableModel, getTranslator(), formLayout);
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			
			// Select all elements by default
			selectAll();
		} else {
			setFormWarning("error.no.template.selected");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectAllLink) {
			selectAll();
		} else if (source == deselectAllLink) {
			deselectAll();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void selectAll() {
		if (tableEl == null || allElements == null) return;
		
		tableEl.selectAll();
		Set<String> selectedIds = context.getSelectedElementIds();
		if (selectedIds == null) {
			selectedIds = new HashSet<>();
			context.setSelectedElementIds(selectedIds);
		}
		for (SelectableCourseElement element : allElements) {
			element.setSelected(true);
			selectedIds.add(element.getNodeId());
		}
		tableEl.reloadData();
	}
	
	private void deselectAll() {
		if (tableEl == null || allElements == null) return;
		
		tableEl.deselectAll();
		Set<String> selectedIds = context.getSelectedElementIds();
		if (selectedIds != null) {
			selectedIds.clear();
		}
		for (SelectableCourseElement element : allElements) {
			element.setSelected(false);
		}
		tableEl.reloadData();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		// If table is not initialized, skip validation
		if (tableEl == null || allElements == null) {
			return allOk;
		}
		
		// Check that at least one element is selected
		Set<Integer> selectedIndices = tableEl.getMultiSelectedIndex();
		if (selectedIndices.isEmpty()) {
			tableEl.setErrorKey("error.no.elements.selected");
			allOk = false;
		} else {
			tableEl.clearError();
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// If table is not initialized, just proceed
		if (tableEl == null || allElements == null) {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			return;
		}
		
		// Collect selected element IDs
		Set<Integer> selectedIndices = tableEl.getMultiSelectedIndex();
		Set<String> selectedIds = new HashSet<>();
		
		boolean allSelected = selectedIndices.size() == allElements.size();
		
		for (Integer index : selectedIndices) {
			if (index < allElements.size()) {
				SelectableCourseElement element = allElements.get(index);
				selectedIds.add(element.getNodeId());
				element.setSelected(true);
			}
		}
		
		// Store selections in context
		context.setSelectedElementIds(selectedIds);
		context.setCopyAllElements(allSelected);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
