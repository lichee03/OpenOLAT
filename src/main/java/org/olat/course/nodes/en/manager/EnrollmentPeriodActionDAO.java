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
package org.olat.course.nodes.en.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.course.nodes.en.model.EnrollmentPeriodAction;
import org.olat.course.nodes.en.model.EnrollmentPeriodAction.ActionType;
import org.olat.course.nodes.en.model.EnrollmentPeriodAction.TriggerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * DAO for managing enrollment period action tracking.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Service
public class EnrollmentPeriodActionDAO {
    
    @Autowired
    private DB dbInstance;
    
    /**
     * Create a new enrollment period action record.
     */
    public EnrollmentPeriodAction createAction(Long courseId, String courseNodeId, 
                                               ActionType actionType, TriggerType triggerType,
                                               boolean success, String message, Integer affectedUsers) {
        EnrollmentPeriodAction action = new EnrollmentPeriodAction();
        action.setCreationDate(new Date());
        action.setCourseId(courseId);
        action.setCourseNodeId(courseNodeId);
        action.setActionType(actionType);
        action.setTriggerType(triggerType);
        action.setExecutedDate(new Date());
        action.setSuccess(success);
        action.setMessage(message);
        action.setAffectedUsers(affectedUsers);
        
        dbInstance.getCurrentEntityManager().persist(action);
        return action;
    }
    
    /**
     * Check if an action has already been executed for a specific period trigger.
     */
    public boolean hasActionBeenExecuted(Long courseId, String courseNodeId, 
                                         ActionType actionType, TriggerType triggerType,
                                         Date sinceDate) {
        String query = "select count(a) from EnrollmentPeriodAction a " +
                      "where a.courseId = :courseId " +
                      "and a.courseNodeId = :nodeId " +
                      "and a.actionType = :actionType " +
                      "and a.triggerType = :triggerType " +
                      "and a.success = true " +
                      "and a.executedDate >= :sinceDate";
        
        Long count = dbInstance.getCurrentEntityManager()
                .createQuery(query, Long.class)
                .setParameter("courseId", courseId)
                .setParameter("nodeId", courseNodeId)
                .setParameter("actionType", actionType)
                .setParameter("triggerType", triggerType)
                .setParameter("sinceDate", sinceDate)
                .getSingleResult();
        
        return count > 0;
    }
    
    /**
     * Get action history for a specific enrollment node.
     */
    public List<EnrollmentPeriodAction> getActionHistory(Long courseId, String courseNodeId, int maxResults) {
        String query = "select a from EnrollmentPeriodAction a " +
                      "where a.courseId = :courseId " +
                      "and a.courseNodeId = :nodeId " +
                      "order by a.executedDate desc";
        
        return dbInstance.getCurrentEntityManager()
                .createQuery(query, EnrollmentPeriodAction.class)
                .setParameter("courseId", courseId)
                .setParameter("nodeId", courseNodeId)
                .setMaxResults(maxResults)
                .getResultList();
    }
    
    /**
     * Delete old action records (for cleanup).
     */
    public int deleteActionsBefore(Date beforeDate) {
        String query = "delete from EnrollmentPeriodAction a where a.executedDate < :beforeDate";
        
        return dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("beforeDate", beforeDate)
                .executeUpdate();
    }
}
