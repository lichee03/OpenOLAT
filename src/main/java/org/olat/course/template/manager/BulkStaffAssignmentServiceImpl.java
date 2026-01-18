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
package org.olat.course.template.manager;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.template.BulkStaffAssignmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of BulkStaffAssignmentService
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
@Service
public class BulkStaffAssignmentServiceImpl implements BulkStaffAssignmentService {
	
	private static final Logger log = Tracing.createLoggerFor(BulkStaffAssignmentServiceImpl.class);
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private BaseSecurity baseSecurity;
	
	@Override
	public void assignCoaches(RepositoryEntry courseEntry, Collection<Identity> coaches, Identity executor) {
		log.info("Assigning " + coaches.size() + " coaches to course: " + courseEntry.getDisplayname());
		for (Identity coach : coaches) {
			repositoryService.addRole(coach, courseEntry, GroupRoles.coach.name());
		}
	}
	
	@Override
	public void assignParticipants(RepositoryEntry courseEntry, Collection<Identity> participants, Identity executor) {
		log.info("Assigning " + participants.size() + " participants to course: " + courseEntry.getDisplayname());
		for (Identity participant : participants) {
			repositoryService.addRole(participant, courseEntry, GroupRoles.participant.name());
		}
	}
	
	@Override
	public void assignOwners(RepositoryEntry courseEntry, Collection<Identity> owners, Identity executor) {
		log.info("Assigning " + owners.size() + " owners to course: " + courseEntry.getDisplayname());
		for (Identity owner : owners) {
			repositoryService.addRole(owner, courseEntry, GroupRoles.owner.name());
		}
	}
	
	@Override
	public void removeCoaches(RepositoryEntry courseEntry, Collection<Identity> coaches, Identity executor) {
		log.info("Removing " + coaches.size() + " coaches from course: " + courseEntry.getDisplayname());
		for (Identity coach : coaches) {
			repositoryService.removeRole(coach, courseEntry, GroupRoles.coach.name());
		}
	}
	
	@Override
	public void removeParticipants(RepositoryEntry courseEntry, Collection<Identity> participants, Identity executor) {
		log.info("Removing " + participants.size() + " participants from course: " + courseEntry.getDisplayname());
		for (Identity participant : participants) {
			repositoryService.removeRole(participant, courseEntry, GroupRoles.participant.name());
		}
	}
	
	@Override
	public void removeOwners(RepositoryEntry courseEntry, Collection<Identity> owners, Identity executor) {
		log.info("Removing " + owners.size() + " owners from course: " + courseEntry.getDisplayname());
		for (Identity owner : owners) {
			repositoryService.removeRole(owner, courseEntry, GroupRoles.owner.name());
		}
	}
	
	@Override
	public void assignStaffToMultipleCourses(Collection<RepositoryEntry> courses, Collection<Identity> coaches,
		Collection<Identity> participants, Collection<Identity> owners, Identity executor) {
		
		log.info("Assigning staff to " + courses.size() + " courses");
		
		for (RepositoryEntry course : courses) {
			if (!coaches.isEmpty()) {
				assignCoaches(course, coaches, executor);
			}
			if (!participants.isEmpty()) {
				assignParticipants(course, participants, executor);
			}
			if (!owners.isEmpty()) {
				assignOwners(course, owners, executor);
			}
		}
		
		log.info("Staff assignment completed for " + courses.size() + " courses");
	}
	
	@Override
	public List<Identity> getAssignedCoaches(RepositoryEntry courseEntry) {
		return repositoryService.getMembers(courseEntry, GroupRoles.coach.name())
			.stream()
			.collect(Collectors.toList());
	}
	
	@Override
	public List<Identity> getAssignedParticipants(RepositoryEntry courseEntry) {
		return repositoryService.getMembers(courseEntry, GroupRoles.participant.name())
			.stream()
			.collect(Collectors.toList());
	}
	
	@Override
	public List<Identity> getAssignedOwners(RepositoryEntry courseEntry) {
		return repositoryService.getMembers(courseEntry, GroupRoles.owner.name())
			.stream()
			.collect(Collectors.toList());
	}
}
