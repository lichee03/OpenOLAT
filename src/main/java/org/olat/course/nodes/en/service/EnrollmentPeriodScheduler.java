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
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled task that monitors enrollment periods and triggers auto-actions
 * when periods start or end.
 * 
 * Runs every 5 minutes to check for enrollment period transitions.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Service
public class EnrollmentPeriodScheduler {
    
    private static final Logger log = Tracing.createLoggerFor(EnrollmentPeriodScheduler.class);
    
    // Check every 5 minutes (300000 milliseconds)
    private static final long CHECK_INTERVAL = 5 * 60 * 1000;
    
    // Grace period for detecting period transitions (10 minutes)
    private static final long GRACE_PERIOD = 10 * 60 * 1000;
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    private RepositoryEntryDAO repositoryEntryDAO;
    
    @Autowired
    private EnrollmentPeriodAutoActionService autoActionService;
    
    /**
     * Scheduled task to check enrollment periods.
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = CHECK_INTERVAL, initialDelay = 60000) // Start after 1 minute
    public void checkEnrollmentPeriods() {
        log.debug("Starting enrollment period check");
        
        try {
            Date now = new Date();
            Date gracePeriodStart = new Date(now.getTime() - GRACE_PERIOD);
            
            // Get all repository entries in batches
            int batchSize = 100;
            int firstResult = 0;
            List<RepositoryEntry> batch;
            
            int periodsChecked = 0;
            int actionsTriggered = 0;
            
            do {
                batch = repositoryEntryDAO.getAllRepositoryEntries(firstResult, batchSize);
                
                for (RepositoryEntry courseEntry : batch) {
                    // Filter for courses only
                    if (!"CourseModule".equals(courseEntry.getOlatResource().getResourceableTypeName())) {
                        continue;
                    }
                    
                    try {
                        // Load course
                        ICourse course = CourseFactory.loadCourse(courseEntry);
                        if (course == null) continue;
                        
                        // Find all enrollment nodes
                        CourseNode rootNode = course.getRunStructure().getRootNode();
                        List<ENCourseNode> enrollmentNodes = findEnrollmentNodes(rootNode);
                        
                        for (ENCourseNode enrollNode : enrollmentNodes) {
                            periodsChecked++;
                            
                            ModuleConfiguration config = enrollNode.getModuleConfiguration();
                            
                            // Check if enrollment period starts
                            Date enrollmentBegin = (Date) config.get(ENCourseNode.CONF_ENROLLMENT_BEGIN);
                            if (enrollmentBegin != null && 
                                enrollmentBegin.after(gracePeriodStart) && 
                                enrollmentBegin.before(now)) {
                                
                                log.info("Enrollment period starting for course={}, node={}", 
                                        courseEntry.getKey(), enrollNode.getIdent());
                                autoActionService.executeStartActions(courseEntry, enrollNode, config);
                                actionsTriggered++;
                            }
                            
                            // Check if enrollment period ends
                            Date enrollmentEnd = (Date) config.get(ENCourseNode.CONF_ENROLLMENT_END);
                            if (enrollmentEnd != null && 
                                enrollmentEnd.after(gracePeriodStart) && 
                                enrollmentEnd.before(now)) {
                                
                                log.info("Enrollment period ending for course={}, node={}", 
                                        courseEntry.getKey(), enrollNode.getIdent());
                                autoActionService.executeEndActions(courseEntry, enrollNode, config);
                                actionsTriggered++;
                            }
                        }
                        
                    } catch (Exception e) {
                        log.error("Error checking enrollment periods for course={}", courseEntry.getKey(), e);
                    }
                }
                
                firstResult += batchSize;
                
            } while (batch.size() == batchSize);
            
            log.debug("Enrollment period check completed: checked={}, actions={}", periodsChecked, actionsTriggered);
            
        } catch (Exception e) {
            log.error("Error in enrollment period scheduler", e);
        }
    }
    
    /**
     * Recursively find all enrollment nodes in the course structure.
     */
    private List<ENCourseNode> findEnrollmentNodes(CourseNode node) {
        List<ENCourseNode> enrollmentNodes = new java.util.ArrayList<>();
        
        if (node instanceof ENCourseNode) {
            enrollmentNodes.add((ENCourseNode) node);
        }
        
        // Recursively check children
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            CourseNode child = (CourseNode) node.getChildAt(i);
            enrollmentNodes.addAll(findEnrollmentNodes(child));
        }
        
        return enrollmentNodes;
    }
}
