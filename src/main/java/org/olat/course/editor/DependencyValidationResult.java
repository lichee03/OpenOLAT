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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhancement 2: Smart Course Element Duplication with Dependency Mapping
 * 
 * Result of dependency validation when attempting to delete or modify a node
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class DependencyValidationResult {
    
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;
    private final List<String> affectedNodeIds;
    private final String summary;
    
    private DependencyValidationResult(boolean valid, String summary, 
            List<String> errors, List<String> warnings, List<String> affectedNodeIds) {
        this.valid = valid;
        this.summary = summary;
        this.errors = errors;
        this.warnings = warnings;
        this.affectedNodeIds = affectedNodeIds;
    }
    
    public static DependencyValidationResult valid() {
        return new DependencyValidationResult(true, "No dependency issues found", 
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    public static DependencyValidationResult invalid(String summary, List<String> errors, 
            List<String> affectedNodeIds) {
        return new DependencyValidationResult(false, summary, errors, 
                new ArrayList<>(), affectedNodeIds);
    }
    
    public static DependencyValidationResult withWarnings(String summary, List<String> warnings,
            List<String> affectedNodeIds) {
        return new DependencyValidationResult(true, summary, new ArrayList<>(), 
                warnings, affectedNodeIds);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public List<String> getAffectedNodeIds() {
        return affectedNodeIds;
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    @Override
    public String toString() {
        return "DependencyValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                ", summary='" + summary + '\'' +
                '}';
    }
}
