# Implementation Checklist: Enhancements 3 & 5

Use this checklist to track integration of Smart Course Element Duplication (Enhancement 3) and Selective Template Element Chooser (Enhancement 5) into OpenOLAT.

---

## Phase 1: Code Integration (2-3 hours)

### 1.1 Copy Source Files
- [ ] Copy `CourseElementDuplicationService.java` to `org.olat.course.enhancement`
- [ ] Copy `CourseElementDuplicationServiceImpl.java` to `org.olat.course.enhancement`
- [ ] Copy `DuplicationResult.java` to `org.olat.course.enhancement`
- [ ] Copy `SelectiveTemplateInstantiationService.java` to `org.olat.course.enhancement`
- [ ] Copy `SelectiveTemplateInstantiationServiceImpl.java` to `org.olat.course.enhancement`
- [ ] Copy `TemplateElement.java` to `org.olat.course.enhancement`
- [ ] Copy `TemplateResource.java` to `org.olat.course.enhancement`
- [ ] Copy `SelectionPreview.java` to `org.olat.course.enhancement`
- [ ] Copy `SelectionValidationResult.java` to `org.olat.course.enhancement`
- [ ] Copy `SelectiveTemplateInstantiateController.java` to `org.olat.course.enhancement.ui`
- [ ] Copy `selective_template.vm` to appropriate template directory

**Verification**: All files compile without errors

### 1.2 Spring Configuration
- [ ] Verify `@Service` annotations are present on implementations
- [ ] Verify component scanning includes `org.olat.course.enhancement` package
- [ ] Verify `@Autowired` dependencies will be auto-wired
- [ ] Check for any manual bean registration needed in application context
- [ ] Verify no bean name conflicts with existing services

**Verification**: Run Spring component scan test

### 1.3 Dependency Resolution
- [ ] Verify all imports are available (CourseFactory, CourseNode, Identity, etc.)
- [ ] Check RepositoryManager availability
- [ ] Check CourseStructureManager availability
- [ ] Verify Velocity template support
- [ ] Check jQuery availability for UI

**Verification**: No unresolved import errors

### 1.4 Database Preparation
- [ ] Decide on optional tracking tables (recommended for audit trail)
- [ ] If enabling: Create `o_course_duplication_log` table
- [ ] If enabling: Create `o_template_instantiation_log` table
- [ ] Create database migration script (if using Liquibase/Flyway)
- [ ] Add DDL to database setup scripts

**Verification**: Tables created successfully in test environment

---

## Phase 2: Configuration & Localization (1 hour)

### 2.1 i18n Keys
Add to `src/main/resources/ApplicationResources.properties`:

**Enhancement 3 Keys (4 keys)**
- [ ] `button.duplicate.element=Duplicate Element`
- [ ] `button.duplicate.tree=Duplicate With Children`
- [ ] `elements.duplicated=Successfully duplicated {0} elements`
- [ ] `error.duplicating.elements=Error duplicating elements: {0}`

**Enhancement 5 Keys (12 keys)**
- [ ] `select.course.elements=Select Course Elements`
- [ ] `select.resources=Select Learning Resources`
- [ ] `copy.options=Copy Options`
- [ ] `deep.copy.resources=Deep Copy Resources`
- [ ] `deep.copy.help=Create full copies of resources instead of links`
- [ ] `selection.preview=Selection Preview`
- [ ] `elements.selected=Course Elements Selected: {0}`
- [ ] `resources.selected=Resources Selected: {0}`
- [ ] `estimated.size=Estimated Size`
- [ ] `no.elements.available=No elements available`
- [ ] `no.resources.available=No resources available`
- [ ] `suggestion.auto.include=Recommend including {0} additional dependent elements`

**Verification**: All keys appear in resource bundle

### 2.2 Configuration Properties (Optional)
Add to `olat.local.properties` or configuration file:

```properties
# Enhancement 3: Duplication
course.duplication.max.tree.size=100
course.duplication.timeout.ms=30000
course.duplication.track.operations=true

# Enhancement 5: Selective Templates
template.selective.max.preview.elements=500
template.selective.resource.copy.default=true
template.selective.deep.copy.enabled=true
```

**Verification**: Properties loaded without errors

---

## Phase 3: Integration Points (2-3 hours)

