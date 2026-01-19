/**
 * OLAT - Online Learning and Training<br>
 * https://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <hr>
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * This file has been modified by the OpenOLAT community. Changes are licensed
 * under the Apache 2.0 license as the original file.
 */
package org.olat.course.nodes.en.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of enrollment eligibility validation.
 * Contains information about whether a user can enroll and reasons why not.
 * 
 * Initial Date: January 19, 2026
 * @author OpenOLAT Community
 */
public class EnrollmentEligibility {
    
    private final boolean eligible;
    private final List<ValidationMessage> errors;
    private final List<ValidationMessage> warnings;
    private final List<ValidationMessage> info;
    
    private EnrollmentEligibility(Builder builder) {
        this.eligible = builder.eligible;
        this.errors = builder.errors;
        this.warnings = builder.warnings;
        this.info = builder.info;
    }
    
    public boolean isEligible() {
        return eligible;
    }
    
    public List<ValidationMessage> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public List<ValidationMessage> getWarnings() {
        return new ArrayList<>(warnings);
    }
    
    public List<ValidationMessage> getInfo() {
        return new ArrayList<>(info);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static EnrollmentEligibility eligible() {
        return new Builder().eligible(true).build();
    }
    
    public static EnrollmentEligibility notEligible(String reason) {
        return new Builder().eligible(false)
                .addError(ValidationMessage.error(reason))
                .build();
    }
    
    public static class Builder {
        private boolean eligible = true;
        private List<ValidationMessage> errors = new ArrayList<>();
        private List<ValidationMessage> warnings = new ArrayList<>();
        private List<ValidationMessage> info = new ArrayList<>();
        
        public Builder eligible(boolean eligible) {
            this.eligible = eligible;
            return this;
        }
        
        public Builder addError(ValidationMessage error) {
            this.errors.add(error);
            this.eligible = false;
            return this;
        }
        
        public Builder addWarning(ValidationMessage warning) {
            this.warnings.add(warning);
            return this;
        }
        
        public Builder addInfo(ValidationMessage info) {
            this.info.add(info);
            return this;
        }
        
        public Builder merge(EnrollmentEligibility other) {
            this.errors.addAll(other.getErrors());
            this.warnings.addAll(other.getWarnings());
            this.info.addAll(other.getInfo());
            if (!other.isEligible()) {
                this.eligible = false;
            }
            return this;
        }
        
        public EnrollmentEligibility build() {
            return new EnrollmentEligibility(this);
        }
    }
    
    public static class ValidationMessage {
        private final MessageType type;
        private final String code;
        private final String message;
        private final String[] params;
        
        private ValidationMessage(MessageType type, String code, String message, String... params) {
            this.type = type;
            this.code = code;
            this.message = message;
            this.params = params;
        }
        
        public static ValidationMessage error(String message, String... params) {
            return new ValidationMessage(MessageType.ERROR, null, message, params);
        }
        
        public static ValidationMessage error(String code, String message, String... params) {
            return new ValidationMessage(MessageType.ERROR, code, message, params);
        }
        
        public static ValidationMessage warning(String message, String... params) {
            return new ValidationMessage(MessageType.WARNING, null, message, params);
        }
        
        public static ValidationMessage info(String message, String... params) {
            return new ValidationMessage(MessageType.INFO, null, message, params);
        }
        
        public MessageType getType() {
            return type;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String[] getParams() {
            return params;
        }
    }
    
    public enum MessageType {
        ERROR, WARNING, INFO
    }
}
