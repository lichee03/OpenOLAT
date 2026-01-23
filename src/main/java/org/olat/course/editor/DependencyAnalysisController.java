/**
 * OpenOLAT - Online Learning and Training
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeDependencyInfo.DependencyType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller for analyzing and displaying course node dependencies
 * before duplication. Shows what resources/nodes the selected node
 * depends on and what depends on it.
 * 
 * @author OpenOLAT
 */
public class DependencyAnalysisController extends BasicController {

    public static final Event COPY_WITH_DEPS_EVENT = new Event("copy-with-deps");
    public static final Event COPY_ONLY_EVENT = new Event("copy-only");
    
    private final VelocityContainer mainVC;
    private final Link copyWithDepsButton;
    private final Link copyOnlyButton;
    private final Link cancelButton;
    
    private final ICourse course;
    private final CourseEditorTreeNode sourceNode;
    private final Map<String, NodeDependencyInfo> dependencyMap;
    private final NodeDependencyInfo nodeInfo;
    
    @Autowired
    private DependencyMapperService dependencyMapperService;
    
    public DependencyAnalysisController(UserRequest ureq, WindowControl wControl,
            ICourse course, CourseEditorTreeNode sourceNode) {
        super(ureq, wControl);
        this.course = course;
        this.sourceNode = sourceNode;
        
        mainVC = createVelocityContainer("dependency_analysis");
        
        // Analyze dependencies
        this.dependencyMap = dependencyMapperService.analyzeDependencies(course);
        this.nodeInfo = dependencyMap.get(sourceNode.getIdent());
        
        // Set up UI
        CourseNode cn = sourceNode.getCourseNode();
        mainVC.contextPut("nodeTitle", cn.getShortTitle());
        mainVC.contextPut("nodeType", cn.getType());
        mainVC.contextPut("nodeId", cn.getIdent());
        
        // Get dependencies info
        boolean hasDependencies = false;
        boolean hasReferencedBy = false;
        
        if (nodeInfo != null) {
            // What this node depends on (getDependsOn)
            Set<String> dependsOnIds = nodeInfo.getDependsOn();
            if (dependsOnIds != null && !dependsOnIds.isEmpty()) {
                hasDependencies = true;
                List<DependencyDisplayInfo> dependencies = new ArrayList<>();
                for (String id : dependsOnIds) {
                    DependencyDisplayInfo info = createDependencyDisplayInfo(id);
                    if (info != null) {
                        dependencies.add(info);
                    }
                }
                mainVC.contextPut("dependencies", dependencies);
            }
            
            // What references this node (getDependedBy)
            Set<String> dependedByIds = nodeInfo.getDependedBy();
            if (dependedByIds != null && !dependedByIds.isEmpty()) {
                hasReferencedBy = true;
                List<DependencyDisplayInfo> referencedBy = new ArrayList<>();
                for (String id : dependedByIds) {
                    DependencyDisplayInfo info = createReferencedByDisplayInfo(id);
                    if (info != null) {
                        referencedBy.add(info);
                    }
                }
                mainVC.contextPut("referencedBy", referencedBy);
            }
            
            // Dependency types summary
            List<DependencyType> types = nodeInfo.getDependencyTypes();
            if (types != null && !types.isEmpty()) {
                List<String> typeDescriptions = new ArrayList<>();
                for (DependencyType type : types) {
                    typeDescriptions.add(getDependencyTypeDescription(type));
                }
                mainVC.contextPut("dependencyTypes", typeDescriptions);
            }
        }
        
        mainVC.contextPut("hasDependencies", hasDependencies);
        mainVC.contextPut("hasReferencedBy", hasReferencedBy);
        mainVC.contextPut("hasAnyDependencies", hasDependencies || hasReferencedBy);
        
        // Validation result
        if (hasDependencies || hasReferencedBy) {
            DependencyValidationResult validation = dependencyMapperService.canDeleteNode(course, sourceNode.getIdent());
            mainVC.contextPut("canCopy", validation.isValid());
            if (validation.getWarnings() != null && !validation.getWarnings().isEmpty()) {
                mainVC.contextPut("warnings", validation.getWarnings());
            }
            if (validation.getErrors() != null && !validation.getErrors().isEmpty()) {
                mainVC.contextPut("errors", validation.getErrors());
            }
        } else {
            mainVC.contextPut("canCopy", true);
        }
        
        // Buttons
        copyWithDepsButton = LinkFactory.createButton("dependency.copy.with.deps", mainVC, this);
        copyWithDepsButton.setElementCssClass("btn btn-primary");
        copyWithDepsButton.setIconLeftCSS("o_icon o_icon_copy");
        copyWithDepsButton.setVisible(hasDependencies);
        
        copyOnlyButton = LinkFactory.createButton("dependency.copy.only", mainVC, this);
        copyOnlyButton.setElementCssClass("btn btn-default");
        copyOnlyButton.setIconLeftCSS("o_icon o_icon_copy");
        
        cancelButton = LinkFactory.createButton("cancel", mainVC, this);
        cancelButton.setElementCssClass("btn btn-default");
        
        putInitialPanel(mainVC);
    }
    
