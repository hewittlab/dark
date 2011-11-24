/*******************************************************************************
 * Copyright (c) 2011  University of Western Australia. All rights reserved.
 * 
 * This file is part of The Ark.
 * 
 * The Ark is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * The Ark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package au.org.theark.lims.web.component.panel.subject;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.theark.core.model.study.entity.Study;
import au.org.theark.lims.model.vo.LimsVO;
import au.org.theark.lims.web.Constants;

/**
 * @author cellis
 * 
 */
public class SubjectDetailForm extends Form<LimsVO> {
	/**
	 * 
	 */
	private static final long		serialVersionUID	= 1L;
	protected static final Logger	log					= LoggerFactory.getLogger(SubjectDetailForm.class);
	protected TextField<String>	subjectUIDTxtFld;
	protected TextField<String>	firstNameTxtFld;
	protected TextField<String>	middleNameTxtFld;
	protected TextField<String>	lastNameTxtFld;
	protected DateTextField			dateOfBirthTxtFld;

	protected Study					study;

	public SubjectDetailForm(String id) {
		super(id);
	}

	public void initialiseDetailForm() {
		subjectUIDTxtFld = new TextField<String>(Constants.SUBJECT_UID);
		subjectUIDTxtFld.setOutputMarkupId(true);
		firstNameTxtFld = new TextField<String>(Constants.PERSON_FIRST_NAME);
		middleNameTxtFld = new TextField<String>(Constants.PERSON_MIDDLE_NAME);
		lastNameTxtFld = new TextField<String>(Constants.PERSON_LAST_NAME);
		dateOfBirthTxtFld = new DateTextField(Constants.PERSON_DOB, au.org.theark.core.Constants.DD_MM_YYYY);
		addDetailFormComponents();
	}

	public void addDetailFormComponents() {
		add(subjectUIDTxtFld);
		add(firstNameTxtFld);
		add(lastNameTxtFld);
		add(dateOfBirthTxtFld);
		setEnabled(false);
	}
}