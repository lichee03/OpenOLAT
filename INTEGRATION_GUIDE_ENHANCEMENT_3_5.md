# Integration Guide: Smart Course Element Duplication & Selective Template Element Chooser

## Overview

This guide explains how to integrate two powerful new enhancements to OpenOLAT's course management system:

1. **Smart Course Element Duplication with Dependency Mapping** (Enhancement 3)
2. **Selective Template Element Chooser** (Enhancement 5)

## Architecture

### Enhancement 3: Smart Element Duplication

**Purpose**: Intelligent duplication of course elements with automatic dependency mapping and configuration preservation.

**Key Components**:
- `CourseElementDuplicationService` - Interface defining duplication operations
- `CourseElementDuplicationServiceImpl` - Implementation with dependency handling
- `DuplicationResult` - Result object with node ID mappings and statistics

**Key Features**:
- Single element duplication
- Recursive tree duplication
- Bulk element duplication  
- Assessment configuration preservation
- Reference fixing
- Dependency mapping (old ID → new ID)
- Non-duplicable node type detection

### Enhancement 5: Selective Template Element Chooser

**Purpose**: Allow users to selectively choose which course elements and resources to copy when creating from templates, solving the "all-or-nothing" limitation.

**Key Components**:
- `SelectiveTemplateInstantiationService` - Interface for selective instantiation
- `SelectiveTemplateInstantiationServiceImpl` - Implementation
- `SelectiveTemplateInstantiateController` - UI controller with element/resource picker
- Data models: `TemplateElement`, `TemplateResource`, `SelectionPreview`, `SelectionValidationResult`

**Key Features**:
- Template element extraction with hierarchy
- Resource discovery and listing
- Selection validation with dependency checking
- Selection preview with size estimation
- Automatic suggestion of dependencies
- Deep copy or link resources options

## File Structure

### Created Files

```
OpenOLAT/src/main/java/org/olat/course/enhancement/
├── CourseElementDuplicationService.java (interface)
├── CourseElementDuplicationServiceImpl.java (implementation)
├── DuplicationResult.java (model)
├── SelectiveTemplateInstantiationService.java (interface)
├── SelectiveTemplateInstantiationServiceImpl.java (implementation)
├── TemplateElement.java (model)
├── TemplateResource.java (model)
├── SelectionPreview.java (model)
├── SelectionValidationResult.java (model)
└── ui/
    ├── SelectiveTemplateInstantiateController.java (UI controller)
    └── selective_template.vm (velocity template)
```

## Integration Steps

### Step 1: Register Services in Spring Context

Add to `org/olat/course/coursemodule/CourseModule.java` or appropriate Spring configuration:

```java
@Bean
public CourseElementDuplicationService courseElementDuplicationService() {
    return new CourseElementDuplicationServiceImpl();
}

@Bean
public SelectiveTemplateInstantiationService selectiveTemplateInstantiationService(
        RepositoryManager repositoryManager,
        CourseElementDuplicationService duplicationService) {
    return new SelectiveTemplateInstantiationServiceImpl(
        repositoryManager, duplicationService);
}
```

Or use `@Service` annotations (already present in implementations).

### Step 2: Integrate with Course Duplication Workflows

**In `CourseRuntimeController`** or course duplication controller:

```java
@Autowired
private CourseElementDuplicationService duplicationService;

// For duplicating selected elements
public void duplicateElements(List<String> nodeIds, CourseNode targetParent) {
    DuplicationResult result = duplicationService.duplicateSelectedElements(
        course, nodeIds, targetParent, true, getIdentity());
    
    if (result.isSuccess()) {
        showInfo("elements.duplicated", 
            String.valueOf(result.getElementsProcessed()));
    } else {
        showError("error.duplicating.elements", result.getErrorMessage());
    }
}

// For duplicating element trees
public void duplicateElementTree(String sourceNodeId) {
    CourseNode sourceNode = course.getCourseEnvironment()
        .getRunStructureManager().getNodeById(sourceNodeId);
    
    DuplicationResult result = duplicationService.duplicateElementTree(
        course, sourceNode, targetParent, true, true, getIdentity());
    
    // Handle result...
}
```

