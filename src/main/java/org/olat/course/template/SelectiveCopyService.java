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

import java.util.List;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.template.model.SelectableCourseElement;
import org.olat.repository.RepositoryEntry;

/**
 * Enhancement 1: Selective Template Element Chooser
 * Service interface for managing selective course element copying
 * 
 * Initial date: January 18, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public interface SelectiveCopyService {
    
    /**
     * Extract all course elements from a course as selectable items
     * 
     * @param course The source course
     * @return List of selectable course elements organized hierarchically
     */
    List<SelectableCourseElement> extractSelectableElements(ICourse course);
    
    /**
     * Copy selected course elements to a new course
     * 
     * @param sourceCourse The source course
     * @param targetEntry The target repository entry
     * @param selectedElements Set of node IDs to copy
     * @param copyLearningResources Whether to copy associated learning resources
     * @param creator The identity creating the copy
     * @return The created course
     */
    ICourse copySelectedElements(ICourse sourceCourse, RepositoryEntry targetEntry, 
            Set<String> selectedElements, boolean copyLearningResources, Identity creator);
    
    /**
     * Validate element selection and resolve dependencies
     * 
     * @param allElements All available elements
     * @param selectedNodeIds Set of selected node IDs
     * @return ValidationResult with errors and warnings
     */
    ValidationResult validateSelection(List<SelectableCourseElement> allElements, Set<String> selectedNodeIds);
    
    /**
     * Auto-select dependent elements based on current selection
     * 
     * @param allElements All available elements
     * @param currentSelection Current set of selected node IDs
     * @return Updated set including auto-selected dependencies
     */
    Set<String> resolveAndAutoSelectDependencies(List<SelectableCourseElement> allElements, 
            Set<String> currentSelection);
    
    /**
     * Result of validation with errors and warnings
     */
    class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
        
        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }
    }
}
