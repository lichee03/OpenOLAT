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
package org.olat.course.template.model;

import org.olat.core.id.Identity;
import org.olat.course.template.CourseTemplate;
import org.olat.repository.RepositoryEntry;

import javax.persistence.*;
import java.util.Date;

/**
 * Implementation of CourseTemplate entity
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
@Entity(name = "coursetemplate")
@Table(name = "o_course_template")
public class CourseTemplateImpl implements CourseTemplate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@Column(name = "description")
	private String description;
	
	@ManyToOne(targetEntity = RepositoryEntry.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "source_course_fk")
	private RepositoryEntry sourceCourse;
	
	@ManyToOne(targetEntity = Identity.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_fk")
	private Identity createdBy;
	
	@Column(name = "created_date")
	private Date createdDate;
	
	@Column(name = "include_structure")
	private boolean includeStructure = true;
	
	@Column(name = "include_elements")
	private boolean includeCourseElements = true;
	
	@Column(name = "include_resources")
	private boolean includeLearningResources = true;
	
	@Column(name = "include_settings")
	private boolean includeCourseSettings = false;
	
	@Column(name = "include_staff")
	private boolean includeStaffAssignments = false;
	
	@Column(name = "active")
	private boolean active = true;
	
	@Column(name = "last_modified")
	private Date lastModified;
	
	public CourseTemplateImpl() {
	}
	
	public CourseTemplateImpl(String name, String description, RepositoryEntry sourceCourse, Identity createdBy) {
		this.name = name;
		this.description = description;
		this.sourceCourse = sourceCourse;
		this.createdBy = createdBy;
		this.createdDate = new Date();
		this.lastModified = new Date();
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
		this.lastModified = new Date();
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
		this.lastModified = new Date();
	}
	
	@Override
	public RepositoryEntry getSourceCourse() {
		return sourceCourse;
	}
	
	public void setSourceCourse(RepositoryEntry sourceCourse) {
		this.sourceCourse = sourceCourse;
	}
	
	@Override
	public Identity getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(Identity createdBy) {
		this.createdBy = createdBy;
	}
	
	@Override
	public Date getCreatedDate() {
		return createdDate;
	}
	
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	@Override
	public boolean isIncludeStructure() {
		return includeStructure;
	}
	
	public void setIncludeStructure(boolean includeStructure) {
		this.includeStructure = includeStructure;
		this.lastModified = new Date();
	}
	
	@Override
	public boolean isIncludeCourseElements() {
		return includeCourseElements;
	}
	
	public void setIncludeCourseElements(boolean includeCourseElements) {
		this.includeCourseElements = includeCourseElements;
		this.lastModified = new Date();
	}
	
	@Override
	public boolean isIncludeLearningResources() {
		return includeLearningResources;
	}
	
	public void setIncludeLearningResources(boolean includeLearningResources) {
		this.includeLearningResources = includeLearningResources;
		this.lastModified = new Date();
	}
	
	@Override
	public boolean isIncludeCourseSettings() {
		return includeCourseSettings;
	}
	
	public void setIncludeCourseSettings(boolean includeCourseSettings) {
		this.includeCourseSettings = includeCourseSettings;
		this.lastModified = new Date();
	}
	
	@Override
	public boolean isIncludeStaffAssignments() {
		return includeStaffAssignments;
	}
	
	public void setIncludeStaffAssignments(boolean includeStaffAssignments) {
		this.includeStaffAssignments = includeStaffAssignments;
		this.lastModified = new Date();
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
		this.lastModified = new Date();
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@Override
	public int hashCode() {
		return key != null ? key.hashCode() : super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CourseTemplateImpl)) return false;
		CourseTemplateImpl other = (CourseTemplateImpl) obj;
		return key != null && key.equals(other.key);
	}
}
