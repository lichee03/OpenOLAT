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
package org.olat.course.nodes.en.validation;

import org.olat.core.id.Identity;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;

/**
 * Interface for enrollment prerequisite validation rules.
 * Each rule validates one aspect of enrollment eligibility.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
public interface PrerequisiteRule {
    
    /**
     * Validate whether the user meets the prerequisite for enrollment.
     * 
     * @param identity The identity attempting to enroll
     * @param group The business group to enroll in
     * @param courseNode The enrollment course node
     * @param userCourseEnv The user's course environment
     * @return EnrollmentEligibility result with validation messages
     */
    EnrollmentEligibility validate(Identity identity, BusinessGroup group, 
                                   ENCourseNode courseNode, UserCourseEnvironment userCourseEnv);
    
    /**
     * Get the priority of this rule. Lower numbers are executed first.
     * @return priority value
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Check if this rule is enabled for the given course node.
     * @param courseNode The enrollment course node
     * @return true if enabled, false otherwise
     */
    default boolean isEnabled(ENCourseNode courseNode) {
        return true;
    }
    
    /**
     * Get a unique identifier for this rule.
     * @return rule identifier
     */
    String getRuleIdentifier();
}
