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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.EnrollmentManager;
import org.olat.course.nodes.en.validation.EnrollmentEligibility;
import org.olat.course.nodes.en.validation.PrerequisiteRule;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates that the user hasn't exceeded the allowed number of enrollments
 * for this enrollment node.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Component
public class MultipleEnrollmentRule implements PrerequisiteRule {
    
    @Autowired
    private EnrollmentManager enrollmentManager;
    
    @Override
    public EnrollmentEligibility validate(Identity identity, BusinessGroup group, 
                                         ENCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
        
        ModuleConfiguration config = courseNode.getModuleConfiguration();
        
        // Get the allowed number of enrollments
        int allowedEnrollments = config.getIntegerSafe(
                ENCourseNode.CONFIG_ALLOW_MULTIPLE_ENROLL_COUNT, 1);
        
        // Get configured groups and areas for this enrollment node
        List<Long> groupKeys = config.getList(ENCourseNode.CONFIG_GROUP_IDS, Long.class);
        List<Long> areaKeys = config.getList(ENCourseNode.CONFIG_AREA_IDS, Long.class);
        
        if (groupKeys == null || groupKeys.isEmpty()) {
            groupKeys = List.of();
        }
        if (areaKeys == null || areaKeys.isEmpty()) {
            areaKeys = List.of();
        }
        
        // Count existing enrollments
        int currentEnrollments = enrollmentManager.getBusinessGroupsWhereEnrolled(
                identity, groupKeys, areaKeys, 
                userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry()).size();
        
        // Check if user has reached the limit
        if (currentEnrollments >= allowedEnrollments) {
            if (allowedEnrollments == 1) {
                return EnrollmentEligibility.notEligible(
                        "You are already enrolled in a group for this course element");
            } else {
                return EnrollmentEligibility.notEligible(
                        String.format("You have reached the maximum of %d enrollments for this course element", 
                                allowedEnrollments));
            }
        }
        
        // Add info about remaining enrollments if multiple are allowed
        if (allowedEnrollments > 1) {
            int remaining = allowedEnrollments - currentEnrollments;
            return EnrollmentEligibility.builder()
                    .eligible(true)
                    .addInfo(EnrollmentEligibility.ValidationMessage.info(
                            String.format("You can enroll in %d more group(s) for this course element", remaining)))
                    .build();
        }
        
        return EnrollmentEligibility.eligible();
    }
    
    @Override
    public int getPriority() {
        return 20;
    }
    
    @Override
    public String getRuleIdentifier() {
        return "multiple-enrollment";
    }
}