### 3.1 Course Controller Integration
In `CourseRuntimeController` or `CourseDetailController`:

- [ ] Inject `CourseElementDuplicationService`
- [ ] Add button/menu item for "Duplicate Element"
- [ ] Add button/menu item for "Duplicate With Children"
- [ ] Implement duplication logic (reference implementation provided)
- [ ] Handle DuplicationResult success/failure cases
- [ ] Display node ID mapping to user if needed
- [ ] Add to context menu for quick access

**Verification**: Duplication buttons appear and are functional

### 3.2 Template Wizard Integration
In `CreateCourseWizardController` or template wizard:

- [ ] Inject `SelectiveTemplateInstantiationService`
- [ ] Insert `SelectiveTemplateInstantiateController` as new step (Step 1.5)
- [ ] Listen for `SelectionEvent` from controller
- [ ] Pass selected elements to course creation logic
- [ ] Handle deep copy option
- [ ] Show progress/status during creation
- [ ] Navigate to newly created course on success

**Verification**: Template wizard shows selective element chooser

### 3.3 Context Menu Integration
In course node context menu handling:

- [ ] Add "Duplicate" action to course nodes
- [ ] Call `duplicationService.duplicateElement()`
- [ ] Show success notification
- [ ] Refresh course structure view

**Verification**: Right-click context menu includes Duplicate option

### 3.4 Bulk Operations Integration (Optional)
If using bulk operations:

- [ ] Add "Duplicate Selected" to bulk operations menu
- [ ] Implement multi-select handling
- [ ] Call `duplicationService.duplicateSelectedElements()`
- [ ] Show progress for bulk operation
- [ ] Provide summary of processed/failed elements

**Verification**: Bulk duplication works for multiple elements

---

## Phase 4: User Interface (1-2 hours)

### 4.1 Velocity Template
- [ ] Verify `selective_template.vm` location
- [ ] Check CSS classes match OpenOLAT theme
- [ ] Verify Bootstrap component usage
- [ ] Test responsive layout on mobile
- [ ] Check font awesome icons availability
- [ ] Verify jQuery selectors work correctly

**Verification**: Template renders without errors, responsive design works

### 4.2 Controller UI Elements
- [ ] Verify velocity container names match template references
- [ ] Check form elements are properly bound
- [ ] Verify toggle buttons work correctly
- [ ] Check tree hierarchy displays correctly
- [ ] Test checkbox interactions
- [ ] Verify preview updates in real-time

**Verification**: All UI interactions work as expected

### 4.3 Styling & Theming
- [ ] Adjust CSS to match OpenOLAT theme (if needed)
- [ ] Check color scheme consistency
- [ ] Verify spacing and padding
- [ ] Test with different screen sizes
- [ ] Check accessibility (ARIA labels, keyboard navigation)

**Verification**: UI looks professional and matches OpenOLAT design

### 4.4 Icons & Graphics
- [ ] Verify Font Awesome icon availability
- [ ] Check icon sizing (16x16, 32x32 as needed)
- [ ] Test icon visibility in light/dark themes
- [ ] Add custom icons if needed

**Verification**: All icons display correctly

---

## Phase 5: Testing (2-3 hours)

### 5.1 Unit Tests
Create test class: `CourseElementDuplicationServiceTest`

- [ ] Test `duplicateElement()` with assessment node
- [ ] Test `duplicateElement()` with content node
- [ ] Test `duplicateElement()` with non-duplicable type
- [ ] Test `duplicateElementTree()` with simple tree
- [ ] Test `duplicateElementTree()` with deep tree (5+ levels)
- [ ] Test `duplicateSelectedElements()` with multiple nodes
- [ ] Test node ID mapping accuracy
- [ ] Test configuration preservation
- [ ] Test error handling

Create test class: `SelectiveTemplateInstantiationServiceTest`

- [ ] Test `getTemplateElements()` returns correct elements
- [ ] Test element hierarchy is preserved
- [ ] Test `getTemplateResources()` discovers linked resources
- [ ] Test `validateSelection()` detects missing dependencies
- [ ] Test `validateSelection()` suggests auto-inclusions
- [ ] Test `previewSelection()` size estimation
- [ ] Test `createCourseWithSelectiveElements()` creates valid course
- [ ] Test error handling for invalid selections
- [ ] Test deep copy vs. link options

