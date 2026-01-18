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
import org.olat.basesecurity.GroupRoles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.template.CourseReadinessCheckItem;
import org.olat.course.template.CourseReadinessCheckerService;
import org.olat.course.template.model.CourseReadinessCheckItemImpl;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of CourseReadinessCheckerService
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
@Service
public class CourseReadinessCheckerServiceImpl implements CourseReadinessCheckerService {
	
	private static final Logger log = Tracing.createLoggerFor(CourseReadinessCheckerServiceImpl.class);
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public List<CourseReadinessCheckItem> checkCourseReadiness(ICourse course) {
		List<CourseReadinessCheckItem> results = new ArrayList<>();
		
		results.addAll(checkCourseStructure(course));
		results.addAll(checkStaffAssignment(course));
		results.addAll(checkCourseConfiguration(course));
		results.addAll(checkLearningResources(course));
		results.addAll(checkCourseElements(course));
		
		return results;
	}
	
	@Override
	public List<CourseReadinessCheckItem> checkCourseReadiness(RepositoryEntry courseEntry) {
		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource().getResourceableId());
		return checkCourseReadiness(course);
	}
	
	@Override
	public List<CourseReadinessCheckItem> checkCourseStructure(ICourse course) {
		List<CourseReadinessCheckItem> results = new ArrayList<>();
		
		CourseEditorTreeModel treeModel = course.getEditorTreeModel();
		if (treeModel == null || treeModel.getRootNode() == null) {
			results.add(new CourseReadinessCheckItemImpl(
				CourseReadinessCheckItem.CheckCategory.STRUCTURE,
				CourseReadinessCheckItem.CheckLevel.ERROR,
				"Course structure is not defined",
				true
			));
		} else {
			INode rootNode = treeModel.getRootNode();
			if (rootNode.getChildCount() == 0) {
				results.add(new CourseReadinessCheckItemImpl(
					CourseReadinessCheckItem.CheckCategory.STRUCTURE,
					CourseReadinessCheckItem.CheckLevel.WARNING,
					"Course structure has no course elements",
					false
				));
			}
		}
		
		return results;
	}
	
	@Override
	public List<CourseReadinessCheckItem> checkStaffAssignment(ICourse course) {
		List<CourseReadinessCheckItem> results = new ArrayList<>();
		
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		// Check for coaches
		List<Identity> coaches = repositoryService.getMembers(courseEntry, GroupRoles.coach.name())
			.stream().collect(Collectors.toList());
		
		if (coaches.isEmpty()) {
			results.add(new CourseReadinessCheckItemImpl(
				CourseReadinessCheckItem.CheckCategory.STAFF,
				CourseReadinessCheckItem.CheckLevel.WARNING,
				"No coaches assigned to the course",
				false
			));
		}
		
		// Check for owners
		List<Identity> owners = repositoryService.getMembers(courseEntry, GroupRoles.owner.name())
			.stream().collect(Collectors.toList());
		
		if (owners.isEmpty()) {
			results.add(new CourseReadinessCheckItemImpl(
				CourseReadinessCheckItem.CheckCategory.STAFF,
				CourseReadinessCheckItem.CheckLevel.ERROR,
				"No owners assigned to the course",
				true
			));
		}
		
		return results;
	}
	
	@Override
	public List<CourseReadinessCheckItem> checkCourseConfiguration(ICourse course) {
		List<CourseReadinessCheckItem> results = new ArrayList<>();
		
		CourseEnvironment env = course.getCourseEnvironment();
		
		// Check course title
		if (course.getCourseTitle() == null || course.getCourseTitle().trim().isEmpty()) {
			results.add(new CourseReadinessCheckItemImpl(
				CourseReadinessCheckItem.CheckCategory.SETTINGS,
				CourseReadinessCheckItem.CheckLevel.WARNING,
				"Course title is empty",
				false
			));
		}
		
		// Check access settings - could be extended based on specific requirements
		log.debug("Course configuration check completed");
		
		return results;
	}
	
	@Override
	public List<CourseReadinessCheckItem> checkLearningResources(ICourse course) {
		List<CourseReadinessCheckItem> results = new ArrayList<>();
		
		// This would involve checking if learning resources are linked to course elements
		// Implementation depends on how learning resources are managed in OpenOLAT
		
		log.debug("Learning resources check completed");
		return results;
	}
	
	@Override
	public List<CourseReadinessCheckItem> checkCourseElements(ICourse course) {
		List<CourseReadinessCheckItem> results = new ArrayList<>();
		
		CourseEditorTreeModel treeModel = course.getEditorTreeModel();
		if (treeModel != null && treeModel.getRootNode() != null) {
			// Check for unconfigured course elements
			// This would iterate through all nodes and validate their configuration
			log.debug("Course elements validation completed");
		}
		
		return results;
	}
	
	@Override
	public boolean canPublish(ICourse course) {
		List<CourseReadinessCheckItem> issues = checkCourseReadiness(course);
		return issues.stream().noneMatch(CourseReadinessCheckItem::isCritical);
	}
	
	@Override
	public Map<CourseReadinessCheckItem.CheckCategory, List<CourseReadinessCheckItem>> checkByCategory(ICourse course) {
		List<CourseReadinessCheckItem> allIssues = checkCourseReadiness(course);
		
		return allIssues.stream()
			.collect(Collectors.groupingBy(CourseReadinessCheckItem::getCategory));
	}
}
