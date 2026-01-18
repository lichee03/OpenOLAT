# OpenOLAT Course Management Enhancement Guide

## Overview
This document provides detailed instructions for enhancing the OpenOLAT Course Management Module with four key features:
1. Course Template Library
2. Template Wizard
3. Bulk Staff Assignment
4. Course Readiness Checker

## Project Structure

### New Files Created

#### 1. Core Interfaces (in `src/main/java/org/olat/course/template/`)
- `CourseTemplate.java` - Interface for course template data
- `CourseTemplateService.java` - Service interface for template management
- `BulkStaffAssignmentService.java` - Service interface for bulk staff operations
- `CourseReadinessCheckItem.java` - Interface for readiness check results
- `CourseReadinessCheckerService.java` - Service interface for course validation

#### 2. Model Classes (in `src/main/java/org/olat/course/template/model/`)
- `CourseTemplateImpl.java` - JPA entity implementing CourseTemplate
- `CourseReadinessCheckItemImpl.java` - Implementation of readiness check items

#### 3. Service Implementations (in `src/main/java/org/olat/course/template/manager/`)
- `CourseTemplateServiceImpl.java` - Implements CourseTemplateService
- `BulkStaffAssignmentServiceImpl.java` - Implements BulkStaffAssignmentService
- `CourseReadinessCheckerServiceImpl.java` - Implements CourseReadinessCheckerService

## Integration Points with Existing Code

### 1. CourseWizardService Integration
**File**: `src/main/java/org/olat/course/wizard/manager/CourseWizardServiceImpl.java`

Add the following to the existing service:

```java
@Autowired
private CourseTemplateService courseTemplateService;

@Autowired
private BulkStaffAssignmentService bulkStaffAssignmentService;

@Autowired
private CourseReadinessCheckerService readinessCheckerService;

/**
 * Create course from template with configurable options
 */
public ICourse createCourseFromTemplate(CourseTemplate template, String courseName, 
    String courseDescription, Identity creator) {
    RepositoryEntry newEntry = courseTemplateService.createCourseFromTemplate(
        template, courseName, courseDescription, creator);
    return CourseFactory.loadCourse(newEntry);
}

/**
 * Bulk assign staff before publishing
 */
public void bulkAssignStaffBeforePublish(ICourse course, Collection<Identity> coaches,
    Collection<Identity> participants, Identity executor) {
    RepositoryEntry courseEntry = course.getCourseEnvironment()
        .getCourseGroupManager().getCourseEntry();
    
    bulkStaffAssignmentService.assignCoaches(courseEntry, coaches, executor);
    bulkStaffAssignmentService.assignParticipants(courseEntry, participants, executor);
}

/**
 * Validate course readiness before publishing
 */
public List<CourseReadinessCheckItem> validateCourseBeforePublish(ICourse course) {
    return readinessCheckerService.checkCourseReadiness(course);
}
```

### 2. CourseFactory Integration
**File**: `src/main/java/org/olat/course/CourseFactory.java`

Add support for template-based course creation:

```java
/**
 * Create course from template
 */
public static RepositoryEntry createFromTemplate(CourseTemplate template, 
    String courseName, String description, Identity creator) {
    CourseTemplateService templateService = CoreSpringFactory.getImpl(CourseTemplateService.class);
    return templateService.createCourseFromTemplate(template, courseName, description, creator);
}
```

### 3. CourseGroupManager Integration
**File**: `src/main/java/org/olat/course/groupsandrights/CourseGroupManager.java`

The bulk staff assignment service works with the existing CourseGroupManager. No changes needed, but the service builds on top of:
- `isIdentityInGroup()` - Check membership
- `hasRight()` - Check permissions
- Coordinate with RepositoryService for member management

### 4. Controller Integration
**File**: `src/main/java/org/olat/course/run/CourseRuntimeController.java`

Add readiness checking before publication:

```java
private void doPublishCourse(UserRequest ureq) {
    CourseReadinessCheckerService readinessChecker = 
        CoreSpringFactory.getImpl(CourseReadinessCheckerService.class);
    
    ICourse course = getOpenCourse();
    List<CourseReadinessCheckItem> issues = readinessChecker.checkCourseReadiness(course);
    
    if (readinessChecker.canPublish(course)) {
        // Proceed with publishing
        publishCourse(ureq);
    } else {
        // Show readiness check dialog
        showReadinessCheckDialog(ureq, issues);
    }
}

private void showReadinessCheckDialog(UserRequest ureq, List<CourseReadinessCheckItem> issues) {
    // Display issues grouped by category
    Map<CourseReadinessCheckItem.CheckCategory, List<CourseReadinessCheckItem>> byCategory 
        = issues.stream()
            .collect(Collectors.groupingBy(CourseReadinessCheckItem::getCategory));
    
    // Create dialog showing issues and allow user to proceed or fix issues
}
```

## Database Schema Changes

Add the following table for course templates:

