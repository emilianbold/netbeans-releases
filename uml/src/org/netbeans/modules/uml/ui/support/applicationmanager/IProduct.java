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


package org.netbeans.modules.uml.ui.support.applicationmanager;

import org.netbeans.modules.uml.core.IApplication;
//import org.netbeans.modules.uml.core.addinframework.IAddIn;
//import org.netbeans.modules.uml.core.addinframework.IAddInManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
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
	
	public IPresentationTypesMgr getPresentationTypesMgr();
	
	public IPresentationResourceMgr getPresentationResourceMgr();

}
