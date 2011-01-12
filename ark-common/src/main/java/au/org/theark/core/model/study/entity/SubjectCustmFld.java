package au.org.theark.core.model.study.entity;

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
import javax.persistence.UniqueConstraint;

import au.org.theark.core.Constants;

/**
 * SubjectCustmFld entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "SUBJECT_CUSTM_FLD", schema = Constants.STUDY_SCHEMA, uniqueConstraints = @UniqueConstraint(columnNames = "NAME"))
public class SubjectCustmFld implements java.io.Serializable {

	// Fields

	private Long id;
	private Study study;
	private DataType dataType;
	private String name;
	private String description;
	private String minValue;
	private String maxValue;
	private String discreteValues;
	private Set<SubjectCustFldDat> subjectCustFldDats = new HashSet<SubjectCustFldDat>(
			0);

	// Constructors

	/** default constructor */
	public SubjectCustmFld() {
	}

	/** minimal constructor */
	public SubjectCustmFld(Long id, Study study,
			DataType dataType) {
		this.id = id;
		this.study = study;
		this.dataType = dataType;
	}

	/** full constructor 
	 * @param minValue 
	 * @param maxValue 
	 * @param discreteValues */
	public SubjectCustmFld(Long id, Study study,
			DataType dataType, String name, String description, 
			String minValue, String maxValue, String discreteValues,
			Set<SubjectCustFldDat> subjectCustFldDats) {
		this.id = id;
		this.study = study;
		this.dataType = dataType;
		this.name = name;
		this.description = description;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.discreteValues = discreteValues;
		this.subjectCustFldDats = subjectCustFldDats;
	}

	// Property accessors
	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STUDY_KEY")
	public Study getStudy() {
		return this.study;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DATA_TYPE_KEY")
	public DataType getDataType() {
		return this.dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	@Column(name = "NAME", unique = true, length = 200)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "DESCRIPTION")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "MIN_VALUE")
	public String getMinValue() {
		return minValue;
	}

	public void setMinValue(String minValue) {
		this.minValue = minValue;
	}

	@Column(name = "MAX_VALUE")
	public String getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(String maxValue) {
		this.maxValue = maxValue;
	}

	@Column(name = "DISCRETE_VALUES")
	public String getDiscreteValues() {
		return discreteValues;
	}

	public void setDiscreteValues(String discreteValues) {
		this.discreteValues = discreteValues;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "subjectCustmFld")
	public Set<SubjectCustFldDat> getSubjectCustFldDats() {
		return this.subjectCustFldDats;
	}

	public void setSubjectCustFldDats(Set<SubjectCustFldDat> subjectCustFldDats) {
		this.subjectCustFldDats = subjectCustFldDats;
	}

}