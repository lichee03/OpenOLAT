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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.en.EnrollmentManager;
import org.olat.course.nodes.en.manager.EnrollmentPeriodActionDAO;
import org.olat.course.nodes.en.model.EnrollmentPeriodAction.ActionType;
import org.olat.course.nodes.en.model.EnrollmentPeriodAction.TriggerType;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for executing auto-actions during enrollment periods.
 * Handles waitlist processing, notifications, and period transitions.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Service
public class EnrollmentPeriodAutoActionService {
    
    private static final Logger log = Tracing.createLoggerFor(EnrollmentPeriodAutoActionService.class);
    
    @Autowired
    private EnrollmentPeriodActionDAO actionDAO;
    
    @Autowired
    private EnrollmentManager enrollmentManager;
    
    @Autowired
    private BusinessGroupService businessGroupService;
    
    @Autowired
    private MailManager mailManager;
    
    /**
     * Execute auto-actions when enrollment period starts.
     */
    public void executeStartActions(RepositoryEntry courseEntry, ENCourseNode courseNode, ModuleConfiguration config) {
        String nodeId = courseNode.getIdent();
        Long courseId = courseEntry.getKey();
        
        log.info("Executing enrollment period START actions for course={}, node={}", courseId, nodeId);
        
        try {
            // Check if already executed (within last 6 hours to handle scheduler delays)
            Date sixHoursAgo = new Date(System.currentTimeMillis() - (6 * 60 * 60 * 1000));
            if (actionDAO.hasActionBeenExecuted(courseId, nodeId, ActionType.PERIOD_START, TriggerType.PERIOD_START, sixHoursAgo)) {
                log.debug("Start actions already executed recently for course={}, node={}", courseId, nodeId);
                return;
            }
            
            int affectedUsers = 0;
            
            // Auto-process waitlist if enabled
            Boolean autoProcessStart = config.getBooleanEntry(ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_START);
            if (autoProcessStart != null && autoProcessStart) {
                affectedUsers += processWaitlistForNode(courseEntry, courseNode, config);
                log.info("Processed waitlist at period start: {} users moved", affectedUsers);
            }
            
            // Send notifications if enabled
            Boolean notifyStart = config.getBooleanEntry(ENCourseNode.CONF_NOTIFY_ON_PERIOD_START);
            if (notifyStart != null && notifyStart) {
                int notified = sendPeriodStartNotifications(courseEntry, courseNode, config);
                log.info("Sent period start notifications to {} users", notified);
            }
            
            // Record action
            actionDAO.createAction(courseId, nodeId, ActionType.PERIOD_START, TriggerType.PERIOD_START, 
                                  true, "Period started successfully", affectedUsers);
            
        } catch (Exception e) {
            log.error("Error executing start actions for course={}, node={}", courseId, nodeId, e);
            actionDAO.createAction(courseId, nodeId, ActionType.PERIOD_START, TriggerType.PERIOD_START,
                                  false, "Error: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Execute auto-actions when enrollment period ends.
     */
    public void executeEndActions(RepositoryEntry courseEntry, ENCourseNode courseNode, ModuleConfiguration config) {
        String nodeId = courseNode.getIdent();
        Long courseId = courseEntry.getKey();
        
        log.info("Executing enrollment period END actions for course={}, node={}", courseId, nodeId);
        
        try {
            // Check if already executed
            Date sixHoursAgo = new Date(System.currentTimeMillis() - (6 * 60 * 60 * 1000));
            if (actionDAO.hasActionBeenExecuted(courseId, nodeId, ActionType.PERIOD_END, TriggerType.PERIOD_END, sixHoursAgo)) {
                log.debug("End actions already executed recently for course={}, node={}", courseId, nodeId);
                return;
            }
            
            int affectedUsers = 0;
            
            // Auto-process waitlist if enabled
            Boolean autoProcessEnd = config.getBooleanEntry(ENCourseNode.CONF_AUTO_PROCESS_WAITLIST_END);
            if (autoProcessEnd != null && autoProcessEnd) {
                affectedUsers += processWaitlistForNode(courseEntry, courseNode, config);
                log.info("Processed waitlist at period end: {} users moved", affectedUsers);
            }
            
            // Send notifications if enabled
            Boolean notifyEnd = config.getBooleanEntry(ENCourseNode.CONF_NOTIFY_ON_PERIOD_END);
            if (notifyEnd != null && notifyEnd) {
                int notified = sendPeriodEndNotifications(courseEntry, courseNode, config);
                log.info("Sent period end notifications to {} users", notified);
            }
            
            // Record action
            actionDAO.createAction(courseId, nodeId, ActionType.PERIOD_END, TriggerType.PERIOD_END,
                                  true, "Period ended successfully", affectedUsers);
            
        } catch (Exception e) {
            log.error("Error executing end actions for course={}, node={}", courseId, nodeId, e);
            actionDAO.createAction(courseId, nodeId, ActionType.PERIOD_END, TriggerType.PERIOD_END,
                                  false, "Error: " + e.getMessage(), 0);
        }
    }
    
    /**
     * Process waitlist for all groups in the enrollment node.
     */
    private int processWaitlistForNode(RepositoryEntry courseEntry, ENCourseNode courseNode, ModuleConfiguration config) {
        int totalProcessed = 0;
        
        // Get all groups configured for this enrollment node
        @SuppressWarnings("unchecked")
        List<Long> groupKeys = (List<Long>) config.get(ENCourseNode.CONFIG_GROUP_IDS);
        
        if (groupKeys != null && !groupKeys.isEmpty()) {
            for (Long groupKey : groupKeys) {
                BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
                if (group != null) {
                    // Process waitlist for this group
                    List<Identity> waitingList = businessGroupService.getMembers(group, GroupRoles.waiting.name());
                    
                    for (Identity identity : waitingList) {
                        // Try to move from waitlist to participants
                        boolean moved = enrollmentManager.moveFromWaitingListToParticipant(
                                identity, group, courseEntry, courseNode);
                        if (moved) {
                            totalProcessed++;
                            log.debug("Moved user {} from waitlist to participants in group {}", 
                                    identity.getKey(), group.getName());
                        }
                    }
                }
            }
        }
        
        return totalProcessed;
    }
    
    /**
     * Send notifications when enrollment period starts.
     */
    private int sendPeriodStartNotifications(RepositoryEntry courseEntry, ENCourseNode courseNode, ModuleConfiguration config) {
        int notificationsSent = 0;
        
        // Get all potential participants (course participants)
        // Implementation depends on notification strategy
        // For now, this is a placeholder
        
        log.debug("Period start notifications - implementation pending");
        
        return notificationsSent;
    }
    
    /**
     * Send notifications when enrollment period ends.
     */
    private int sendPeriodEndNotifications(RepositoryEntry courseEntry, ENCourseNode courseNode, ModuleConfiguration config) {
        int notificationsSent = 0;
        
        // Get all enrolled users
        // Send notification about period closure
        // Implementation pending
        
        log.debug("Period end notifications - implementation pending");
        
        return notificationsSent;
    }
}
