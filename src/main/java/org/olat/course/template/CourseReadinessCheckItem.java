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
import java.util.Map;

import org.olat.course.ICourse;

/**
 * Readiness check result item
 * 
 * Initial date: 18 Jan 2026<br>
 * @author development team
 *
 */
public interface CourseReadinessCheckItem {
	
	/**
	 * Check categories: STRUCTURE, ELEMENTS, STAFF, SETTINGS, RESOURCES
	 */
	enum CheckCategory {
		STRUCTURE,
		ELEMENTS,
		STAFF,
		SETTINGS,
		RESOURCES
	}
	
	/**
	 * Check levels: INFO, WARNING, ERROR
	 */
	enum CheckLevel {
		INFO,
		WARNING,
		ERROR
	}
	
	/**
	 * Get the check category
	 * @return
	 */
	CheckCategory getCategory();
	
	/**
	 * Get the check level
	 * @return
	 */
	CheckLevel getLevel();
	
	/**
	 * Get the check message
	 * @return
	 */
	String getMessage();
	
	/**
	 * Get if this is a critical issue that prevents publication
	 * @return
	 */
	boolean isCritical();
}
