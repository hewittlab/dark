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
package au.org.theark.core.web.component.customfieldupload;

import org.apache.shiro.SecurityUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.theark.core.model.study.entity.ArkModule;
import au.org.theark.core.model.study.entity.Payload;
import au.org.theark.core.model.study.entity.Upload;
import au.org.theark.core.service.IArkCommonService;
import au.org.theark.core.util.ByteDataResourceRequestHandler;
import au.org.theark.core.web.component.button.ArkDownloadTemplateButton;
import au.org.theark.core.web.component.panel.ConfirmationAnswer;
import au.org.theark.core.web.component.panel.YesNoPanel;

@SuppressWarnings( { "unchecked" })
public class SearchResultListPanel extends Panel {


	@SpringBean(name = au.org.theark.core.Constants.ARK_COMMON_SERVICE)
	private IArkCommonService<Void>			iArkCommonService;

	private static final long	serialVersionUID	= 6069001768176246767L;
	private transient Logger	log					= LoggerFactory.getLogger(SearchResultListPanel.class);
	private ModalWindow 			confirmModal;
	private ConfirmationAnswer		confirmationAnswer;
	private final String modalText = "<p align='center'>You are about to delete the uploaded file </p>"
			+ "</br>"
			+"<p align='center'><b>*</b> (Attachment ID: <b>#</b>).</p>"
			+ "</br>"
			+ "<p align='center'> Data that were uploaded from this file will remain in The Ark; only the record of the upload process will be deleted.</p>"
			+ "</br>"
			+ "<p align='center'>Do you wish to continue?</p>"
			+ "</br>";
	private SearchResultListPanel me;
	
