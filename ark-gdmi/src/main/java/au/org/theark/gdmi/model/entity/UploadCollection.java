package au.org.theark.gdmi.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * UploadCollection entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "UPLOAD_COLLECTION", schema = "GDMI")
public class UploadCollection implements java.io.Serializable {

	// Fields

	private long id;
	private Collection collection;
	private Upload upload;
	private String userId;
	private String insertTime;
	private String updateUserId;
	private String updateTime;

	// Constructors

	/** default constructor */
	public UploadCollection() {
	}

	/** minimal constructor */
	public UploadCollection(long id, Collection collection, Upload upload) {
		this.id = id;
		this.collection = collection;
		this.upload = upload;
	}

	/** full constructor */
	public UploadCollection(long id, Collection collection, Upload upload,
			String userId, String insertTime, String updateUserId,
			String updateTime) {
		this.id = id;
		this.collection = collection;
		this.upload = upload;
		this.userId = userId;
		this.insertTime = insertTime;
		this.updateUserId = updateUserId;
		this.updateTime = updateTime;
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
	@JoinColumn(name = "COLLECTION_PK", nullable = false)
	public Collection getCollection() {
		return this.collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "UPLOAD_PK", nullable = false)
	public Upload getUpload() {
		return this.upload;
	}

	public void setUpload(Upload upload) {
		this.upload = upload;
	}

	@Column(name = "USER_ID", length = 50)
	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "INSERT_TIME")
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

}