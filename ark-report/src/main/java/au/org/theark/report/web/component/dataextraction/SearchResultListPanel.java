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
package au.org.theark.report.web.component.dataextraction;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.IResourceStream;

import au.org.theark.core.Constants;
import au.org.theark.core.model.pheno.entity.PhenoDataSetFieldDisplay;
import au.org.theark.core.model.report.entity.BiocollectionField;
import au.org.theark.core.model.report.entity.BiospecimenField;
import au.org.theark.core.model.report.entity.ConsentStatusField;
import au.org.theark.core.model.report.entity.DemographicField;
import au.org.theark.core.model.report.entity.Search;
import au.org.theark.core.model.report.entity.SearchFile;
import au.org.theark.core.model.study.entity.ArkFunction;
import au.org.theark.core.model.study.entity.CustomFieldDisplay;
import au.org.theark.core.model.study.entity.CustomFieldType;
import au.org.theark.core.model.study.entity.Study;
import au.org.theark.core.service.IArkCommonService;
import au.org.theark.core.util.ArkString;
import au.org.theark.core.util.ByteDataResourceRequestHandler;
import au.org.theark.core.vo.ArkCrudContainerVO;
import au.org.theark.core.vo.SearchVO;
import au.org.theark.core.web.component.AbstractDetailModalWindow;
import au.org.theark.core.web.component.ArkCRUDHelper;
import au.org.theark.core.web.component.link.ArkBusyAjaxLink;
import au.org.theark.phenotypic.service.IPhenotypicService;
import au.org.theark.report.job.DataExtractionUploadExecutor;
import au.org.theark.report.service.IReportService;
import au.org.theark.report.web.component.dataextraction.form.ContainerForm;
import au.org.theark.report.web.component.searchresult.SearchResultPanel;

public class SearchResultListPanel extends Panel {


	private static final long	serialVersionUID	= 1L;

	private ContainerForm		containerForm;
	private ArkCrudContainerVO	arkCrudContainerVO;
	private AbstractDetailModalWindow modalWindow; 

	@SpringBean(name = au.org.theark.core.Constants.ARK_COMMON_SERVICE)
	private IArkCommonService				iArkCommonService;
	@SpringBean(name = Constants.ARK_PHENO_DATA_SERVICE)
	private IPhenotypicService 				iPhenoService;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(au.org.theark.core.Constants.DD_MM_YYYY_HH_MM_SS);
	
	@SpringBean(name = au.org.theark.report.service.Constants.REPORT_SERVICE)
	private IReportService							reportService;
	
	/**
	 * 
	 * @param id
	 * @param crudContainerVO
	 * @param searchContainerForm
	 */
	public SearchResultListPanel(String id, ArkCrudContainerVO crudContainerVO, ContainerForm searchContainerForm) {
		super(id);
		arkCrudContainerVO = crudContainerVO;
		containerForm = searchContainerForm;
		
		modalWindow= new AbstractDetailModalWindow("detailModalWindow") {

			private static final long	serialVersionUID	= 1L;
			@Override
			protected void onCloseModalWindow(AjaxRequestTarget target) {
				// what to do when modal closed
			}
		};
		add(modalWindow);
		
		AjaxButton ajaxButton = new AjaxButton("refresh") {

			private static final long	serialVersionUID	= 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				target.add(SearchResultListPanel.this);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				this.error("A system error occurred. Could not process download request");
			};
		};

