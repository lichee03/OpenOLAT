/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.member;

import java.util.HashMap;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;

/**
 * Enhancement 3: Bulk Staff Assignment
 * 
 * Summary of staff assignments for a course
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class StaffAssignmentSummary {
    
    private final Long courseKey;
    private final String courseName;
    private final Map<GroupRoles, Integer> roleCountMap;
    private final int totalStaffCount;
    private final boolean hasOwners;
    private final boolean hasCoaches;
    private final boolean hasParticipants;
    
    private StaffAssignmentSummary(Builder builder) {
        this.courseKey = builder.courseKey;
        this.courseName = builder.courseName;
        this.roleCountMap = builder.roleCountMap;
        this.totalStaffCount = builder.totalStaffCount;
        this.hasOwners = builder.roleCountMap.getOrDefault(GroupRoles.owner, 0) > 0;
        this.hasCoaches = builder.roleCountMap.getOrDefault(GroupRoles.coach, 0) > 0;
        this.hasParticipants = builder.roleCountMap.getOrDefault(GroupRoles.participant, 0) > 0;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public Long getCourseKey() {
        return courseKey;
    }
    
    public String getCourseName() {
        return courseName;
    }
    
    public int getCountByRole(GroupRoles role) {
        return roleCountMap.getOrDefault(role, 0);
    }
    
    public int getOwnerCount() {
        return getCountByRole(GroupRoles.owner);
    }
    
    public int getCoachCount() {
        return getCountByRole(GroupRoles.coach);
    }
    
    public int getParticipantCount() {
        return getCountByRole(GroupRoles.participant);
    }
    
    public int getTotalStaffCount() {
        return totalStaffCount;
    }
    
    public boolean hasOwners() {
        return hasOwners;
    }
    
    public boolean hasCoaches() {
        return hasCoaches;
    }
    
    public boolean hasParticipants() {
        return hasParticipants;
    }
    
    public Map<GroupRoles, Integer> getRoleCountMap() {
        return new HashMap<>(roleCountMap);
    }
    
    @Override
    public String toString() {
        return "StaffAssignmentSummary{" +
                "courseKey=" + courseKey +
                ", owners=" + getOwnerCount() +
                ", coaches=" + getCoachCount() +
                ", participants=" + getParticipantCount() +
                ", total=" + totalStaffCount +
                '}';
    }
    
    public static class Builder {
        private Long courseKey;
        private String courseName;
        private Map<GroupRoles, Integer> roleCountMap = new HashMap<>();
        private int totalStaffCount = 0;
        
        public Builder courseKey(Long key) {
            this.courseKey = key;
            return this;
        }
        
        public Builder courseName(String name) {
            this.courseName = name;
            return this;
        }
        
        public Builder setRoleCount(GroupRoles role, int count) {
            this.roleCountMap.put(role, count);
            return this;
        }
        
        public Builder incrementRoleCount(GroupRoles role) {
            this.roleCountMap.merge(role, 1, Integer::sum);
            return this;
        }
        
        public Builder totalStaffCount(int count) {
            this.totalStaffCount = count;
            return this;
        }
        
        public StaffAssignmentSummary build() {
            if (totalStaffCount == 0) {
                totalStaffCount = roleCountMap.values().stream().mapToInt(Integer::intValue).sum();
            }
            return new StaffAssignmentSummary(this);
        }
    }
}
