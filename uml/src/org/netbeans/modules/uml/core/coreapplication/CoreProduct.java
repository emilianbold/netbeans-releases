/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.Application;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.UMLCreationFactory;
//import org.netbeans.modules.uml.core.addinframework.AddInEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.EventDispatchController;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.generativeframework.TemplateManager;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.ActivityEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
//import com.embarcadero.describe.foundation.IConfigManager;
//import com.embarcadero.describe.foundation.ConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidatorEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.dynamics.DynamicsEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ClassifierEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.structure.StructureEventDispatcher;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManager;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceManagerEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.FacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.LanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.roundtripframework.RTMode;
import org.netbeans.modules.uml.core.roundtripframework.RoundTripController;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageService;
import org.netbeans.modules.uml.core.support.umlmessagingcore.MessageService;
import org.netbeans.modules.uml.core.support.umlmessagingcore.UMLMessagingEventDispatcher;
import org.netbeans.modules.uml.core.support.umlsupport.PreventReEntrance;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceManager;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManager;

/**
 * @author sumitabhk
 *
 */
public class CoreProduct implements ICoreProduct
{

	/// The IApplication object.  This wraps an actual IApplication object.
	protected IApplication m_Application = null;

	/// This guy is the factory used to create our objects
	private ICreationFactory m_Factory = null;

	/// Here's the messenger service that is used to send messages.  The gui listens and places it in the output pane.
	private IMessageService m_MessageService = null;

	/// The current workspace
	private IWorkspace m_CurrentWorkspace = null;

	/// The workspace manager
	private IWorkspaceManager m_WSManager = null;

	/// Controls the collection of EventDispatcher objects for this Product
	protected IEventDispatchController m_DispatchController = null;

	/// The one and only RoundTripController
	private IRoundTripController m_RTController = null;

	/// The one and only FacilityManager.
	private IFacilityManager m_FacilityManager = null;

	/// The one and only LangaugeManager.
	private ILanguageManager m_LanguageManager = null;

	/// The one and only PreferenceManager.
	private IPreferenceManager2 m_PreferenceManager = null;

	/// The one and only ConfigManager
	private IConfigManager m_ConfigManager = null;

	/// The one and only NavigatorFactory.
	/// Used for creating Navigator objects that know how to navigate to Element objects.
	/// One example of a Navigator is the SourceNavigator that knows how to navigate to
	/// an Element's (e.g. operation, attribute, classifier) source code.
	private INavigatorFactory m_pNavigatorFactory = null;

	/// The TemplateManager is the entry point into the Generative Framework
	private ITemplateManager m_TemplateManager = null;

	/// The data formatter keeps his language factories cached up, so this guy manages that cache
	private IDataFormatter m_DataFormatter = null;

	 // the one and only design center manager
	private IDesignCenterManager m_DesignCenterManager = null;

	/// Actual COM interface this impl represents;
	private ICoreProduct m_COMInterface = null;

