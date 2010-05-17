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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor;

public interface IProduct extends ICoreProduct
{
	/**
	 * Creates a new Application and initializes the addins if asked.  If bInitializeAddins is false then call IProduct::InitializeAddIns
	*/
	public IApplication initialize2( boolean bInitializeAddins );

	/**
	 * Get the messenger object used for GUI dialogs
	*/
	public IMessenger getMessenger();

	/**
	 * Get/Set the uml project tree
	*/
	public IProjectTreeControl getProjectTree();

	/**
	 * Get/Set the uml project tree
	*/
	public void setProjectTree( IProjectTreeControl value );

        /**
	 * Get uml project tree Model
	*/
        public IProjectTreeModel getProjectTreeModel();
   
    
        /**
        * Set uml project tree model
        */
        public void setProjectTreeModel(IProjectTreeModel newTreeModel);
   
    
	/**
	 * Get/Set the uml project tree
	*/
	public IProjectTreeControl getDesignCenterTree();

	/**
	 * Get/Set the uml project tree
	*/
	public void setDesignCenterTree( IProjectTreeControl value );

	/**
	 * Get/Set the uml property editor
	*/
	public IPropertyEditor getPropertyEditor();

	/**
	 * Get/Set the uml property editor
	*/
	public void setPropertyEditor( IPropertyEditor value );

	/**
	 * Get/Set the uml documentation editor
	*/
	//public IDocHost getDocHost();

	/**
	 * Get/Set the uml documentation editor
	*/
	//public void setDocHost( IDocHost value );

	/**
	 * Get/Set the diagram manager
	*/
	public IProductDiagramManager getDiagramManager();

	/**
	 * Get/Set the diagram manager
	*/
	public void setDiagramManager( IProductDiagramManager value );

	/**
	 * Get/Set the project manager
	*/
	public IProductProjectManager getProjectManager();

	/**
	 * Get/Set the project manager
	*/
	public void setProjectManager( IProductProjectManager value );

	/**
	 * Get/Set the user interface proxy
	*/
	public IProxyUserInterface getProxyUserInterface();

	/**
	 * Get/Set the user interface proxy
	*/
	public void setProxyUserInterface( IProxyUserInterface value );

	/**
	 * Get/Set the addin manager
	*/
//	public IAddInManager getAddInManager();

	/**
	 * Get/Set the addin manager
	*/
//	public void setAddInManager( IAddInManager value );

	/**
	 * Adds a diagram to the product
	*/
	public void addDiagram( IDiagram pDiagram );

	/**
	 * Removes a diagram from the product
	*/
	public void removeDiagram( IDiagram pDiagram );

	/**
	 * Gets a diagram from the product
	*/
	public IDiagram getDiagram( String sFilename );

	/**
	 * Returns all the drawing areas
	*/
	public ETList<IDiagram> getAllDrawingAreas();

	/**
	 * Displays the AddIn Manager dialog over the passed in window.
	*/
	public void displayAddInDialog( int parentHwnd );

	/**
	 * Gets an addin based on its progID.
	*/
//	public IAddIn getAddIn( String progID );

	/**
	 * Unloads and deinitializes all the addins.
	*/
//	public void deInitAddIns();

	/**
	 * Initializes all the addins.
	*/
	public void initializeAddIns();

	/**
	 * Get the ISCMIntegrator interface.
	*/
	public ISCMIntegrator getSCMIntegrator();

   /**
    * Allows the users to display progress to the users through a ProgressCtrl
   */
   public IProgressCtrl getProgressCtrl();

	/**
	 * Allows the users to display progress to the users through a ProgressCtrl
	*/
	public void setProgressCtrl( IProgressCtrl value );

   /**
    * Allows the users to display progress to the users through a Progress Dialog
   */
   public IProgressDialog getProgressDialog();

	/**
	 * Access the Accelerator Manager.
	*/
	public IAcceleratorManager getAcceleratorManager();

	/**
	 * Access the Accelerator Manager.
	*/
	public void setAcceleratorManager( IAcceleratorManager value );

	/**
	 * This is the diagram that's currently being written or read.
	*/
	public IDiagram getSerializingDiagram();

	/**
	 * This is the diagram that's currently being written or read.
	*/
	public void setSerializingDiagram( IDiagram value );

	/**
	 * Adds a COM interface to for later retrieval.
	*/
	public void addControl( int nID, Object pControl );

	/**
	 * Gets a COM interface that was previously saved.
	*/
	public Object getControl( int nID );

	/**
	 * Remove a COM interface that was previously saved.
	*/
	public void removeControl( int nID );

	/**
	 * This is the clipboard used between diagrams.
	*/
	public String getCrossDiagramClipboard();

	/**
	 * This is the clipboard used between diagrams.
	*/
	public void setCrossDiagramClipboard( String value );

	/**
	 * Get/Set the vba integrator
	*/
	public Object getVBAIntegrator();

	/**
	 * Get/Set the vba integrator
	*/
	public void setVBAIntegrator( Object value );
	
        // TODO: meteora
//	public IPresentationTypesMgr getPresentationTypesMgr();
//	
//	public IPresentationResourceMgr getPresentationResourceMgr();

}
