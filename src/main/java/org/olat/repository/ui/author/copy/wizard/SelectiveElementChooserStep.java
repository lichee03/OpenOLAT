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
package org.olat.repository.ui.author.copy.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.ICourse;
import org.olat.course.template.ui.SelectiveElementChooserController;

/**
 * Enhancement 1: Selective Template Element Chooser
 * Wizard step for selecting which course elements to copy
 * 
 * Initial date: January 18, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class SelectiveElementChooserStep extends BasicStep {
    
    private final ICourse sourceCourse;
    
    public SelectiveElementChooserStep(UserRequest ureq, ICourse sourceCourse) {
        super(ureq);
        this.sourceCourse = sourceCourse;
        setI18nTitleAndDescr("selective.element.chooser.step.title", "selective.element.chooser.step.description");
        setNextStep(NOSTEP); // This can be configured based on workflow
    }
    
    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return new PrevNextFinishConfig(true, true, false);
    }
    
    @Override
    public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, 
            StepsRunContext stepsRunContext, Form form) {
        return new SelectiveElementChooserController(ureq, windowControl, form, stepsRunContext, sourceCourse);
    }
}
