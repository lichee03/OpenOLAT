# Quick Reference: Enhancements 3 & 5

## Enhancement 3: Smart Course Element Duplication

### Service Interface
```java
public interface CourseElementDuplicationService {
    CourseNode duplicateElement(ICourse course, CourseNode sourceNode, 
        CourseNode targetParent, boolean preserveAssessment, Identity executor);
    
    DuplicationResult duplicateElementTree(ICourse course, CourseNode sourceNode,
        CourseNode targetParent, boolean preserveAssessment, 
        boolean preserveReferences, Identity executor);
    
    DuplicationResult duplicateSelectedElements(ICourse course, 
        List<String> selectedNodeIds, CourseNode targetParent,
        boolean preserveAssessment, Identity executor);
    
    Map<String, String> getNodeIdMapping(ICourse course);
    boolean canDuplicate(String nodeType);
    List<String> getNonDuplicableNodeTypes();
}
```

### Quick Usage
```java
// Inject service
@Autowired
private CourseElementDuplicationService duplicationService;

// Duplicate single element
CourseNode newNode = duplicationService.duplicateElement(
    course, sourceNode, parentNode, true, executor);

// Duplicate with children
DuplicationResult result = duplicationService.duplicateElementTree(
    course, sourceNode, parentNode, true, true, executor);

// Get mapping
Map<String, String> mapping = result.getNodeIdMapping();
```

### Result Object
```java
DuplicationResult {
    boolean success;
    String errorMessage;
    int elementsProcessed;
    int elementsFailed;
    Map<String, String> nodeIdMapping;  // old ID -> new ID
    List<String> warnings;
    long processingTimeMs;
}
```

---

## Enhancement 5: Selective Template Element Chooser

### Service Interface
```java
public interface SelectiveTemplateInstantiationService {
    List<TemplateElement> getTemplateElements(RepositoryEntry templateEntry);
    List<TemplateResource> getTemplateResources(RepositoryEntry templateEntry);
    
    RepositoryEntry createCourseWithSelectiveElements(
        RepositoryEntry templateEntry, String courseName, String courseDescription,
        Set<String> selectedNodeIds, Set<String> selectedResourceIds,
        boolean deepCopyResources, Identity creator);
    
    SelectionPreview previewSelection(RepositoryEntry templateEntry,
        Set<String> selectedNodeIds, Set<String> selectedResourceIds);
    
    List<String> getElementDependencies(RepositoryEntry templateEntry, String nodeId);
    SelectionValidationResult validateSelection(RepositoryEntry templateEntry,
        Set<String> selectedNodeIds);
    
    long estimateResourceSize(RepositoryEntry templateEntry,
        Set<String> selectedResourceIds);
}
```

### Quick Usage
```java
// Inject service
@Autowired
private SelectiveTemplateInstantiationService selectiveService;

// Get available elements
List<TemplateElement> elements = selectiveService.getTemplateElements(template);

// Get available resources
List<TemplateResource> resources = selectiveService.getTemplateResources(template);

// Validate user selection
SelectionValidationResult validation = selectiveService.validateSelection(
    template, selectedNodeIds);

// Preview before creating
SelectionPreview preview = selectiveService.previewSelection(
    template, selectedNodeIds, selectedResourceIds);

// Create course
RepositoryEntry newCourse = selectiveService.createCourseWithSelectiveElements(
    template, "Course Name", "Description", 
    selectedNodeIds, selectedResourceIds, true, executor);
```

### Model Objects
```java
TemplateElement {
    String nodeId;
    String title;
    String nodeType;
    int level;              // depth in hierarchy
    String parentNodeId;
    boolean selected;
}

TemplateResource {
    String resourceId;
    String name;
    String type;
    long size;
    String linkedNodeId;
    boolean selected;
}

SelectionPreview {
    int totalElementsSelected;
    int totalResourcesSelected;
    long estimatedSize;
    List<String> warnings;
    List<String> dependencies;
}

SelectionValidationResult {
    boolean valid;
    List<String> errors;
    List<String> warnings;
    List<String> suggestedAutoInclusions;
}
```

---

