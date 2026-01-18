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
 * Represents a selectable course element in template hierarchy
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public class TemplateElement {
	
	private String nodeId;
	private String title;
	private String nodeType;
	private String iconCssClass;
	private int level; // depth in tree
	private String parentNodeId;
	private boolean hasChildren;
	private boolean hasLinkedResource;
	private String resourceType;
	private boolean selected;
	private String description;
	
	public TemplateElement(String nodeId, String title, String nodeType) {
		this.nodeId = nodeId;
		this.title = title;
		this.nodeType = nodeType;
		this.selected = false;
		this.hasChildren = false;
	}
	
	public String getNodeId() {
		return nodeId;
	}
	
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getNodeType() {
		return nodeType;
	}
	
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
	public String getIconCssClass() {
		return iconCssClass;
	}
	
	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public String getParentNodeId() {
		return parentNodeId;
	}
	
	public void setParentNodeId(String parentNodeId) {
		this.parentNodeId = parentNodeId;
	}
	
	public boolean isHasChildren() {
		return hasChildren;
	}
	
	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}
	
	public boolean isHasLinkedResource() {
		return hasLinkedResource;
	}
	
	public void setHasLinkedResource(boolean hasLinkedResource) {
		this.hasLinkedResource = hasLinkedResource;
	}
	
	public String getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
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
		return "TemplateElement [nodeId=" + nodeId + ", title=" + title 
			+ ", nodeType=" + nodeType + ", level=" + level + ", selected=" 
			+ selected + "]";
	}
}
