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
package org.olat.course.template;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;

/**
 * Service for bulk staff assignment in courses
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public interface BulkStaffAssignmentService {
	
	/**
	 * Assign multiple coaches to a course
	 * @param courseEntry
	 * @param coaches
	 * @param executor
	 */
	void assignCoaches(RepositoryEntry courseEntry, Collection<Identity> coaches, Identity executor);
	
	/**
	 * Assign multiple participants to a course
	 * @param courseEntry
	 * @param participants
	 * @param executor
	 */
	void assignParticipants(RepositoryEntry courseEntry, Collection<Identity> participants, Identity executor);
	
	/**
	 * Assign multiple owners to a course
	 * @param courseEntry
	 * @param owners
	 * @param executor
	 */
	void assignOwners(RepositoryEntry courseEntry, Collection<Identity> owners, Identity executor);
	
	/**
	 * Remove multiple coaches from a course
	 * @param courseEntry
	 * @param coaches
	 * @param executor
	 */
	void removeCoaches(RepositoryEntry courseEntry, Collection<Identity> coaches, Identity executor);
	
	/**
	 * Remove multiple participants from a course
	 * @param courseEntry
	 * @param participants
	 * @param executor
	 */
	void removeParticipants(RepositoryEntry courseEntry, Collection<Identity> participants, Identity executor);
	
	/**
	 * Remove multiple owners from a course
	 * @param courseEntry
	 * @param owners
	 * @param executor
	 */
	void removeOwners(RepositoryEntry courseEntry, Collection<Identity> owners, Identity executor);
	
	/**
	 * Assign staff to multiple courses
	 * @param courses
	 * @param coaches
	 * @param participants
	 * @param owners
	 * @param executor
	 */
	void assignStaffToMultipleCourses(Collection<RepositoryEntry> courses, Collection<Identity> coaches,
		Collection<Identity> participants, Collection<Identity> owners, Identity executor);
	
	/**
	 * Get currently assigned coaches
	 * @param courseEntry
	 * @return
	 */
	List<Identity> getAssignedCoaches(RepositoryEntry courseEntry);
	
	/**
	 * Get currently assigned participants
	 * @param courseEntry
	 * @return
	 */
	List<Identity> getAssignedParticipants(RepositoryEntry courseEntry);
	
	/**
	 * Get currently assigned owners
	 * @param courseEntry
	 * @return
	 */
	List<Identity> getAssignedOwners(RepositoryEntry courseEntry);
}
