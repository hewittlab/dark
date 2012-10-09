package au.org.theark.core.model.study.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import au.org.theark.core.Constants;

/**
 * @author nivedann
 *
 */
@Entity
@Table(name = "ARK_MODULE_FUNCTION", schema = Constants.STUDY_SCHEMA)
public class ArkModuleFunction implements Serializable{
	
	private Long id;
	private ArkModule arkModule;
	private ArkFunction arkFunction;
	private Long functionSequence;
	
	/**
	 * Constructor
	 */
	public ArkModuleFunction(){
		arkModule = new ArkModule();
		arkFunction = new ArkFunction();
	}
	
	@Id
	@SequenceGenerator(name="ark_module_function_generator", sequenceName="ARK_MODULE_FUNCTION_SEQUENCE")
	@GeneratedValue(strategy=GenerationType.AUTO, generator = "ark_module_function_generator")
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ARK_MODULE_ID")
	public ArkModule getArkModule() {
		return arkModule;
	}

	public void setArkModule(ArkModule arkModule) {
		this.arkModule = arkModule;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ARK_FUNCTION_ID")
	public ArkFunction getArkFunction() {
		return arkFunction;
	}

	public void setArkFunction(ArkFunction arkFunction) {
		this.arkFunction = arkFunction;
	}

	@Column(name ="FUNCTION_SEQUENCE", precision = 22, scale = 0)
	public Long getFunctionSequence() {
		return functionSequence;
	}

	public void setFunctionSequence(Long functionSequence) {
		this.functionSequence = functionSequence;
	}
	
	

}