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
package org.olat.course.member;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Enhancement 3: Bulk Staff Assignment
 * 
 * Controller for assigning multiple users to multiple courses at once.
 * Users can enter usernames/emails in bulk and select role to assign.
 * 
 * @author OpenOLAT Enhancement Team
 */
public class BulkStaffAssignmentController extends FormBasicController {

    private static final String[] ROLE_KEYS = new String[] { 
        GroupRoles.owner.name(), 
        GroupRoles.coach.name(), 
        GroupRoles.participant.name() 
    };
    
    private TextAreaElement usernamesEl;
    private SingleSelection roleEl;
    private MultipleSelectionElement sendMailEl;
    
    private final RepositoryEntry repoEntry;
    private List<Identity> validIdentities;
    private List<String> invalidUsernames;
    
    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private BulkStaffAssignmentService bulkAssignmentService;
    
    public BulkStaffAssignmentController(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry) {
        super(ureq, wControl);
        this.repoEntry = repoEntry;
        this.validIdentities = new ArrayList<>();
        this.invalidUsernames = new ArrayList<>();
        
        initForm(ureq);
    }
    
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("bulk.assign.title");
        setFormDescription("bulk.assign.description");
        
        // Usernames text area
        usernamesEl = uifactory.addTextAreaElement("bulk.usernames", "bulk.usernames", 10000, 8, 60, false, false, "", formLayout);
        usernamesEl.setHelpTextKey("bulk.usernames.hint", null);
        usernamesEl.setMandatory(true);
        
        // Role selection
        String[] roleValues = new String[] {
            translate("role.repo.owner"),
            translate("role.repo.tutor"),
            translate("role.repo.participant")
        };
        roleEl = uifactory.addDropdownSingleselect("bulk.role", "bulk.role", formLayout, ROLE_KEYS, roleValues);
        roleEl.select(GroupRoles.participant.name(), true);
        
        // Send mail option
        String[] sendMailKeys = new String[] { "send" };
        String[] sendMailValues = new String[] { translate("bulk.sendmail.label") };
        sendMailEl = uifactory.addCheckboxesHorizontal("bulk.sendmail", "bulk.sendmail", formLayout, sendMailKeys, sendMailValues);
        
        // Buttons
        FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        formLayout.add(buttonsCont);
        uifactory.addFormSubmitButton("bulk.validate", buttonsCont);
        uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
    }
    
    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean valid = super.validateFormLogic(ureq);
        
        usernamesEl.clearError();
        if (usernamesEl.getValue().trim().isEmpty()) {
            usernamesEl.setErrorKey("form.legende.mandatory");
            valid = false;
        }
        
        return valid;
    }
    
    @Override
    protected void formOK(UserRequest ureq) {
        // Parse and validate usernames
        String input = usernamesEl.getValue();
        Set<String> usernames = parseUsernames(input);
        
        validIdentities.clear();
        invalidUsernames.clear();
        
        for (String username : usernames) {
            Identity identity = securityManager.findIdentityByNameCaseInsensitive(username);
            if (identity == null) {
                // Try by email
                identity = userManager.findUniqueIdentityByEmail(username);
            }
            
            if (identity != null) {
                validIdentities.add(identity);
            } else {
                invalidUsernames.add(username);
            }
        }
        
        if (validIdentities.isEmpty()) {
            showError("bulk.no.valid.users");
            return;
        }
        
        // Show validation summary
        if (!invalidUsernames.isEmpty()) {
            String invalidList = String.join(", ", invalidUsernames);
            showWarning("bulk.invalid.users", invalidList);
        }
        
        // Perform the assignment
        GroupRoles role = GroupRoles.valueOf(roleEl.getSelectedKey());
        
        BulkAssignmentResult result = bulkAssignmentService.assignStaffToCourse(
            repoEntry, 
            validIdentities, 
            role
        );
        
        if (result.isSuccess()) {
            showInfo("bulk.assign.success", String.valueOf(result.getSuccessCount()));
            fireEvent(ureq, Event.DONE_EVENT);
        } else {
            String errorMsg = result.getErrors().isEmpty() ? 
                translate("bulk.assign.error") : 
                result.getErrors().get(0).getErrorMessage();
            showError("bulk.assign.partial", new String[] { 
                String.valueOf(result.getSuccessCount()), 
                String.valueOf(result.getFailureCount()),
                errorMsg
            });
            if (result.getSuccessCount() > 0) {
                fireEvent(ureq, Event.CHANGED_EVENT);
            }
        }
    }
    
    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
    
    private Set<String> parseUsernames(String input) {
        Set<String> usernames = new HashSet<>();
        if (input == null || input.trim().isEmpty()) {
            return usernames;
        }
        
        // Split by newlines, commas, semicolons, or tabs
        String[] parts = input.split("[,;\\s\\n\\r\\t]+");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                usernames.add(trimmed);
            }
        }
        return usernames;
    }
    
    public List<Identity> getValidIdentities() {
        return validIdentities;
    }
    
    public List<String> getInvalidUsernames() {
        return invalidUsernames;
    }
}
