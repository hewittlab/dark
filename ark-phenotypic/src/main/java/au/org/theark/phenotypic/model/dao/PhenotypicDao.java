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
package au.org.theark.phenotypic.model.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import au.org.theark.core.Constants;
import au.org.theark.core.dao.HibernateSessionDao;
import au.org.theark.core.exception.ArkRunTimeException;
import au.org.theark.core.exception.ArkRunTimeUniqueException;
import au.org.theark.core.exception.ArkSystemException;
import au.org.theark.core.exception.ArkUniqueException;
import au.org.theark.core.exception.EntityCannotBeRemoved;
import au.org.theark.core.exception.EntityExistsException;
import au.org.theark.core.model.pheno.entity.LinkPhenoDataSetCategoryField;
import au.org.theark.core.model.pheno.entity.PhenoDataSetCategory;
import au.org.theark.core.model.pheno.entity.PhenoDataSetCollection;
import au.org.theark.core.model.pheno.entity.PhenoDataSetData;
import au.org.theark.core.model.pheno.entity.PhenoDataSetField;
import au.org.theark.core.model.pheno.entity.PhenoDataSetFieldDisplay;
import au.org.theark.core.model.pheno.entity.PhenoDataSetGroup;
import au.org.theark.core.model.pheno.entity.PickedPhenoDataSetCategory;
import au.org.theark.core.model.pheno.entity.QuestionnaireStatus;
import au.org.theark.core.model.study.entity.ArkFunction;
import au.org.theark.core.model.study.entity.ArkUser;
import au.org.theark.core.model.study.entity.AuditHistory;
import au.org.theark.core.model.study.entity.CustomField;
import au.org.theark.core.model.study.entity.CustomFieldCategory;
import au.org.theark.core.model.study.entity.CustomFieldDisplay;
import au.org.theark.core.model.study.entity.CustomFieldGroup;
import au.org.theark.core.model.study.entity.DelimiterType;
import au.org.theark.core.model.study.entity.FileFormat;
import au.org.theark.core.model.study.entity.LinkSubjectStudy;
import au.org.theark.core.model.study.entity.PhenoDataSetFieldCategoryUpload;
import au.org.theark.core.model.study.entity.PhenoFieldUpload;
import au.org.theark.core.model.study.entity.Study;
import au.org.theark.core.model.study.entity.Upload;
import au.org.theark.core.service.IArkCommonService;
import au.org.theark.core.vo.CustomFieldGroupVO;
import au.org.theark.core.vo.PhenoDataCollectionVO;
import au.org.theark.core.vo.PhenoDataSetFieldGroupVO;

@SuppressWarnings("unchecked")
@Repository("phenotypicDao")
public class PhenotypicDao extends HibernateSessionDao implements IPhenotypicDao {
	static Logger		log	= LoggerFactory.getLogger(PhenotypicDao.class);

	private IArkCommonService<Void>	iArkCommonService;
	@Autowired
	public void setiArkCommonService(IArkCommonService<Void> iArkCommonService) {
		this.iArkCommonService = iArkCommonService;
	}

