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
package org.olat.course.enhancement.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.course.enhancement.SelectiveTemplateInstantiationService;
import org.olat.course.enhancement.SelectionPreview;
import org.olat.course.enhancement.SelectionValidationResult;
import org.olat.course.enhancement.TemplateElement;
import org.olat.course.enhancement.TemplateResource;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for selecting course elements and resources when creating from template
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public class SelectiveTemplateInstantiateController extends BasicController {
	
	private static final Logger log = Tracing.createLogger(SelectiveTemplateInstantiateController.class);
	
	private VelocityContainer mainVC;
	private Link continueButton;
	private Link backButton;
	
	@Autowired
	private SelectiveTemplateInstantiationService selectiveTemplateService;
	
	private RepositoryEntry templateEntry;
	private Set<String> selectedNodeIds;
	private Set<String> selectedResourceIds;
	
	public SelectiveTemplateInstantiateController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry templateEntry) {
		
		super(ureq, wControl);
		this.templateEntry = templateEntry;
		this.selectedNodeIds = new HashSet<>();
		this.selectedResourceIds = new HashSet<>();
		
		log.info("Initializing selective template instantiation for: {}", 
			templateEntry.getDisplayname());
		
		initForm(ureq);
		loadTemplateElements();
		loadTemplateResources();
	}
	
	private void initForm(UserRequest ureq) {
		mainVC = createVelocityContainer("selective_template");
		
		// Elements section
		VelocityContainer elementsVC = createVelocityContainer("template_elements");
		mainVC.put("elements", elementsVC);
		
		// Resources section
		VelocityContainer resourcesVC = createVelocityContainer("template_resources");
		mainVC.put("resources", resourcesVC);
		
		// Copy options
		VelocityContainer optionsVC = createVelocityContainer("copy_options");
		mainVC.put("options", optionsVC);
		
		// Preview section
		VelocityContainer previewVC = createVelocityContainer("selection_preview");
		mainVC.put("preview", previewVC);
		
		putInitialPanel(mainVC);
	}
	
	private void loadTemplateElements() {
		try {
			List<TemplateElement> elements = selectiveTemplateService.getTemplateElements(templateEntry);
			log.info("Loaded {} template elements", elements.size());
			
			// Build hierarchical display
			// This would be rendered in the velocity template
			mainVC.contextPut("templateElements", elements);
		} catch (Exception e) {
			log.error("Error loading template elements: " + e.getMessage(), e);
			showError("error.loading.elements");
		}
	}
	
	private void loadTemplateResources() {
		try {
			List<TemplateResource> resources = selectiveTemplateService
				.getTemplateResources(templateEntry);
			log.info("Loaded {} template resources", resources.size());
			
			mainVC.contextPut("templateResources", resources);
		} catch (Exception e) {
			log.error("Error loading template resources: " + e.getMessage(), e);
			showError("error.loading.resources");
		}
	}
	
	private void updateSelection() {
		try {
			// Collect selected element IDs from form
			selectedNodeIds.clear();
			selectedResourceIds.clear();
			
			// Get selected from UI (would be populated from form data)
			// Validate selection
			SelectionValidationResult validation = selectiveTemplateService
				.validateSelection(templateEntry, selectedNodeIds);
			
			if (!validation.isValid()) {
				showWarning("error.invalid.selection");
				for (String error : validation.getErrors()) {
					showWarning(error);
				}
				return;
			}
			
			// Show suggestions
			if (!validation.getSuggestedAutoInclusions().isEmpty()) {
				showInfo("suggestion.auto.include", 
					String.valueOf(validation.getSuggestedAutoInclusions().size()));
			}
			
			// Generate preview
			SelectionPreview preview = selectiveTemplateService.previewSelection(
				templateEntry, selectedNodeIds, selectedResourceIds);
			
			mainVC.contextPut("preview", preview);
		} catch (Exception e) {
			log.error("Error updating selection: " + e.getMessage(), e);
			showError("error.update.selection");
		}
	}
	
	@Override
	protected void doDispose() {
		// Cleanup resources if needed
		super.doDispose();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Process form submission
		updateSelection();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == continueButton) {
			updateSelection();
			// Fire event with selection
			fireEvent(ureq, new SelectionEvent(selectedNodeIds, selectedResourceIds));
		} else if (source == backButton) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
		super.event(ureq, source, event);
	}
	
	/**
	 * Event fired when selection is confirmed
	 */
	public static class SelectionEvent extends Event {
		private static final long serialVersionUID = 1L;
		
		private Set<String> selectedNodeIds;
		private Set<String> selectedResourceIds;
		
		public SelectionEvent(Set<String> selectedNodeIds, Set<String> selectedResourceIds) {
			super("selection");
			this.selectedNodeIds = new HashSet<>(selectedNodeIds);
			this.selectedResourceIds = new HashSet<>(selectedResourceIds);
		}
		
		public Set<String> getSelectedNodeIds() {
			return selectedNodeIds;
		}
		
		public Set<String> getSelectedResourceIds() {
			return selectedResourceIds;
		}
	}
}
