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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

public interface IDrawEngine
{
	/**
	 * This is the name of the drawengine used when storing and reading from the product archive
	*/
	public String getDrawEngineID();

	/**
	 * This returns the ModelElementType
	 */
	public String getElementType();

	/**
	 * This is the string to be used when looking for other similar drawengines.
	*/
	public String getDrawEngineMatchID();

	/**
	 * This is the string to be used when creating presentation elements.
	*/
	public String getPresentationType();

	/**
	 * Get the parent diagram
	*/
	public IDiagram getDiagram();
	
	/*
	 * Get the presentation element;
	 */
	public IGraphPresentation getPresentation();

	/**
	 * Notifies the node an event has been generated at the graph.
	*/
	public void onGraphEvent( int nKind );


	/**
	 * Gives each compartment an opportunity to set the mouse cursor
	*/
	public boolean handleSetCursor( ISetCursorEvent pSetCursorEvent );

	/**
	 * Performs the actual drawing
	*/
	public void doDraw( IDrawInfo pDrawInfo );

	/**
	 * Initializes the draw engine resources
	*/
	public void initResources();

	/*
	 * Returns true if the drawengine is intialized.
	 */
	public boolean isInitialized();

	/**
	 * Redraw the view this engine is attached to
	*/
	public long invalidate();

	/**
	 * Invalidates a region of the drawing area
	*/
	public long invalidateRect( IETRect rect );

	/**
	 * Fired when the context menu is about to be displayed
	*/
	public long onContextMenu( IProductContextMenu pContextMenu, int logicalX, int logicalY );
	public void onContextMenu( IMenuManager manager);

	/**
	 * Fired when a context menu has been selected
	*/
	public long onContextMenuHandleSelection( IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem );

	/**
	 * Initialize the compartments
	*/
	public void initCompartments( IPresentationElement pElement );

	/**
	 * Clears out the compartments and reinitializes them
	*/
	public long reinitCompartments( IPresentationElement pElement );

