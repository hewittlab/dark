package au.org.theark.gdmi.model.entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * MetaData entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "META_DATA", schema = "GDMI")
public class MetaData implements java.io.Serializable {

	// Fields

	private long id;
	private MetaDataField metaDataField;
	private Collection collection;
	private String value;
	private String userId;
	private String insertTime;
	private String updateUserId;
	private String updateTime;
	private Set<SubjectMarkerMetaData> subjectMarkerMetaDatas = new HashSet<SubjectMarkerMetaData>(
			0);
	private Set<MarkerMetaData> markerMetaDatas = new HashSet<MarkerMetaData>(0);
	private Set<SubjectMetaData> subjectMetaDatas = new HashSet<SubjectMetaData>(
			0);

	// Constructors

	/** default constructor */
	public MetaData() {
	}

	/** minimal constructor */
	public MetaData(long id, MetaDataField metaDataField,
			Collection collection, String userId, String insertTime) {
		this.id = id;
		this.metaDataField = metaDataField;
		this.collection = collection;
		this.userId = userId;
		this.insertTime = insertTime;
	}

	/** full constructor */
	public MetaData(long id, MetaDataField metaDataField,
			Collection collection, String value, String userId,
			String insertTime, String updateUserId, String updateTime,
			Set<SubjectMarkerMetaData> subjectMarkerMetaDatas,
			Set<MarkerMetaData> markerMetaDatas,
			Set<SubjectMetaData> subjectMetaDatas) {
		this.id = id;
		this.metaDataField = metaDataField;
		this.collection = collection;
		this.value = value;
		this.userId = userId;
		this.insertTime = insertTime;
		this.updateUserId = updateUserId;
		this.updateTime = updateTime;
		this.subjectMarkerMetaDatas = subjectMarkerMetaDatas;
		this.markerMetaDatas = markerMetaDatas;
		this.subjectMetaDatas = subjectMetaDatas;
	}

	// Property accessors
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "META_DATA_FIELD_ID", nullable = false)
	public MetaDataField getMetaDataField() {
		return this.metaDataField;
	}

	public void setMetaDataField(MetaDataField metaDataField) {
		this.metaDataField = metaDataField;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COLLECTION_ID", nullable = false)
	public Collection getCollection() {
		return this.collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	@Column(name = "VALUE", length = 2000)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Column(name = "USER_ID", nullable = false, length = 50)
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "INSERT_TIME", nullable = false)
	public String getInsertTime() {
		return this.insertTime;
	}

	public void setInsertTime(String insertTime) {
		this.insertTime = insertTime;
	}

	@Column(name = "UPDATE_USER_ID", length = 50)
	public String getUpdateUserId() {
		return this.updateUserId;
	}

	public void setUpdateUserId(String updateUserId) {
		this.updateUserId = updateUserId;
	}

	@Column(name = "UPDATE_TIME")
	public String getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "metaData")
	public Set<SubjectMarkerMetaData> getSubjectMarkerMetaDatas() {
		return this.subjectMarkerMetaDatas;
	}

	public void setSubjectMarkerMetaDatas(
			Set<SubjectMarkerMetaData> subjectMarkerMetaDatas) {
		this.subjectMarkerMetaDatas = subjectMarkerMetaDatas;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "metaData")
	public Set<MarkerMetaData> getMarkerMetaDatas() {
		return this.markerMetaDatas;
	}

	public void setMarkerMetaDatas(Set<MarkerMetaData> markerMetaDatas) {
		this.markerMetaDatas = markerMetaDatas;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "metaData")
	public Set<SubjectMetaData> getSubjectMetaDatas() {
		return this.subjectMetaDatas;
	}

	public void setSubjectMetaDatas(Set<SubjectMetaData> subjectMetaDatas) {
		this.subjectMetaDatas = subjectMetaDatas;
	}

}