	private static int entered = 0;
	/**
	 * 
	 */
	public CoreProduct() 
	{
		super();
		m_WSManager = new WorkspaceManager();
		m_DispatchController = new EventDispatchController();
		m_Factory = new UMLCreationFactory();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.coreapplication.ICoreProduct#getCoreMessenger()
	 */
	public ICoreMessenger getCoreMessenger() 
	{
		return null;
	}

	/**
	 * CoreProductImpl Get rid of resources.
	 */
	public void preDestroy() 
	{
		quit();
	}

	/**
	 * Returns the IApplication that this product wraps.
	 *
	 * @param pVal The returned IApplication.
	 */
	public IApplication getApplication() 
	{
		return m_Application;
	}

	/**
	 * Sets the IApplication that this product wraps.
	 *
	 * @param newVal The new IADApplication to wrap.
	 */
	public void setApplication(IApplication value) 
	{
		m_Application = value;
	}

	/**
	 * Returns a shared IMessageService object for the product.  Using a shared messenger allows
	 * connection points to notify those that are interested in knowing that new messages have been added.
	 *
	 * @param pVal The returned messenger.
	 */
	public IMessageService getMessageService() 
	{
		if (m_MessageService == null)
		{
			m_MessageService = new MessageService();
		}
		return m_MessageService;
	}

	/**
	 * Gets the current workspace.  There's only one workspace open.
	 *
	 * @param pVal The current workspace.
	 */
	public IWorkspace getCurrentWorkspace() 
	{
		return m_CurrentWorkspace;
	}

	/**
	 * Sets the current workspace.  There's only one workspace open.  
	 * 
	 * @param newVal The workspace set.
	 */
	public void setCurrentWorkspace(IWorkspace value) 
	{
		m_CurrentWorkspace = value;
	}

	/**
	 *
	 * Opens a workspace file, returning the Workspace object that represents
	 * the data in that file. The Workspace returned becomes the currently active
	 * workspace on this Product.
	 *
	 * @param location[in] The absolute path to the Workspace file.
	 * @param space[out] The Workspace object.
	 *
	 * @return HRESULT
	 * 
	 */
	public IWorkspace openWorkspace(String location) 
		throws InvalidArguments, WorkspaceManagementException 
	{
		IWorkspace space = null;
		if (m_WSManager != null)
		{
			space = m_WSManager.openWorkspace(location);
		}		
		if (space != null)
		{
			m_CurrentWorkspace = space;
		}
		return space;
	}

	/**
	 *
	 * Closes the passed-in Workspace. All open WorkspaceProjects will also be closed.
	 *
	 * @param space[in] The Workspace to close.
	 * @param fileName[in] The absolute location to save the Workspace if the save flag
	 *                     is true.
	 * @param save[in] 
	 *                - true to save the contents of the Workspace, else
	 *                - false to discard changes since the last close and save
	 *                  of the workspace.
	 *
	 * @return HREUSLT
	 */
	public void closeWorkspace(IWorkspace space, String fileName, boolean save) 
		throws InvalidArguments, WorkspaceManagementException 
	{
		m_WSManager.closeWorkspace(space, fileName, save);		
	}

	/**
	 *
	 * Retrieves the WorkspaceManager on this product.
	 *
	 * @param pVal[out] The WorkspaceManager.
	 *
	 * @return HRESULT
	 * 
	 */
	public IWorkspaceManager getWorkspaceManager() 
	{
		return m_WSManager;
	}

	/**
	 *
	 * Sets the WorkspaceManager on this product.
	 *
	 * @param newVal[in] The new manager.
	 *
	 * @return HRESULT
	 * 
	 */
	public void setWorkspaceManager(IWorkspaceManager value) 
	{
		m_WSManager = value;
	}

	/**
	 *
	 * Creates a new Workspace file at the location specified.
	 *
	 * @param location[in] The absolute path to the resultant workspace file.
	 * @param name[in] The name of the new Workspace.
	 * @param space[out] The new Workspace.
	 *
	 * @return HRESULT
	 * 
	 */
	public IWorkspace createWorkspace(String location, String name) 
		throws InvalidArguments, WorkspaceManagementException 
	{
		IWorkspace space = null;
		if (m_WSManager != null)
		{
			space = m_WSManager.createWorkspace(location, name);
		}
		if (space != null)
		{
			m_CurrentWorkspace = space;
		}
		return space;
	}

	/**
	 *
	 * Creates the Application specific to this Product.
	 *
	 * @param app[out] The new Application.
	 *
	 * @return HRESULT
	 *
	 */
	public IApplication initialize() 
	{
		IApplication app = null;
		initializeCore();
		boolean proceed = firePreInit();
		if (proceed)
		{
			establishFacilityManager();
			establishLanguageManager();
			establishPreferenceManager();
			establishDefaultNavigatorFactory();
			establishTemplateManager();
			
			app = createProductApplication();
			if (app != null)
			{
				setApplication(app);
				if (m_Application != null && m_Factory != null)
				{
					// Set the ConfigManager on the CreationFactory. The CreationFactory
					// does NOT up the refcount.
					m_Factory.setConfigManager(m_ConfigManager);
				}
				// This is here only because roundtrip needs to get ProjectCreated events
				// after application, because only the application knows how to get an
				// IProject from an IWSProject. In the future, if we create a CoreApplication
				// event dispatcher, we will want to create a ProjectAddedToApp event, then
				// roundtrip need only listen to that.
				establishRoundTrip();
				fireInited();
			}
		}
		return app;
	}

	/**
	 *
	 * Fires the initialized event for the core product.
	 *
	 * @return HRESULT
	 *
	 */
	private void fireInited() 
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		ICoreProductEventDispatcher disp = (ICoreProductEventDispatcher) 
								ret.getDispatcher(EventDispatchNameKeeper.coreProduct());		
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("CoreProductInitialized");
			disp.fireCoreProductInitialized(this, payload);
		}
	}

	/**
	 *
	 * Create the RoundTripController and places it on the Product.
	 *
	 * @return HRESULT
	 *
	 */
	private void establishRoundTrip() 
	{
		if (m_RTController == null)
		{
			m_RTController = new RoundTripController();
                        
                        // conover - disable Round Trip by default
			// m_RTController.initialize(this, RTMode.RTM_LIVE);
			m_RTController.initialize(this, RTMode.RTM_OFF);
		}
	}

	/**
	 *
	 * Creates the application specific to the domain. This is generally
	 * overridden.
	 *
	 * @param app[out] The application.
	 *
	 * @return HRESULT
	 *
	 */
	protected IApplication createProductApplication() 
	{
		IApplication coreApp = new Application();
		return coreApp;
	}

	/**
	 *
	 * Establishes the one and only TemplateManager on the product
	 *
	 * @return HRESULT
	 *
	 */
	private void establishTemplateManager() 
	{
		if (m_TemplateManager == null)
		{
			m_TemplateManager = new TemplateManager();
		}
	}

	/** 
	 * Creates the default NavigatorFactory and sets the CoreProduct's NavigatorFactory property
	 * to the newly created NavigatorFactory.
	 *
	 * @return HRESULT
	 */
	private void establishDefaultNavigatorFactory() 
	{
		m_pNavigatorFactory = new NavigatorFactory();
	}

	/**
	 *
	 * Builds all the EventDispatcher objects specialized for this Product.
	 *
	 * @return HRESULT
	 */
	protected void establishDispatchers() 
	{
		IEventDispatcher disp = new WorkspaceEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.workspaceName(), disp);
		
		disp = new ElementChangeEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.modifiedName(), disp);
		
