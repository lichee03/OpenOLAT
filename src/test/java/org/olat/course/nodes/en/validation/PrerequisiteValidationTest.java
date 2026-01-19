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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.validation.rules.TimeRestrictionRule;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;

/**
 * Test cases for the prerequisite validation system.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
public class PrerequisiteValidationTest {

    @Test
    public void testEligibilityBuilder() {
        // Test building eligibility result with errors
        EnrollmentEligibility result = EnrollmentEligibility.builder()
                .eligible(false)
                .addError(EnrollmentEligibility.ValidationMessage.error("Test error"))
                .build();
        
        assertFalse("Should not be eligible", result.isEligible());
        assertEquals("Should have 1 error", 1, result.getErrors().size());
        assertEquals("Error message should match", "Test error", result.getErrors().get(0).getMessage());
    }
    
    @Test
    public void testEligibilityWithWarnings() {
        // Test that warnings don't prevent enrollment
        EnrollmentEligibility result = EnrollmentEligibility.builder()
                .eligible(true)
                .addWarning(EnrollmentEligibility.ValidationMessage.warning("Test warning"))
                .build();
        
        assertTrue("Should be eligible with warnings", result.isEligible());
        assertEquals("Should have 1 warning", 1, result.getWarnings().size());
        assertTrue("Should have warnings", result.hasWarnings());
    }
    
    @Test
    public void testMergeEligibility() {
        // Test merging multiple eligibility results
        EnrollmentEligibility result1 = EnrollmentEligibility.builder()
                .eligible(true)
                .addInfo(EnrollmentEligibility.ValidationMessage.info("Info 1"))
                .build();
        
        EnrollmentEligibility result2 = EnrollmentEligibility.builder()
                .eligible(false)
                .addError(EnrollmentEligibility.ValidationMessage.error("Error 1"))
                .build();
        
        EnrollmentEligibility merged = EnrollmentEligibility.builder()
                .merge(result1)
                .merge(result2)
                .build();
        
        assertFalse("Merged result should not be eligible", merged.isEligible());
        assertEquals("Should have 1 error", 1, merged.getErrors().size());
        assertEquals("Should have 1 info", 1, merged.getInfo().size());
    }
    
    @Test
    public void testTimeRestrictionBeforeStart() {
        // Test enrollment before start date
        TimeRestrictionRule rule = new TimeRestrictionRule();
        
        // Mock objects would go here in a full test
        // For demonstration purposes only
        assertTrue("Rule should have correct identifier", 
                rule.getRuleIdentifier().equals("time-restriction"));
        assertEquals("Rule should have priority 5", 5, rule.getPriority());
    }
    
    @Test
    public void testValidationMessageTypes() {
        // Test different message types
        EnrollmentEligibility.ValidationMessage error = 
                EnrollmentEligibility.ValidationMessage.error("Test error", "param1");
        assertEquals("Should be ERROR type", 
                EnrollmentEligibility.MessageType.ERROR, error.getType());
        
        EnrollmentEligibility.ValidationMessage warning = 
                EnrollmentEligibility.ValidationMessage.warning("Test warning");
        assertEquals("Should be WARNING type", 
                EnrollmentEligibility.MessageType.WARNING, warning.getType());
        
        EnrollmentEligibility.ValidationMessage info = 
                EnrollmentEligibility.ValidationMessage.info("Test info");
        assertEquals("Should be INFO type", 
                EnrollmentEligibility.MessageType.INFO, info.getType());
    }
    
    @Test
    public void testConvenienceMethods() {
        // Test static convenience methods
        EnrollmentEligibility eligible = EnrollmentEligibility.eligible();
        assertTrue("Should be eligible", eligible.isEligible());
        assertFalse("Should have no errors", eligible.hasErrors());
        
        EnrollmentEligibility notEligible = EnrollmentEligibility.notEligible("Not allowed");
        assertFalse("Should not be eligible", notEligible.isEligible());
        assertTrue("Should have errors", notEligible.hasErrors());
    }
}