### 5.2 Integration Tests
- [ ] Test duplication within actual course structure
- [ ] Test template creation with UI interaction
- [ ] Test complete workflow: select elements → create course
- [ ] Test with different user roles (admin, instructor, user)
- [ ] Test concurrent duplication operations
- [ ] Test with large courses (50+ elements)
- [ ] Test reference fixing across elements

### 5.3 Functional Testing (Manual)
- [ ] Duplicate single element - verify creates correct node
- [ ] Duplicate tree - verify hierarchy maintained
- [ ] Duplicate with children - verify all children copied
- [ ] Check assessment configs preserved
- [ ] Check module configurations copied
- [ ] Test selective template: select multiple elements
- [ ] Test preview shows correct counts
- [ ] Test creation with selective elements
- [ ] Test with/without deep copy option
- [ ] Test error cases with invalid selections

### 5.4 UI Testing
- [ ] Element checkboxes toggle correctly
- [ ] Resource checkboxes work independently
- [ ] Parent/child relationships work (auto-check parent)
- [ ] Preview updates in real-time
- [ ] Dependency warnings display
- [ ] Size estimation displays correctly
- [ ] Continue/Back buttons function
- [ ] Mobile responsive layout works

### 5.5 Performance Testing
- [ ] Duplicate 10-node tree - measure time
- [ ] Duplicate 50-node tree - measure time
- [ ] Bulk duplicate 100 elements - measure time
- [ ] Load template with 100 elements - measure time
- [ ] Validate selection on 100 elements - measure time
- [ ] Verify no memory leaks in long operations

**Expected Performance**:
- Single element: < 100ms
- 10-node tree: 100-200ms
- 50-node tree: 500-1000ms
- 100-element bulk: 1-3s

### 5.6 Database Testing (if tracking tables enabled)
- [ ] Verify duplication operations logged correctly
- [ ] Check template instantiation log entries created
- [ ] Verify processing times recorded
- [ ] Check error messages stored
- [ ] Verify node ID mappings captured

**Verification**: All tests pass, 85%+ code coverage

---

## Phase 6: Documentation & Training (1 hour)

### 6.1 Code Documentation
- [ ] Review Javadoc comments
- [ ] Add inline comments for complex logic
- [ ] Document configuration options
- [ ] Add usage examples in code

**Verification**: All public methods have Javadoc

### 6.2 User Documentation
- [ ] Create user guide for duplication feature
- [ ] Create user guide for selective templates
- [ ] Add screenshots to documentation
- [ ] Create FAQ section
- [ ] Document limitations and known issues

### 6.3 Administrator Documentation
- [ ] Document configuration options
- [ ] Document optional database tables
- [ ] Create troubleshooting guide
- [ ] Document performance tuning options
- [ ] Document maintenance procedures

### 6.4 Developer Documentation
- [ ] Verify INTEGRATION_GUIDE_ENHANCEMENT_3_5.md is accurate
- [ ] Verify QUICK_REFERENCE_ENHANCEMENT_3_5.md is current
- [ ] Create API documentation
- [ ] Document extension points

**Verification**: All documentation is accurate and complete

---

## Phase 7: Deployment Preparation (1 hour)

### 7.1 Version Management
- [ ] Update version number if needed
- [ ] Update release notes
- [ ] Document breaking changes (if any)
- [ ] Document migration steps

### 7.2 Rollback Plan
- [ ] Create rollback script for database changes
- [ ] Document feature flag (if implemented)
- [ ] Create backup procedures
- [ ] Document recovery steps

### 7.3 Security Review
- [ ] Check for SQL injection vulnerabilities
- [ ] Verify authentication/authorization checks
- [ ] Check permission validation for duplication
- [ ] Verify audit logging
- [ ] Review data access patterns

### 7.4 Performance Validation
- [ ] Run load tests with realistic data volumes
- [ ] Monitor database query performance
- [ ] Check memory usage patterns
- [ ] Identify any N+1 query issues
- [ ] Verify connection pooling

**Verification**: No security issues, performance acceptable

---

## Phase 8: Deployment & Verification (1 hour)

### 8.1 Pre-Deployment
- [ ] Run full test suite
- [ ] Perform final code review
- [ ] Backup production database
- [ ] Verify rollback plan
- [ ] Notify users of change window

