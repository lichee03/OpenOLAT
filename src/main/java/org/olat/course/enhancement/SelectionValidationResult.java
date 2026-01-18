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
 * Result of validating selected course elements
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public class SelectionValidationResult {
	
	private boolean valid;
	private List<String> errors;
	private List<String> warnings;
	private List<String> suggestedAutoInclusions; // elements that should be included
	
	public SelectionValidationResult() {
		this.valid = true;
		this.errors = new ArrayList<>();
		this.warnings = new ArrayList<>();
		this.suggestedAutoInclusions = new ArrayList<>();
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public List<String> getErrors() {
		return errors;
	}
	
	public void addError(String error) {
		this.errors.add(error);
		this.valid = false;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public void addWarning(String warning) {
		this.warnings.add(warning);
	}
	
	public List<String> getSuggestedAutoInclusions() {
		return suggestedAutoInclusions;
	}
	
	public void addSuggestedAutoInclusion(String nodeId) {
		this.suggestedAutoInclusions.add(nodeId);
	}
	
	public boolean hasIssues() {
		return !valid || !warnings.isEmpty() || !suggestedAutoInclusions.isEmpty();
	}
	
	@Override
	public String toString() {
		return "SelectionValidationResult [valid=" + valid + ", errors=" 
			+ errors.size() + ", warnings=" + warnings.size() 
			+ ", suggestions=" + suggestedAutoInclusions.size() + "]";
	}
}
