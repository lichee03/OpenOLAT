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
 * Result of a course element duplication operation
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public class DuplicationResult {
	
	private boolean success;
	private String errorMessage;
	private int elementsProcessed;
	private int elementsFailed;
	private java.util.Map<String, String> nodeIdMapping; // old ID -> new ID
	private java.util.List<String> warnings;
	private long processingTimeMs;
	
	public DuplicationResult() {
		this.success = true;
		this.elementsProcessed = 0;
		this.elementsFailed = 0;
		this.nodeIdMapping = new java.util.HashMap<>();
		this.warnings = new java.util.ArrayList<>();
		this.processingTimeMs = 0;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		this.success = false;
	}
	
	public int getElementsProcessed() {
		return elementsProcessed;
	}
	
	public void setElementsProcessed(int elementsProcessed) {
		this.elementsProcessed = elementsProcessed;
	}
	
	public int getElementsFailed() {
		return elementsFailed;
	}
	
	public void setElementsFailed(int elementsFailed) {
		this.elementsFailed = elementsFailed;
	}
	
	public void incrementElementsProcessed() {
		this.elementsProcessed++;
	}
	
	public void incrementElementsFailed() {
		this.elementsFailed++;
	}
	
	public java.util.Map<String, String> getNodeIdMapping() {
		return nodeIdMapping;
	}
	
	public void setNodeIdMapping(java.util.Map<String, String> nodeIdMapping) {
		this.nodeIdMapping = nodeIdMapping;
	}
	
	public void addNodeMapping(String oldId, String newId) {
		if (nodeIdMapping == null) {
			nodeIdMapping = new java.util.HashMap<>();
		}
		nodeIdMapping.put(oldId, newId);
	}
	
	public java.util.List<String> getWarnings() {
		return warnings;
	}
	
	public void addWarning(String warning) {
		if (warnings == null) {
			warnings = new java.util.ArrayList<>();
		}
		warnings.add(warning);
	}
	
	public long getProcessingTimeMs() {
		return processingTimeMs;
	}
	
	public void setProcessingTimeMs(long processingTimeMs) {
		this.processingTimeMs = processingTimeMs;
	}
	
	@Override
	public String toString() {
		return "DuplicationResult [success=" + success + ", elementsProcessed="
			+ elementsProcessed + ", elementsFailed=" + elementsFailed 
			+ ", mappings=" + (nodeIdMapping != null ? nodeIdMapping.size() : 0)
			+ ", processingTimeMs=" + processingTimeMs + "]";
	}
}
