/**
 * OLAT - Online Learning and Training<br>
 * https://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <hr>
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * This file has been modified by the OpenOLAT community. Changes are licensed
 * under the Apache 2.0 license as the original file.
 */
package org.olat.course.nodes.en.validation.rules;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.validation.EnrollmentEligibility;
import org.olat.course.nodes.en.validation.PrerequisiteRule;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.springframework.stereotype.Component;

/**
 * Validates enrollment time restrictions (enrollment start/end dates).
 * Checks if enrollment is within the configured period and provides
 * warnings when the period is about to close.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Component
public class TimeRestrictionRule implements PrerequisiteRule {
    
    @Override
    public EnrollmentEligibility validate(Identity identity, BusinessGroup group, 
                                         ENCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
        ModuleConfiguration config = courseNode.getModuleConfiguration();
        Date now = new Date();
        
        // Check enrollment start date
        Date enrollmentBegin = (Date) config.get(ENCourseNode.CONF_ENROLLMENT_BEGIN);
        if (enrollmentBegin != null && now.before(enrollmentBegin)) {
            String formattedDate = Formatter.getInstance(userCourseEnv.getIdentityEnvironment().getLocale())
                    .formatDateAndTime(enrollmentBegin);
            return EnrollmentEligibility.notEligible(
                    String.format("Enrollment opens on %s", formattedDate));
        }
        // Check enrollment end date
        Date enrollmentEnd = (Date) config.get(ENCourseNode.CONF_ENROLLMENT_END);
        if (enrollmentEnd != null && now.after(enrollmentEnd)) {
            String formattedDate = Formatter.getInstance(userCourseEnv.getIdentityEnvironment().getLocale())
                    .formatDateAndTime(enrollmentEnd);
            return EnrollmentEligibility.notEligible(
                    String.format("Enrollment closed on %s", formattedDate));
        }
        // Add warning if enrollment closes soon (within 24 hours)
        if (enrollmentEnd != null) {
            long timeUntilEnd = enrollmentEnd.getTime() - now.getTime();
            long hoursUntilEnd = timeUntilEnd / (1000 * 60 * 60);
            
            if (hoursUntilEnd > 0 && hoursUntilEnd <= 24) {
                String formattedDate = Formatter.getInstance(userCourseEnv.getIdentityEnvironment().getLocale())
                        .formatDateAndTime(enrollmentEnd);
                return EnrollmentEligibility.builder()
                        .eligible(true)
                        .addWarning(EnrollmentEligibility.ValidationMessage.warning(
                                String.format("Enrollment closes soon on %s", formattedDate)))
                        .build();
            }
        }
        return EnrollmentEligibility.eligible();
    }
    
    @Override
    public int getPriority() {
        return 5; // Check time restrictions first
    }
    
    @Override
    public String getRuleIdentifier() {
        return "time-restriction";
    }
}