    private DependencyDisplayInfo createDependencyDisplayInfo(String nodeId) {
        CourseEditorTreeNode targetNode = course.getEditorTreeModel().getCourseEditorNodeById(nodeId);
        if (targetNode == null) {
            return null;
        }
        CourseNode cn = targetNode.getCourseNode();
        
        // Find the dependency types for this specific reference
        String typeStr = getDependencyTypesSummary(nodeInfo);
        
        return new DependencyDisplayInfo(
            cn.getIdent(),
            cn.getShortTitle(),
            cn.getType(),
            typeStr,
            getIconCssClass(cn.getType())
        );
    }
    
    private DependencyDisplayInfo createReferencedByDisplayInfo(String nodeId) {
        CourseEditorTreeNode refNode = course.getEditorTreeModel().getCourseEditorNodeById(nodeId);
        if (refNode == null) {
            return null;
        }
        CourseNode cn = refNode.getCourseNode();
        
        NodeDependencyInfo refInfo = dependencyMap.get(nodeId);
        String typeStr = "";
        if (refInfo != null) {
            typeStr = getDependencyTypesSummary(refInfo);
        }
        
        return new DependencyDisplayInfo(
            cn.getIdent(),
            cn.getShortTitle(),
            cn.getType(),
            typeStr,
            getIconCssClass(cn.getType())
        );
    }
    
    private String getDependencyTypesSummary(NodeDependencyInfo info) {
        List<DependencyType> types = info.getDependencyTypes();
        if (types == null || types.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(getDependencyTypeShort(types.get(i)));
        }
        return sb.toString();
    }
    
    private String getDependencyTypeDescription(DependencyType type) {
        return switch (type) {
            case SHARED_RESOURCE -> translate("dependency.type.learning.resource");
            case VISIBILITY_CONDITION -> translate("dependency.type.visibility");
            case PREREQUISITE -> translate("dependency.type.prerequisite");
            case SCORE_CALCULATION -> translate("dependency.type.scoring");
            case DATA_REFERENCE -> translate("dependency.type.content");
            case ASSESSMENT_DEPENDENCY -> translate("dependency.type.access");
            case STRUCTURE_PARENT -> translate("dependency.type.prerequisite");
        };
    }
    
    private String getDependencyTypeShort(DependencyType type) {
        return switch (type) {
            case SHARED_RESOURCE -> translate("dependency.type.short.resource");
            case VISIBILITY_CONDITION -> translate("dependency.type.short.visibility");
            case PREREQUISITE -> translate("dependency.type.short.prerequisite");
            case SCORE_CALCULATION -> translate("dependency.type.short.scoring");
            case DATA_REFERENCE -> translate("dependency.type.short.content");
            case ASSESSMENT_DEPENDENCY -> translate("dependency.type.short.access");
            case STRUCTURE_PARENT -> translate("dependency.type.short.prerequisite");
        };
    }
    
    private String getIconCssClass(String nodeType) {
        return "o_icon o_" + nodeType + "_icon";
    }
    
    public CourseEditorTreeNode getSourceNode() {
        return sourceNode;
    }
    
    public boolean shouldIncludeDependencies() {
        return nodeInfo != null && nodeInfo.getDependsOn() != null && !nodeInfo.getDependsOn().isEmpty();
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == copyWithDepsButton) {
            fireEvent(ureq, COPY_WITH_DEPS_EVENT);
        } else if (source == copyOnlyButton) {
            fireEvent(ureq, COPY_ONLY_EVENT);
        } else if (source == cancelButton) {
            fireEvent(ureq, Event.CANCELLED_EVENT);
        }
    }
    
    /**
     * Display info class for dependencies shown in the UI
     */
    public static class DependencyDisplayInfo {
        private final String nodeId;
        private final String title;
        private final String type;
        private final String dependencyType;
        private final String iconCss;
        
        public DependencyDisplayInfo(String nodeId, String title, String type, 
                String dependencyType, String iconCss) {
            this.nodeId = nodeId;
            this.title = title;
            this.type = type;
            this.dependencyType = dependencyType;
            this.iconCss = iconCss;
        }
        
        public String getNodeId() {
            return nodeId;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getType() {
            return type;
        }
        
        public String getDependencyType() {
            return dependencyType;
        }
        
        public String getIconCss() {
            return iconCss;
        }
    }
}
