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


package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphObjectValidation;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.graph.TSGraphObject;

public interface IETGraphObject extends ITSGraphObject
{
	/**
	 * The graph object attached to this object.
	*/
	public void setObjectView( TSEObjectUI value );

	/**
	 * The graph object attached to this object.
	*/
	public TSEObjectUI getObjectView();

	/**
	 * The presentation element attached to this object.
	*/
	public void setPresentationElement( IPresentationElement value );

	/**
	 * The presentation element attached to this object.
	*/
	public IPresentationElement getPresentationElement();

	/**
	 * This routine is the TS archive routine.  The OLE_HANDLE is a TSEData*
	*/
	public void readData( /* long */ int pTSEData );

	/**
	 * This routine is the TS archive routine.  The OLE_HANDLE is a TSEDataMgr*
	*/
	public void writeData( /* long */ int pTSEDataMgr );

	/**
	 * This routine is called when the compartment needs to be saved to the IProductArchive
	*/
	public void writeToArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement );

	/**
	 * This routine is called when the compartment needs to be restored from the IProductArchive.
	*/
	public void readFromArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement );

	/**
	 * Returns the Model Element XMIID that was loaded from the file.
	*/
	public String getReloadedModelElementXMIID();

	/**
	 * Returns the Model Element XMIID that was loaded from the file.
	*/
	public void setReloadedModelElementXMIID( String value );

	/**
	 * Returns the Toplevel Element XMIID that was loaded from the file.
	*/
	public String getReloadedTopLevelXMIID();

	/**
	 * Returns the Toplevel Element XMIID that was loaded from the file.
	*/
	public void setReloadedTopLevelXMIID( String value );

	/**
	 * Returns the Toplevel Element XMIID that was loaded from the file.
	*/
	public String getReloadedPresentationXMIID();

	/**
	 * Returns the Toplevel Element XMIID that was loaded from the file.
	*/
	public void setReloadedPresentationXMIID( String value );

	/**
	 * Returns the Toplevel Element XMIID that was loaded from the file.
	*/
	public String getReloadedOwnerPresentationXMIID();

	/**
	 * Returns the Toplevel Element XMIID that was loaded from the file.
	*/
	public void setReloadedOwnerPresentationXMIID( String value );

	/**
	 * Returns a space delimited list of XMIID's which correspond to any referred elements.
	*/
	public IStrings getReferredElements();

	/**
	 * Returns a space delimited list of XMIID's which correspond to any referred elements.
	*/
	public void setReferredElements( IStrings value );

	/**
	 * Returns the IDiagram
	*/
	public IDiagram getDiagram();

	/**
	 * Returns the IDiagram
	*/
	public void setDiagram( IDiagram value );

	/**
	 * Saves the product element to the product archive
	*/
	public void save( IProductArchive pProductArchive );

	/**
	 * Loads the product element from the product archive
	*/
	public void load( IProductArchive pProductArchive );

	/**
	 * Notification that the load process has completed
	*/
	public void postLoad();

	/**
	 * When VK_DELETE is received by the diagram this function is called to affect the model, not just a deletion of a presentation element.
	*/
	public void affectModelElementDeletion();

	/**
	 * Tell the element that the model element has changed
	*/
	public void modelElementHasChanged( INotificationTargets pTargets );

	/**
	 * Tell the element that the model element has been deleted
	*/
	public void modelElementDeleted( INotificationTargets pTargets );

	/**
	 * Returns the draw engine
	*/
	public IDrawEngine getEngine();

	/**
	 * Handle a keydown event
	*/
	public boolean onKeydown( int KeyCode, int Shift );

	/**
	 * Handle a keytyped event
	*/
	public boolean onCharTyped(char ch);

	/**
	 * Handle a keyup event
	*/
	public boolean onKeyup( int KeyCode, int Shift );

	/**
	 * Notifies the node an event has been generated at the graph.
	*/
	public void onGraphEvent( int nKind );

	/**
	 * Retrieves the Top Level XMI ID
	*/
	public String getTopLevelXMIID();

	/**
	 * Sizes the node, edge or label to the contents of the object
	*/
	public void sizeToContents();

	public void onContextMenu(IMenuManager manager);
	
	public IElement create(INamespace space, String initStr);
	public void attach(IElement modEle, String initStr);
	public void onPostAddLink(IETGraphObject newLink, boolean isFromNode);
	public void setVisible(boolean visible);
	public boolean isVisible();
	
	
	/**
	 * Create the presentation and model element for this node and attach yourself
	*/
	public void create( INamespace pNamespace, String sInitializationString, IPresentationElement pCreatedPresentationElement, IElement pCreatedElement );


	/**
	 * Resets the draw engine
	*/
	public long resetDrawEngine( String sInitializationString );

	/**
	 * Invalidates this element
	*/
	public void invalidate();

	/**
	 * Notifies the node that a context menu is about to be displayed
	*/
	public void onContextMenu( IProductContextMenu pContextMenu, int logicalX, int logicalY );

	/**
	 * Notifies the node that a context menu has been selected
	*/
	public void onContextMenuHandleSelection( IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem );


	/**
	 * Set the parent on this guys engine
	*/
	public long setEngineParent( TSEObjectUI pObjectView );

	/**
	 * Gets the initialization string which controls what drawengine will be created.
	*/
	public String getInitializationString();

	/**
	 * Gets the initialization string which controls what drawengine will be created.
	*/
	public void setInitializationString( String value );

	/**
	 * Handle dragging via the left mouse. Return FALSE is dragging is not to be continued.
	*/
	public boolean handleLeftMouseBeginDrag( IETPoint pStartPos, IETPoint pCurrentPos );

	/**
	 * Handle dragging via the left mouse. Return FALSE is dragging is not to be continued.
	*/
	public boolean handleLeftMouseDrag( IETPoint pStartPos, IETPoint pCurrentPos );

	/**
	 * Handle dropping via the left mouse. Return FALSE means invalid data for this product element.
	*/
	public boolean handleLeftMouseDrop( IETPoint ptCurrentPos, IElement[] pElements, boolean bMoving );

	/**
	 * Returns the TSGraphObject this guy represents (TSNode or TSEdge)
	*/
	public TSGraphObject getGraphObject();


	/**
	 * Notifies the node that a link is about to be deleted
	*/
	public long onPreDeleteLink( IETGraphObject pLinkAboutToBeDeleted, boolean bIsFromNode );

	/**
	 * Validates this object.
	*/
	public long validate( IGraphObjectValidation pValidationKind );

	/**
	 * Transforms this presentation element into another, such as an AssocationEdge into an AggregationEdge.
	*/
	public IPresentationElement transform( String typeName );

	/**
	 * The synch state of this element.
	*/
	public int getSynchState();

	/**
	 * The synch state of this element.
	*/
	public void setSynchState( /* SynchStateKind */ int value );

	/**
	 * This flag is set if the diagram was closed when the IElement got deleted.  Its set on load and means this PE should be deleted.
	*/
	public boolean getWasModelElementDeleted();

	/**
	 * Returns a list of selected and draggable elements.
	*/
	public ETList <IElement> getDragElements();

	/**
	 * Handle an application-defined accelerator.
	*/
	public boolean handleAccelerator( String accelerator );

	/**
	 * Synchronizes all presentation elements with data.
	*/
	public long performDeepSynch();
	
	
	
}