		ajaxButton.setVisible(true);
		ajaxButton.setDefaultFormProcessing(false);
		add(ajaxButton);
		setOutputMarkupId(true);
		//add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(10)));
	}

	/**
	 * 
	 * @param iModel
	 * @param searchContainer
	 * @return
	 */
	public PageableListView<Search> buildPageableListView(IModel iModel) {

		PageableListView<Search> sitePageableListView = new PageableListView<Search>("searchList", iModel, iArkCommonService.getRowsPerPage()) {

			private static final long	serialVersionUID	= 1L;

			@Override
			protected void populateItem(final ListItem<Search> item) {

				Search search = item.getModelObject();

				/* The Search ID */
				if (search.getId() != null) {
					// Add the study Component Key here
					item.add(new Label("search.id", search.getId().toString()));
				}
				else {
					item.add(new Label("search.id", ""));
				}
				
				if (search.getStatus() != null) {
					// Add the study Component Key here
					item.add(new Label("search.status", ArkString.toSentenceCase(search.getStatus())));
				}
				else {
					item.add(new Label("search.status", ""));
				}
				
				if (search.getStartTime() != null) {
					item.add(new Label("search.startTime", dateFormat.format(search.getStartTime())));
				}
				else {
					item.add(new Label("search.startTime", ""));
				}
				
				if (search.getFinishTime() != null) {
					item.add(new Label("search.finishTime", dateFormat.format(search.getFinishTime())));
				}
				else {
					item.add(new Label("search.finishTime", ""));
				}
				
				item.add(buildExcludeSubjectUIsButton(search));
				
				/* Search Name Link */
				item.add(buildLink(search));

				item.add(buildDownloadButton(search));
				
				item.add(buildRunSearchButton(search));

				/* The Search Name
				if (search.getName() != null) {
					item.add(new Label("search.name", search.getName()));
				}
				else {
					item.add(new Label("search.name", "no name given"));
				} */
				
				// TODO when displaying text escape any special characters
				/* Description *
				if (search.getDescription() != null) {
					item.add(new Label("search.description", search.getDescription()));// the ID here must match the ones in mark-up
				}
				else {
					item.add(new Label("search.description", ""));// the ID here must match the ones in mark-up
				}*/

				/* For the alternative stripes */
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

	@SuppressWarnings( { "unchecked", "serial" })
	private AjaxLink buildLink(final Search search) {

		ArkBusyAjaxLink link = new ArkBusyAjaxLink("search.name") {

			@Override
			public void onClick(AjaxRequestTarget target) {

				SearchVO searchVo = containerForm.getModelObject();
				//searchVo.setMode(Constants.MODE_EDIT);
				searchVo.setSearch(search);// Sets the selected object into the model
				
				Collection<DemographicField> availableDemographicFields = iArkCommonService.getAllDemographicFields();
				containerForm.getModelObject().setAvailableDemographicFields(availableDemographicFields);
				Collection<DemographicField> selectedDemographicFields =iArkCommonService.getSelectedDemographicFieldsForSearch(search);//, true);
				containerForm.getModelObject().setSelectedDemographicFields(selectedDemographicFields);
				
				Collection<ConsentStatusField> availableConsentStatusFields = iArkCommonService.getAllConsentStatusFields();
				containerForm.getModelObject().setAvailableConsentStatusFields(availableConsentStatusFields);
				Collection<ConsentStatusField> selectedConsentStatusFields = iArkCommonService.getSelectedConsentStatusFieldsForSearch(search);
				containerForm.getModelObject().setSelectedConsentStatusFields(selectedConsentStatusFields);


				Collection<BiospecimenField> availableBiospecimenFields = iArkCommonService.getAllBiospecimenFields();
				containerForm.getModelObject().setAvailableBiospecimenFields(availableBiospecimenFields);
				Collection<BiospecimenField> selectedBiospecimenFields =iArkCommonService.getSelectedBiospecimenFieldsForSearch(search);//, true);
				containerForm.getModelObject().setSelectedBiospecimenFields(selectedBiospecimenFields);
				
				Collection<BiocollectionField> availableBiocollectionFields = iArkCommonService.getAllBiocollectionFields();
				containerForm.getModelObject().setAvailableBiocollectionFields(availableBiocollectionFields);
				Collection<BiocollectionField> selectedBiocollectionFields =iArkCommonService.getSelectedBiocollectionFieldsForSearch(search);//, true);
				containerForm.getModelObject().setSelectedBiocollectionFields(selectedBiocollectionFields);

				

				Long studyId = (Long) SecurityUtils.getSubject().getSession().getAttribute(au.org.theark.core.Constants.STUDY_CONTEXT_ID);
				Study study = iArkCommonService.getStudy(studyId); 
				ArkFunction arkFunctionPheno = iArkCommonService.getArkFunctionByName(Constants.FUNCTION_KEY_VALUE_DATA_DICTIONARY);
				ArkFunction arkFunctionLIMS = iArkCommonService.getArkFunctionByName(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_LIMS_CUSTOM_FIELD);
				CustomFieldType cusFldTypeBioCollection=iArkCommonService.getCustomFieldTypeByName(au.org.theark.core.Constants.BIOCOLLECTION);
				CustomFieldType cusFldTypeBiospecimen=iArkCommonService.getCustomFieldTypeByName(au.org.theark.core.Constants.BIOSPECIMEN);
				ArkFunction arkFunctionSubject = iArkCommonService.getArkFunctionByName(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_SUBJECT_CUSTOM_FIELD);
				
				SearchFile searchFile =reportService.getSearchFileByStudyAndSearch(study, search);
				containerForm.getModelObject().setSearchFile(searchFile);

				Collection<PhenoDataSetFieldDisplay> availablePhenoDataSetFieldDisplays = iPhenoService.getPhenoFieldDisplaysIn(study, arkFunctionPheno);
				containerForm.getModelObject().setAvailablePhenoDataSetFieldDisplays(availablePhenoDataSetFieldDisplays);
				Collection<PhenoDataSetFieldDisplay> selectedPhenoDataSetFieldDisplays =iArkCommonService.getSelectedPhenoDataSetFieldDisplaysForSearch(search);//, true);
				containerForm.getModelObject().setSelectedPhenoDataSetFieldDisplays(selectedPhenoDataSetFieldDisplays);


				Collection<CustomFieldDisplay> availableSubjectCustomFieldDisplays = iArkCommonService.getCustomFieldDisplaysIn(study, arkFunctionSubject);
				containerForm.getModelObject().setAvailableSubjectCustomFieldDisplays(availableSubjectCustomFieldDisplays);
				Collection<CustomFieldDisplay> selectedSubjectCustomFieldDisplays =iArkCommonService.getSelectedSubjectCustomFieldDisplaysForSearch(search);//, true);
				containerForm.getModelObject().setSelectedSubjectCustomFieldDisplays(selectedSubjectCustomFieldDisplays);


				Collection<CustomFieldDisplay> availableBiocollectionCustomFieldDisplays = iArkCommonService.getCustomFieldDisplaysInLIMS(study, arkFunctionLIMS, cusFldTypeBioCollection);
				containerForm.getModelObject().setAvailableBiocollectionCustomFieldDisplays(availableBiocollectionCustomFieldDisplays);
				Collection<CustomFieldDisplay> selectedBiocollectionCustomFieldDisplays =iArkCommonService.getSelectedLIMSCustomFieldDisplaysForSearchOnCustomFieldType(search, cusFldTypeBioCollection);
				containerForm.getModelObject().setSelectedBiocollectionCustomFieldDisplays(selectedBiocollectionCustomFieldDisplays);


				Collection<CustomFieldDisplay> availableBiospecimenCustomFieldDisplays = iArkCommonService.getCustomFieldDisplaysInLIMS(study, arkFunctionLIMS,cusFldTypeBiospecimen);
				containerForm.getModelObject().setAvailableBiospecimenCustomFieldDisplays(availableBiospecimenCustomFieldDisplays);
				Collection<CustomFieldDisplay> selectedBiospecimenCustomFieldDisplays =iArkCommonService.getSelectedLIMSCustomFieldDisplaysForSearchOnCustomFieldType(search,cusFldTypeBiospecimen);//, true);
				containerForm.getModelObject().setSelectedBiospecimenCustomFieldDisplays(selectedBiospecimenCustomFieldDisplays);
				//Update the Create Filter button.
				AjaxButton ajaxButton = (AjaxButton) arkCrudContainerVO.getDetailPanelFormContainer().get("createFilters");
				ajaxButton.add(new AttributeModifier("value", new Model<String>(iArkCommonService.isAnyFilterAddedForSearch(search)?"Edit Filters":"Create Filters")));
				target.add(ajaxButton);
				target.add(arkCrudContainerVO.getDetailPanelFormContainer());
				// Render the UI
				ArkCRUDHelper.preProcessDetailPanelOnSearchResults(target, arkCrudContainerVO);
			}
		};

		// Add the label for the link
		Label nameLinkLabel = new Label("nameLbl", search.getName());
		link.add(nameLinkLabel);
		return link;

	}
	
	private AjaxButton buildDownloadButton(final Search search) {
		AjaxButton ajaxButton = new AjaxButton(Constants.DOWNLOAD_FILE) {

			private static final long	serialVersionUID	= 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				// Set the modalWindow title and content
				modalWindow.setTitle("Search Results");
				
				SearchResultPanel srp = new SearchResultPanel("content", search.getId());
				modalWindow.setContent(srp);
				//modalWindow.setContent(new EmptyPanel("content"));
				modalWindow.show(target);
				
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				this.error("A system error occurred. Could not process download request");
			};
			
			@Override
			public boolean isVisible() {
				return search.getStatus() != null && search.getStatus().equalsIgnoreCase("FINISHED");
			}
		};

		ajaxButton.setVisible(true);
		ajaxButton.setDefaultFormProcessing(false);

		return ajaxButton;
	}
	
	private AjaxButton buildExcludeSubjectUIsButton(final Search search) {
		AjaxButton ajaxButton = new AjaxButton(Constants.DOWNLOAD_EXCLUDE_UIS) {

			private static final long	serialVersionUID	= 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				SearchFile searchFile=reportService.getSearchFileByStudyAndSearch(search.getStudy(), search);
				Long studyId =searchFile.getStudy().getId();
				Long searchId=searchFile.getSearch().getId();
				String fileId = searchFile.getFileId();
				String checksum = searchFile.getChecksum();
				File file = null;
					try {
						file=iArkCommonService.retriveArkFileAttachmentAsFile(studyId,searchId.toString(),au.org.theark.report.web.Constants.REPORT_DATA_EXTRACTION_SUBJECT_UID_RESTRICT_FILE,fileId,checksum);
						byte[] data = FileUtils.readFileToByteArray(file);
						getRequestCycle().scheduleRequestHandlerAfterCurrent(new ByteDataResourceRequestHandler("text/plain", data, searchFile.getFilename()));
					}catch (Exception e) {
						e.printStackTrace();
					}
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				this.error("A system error occurred. Could not process download request");
			};
			@Override
			public boolean isVisible() {
				SearchFile searchFile=reportService.getSearchFileByStudyAndSearch(search.getStudy(), search);
				return (searchFile!=null && searchFile.getId()!=null);
				
			}
		};

		ajaxButton.setVisible(true);
		ajaxButton.setDefaultFormProcessing(false);

		return ajaxButton;
	}

	private AjaxButton buildRunSearchButton(final Search search) {
		AjaxButton ajaxButton = new AjaxButton(au.org.theark.report.web.Constants.RUN_BATCH_QUERY) {

			private static final long	serialVersionUID	= 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				//TODO ASAP : this will be replaced by call to job
				//iArkCommonService.runSearch(search.getId());
				try {
					SecurityUtils.getSubject();
					search.setStartTime(new java.util.Date(System.currentTimeMillis()));
					search.setStatus("RUNNING");
					search.setFinishTime(null);
					iArkCommonService.update(search);
					String currentUser = SecurityUtils.getSubject().getPrincipals().getPrimaryPrincipal().toString();
					DataExtractionUploadExecutor task = new DataExtractionUploadExecutor(iArkCommonService, search.getId(), currentUser);//, studyId);
					task.run();
					target.add(SearchResultListPanel.this);
					target.appendJavaScript("alert('Data files are being created as a background job. You may download when complete.');");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.out.println("TODO: decent logging and handling" + e.getMessage());
					e.printStackTrace();
				}
				finally {
				}
			
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				this.error("A system error occurred. Could not process download request");
			};
			
			@Override
			public boolean isVisible() {
				//TODO return correct status
				return search.getStatus() != null && !search.getStatus().equalsIgnoreCase("RUNNING");
				//return search.getStatus() != null && search.getStatus().equalsIgnoreCase("READY TO RUN") || true;
			}
			
		};

		//ajaxButton.setVisible(true);
		ajaxButton.setDefaultFormProcessing(false);
		//log.warn("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n how many times is this run?");

		//TODO move back
		//if (upload.getPayload() == null)
		//if (data == null)
		//ajaxButton.setVisible(false);

		return ajaxButton;
	}

}