	public java.util.Collection<PhenoDataSetCollection> getPhenoCollectionByStudy(Study study) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCollection.class);

		if (study != null) {
			criteria.add(Restrictions.eq(au.org.theark.phenotypic.web.Constants.PHENO_COLLECTION_STUDY, study));
		}
		criteria.addOrder(Order.asc("name"));

		java.util.List<PhenoDataSetCollection> collectionList = criteria.list();
		return collectionList;
	}
	public void deletePhenoCollection(PhenoDataSetCollection collection) {
		getSession().delete(collection);
	}
	public void createUpload(Upload phenoUpload) {
		Session session = getSession();

		//currentUser = SecurityUtils.getSubject();
		Date dateNow = new Date(System.currentTimeMillis());
		//phenoUpload.setInsertTime(dateNow);
		//phenoUpload.setUserId(currentUser.getPrincipal().toString());

		if (phenoUpload.getStartTime() == null)
			phenoUpload.setStartTime(dateNow);

		session.save(phenoUpload);
	}
	public void updateUpload(Upload upload) {

		getSession().update(upload);
	}

	
	public java.util.Collection<FileFormat> getFileFormats() {
		Criteria criteria = getSession().createCriteria(FileFormat.class);
		java.util.Collection<FileFormat> fileFormatCollection = criteria.list();
		return fileFormatCollection;
	}

	public Collection<DelimiterType> getDelimiterTypes() {
		Criteria criteria = getSession().createCriteria(DelimiterType.class);
		java.util.Collection<DelimiterType> delimiterTypeCollection = criteria.list();
		return delimiterTypeCollection;
	}

	public long getCountOfFieldsInStudy(Study study) {
		return -1L;
	}

	public long getCountOfFieldsWithDataInStudy(Study study) {
		return -1L;
		
	}
	public long getCountOfCollectionsInStudy(Study study) {
		int count = 0;

		if (study.getId() != null) {
			Criteria criteria = getSession().createCriteria(PhenoDataSetCollection.class);
			criteria.add(Restrictions.eq("study", study));

			java.util.Collection<PhenoDataSetCollection> phenoCollection = criteria.list();
			count = phenoCollection.size();
		}

		return count;
	}

	public long getCountOfCollectionsWithDataInStudy(Study study) {
		long count = 0;

		if (study.getId() != null) {
			Collection<PhenoDataSetCollection> phenoCollectionColn = getPhenoCollectionByStudy(study);

			for (Iterator iterator = phenoCollectionColn.iterator(); iterator.hasNext();) {
				PhenoDataSetCollection phenoCollection = (PhenoDataSetCollection) iterator.next();

				Criteria criteria = getSession().createCriteria(PhenoDataSetData.class);
				criteria.add(Restrictions.eq("phenCollection", phenoCollection));
				ProjectionList projList = Projections.projectionList();
				projList.add(Projections.countDistinct("collection"));
				criteria.setProjection(projList);
				List list = criteria.list();
				count = count + ((Long) list.get(0));
			}
		}

		return count;
	}

	public DelimiterType getDelimiterType(Long id) {
		DelimiterType delimiterType = (DelimiterType) getSession().get(DelimiterType.class, id);
		return delimiterType;
	}

	public boolean phenoCollectionHasData(PhenoDataSetCollection phenoCollection) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetData.class);

		if (phenoCollection != null) {
			criteria.add(Restrictions.eq("phenoCollection", phenoCollection));
		}

		return criteria.list().size() > 0;
	}

	public String getDelimiterTypeByDelimiterChar(char delimiterCharacter) {
		String delimiterTypeName = null;
		Criteria criteria = getSession().createCriteria(DelimiterType.class);
		criteria.add(Restrictions.eq("delimiterCharacter", delimiterCharacter));

		if (criteria.list().size() > 0) {
			DelimiterType delimiterType = (DelimiterType) criteria.list().get(0);
			delimiterTypeName = delimiterType.getName();
		}
		return delimiterTypeName;
	}

	public FileFormat getFileFormatByName(String name) {
		FileFormat fileFormat = null;
		Criteria criteria = getSession().createCriteria(FileFormat.class);
		criteria.add(Restrictions.eq("name", name));

		if (criteria.list().size() > 0) {
			fileFormat = (FileFormat) criteria.list().get(0);
		}
		return fileFormat;
	}

	public Long isPhenoDataSetFieldUsed(PhenoDataSetData phenoData) {
		Long count = new Long("0");
		PhenoDataSetField phenoDataSetField = phenoData.getPhenoDataSetFieldDisplay().getPhenoDataSetField();
		
		Study study = phenoDataSetField.getStudy();
		ArkFunction arkFunction = phenoDataSetField.getArkFunction();
		
		Criteria criteria = getSession().createCriteria(PhenoDataSetData.class, "pd");
		criteria.createAlias("pd.phenoDataSetFieldDisplay", "pdsfd");
		criteria.createAlias("pdsfd.phenoDataSetField", "pdsf");
		criteria.createAlias("pdsf.arkFunction", "aF");
		criteria.createAlias("pdsf.study", "s");
		criteria.add(Restrictions.eq("aF.id", arkFunction.getId()));
		criteria.add(Restrictions.eq("pdsfd.id", phenoData.getPhenoDataSetFieldDisplay().getId()));
		criteria.add(Restrictions.eq("s.id", study.getId()));
		
		count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
				
		return count;
	}

	/**
	 * Create Pheno data
	 */
	public void createPhenoData(PhenoDataSetData phenoData) {
		getSession().save(phenoData);
	}

	/**
	 * Delete Pheno data
	 */
	public void deletePhenoData(PhenoDataSetData phenoData) {
		getSession().delete(phenoData);
	}

	/**
	 * Update Pheno data
	 */
	public void updatePhenoData(PhenoDataSetData phenoData) {
		getSession().update(phenoData);
	}

	public PhenoDataSetCollection getPhenoCollection(Long id) {
		return (PhenoDataSetCollection) getSession().get(PhenoDataSetCollection.class, id);
	}

	public long getPhenoDataCount(PhenoDataSetCollection phenoCollection,PhenoDataSetCategory phenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.createAlias("phenoDataSetGroup", "qnaire");
		if(phenoCollection.getQuestionnaire()!=null){
			criteria.add(Restrictions.eq("qnaire.id", phenoCollection.getQuestionnaire().getId()));
		}
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		return count.intValue();
	}

	public List<PhenoDataSetData> getPhenoDataList(PhenoDataSetCollection phenoCollection,PhenoDataSetCategory phenoDataSetCategory, int first, int count) {
		
		List<PhenoDataSetData> phenoDataList = new ArrayList<PhenoDataSetData>();

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT pdsfd, pdsd ");
		sb.append("  FROM PhenoDataSetFieldDisplay AS pdsfd ");
		sb.append(" INNER JOIN pdsfd.phenoDataSetGroup AS pdsg ");
		sb.append(" INNER JOIN pdsg.phenoDataSetCollections pdsc ");
		sb.append("  LEFT JOIN pdsfd.phenoDataSetData AS pdsd ");
		sb.append("  WITH pdsd.phenoDataSetCollection.id = :pcId ");
		sb.append(" WHERE pdsc.id = :pcId ");
		if(phenoDataSetCategory!=null){
			sb.append(" and pdsfd.phenoDataSetCategory = :phenoDataSetCategory ");
		}
		sb.append(" and pdsfd.phenoDataSetField is not null ");
		sb.append(" ORDER BY pdsfd.phenoDataSetFiledOrderNumber ");
		Query query = getSession().createQuery(sb.toString());
		query.setParameter("pcId", phenoCollection.getId());
		if(phenoDataSetCategory!=null){
			query.setParameter("phenoDataSetCategory", phenoDataSetCategory);
		}
		query.setFirstResult(first);
		query.setMaxResults(count);
		
		List<Object[]> listOfObjects = query.list();
		for (Object[] objects : listOfObjects) {
			PhenoDataSetFieldDisplay pfd = new PhenoDataSetFieldDisplay();
			PhenoDataSetData phenoData = new PhenoDataSetData();
			if (objects.length > 0 && objects.length >= 1) {
				pfd = (PhenoDataSetFieldDisplay)objects[0];
					if (objects[1] != null) {
						phenoData = (PhenoDataSetData)objects[1];
					} 
					else {
						phenoData.setPhenoDataSetFieldDisplay(pfd);
					}
					phenoDataList.add(phenoData);	
			}
		}
		return phenoDataList;
	}
	
	/**
	 * Create  a CustomFieldGroup and then link the selected custom fields into the Group via
	 * the CustomFieldDisplay. For each Custom Field create a new CustomFieldDisplay
	 * @param customFieldGroupVO
	 */
	public void createCustomFieldGroup(CustomFieldGroupVO customFieldGroupVO) throws ArkSystemException{
		
		CustomFieldGroup customFieldGroup = customFieldGroupVO.getCustomFieldGroup();
		Session session = getSession();
		if(customFieldGroup.getPublished() == null){
			customFieldGroup.setPublished( new Boolean("false"));
		}
		session.save(customFieldGroup);
		ArrayList<CustomField> customFieldList = customFieldGroupVO.getSelectedCustomFields();
		
		int fieldposition = 0;
		for (CustomField customField : customFieldList) {
			++fieldposition;
			CustomFieldDisplay customFieldDisplay = new CustomFieldDisplay();
			customFieldDisplay.setCustomFieldGroup(customFieldGroup);
			customFieldDisplay.setCustomField(customField);
			customFieldDisplay.setSequence( new Long(fieldposition));
			session.save(customFieldDisplay);
			log.debug("Saved CustomFieldDisplay for Custom Field Group");
		}
	}

	public long getPhenoCollectionCount(PhenoDataCollectionVO collectionCriteria) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCollection.class);
		criteria.add(Restrictions.eq("linkSubjectStudy", collectionCriteria.getPhenoDataSetCollection().getLinkSubjectStudy()));
		criteria.setProjection(Projections.rowCount());
		Long count = (Long) criteria.uniqueResult();
		return count;
	}

	public List<PhenoDataSetCollection> searchPageablePhenoCollection(PhenoDataCollectionVO collectionCriteria, int first, int count) {
		
		Criteria criteria = getSession().createCriteria(PhenoDataSetCollection.class);
		criteria.add(Restrictions.eq("linkSubjectStudy", collectionCriteria.getPhenoDataSetCollection().getLinkSubjectStudy()));
		// Just a precaution (PhenoCollection to should always map to a CustomFieldGroup where the ArkFunction will correspond to Pheno) 
		//criteria.add(Restrictions.eq("qnaire.arkFunction", collectionCriteria.getArkFunction()));	
		criteria.setFirstResult(first);
		criteria.setMaxResults(count);
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.groupProperty("id"), "id");
		projectionList.add(Projections.groupProperty("name"), "name");
		projectionList.add(Projections.groupProperty("questionnaire"), "questionnaire");
		projectionList.add(Projections.groupProperty("description"), "description");
		projectionList.add(Projections.groupProperty("recordDate"), "recordDate");
		projectionList.add(Projections.groupProperty("reviewedDate"), "reviewedDate");
		projectionList.add(Projections.groupProperty("reviewedBy"), "reviewedBy");
		projectionList.add(Projections.groupProperty("status"), "status");
		criteria.setProjection(projectionList);
		criteria.setResultTransformer(Transformers.aliasToBean(PhenoDataSetCollection.class));
		return (List<PhenoDataSetCollection>) criteria.list();
	}
	
	public List<CustomField> getCustomFieldsLinkedToCustomFieldGroup(CustomFieldGroup customFieldCriteria){
		
		Criteria criteria = getSession().createCriteria(CustomFieldDisplay.class);
		criteria.add(Restrictions.eq("customFieldGroup",customFieldCriteria));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("customField"));
		criteria.setProjection(projectionList);
		criteria.addOrder(Order.asc("sequence"));
		List<CustomField> fieldsList = criteria.list();
		//log.warn("______________customFieldsList = " + fieldsList.size());
		return fieldsList;
		
	}

	public List<QuestionnaireStatus> getPhenoCollectionStatusList() {
		List<QuestionnaireStatus> resultList = new ArrayList<QuestionnaireStatus>(0);
		Criteria criteria = getSession().createCriteria(QuestionnaireStatus.class);
		resultList = criteria.list();
		return resultList;
	}
	
	private List<CustomFieldDisplay> getCustomFieldDisplayForCustomFieldGroup(CustomFieldGroup customFieldGroup){
		Criteria criteria = getSession().createCriteria(CustomFieldDisplay.class);
		criteria.add(Restrictions.eq("customFieldGroup",customFieldGroup));
		criteria.addOrder(Order.asc("sequence"));
		return criteria.list();
	}
	
	/**
	 * Update CustomFieldGroup and its related CustomFields(Add or remove)
	 */
	public void updateCustomFieldGroup(CustomFieldGroupVO customFieldGroupVO) throws EntityExistsException,ArkSystemException{
		
		CustomFieldGroup customFieldGroup = customFieldGroupVO.getCustomFieldGroup();
		Session session = getSession();
		session.update(customFieldGroup);//Update
		
		if(!customFieldGroup.getPublished()){//Allow Removal only if the form is not published
			Collection<CustomFieldDisplay> customFieldDisplayToRemove = getCustomFieldDisplayToRemove(customFieldGroupVO.getSelectedCustomFields(), customFieldGroup);	
			for (CustomFieldDisplay cfd : customFieldDisplayToRemove) {
				session.delete(cfd);
			}
		}
	
		ArrayList<CustomFieldDisplay> customFieldsToAdd = getCustomFieldsToAdd(customFieldGroupVO.getSelectedCustomFields(), customFieldGroup);
		for (CustomFieldDisplay fieldToAdd : customFieldsToAdd) {
			session.saveOrUpdate(fieldToAdd);//Add a new CustomFieldDisplay field that is linked to the CustomField	
		}
		
		ArrayList<CustomField> list = customFieldGroupVO.getSelectedCustomFields();
		int position = 0;
		
		for (CustomField customField : list) {
			++position;
			CustomFieldDisplay cfd = iArkCommonService.getCustomFieldDisplayByCustomField(customField,customFieldGroupVO.getCustomFieldGroup());
			cfd.setSequence(new Long(position));
			session.update(cfd);
		}
	
		
	}
	
	/**
	 * Creates Collection that will contain the list of new CustomFields that must be added/linked to the CustomFieldGroup
	 * @param selectedCustomFields
	 * @param customFieldGroup
	 * @return Collection<CustomField>
	 */
	private ArrayList<CustomFieldDisplay> getCustomFieldsToAdd(Collection<CustomField> selectedCustomFields, CustomFieldGroup customFieldGroup){
		
		ArrayList<CustomFieldDisplay> cfdisplayList = new ArrayList<CustomFieldDisplay>();
		List<CustomField> existingCustomFieldList = getCustomFieldsLinkedToCustomFieldGroup(customFieldGroup);// Existing List of CustomFieldsthat were linked to this CustomFieldGroup
		ArrayList<CustomField> nonProxyCustomFieldList = new ArrayList<CustomField>();
		
		/**
		 * Note:
		 * getCustomFieldsLinkedToCustomFieldGroup() returns a projected List representing CustomField from CustomFieldDisplay. Since CustomField was a lazily loaded object, it is represented as a proxy object.
		 * For us to do a comparison using contains the equals() will fail when the class is compared. To be able to do that we convert to an underlying object before we do the final comparison.
		 * Since Hibernate returns proxy objects for LazyInitialisation when the equals() is invoked the class comparison will fail. 
		 */
		
		for (Object obj : existingCustomFieldList) {
			if(obj instanceof HibernateProxy){
				CustomField  cf = (CustomField)((HibernateProxy)obj).getHibernateLazyInitializer().getImplementation();
				nonProxyCustomFieldList.add(cf);
			}
		}

		for (CustomField customField : selectedCustomFields) {
			if((!nonProxyCustomFieldList.contains(customField))){
				
				CustomFieldDisplay customFieldDisplay = new CustomFieldDisplay();
				customFieldDisplay.setCustomFieldGroup(customFieldGroup);
				customFieldDisplay.setCustomField(customField);
				cfdisplayList.add(customFieldDisplay);
			}else{
				//Retrieve the customField for the sequence could have changed
				//String name = customField.getName();
				CustomFieldDisplay cfd = iArkCommonService.getCustomFieldDisplayByCustomField(customField);
				cfdisplayList.add(cfd);
			}
		}
		return cfdisplayList;
	}
	
	/**
	 * Determine the list of CustomField that was linked to this CustomFieldGroup and is not used by anyone and then if this is true add it to a list that will be processed later
	 * for removal.
	 * @param selectedCustomFields
	 * @param customFieldGroup
	 * @return
	 */
	private Collection<CustomFieldDisplay> getCustomFieldDisplayToRemove(Collection<CustomField> selectedCustomFields, CustomFieldGroup customFieldGroup){
		
		Collection<CustomFieldDisplay> customFieldDisplayList = getCustomFieldDisplayForCustomFieldGroup(customFieldGroup);
		Collection<CustomFieldDisplay> customFieldDisplayToRemove = new ArrayList<CustomFieldDisplay>();
		for (CustomFieldDisplay existingCustomFieldDisplay : customFieldDisplayList) {
			
			if(existingCustomFieldDisplay.getCustomField() instanceof HibernateProxy){
				CustomField  cf = (CustomField)((HibernateProxy)existingCustomFieldDisplay.getCustomField()).getHibernateLazyInitializer().getImplementation();
				if(!selectedCustomFields.contains(cf)){
					customFieldDisplayToRemove.add(existingCustomFieldDisplay);	
				}
			}
		}
		return customFieldDisplayToRemove;
	}
	
	public Collection<PhenoDataSetFieldDisplay> getCFDLinkedToQuestionnaire(PhenoDataSetGroup phenoDataSetGroup, int first, int count){
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroup));
		criteria.setFirstResult(first);
		criteria.setMaxResults(count);
		criteria.addOrder(Order.asc("phenoDataSetFiledOrderNumber"));
		return criteria.list();
		
	}
	
	public long getCFDLinkedToQuestionnaireCount(PhenoDataSetGroup phenoDataSetGroup){
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroup));
		criteria.setProjection(Projections.rowCount());
		return (Long)criteria.uniqueResult();
	}

	public void createPhenoCollection(PhenoDataSetCollection phenoCollection) {
		getSession().save(phenoCollection);
	}

	public void updatePhenoCollection(PhenoDataSetCollection phenoCollection) {
		getSession().update(phenoCollection);
	}
	
	public void deletePhenoCollectionRegardlessOfData(PhenoDataSetCollection phenoCollection) {
		// This relies on CASCADE ON DELETE on the database [pheno].[pheno_data] table
		getSession().delete(phenoCollection);
	}
	
	public void deleteCustomFieldGroup(CustomFieldGroupVO customFieldGroupVO){
		//Delete all the CustomFieldDisplay Items linked to the Group
		Session session = getSession();
		Collection<CustomFieldDisplay> customFieldDisplayList = getCustomFieldDisplayForCustomFieldGroup(customFieldGroupVO.getCustomFieldGroup());
		for (CustomFieldDisplay customFieldDisplay : customFieldDisplayList) {
			session.delete(customFieldDisplay);
		}
		session.delete(customFieldGroupVO.getCustomFieldGroup());
	}
	
	/**
	 * The method checks if the given questionnaire's fields have data linked to it.
	 * 
	 * @param customFieldGroup
	 */
	public void isDataAvailableForQuestionnaire(CustomFieldGroup customFieldGroup){
		
		Criteria criteria = getSession().createCriteria(CustomField.class, "cf");
		criteria.createAlias("customFieldDisplay", "cfd", JoinType.LEFT_OUTER_JOIN);	// Left join to CustomFieldDisplay
		criteria.createAlias("cfd.customFieldGroup", "cfg", JoinType.LEFT_OUTER_JOIN); // Left join to CustomFieldGroup
		criteria.add(Restrictions.eq("cf.study", customFieldGroup.getStudy()));
		
		ArkFunction function = iArkCommonService.getArkFunctionByName(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_DATA_DICTIONARY);
		criteria.add(Restrictions.eq("cf.arkFunction", function));
		criteria.add(Restrictions.eq("cfg.id", customFieldGroup.getId()));
		
		DetachedCriteria fieldDataCriteria = DetachedCriteria.forClass(PhenoDataSetData.class, "pd");
		// Join CustomFieldDisplay and PhenoData on ID FK
		fieldDataCriteria.add(Property.forName("cfd.id").eqProperty("pd." + "customFieldDisplay.id"));
		criteria.add(Subqueries.exists(fieldDataCriteria.setProjection(Projections.property("pd.customFieldDisplay"))));
		
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("cfg.name"), "questionnaire");
		projectionList.add(Projections.property("cf.name"), "fieldName");
		projectionList.add(Projections.property("cf.description"), "description");
		
		
	}

	public QuestionnaireStatus getPhenoCollectionStatusByName(String statusName) {
		Criteria criteria = getSession().createCriteria(QuestionnaireStatus.class);
		criteria.add(Restrictions.eq("name", statusName).ignoreCase());
		QuestionnaireStatus result = (QuestionnaireStatus)criteria.uniqueResult();
		return result;
	}
	
	public java.util.Collection<Upload> searchUpload(Upload upload) {
		Criteria criteria = getSession().createCriteria(Upload.class);

		if (upload.getId() != null) {
			criteria.add(Restrictions.eq(au.org.theark.phenotypic.web.Constants.UPLOAD_ID, upload.getId()));
		}

		if (upload.getStudy() != null) {
			criteria.add(Restrictions.eq(au.org.theark.phenotypic.web.Constants.UPLOAD_STUDY, upload.getStudy()));
		}
		
		if(upload.getArkFunction() != null){
			criteria.add(Restrictions.eq("arkFunction",upload.getArkFunction()));
		}

		if (upload.getFileFormat() != null) {
			criteria.add(Restrictions.ilike(au.org.theark.phenotypic.web.Constants.UPLOAD_FILE_FORMAT, upload.getFileFormat()));
		}

		if (upload.getDelimiterType() != null) {
			criteria.add(Restrictions.ilike(au.org.theark.phenotypic.web.Constants.UPLOAD_DELIMITER_TYPE, upload.getDelimiterType()));
		}

		if (upload.getFilename() != null) {
			criteria.add(Restrictions.ilike(au.org.theark.phenotypic.web.Constants.UPLOAD_FILENAME, upload.getFilename()));
		}
		
		criteria.addOrder(Order.desc(au.org.theark.phenotypic.web.Constants.UPLOAD_ID));
		java.util.Collection<Upload> uploadCollection = criteria.list();

		return uploadCollection;
	}
	
	public void deleteUpload(Upload studyUpload){
		//TODO if the upload was successful it must stop the user from removing an uploaded file
		getSession().delete(studyUpload);
	}
	
	public Upload getUpload(Long id){
		return (Upload)getSession().get(Upload.class, id);
	}	

	public Collection<CustomFieldGroup> getCustomFieldGroupList(Study study){
		Criteria criteria = getSession().createCriteria(CustomFieldGroup.class);
		criteria.add(Restrictions.eq("study", study));
		Collection<CustomFieldGroup>  result = criteria.list();
		return result;
	}
	
	public void processPhenoCollectionsWithTheirDataToInsertBatch(List<PhenoDataSetCollection> phenoCollectionsWithTheirDataToInsert, Study study){
		Session session = getSession();
//		int count = 0;
		for(PhenoDataSetCollection collectionToInsert : phenoCollectionsWithTheirDataToInsert){
			//TODO : investigate more efficient way to deal with null parent entity
			Set<PhenoDataSetData> dataToSave = collectionToInsert.getPhenoDataSetData();
			collectionToInsert.setPhenoDataSetData(new HashSet<PhenoDataSetData>());
			
			session.save(collectionToInsert);
			session.refresh(collectionToInsert);
			for(PhenoDataSetData data : dataToSave){
				data.setPhenoDataSetCollection(collectionToInsert);
				session.save(data);
			}
		}
		session.flush();
		session.clear();
	}

	/**
	 * 
	 */
	public List<List<String>>  getPhenoDataAsMatrix (Study study, List<String> subjectUids, List<PhenoDataSetField> phenoDataSetFields, List<PhenoDataSetGroup> phenoDataSetGroups,PhenoDataSetCategory phenoDataSetCategory) {
		List<List<String>>  dataSet = new ArrayList<List<String>>();
		StringBuffer dataHQLquery = new StringBuffer();
		StringBuffer noDataHQLquery = new StringBuffer();
		StringBuffer phenoFieldColumnSQL = new StringBuffer();
		List<String> header = new ArrayList<String>(0);
		
		//stringBuffer.append("SELECT data.* FROM (\n");
		//ARK-799
//		dataHQLquery.append("SELECT lss.subjectUID, pc.recordDate, pc.description, \n"); 
		dataHQLquery.append("SELECT lss.subjectUID, pdsc.recordDate, \n"); 
		noDataHQLquery.append("SELECT lss.subjectUID, cast(null as char) AS recordDate, cast(null as char) AS name, \n"); 
		header.add("SUBJECTUID");
		header.add("RECORD_DATE");
		//ARK-799
//		header.add("COLLECTION");
		
		// Loop for all custom goups
		for(PhenoDataSetGroup pdsg : phenoDataSetGroups) {
			// Get all custom fields for the group and create pivot SQL to create column
			//for(PhenoDataSetFieldDisplay pdsfd : getPhenoDataSetFieldDisplayForPhenoDataSetFieldGroup(pdsg)) {
			for(PhenoDataSetField pdsfd : getPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroupAndPhenoDataSetCategory(pdsg,phenoDataSetCategory)) {
			
				//MAX(IF(custom_field_display_id = 14, pd.number_data_value, NULL)) AS cfd14,
				phenoFieldColumnSQL.append("(MAX(CASE WHEN pdsd.phenoDataSetFieldDisplay.id = ");
				phenoFieldColumnSQL.append(getPhenoDataSetFieldDisplayByPhenoDataSetFieldAndGroup(pdsfd,pdsg).getId());
				
				// Determine field type and append SQL accordingly
				if(pdsfd.getFieldType().getName().equalsIgnoreCase(Constants.FIELD_TYPE_DATE)) {
					phenoFieldColumnSQL.append(" THEN pdsd.dateDataValue ELSE NULL END) ");
				}
				if(pdsfd.getFieldType().getName().equalsIgnoreCase(Constants.FIELD_TYPE_NUMBER)) {
					phenoFieldColumnSQL.append(" THEN pdsd.numberDataValue ELSE NULL END) ");
				}
				if (pdsfd.getFieldType().getName().equalsIgnoreCase(Constants.FIELD_TYPE_CHARACTER)) {
					phenoFieldColumnSQL.append(" THEN pdsd.textDataValue ELSE NULL END) ");
				}
				
				phenoFieldColumnSQL.append(") ");
				phenoFieldColumnSQL.append(",");
				
				noDataHQLquery.append("cast(null as char) ");
				noDataHQLquery.append(",");
				
				header.add(pdsfd.getName().toUpperCase());
			}
		}
		// Remove erroneous ',' char from end of strings
		if(phenoFieldColumnSQL.length() > 0) {
			phenoFieldColumnSQL.setLength(phenoFieldColumnSQL.length()-1);
			noDataHQLquery.setLength(noDataHQLquery.length()-1);
			dataHQLquery.append(phenoFieldColumnSQL);
			
			dataHQLquery.append("\nFROM \n");
			dataHQLquery.append(" PhenoDataSetData pdsd, ");
			dataHQLquery.append(" PhenoDataSetCollection pdsc, ");
			dataHQLquery.append(" LinkSubjectStudy lss, ");
			dataHQLquery.append(" PhenoDataSetFieldDisplay pdsfd \n");
			dataHQLquery.append(" WHERE pdsd.phenoDataSetCollection.id = pdsc.id \n");
			dataHQLquery.append(" AND pdsc.linkSubjectStudy.id = lss.id \n");
			dataHQLquery.append(" AND lss.study = :study \n");
			dataHQLquery.append(" AND lss.subjectUID IN (:subjectUids) \n");
			dataHQLquery.append(" AND pdsfd.phenoDataSetGroup in (:phenoDataSetGroups) \n");
			dataHQLquery.append(" AND pdsd.phenoDataSetFieldDisplay.id = pdsfd.id \n");
			dataHQLquery.append("GROUP BY lss.subjectUID, pdsd.phenoDataSetCollection");
			
			noDataHQLquery.append("\nFROM LinkSubjectStudy lss\n");
			noDataHQLquery.append("WHERE lss.study = :study \n");
			noDataHQLquery.append("AND lss.id NOT IN (SELECT pdsc.linkSubjectStudy.id FROM PhenoDataSetCollection pdsc WHERE pdsc.questionnaire IN (:phenoDataSetGroups))\n");
			
			String hqlQuery = dataHQLquery.toString();
			
			Session session = getSession();
			
			Query dataQuery = session.createQuery(hqlQuery);
			dataQuery.setParameter("study", study);
			dataQuery.setParameterList("subjectUids", subjectUids);
			dataQuery.setParameterList("phenoDataSetGroups", phenoDataSetGroups);
			
			// Add header as first list item
			dataSet.add(header);
			// Add data
			//ArrayList<List<String>> dataList = new ArrayList<List<String>>();
			//dataList = (ArrayList<List<String>>) dataQuery.list();
			
			//This result set contains a List of Object arrays���each array represents one set of properties
	      Iterator it=dataQuery.iterate();
	      while (it.hasNext()) {
	          Object[] val = (Object[]) it.next();
	          List<String> stringList = new ArrayList<String>();
	          for(Object o : val) {
	         	 stringList.add(o !=null ? o.toString() : new String());
	          }
	          dataSet.add(stringList);
	      }
			
			
			
			/*hqlQuery = noDataHQLquery.toString();
			
			Query noDataQuery = session.createQuery(hqlQuery);
			noDataQuery.setParameter("study", study);
			noDataQuery.setParameterList("phenoDataSetGroups", phenoDataSetGroups);*/
			//noDataQuery.list();
			//dataSet.addAll(noDataQuery.list());
		}
		return dataSet;
	}
	
	public List<PhenoDataSetGroup> getPhenoDataSetGroupsByLinkSubjectStudy(LinkSubjectStudy linkSubjectStudy) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCollection.class);
		criteria.add(Restrictions.eq("linkSubjectStudy", linkSubjectStudy));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.groupProperty("questionnaire"), "questionnaire");
		criteria.setProjection(projectionList);
		criteria.setResultTransformer(Transformers.aliasToBean(PhenoDataSetCollection.class));
		List<PhenoDataSetCollection>  phenoDataSetCollections = (List<PhenoDataSetCollection>)criteria.list();
		List<PhenoDataSetGroup> phenoDataSetGroups=new ArrayList<PhenoDataSetGroup>();
		for (PhenoDataSetCollection phenoDataSetCollection : phenoDataSetCollections) {
			phenoDataSetGroups.add(phenoDataSetCollection.getQuestionnaire());
		}
		return phenoDataSetGroups;
	}

	public CustomFieldGroup getCustomFieldGroupByNameAndStudy(String name, Study study) {
		Criteria criteria = getSession().createCriteria(CustomFieldGroup.class);
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("study", study));
		
		CustomFieldGroup  result = null; 
		result = (CustomFieldGroup) criteria.uniqueResult();
		return result;
	}

	public PhenoDataSetGroup getPhenoFieldGroupById(Long id) {
		PhenoDataSetGroup phenoDataSetGroup = (PhenoDataSetGroup) getSession().get(PhenoDataSetGroup.class, id);
		return phenoDataSetGroup;
	}

	public List<PhenoDataSetCollection> getSubjectMatchingPhenoCollections(LinkSubjectStudy subject, PhenoDataSetGroup phenoDataSetGroup,Date recordDate) {
		log.info("subject " + subject.getSubjectUID());
		log.info("phenoDataSetGroup " + phenoDataSetGroup.getName());
		log.info("date: " + recordDate);
		Criteria criteria = getSession().createCriteria(PhenoDataSetCollection.class);
		criteria.add(Restrictions.eq("linkSubjectStudy", subject));
		criteria.add(Restrictions.eq("questionnaire", phenoDataSetGroup));
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(recordDate);
		
		//Removing the "Time" section of the Dates as that's not important in this context
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		Date low = cal.getTime();
		cal.add(Calendar.DATE, 1);
		Date high = cal.getTime();
		criteria.add(Restrictions.lt("recordDate", high));
		criteria.add(Restrictions.ge("recordDate", low));
		
		return criteria.list();
	}

	@Override
	public PhenoDataSetCategory getPhenoDataSetCategory(Long id) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.setMaxResults(1);
		return (PhenoDataSetCategory)criteria.uniqueResult();
	}

	@Override
	public List<PhenoDataSetCategory> getAvailableAllCategoryList(Study study,ArkFunction arkFunction) throws ArkSystemException {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		List<PhenoDataSetCategory> phenoDataSetCategoryList = (List<PhenoDataSetCategory>) criteria.list();
		return phenoDataSetCategoryList;
	}

	@Override
	public long getPhenoDataSetCategoryCount(PhenoDataSetCategory phenoDataSetCategoryCriteria) {
		// Handle for study or function not in context
		if (phenoDataSetCategoryCriteria.getStudy() == null || phenoDataSetCategoryCriteria.getArkFunction() == null) {
			return 0;
		}
		Criteria criteria = buildGeneralPhenoDataSetCategoryCritera(phenoDataSetCategoryCriteria);
		criteria.setProjection(Projections.rowCount());
		Long totalCount = (Long) criteria.uniqueResult();
		return totalCount;
	}
	
	protected Criteria buildGeneralPhenoDataSetCategoryCritera(PhenoDataSetCategory phenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		
		// Must be constrained on study and function
		criteria.add(Restrictions.eq("study", phenoDataSetCategory.getStudy()));
		
		criteria.add(Restrictions.eq("arkFunction", phenoDataSetCategory.getArkFunction()));
		
		if (phenoDataSetCategory.getId() != null) {
			criteria.add(Restrictions.eq("id", phenoDataSetCategory.getId()));
		}
	
		if (phenoDataSetCategory.getName() != null) {
			criteria.add(Restrictions.ilike("name", phenoDataSetCategory.getName(), MatchMode.ANYWHERE));
		}
		if (phenoDataSetCategory.getDescription() != null) {
			criteria.add(Restrictions.ilike("description", phenoDataSetCategory.getDescription(), MatchMode.ANYWHERE));
		}
		return criteria;
	}


	@Override
	public List<PhenoDataSetCategory> getAvailableAllCategoryListExceptThis(Study study, ArkFunction arkFunction,PhenoDataSetCategory thisPhenoDataSetCategory)throws ArkSystemException {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.ne("id", thisPhenoDataSetCategory.getId()));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		List<PhenoDataSetCategory> phenoDataSetCategoryList = (List<PhenoDataSetCategory>) criteria.list();
		return phenoDataSetCategoryList;
	}

	@Override
	public List<PhenoDataSetCategory> searchPageablePhenoDataSetCategories(PhenoDataSetCategory phenoDataSetCategoryCriteria, int first,int count) {
		Criteria criteria = buildGeneralPhenoDataSetCategoryCritera(phenoDataSetCategoryCriteria);
		criteria.setFirstResult(first);
		criteria.setMaxResults(count);
		criteria.addOrder(Order.asc("name"));
		List<PhenoDataSetCategory> phenoDataSetCategoryList = (List<PhenoDataSetCategory>) criteria.list();
		return phenoDataSetCategoryList;
	}

	@Override
	public void createPhenoDataSetCategory(PhenoDataSetCategory phenoDataSetCategory)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException {
		getSession().save(phenoDataSetCategory);
		
	}

	@Override
	public void updatePhenoDataSetCategory(PhenoDataSetCategory phenoDataSetCategory)throws ArkSystemException, ArkUniqueException {
		getSession().update(phenoDataSetCategory);
		
	}

	@Override
	public void deletePhenoDataSetCategory(PhenoDataSetCategory phenoDataSetCategory)throws ArkSystemException, EntityCannotBeRemoved {
		getSession().delete(phenoDataSetCategory);
		
	}
	public void createAuditHistory(AuditHistory auditHistory, String userId, Study study) {
		Date date = new Date(System.currentTimeMillis());

		if (userId == null) {// if not forcing a userID manually, get
			// currentuser
			Subject currentUser = SecurityUtils.getSubject();
			auditHistory.setArkUserId((String) currentUser.getPrincipal());
		}
		else {
			auditHistory.setArkUserId(userId);
		}
		if (study == null) {
			Long sessionStudyId = (Long) SecurityUtils.getSubject().getSession().getAttribute(au.org.theark.core.Constants.STUDY_CONTEXT_ID);
			if (sessionStudyId != null && auditHistory.getStudyStatus() == null) {
				auditHistory.setStudyStatus(getStudy(sessionStudyId).getStudyStatus());
			}
			else {

				if (auditHistory.getEntityType().equalsIgnoreCase(au.org.theark.core.Constants.ENTITY_TYPE_STUDY)) {
					Study studyFromDB = getStudy(auditHistory.getEntityId());
					if (studyFromDB != null) {
						auditHistory.setStudyStatus(studyFromDB.getStudyStatus());
					}
				}
			}
		}
		else {
			auditHistory.setStudyStatus(study.getStudyStatus());
		}
		auditHistory.setDateTime(date);
		getSession().save(auditHistory);
	}
	private Study getStudy(Long id) {
		Study study = (Study) getSession().get(Study.class, id);
		return study;
	}
	@Override
	public boolean isPhenoDataSetCategoryUnique(String phenoDataSetCategoryName,Study study, PhenoDataSetCategory phenoDataSetCategoryToUpdate){
		boolean isUnique = true;
		StatelessSession stateLessSession = getStatelessSession();
		Criteria criteria = stateLessSession.createCriteria(CustomFieldCategory.class);
		criteria.add(Restrictions.eq("name", phenoDataSetCategoryName));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", phenoDataSetCategoryToUpdate.getArkFunction()));
		criteria.setMaxResults(1);
		
		PhenoDataSetCategory existingPhenoDataSetCategory = (PhenoDataSetCategory) criteria.uniqueResult();
		
		if( (phenoDataSetCategoryToUpdate.getId() != null && phenoDataSetCategoryToUpdate.getId() > 0)){
			
			if(existingPhenoDataSetCategory != null && !phenoDataSetCategoryToUpdate.getId().equals(existingPhenoDataSetCategory.getId())){
				isUnique = false;
			}
		}else{
			if(existingPhenoDataSetCategory != null){
				isUnique = false;
			}
		}
		stateLessSession.close();
		return isUnique;
	}
	/**
	 * check the Custom field category for the data intergrity.
	 */
	@Override
	public boolean isPhenoDataSetCategoryAlreadyUsed(PhenoDataSetCategory phenoDataSetCategory) {
	/**
	 * if a phenoDatasetCategory been used by the system it should be at least one or more of this table.
	 * PickedPhenoDataSetCategory
	 * LinkPhenoDataSetCategoryField
	 * PhenoDataSetFieldDisplay
	 *  
	 */
		Boolean status1=false,status2=false,status3=false;
		
		StatelessSession stateLessSessionOne = getStatelessSession();
		Criteria criteria = stateLessSessionOne.createCriteria(PickedPhenoDataSetCategory.class);
		ArkFunction arkFunction = iArkCommonService.getArkFunctionByName(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_PHENO_COLLECTION);
		criteria.add(Restrictions.eq("arkFunction",arkFunction ));
		criteria.add(Restrictions.eq("study",phenoDataSetCategory.getStudy() ));
		criteria.add(Restrictions.eq("phenoDataSetCategory", phenoDataSetCategory));
		List<PickedPhenoDataSetCategory> phenoDataSetCategories= (List<PickedPhenoDataSetCategory>) criteria.list();
		if (phenoDataSetCategories.size() > 0){
			status1= true;
		}else{
			status1= false;
		}
		StatelessSession stateLessSessionTwo = getStatelessSession();
		Criteria criteriaTwo = stateLessSessionTwo.createCriteria(LinkPhenoDataSetCategoryField.class);
		criteriaTwo.add(Restrictions.eq("arkFunction", arkFunction));
		criteriaTwo.add(Restrictions.eq("study", phenoDataSetCategory.getStudy()));
		criteriaTwo.add(Restrictions.eq("phenoDataSetCategory", phenoDataSetCategory));
		List<LinkPhenoDataSetCategoryField> linkPhenoDataSetCategoryFields= (List<LinkPhenoDataSetCategoryField>) criteriaTwo.list();
		if (linkPhenoDataSetCategoryFields.size() > 0){
			status2= true;
		}else{
			status2= false;
		}
		StatelessSession stateLessSessionThree = getStatelessSession();
		Criteria criteriaThree = stateLessSessionThree.createCriteria(PhenoDataSetFieldDisplay.class);
		criteriaThree.createAlias("phenoDataSetGroup", "phenoDSG");
		criteriaThree.add(Restrictions.eq("phenoDSG.arkFunction",arkFunction ));
		criteriaThree.add(Restrictions.eq("phenoDSG.study", phenoDataSetCategory.getStudy()));
		criteriaThree.add(Restrictions.eq("phenoDataSetCategory", phenoDataSetCategory));
		List<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplays= (List<PhenoDataSetFieldDisplay>) criteriaThree.list();
		if (phenoDataSetFieldDisplays.size() > 0){
			status3= true;
		}else{
			status3= false;
		}
		return status1 || status2 || status3;
	}
	public PhenoDataSetField getPhenoDataSetField(Long id){
		Criteria criteria = getSession().createCriteria(PhenoDataSetField.class);
		criteria.add(Restrictions.eq("id", id));
		criteria.setMaxResults(1);
		return (PhenoDataSetField)criteria.uniqueResult();	
	}
	public long getPhenoFieldCount(PhenoDataSetField phenofieldcriteria) {
		// Handle for study or function not in context
		if (phenofieldcriteria.getStudy() == null || phenofieldcriteria.getArkFunction() == null) {
				return 0;
		}
			Criteria criteria = buildGeneralPhenoFieldCritera(phenofieldcriteria);
			criteria.setProjection(Projections.rowCount());
			Long totalCount = (Long) criteria.uniqueResult();
			return totalCount;
	}
	/**
	 * Search method to the  fileds.
	 * @param phenoDataSet
	 * @return
	 */
	protected Criteria buildGeneralPhenoFieldCritera(PhenoDataSetField phenoDataSetField) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetField.class);
		
		criteria.add(Restrictions.eq("study", phenoDataSetField.getStudy()));
		criteria.add(Restrictions.eq("arkFunction", phenoDataSetField.getArkFunction()));
		
		
		if(phenoDataSetField.getFieldType()!=null){
			criteria.add(Restrictions.eq("fieldType", phenoDataSetField.getFieldType()));
		}
		if (phenoDataSetField.getId() != null) {
			criteria.add(Restrictions.eq("id", phenoDataSetField.getId()));
		}
		if (phenoDataSetField.getName() != null) {
			criteria.add(Restrictions.ilike("name", phenoDataSetField.getName(), MatchMode.ANYWHERE));
		}
		if (phenoDataSetField.getDescription() != null) {
			criteria.add(Restrictions.ilike("description", phenoDataSetField.getDescription(), MatchMode.ANYWHERE));
		}
		if (phenoDataSetField.getUnitType() != null && phenoDataSetField.getUnitType().getName() != null && phenoDataSetField.getUnitTypeInText() !=null) {
			criteria.createAlias("unitType", "ut");
			criteria.add(Restrictions.ilike("ut.name", phenoDataSetField.getUnitType().getName(), MatchMode.ANYWHERE));
		}
		if(phenoDataSetField.getUnitTypeInText() !=null){
			criteria.add(Restrictions.ilike("unitTypeInText", phenoDataSetField.getUnitTypeInText(),MatchMode.ANYWHERE));
		}
		if (phenoDataSetField.getMinValue() != null) {
			criteria.add(Restrictions.ilike("minValue", phenoDataSetField.getMinValue(), MatchMode.ANYWHERE));
		}
		if (phenoDataSetField.getMaxValue() != null) {
			criteria.add(Restrictions.ilike("maxValue", phenoDataSetField.getMaxValue(), MatchMode.ANYWHERE));
		}
		return criteria;
	}
	
	@Override
	public List<PhenoDataSetCategory> getCategoriesListInPhenoDataSetField(Study study, ArkFunction arkFunction) throws ArkSystemException {
		List<PhenoDataSetCategory> phenoDataSetCategories= new ArrayList<PhenoDataSetCategory>();
		
		return phenoDataSetCategories;
	}
	@SuppressWarnings("unchecked")
	public List<PhenoDataSetField> searchPageablePhenoFields(PhenoDataSetField phenoDataSetCriteria, int first, int count) {
		Criteria criteria = buildGeneralPhenoFieldCritera(phenoDataSetCriteria);
		criteria.setFirstResult(first);
		criteria.setMaxResults(count);
		criteria.addOrder(Order.asc("name"));
		List<PhenoDataSetField> phenoDataSetList = (List<PhenoDataSetField>) criteria.list();
		return phenoDataSetList;
	}
	public PhenoDataSetFieldDisplay getPhenoDataSetFieldDisplayByPhenoDataSet(PhenoDataSetField pheDataSetFieldCriteria){
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetField.id", pheDataSetFieldCriteria.getId()));
		criteria.setMaxResults(1);
		return (PhenoDataSetFieldDisplay)criteria.uniqueResult();
	}
	
	@Override
	public List<PhenoDataSetCategory> getAvailableAllCategoryListInStudy(Study study, ArkFunction arkFunction)throws ArkSystemException {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		List<PhenoDataSetCategory> phenoFiedCategoryList = (List<PhenoDataSetCategory>) criteria.list();
		return phenoFiedCategoryList;
		
	}
	public boolean isPhenoDataSetFieldUnqiue(String phenoFieldName, Study study, PhenoDataSetField phenoFieldToUpdate){
		boolean isUnique = true;
		StatelessSession stateLessSession = getStatelessSession();
		Criteria criteria = stateLessSession.createCriteria(PhenoDataSetField.class);
		criteria.add(Restrictions.eq("name", phenoFieldName));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", phenoFieldToUpdate.getArkFunction()));
		criteria.setMaxResults(1);
		
		PhenoDataSetField existingField = (PhenoDataSetField) criteria.uniqueResult();
		
		if( (phenoFieldToUpdate.getId() != null && phenoFieldToUpdate.getId() > 0)){
			
			if(existingField != null && !phenoFieldToUpdate.getId().equals(existingField.getId())){
				isUnique = false;
			}
		}else{
			if(existingField != null){
				isUnique = false;
			}
		}
		stateLessSession.close();
		return isUnique;
	}
	public void updatePhenoDataSetField(PhenoDataSetField phenoDataSetField) throws  ArkSystemException{
		if(phenoDataSetField.getFieldType().getName().equals(Constants.FIELD_TYPE_NUMBER)) {
			if(phenoDataSetField.getMinValue()!=null){
				phenoDataSetField.setMinValue(phenoDataSetField.getMinValue().replace(",",""));				
			}
			if(phenoDataSetField.getMaxValue()!=null){
				phenoDataSetField.setMaxValue(phenoDataSetField.getMaxValue().replace(",",""));
			}
		}
		getSession().update(phenoDataSetField);
	}
	
	public void updatePhenoDataSetDisplay(PhenoDataSetFieldDisplay phenDataSetFieldDisplay) throws  ArkSystemException{
		getSession().update(phenDataSetFieldDisplay);
	}
	
	@Override
	public void mergePhenoDataSetFieldCategory(PhenoDataSetCategory phenoDataSetCategory)throws ArkSystemException {
		getSession().merge(phenoDataSetCategory);
		
	}
	public List<PhenoDataSetCategory> getAllSubCategoriesOfThisCategory(Study study,ArkFunction arkFunction,PhenoDataSetCategory parentphenoDataSetFieldCategory){
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("parentCategory", parentphenoDataSetFieldCategory));
		return  (List<PhenoDataSetCategory>) criteria.list();
		
	}
	/**
	 * 
	 */
	public void deletePhenoDataSetField(PhenoDataSetField phenoDataSetField) throws ArkSystemException{
		getSession().delete(phenoDataSetField);
	}
	/**
	 * 
	 */
	public void deletePhenoDataSetFieldDisplay(PhenoDataSetFieldDisplay phenoDataSetFieldDisplay) throws ArkSystemException{
		getSession().delete(phenoDataSetFieldDisplay);
	}
	/**
	 * 
	 */
	public void createPhenoDataSetField(PhenoDataSetField phenoDataSetField)throws ArkSystemException{
		getSession().save(phenoDataSetField);
	}
	/**
	 * 
	 */
	public void createPhenoDataSetFieldDisplay(PhenoDataSetFieldDisplay phenoDataSetFieldDisplay)throws ArkSystemException{
		getSession().save(phenoDataSetFieldDisplay);
	}
	/**
	 * 
	 * @param phenoDataSetFieldDisplay
	 * @throws ArkSystemException
	 */
	public void updatePhenoDataSetFieldDisplay(PhenoDataSetFieldDisplay phenoDataSetFieldDisplay)throws ArkSystemException{
		getSession().update(phenoDataSetFieldDisplay);
		
	}

	@Override
	public List<PhenoDataSetField> getPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroup(PhenoDataSetGroup phenoDataSetGroupCriteria) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroupCriteria));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.property("phenoDataSetField"));
		criteria.setProjection(projectionList);
		criteria.addOrder(Order.asc("phenoDataSetFiledOrderNumber"));
		List<PhenoDataSetField> fieldsList = criteria.list();
		return fieldsList;
	}

	@Override
	public void createPhenoDataSetFieldGroup(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO)throws EntityExistsException, ArkSystemException {
		PhenoDataSetGroup phenoDataSetGroup = phenoDataSetFieldGroupVO.getPhenoDataSetGroup();
		Session session = getSession();
		session.save(phenoDataSetGroup);
		session.flush();
		insertToDispalyAndDeleteFromLinkAndPicked(phenoDataSetFieldGroupVO,phenoDataSetGroup, session);	
		log.debug("Saved All PhenoDataSetDisplays for PhenoDataSet Group");	
	}

	@Override
	public void updatePhenoDataSetFieldGroup(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO)throws EntityExistsException, ArkSystemException {
		PhenoDataSetGroup phenoDataSetGroup = phenoDataSetFieldGroupVO.getPhenoDataSetGroup();
		Session session = getSession();
		session.saveOrUpdate(phenoDataSetGroup);//Update phenoDataSetGroup
			Collection<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplayToRemove = getPhenoFieldDisplayToRemove(phenoDataSetGroup);	
			for (PhenoDataSetFieldDisplay phenoDataSetFieldDisplay : phenoDataSetFieldDisplayToRemove) {
				session.delete(phenoDataSetFieldDisplay);
				session.flush();
			}
			insertToDispalyAndDeleteFromLinkAndPicked(phenoDataSetFieldGroupVO, phenoDataSetGroup, session);	
			log.debug("Update PhenoDataSetFieldDisplay for PhenoDataSet Group");
	
	}
	private void insertToDispalyAndDeleteFromLinkAndPicked(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO,PhenoDataSetGroup phenoDataSetGroup, Session session) {
		//Get the Picked Pheno Dataset categories.
		List<PickedPhenoDataSetCategory> phenoDataSetCategories=phenoDataSetFieldGroupVO.getPickedAvailableCategories();
		for (PickedPhenoDataSetCategory pickedPhenoDataSetCategory : phenoDataSetCategories) {
				//Get the Linked Pheno Dataset fields for  PickedPhenoDataSetCategory
				List<LinkPhenoDataSetCategoryField> linkPhenoDataSetCategoryFields=getLinkPhenoDataSetCategoryFieldsForPickedPhenoDataSetCategory(pickedPhenoDataSetCategory);
				if(!linkPhenoDataSetCategoryFields.isEmpty()){
					for (LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField : linkPhenoDataSetCategoryFields) {
						PhenoDataSetFieldDisplay phenoDataSetFieldDisplay = new PhenoDataSetFieldDisplay();
						phenoDataSetFieldDisplay.setPhenoDataSetGroup(phenoDataSetGroup);
						phenoDataSetFieldDisplay.setPhenoDataSetCategory(pickedPhenoDataSetCategory.getPhenoDataSetCategory());
						if(pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory()!=null){
							phenoDataSetFieldDisplay.setParentPhenoDataSetCategory(pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory().getPhenoDataSetCategory());
						}
						phenoDataSetFieldDisplay.setPhenoDataSetCategoryOrderNumber(pickedPhenoDataSetCategory.getOrderNumber());
						phenoDataSetFieldDisplay.setPhenoDataSetField(linkPhenoDataSetCategoryField.getPhenoDataSetField());
						phenoDataSetFieldDisplay.setPhenoDataSetFiledOrderNumber(linkPhenoDataSetCategoryField.getOrderNumber());
						session.save(phenoDataSetFieldDisplay);
					}
				}else{
					PhenoDataSetFieldDisplay phenoDataSetFieldDisplay = new PhenoDataSetFieldDisplay();
					phenoDataSetFieldDisplay.setPhenoDataSetGroup(phenoDataSetGroup);
					phenoDataSetFieldDisplay.setPhenoDataSetCategory(pickedPhenoDataSetCategory.getPhenoDataSetCategory());
					if(pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory()!=null){
						phenoDataSetFieldDisplay.setParentPhenoDataSetCategory(pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory().getPhenoDataSetCategory());
					}
					phenoDataSetFieldDisplay.setPhenoDataSetCategoryOrderNumber(pickedPhenoDataSetCategory.getOrderNumber());
					session.save(phenoDataSetFieldDisplay);
				}
		}
	}
	/**
	 * Creates Collection that will contain the list of new PhenoDataSetField that must be added/linked to the PhenoFieldGroup
	 * @param selectedCustomFields
	 * @param customFieldGroup
	 * @return
	 */
	private ArrayList<PhenoDataSetFieldDisplay> getPhenoDataSetFieldsToAdd(Collection<PhenoDataSetField> selectedPhenoDataSetFields, PhenoDataSetGroup phenoDataSetGroup){
		
		ArrayList<PhenoDataSetFieldDisplay> phenodatasetdisplayList = new ArrayList<PhenoDataSetFieldDisplay>();
		List<PhenoDataSetField> existingPhenoFieldList = getPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroup(phenoDataSetGroup);// Existing List of CustomFieldsthat were linked to this CustomFieldGroup
		ArrayList<PhenoDataSetField> nonProxyPhenoFieldList = new ArrayList<PhenoDataSetField>();
		
		/**
		 * Note:
		 * getCustomFieldsLinkedToCustomFieldGroup() returns a projected List representing CustomField from CustomFieldDisplay. Since CustomField was a lazily loaded object, it is represented as a proxy object.
		 * For us to do a comparison using contains the equals() will fail when the class is compared. To be able to do that we convert to an underlying object before we do the final comparison.
		 * Since Hibernate returns proxy objects for LazyInitialisation when the equals() is invoked the class comparison will fail. 
		 */
		
		for (Object obj : existingPhenoFieldList) {
			if(obj instanceof HibernateProxy){
				PhenoDataSetField psf = (PhenoDataSetField)((HibernateProxy)obj).getHibernateLazyInitializer().getImplementation();
				nonProxyPhenoFieldList.add(psf);
			}
		}

		for (PhenoDataSetField phenoDataSetField : selectedPhenoDataSetFields) {
			if((!nonProxyPhenoFieldList.contains(phenoDataSetField))){
				
				PhenoDataSetFieldDisplay phenoDataSetFieldDisplay = new PhenoDataSetFieldDisplay();
				phenoDataSetFieldDisplay.setPhenoDataSetGroup(phenoDataSetGroup);
				phenoDataSetFieldDisplay.setPhenoDataSetField(phenoDataSetField);
				phenodatasetdisplayList.add(phenoDataSetFieldDisplay);
			}else{
				//Retrieve the customField for the sequence could have changed
				//String name = customField.getName();
				//PhenoDataSetFieldDisplay cfd = iArkCommonService.getCustomFieldDisplayByCustomField(customField);
				PhenoDataSetFieldDisplay pdsfd=getPhenoDataSetFieldDisplayByPhenoDataSetFieldAndGroup(phenoDataSetField, phenoDataSetGroup);
				phenodatasetdisplayList.add(pdsfd);
			}
		}
		return phenodatasetdisplayList;
	}
	
	/**
	 * Determine the list of PhenoFields that was linked to this PhenoDataSetFieldGroup and is not used by anyone and then if this is true add it to a list that will be processed later
	 * for removal.
	 * @param selectedCustomFields
	 * @param customFieldGroup
	 * @return
	 */
	private Collection<PhenoDataSetFieldDisplay> getPhenoFieldDisplayToRemove(PhenoDataSetGroup phenoDataSetGroup){
		
		Collection<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplaysList = getPhenoDataSetFieldDisplayForPhenoDataSetFieldGroup(phenoDataSetGroup);
		Collection<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplayToRemove = new ArrayList<PhenoDataSetFieldDisplay>();
		//To do discard all the used pheno fileds 
		for (PhenoDataSetFieldDisplay phenoDataSetFieldDisplay : phenoDataSetFieldDisplaysList) {
				phenoDataSetFieldDisplayToRemove.add(phenoDataSetFieldDisplay);
		}
		return phenoDataSetFieldDisplayToRemove;
	}
	@Override
	public void deletePhenoDataSetFieldGroup(PhenoDataSetFieldGroupVO phenoDataSetFieldGroupVO) {
		//Delete all the PhenoFieldDisplay Items linked to the Group
		Session session = getSession();
		//Delete Display
		PhenoDataSetGroup phenoDataSetGroup=phenoDataSetFieldGroupVO.getPhenoDataSetGroup();
		Collection<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplayList = getPhenoDataSetFieldDisplayForPhenoDataSetFieldGrroup(phenoDataSetGroup);
		for (PhenoDataSetFieldDisplay phenoDataSetFieldDisplay : phenoDataSetFieldDisplayList) {
			session.delete(phenoDataSetFieldDisplay);
		}
		//Delete picked and linked
		deletePickedCategoriesAndAllTheirChildren(phenoDataSetGroup.getStudy(),phenoDataSetGroup.getArkFunction() , phenoDataSetFieldGroupVO.getArkUser());
		//Delete group.
		session.delete(phenoDataSetFieldGroupVO.getPhenoDataSetGroup());
	}
	private List<PhenoDataSetFieldDisplay> getPhenoDataSetFieldDisplayForPhenoDataSetFieldGrroup(PhenoDataSetGroup phenoDataSetGroup){
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroup));
		criteria.addOrder(Order.asc("phenoDataSetFiledOrderNumber"));
		return criteria.list();
	}
	@Override
	public PhenoDataSetFieldDisplay getPhenoDataSetFieldDisplayByPhenoDataSetFieldAndGroup(PhenoDataSetField phenoDataSetField,PhenoDataSetGroup phenoDataSetGroup){
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetField", phenoDataSetField));
		criteria.add(Restrictions.eq("phenoDataSetGroup", phenoDataSetGroup));
		criteria.setMaxResults(1);
		return (PhenoDataSetFieldDisplay)criteria.uniqueResult();
	}
	@Override
	public long getPhenoDataSetFieldGroupCount(PhenoDataSetGroup phenoDataSetGroup) {
		// Handle for study or function not in context
				if (phenoDataSetGroup.getStudy() == null || phenoDataSetGroup.getArkFunction() == null) {
					return 0L;
				}
				Criteria criteria = buildGenericPhenoDataSetFieldGroupCriteria(phenoDataSetGroup);
				criteria.setProjection(Projections.rowCount());
				Long totalCount = (Long) criteria.uniqueResult();
				return totalCount;
	}
	private Criteria buildGenericPhenoDataSetFieldGroupCriteria(PhenoDataSetGroup phenoDataSetGroup){
		
		Criteria criteria = getSession().createCriteria(PhenoDataSetGroup.class);
		
		criteria.add(Restrictions.eq("study", phenoDataSetGroup.getStudy()));
		criteria.add(Restrictions.eq("arkFunction", phenoDataSetGroup.getArkFunction()));
		
		if (phenoDataSetGroup.getName() != null) {
			criteria.add(Restrictions.ilike("name", phenoDataSetGroup.getName(), MatchMode.ANYWHERE));
		}
		
		if(phenoDataSetGroup.getPublished() != null){
			criteria.add(Restrictions.eq("published", phenoDataSetGroup.getPublished()));
		}
		return criteria;
		
	}

	@Override
	public List<PhenoDataSetField> getPhenoDataSetFieldList(PhenoDataSetField phenoDataSetFieldCriteria) {
		Criteria criteria = buildGeneralPhenoFieldCritera(phenoDataSetFieldCriteria);
		// Return fields ordered alphabetically
		criteria.addOrder(Order.asc("name"));
		List<PhenoDataSetField> phenoDataSetFieldList = (List<PhenoDataSetField>) criteria.list();
		//log.warn("custom field criteria (just using name got a list of size " + customFieldList.size());
		return phenoDataSetFieldList;
	}

	@Override
	public List<PhenoDataSetGroup> getPhenoDataSetGroups(PhenoDataSetGroup phenoDataSetGroup, int first, int count) {
		Criteria criteria = buildGenericPhenoDataSetFieldGroupCriteria(phenoDataSetGroup);
		criteria.setFirstResult(first);
		criteria.setMaxResults(count);
		List<PhenoDataSetGroup> list = (List<PhenoDataSetGroup>)criteria.list();
		return list;	
	}
	public List<PickedPhenoDataSetCategory> getPickedPhenoDataSetCategories(Study study,ArkFunction arkFunction,ArkUser arkUser){
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		criteria.addOrder(Order.asc("orderNumber"));
		return (List<PickedPhenoDataSetCategory>)criteria.list();
	}

	@Override
	public List<PhenoDataSetCategory> getAvailablePhenoCategoryListNotPicked(Study study, ArkFunction arkFunctionPhenoCat,ArkFunction arkFunctionPhenoCollection,ArkUser arkUser) throws ArkSystemException {
		
		List<PickedPhenoDataSetCategory> pickedPhenoSetCatLst=getPickedPhenoDataSetCategories(study, arkFunctionPhenoCollection,arkUser);
		List<Long> pickedPhenoDataIdLst=new ArrayList<Long>();
		for (PickedPhenoDataSetCategory pickedPhenoDataSetCategory : pickedPhenoSetCatLst) {
			pickedPhenoDataIdLst.add(pickedPhenoDataSetCategory.getPhenoDataSetCategory().getId());
		}
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunctionPhenoCat));
		criteria.add(Restrictions.eq("study", study));
		if(!pickedPhenoDataIdLst.isEmpty()) {
			criteria.add(Restrictions.not(Restrictions.in("id", pickedPhenoDataIdLst)));
		}
		return criteria.list();
	}

	@Override
	public void createPickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException, EntityExistsException {
		getSession().save(pickedPhenoDataSetCategory);
	}
	@Override
	public void deletePickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory)throws ArkSystemException, EntityCannotBeRemoved {
		getSession().delete(pickedPhenoDataSetCategory);
		
	}
	@Override
	public PickedPhenoDataSetCategory getPickedPhenoDataSetCategoryFromPhenoDataSetCategory(Study study, ArkFunction arkFunction,ArkUser arkUser,PhenoDataSetCategory phenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		criteria.add(Restrictions.eq("phenoDataSetCategory", phenoDataSetCategory));
		return (PickedPhenoDataSetCategory)criteria.uniqueResult();
	}
	@Override
	public void deleteLinkPhenoDataSetCategoryField(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField)throws ArkSystemException, EntityCannotBeRemoved {
		getSession().delete(linkPhenoDataSetCategoryField);
	}
	@Override
	public void createLinkPhenoDataSetCategoryField(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException, EntityExistsException {
		getSession().save(linkPhenoDataSetCategoryField);
	}
	/**
	 * Shows all the available pheno fields which not linked with categories.
	 */
	@Override
	public List<PhenoDataSetField> getAvailablePhenoFieldListNotInLinked(Study study, ArkFunction arkFunctionPhenoField,ArkFunction arkFunctionPhenoCollection,ArkUser arkUser) throws ArkSystemException {
		List<LinkPhenoDataSetCategoryField> linkPhenoDataSetCategoryFields=getLinkPhenoDataSetCategoryFieldLst(study, arkFunctionPhenoCollection,arkUser);
		List<Long> linkedPhenoDataIdLst=new ArrayList<Long>();
		for (LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField : linkPhenoDataSetCategoryFields) {
			linkedPhenoDataIdLst.add(linkPhenoDataSetCategoryField.getPhenoDataSetField().getId());
		}
		Criteria criteria = getSession().createCriteria(PhenoDataSetField.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunctionPhenoField));
		criteria.add(Restrictions.eq("study", study));
		if(!linkedPhenoDataIdLst.isEmpty()) {
			criteria.add(Restrictions.not(Restrictions.in("id", linkedPhenoDataIdLst)));
		}
		return criteria.list();
	}
	
	/**
	 * Shows all the Linked Pheno data categories.
	 */
	@Override
	public List<LinkPhenoDataSetCategoryField> getLinkPhenoDataSetCategoryFieldLst(Study study, ArkFunction arkFunction,ArkUser arkUser) {
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		return criteria.list();
	}

	@Override
	public void updatePickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException {
		getSession().update(pickedPhenoDataSetCategory);
		
	}

	@Override
	public List<PhenoDataSetField> getLinkedPhenoDataSetFieldsForSelectedCategories(Study study, ArkFunction arkFunction,ArkUser arkUser,List<PhenoDataSetCategory> phenoDataSetCategories) {
		List<LinkPhenoDataSetCategoryField> linkPhenoDataSetCategoryFields=new ArrayList<LinkPhenoDataSetCategoryField>();
		List<PhenoDataSetField> sumofPhenoDataSetFields=new ArrayList<PhenoDataSetField>();
			for (PhenoDataSetCategory phenoDataSetCategory : phenoDataSetCategories) {
				Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
				criteria.add(Restrictions.eq("study", study));
				criteria.add(Restrictions.eq("arkFunction", arkFunction));
				criteria.add(Restrictions.eq("arkUser", arkUser));
				criteria.add(Restrictions.eq("phenoDataSetCategory",phenoDataSetCategory));
				criteria.addOrder(Order.asc("orderNumber"));
				linkPhenoDataSetCategoryFields=(List<LinkPhenoDataSetCategoryField>)criteria.list();
				for (LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField : linkPhenoDataSetCategoryFields) {
					sumofPhenoDataSetFields.add(linkPhenoDataSetCategoryField.getPhenoDataSetField());
				}
				linkPhenoDataSetCategoryFields.clear();
			}
		return sumofPhenoDataSetFields;
		
	}

	@Override
	public LinkPhenoDataSetCategoryField getLinkPhenoDataSetCategoryField(Study study, ArkFunction arkFunction,ArkUser arkUser,PhenoDataSetCategory phenoDataSetCategory,PhenoDataSetField phenoDataSetField) {
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("phenoDataSetCategory",phenoDataSetCategory));
		criteria.add(Restrictions.eq("phenoDataSetField",phenoDataSetField));
		return (LinkPhenoDataSetCategoryField)criteria.uniqueResult();
		
	}

	@Override
	public boolean isSelectedCategoriesAlreadyAssignedToFields(Study study, ArkFunction arkFunction,ArkUser arkUser,List<PhenoDataSetCategory> phenoDataSetCategories) {
		return !getLinkedPhenoDataSetFieldsForSelectedCategories(study, arkFunction,arkUser, phenoDataSetCategories).isEmpty(); 
	}

	@Override
	public Long getNextAvailbleNumberForPickedCategory(Study study,ArkFunction arkFunction, ArkUser arkUser) {
		Long maxNumber;
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		//criteria.add(Restrictions.isNull("parentPickedPhenoDataSetCategory"));
	    criteria.setProjection(Projections.max("orderNumber"));
	    maxNumber= (Long)criteria.uniqueResult();
	    if(maxNumber!=null){
	    	return ++maxNumber;
	    }else{
	    	return new Long(1);
	    }
		
	}

	@Override
	public PickedPhenoDataSetCategory getSwapOverPickedPhenoDataSetCategoryForUpButton(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", pickedPhenoDataSetCategory.getArkFunction()));
		criteria.add(Restrictions.eq("study", pickedPhenoDataSetCategory.getStudy()));
		criteria.add(Restrictions.eq("arkUser", pickedPhenoDataSetCategory.getArkUser()));
		if(pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory()!=null){
			criteria.add(Restrictions.eq("parentPickedPhenoDataSetCategory", pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory()));
		}else{
			criteria.add(Restrictions.isNull("parentPickedPhenoDataSetCategory"));
		}	
		criteria.add(Restrictions.lt("orderNumber",pickedPhenoDataSetCategory.getOrderNumber()));
		criteria.addOrder(Order.desc("orderNumber"));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		List<PickedPhenoDataSetCategory>  pickedPhenoDataSetCategories=(List<PickedPhenoDataSetCategory>)criteria.list();
		if(pickedPhenoDataSetCategories.size() > 0){
			return pickedPhenoDataSetCategories.get(0);
		}else{
			return null;
		}
	}
	@Override
	public PickedPhenoDataSetCategory getSwapOverPickedPhenoDataSetCategoryForDownButton(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) {
			Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
			criteria.add(Restrictions.eq("arkFunction", pickedPhenoDataSetCategory.getArkFunction()));
			criteria.add(Restrictions.eq("study", pickedPhenoDataSetCategory.getStudy()));
			criteria.add(Restrictions.eq("arkUser", pickedPhenoDataSetCategory.getArkUser()));
			if(pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory()!=null){
				criteria.add(Restrictions.eq("parentPickedPhenoDataSetCategory", pickedPhenoDataSetCategory.getParentPickedPhenoDataSetCategory()));
			}else{
				criteria.add(Restrictions.isNull("parentPickedPhenoDataSetCategory"));
			}
			criteria.add(Restrictions.gt("orderNumber",pickedPhenoDataSetCategory.getOrderNumber()));
			criteria.addOrder(Order.asc("orderNumber"));
			criteria.setFirstResult(0);
			criteria.setMaxResults(1);
			List<PickedPhenoDataSetCategory>  pickedPhenoDataSetCategories=(List<PickedPhenoDataSetCategory>)criteria.list();
			if(pickedPhenoDataSetCategories.size() > 0){
				return pickedPhenoDataSetCategories.get(0);
			}else{
				return null;
			}
	}
	@Override
	public Long getNextAvailbleNumberForAssignedField(Study study,ArkFunction arkFunction, ArkUser arkUser,PhenoDataSetCategory phenoDataSetCategory) {
		Long maxNumber;
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		criteria.add(Restrictions.eq("phenoDataSetCategory", phenoDataSetCategory));
	    criteria.setProjection(Projections.max("orderNumber"));
	    maxNumber= (Long)criteria.uniqueResult();
	    if(maxNumber!=null){
	    	return ++maxNumber;
	    }else{
	    	return new Long(1);
	    }
	}

	@Override
	public LinkPhenoDataSetCategoryField getSwapOverPhenoDataSetFieldForUpButton(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField) {
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("arkFunction", linkPhenoDataSetCategoryField.getArkFunction()));
		criteria.add(Restrictions.eq("study", linkPhenoDataSetCategoryField.getStudy()));
		criteria.add(Restrictions.eq("arkUser", linkPhenoDataSetCategoryField.getArkUser()));
		criteria.add(Restrictions.eq("phenoDataSetCategory", linkPhenoDataSetCategoryField.getPhenoDataSetCategory()));
		criteria.add(Restrictions.lt("orderNumber",linkPhenoDataSetCategoryField.getOrderNumber()));
		criteria.addOrder(Order.desc("orderNumber"));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		List<LinkPhenoDataSetCategoryField>  linkPhenoDataSetCategoryFields=(List<LinkPhenoDataSetCategoryField>)criteria.list();
		if(linkPhenoDataSetCategoryFields.size() > 0){
			return linkPhenoDataSetCategoryFields.get(0);
		}else{
			return null;
		}
	}

	@Override
	public LinkPhenoDataSetCategoryField getSwapOverPhenoDataSetFieldForDownButton(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField) {
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("arkFunction", linkPhenoDataSetCategoryField.getArkFunction()));
		criteria.add(Restrictions.eq("study", linkPhenoDataSetCategoryField.getStudy()));
		criteria.add(Restrictions.eq("arkUser", linkPhenoDataSetCategoryField.getArkUser()));
		criteria.add(Restrictions.eq("phenoDataSetCategory", linkPhenoDataSetCategoryField.getPhenoDataSetCategory()));
		criteria.add(Restrictions.gt("orderNumber",linkPhenoDataSetCategoryField.getOrderNumber()));
		criteria.addOrder(Order.asc("orderNumber"));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		List<LinkPhenoDataSetCategoryField>  linkPhenoDataSetCategoryFields=(List<LinkPhenoDataSetCategoryField>)criteria.list();
		if(linkPhenoDataSetCategoryFields.size() > 0){
			return linkPhenoDataSetCategoryFields.get(0);
		}else{
			return null;
		}
	}

	@Override
	public void updateLinkPhenoDataSetCategoryField(LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField)throws ArkSystemException, ArkRunTimeUniqueException,ArkRunTimeException {
		getSession().update(linkPhenoDataSetCategoryField);
		
	}

	@Override
	public PhenoDataSetCategory getPhenoDataSetCategoryForAssignedPhenoDataSetField(Study study, ArkFunction arkFunction, ArkUser arkUser,PhenoDataSetField phenoDataSetField) {
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		criteria.add(Restrictions.eq("phenoDataSetField", phenoDataSetField));
		LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField=(LinkPhenoDataSetCategoryField)criteria.uniqueResult();
		return linkPhenoDataSetCategoryField.getPhenoDataSetCategory();
	}

	@Override
	public Boolean isPickedPhenoDataSetCategoryIsAParentOfAnotherCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", pickedPhenoDataSetCategory.getArkFunction()));
		criteria.add(Restrictions.eq("study", pickedPhenoDataSetCategory.getStudy()));
		criteria.add(Restrictions.eq("arkUser", pickedPhenoDataSetCategory.getArkUser()));
		criteria.add(Restrictions.eq("parentPickedPhenoDataSetCategory", pickedPhenoDataSetCategory));
		return !((List<PickedPhenoDataSetCategory>)criteria.list()).isEmpty();
	}

	@Override
	public List<PickedPhenoDataSetCategory> getChildrenOfPickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", pickedPhenoDataSetCategory.getArkFunction()));
		criteria.add(Restrictions.eq("study", pickedPhenoDataSetCategory.getStudy()));
		criteria.add(Restrictions.eq("arkUser", pickedPhenoDataSetCategory.getArkUser()));
		criteria.add(Restrictions.eq("parentPickedPhenoDataSetCategory", pickedPhenoDataSetCategory));
		criteria.addOrder(Order.asc("orderNumber"));
		return (List<PickedPhenoDataSetCategory>)criteria.list();
	}

	@Override
	public List<PickedPhenoDataSetCategory> getAllParentPickedPhenoDataSetCategories(Study study, ArkFunction arkFunction, ArkUser arkUser) {
		Criteria criteria = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkUser", arkUser));
		criteria.add(Restrictions.isNull("parentPickedPhenoDataSetCategory"));
		criteria.addOrder(Order.asc("orderNumber"));
		return (List<PickedPhenoDataSetCategory>)criteria.list();
	}

	@Override
	public List<LinkPhenoDataSetCategoryField> getLinkPhenoDataSetCategoryFieldsForPickedPhenoDataSetCategory(PickedPhenoDataSetCategory pickedPhenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteria.add(Restrictions.eq("study", pickedPhenoDataSetCategory.getStudy()));
		criteria.add(Restrictions.eq("arkFunction", pickedPhenoDataSetCategory.getArkFunction()));
		criteria.add(Restrictions.eq("arkUser", pickedPhenoDataSetCategory.getArkUser()));
		criteria.add(Restrictions.eq("phenoDataSetCategory",pickedPhenoDataSetCategory.getPhenoDataSetCategory()));
		criteria.addOrder(Order.asc("orderNumber"));
		return(List<LinkPhenoDataSetCategoryField>)criteria.list();
	}
	@Override
	public PhenoDataSetCategory getPhenoDataFieldCategoryByNameStudyAndArkFunction(String name, Study study, ArkFunction arkFunction) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetCategory.class);
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		return (PhenoDataSetCategory) criteria.uniqueResult();
	}

	@Override
	public PhenoDataSetField getPhenoDataSetFieldByNameStudyArkFunction(String name, Study study, ArkFunction arkFunction) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetField.class);
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		return (PhenoDataSetField) criteria.uniqueResult();
	}

	@Override
	public void createPhenoDataSetFieldCategoryUpload(PhenoDataSetFieldCategoryUpload phenoDataSetFieldCategoryUpload) {
		getSession().save(phenoDataSetFieldCategoryUpload);
	}

	@Override
	public void createPhenoDataSetFieldUpload(PhenoFieldUpload phenoFieldUpload) {
		getSession().save(phenoFieldUpload);
	}

	/**
	 * This return both categories and the fields of phenodatasetfieldsDisplay tables.
	 * Note:Not only the fields.          
	 */
	@Override
	public List<PhenoDataSetFieldDisplay> getPhenoDataSetFieldDisplayForPhenoDataSetFieldGroup(PhenoDataSetGroup phenoDataSetGroup) {
			Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
			criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroup));
			//Ordering first from the category and then from the field
			criteria.addOrder(Order.asc("phenoDataSetCategoryOrderNumber")).addOrder(Order.asc("phenoDataSetFiledOrderNumber"));
			return criteria.list();
	}

	@Override
	public void deletePickedCategoriesAndAllTheirChildren(Study study,ArkFunction arkFunction, ArkUser arkUser) {
		//Delete all fields
		Criteria criteriaField = getSession().createCriteria(LinkPhenoDataSetCategoryField.class);
		criteriaField.add(Restrictions.eq("study", study));
		criteriaField.add(Restrictions.eq("arkFunction",arkFunction ));
		criteriaField.add(Restrictions.eq("arkUser", arkUser));
		List<LinkPhenoDataSetCategoryField> linkPhenoDataSetCategoryFields=(List<LinkPhenoDataSetCategoryField>)criteriaField.list();
		for (LinkPhenoDataSetCategoryField linkPhenoDataSetCategoryField : linkPhenoDataSetCategoryFields) {
			getSession().delete(linkPhenoDataSetCategoryField);
		}
		//Delete all categories.
		Criteria criteriaCategory = getSession().createCriteria(PickedPhenoDataSetCategory.class);
		criteriaCategory.add(Restrictions.eq("study", study));
		criteriaCategory.add(Restrictions.eq("arkFunction",arkFunction ));
		criteriaCategory.add(Restrictions.eq("arkUser", arkUser));
		List<PickedPhenoDataSetCategory> pickedPhenoDataSetCategories=(List<PickedPhenoDataSetCategory>)criteriaCategory.list();
		for (PickedPhenoDataSetCategory pickedPhenoDataSetCategory : pickedPhenoDataSetCategories) {
			getSession().delete(pickedPhenoDataSetCategory);
		}
		getSession().flush();
	}

	@Override
	public List<PhenoDataSetFieldDisplay> getPhenoFieldDisplaysIn(List<String> fieldNameCollection, Study study,ArkFunction arkFunction, PhenoDataSetGroup phenoDataSetGroup) {
			if (fieldNameCollection == null || fieldNameCollection.isEmpty()) {
				return new ArrayList<PhenoDataSetFieldDisplay>();
			}
			else {
				List<String> lowerCaseNames = new ArrayList<String>();
				for (String name : fieldNameCollection) {
					lowerCaseNames.add(name.toLowerCase());
				}
				/*String queryString = "select cfd from PhenoDataSetFieldDisplay cfd " + 
						" where cfd.customFieldGroup =:customFieldGroup and customField.id in ( " + 
						" SELECT id from CustomField cf " + 
						" where cf.study =:study " + " and lower(cf.name) in (:names) " + " and cf.arkFunction =:arkFunction )";
				*/String queryString = "select pdsfd from PhenoDataSetFieldDisplay pdsfd " + 
						" where pdsfd.phenoDataSetGroup =:phenoDataSetGroup and phenoDataSetField.id in ( " + 
						" SELECT id from PhenoDataSetField pdsf " + 
						" where pdsf.study =:study " + " and lower(pdsf.name) in (:names) " + " and pdsf.arkFunction =:arkFunction )";
				Query query = getSession().createQuery(queryString); 
				query.setParameter("study", study);
				// query.setParameterList("names", fieldNameCollection);
				query.setParameterList("names", lowerCaseNames);
				query.setParameter("arkFunction", arkFunction);
				query.setParameter("phenoDataSetGroup", phenoDataSetGroup);
				return query.list();
			}
	}

	@Override
	public List<PhenoDataSetFieldDisplay> getPhenoFieldDisplaysIn(Study study,ArkFunction arkFunction) {
		String queryString = "select pdsfd from PhenoDataSetFieldDisplay pdsfd " +
				" where phenoDataSetField.id in ( " +
				" SELECT id from PhenoDataSetField pdsf " +
				" where pdsf.study =:study " + " and pdsf.arkFunction =:arkFunction )";
		Query query = getSession().createQuery(queryString);
		query.setParameter("study", study);
		query.setParameter("arkFunction", arkFunction);
		return query.list();
	}

	@Override
	public long getPhenoFieldGroupCount(Study study,ArkFunction arkFunction,Boolean status) {
		// Handle for study or function not in context
		/*if (phenoDataSetGroup.getStudy() == null || phenoDataSetGroup.getArkFunction() == null) {
			return 0L;
		}*/
		//Criteria criteria = buildGenericPhenoFieldGroupCriteria(phenoDataSetGroup);
		Criteria criteria = getSession().createCriteria(PhenoDataSetGroup.class);
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("published", true));
		criteria.setProjection(Projections.rowCount());
		Long totalCount = (Long) criteria.uniqueResult();
		return totalCount;
	}
	private Criteria buildGenericPhenoFieldGroupCriteria(PhenoDataSetGroup phenoDataSetGroup){
		
		Criteria criteria = getSession().createCriteria(PhenoDataSetGroup.class);
		
		criteria.add(Restrictions.eq("study", phenoDataSetGroup.getStudy()));
		criteria.add(Restrictions.eq("arkFunction", phenoDataSetGroup.getArkFunction()));
		
		if (phenoDataSetGroup.getName() != null) {
			criteria.add(Restrictions.ilike("name", phenoDataSetGroup.getName(), MatchMode.ANYWHERE));
		}
		
		if(phenoDataSetGroup.getPublished() != null){
			criteria.add(Restrictions.eq("published", phenoDataSetGroup.getPublished()));
		}
		return criteria;
		
	}
	public PhenoDataSetField getPhenoDataSetFieldByNameStudyPFG(String phenoFieldName, Study study, ArkFunction arkFunction, PhenoDataSetGroup phenoDataSetGroup)throws ArkRunTimeException,ArkSystemException{
		
		Query q = getSession().createQuery("Select phenoDataSetField from PhenoDataSetField phenoDataSetField " +
		" where phenoDataSetField.name =:phenoDataSetField " +
		" and lower(phenoDataSetField.study) =lower(:study) " +
		" and phenoDataSetField.arkFunction =:arkFunction " +
		" and exists (" +
		"				from PhenoDataSetFieldDisplay as phenoDataSetFieldDisplay " +
		"				where phenoDataSetFieldDisplay.phenoDataSetField = phenoDataSetField " +
		"				and phenoDataSetFieldDisplay.phenoDataSetGroup =:phenoDataSetGroup ) ");
		q.setParameter("phenoDataSetField", phenoFieldName);
		q.setParameter("study", study);
		q.setParameter("arkFunction", arkFunction);
		q.setParameter("phenoDataSetGroup", phenoDataSetGroup);
		List<PhenoDataSetField> results =null;
		try{
			 results = q.list();
		}catch(HibernateException hiberEx){ 
			throw new ArkRunTimeException("Problem finding the phono data set fields.");
		}
		if(results.size()>0){
			return (PhenoDataSetField)results.get(0);
		}
		return null;
	}
	public List<PhenoDataSetGroup> getPhenoDataSetFieldGroups(PhenoDataSetGroup phenoDataSetGroup, int first, int count){
		
		Criteria criteria = buildGenericPhenoFieldGroupCriteria(phenoDataSetGroup);
		criteria.setFirstResult(first);
		criteria.setMaxResults(count);
		List<PhenoDataSetGroup> list = (List<PhenoDataSetGroup>)criteria.list();
		return list;
	}
	
	@Override
	public List<PhenoDataSetFieldDisplay> getPhenoDataSetFieldDisplayForPhenoDataSetFieldGroupOrderByPhenoDataSetCategory(PhenoDataSetGroup phenoDataSetGroup) {
			Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
			criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroup));
			ProjectionList projectionList = Projections.projectionList();
			projectionList.add(Projections.groupProperty("phenoDataSetGroup"), "phenoDataSetGroup");
			projectionList.add(Projections.groupProperty("phenoDataSetCategory"), "phenoDataSetCategory");
			projectionList.add(Projections.groupProperty("parentPhenoDataSetCategory"), "parentPhenoDataSetCategory");
			projectionList.add(Projections.groupProperty("phenoDataSetCategoryOrderNumber"), "phenoDataSetCategoryOrderNumber");
			projectionList.add(Projections.groupProperty("phenoDataSetField"), "phenoDataSetField");
			criteria.setProjection(projectionList);
			criteria.addOrder(Order.asc("phenoDataSetCategoryOrderNumber"));
			criteria.setResultTransformer(Transformers.aliasToBean(PhenoDataSetFieldDisplay.class));
			return (List<PhenoDataSetFieldDisplay>)criteria.list();
	}
	@Override
	public List<PhenoDataSetField> getPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroupAndPhenoDataSetCategory(PhenoDataSetGroup phenoDataSetGroupCriteria,PhenoDataSetCategory phenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroupCriteria));
		criteria.add(Restrictions.eq("phenoDataSetCategory",phenoDataSetCategory));
		criteria.add(Restrictions.isNotNull("phenoDataSetField"));
		criteria.addOrder(Order.asc("phenoDataSetFiledOrderNumber"));
		List<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplays = (List<PhenoDataSetFieldDisplay>)criteria.list();
		List<PhenoDataSetField>  phenoDataSetFields=new ArrayList<PhenoDataSetField>();
		for (PhenoDataSetFieldDisplay phenoDataSetFieldDisplay : phenoDataSetFieldDisplays) {
			phenoDataSetFields.add(phenoDataSetFieldDisplay.getPhenoDataSetField());
		}
		return phenoDataSetFields;
	}
	@Override
	public List<Boolean> getPublishedSatusLst(Study study,ArkFunction arkFunction){
		Criteria criteria = getSession().createCriteria(PhenoDataSetGroup.class);
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		ProjectionList projectionList = Projections.projectionList();
		projectionList.add(Projections.groupProperty("published"), "published");
		criteria.setProjection(projectionList);
		criteria.setResultTransformer(Transformers.aliasToBean(PhenoDataSetGroup.class));
		List<PhenoDataSetGroup> phenoDataSetGroups=(List<PhenoDataSetGroup>)criteria.list();
		List<Boolean> pubishStatusLst=new ArrayList<Boolean>();
		for (PhenoDataSetGroup phenoDataSetGroup : phenoDataSetGroups) {
			pubishStatusLst.add(phenoDataSetGroup.getPublished());
		}
		return pubishStatusLst;
	}
	@Override
	public PhenoDataSetCategory getPhenoDataSetCategoryById(Long id) {
		PhenoDataSetCategory phenoDataSetCategory = (PhenoDataSetCategory) getSession().get(PhenoDataSetCategory.class, id);
		return phenoDataSetCategory;
	}

	@Override
	public boolean isPhenoDataSetFieldCategoryBeingUsed(PhenoDataSetCategory phenoDataSetCategory) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetCategory",phenoDataSetCategory));
		return ((List<PhenoDataSetFieldDisplay>)criteria.list()).size()>0;
	}

	@Override
	public List<PhenoDataSetField> getAllPhenoDataSetFieldsLinkedToPhenoDataSetFieldGroup(PhenoDataSetGroup phenoDataSetGroupCriteria) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetFieldDisplay.class);
		criteria.add(Restrictions.eq("phenoDataSetGroup",phenoDataSetGroupCriteria));
		criteria.add(Restrictions.isNotNull("phenoDataSetField"));
		criteria.addOrder(Order.asc("phenoDataSetCategoryOrderNumber")).addOrder(Order.asc("phenoDataSetFiledOrderNumber"));
		List<PhenoDataSetFieldDisplay> phenoDataSetFieldDisplays = (List<PhenoDataSetFieldDisplay>)criteria.list();
		List<PhenoDataSetField>  phenoDataSetFields=new ArrayList<PhenoDataSetField>();
		for (PhenoDataSetFieldDisplay phenoDataSetFieldDisplay : phenoDataSetFieldDisplays) {
			phenoDataSetFields.add(phenoDataSetFieldDisplay.getPhenoDataSetField());
		}
		return phenoDataSetFields;
	}

	@Override
	public boolean isInEncodedValues(PhenoDataSetField phenoDataSetField, String value) {
		if(phenoDataSetField.getMissingValue()!=null && value!=null && value.trim().equalsIgnoreCase(phenoDataSetField.getMissingValue().trim())) {
			return true;
		}
		// Validate if encoded values is definedisInEncodedValues, and not a DATE fieldType
		if (phenoDataSetField != null && !phenoDataSetField.getFieldType().getName().equalsIgnoreCase(Constants.FIELD_TYPE_DATE)) {
			try {
				StringTokenizer stringTokenizer = new StringTokenizer(phenoDataSetField.getEncodedValues(), Constants.ENCODED_VALUES_TOKEN);
				// Iterate through all discrete defined values and compare to field data value
				while (stringTokenizer.hasMoreTokens()) {
					String encodedValueToken = stringTokenizer.nextToken();
					StringTokenizer encodedValueSeparator = new StringTokenizer(encodedValueToken, Constants.ENCODED_VALUES_SEPARATOR);
					String encodedValue = encodedValueSeparator.nextToken().trim();
					if (encodedValue.equalsIgnoreCase(value)) {
						return true;
					}
				}
			}
			catch (NullPointerException npe) {
				log.error("Field data null format exception " + npe.getMessage());
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isSameNameFieldGroupExsistsForTheStudy(String name,Study study,ArkFunction arkFunction) {
		Criteria criteria = getSession().createCriteria(PhenoDataSetGroup.class);
		criteria.add(Restrictions.eq("study", study));
		criteria.add(Restrictions.eq("arkFunction", arkFunction));
		criteria.add(Restrictions.eq("name", name));
		return ((List<PhenoDataSetGroup>)criteria.list()).size()>0;
	}

	@Override
	public void deletePhenoDatasetData(PhenoDataSetCollection phenoDataSetCollection) {
		Set<PhenoDataSetData> phenoDataSetDatas= phenoDataSetCollection.getPhenoDataSetData();
		for (PhenoDataSetData phenoDataSetData : phenoDataSetDatas) {
			getSession().delete(phenoDataSetData);
		}
	}
}
