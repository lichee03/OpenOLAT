
# OpenOLAT Enhancement System (CBSE Assignment)

**OpenOlat** is a web-based e-learning platform for teaching, learning, assessment and communication, an LMS, a learning management system. OpenOlat impresses with its simple and intuitive operation and rich feature set.

A sophisticated modular toolkit provides course authors with a wide range of didactic possibilities. Each OpenOlat installation can be individually extended, adapted to organizational needs, and integrated into existing IT infrastructures. The architecture is designed for minimal resource consumption, scalability and security in order to guarantee high system reliability.

> **Note:** OSGi modularization and runtime integration are handled in a separate repository.

---

## 📦 Enhanced Modules & Responsibilities

| Module                       | Person in Charge        |
|------------------------------|------------------------|
| Course Management Module     | Chai Li Chee           |
| Assessment Management Module | Poh Sharon             |
| Enrollment Module            | Goh Kah Kheng          |
| Scheduling Module            | Al Rubab Ibn Yeahyea   |
| Communication Module         | Eugene See Yi Le       |

---

## 🧠 Module Enhancements

### 1. Course Management Module
| Enhancement                                                  | Description                                                                                                                                                                                    |
| ------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Course Template Library with Selective Element Chooser**   | Enables course creators to preview available course templates and selectively include, exclude, or configure individual sections, elements, and learning resources *before* creating a course. |
| **Smart Course Element Duplication with Dependency Mapping** | Allows duplication of complex or hierarchical course elements while preserving parent–child relationships, configurations, resource references, and assessment logic.                          |
| **Bulk Staff Assignment**                                    | Allows administrators to assign multiple staff members to a course simultaneously, significantly reducing manual setup time and administrative overhead.                                       |
| **Course Readiness Checker**                                 | Automatically validates critical course configurations and dependencies to identify issues and ensure courses are fully prepared prior to publication.                                         |


### 2. Assessment Management Module

The following enhancements were implemented to improve assessment creation, feedback quality, and performance insights:

| Enhancement              | Description                                                                 |
|--------------------------|-----------------------------------------------------------------------------|
| **Assessment Templates** | Allows instructors to reuse predefined assessment structures across courses  |
| **Structured Feedback**  | Supports categorized and rule-based feedback linked to assessment results    |
| **Peer Review**          | Enables students to review and evaluate peer submissions                     |
| **Performance Analysis** | Provides statistical summaries of assessment results and participation       |

These enhancements reduce manual effort and improve assessment consistency without changing core system behavior.

### 3. Enrollment Module
The Enrollment Module adds flexible enrollment management for courses, including automated checks and period-driven actions to manage waiting lists and capacity.

| Enhancement                                                         | Description                                                                                                                             |
|---------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **Automated Prerequisite & Eligibility Checking**                   | Implements rule-based eligibility validation to prevent invalid enrollments (outside enrollment windows, over-capacity, duplicate enrollments).
| **Enrollment Periods with Auto-Actions (Waiting List automation)** | Auto-actions are executed by `EnrollmentPeriodAutoActionService` to process waiting lists, send notifications, and record executed actions to avoid duplicates.|
### 4. Scheduling Module

The Scheduling Module introduces intelligent automation and conflict management to streamline personal and group calendar organization.

| Enhancement | Description |
| :--- | :--- |
| **Smart Quick Add (NLP)** | Implements a "One-Box" entry system where users can type natural language phrases (e.g., "Meeting next Friday at 2pm") to create events instantly without manual date picking. |
| **Real-Time Conflict Detection** | Proactively scans the user's schedule during event creation and warns of overlaps before the event is saved, preventing accidental double-booking. |
| **Personalized "My View" Filtering** | Provides a dedicated view that isolates the user's personal commitments from broad group or course-wide events, reducing calendar clutter. |
| **Smart Visual Cues** | Adds distinct visual indicators (e.g., specific icons or highlights) for different event types, making it easier to distinguish between quick reminders, formal classes, and group meetings at a glance. |


### 5. Communication and Collaboration Module

The following enhancements were implemented to improve forum interaction, community moderation, and knowledge discovery:

| Enhancement                    | Description                                                                                          |
|--------------------------------|------------------------------------------------------------------------------------------------------|
| **Q&A Thread with Best Answer** | Enables thread authors and moderators to mark helpful replies as "Best Answer" for quick knowledge discovery |
| **Report Abuse User Interface** | Provides students/participants with a simple interface to report inappropriate messages directly to moderators |
| **Moderator Dashboard**        | Offers a centralized admin panel for moderators to review, dismiss, or take action on abuse reports |

These enhancements promote community self-moderation, improve forum content quality, and streamline moderator workflows without affecting core forum functionality.