	/**
	 * This routine is called when the node needs to be saved to the IProductArchive
	*/
	public long writeToArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement );

	/**
	 * This routine is called when the node needs to be restored from the IProductArchive.
	*/
	public long readFromArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement );

	/**
	 * Notification of a post load event.
	*/
	public long postLoad();

	/**
	 * Sets the parent object view this engine represents.
	*/
	public void setParent( IETGraphObjectUI pParent );

	/**
	 * Gets the parent object view this engine represents.
	*/
	public IETGraphObjectUI getParent();

	/**
	 * Gets the parent IETElement if the view implements that interface.
	*/
	public ITSGraphObject getParentETElement();

	/**
	 * Creates a copy of this object.
	*/
	public Object clone();

	/**
	 * Sets an anchor on the current mouse position for possible extend operations.
	*/
	public long anchorMouseEvent( MouseEvent pMouseEvent, ICompartment pCompartment );

	/**
	 * Returns the last compartment that had a mouse anchor event.
	*/
	public ICompartment getAnchoredCompartment();

	/**
	 * Establishes the anchored compartment.
	*/
	public long setAnchoredCompartment( ICompartment pCompartment );

	/**
	 * Sets all compartments to the specified state.
	*/
	public void selectAllCompartments( boolean bSelected );

	/**
	 * Performs an extended select on all compartments between the mouse event position and the anchor position.
	*/
	public void selectExtendCompartments( MouseEvent pEvent );

	/**
	 * Add a compartment to this draw engine - used for list type compartments
	*/
	public void addCompartment( ICompartment pCompartment, int nPos );

	/**
	 * Create and add a compartment to this draw engine, if blank or -1 adds to the bottom.
	*/
	public ICompartment createAndAddCompartment( String sCompartmentID, int nPos );

	/**
	 * Get the list of compartments
	*/
	public ETList <ICompartment> getCompartments();

	/**
	 * Get a list of selected compartments.
	*/
	public ETList <ICompartment> getSelectedCompartments();

	/**
	 * Does this draw engine have selected compartments.
	*/
	public boolean hasSelectedCompartments();

	/**
	 * Set the menu button sensitivity and check
	*/
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind );

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
	 * Return the window handled to the drawing area
	*/
	public int getWindow();

	/**
	 * Calculates the size of this draw engine.  Set bAt100Pct to TRUE to return the 100% (unzoomed) size.
	*/
	public IETSize calculateOptimumSize( IDrawInfo pDrawInfo, boolean bAt100Pct );

	/**
	 * Size node to the contents of the draw engine.
	*/
	public void sizeToContents();

   /**
    * Calls SizeToContents() at a more opertune time.
   */
   public void delayedSizeToContents();

	/**
	 * Deletes and recreates all labels at a more opertune time.
	*/
	public void delayedDeleteAndReinitializeAllLabels();

	/**
	 * The rectangle used for last drawing operation, in screen coordinates
	*/
	public IETRect getBoundingRect();

	/**
	 * The rectangle used for last drawing operation, in logical coordinates
	*/
	public IETRect getLogicalBoundingRect( boolean bIncludeLabels );

	/**
	 * Set the drawing area to dirty
	*/
	public long setIsDirty();

	/**
	 * Allows this object to add tooltip data.
	*/
	public long queryToolTipData( IToolTipData pToolTipData );

	/**
	 * Notifier that the model element has changed, if available the changed IFeature is passed along.
	*/
	public long modelElementHasChanged( INotificationTargets pTargets );

	/**
	 * Notifier that the model element has been deleted, if available the changed IFeature is passed along.
	*/
	public long modelElementDeleted( INotificationTargets pTargets );

	/**
	 * Notifier that one of our compartments has been expanded/collapsed.
	*/
	public long onCompartmentCollapsed( ICompartment pCompartment, boolean bCollapsed );

	/**
	 * Is this draw engine valid for this model element?  Pass in a null pElement to check the current element
	*/
	public boolean isDrawEngineValidForModelElement();

	/**
	 * Validates the model data against the displayed data.
	*/
	public boolean validateNode();

	/**
	 * Get/Set the default compartment.
	*/
	public ICompartment getDefaultCompartment();

	/**
	 * Get/Set the default compartment.
	*/
	public void setDefaultCompartment( ICompartment value );

	/**
	 * Returns a collection of elements in response to a drag event
	*/
	public IElement[] getOLEDragElements();

	/**
	 * Synchronizes all presentation elements with data.
	*/
	public long performDeepSynch();

	/**
	 * Should we draw a red outline if we're out of sync.
	*/
	public boolean getCheckSyncStateDuringDraw();

	/**
	 * Should we draw a red outline if we're out of sync.
	*/
	public void setCheckSyncStateDuringDraw( boolean value );

	/**
	 * Removes this compartment from the drawengine, optionally deleting all associated model elements.
	*/
	public void removeCompartment( ICompartment pCompartment, boolean bDeleteElement );

	/**
	 * Handle an application-defined accelerator.
	*/
	public boolean handleAccelerator( String accelerator );

	/**
	 * Finds the compartment with the given compartment ID.
	*/
	public ICompartment findCompartmentByCompartmentID( String sCompartmentID );

	/**
	 * Finds the compartment with the given title
	*/
	public ICompartment findCompartmentByTitle( String sName );

	/**
	 * Finds the compartment that contains the element.
	*/
	public ICompartment findCompartmentContainingElement( IElement pElement );

	/**
	 * Is the compartment contained in this drawengine somewhere?
	*/
	public boolean findCompartment( ICompartment pCompartment );

	/**
	 * Retrieves the compartment under a point.  Point must be in client coordinates.
	*/
	public ICompartment getCompartmentAtPoint( IETPoint pCurrentPos );

	/**
	 * Is this drawengine readonly?
	*/
	public boolean getReadOnly();

	/**
	 * Is this drawengine readonly?
	*/
	public void setReadOnly( boolean value );

	/**
	 * Called when a node is resized.
	*/
	public void onResized( );

	/**
	 * Called before the owner node is resized so that this view can restrict the way in which a resize can occur
	*/
	public Dimension validateResize( int x, int y );

	/**
	 * When VK_DELETE is received by the diagram this function is called to affect the model, not just a deletion of a presentation element.
	*/
	public void affectModelElementDeletion();

	/**
	 * Lays out the draw engine compartments, and other elements
	*/
	public void layout();

	/**
	 * Returns the label manager for this node or edge
	*/
	public ILabelManager getLabelManager();

	/**
	 * Returns the edge manager for this node
	*/
	public IEventManager getEventManager();

	public String getManagerMetaType(int nManagerKind);

	/**
	 * Returns true when the draw engine can graphically contain other draw engines
	*/
	public boolean getIsGraphicalContainer();

   /**
    Retrieves the smallest node draw engine that graphically contains this draw engine
    */
   public IDrawEngine getGraphicalContainer();

   /**
    * Clears the member variable that retains the graphical container
    */
   public void resetGraphicalContainer();

	/**
	 * Finds the list compartment that contains this compartment.
	*/
	public IListCompartment findListCompartmentContainingCompartment( ICompartment pCompartment );

	/**
	 * Analogous to TSENodeView::setupOwner (and edge).  Called when the user is initially dropping the node/edge onto a diagram
	*/
	public void setupOwner();

	/**
	 * Displays a color dialog
	*/
	public boolean displayColorDialog( /* ResourceIDKind */ int nKind, int pCOLORREF );

	/**
	 * Makes the current value of the resource kind the preferred value (updates preferences)
	*/
	public void updateColorPreferenceToCurrent( /* ResourceIDKind */ int nKind );

	/**
	 * Displays a font dialog.  If user cancels dialog pUserSelectedFont is NULL
	*/
	public void displayFontDialog( int pCOLORREF, Object pUserSelectedFont, boolean bUserSelectedFontOrColor );

	/**
	 * Fired before the user is questioned about name collisions.  bQuestionUser to FALSE to not ask the user.
	*/
	public boolean preHandleNameCollision( ICompartment pCompartmentBeingEdited, INamedElement pElement, INamedElement pFirstCollidingElement );


	/**
	 * Handle dragging via the left mouse. Return FALSE is dragging is not to be continued.
	*/
	public boolean handleLeftMouseBeginDrag( IETPoint pStartPos, IETPoint pCurrentPos );

	/**
	 * Handle dragging via the left mouse. Return FALSE is dragging is not to be continued.
	*/
	public boolean handleLeftMouseDrag( IETPoint pStartPos, IETPoint pCurrentPos );

	/**
	 * Handle dropping via the left mouse.
	*/
	public boolean handleLeftMouseDrop( IETPoint pCurrentPos, List pElements, boolean bMoving );

	/**
	 * Right mouse button down event
	*/
	public boolean handleRightMouseButton(MouseEvent pEvent);

	/**
	 * mouse button down event
	*/
	public boolean handleLeftMouseButton(MouseEvent pEvent);

	/**
	 *  mouse button down event
	*/
	public boolean handleLeftMouseButtonPressed(MouseEvent pEvent);

	/**
	 * Right mouse button down event
	*/
	public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent);
	
	public boolean copy(IDrawEngine pConstDrawEngine);

	public IETGraphObjectUI getUI();
	public int getLastDrawPointY();
	public void setLastDrawPointY(int i);
	public void updateLastDrawPointY(double d);
	
	public double getLastDrawPointWorldY();
	public void setLastDrawPointWorldY(double i);
	public void updateLastDrawPointWorldY(double d);
	
	public void createCompartments() throws ETException;

	public void init() throws ETException;

	public IDrawingAreaControl getDrawingArea();

	/**
	 * @return DrawEngine's ui resources
	 */
	public UIResources getResources();
	
	/**
	 * @param resourceKind see UIResources class for the values list
	 *
	 * @return DrawEngine's ui resource name
	 */
	public String getResourceName(int resourceKind);
	
	/**
	 * @param resourceKind see UIResources class for the values list
	 */
	public void setFontResource(int resourceKind, Font font);

	/**
	 * @param resourceKind see UIResources class for the values list
	 */
	public void setColorResource(int resourceKind, Color color);
	
	/*
	 * Dispatches draw to all the compartments
	 */
	public void dispatchDrawToCompartments(IDrawInfo pInfo, IETRect pDeviceBounds);
	
	public int getLastResizeOriginator();
	public void setLastResizeOriginator(int i);
	
	/*
	 * Gets called just before the parent is discarded from the diagram.
	 */
	public void onDiscardParentETElement();
}
