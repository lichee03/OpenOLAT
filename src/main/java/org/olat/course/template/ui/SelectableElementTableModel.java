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
package org.olat.course.template.ui;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.course.template.model.SelectableCourseElement;

/**
 * Enhancement 1: Selective Template Element Chooser
 * Table model for displaying and selecting course elements
 * 
 * Initial date: January 18, 2026<br>
 * @author OpenOLAT Enhancement Team
 */
public class SelectableElementTableModel extends DefaultFlexiTableDataModel<SelectableCourseElement> 
        implements SortableFlexiTableDataModel<SelectableCourseElement> {
    
    private final Translator translator;
    private final Set<String> selectedIds;
    
    public SelectableElementTableModel(FlexiTableColumnModel columnModel, List<SelectableCourseElement> elements, 
            Set<String> selectedIds, Translator translator) {
        super(columnModel);
        setObjects(elements);
        this.translator = translator;
        this.selectedIds = selectedIds;
    }
    
    @Override
    public void sort(SortKey orderBy) {
        // Implement sorting if needed
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        SelectableCourseElement element = getObject(row);
        return getValueAt(element, col);
    }
    
    public Object getValueAt(SelectableCourseElement element, int col) {
        // Column indices now match what's added to columnsModel (no select column)
        switch (col) {
            case 0: // title
                // Indent based on level
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < element.getLevel(); i++) {
                    sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
                }
                sb.append(element.getShortTitle());
                if (element.isRequired()) {
                    sb.append(" <span class='o_required'>*</span>");
                }
                return sb.toString();
            case 1: // type
                return translateNodeType(element.getNodeType());
            case 2: // level
                return element.getLevel();
            case 3: // resources
                return element.getLearningResources().size();
            case 4: // dependencies
                return element.getDependencies().size();
            default:
                return null;
        }
    }
    
    private String translateNodeType(String nodeType) {
        // Translate node type codes to user-friendly names
        switch (nodeType) {
            case "st": return translator.translate("node.type.structure");
            case "sp": return translator.translate("node.type.singlepage");
            case "bc": return translator.translate("node.type.folder");
            case "tu": return translator.translate("node.type.task");
            case "iqtest": return translator.translate("node.type.test");
            case "iqself": return translator.translate("node.type.selftest");
            case "ms": return translator.translate("node.type.assessment");
            case "en": return translator.translate("node.type.enrollment");
            case "wiki": return translator.translate("node.type.wiki");
            case "blog": return translator.translate("node.type.blog");
            case "fo": return translator.translate("node.type.forum");
            case "gta": return translator.translate("node.type.grouptask");
            default: return nodeType;
        }
    }
    
    public enum ElementCols implements FlexiSortableColumnDef {
        title("selective.element.chooser.table.title"),
        type("selective.element.chooser.table.type"),
        level("selective.element.chooser.table.level"),
        resources("selective.element.chooser.table.resources"),
        dependencies("selective.element.chooser.table.dependencies");
        
        private final String i18nKey;
        
        private ElementCols(String i18nKey) {
            this.i18nKey = i18nKey;
        }
        
        @Override
        public String i18nHeaderKey() {
            return i18nKey;
        }
        
        @Override
        public boolean sortable() {
            return true;
        }
        
        @Override
        public String sortKey() {
            return name();
        }
    }
}