```sql
CREATE TABLE o_course_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_course_fk BIGINT NOT NULL,
    created_by_fk BIGINT NOT NULL,
    created_date DATETIME,
    include_structure BOOLEAN DEFAULT TRUE,
    include_elements BOOLEAN DEFAULT TRUE,
    include_resources BOOLEAN DEFAULT TRUE,
    include_settings BOOLEAN DEFAULT FALSE,
    include_staff BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    last_modified DATETIME,
    
    FOREIGN KEY (source_course_fk) REFERENCES o_repositoryentry(repositoryid),
    FOREIGN KEY (created_by_fk) REFERENCES o_bs_identity(id),
    
    INDEX idx_source_course (source_course_fk),
    INDEX idx_created_by (created_by_fk),
    INDEX idx_active (active)
);
```

## Usage Examples

### 1. Creating a Course Template
```java
@Autowired
private CourseTemplateService templateService;

public void createTemplateExample() {
    RepositoryEntry sourceEntry = // get existing course
    Identity creator = // get current user
    
    CourseTemplate template = templateService.createTemplate(
        sourceEntry,
        "Biology 101 Template",
        "Reusable template for Biology courses",
        creator
    );
    
    // Configure what gets copied
    templateService.updateTemplateConfig(
        template,
        true,  // include structure
        true,  // include course elements
        true,  // include learning resources
        false, // include course settings
        true   // include staff assignments
    );
}
```

### 2. Creating Course from Template
```java
public void createCourseFromTemplateExample() {
    CourseTemplate template = templateService.getTemplate(templateId);
    
    RepositoryEntry newCourse = templateService.createCourseFromTemplate(
        template,
        "Biology 101 - Spring 2026",
        "Biology course for Spring semester",
        currentIdentity
    );
}
```

### 3. Bulk Staff Assignment
```java
@Autowired
private BulkStaffAssignmentService bulkStaffService;

public void assignStaffExample() {
    RepositoryEntry courseEntry = // get course
    Collection<Identity> coaches = // get list of coaches
    Collection<Identity> participants = // get list of participants
    
    bulkStaffService.assignCoaches(courseEntry, coaches, currentIdentity);
    bulkStaffService.assignParticipants(courseEntry, participants, currentIdentity);
}
```

### 4. Course Readiness Check
```java
@Autowired
private CourseReadinessCheckerService readinessChecker;

public void checkCourseReadinessExample() {
    ICourse course = CourseFactory.loadCourse(courseResourceId);
    
    // Get all issues
    List<CourseReadinessCheckItem> issues = readinessChecker.checkCourseReadiness(course);
    
    // Check if can publish
    if (readinessChecker.canPublish(course)) {
        // Safe to publish
        publishCourse(course);
    } else {
        // Show issues to user
        issues.forEach(item -> 
            log.warn(item.getLevel() + " [" + item.getCategory() + "]: " + item.getMessage())
        );
    }
    
    // Get issues by category
    Map<CheckCategory, List<CourseReadinessCheckItem>> byCategory = 
        readinessChecker.checkByCategory(course);
}
```

## Spring Configuration

The services are automatically registered as Spring beans using `@Service` annotations. Ensure your application context includes:

1. Component scanning for `org.olat.course.template` package
2. Transaction management for entity persistence
3. Proper JPA configuration for the new `CourseTemplateImpl` entity

### Application Properties to Add

```properties
# Course Template Settings
course.template.enabled=true
course.template.max-templates=100
course.template.default-include-structure=true
course.template.default-include-elements=true
```

## Extension Points

### Custom Template Copying Logic
Override the template copying methods in `CourseTemplateServiceImpl`:
- `copyCourseStructure()` - Customize structure copying
- `copyCourseElements()` - Customize element copying  
- `copyCourseSettings()` - Customize settings copying

### Custom Readiness Checks
Extend `CourseReadinessCheckerServiceImpl` to add domain-specific checks:

```java
@Service
public class CustomCourseReadinessCheckerServiceImpl extends CourseReadinessCheckerServiceImpl {
    
    @Override
    public List<CourseReadinessCheckItem> checkLearningResources(ICourse course) {
        List<CourseReadinessCheckItem> results = super.checkLearningResources(course);
        
        // Add custom checks
        // ...
        
        return results;
    }
}
```

## Testing

Create unit tests in `src/test/java/org/olat/course/template/`:

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class CourseTemplateServiceTest {
    
    @Autowired
    private CourseTemplateService templateService;
    
    @Test
    public void testCreateTemplate() {
        // Test template creation
    }
    
    @Test
    public void testCreateCourseFromTemplate() {
        // Test course creation from template
    }
}
```

## Summary of Files to Modify

1. **CourseWizardServiceImpl.java** - Add new methods for template integration
2. **CourseFactory.java** - Add methods for template-based course creation
3. **Database schema** - Add o_course_template table
4. **Spring context configuration** - Register new beans

## Files to Create

All files have been created in the `src/main/java/org/olat/course/template/` and 
`src/main/java/org/olat/course/template/manager/` directories as detailed above.
