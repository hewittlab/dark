package au.org.theark.study.web.menu;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.theark.core.Constants;
import au.org.theark.core.model.study.entity.ArkFunction;
import au.org.theark.core.model.study.entity.ArkModule;
import au.org.theark.core.service.IArkCommonService;
import au.org.theark.core.vo.ArkUserVO;
import au.org.theark.core.web.component.menu.AbstractArkTabPanel;
import au.org.theark.core.web.component.tabbedPanel.ArkAjaxTabbedPanel;
import au.org.theark.study.web.component.managestudy.StudyContainer;
import au.org.theark.study.web.component.manageuser.UserContainerPanel;
import au.org.theark.study.web.component.mydetails.MyDetailsContainer;
import au.org.theark.study.web.component.studycomponent.StudyComponentContainerPanel;

/**
 * <p>
 * The <code>StudySubMenuTab</code> class that extends the {@link au.org.theark.core.web.component.menu.AbstractArkTabPanel AbstractArkTabPanel}
 * class. It provides the implementation of the Study tab panel (sub-menu).
 * </p>
 * 
 * @author nivedann
 * @author cellis
 */
public class StudySubMenuTab extends AbstractArkTabPanel {
	/**
	 * 
	 */
	private static final long			serialVersionUID	= -2725142200726870636L;
	private transient static Logger	log					= LoggerFactory.getLogger(StudySubMenuTab.class);
	@SpringBean(name = au.org.theark.core.Constants.ARK_COMMON_SERVICE)
	private IArkCommonService<Void>	iArkCommonService;

	private WebMarkupContainer			studyNameMarkup;
	private WebMarkupContainer			studyLogoMarkup;
	private WebMarkupContainer			arkContextMarkup;
	private MainTabProviderImpl		mainTabProvider;
	private List<ITab>					moduleSubTabsList	= new ArrayList<ITab>();

	/**
	 * StudySubMenuTab Constructor
	 * 
	 * @param id
	 *           this component identifier
	 * @param studyNameMarkup
	 *           the WebMarkupContainer that references the study name in context
	 * @param studyLogoMarkup
	 *           the WebMarkupContainer that references the study logo in context
	 * @param arkContextMarkup
	 *           the WebMarkupContainer that references the context items
	 */
	public StudySubMenuTab(String id, WebMarkupContainer studyNameMarkup, WebMarkupContainer studyLogoMarkup, WebMarkupContainer arkContextMarkup) {
		super(id);
		this.studyNameMarkup = studyNameMarkup;
		this.studyLogoMarkup = studyLogoMarkup;
		this.arkContextMarkup = arkContextMarkup;
		buildTabs();
	}

	/**
	 * StudySubMenuTab Constructor
	 * 
	 * @param id
	 *           this component identifier
	 * @param studyNameMarkup
	 *           the WebMarkupContainer that references the study name in context
	 * @param studyLogoMarkup
	 *           the WebMarkupContainer that references the study logo in context
	 * @param arkContextMarkup
	 *           the WebMarkupContainer that references the context items
	 * @param mainTabProvider
	 *           the reference to the main tabs (to allow repaint on Study selection)
	 */
	public StudySubMenuTab(String id, WebMarkupContainer studyNameMarkup, WebMarkupContainer studyLogoMarkup, WebMarkupContainer arkContextMarkup, MainTabProviderImpl mainTabProvider) {
		super(id);
		this.studyNameMarkup = studyNameMarkup;
		this.studyLogoMarkup = studyLogoMarkup;
		this.arkContextMarkup = arkContextMarkup;
		this.mainTabProvider = mainTabProvider;
		buildTabs();
	}

	/**
	 * Build the list of tabs that represent the sub-menus
	 */
	public void buildTabs() {
		ArkModule arkModule = iArkCommonService.getArkModuleByName(Constants.ARK_MODULE_STUDY);
		List<ArkFunction> arkFunctionList = iArkCommonService.getModuleFunction(arkModule);// Gets a list of ArkFunctions for the given Module

		/*
		 * Iterate each ArkFunction render the Tabs.When something is clicked it uses the arkFunction and calls processAuthorizationCache to clear
		 * principals of the user and loads the new set of principals.(permissions)
		 */
		for (final ArkFunction menuArkFunction : arkFunctionList) {
			moduleSubTabsList.add(new AbstractTab(new StringResourceModel(menuArkFunction.getResourceKey(), this, null)) {
				/**
				 * 
				 */
				private static final long	serialVersionUID	= -8421399480756599074L;

				@Override
				public Panel getPanel(String panelId) {
					Panel panelToReturn = null;// Set up a common tab that will be accessible for all users

					// Clear authorisation cache
					processAuthorizationCache(au.org.theark.core.Constants.ARK_MODULE_STUDY, menuArkFunction);
					if (menuArkFunction.getName().equalsIgnoreCase(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_USER)) {
						panelToReturn = new UserContainerPanel(panelId);
					}
					else if (menuArkFunction.getName().equalsIgnoreCase(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_STUDY)) {
						panelToReturn = new StudyContainer(panelId, studyNameMarkup, studyLogoMarkup, arkContextMarkup, mainTabProvider.getModuleTabbedPanel());
					}
					else if (menuArkFunction.getName().equalsIgnoreCase(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_STUDY_COMPONENT)) {
						panelToReturn = new StudyComponentContainerPanel(panelId);
					}
					else if (menuArkFunction.getName().equalsIgnoreCase(au.org.theark.core.Constants.FUNCTION_KEY_VALUE_MY_DETAIL)) {
						Subject currentUser = SecurityUtils.getSubject();
						panelToReturn = new MyDetailsContainer(panelId, new ArkUserVO(), currentUser);
					}
					return panelToReturn;
				}
			});
		}

		ArkAjaxTabbedPanel moduleTabbedPanel = new ArkAjaxTabbedPanel(Constants.MENU_STUDY_SUBMENU, moduleSubTabsList);
		add(moduleTabbedPanel);
	}

	/**
	 * @param log
	 *           the log to set
	 */
	public static void setLog(Logger log) {
		StudySubMenuTab.log = log;
	}

	/**
	 * @return the log
	 */
	public static Logger getLog() {
		return log;
	}
}