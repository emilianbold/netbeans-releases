/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.modules.uml.core.coreapplication;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
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