### Step 3: Integrate with Template Wizard

**Modify `CreateCourseFromTemplateStep02Controller`** to use selective instantiation:

```java
@Autowired
private SelectiveTemplateInstantiationService selectiveTemplateService;

@Override
protected void doNext(UserRequest ureq) {
    // Launch selective element chooser
    SelectiveTemplateInstantiateController selectController = 
        new SelectiveTemplateInstantiateController(ureq, getWindowControl(), 
            templateEntry);
    
    // Listen for selection event
    listenTo(selectController);
}

@Override
protected void event(UserRequest ureq, Controller source, Event event) {
    if (source instanceof SelectiveTemplateInstantiateController) {
        SelectionEvent selectionEvent = (SelectionEvent) event;
        Set<String> selectedElements = selectionEvent.getSelectedNodeIds();
        Set<String> selectedResources = selectionEvent.getSelectedResourceIds();
        
        // Create course with selective elements
        RepositoryEntry newCourse = selectiveTemplateService
            .createCourseWithSelectiveElements(
                templateEntry,
                courseName,
                description,
                selectedElements,
                selectedResources,
                true, // deep copy
                getIdentity());
        
        if (newCourse != null) {
            showInfo("course.created.from.template");
        }
    }
}
```

### Step 4: Add UI Integration Points

**In `CreateCourseWizardController`**, insert after step 01:

```java
// Between existing steps, add:
private static final int STEP_SELECT_ELEMENTS = 2; // New step

@Override
protected Step getStep(int step) {
    switch(step) {
        case STEP_01: return new CreateCourseFromTemplateStep01(ureq);
        case STEP_SELECT_ELEMENTS: return new SelectiveTemplateStep(ureq);
        case STEP_02: return new CreateCourseFromTemplateStep02(ureq);
        // ...
    }
}
```

### Step 5: Update Persistence (if needed)

If tracking duplication history:

```java
// Add to course audit/tracking tables
INSERT INTO o_course_element_audit 
    (source_element_id, target_element_id, course_id, duplication_type, created_by, created_date)
VALUES (?, ?, ?, 'SELECTIVE', ?, NOW());
```

### Step 6: Add i18n Messages

**In `ApplicationResources.properties`**:

```properties
# Enhancement 3: Duplication
button.duplicate.element=Duplicate Element
button.duplicate.tree=Duplicate With Children
duplicating.elements=Duplicating {0} elements...
elements.duplicated=Successfully duplicated {0} elements
error.duplicating.elements=Error duplicating elements: {0}
element.not.duplicable=Element type {0} cannot be duplicated

# Enhancement 5: Selective Instantiation
select.course.elements=Select Course Elements
select.resources=Select Learning Resources
copy.options=Copy Options
deep.copy.resources=Deep Copy Resources
deep.copy.help=Create full copies of resources instead of links
selection.preview=Selection Preview
elements.selected=Course Elements Selected: {0}
resources.selected=Resources Selected: {0}
estimated.size=Estimated Size
no.elements.available=No elements available
no.resources.available=No resources available
linked.to=Linked to
suggestion.auto.include=Recommend including {0} additional dependent elements
error.invalid.selection=Invalid selection - please review errors
error.update.selection=Error updating selection preview
error.loading.elements=Could not load template elements
error.loading.resources=Could not load template resources
warnings=Warnings
dependencies=Dependencies
back=Back
continue=Continue
```

## Usage Examples

### Example 1: Duplicate a Single Element

```java
// Get the course and source element
ICourse course = CourseFactory.loadCourse(courseEntry);
CourseNode sourceElement = course.getCourseEnvironment()
    .getRunStructureManager().getNodeById("assessment-123");

// Duplicate it
CourseNode newElement = duplicationService.duplicateElement(
    course, sourceElement, parentNode, true, currentUser);

if (newElement != null) {
    System.out.println("Created: " + newElement.getIdent());
}
```

