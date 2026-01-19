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
package org.olat.course.publish;

/**
 * Enhancement 4: Course Readiness Checker
 * 
 * Types of readiness checks that can be performed
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public enum ReadinessCheckType {
    
    /** Check course structure (has nodes, proper hierarchy) */
    STRUCTURE,
    
    /** Check all nodes have required content */
    CONTENT,
    
    /** Check referenced learning resources exist */
    RESOURCES,
    
    /** Check access conditions are valid */
    ACCESS_CONDITIONS,
    
    /** Check assessment configurations */
    ASSESSMENTS,
    
    /** Check staff assignments (owners, coaches) */
    STAFF,
    
    /** Check enrollment configuration */
    ENROLLMENT,
    
    /** Check metadata (title, description) */
    METADATA,
    
    /** Check catalog/taxonomy assignments */
    CATALOG,
    
    /** Check dates and deadlines */
    DATES,
    
    /** Check notification settings */
    NOTIFICATIONS,
    
    /** Check all external links are valid */
    LINKS,
    
    /** Check media and attachments */
    MEDIA,
    
    /** Check certificate configuration */
    CERTIFICATES,
    
    /** Check grading configuration */
    GRADING,
    
    /** Perform all checks */
    ALL
}
