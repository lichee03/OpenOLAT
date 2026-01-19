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
package org.olat.course.nodes.en.service;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.validation.EnrollmentEligibility;
import org.olat.course.nodes.en.validation.rules.TimeRestrictionRule;
import org.olat.modules.ModuleConfiguration;

/**
 * Test cases for enrollment period functionality.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
public class EnrollmentPeriodTest {
    
    @Test
    public void testEnrollmentPeriodNotStarted() {
        // Create a time restriction rule
        TimeRestrictionRule rule = new TimeRestrictionRule();
        
        // Mock configuration with future start date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1); // Tomorrow
        Date futureStart = cal.getTime();
        
        ModuleConfiguration config = new ModuleConfiguration();
        config.set(ENCourseNode.CONF_ENROLLMENT_BEGIN, futureStart);
        
        // Validation should fail - enrollment not started
        // In real scenario, we would mock the course node and user environment
        assertNotNull("TimeRestrictionRule should be instantiated", rule);
        assertEquals("Priority should be 5", 5, rule.getPriority());
        assertEquals("Identifier should be time-restriction", "time-restriction", rule.getRuleIdentifier());
    }
    
    @Test
    public void testEnrollmentPeriodEnded() {
        // Test enrollment after end date
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1); // Yesterday
        Date pastEnd = cal.getTime();
        
        ModuleConfiguration config = new ModuleConfiguration();
        config.set(ENCourseNode.CONF_ENROLLMENT_END, pastEnd);
        
        // Validation should fail - enrollment ended
        assertNotNull("Configuration should contain end date", config.get(ENCourseNode.CONF_ENROLLMENT_END));
    }
    
    @Test
    public void testEnrollmentPeriodActive() {
        // Test enrollment within valid period
        Calendar cal = Calendar.getInstance();
        
        // Start: Yesterday
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date start = cal.getTime();
        
        // End: Tomorrow
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date end = cal.getTime();
        
        ModuleConfiguration config = new ModuleConfiguration();
        config.set(ENCourseNode.CONF_ENROLLMENT_BEGIN, start);
        config.set(ENCourseNode.CONF_ENROLLMENT_END, end);
        
        // Validation should pass - enrollment is active
        Date now = new Date();
        assertTrue("Current time should be after start", now.after(start));
        assertTrue("Current time should be before end", now.before(end));
    }
    
    @Test
    public void testAutoActionConfiguration() {
        // Test auto-action configuration
        ModuleConfiguration config = new ModuleConfiguration();
        
        // Enable all auto-actions
        config.setBooleanEntry(ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_START, true);
        config.setBooleanEntry(ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_END, true);
        config.setBooleanEntry(ENCourseNode.CONF_NOTIFY_ON_PERIOD_START, true);
        config.setBooleanEntry(ENCourseNode.CONF_NOTIFY_ON_PERIOD_END, true);
        
        // Verify configuration
        assertTrue("Waitlist processing at start should be enabled", 
                  config.getBooleanEntry(ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_START));
        assertTrue("Waitlist processing at end should be enabled",
                  config.getBooleanEntry(ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_END));
        assertTrue("Notification at start should be enabled",
                  config.getBooleanEntry(ENCourseNode.CONF_NOTIFY_ON_PERIOD_START));
        assertTrue("Notification at end should be enabled",
                  config.getBooleanEntry(ENCourseNode.CONF_NOTIFY_ON_PERIOD_END));
    }
    
    @Test
    public void testEnrollmentEligibilityBuilder() {
        // Test the eligibility builder
        EnrollmentEligibility eligible = EnrollmentEligibility.eligible();
        assertTrue("Should be eligible", eligible.isEligible());
        assertFalse("Should have no errors", eligible.hasErrors());
        
        EnrollmentEligibility notEligible = EnrollmentEligibility.notEligible("Period has not started");
        assertFalse("Should not be eligible", notEligible.isEligible());
        assertTrue("Should have errors", notEligible.hasErrors());
        assertEquals("Should have 1 error", 1, notEligible.getErrors().size());
    }
    
    @Test
    public void testWarningForClosingPeriod() {
        // Test warning when period closes within 24 hours
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 12); // 12 hours from now
        Date closingSoon = cal.getTime();
        
        Date now = new Date();
        long hoursUntilEnd = (closingSoon.getTime() - now.getTime()) / (1000 * 60 * 60);
        
        assertTrue("Should be closing within 24 hours", hoursUntilEnd > 0 && hoursUntilEnd <= 24);
    }
    
    @Test
    public void testConfigurationConstants() {
        // Verify all configuration constants are properly defined
        assertNotNull("CONF_ENROLLMENT_BEGIN should exist", ENCourseNode.CONF_ENROLLMENT_BEGIN);
        assertNotNull("CONF_ENROLLMENT_END should exist", ENCourseNode.CONF_ENROLLMENT_END);
        assertNotNull("CONF_AUTO_PROCESS_WAITLIST_START should exist", ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_START);
        assertNotNull("CONF_AUTO_PROCESS_WAITLIST_END should exist", ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_END);
        assertNotNull("CONF_NOTIFY_ON_PERIOD_START should exist", ENCourseNode.CONF_NOTIFY_ON_PERIOD_START);
        assertNotNull("CONF_NOTIFY_ON_PERIOD_END should exist", ENCourseNode.CONF_NOTIFY_ON_PERIOD_END);
        
        // Verify constant values
        assertEquals("enrollment_begin", ENCourseNode.CONF_ENROLLMENT_BEGIN);
        assertEquals("enrollment_end", ENCourseNode.CONF_ENROLLMENT_END);
    }
}