//		disp = new AddInEventDispatcher();
//		m_DispatchController.addDispatcher(EventDispatchNameKeeper.addInName(), disp);
		
		disp = new UMLMessagingEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.messagingName(), disp);
		
		disp = new RelationValidatorEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.relation(), disp);
		
		disp = new ElementLifeTimeEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.lifeTime(), disp);
		
		disp = new ClassifierEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.classifier(), disp);
		
		disp = new CoreProductEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.coreProduct(), disp);
		
		disp = new PreferenceManagerEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.preferenceManager(), disp);
		
		disp = new DynamicsEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.dynamics(), disp);
		
		disp = new ActivityEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.activities(), disp);
		
		disp = new StructureEventDispatcher();
		m_DispatchController.addDispatcher(EventDispatchNameKeeper.structure(), disp);
		
	}

	/**
	 *
	 * Creates the Preference manager and places it on the Product.
	 *
	 * @return HRESULT
	 *
	 */
	private void establishPreferenceManager() 
	{
		if (m_PreferenceManager == null)
		{
			m_PreferenceManager = new PreferenceManager();
			if (m_ConfigManager != null)
			{
				String configLoc = m_ConfigManager.getDefaultConfigLocation();
				configLoc += "PreferenceProperties.etc";
				m_PreferenceManager.registerFile(configLoc);
			}
		}
	}

	/**
	 *
	 * Creates the Language manager and places it on the Product.
	 *
	 * @return HRESULT
	 *
	 */
	private void establishLanguageManager() 
	{
		if (m_LanguageManager == null)
		{
			m_LanguageManager = new LanguageManager();
		}
	}

	/**
	 *
	 * Creates the Facilitiy Manager and places it on the Product.
	 *
	 * @return HRESULT
	 *
	 */
	private void establishFacilityManager()
	{
		if (m_FacilityManager == null)
		{
			m_FacilityManager = new FacilityManager();

			// I will initialize the facility manager by specifing the 
			// configuration file that will define all the facilities for the application.
			m_FacilityManager.setConfigurationFile("Facilities.etc");
		}
	}

	/**
	 *
	 * Called to alert listeners that this product is about to be initialized.
	 *
	 * @param proceed[out] The proceed flag. If false, application initialization
	 *                     will be completely halted.
	 *
	 * @return HRESULT
	 *
	 */
	private boolean firePreInit() 
	{
		boolean proceed = true;
        
		establishDispatchers();
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		ICoreProductEventDispatcher disp = (ICoreProductEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.coreProduct());
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("CoreProductPreInit");
			proceed = disp.fireCoreProductPreInit(this, payload);
		}
		return proceed;
	}

	/**
	 *
	 * Establishes all the dispatchers resident in Core, as well as the
	 * roundtrip controller.
	 *
	 */
	private void initializeCore() 
	{
		m_ConfigManager = new ConfigManager();
	}

	public ICreationFactory getCreationFactory()
	{
		return m_Factory;
	}

	/**
	 *
	 * Retrieves the EventDispatchController off this Product.
	 *
	 * @param pVal[out] The controller.
	 *
	 * @return HRESULT
	 */
	public IEventDispatchController getEventDispatchController() 
	{
		return m_DispatchController;
	}

	/**
	 *
	 * Sets the EventDispatchController on this product.
	 *
	 * @param newVal[in] The new controller.
	 *
	 * @return HRESULT
	 */
	public void setEventDispatchController(IEventDispatchController value) 
	{
		m_DispatchController = value;
	}

	/**
	 *
	 * Retrieves the EventDispatcher off the internal controller by matching the ID
	 * passed in.
	 *
	 * @param id[in] The name of the EventDispatcher to retrieve.
	 * @param pVal[out] The found Dispatcher.
	 *
	 * @return HRESULT
	 */
	public IEventDispatcher getEventDispatcher(String id) 
	{
		IEventDispatcher disp = null;
		if (m_DispatchController != null)
		{
			disp = m_DispatchController.retrieveDispatcher(id);
		}
		return disp;
	}

	/**
	 *
	 * Sets the RoundTripController on this product.
	 *
	 * @param newVal[in] The controller.
	 *
	 * @return HRESULT
	 *
	 */
	public void setRoundTripController(IRoundTripController value) 
	{
		m_RTController = value;
	}

	public IRoundTripController getRoundTripController() 
	{
		return m_RTController;
	}

	/**
	 *
	 * Sets the Facility Manager for this entire Product.
	 *
	 * @param newVal[in] The manager.
	 *
	 * @return HRESULT
	 *
	 */
	public void setFacilityManager(IFacilityManager value) 
	{
		m_FacilityManager = value;
	}

	/**
	 *
	 * Gets the Facility Manager for this entire Product.
	 *
	 * @param pVal[out] The current manager.
	 *
	 * @return HRESULT
	 *
	 */
	public IFacilityManager getFacilityManager() 
	{
		return m_FacilityManager;
	}

	/**
	 *
	 * Sets the Language Manager for this entire Product.
	 *
	 * @param newVal[in] The manager.
	 *
	 * @return HRESULT
	 *
	 */
	public void setLanguageManager(ILanguageManager value) 
	{
		m_LanguageManager = value;
	}

	/**
	 *
	 * Gets the Language Manager for this entire Product.
	 *
	 * @param pVal[out] The current manager.
	 *
	 * @return HRESULT
	 *
	 */
	public ILanguageManager getLanguageManager() 
	{
		return m_LanguageManager;
	}

	/**
	 *
	 * Sets the Preference Manager for this entire Product.
	 *
	 * @param newVal[in] The manager.
	 *
	 * @return HRESULT
	 *
	 */
	public void setPreferenceManager(IPreferenceManager2 value) 
	{
		m_PreferenceManager = value;
	}

	/**
	 *
	 * Gets the Preference Manager for this entire Product.
	 *
	 * @param pVal[out] The current manager.
	 *
	 * @return HRESULT
	 *
	 */
	public IPreferenceManager2 getPreferenceManager() 
	{
		return m_PreferenceManager;
	}

	/**
	 *
	 * Retrieves the ConfigManager for this product.
	 *
	 * @param pVal[out] The current config manager.
	 *
	 * @return HRESULT
	 *
	 */
	public IConfigManager getConfigManager() 
	{
	    if (m_ConfigManager == null) 
	    {
		initialize();
	    }
	    return m_ConfigManager;
	}

	/**
	 *
	 * Sets the config manager for this product.
	 *
	 * @param newVal[in] The new manager.
	 *
	 * @return HRESULT
	 *
	 */
	public void setConfigManager(IConfigManager value) 
	{
		m_ConfigManager = value;
	}

	/**
	 *
	 * Releases all resources allocated by the CoreProduct.
	 *
	 *
	 * @return OK, else error codes.
	 *
	 */
	public void quit() 
	{
		// The call to the put_CoreProduct later in this method
		// will cause this method to be called again. We need
		// to prevent this
		//PreventReEntrance reEnt = new PreventReEntrance(entered);
      //entered++;
      
      PreventReEntrance reEnt = new PreventReEntrance();
      entered = reEnt.startBlocking(entered);		
		
		try	
		{
			if (!reEnt.isBlocking())
			{
				if (m_Application != null)
				{
					m_Application.closeAllProjects(false);
				}
				
            // Fire the pre quit event
            firePreQuit();
         
				if (m_Application != null)
				{
					m_Application.destroy();
				}
			
				revokeRoundTrip();
				revokeDispatchers();
				revokeFacilityManager();
				revokeLanguageManager();
				revokePreferenceManager();
			
				if (m_Factory != null)
				{
					FactoryRetriever fact = FactoryRetriever.instance();
					fact = null;
					m_Factory.cleanUp();
				}
			
				m_Factory = null;
				// The call to put_CoreProduct clears this CoreProduct from teh
				// CoreProductManager's list of products. If we don't do this
				// here, the CoreProductManager holds on to our pointer until
				// the manager exits. Since we do this here, the user should
				// see the CoreProduct release as soon as they release their
				// pointer to the CoreProduct.
			
				ICoreProductManager prodMan = CoreProductManager.instance();
				prodMan.setCoreProduct(null);
			
				m_Application = null;
				m_TemplateManager = null;
				m_DataFormatter = null;
			}
		}
		finally
		{
			entered = reEnt.releaseBlock();
			//entered--;
		}
	}

	/**
	 *
	 * Detaches the current Preference Manager from this product.
	 *
	 * @return HRESULT
	 *
	 */
	private void revokePreferenceManager() 
	{
		if( m_PreferenceManager != null)
		{
		   // Currently there is nothing to do to deinitalize the Preference manager.
		}
	}

	/**
	 *
	 * Detaches the current Facility Manager from this product.
	 *
	 * @return HRESULT
	 *
	 */
	private void revokeLanguageManager() 
	{
		if( m_LanguageManager != null)
		{
		   // Currently there is nothing to do to deinitalize the language manager.
		}
	}

	/**
	 *
	 * Detaches the current Facility Manager from this product.
	 *
	 * @return HRESULT
	 *
	 */
	private void revokeFacilityManager() 
	{
		if( m_FacilityManager != null )
		{
		   // Currently there is nothing to do to deinitalize the facility manager.
		}
	}

	/**
	 *
	 * Revokes all the EventDispatchers installed by this product.
	 *
	 * @return 
	 */
	private void revokeDispatchers() 
	{
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.workspaceName());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.modifiedName());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.addInName());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.messagingName());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.relation());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.lifeTime());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.classifier());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.coreProduct());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.preferenceManager());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.dynamics());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.structure());
		m_DispatchController.removeDispatcher(EventDispatchNameKeeper.activities());
	}

	/**
	 *
	 * Detaches the current RoundTripController from this product.
	 *
	 * @return HRESULT
	 *
	 */
	private void revokeRoundTrip() 
	{
		if (m_RTController != null)
		{
			m_RTController.deInitialize();
			m_RTController = null;
		}
	}

	private void firePostQuit() 
	{
	}

	private void firePreQuit() 
	{
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		ICoreProductEventDispatcher disp = 
							(ICoreProductEventDispatcher) ret.getDispatcher
							(EventDispatchNameKeeper.coreProduct());
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("CoreProductPreQuit");
			disp.fireCoreProductPreQuit(this, payload);
		}
	}

	/**
	 *
	 * Determines whether or not this product contains GUI components.
	 * The CoreProduct by itself will always return false. Sub class
	 * products may not however.
	 *
	 * @param pVal[out] true if this product is part of a larger
	 *                  GUI product, else 
	 *                  false if this is simply a server based product
	 *                  with no shell.
	 *
	 * @return HRESULT
	 *
	 */
	public boolean isGUIProduct() 
	{
		return false;
	}

	/** 
	 * Gets the CoreProduct's navigator factory.
	 * 
	 * @param pVal[out] the CoreProduct's navigator factory.
	 * 
	 * @return HRESULT
	 */
	public INavigatorFactory getNavigatorFactory() 
	{
		return m_pNavigatorFactory;
	}

	/** 
	 * Sets the CoreProduct's navigator factory.
	 * 
	 * @param newVal[in] the CoreProduct's new navigator factory.
	 * 
	 * @return HRESULT
	 */
	public void setNavigatorFactory(INavigatorFactory value) 
	{
		m_pNavigatorFactory = value;
	}

	/** 
	 * Gets the CoreProduct's DiagramCleanupManager.
	 * 
	 * @param pVal[out] the CoreProduct's DiagramCleanupManager.
	 * 
	 * @return HRESULT
	 */
	public IDiagramCleanupManager getDiagramCleanupManager() 
	{
		// The core product doesn't provide one of these
		return null;
	}

	/**
	 * The TemplateManager, used for Generative Templates
	 */
	public ITemplateManager getTemplateManager() 
	{
		return m_TemplateManager;
	}

	public void setTemplateManager(ITemplateManager value) 
	{
		m_TemplateManager = value;
	}

	/**
	 * Get/Set the data formatter
	 */
	public IDataFormatter getDataFormatter() 
	{
		if (m_DataFormatter == null)
		{
			m_DataFormatter = new DataFormatter();
		}
		return m_DataFormatter;
	}

	public void setDataFormatter(IDataFormatter value) 
	{
		m_DataFormatter = value;
	}

	/**
	 * Get/Set the DesignCenterManager
	 */
	public IDesignCenterManager getDesignCenterManager() 
	{
		return m_DesignCenterManager;
	}

	/**
	 * Get/Set the data formatter
	 */
	public void setDesignCenterManager(IDesignCenterManager value) 
	{
		m_DesignCenterManager = value;
	}
	
	/**
	 * Saves all modified data associated with this product.
	 *
	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element.
	 */
	public void save() throws WorkspaceManagementException
	{
		boolean proceed = true;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		ICoreProductEventDispatcher disp = (ICoreProductEventDispatcher) ret.getDispatcher(EventDispatchNameKeeper.coreProduct());
		if (disp != null)
		{
			IEventPayload payload = disp.createPayload("CoreProductPreSaved");
			proceed = disp.fireCoreProductPreSaved(this, payload);
		}
		if (proceed)
		{
			// Currently, the only thing the product knows about directly
			// is the current workspace
			if (m_CurrentWorkspace != null)
			{
				m_CurrentWorkspace.save(null);
			}
			if (disp != null)
			{
				IEventPayload payload = disp.createPayload("CoreProductSaved");
				disp.fireCoreProductSaved(this, payload);
			}
		}
	}
}


