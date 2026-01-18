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
package org.olat.course.enhancement;

/**
 * Represents a learning resource in a template course
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public class TemplateResource {
	
	private String resourceId;
	private String name;
	private String type; // scorm, wiki, etc
	private long size;
	private String linkedNodeId; // which course element references it
	private boolean selected;
	private String description;
	
	public TemplateResource(String resourceId, String name, String type) {
		this.resourceId = resourceId;
		this.name = name;
		this.type = type;
		this.selected = false;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public String getLinkedNodeId() {
		return linkedNodeId;
	}
	
	public void setLinkedNodeId(String linkedNodeId) {
		this.linkedNodeId = linkedNodeId;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "TemplateResource [resourceId=" + resourceId + ", name=" + name 
			+ ", type=" + type + ", size=" + size + ", selected=" + selected + "]";
	}
}