## UI Controller

### Class: SelectiveTemplateInstantiateController

```java
// Launch controller
SelectiveTemplateInstantiateController controller = 
    new SelectiveTemplateInstantiateController(ureq, wControl, templateEntry);

// Listen for event
listenTo(controller);

// Handle selection event
if (event instanceof SelectiveTemplateInstantiateController.SelectionEvent) {
    SelectionEvent selEvent = (SelectionEvent) event;
    Set<String> elements = selEvent.getSelectedNodeIds();
    Set<String> resources = selEvent.getSelectedResourceIds();
}
```

---

## Spring Configuration

### Auto-Registration (via @Service annotations)
Both implementations are already annotated with `@Service`, so they'll be auto-registered when component scanning is enabled.

### Manual Registration (if needed)
```java
@Configuration
public class EnhancementConfig {
    
    @Bean
    public CourseElementDuplicationService duplicationService() {
        return new CourseElementDuplicationServiceImpl();
    }
    
    @Bean
    public SelectiveTemplateInstantiationService selectiveService(
            RepositoryManager repositoryManager,
            CourseElementDuplicationService duplicationService) {
        return new SelectiveTemplateInstantiationServiceImpl(
            repositoryManager, duplicationService);
    }
}
```

---

## i18n Keys

### Enhancement 3
- `button.duplicate.element` - Button label
- `button.duplicate.tree` - Button label
- `elements.duplicated` - Success message
- `error.duplicating.elements` - Error message

### Enhancement 5
- `select.course.elements` - Panel title
- `select.resources` - Panel title
- `selection.preview` - Section heading
- `elements.selected` - Preview stat
- `estimated.size` - Preview stat

---

## Common Integration Points

### 1. Course Duplication Dialog
```java
courseRunController.getCourseFolderController()
    .getMenuController()
    .addCommand("duplicate", new ActionListener() {
        showDuplicateDialog();
    });
```

### 2. Template Wizard
Add as Step 1.5:
```java
// In CreateCourseWizardController
addStep(new SelectiveTemplateInstantiateStep());
```

### 3. Context Menu
```java
CourseNode.addContextMenuAction("duplicate", duplicationService::duplicateElement);
```

---

## Database Tables (Optional)

### Tracking Duplication
```sql
o_course_duplication_log {
    id, course_id, source_node_id, target_node_id,
    duplication_type, created_by, created_date, 
    processing_time_ms, success, error_message
}
```

### Tracking Template Usage
```sql
o_template_instantiation_log {
    id, template_id, created_course_id, selected_elements,
    selected_resources, deep_copy, created_by, created_date
}
```

---

## Non-Duplicable Node Types
By default, these node types cannot be duplicated:
- `surveystart`
- `surveycourse`

Check via:
```java
List<String> nonDup = duplicationService.getNonDuplicableNodeTypes();
boolean canDup = duplicationService.canDuplicate("assessment");
```

---

## Performance Tips

| Operation | Typical Time | Nodes Recommended |
|-----------|------------|------------------|
| Single element | < 100ms | N/A |
| Tree (shallow) | 100-500ms | < 10 children |
| Tree (deep) | 500-2000ms | < 50 total |
| Bulk (100 elements) | 1-5s | 100 max |

For larger operations, consider async processing.

---

## Error Handling

```java
DuplicationResult result = duplicationService.duplicateElementTree(...);
if (!result.isSuccess()) {
    // result.getErrorMessage() - main error
    // result.getElementsFailed() - count of failed nodes
    // result.getWarnings() - list of warnings
    log.error("Duplication failed: " + result.getErrorMessage());
}
```

---

## Testing Checklist

- [ ] Single element duplication preserves configuration
- [ ] Tree duplication creates proper hierarchy
- [ ] ID mapping is bidirectional
- [ ] Assessment configs are preserved when requested
- [ ] Non-duplicable types are rejected
- [ ] Template elements load correctly
- [ ] Resources are discovered
- [ ] Selection validation detects dependencies
- [ ] Preview calculates correct size
- [ ] Course creation succeeds with selected elements
- [ ] UI elements render correctly
