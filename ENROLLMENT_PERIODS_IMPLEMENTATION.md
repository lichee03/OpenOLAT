# Enrollment Periods with Auto-Actions Implementation

## Overview

Complete Spring-based implementation of enrollment period management with automated actions for the OpenOLAT enrollment system.

## Features Implemented

### 1. **Enrollment Period Configuration**

- Start date (`CONF_ENROLLMENT_BEGIN`)
- End date (`CONF_ENROLLMENT_END`)
- Auto-process waitlist on period start (`CONF_AUTO_PROCESS_WAITLIST_START`)
- Auto-process waitlist on period end (`CONF_AUTO_PROCESS_WAITLIST_END`)
- Send notifications on period start (`CONF_NOTIFY_ON_PERIOD_START`)
- Send notifications on period end (`CONF_NOTIFY_ON_PERIOD_END`)

### 2. **Time-Based Validation**

- **TimeRestrictionRule** now actively validates enrollment periods
- Blocks enrollment before start date
- Blocks enrollment after end date
- Warns users when enrollment closes within 24 hours

### 3. **Automated Actions**

- **EnrollmentPeriodAutoActionService**: Executes auto-actions
  - Processes waitlist automatically
  - Sends notifications to participants
  - Tracks execution to prevent duplicates

### 4. **Scheduled Monitoring**

- **EnrollmentPeriodScheduler**: Runs every 5 minutes
  - Checks all enrollment nodes across all courses
  - Detects period start/end transitions
  - Triggers appropriate auto-actions
  - 10-minute grace period for detection

### 5. **Action Tracking**

- **EnrollmentPeriodAction** entity: Persists executed actions
- **EnrollmentPeriodActionDAO**: Database operations
- Prevents duplicate execution of auto-actions
- Maintains audit trail of all automated actions

### 6. **Waitlist Management**

- New method in EnrollmentManager: `moveFromWaitingListToParticipant()`
- Automatically moves users from waitlist when space becomes available
- Respects group capacity limits
- Updates enrollment properties correctly

## Files Created/Modified

### New Files Created:

1. `EnrollmentPeriodAction.java` - JPA entity for action tracking
2. `EnrollmentPeriodActionDAO.java` - Database access object
3. `EnrollmentPeriodAutoActionService.java` - Auto-action execution service
4. `EnrollmentPeriodScheduler.java` - Spring scheduled task
5. `EnrollmentSchedulingConfig.java` - Spring scheduling configuration
6. `alter_enrollment_period_actions.sql` - Database schema

### Modified Files:

1. `ENCourseNode.java` - Added 6 new configuration constants
2. `TimeRestrictionRule.java` - Implemented actual date validation
3. `EnrollmentManager.java` - Added waitlist processing method

## Database Schema

```sql
CREATE TABLE o_en_period_action (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creationdate DATETIME NOT NULL,
    course_id BIGINT NOT NULL,
    course_node_id VARCHAR(64) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    trigger_type VARCHAR(32) NOT NULL,
    executed_date DATETIME NOT NULL,
    success BOOLEAN NOT NULL,
    message VARCHAR(2048),
    affected_users INT
);
```

## How It Works

### Period Start Flow:

1. **Scheduler detects** enrollment period has started (within last 10 minutes)
2. **Checks tracking** - Has start action already been executed?
3. **Executes auto-actions**:
   - If `CONF_AUTO_PROCESS_WAITLIST_START` = true → Process waitlist
   - If `CONF_NOTIFY_ON_PERIOD_START` = true → Send notifications
4. **Records action** in database to prevent re-execution

### Period End Flow:

1. **Scheduler detects** enrollment period has ended
2. **Checks tracking** - Has end action already been executed?
3. **Executes auto-actions**:
   - If `CONF_AUTO_PROCESS_WAITLIST_END` = true → Process waitlist
   - If `CONF_NOTIFY_ON_PERIOD_END` = true → Send notifications
4. **Records action** in database

### Waitlist Processing:

1. Get all groups configured in enrollment node
2. For each group:
   - Get users on waitlist
   - Check group capacity
   - Move users to participants if space available
   - Update enrollment properties
3. Return count of users successfully moved

## Spring Integration

### Scheduling Configuration:

```java
@Configuration
@EnableScheduling
public class EnrollmentSchedulingConfig {
    // Enables @Scheduled annotations
}
```

### Scheduled Task:

```java
@Service
public class EnrollmentPeriodScheduler {
    @Scheduled(fixedRate = 300000, initialDelay = 60000)
    public void checkEnrollmentPeriods() {
        // Check every 5 minutes
    }
}
```

### Dependency Injection:

- All services use `@Service` annotation
- Dependencies injected via `@Autowired`
- Spring manages component lifecycle

## Next Steps (UI Integration)

To make this fully functional, you need to:

1. **Update ENEditGroupAreaFormController.java**:

   ```java
   // Add date choosers
   private DateChooser enrollmentBeginEl;
   private DateChooser enrollmentEndEl;

   // Add checkboxes for auto-actions
   private MultipleSelectionElement autoProcessWaitlistStart;
   private MultipleSelectionElement autoProcessWaitlistEnd;
   private MultipleSelectionElement notifyOnStart;
   private MultipleSelectionElement notifyOnEnd;
   ```

2. **Create Velocity Template** for the UI form

3. **Test the Implementation**:
   - Set enrollment dates for a test course
   - Enable auto-actions
   - Wait for scheduler to trigger
   - Check logs and database for executed actions

## Configuration Example

In the course editor, administrators can configure:

- **Enrollment Opens**: January 25, 2026 at 9:00 AM
- **Enrollment Closes**: February 1, 2026 at 5:00 PM
- **Auto-process waitlist when opening**: ✓ Enabled
- **Auto-process waitlist when closing**: ✓ Enabled
- **Send notification when opening**: ✓ Enabled
- **Send notification when closing**: ✓ Enabled

## Logging

All actions are logged with INFO level:

- Period start/end detection
- Auto-action execution
- Waitlist processing results
- Notification sending

Errors logged with ERROR level:

- Failed auto-actions
- Database errors
- Scheduling failures

## Performance Considerations

- Scheduler runs every 5 minutes (configurable)
- Uses 10-minute grace period to catch transitions
- Tracks executed actions to prevent duplicates
- Batch processes multiple courses efficiently
- Database indexed on critical fields

## Security

- Auto-actions execute with system privileges
- Action tracking provides audit trail
- All operations logged
- Respects group capacity limits
- Maintains enrollment property consistency

## Extensibility

Easy to add new auto-actions:

1. Add new `ActionType` enum value
2. Implement logic in `EnrollmentPeriodAutoActionService`
3. Add configuration constant in `ENCourseNode`
4. Update UI to expose new option

## Summary

✅ **Complete Spring-based implementation**
✅ **Scheduled monitoring every 5 minutes**
✅ **Automated waitlist processing**
✅ **Action tracking and audit trail**
✅ **Time-based enrollment validation**
✅ **Extensible architecture**
✅ **Database persistence**

🔧 **TODO: UI Integration** - Add form fields to enrollment node editor
