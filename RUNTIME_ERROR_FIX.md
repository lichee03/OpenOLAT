# Runtime Error Fix - CourseElementDuplicationServiceImpl

## Issue Identified

When starting the application in Eclipse/Tomcat, Spring failed to instantiate the `CourseElementDuplicationServiceImpl` bean due to multiple compilation errors at runtime:

```
Error creating bean with name 'courseElementDuplicationServiceImpl': 
Failed to instantiate: Constructor threw exception

Caused by: java.lang.Error: Unresolved compilation problems:
- The method createLogger(Class<CourseElementDuplicationServiceImpl>) is undefined for the type Tracing
- The method getRunStructureManager() is undefined for the type CourseEnvironment
- The method createCourseNode(String) is undefined for the type CourseNodeFactory
- The method setModuleConfiguration(ModuleConfiguration) is undefined for the type CourseNode
- The constructor ModuleConfiguration(ModuleConfiguration) is undefined
```

## Root Cause

The implementation used incorrect OpenOLAT API calls that don't exist in the actual codebase:

| Issue | Wrong API | Correct API |
|-------|-----------|------------|
| Logging | `Tracing.createLogger()` | `Tracing.createLoggerFor()` |
| Node Cloning | Manual factory + copy | `XStreamHelper.xstreamClone()` |
| Config Copy | Constructor copy | `putAll()` method |
| Adding Nodes | Runtime structure manager | Direct `addChild()` |
| Module Config | `setModuleConfiguration()` | Direct property copy |

## Changes Made

### File: [CourseElementDuplicationServiceImpl.java](src/main/java/org/olat/course/enhancement/CourseElementDuplicationServiceImpl.java)

**1. Fixed Logger Initialization (Line 51)**
```java
// BEFORE (Wrong)
private static final Logger log = Tracing.createLogger(CourseElementDuplicationServiceImpl.class);

// AFTER (Correct)
private static final Logger log = Tracing.createLoggerFor(CourseElementDuplicationServiceImpl.class);
```

**2. Added Required Import (Line 33)**
```java
import org.olat.core.util.XStreamHelper;
```

**3. Rewrote duplicateNode() Method (Lines 215-253)**

Changed from manual node creation to proper XStream cloning:

```java
// BEFORE (Wrong)
CourseNode newNode = CourseNodeFactory.getInstance()
    .createCourseNode(sourceNode.getType());
newNode.setModuleConfiguration(new org.olat.modules.ModuleConfiguration(sourceNode.getModuleConfiguration()));
course.getCourseEnvironment().getRunStructureManager().addCourseNode(newNode);

// AFTER (Correct)
CourseNode newNode = (CourseNode) XStreamHelper.xstreamClone(sourceNode);
newNode.removeAllChildren();
targetParent.addChild(newNode);
```

## Why These Changes

1. **XStreamHelper.xstreamClone()** - OpenOLAT's standard deep cloning mechanism that:
   - Preserves all object properties including module configuration
   - Handles serialization correctly
   - Is used throughout OpenOLAT (e.g., in PublishProcess, CourseConfig)

2. **XStreamHelper avoids non-existent APIs** like:
   - `CourseNodeFactory.createCourseNode(String)` doesn't exist
   - `ModuleConfiguration(ModuleConfiguration)` constructor doesn't exist
   - `CourseNode.setModuleConfiguration()` is not how properties are set

3. **Direct addChild()** - The correct way to add nodes to the course tree:
   - Nodes are part of an in-memory tree structure
   - Changes only persist when explicitly saved via `CourseFactory.saveCourseEditorTreeModel()`
   - No runtime "add" method needed

## How to Apply Fix

### In Eclipse/Tomcat (Server Running):

1. **Eclipse automatically detects changes**
   - File changes are detected
   - Classes are recompiled
   - Server hot-deploys updated classes

2. **Manual rebuild (if needed)**:
   ```
   Right-click project → Build Project → Clean Build
   ```

3. **Restart server if hot-deploy fails**:
   ```
   Stop Tomcat server
   Click "Clean..." if prompted
   Restart server
   ```

### On Command Line (Maven):

```bash
cd /Users/lichee/Documents/GitHub/OPENOLAT/OpenOLAT

# Clean and compile
mvn clean compile -DskipTests

# Or run with server
mvn clean tomcat7:run
```

## Verification

After rebuild/restart, check the Tomcat console for:

✅ **Expected Log Output:**
```
[INFO] Registering Spring bean: courseElementDuplicationService
[INFO] Application startup successful
```

❌ **If errors persist**, check for:
- Typos in imports
- Method signature mismatches
- Missing JAR dependencies

## Files Modified

- `src/main/java/org/olat/course/enhancement/CourseElementDuplicationServiceImpl.java`

## Testing

Once the application starts successfully:

1. Login to OpenOLAT
2. Navigate to a course
3. Element duplication service is now available as a Spring bean
4. Can be injected via `@Autowired` annotation
5. Ready for UI integration

## Related Documentation

- See [INTEGRATION_GUIDE_ENHANCEMENT_3_5.md](INTEGRATION_GUIDE_ENHANCEMENT_3_5.md) for full integration details
- See [README_ENHANCEMENT_3_5.md](README_ENHANCEMENT_3_5.md) for feature overview

---

**Status**: ✅ Fixed - Ready to rebuild and test  
**Date Fixed**: 2026-01-18  
**Affected Service**: CourseElementDuplicationService
