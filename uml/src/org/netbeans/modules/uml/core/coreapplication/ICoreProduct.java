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

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
//import com.embarcadero.describe.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.roundtripframework.IRoundTripController;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageService;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceManager;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;

public interface ICoreProduct
{
	/**
	 * Get the messenger object used for GUI dialogs
	 */
	public ICoreMessenger getCoreMessenger();

	/**
	 * Tells the product to release all its resources - it is about to get deleted
	 */
	public void preDestroy();

	/**
	 * Get/Set the application this object points to
	 */
	public IApplication getApplication();

	/**
	 * Get/Set the application this object points to
	 */
	public void setApplication( IApplication value );

	/**
	 * Get the messenger service for communicating within an application
	 */
	public IMessageService getMessageService();

	/**
	 * Get/Set the current workspace
	 */
	public IWorkspace getCurrentWorkspace();

	/**
	 * Get/Set the current workspace
	 */
	public void setCurrentWorkspace( IWorkspace value );

	/**
	 * Opens the Workspace at the given location.
	 */
	public IWorkspace openWorkspace( String location )
		throws InvalidArguments, WorkspaceManagementException ;

	/**
	 * Closes the passed in Workspace.
	 */
	public void closeWorkspace( IWorkspace space, String fileName, boolean save )
		throws InvalidArguments, WorkspaceManagementException ;

	/**
	 * Sets / Gets the WorkspaceManager object responsible for the creation of Workspaces.
	 */
	public IWorkspaceManager getWorkspaceManager();

	/**
	 * Sets / Gets the WorkspaceManager object responsible for the creation of Workspaces.
	 */
	public void setWorkspaceManager( IWorkspaceManager value );

	/**
	 * Creates a new workspace.
	 */
	public IWorkspace createWorkspace( String location, String name )
		throws InvalidArguments, WorkspaceManagementException ;

	/**
	 * Creates a new Application.
	 */
	public IApplication initialize();

	/**
	 * property CreationFactory
	 */
	public ICreationFactory getCreationFactory();

	/**
	 * Get/Set the EventDispatchController for this entire Product.
	 */
	public IEventDispatchController getEventDispatchController();

	/**
	 * Get/Set the EventDispatchController for this entire Product.
	 */
	public void setEventDispatchController( IEventDispatchController value );

	/**
	 * Retrieves the IEventDispatcher interface that matches the passed in name.
	 */
	public IEventDispatcher getEventDispatcher( String id );

	/**
	 * Get/Set the RoundTripController for this entire Product.
	 */
	public void setRoundTripController( IRoundTripController value );

	/**
	 * Get/Set the RoundTripController for this entire Product.
	 */
	public IRoundTripController getRoundTripController();

	/**
	 * Get/Set the Facility Manager for this entire Product.
	 */
	public void setFacilityManager( IFacilityManager value );

	/**
	 * Get/Set the Facility Manager for this entire Product.
	 */
	public IFacilityManager getFacilityManager();

	/**
	 * Get/Set the Language Manager for this entire Product.
	 */
	public void setLanguageManager( ILanguageManager value );

	/**
	 * Get/Set the Language Manager for this entire Product.
	 */
	public ILanguageManager getLanguageManager();

	/**
	 * Get/Set the Preference Manager for this entire Product.
	 */
	public void setPreferenceManager( IPreferenceManager2 value );

	/**
	 * Get/Set the Preference Manager for this entire Product.
	 */
	public IPreferenceManager2 getPreferenceManager();

	/**
	 * Get/Set the ConfigManager for this entire Product.
	 */
	public IConfigManager getConfigManager();

	/**
	 * Get/Set the ConfigManager for this entire Product.
	 */
	public void setConfigManager( IConfigManager value );

	/**
	 * Tells the product to release all its resources.
	 */
	public void quit();

	/**
	 * Determines whether or not this product maintains GUI components or not.
	 */
	public boolean isGUIProduct();

	/**
	 * Get/Set the NavigatorFactory for this entire Product.
	 */
	public INavigatorFactory getNavigatorFactory();

	/**
	 * Get/Set the NavigatorFactory for this entire Product.
	 */
	public void setNavigatorFactory( INavigatorFactory value );

	/**
	 * Get/Set the DiagramCleanupManager for this entire Product.
	 */
	public IDiagramCleanupManager getDiagramCleanupManager();

	/**
	 * The TemplateManager, used for Generative Templates.
	 */
	public ITemplateManager getTemplateManager();

	/**
	 * The TemplateManager, used for Generative Templates.
	 */
	public void setTemplateManager( ITemplateManager value );

	/**
	 * Get/Set the data formatter.
	 */
	public IDataFormatter getDataFormatter();

	/**
	 * Get/Set the data formatter.
	 */
	public void setDataFormatter( IDataFormatter value );

	/**
	 * Get/Set the design center manager.
	 */
	public IDesignCenterManager getDesignCenterManager();

	/**
	 * Get/Set the design center manager.
	 */
	public void setDesignCenterManager( IDesignCenterManager value );
	
	/**
	 * Saves all modified data associated with this product.
	 *
	 * @throws WorkspaceManagementException Thrown when an error occurs
	 *         while saving the element.
	 */
	public void save()  throws WorkspaceManagementException;

}