### 8.2 Deployment
- [ ] Copy new Java files to production
- [ ] Update Spring configuration
- [ ] Update i18n properties
- [ ] Run database migration (if applicable)
- [ ] Clear application caches
- [ ] Restart application

### 8.3 Post-Deployment Verification
- [ ] Verify services are loaded in Spring context
- [ ] Check application logs for errors
- [ ] Verify database connections
- [ ] Test duplication feature in production
- [ ] Test selective template feature
- [ ] Verify i18n keys display correctly
- [ ] Monitor application performance
- [ ] Check database growth (if logging)

### 8.4 User Communication
- [ ] Notify users of new features
- [ ] Share user documentation
- [ ] Provide training materials
- [ ] Establish support channel for issues

**Verification**: Features work correctly in production

---

## Known Issues & Troubleshooting

### Issue: Service not injected (NullPointerException)
**Solution**: 
- [ ] Verify @Service annotation present
- [ ] Check component scanning configuration
- [ ] Restart application
- [ ] Check Spring logs for initialization errors

### Issue: Duplication fails for assessment nodes
**Solution**:
- [ ] Verify `preserveAssessment=true`
- [ ] Check ModuleConfiguration is cloneable
- [ ] Verify assessment module is available
- [ ] Check logs for specific error

### Issue: Template elements not loading
**Solution**:
- [ ] Verify CourseFactory is working
- [ ] Check template course is accessible
- [ ] Verify RunStructureManager is initialized
- [ ] Check for circular references in template

### Issue: Selection validation shows false warnings
**Solution**:
- [ ] Review dependency detection logic
- [ ] Check if parent/child relationships correct
- [ ] Verify reference scanning logic
- [ ] Add debugging output to trace issue

### Issue: UI not rendering
**Solution**:
- [ ] Verify velocity template path
- [ ] Check for missing CSS/JS
- [ ] Review velocity context variables
- [ ] Check browser console for JavaScript errors

---

## Sign-Off Checklist

Before marking as complete:

- [ ] All code files integrated and compiling
- [ ] Spring beans properly registered
- [ ] Database tables created (if applicable)
- [ ] i18n keys added to properties files
- [ ] All integration points implemented
- [ ] UI elements functional
- [ ] All unit tests passing
- [ ] Integration tests passing
- [ ] Functional testing complete
- [ ] Performance acceptable
- [ ] Security review passed
- [ ] Documentation complete
- [ ] Deployed to production
- [ ] Post-deployment verification passed
- [ ] Users trained and notified

---

## Notes & Comments

**Completed By**: _____________________

**Date Completed**: _____________________

**Deployment Environment**: _____________________

**Issues Encountered**:
```
[List any issues and resolutions]
```

**Recommendations for Future**:
```
[List any recommendations for improvements]
```

---

## Appendix: Quick Commands

### Database Migration
```sql
-- Create duplication log (optional)
CREATE TABLE o_course_duplication_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    source_node_id VARCHAR(255),
    target_node_id VARCHAR(255),
    duplication_type VARCHAR(50),
    created_by BIGINT,
    created_date DATETIME,
    processing_time_ms BIGINT,
    success BOOLEAN,
    error_message TEXT,
    FOREIGN KEY (course_id) REFERENCES o_repository_entry(repositoryentry_id)
);

-- Create template instantiation log (optional)
CREATE TABLE o_template_instantiation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_id BIGINT NOT NULL,
    created_course_id BIGINT NOT NULL,
    selected_elements INT,
    selected_resources INT,
    deep_copy BOOLEAN,
    created_by BIGINT,
    created_date DATETIME,
    FOREIGN KEY (template_id) REFERENCES o_repository_entry(repositoryentry_id),
    FOREIGN KEY (created_course_id) REFERENCES o_repository_entry(repositoryentry_id)
);
```

### Verification Commands
```bash
# Check file locations
find . -name "CourseElementDuplicationService*.java"
find . -name "SelectiveTemplate*.java"

# Compile check
mvn clean compile

# Test execution
mvn test -Dtest=CourseElementDuplicationServiceTest
mvn test -Dtest=SelectiveTemplateInstantiationServiceTest
```

---

**End of Implementation Checklist**
