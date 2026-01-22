package org.olat.modules.fo.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.fo.ui.AbuseReportAdminController.AbuseReportRow;

/**
 * Renders the message title and a snippet of the body.
 */
public class AbuseReportMessageCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		if (cellValue instanceof AbuseReportRow) {
			AbuseReportRow reportRow = (AbuseReportRow) cellValue;
			
			// Title
			target.append("<strong>").append(StringHelper.escapeHtml(reportRow.getMessageTitle())).append("</strong>");
			
			// Body snippet
			String body = reportRow.getMessageBody();
			if(StringHelper.containsNonWhitespace(body)) {
				// Strip HTML tags for the snippet
				String text = FilterFactory.getHtmlTagAndDescapingFilter().filter(body);
				if(text.length() > 100) {
					text = text.substring(0, 100) + "...";
				}
				target.append("<div class='o_message_snippet text-muted small'>")
				      .append(StringHelper.escapeHtml(text))
				      .append("</div>");
			}
		}
	}
}