	/**
	 * 
	 * @param id
	 */
	public SearchResultListPanel(String id) {
		super(id);
		this.setOutputMarkupId(true);
		me=this;
		Long sessionModuleId = (Long) SecurityUtils.getSubject().getSession().getAttribute(au.org.theark.core.Constants.ARK_MODULE_KEY);
		ArkModule arkModule=iArkCommonService.getArkModuleById(sessionModuleId);
		
		//Refer ARK-1386 programmatically pick the header details according to the module.     
		ArkDownloadTemplateButton downloadFieldTemplateButton = new ArkDownloadTemplateButton("downloadTemplateField",
				(arkModule.getName().equals(au.org.theark.core.Constants.ARK_MODULE_LIMS))?"LIMSCustomFieldUpload":"SubjectCustomFieldUpload",changeCustomFieldHeaderWithModuleName(arkModule, au.org.theark.core.Constants.CUSTOM_FIELD_UPLOAD_HEADER)) {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				this.error("An unexpected error occurred. The download custom field template request could not be processed.");
			}
		};
		ArkDownloadTemplateButton downloadCategoryTemplateButton = new ArkDownloadTemplateButton("downloadTemplateCategory", "CustomFieldCategoryUpload", au.org.theark.core.Constants.CUSTOM_FIELD_CATEGORY_UPLOAD_HEADER) {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				this.error("An unexpected error occurred. The download custom field category request could not be processed.");
			}
		};
		initConfirmModel();
		add(downloadFieldTemplateButton);
		add(downloadCategoryTemplateButton);
	}

	/**
	 * 
	 * @param iModel
	 * @return the pageableListView of Upload
	 */
	public PageableListView<Upload> buildPageableListView(IModel iModel) {
		PageableListView<Upload> sitePageableListView = new PageableListView<Upload>(Constants.RESULT_LIST, iModel, iArkCommonService.getRowsPerPage()) {

			private static final long	serialVersionUID	= 1L;

			@Override
			protected void populateItem(final ListItem<Upload> item) {
				Upload upload = item.getModelObject();

				// The ID
				if (upload.getId() != null) {
					// Add the id component here
					item.add(new Label(Constants.UPLOADVO_UPLOAD_ID, upload.getId().toString()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_ID, ""));
				}

				// / The filename
				if (upload.getFilename() != null) {
					// Add the id component here
					item.add(new Label(Constants.UPLOADVO_UPLOAD_FILENAME, upload.getFilename()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_FILENAME, ""));
				}

				// File Format
				if (upload.getFileFormat() != null) {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_FILE_FORMAT, upload.getFileFormat().getName()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_FILE_FORMAT, ""));
				}

				// UserId
				if (upload.getUserId() != null) {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_USER_ID, upload.getUserId()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_USER_ID, ""));
				}

				// Start time
				if (upload.getStartTime() != null) {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_START_TIME, upload.getStartTime().toString()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_START_TIME, ""));
				}

				// Finish time
				if (upload.getFinishTime() != null) {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_FINISH_TIME, upload.getFinishTime().toString()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_FINISH_TIME, ""));
				}
				
				if (upload.getUploadStatus() != null && upload.getUploadStatus().getShortMessage()!=null) {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_UPLOAD_STATUS_NAME, upload.getUploadStatus().getShortMessage()));
				}
				else {
					item.add(new Label(Constants.UPLOADVO_UPLOAD_UPLOAD_STATUS_NAME, ""));
				}


				// Download file link button
				item.add(buildDownloadButton(upload));
				
				// Download upload report button
				item.add(buildDownloadReportButton(upload));
				
				item.add(buildDeleteUploadButton(upload));

				// For the alternative stripes
				item.add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
					private static final long	serialVersionUID	= 1L;

					@Override
					public String getObject() {
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}
		};
		return sitePageableListView;
	}

	private AjaxButton buildDownloadButton(final Upload upload) {
		AjaxButton ajaxButton = new AjaxButton(au.org.theark.core.Constants.DOWNLOAD_FILE) {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				Payload payload  = iArkCommonService.getPayloadForUpload(upload);
				byte[] data = payload.getPayload();
				getRequestCycle().scheduleRequestHandlerAfterCurrent(new ByteDataResourceRequestHandler("text/csv", data, upload.getFilename()));
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				log.error("onError called when buildDownloadButton pressed");
			};
		};
		ajaxButton.setDefaultFormProcessing(false);
		//TODO TEST...payload should never be nulll...but test.
		//if (upload.getPayload() == null) {
		//ajaxButton.setVisible(false);
		//}

		return ajaxButton;
	}

	/**
	 * 
	 * @param upload
	 * @return
	 */
	private AjaxButton buildDownloadReportButton(final Upload upload) {
		AjaxButton ajaxButton = new AjaxButton(au.org.theark.core.Constants.DOWNLOAD_REPORT) {
			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				byte[] data = upload.getUploadReport();
				getRequestCycle().scheduleRequestHandlerAfterCurrent(new ByteDataResourceRequestHandler("text/plain", data, "uploadReport" + upload.getId() + ".txt"));
			}
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				log.error("onError called when buildDownloadReportButton pressed");
			};
		};
		ajaxButton.setVisible(true);
		ajaxButton.setDefaultFormProcessing(false);
		if (upload.getUploadReport() == null) {
			ajaxButton.setVisible(false);
		}
		return ajaxButton;
	}
	/**
	 * 
	 * @param upload
	 * @return
	 */
	private AjaxButton buildDeleteUploadButton(Upload upload){
		AjaxButton ajaxButton = new AjaxButton(au.org.theark.core.Constants.DELETE_UPLOAD){
			private static final long serialVersionUID = 1L;
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				updateModelAndVarifyForDeleteUpload(upload);
				confirmModal.show(target);
			}
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				log.error("onError called when buildDeleteUploadButton pressed");
			};
		};
		ajaxButton.setDefaultFormProcessing(false);
		return ajaxButton;
	}
	/**
	 * 
	 * @param upload
	 */
	private void updateModelAndVarifyForDeleteUpload(Upload upload) {
		confirmModal.setContent(new YesNoPanel(confirmModal.getContentId(), modalText.replace("*",upload.getFilename()).replace("#", " "+upload.getId()),"Warning", confirmModal, confirmationAnswer));
		confirmModal.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
		private static final long serialVersionUID = 1L;
			public void onClose(AjaxRequestTarget target) {
				if (confirmationAnswer.isAnswer() ) {
					iArkCommonService.deleteUpload(upload);
					target.add(me);
				} else {//if no nothing be done.Just close I guess
				}
			}
		});
		addOrReplace(confirmModal);
	}
	
	private void initConfirmModel(){
		confirmationAnswer = new ConfirmationAnswer(false);
		confirmModal = new ModalWindow("confirmModal");
		confirmModal.setCookieName("yesNoPanel");
		confirmModal.setContent(new YesNoPanel(confirmModal.getContentId(), modalText,"Delete upload record.", confirmModal, confirmationAnswer));
		addOrReplace(confirmModal);
	}

	/**
	 * 
	 * @param arkModule
	 * @param header
	 * @return
	 */
	private String[][] changeCustomFieldHeaderWithModuleName(ArkModule arkModule, String[][] header) {
		if (arkModule.getName().equals(au.org.theark.core.Constants.ARK_MODULE_LIMS)) {
			for (int rows = 0; rows < header.length; rows++) {
				for (int columns = 0; columns < header[rows].length; columns++) {
					header[rows][columns] = header[rows][columns].replaceAll("Subject", "Biocollection");
					header[rows][columns] = header[rows][columns].replaceAll("Family", "Biospecimen");
					header[rows][columns] = header[rows][columns].replaceAll("subject", "biocollection");
					header[rows][columns] = header[rows][columns].replaceAll("family", "biospecimen");
				}
			}
		}else if(arkModule.getName().equals(au.org.theark.core.Constants.ARK_MODULE_STUDY)){
			for (int rows = 0; rows < header.length; rows++) {
				for (int columns = 0; columns < header[rows].length; columns++) {
					header[rows][columns] = header[rows][columns].replaceAll("Biocollection","Subject");
					header[rows][columns] = header[rows][columns].replaceAll("Biospecimen","Family");
					header[rows][columns] = header[rows][columns].replaceAll("biocollection","subject");
					header[rows][columns] = header[rows][columns].replaceAll("biospecimen","family");
				}
			}
		}
		return header;
	}
}