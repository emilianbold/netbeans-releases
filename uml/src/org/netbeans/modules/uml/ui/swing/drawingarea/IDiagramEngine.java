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


package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuSorter;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.trackbar.JTrackBar;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.export.TSEPrintSetup;
import com.tomsawyer.graph.TSGraphObject;
//import com.tomsawyer.util.TSDList;

public interface IDiagramEngine
{
	/**
	 * Tells the diagram engine that it should control the argument diagram
	*/
	public void attach( IDrawingAreaControl pParentControl );

	/**
	 * Tells the diagram engine that it should release all references and prepare to be deleted
	*/
	public void detach();

	/**
	 * This routine is called when the diagram needs to be saved to the IProductArchive
	*/
	public void writeToArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement );

	/**
	 * This routine is called when the diagram needs to be restored from the IProductArchive.
	*/
	public void readFromArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement );

	/**
	 * Returns the parent control for this diagram engine
	*/
	public IDrawingAreaControl getDrawingArea();

	/**
	 * Returns the the namespace to use when elements are created on the diagram.  Usually this is the same as the namespace of the diagram
	*/
	public INamespace getNamespaceForCreatedElements();

	/**
	 * If the user wants to change the accelerator, here the routine where that
    * should be done.
	 */
	public void setQuickKeys( TSEGraphWindow pGraphEditor );

	/**
	 * Register for the accelerators that the drawing area may be interested in.
	*/
	public void registerAccelerators();

	/**
	 * Called after a new diagram is initialized to setup our default layout settings.
	*/
	public void setupLayoutSettings( boolean bNewDiagram );

	/**
	 * Revoke the accelerators.
	*/
	public void revokeAccelerators();

	/**
	 * Handles the accelerator message coming from windows. bActive says that this drawing area is the active(or top) window, bWeHaveFocus means this drawing area has keyboard focus
	*/
	public boolean onAccelerator( int nMsg, int wParam, int lParam, boolean bActive, boolean bWeHaveFocus, int nKeyCode );

	/**
	 * This is the guy that should sort the drawing area context menus
	*/
	public IProductContextMenuSorter getContextMenuSorter();

	/**
	 * Notifies the node that a context menu is about to be displayed
	*/
	public void onContextMenu( IProductContextMenu pContextMenu, int logicalX, int logicalY );
	public void onContextMenu( IMenuManager manager );

	/**
	 * Notifies the node that a context menu has been selected
	*/
	public void onContextMenuHandleSelection( IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem );

	/**
	 * Set the menu button sensitivity and check state
	*/
	public void setSensitivityAndCheck( IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind );

	/**
	 * Returns the relationship discovery object that's appropriate for this diagram
	*/
	public ICoreRelationshipDiscovery getRelationshipDiscovery();

	/**
	 * Puts the drawing area into a specific mode based on the sButtonID.  sButtonID is a key in the PresentationTypes.etc file.
	*/
	public boolean enterModeFromButton( String sButtonID );

	/**
	 * Allows the diagram engines the ability to change the element being dropped
	*/
	public ETPairT<Boolean,IElement> processOnDropElement( IElement pElementBeingDropped );

	/**
	 * Called after a new diagram is initialized
	*/
	public void initializeNewDiagram();

	/**
	 * Called to determine if we need to initialize the trackbar
	*/
	public JTrackBar initializeTrackBar();

	/**
	 * Layout is about to happen.  Set bHandled to true to cancel normal handling
	*/
	public boolean preDoLayout( /* LayoutKind */ int nLayoutStyle );

	/**
	 * Layout has just happened
	*/
	public void postDoLayout();

	/**
	 * Copy is about to happen.  Set bHandled to true to cancel normal handling
	*/
	public boolean preCopy();

	/**
	 * Copy has just happened
	*/
	public void postCopy();

	/**
	 * Print is about to happen.
	*/
	public void prePrint( TSEPrintSetup pPrintHelper );

	/**
	 * Print has happened
	*/
	public void postPrint( TSEPrintSetup pPrintHelper );

	/**
	 * A deep sync broadcast was received and is about to happen.  Set bHandled to true to cancel normal handling
	*/
	public boolean preDeepSyncBroadcast( ETList<IElement> pDeepSyncElements );

	/**
	 * A deep sync broadcast was received and responded to
	*/
	public void postDeepSyncBroadcast( ETList<IElement> pDeepSyncElements );

	/**
	 * Called before pumping the messages
	*/
	public void prePumpMessages();

	/**
	 * Called after pumping the messages
	*/
	public void postPumpMessages();

	/**
	 * Called after an element has been added to the diagram
	*/
	public void postAddObject( TSGraphObject pGraphObject );

	/**
	 * Called after an element has been added to the diagram, this routine specifically changes the dropped namespace or region as necessary
	*/
	public void postAddObjectHandleContainment( IPresentationElement pPE );

	/**
	 * Allows the engine to handle a delayed action
	*/
	public boolean handleDelayedAction( IDelayedAction pAction );

	/**
	 * Possibly convert any of the diagrams to model elements
	*/
	public void convertDiagramsToElements( IElement[] pMEs, IStrings pDiagramLocations );

	/**
	 * Called after elements have been droped onto the diagram
	*/
	public void postOnDrop( ETList<IElement> pMEs, boolean bAutoRouteEdges );

	/**
	 * Called before a COM interface for creating presentation elements is called.
	*/
	public boolean preCreatePresentationElement( IElement pElement );

	/**
	 * Called to create a specific tool
	*/
	//public void createTool( String sTool, TSTool pTool );

	/**
	 * Puts the drawing area into a certain mouse mode
	*/
	public void enterMode( /* DrawingToolKind */ int nDrawingToolKind );

	/**
	 * Puts the drawing area into a certain mouse mode
	*/
	public void enterMode2( String sMode, String sFullInitString, String sTSViewString, String sGraphObjectObjectInitString );

	/**
	 * The user has entered VK_DELETE.  Allows verification dialogs to be displayed
	*/
	public boolean preHandleDeleteKey();

	/**
	 * Premove event from the drawing area
	*/
	public void onPreMoveObjects(ETList < IETGraphObject > affectedObjects, int dx, int dy);

	/**
	 * Delayed Postmove event from the drawing area
	*/
	public void delayedPostMoveObjects(ETList < IPresentationElement > pPEs, int nDeltaX, int nDeltaY);

	/**
	 * Preresize event from the drawing area
	*/
	public boolean onPreResizeObjects( TSGraphObject graphObject );

	/**
	 * Postresize event from the drawing area
	*/
	public boolean onPostResizeObjects( TSGraphObject graphObject );

	/**
	 * Fired before the control is scrolled and/or zoomed
	*/
	public boolean onPreScrollZoom( double pageCenterX, double pageCenterY, double zoomLevel );

	/**
	 * Fired after the control has been scrolled and/or zoomed
	*/
	public boolean onPostScrollZoom();

	/**
	 * Fired before a cross diagram paste happens.  This allows the diagram engine to enter into paste tool mode.
	*/
	public boolean beginCrossDiagramPaste();

	/**
	 * Ask the user what to do about a name collision
	*/
	public boolean questionUserAboutNameCollision( ICompartment pCompartmentBeingEdited, INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements );

	/**
	 * Fired before the user is questioned about name collisions.  bQuestionUser to FALSE to not ask the user.
	*/
	public boolean preHandleNameCollision( ICompartment pCompartmentBeingEdited, INamedElement pElement, INamedElement pFirstCollidingElement );

	/**
	 * Reattaches the presentation element to the new model element
	*/
	public void handlePresentationElementReattach( ICompartment pCompartmentBeingEdited, INamedElement pElement, INamedElement pFirstCollidingElement );

   /**
    * Test if the user wants to delete the data behind the presenation.
    * 
    * @return <code>true</code> if the user wants to delete the data associted 
    *         with the presentation elements.
    */
   public DataVerificationResults verifyDataDeletion(ETList < TSENode > selectedNodes,
                                                     ETList < TSEEdge > selectedEdges, 
                                                     ETList < TSENodeLabel > selectedNodeLabels, 
                                                     ETList < TSEEdgeLabel > selectedEdgeLabels);
}