### Example 2: Duplicate Element Tree

```java
DuplicationResult result = duplicationService.duplicateElementTree(
    course, sourceNode, targetParent, 
    true,   // preserve assessment configs
    true,   // fix references
    executor);

if (result.isSuccess()) {
    System.out.println("Processed: " + result.getElementsProcessed());
    System.out.println("ID mappings: " + result.getNodeIdMapping());
} else {
    System.out.println("Error: " + result.getErrorMessage());
}
```

### Example 3: Selective Template Creation

```java
// Get available elements
List<TemplateElement> elements = selectiveTemplateService
    .getTemplateElements(templateEntry);

// User selects elements...
Set<String> selectedIds = new HashSet<>(
    Arrays.asList("elem-1", "elem-2", "elem-5"));

// Preview before creation
SelectionPreview preview = selectiveTemplateService.previewSelection(
    templateEntry, selectedIds, Collections.emptySet());

System.out.println("Will create: " + preview.getTotalElementsSelected() 
    + " elements");

// Validate
SelectionValidationResult validation = selectiveTemplateService
    .validateSelection(templateEntry, selectedIds);

if (validation.isValid()) {
    // Create course
    RepositoryEntry newCourse = selectiveTemplateService
        .createCourseWithSelectiveElements(
            templateEntry,
            "My New Course",
            "Course description",
            selectedIds,
            Collections.emptySet(),
            true,
            currentUser);
}
```

## Database Considerations

### Optional: Add Duplication Tracking

```sql
CREATE TABLE o_course_duplication_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    source_node_id VARCHAR(255),
    target_node_id VARCHAR(255),
    duplication_type VARCHAR(50), -- 'SINGLE', 'TREE', 'SELECTED'
    created_by BIGINT,
    created_date DATETIME,
    processing_time_ms BIGINT,
    success BOOLEAN,
    error_message TEXT,
    FOREIGN KEY (course_id) REFERENCES o_repository_entry(repositoryentry_id)
);
```

### Optional: Add Template Instantiation Log

```sql
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

## Testing

### Unit Test Example

```java
@Test
public void testDuplicateElement() {
    ICourse course = createTestCourse();
    CourseNode source = createTestNode("assessment-node");
    CourseNode target = createTestParent();
    
    CourseNode result = duplicationService.duplicateElement(
        course, source, target, true, testUser);
    
    assertNotNull(result);
    assertEquals("assessment-node", source.getType());
    assertNotEquals(source.getIdent(), result.getIdent());
}

@Test
public void testSelectionValidation() {
    RepositoryEntry template = createTemplateWithDependencies();
    Set<String> selection = new HashSet<>(Arrays.asList("node1", "node3"));
    
    SelectionValidationResult result = selectiveTemplateService
        .validateSelection(template, selection);
    
    assertTrue(result.hasIssues());
    assertTrue(result.getSuggestedAutoInclusions().contains("node2"));
}
```

## Performance Notes

- **Duplication Time**: O(n) where n = number of nodes in tree
- **Memory**: Proportional to number of nodes and configuration size
- **Database**: Each duplication creates 1+ new DB records
- **Recommendation**: Limit tree duplication to subtrees < 100 nodes for UI responsiveness

## Future Enhancements

1. **Async Duplication**: Support background duplication for large trees
2. **Versioning**: Track duplication chains and versions
3. **Diff View**: Show differences between original and duplicated elements
4. **Rollback**: Undo duplication operations
5. **Templates**: Save duplication patterns as reusable templates

## Troubleshooting

### Issue: Duplication fails for assessment nodes

**Solution**: Ensure `preserveAssessment=true` and assessment module is properly configured.

### Issue: References not fixed after duplication

**Solution**: Set `preserveReferences=true` and ensure reference types are supported.

### Issue: Selection validation shows false warnings

**Solution**: Review dependency detection logic in `getElementDependencies()`.

## Support

For issues or questions about these enhancements, refer to the main [DELIVERY_PACKAGE.md](DELIVERY_PACKAGE.md) documentation.
