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

import java.util.ArrayList;
import java.util.List;

/**
 * Preview of what will be created based on selection
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public class SelectionPreview {
	
	private int totalElementsSelected;
	private int totalResourcesSelected;
	private long estimatedSize;
	private List<String> selectedNodeNames;
	private List<String> selectedResourceNames;
	private List<String> warnings;
	private List<String> dependencies;
	
	public SelectionPreview() {
		this.selectedNodeNames = new ArrayList<>();
		this.selectedResourceNames = new ArrayList<>();
		this.warnings = new ArrayList<>();
		this.dependencies = new ArrayList<>();
	}
	
	public int getTotalElementsSelected() {
		return totalElementsSelected;
	}
	
	public void setTotalElementsSelected(int totalElementsSelected) {
		this.totalElementsSelected = totalElementsSelected;
	}
	
	public int getTotalResourcesSelected() {
		return totalResourcesSelected;
	}
	
	public void setTotalResourcesSelected(int totalResourcesSelected) {
		this.totalResourcesSelected = totalResourcesSelected;
	}
	
	public long getEstimatedSize() {
		return estimatedSize;
	}
	
	public void setEstimatedSize(long estimatedSize) {
		this.estimatedSize = estimatedSize;
	}
	
	public List<String> getSelectedNodeNames() {
		return selectedNodeNames;
	}
	
	public void addSelectedNodeName(String name) {
		this.selectedNodeNames.add(name);
	}
	
	public List<String> getSelectedResourceNames() {
		return selectedResourceNames;
	}
	
	public void addSelectedResourceName(String name) {
		this.selectedResourceNames.add(name);
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void addWarning(String warning) {
		this.warnings.add(warning);
	}
	
	public List<String> getDependencies() {
		return dependencies;
	}
	
	public void addDependency(String dependency) {
		this.dependencies.add(dependency);
	}
	
	@Override
	public String toString() {
		return "SelectionPreview [elements=" + totalElementsSelected 
			+ ", resources=" + totalResourcesSelected + ", estimatedSize=" 
			+ estimatedSize + ", warnings=" + warnings.size() + "]";
	}
}
