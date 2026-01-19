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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for validating enrollment prerequisites.
 * Orchestrates multiple validation rules and provides a unified eligibility result.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Service
public class PrerequisiteValidationService {
    
    private static final Logger log = Tracing.createLoggerFor(PrerequisiteValidationService.class);
    
    @Autowired
    private List<PrerequisiteRule> rules;
    
    /**
     * Validate enrollment eligibility by executing all registered prerequisite rules.
     * 
     * @param identity The identity attempting to enroll
     * @param group The business group to enroll in
     * @param courseNode The enrollment course node
     * @param userCourseEnv The user's course environment
     * @return Aggregated enrollment eligibility result
     */
    public EnrollmentEligibility validateEligibility(Identity identity, BusinessGroup group, 
                                                      ENCourseNode courseNode, UserCourseEnvironment userCourseEnv) {
        log.debug("Validating enrollment eligibility for user={}, group={}, course={}", 
                identity.getKey(), group.getName(), courseNode.getShortTitle());
        
        EnrollmentEligibility.Builder builder = EnrollmentEligibility.builder();
        
        // Get enabled rules sorted by priority
        List<PrerequisiteRule> enabledRules = rules.stream()
                .filter(rule -> rule.isEnabled(courseNode))
                .sorted(Comparator.comparingInt(PrerequisiteRule::getPriority))
                .collect(Collectors.toList());
        
        log.debug("Executing {} prerequisite rules", enabledRules.size());
        
        // Execute each rule and merge results
        for (PrerequisiteRule rule : enabledRules) {
            try {
                log.debug("Executing rule: {}", rule.getRuleIdentifier());
                EnrollmentEligibility ruleResult = rule.validate(identity, group, courseNode, userCourseEnv);
                builder.merge(ruleResult);
                
                if (!ruleResult.isEligible()) {
                    log.debug("Rule {} failed: {}", rule.getRuleIdentifier(), 
                            ruleResult.getErrors().stream()
                                    .map(msg -> msg.getMessage())
                                    .collect(Collectors.joining(", ")));
                }
            } catch (Exception e) {
                log.error("Error executing prerequisite rule: {}", rule.getRuleIdentifier(), e);
                builder.addError(EnrollmentEligibility.ValidationMessage.error(
                        "error.rule.execution", 
                        "Error validating prerequisite: " + rule.getRuleIdentifier(),
                        new String[0]));
            }
        }
        
        EnrollmentEligibility result = builder.build();
        log.debug("Eligibility validation completed. Eligible={}, Errors={}, Warnings={}", 
                result.isEligible(), result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }
    
    /**
     * Get all registered prerequisite rules.
     * @return List of prerequisite rules
     */
    public List<PrerequisiteRule> getRegisteredRules() {
        return rules;
    }
}
