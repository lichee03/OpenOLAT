package org.olat.core.commons.services.notifications.ui;

import java.util.Date;

import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;

/**
 * Initial date: Mär 23, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class NotificationSubscriptionRow {

	private Long key;
	private String section;
	private String addDesc;
	private Publisher publisher;
	private FormToggle statusToggle;
	private FormLink learningResource;
	private FormLink subRes;
	private Date creationDate;
	private Date lastEmail;
	private FormLink deleteLink;
	private SingleSelection frequencySelection;

	public NotificationSubscriptionRow(String section, Publisher publisher, FormLink learningResource,
			FormLink subRes, String addDesc, FormToggle statusToggle, Date creationDate,
			Date lastEmail, FormLink deleteLink, Long subKey, SingleSelection frequencySelection) {
		this.key = subKey;
		this.section = section;
		this.learningResource = learningResource;
		this.subRes = subRes;
		this.addDesc = addDesc;
		this.publisher = publisher;
		this.statusToggle = statusToggle;
		this.creationDate = creationDate;
		this.lastEmail = lastEmail;
		this.deleteLink = deleteLink;
		this.frequencySelection = frequencySelection;
	}

	public Long getKey() {
		return key;
	}

	public String getSection() {
		return section;
	}
	
	public Publisher getPublisher() {
		return publisher;
	}

	public FormLink getLearningResource() {
		return learningResource;
	}

	public FormLink getSubRes() {
		return subRes;
	}

	public String getAddDesc() {
		return addDesc;
	}

	public FormToggle getStatusToggle() {
		return statusToggle;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastEmail() {
		return lastEmail;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}

	public SingleSelection getFrequencySelection() {
		return frequencySelection;
	}
}
