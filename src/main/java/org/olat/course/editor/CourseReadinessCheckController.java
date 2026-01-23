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
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.ICourse;
import org.olat.course.publish.AutoFixReport;
import org.olat.course.publish.CourseReadinessCheckerService;
import org.olat.course.publish.CourseReadinessReport;
import org.olat.course.publish.ReadinessCheckType;
import org.olat.course.publish.ReadinessIssue;
import org.olat.course.publish.ReadinessIssueType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Enhancement 4: Course Readiness Checker UI
 * 
 * Controller for displaying course readiness check results
 * 
 * Initial date: January 19, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class CourseReadinessCheckController extends BasicController {
    
    private final VelocityContainer mainVC;
    private final ICourse course;
    private CourseReadinessReport currentReport;
    
    private Link refreshLink;
    private Link autoFixLink;
    private Link closeLink;
    
    @Autowired
    private CourseReadinessCheckerService readinessCheckerService;
    
    public CourseReadinessCheckController(UserRequest ureq, WindowControl wControl, ICourse course) {
        super(ureq, wControl);
        this.course = course;
        
        mainVC = createVelocityContainer("readiness_check");
        
        refreshLink = LinkFactory.createButton("readiness.refresh", mainVC, this);
        refreshLink.setIconLeftCSS("o_icon o_icon_refresh");
        
        autoFixLink = LinkFactory.createButton("readiness.autofix", mainVC, this);
        autoFixLink.setIconLeftCSS("o_icon o_icon_wizard");
        autoFixLink.setVisible(false);
        
        closeLink = LinkFactory.createButton("close", mainVC, this);
        
        // Perform initial check
        performReadinessCheck();
        
        putInitialPanel(mainVC);
    }
    
    private void performReadinessCheck() {
        currentReport = readinessCheckerService.checkReadiness(course);
        updateUI();
    }
    
    private void updateUI() {
        mainVC.contextPut("courseTitle", currentReport.getCourseTitle());
        mainVC.contextPut("ready", currentReport.isReady());
        mainVC.contextPut("readinessScore", currentReport.getReadinessScore());
        mainVC.contextPut("totalIssues", currentReport.getTotalIssueCount());
        mainVC.contextPut("criticalCount", currentReport.getCriticalCount());
        mainVC.contextPut("errorCount", currentReport.getErrorCount());
        mainVC.contextPut("warningCount", currentReport.getWarningCount());
        mainVC.contextPut("checkDuration", currentReport.getCheckDurationMs());
        
        // Prepare issue lists for display
        List<IssueRow> criticalIssues = new ArrayList<>();
        List<IssueRow> errorIssues = new ArrayList<>();
        List<IssueRow> warningIssues = new ArrayList<>();
        List<IssueRow> infoIssues = new ArrayList<>();
        
        for (ReadinessIssue issue : currentReport.getIssues()) {
            IssueRow row = new IssueRow(issue);
            switch (issue.getSeverity()) {
                case CRITICAL:
                    criticalIssues.add(row);
                    break;
                case ERROR:
                    errorIssues.add(row);
                    break;
                case WARNING:
                    warningIssues.add(row);
                    break;
                case INFO:
                    infoIssues.add(row);
                    break;
            }
        }
        
        mainVC.contextPut("criticalIssues", criticalIssues);
        mainVC.contextPut("errorIssues", errorIssues);
        mainVC.contextPut("warningIssues", warningIssues);
        mainVC.contextPut("infoIssues", infoIssues);
        
        // Show auto-fix button if there are fixable issues
        boolean hasAutoFixable = !currentReport.getAutoFixableIssues().isEmpty();
        autoFixLink.setVisible(hasAutoFixable);
        mainVC.contextPut("hasAutoFixable", hasAutoFixable);
        mainVC.contextPut("autoFixableCount", currentReport.getAutoFixableIssues().size());
        
        // Status icon
        String statusIcon;
        String statusClass;
        if (currentReport.isReady()) {
            statusIcon = "o_icon_status_done";
            statusClass = "o_readiness_ready";
        } else if (currentReport.getCriticalCount() > 0) {
            statusIcon = "o_icon_error";
            statusClass = "o_readiness_critical";
        } else {
            statusIcon = "o_icon_warn";
            statusClass = "o_readiness_warning";
        }
        mainVC.contextPut("statusIcon", statusIcon);
        mainVC.contextPut("statusClass", statusClass);
    }
    
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == refreshLink) {
            performReadinessCheck();
            showInfo("readiness.check.refreshed");
        } else if (source == autoFixLink) {
            doAutoFix(ureq);
        } else if (source == closeLink) {
            fireEvent(ureq, Event.DONE_EVENT);
        }
    }
    
    private void doAutoFix(UserRequest ureq) {
        // Get all auto-fixable issue types
        Set<ReadinessIssueType> fixableTypes = EnumSet.noneOf(ReadinessIssueType.class);
        for (ReadinessIssue issue : currentReport.getAutoFixableIssues()) {
            fixableTypes.add(issue.getType());
        }
        
        // Perform auto-fix
        AutoFixReport fixReport = readinessCheckerService.autoFix(course, fixableTypes);
        
        if (fixReport.isFullySuccessful()) {
            showInfo("readiness.autofix.success", String.valueOf(fixReport.getSuccessCount()));
        } else if (fixReport.hasFailures()) {
            showWarning("readiness.autofix.partial", new String[] {
                String.valueOf(fixReport.getSuccessCount()),
                String.valueOf(fixReport.getFailureCount())
            });
        } else {
            showInfo("readiness.autofix.nothing");
        }
        
        // Refresh the report
        performReadinessCheck();
    }
    
    /**
     * Row wrapper for displaying issues in the velocity template
     */
    public static class IssueRow {
        private final ReadinessIssue issue;
        
        public IssueRow(ReadinessIssue issue) {
            this.issue = issue;
        }
        
        public String getType() {
            return issue.getType().name();
        }
        
        public String getMessage() {
            return issue.getMessage();
        }
        
        public String getNodeName() {
            return issue.getNodeName();
        }
        
        public String getNodeId() {
            return issue.getNodeId();
        }
        
        public String getSuggestedFix() {
            return issue.getSuggestedFix();
        }
        
        public boolean isAutoFixable() {
            return issue.isAutoFixable();
        }
        
        public String getCategory() {
            return issue.getCheckCategory() != null ? issue.getCheckCategory().name() : "";
        }
        
        public String getSeverity() {
            return issue.getSeverity().name();
        }
        
        public String getSeverityIcon() {
            switch (issue.getSeverity()) {
                case CRITICAL:
                    return "o_icon_error";
                case ERROR:
                    return "o_icon_error";
                case WARNING:
                    return "o_icon_warn";
                default:
                    return "o_icon_info";
            }
        }
    }
}
