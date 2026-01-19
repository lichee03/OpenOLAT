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
package org.olat.course.nodes.en.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;

/**
 * Tracks executed auto-actions for enrollment periods to prevent duplicate execution.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
@Entity
@Table(name = "o_en_period_action")
public class EnrollmentPeriodAction implements Persistable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, insertable = true, updatable = false)
    private Long key;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creationdate", nullable = false, insertable = true, updatable = false)
    private Date creationDate;
    
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    
    @Column(name = "course_node_id", nullable = false, length = 64)
    private String courseNodeId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 32)
    private ActionType actionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 32)
    private TriggerType triggerType;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "executed_date", nullable = false)
    private Date executedDate;
    
    @Column(name = "success", nullable = false)
    private boolean success;
    
    @Column(name = "message", length = 2048)
    private String message;
    
    @Column(name = "affected_users")
    private Integer affectedUsers;
    
    public EnrollmentPeriodAction() {
        // For Hibernate
    }
    
    @Override
    public Long getKey() {
        return key;
    }
    
    public void setKey(Long key) {
        this.key = key;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
    
    public Long getCourseId() {
        return courseId;
    }
    
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    
    public String getCourseNodeId() {
        return courseNodeId;
    }
    
    public void setCourseNodeId(String courseNodeId) {
        this.courseNodeId = courseNodeId;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public TriggerType getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }
    
    public Date getExecutedDate() {
        return executedDate;
    }
    
    public void setExecutedDate(Date executedDate) {
        this.executedDate = executedDate;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getAffectedUsers() {
        return affectedUsers;
    }
    
    public void setAffectedUsers(Integer affectedUsers) {
        this.affectedUsers = affectedUsers;
    }
    
    @Override
    public int hashCode() {
        return key == null ? 0 : key.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EnrollmentPeriodAction other = (EnrollmentPeriodAction) obj;
        return key != null && key.equals(other.key);
    }
    
    @Override
    public boolean equalsByPersistableKey(Persistable persistable) {
        return equals(persistable);
    }
    
    public enum ActionType {
        WAITLIST_PROCESSING,
        NOTIFICATION,
        PERIOD_START,
        PERIOD_END
    }
    
    public enum TriggerType {
        PERIOD_START,
        PERIOD_END,
        MANUAL
    }
}
