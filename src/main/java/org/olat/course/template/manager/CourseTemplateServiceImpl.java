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

import javax.persistence.EntityManager;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.template.CourseTemplate;
import org.olat.course.template.CourseTemplateService;
import org.olat.course.template.model.CourseTemplateImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of CourseTemplateService
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
@Service
public class CourseTemplateServiceImpl implements CourseTemplateService {
	
	private static final Logger log = Tracing.createLoggerFor(CourseTemplateServiceImpl.class);
	
	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Override
	public CourseTemplate createTemplate(RepositoryEntry sourceEntry, String name, String description, Identity identity) {
		CourseTemplateImpl template = new CourseTemplateImpl(name, description, sourceEntry, identity);
		entityManager.persist(template);
		entityManager.flush();
		return template;
	}
	
	@Override
	public CourseTemplate updateTemplate(CourseTemplate template, String name, String description) {
		if (template instanceof CourseTemplateImpl) {
			CourseTemplateImpl impl = (CourseTemplateImpl) template;
			impl.setName(name);
			impl.setDescription(description);
			entityManager.merge(impl);
			entityManager.flush();
			return impl;
		}
		return template;
	}
	
	@Override
	public CourseTemplate updateTemplateConfig(CourseTemplate template, 
		boolean includeStructure, boolean includeCourseElements, boolean includeLearningResources,
		boolean includeCourseSettings, boolean includeStaffAssignments) {
		if (template instanceof CourseTemplateImpl) {
			CourseTemplateImpl impl = (CourseTemplateImpl) template;
			impl.setIncludeStructure(includeStructure);
			impl.setIncludeCourseElements(includeCourseElements);
			impl.setIncludeLearningResources(includeLearningResources);
			impl.setIncludeCourseSettings(includeCourseSettings);
			impl.setIncludeStaffAssignments(includeStaffAssignments);
			entityManager.merge(impl);
			entityManager.flush();
			return impl;
		}
		return template;
	}
	
	@Override
	public CourseTemplate getTemplate(Long templateKey) {
		return entityManager.find(CourseTemplateImpl.class, templateKey);
	}
	
	@Override
	public List<CourseTemplate> getActiveTemplates() {
		String query = "SELECT ct FROM coursetemplate ct WHERE ct.active = true ORDER BY ct.name";
		return entityManager.createQuery(query, CourseTemplate.class)
			.getResultList();
	}
	
	@Override
	public List<CourseTemplate> getTemplatesFor(RepositoryEntryRef sourceEntryRef) {
		String query = "SELECT ct FROM coursetemplate ct WHERE ct.sourceCourse.key = :sourceKey ORDER BY ct.name";
		return entityManager.createQuery(query, CourseTemplate.class)
			.setParameter("sourceKey", sourceEntryRef.getKey())
			.getResultList();
	}
	
	@Override
	public List<CourseTemplate> getTemplatesByCreator(Identity identity) {
		String query = "SELECT ct FROM coursetemplate ct WHERE ct.createdBy.key = :identityKey ORDER BY ct.createdDate DESC";
		return entityManager.createQuery(query, CourseTemplate.class)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
	}
	
	@Override
	public CourseTemplate setTemplateActive(CourseTemplate template, boolean active) {
		if (template instanceof CourseTemplateImpl) {
			CourseTemplateImpl impl = (CourseTemplateImpl) template;
			impl.setActive(active);
			entityManager.merge(impl);
			entityManager.flush();
			return impl;
		}
		return template;
	}
	
	@Override
	public void deleteTemplate(CourseTemplate template) {
		if (template instanceof CourseTemplateImpl) {
			CourseTemplateImpl impl = entityManager.merge((CourseTemplateImpl) template);
			entityManager.remove(impl);
			entityManager.flush();
		}
	}
	
	@Override
	public RepositoryEntry createCourseFromTemplate(CourseTemplate template, String newCourseName,
		String newCourseDescription, Identity creator) {
		
		log.info("Creating course from template: " + template.getName());
		
		// Create base course entry
		RepositoryEntry newEntry = repositoryService.create(creator, null, null, newCourseName, 
			newCourseDescription, null, 4); // 4 = course resource type
		
		// Copy structure if needed
		if (template.isIncludeStructure() || template.isIncludeCourseElements()) {
			ICourse sourceCourse = CourseFactory.loadCourse(template.getSourceCourse().getOlatResource().getResourceableId());
			ICourse newCourse = CourseFactory.openCourseEditSession(newEntry.getOlatResource().getResourceableId());
			
			if (template.isIncludeStructure()) {
				// Copy course structure from source to new course
				copyCourseStructure(sourceCourse, newCourse);
			}
			
			if (template.isIncludeCourseElements()) {
				// Copy course elements configuration
				copyCourseElements(sourceCourse, newCourse);
			}
			
			if (template.isIncludeCourseSettings()) {
				// Copy course settings/configuration
				copyCourseSettings(sourceCourse, newCourse);
			}
			
			CourseFactory.saveCourse(newCourse.getResourceableId());
			CourseFactory.closeCourseEditSession(newCourse.getResourceableId(), true);
		}
		
		log.info("Course created successfully from template");
		return newEntry;
	}
	
	private void copyCourseStructure(ICourse source, ICourse target) {
		// TODO: Implement structure copying logic
		// This would involve copying the course editor tree structure
		log.debug("Copying course structure");
	}
	
	private void copyCourseElements(ICourse source, ICourse target) {
		// TODO: Implement course elements copying logic
		// This would iterate through course nodes and copy their configurations
		log.debug("Copying course elements");
	}
	
	private void copyCourseSettings(ICourse source, ICourse target) {
		// TODO: Implement course settings copying logic
		// This would copy general course configuration, access settings, etc.
		log.debug("Copying course settings");
	}
}
