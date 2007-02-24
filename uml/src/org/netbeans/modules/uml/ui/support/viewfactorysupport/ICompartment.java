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

import java.awt.event.MouseEvent;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

import java.awt.Color;
import java.awt.Font;

public interface ICompartment
{
   public CompartmentResourceUser getCompartmentResourceUser();
   
   /** Used to specify that he compartment should expand to the end of the node. */
   public final static int EXPAND_TO_NODE = -1;
   
   /**
    * This is the name of the compartment used when storing and reading from the product archive
    */
   public String getCompartmentID();
   
   /**
    * Notifies the compartment an event has been generated at the graph.
    */
   public long onGraphEvent( /* GraphEventKind */ int nKind );
   
   /**
    * Performs the actual drawing
    */
   public void draw( IDrawInfo pDrawInfo, IETRect BoundingRect );
   
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
    * Set the menu button sensitivity
    */
   public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind );
   
   /**
    * Should this compartment show its name
    */
   public boolean getShowName();
   
   /**
    * Should this compartment show its name
    */
   public void setShowName( boolean value );
   
   /**
    * The compartments name
    */
   public String getName();
   
   /**
    * The compartments name
    */
   public void setName( String value );
   
   /**
    * Determines if this compartment has anything to write to the presentation archive
    */
   public boolean hasOverride();
   
   /**
    * Calculates the size of this compartment using the current font and name value.  Set bAt100Pct to TRUE to return the 100% (unzoomed) size.
    */
   public IETSize calculateOptimumSize( IDrawInfo pDrawInfo, boolean bAt100Pct );
   
   /**
    * Gets the last calculated optimum size of this compartment.
    */
   public IETSize getOptimumSize( boolean bAt100Pct );
   
   /**
    * Gets the current size of this compartment which is either the optimum size or a user defined size.
    */
   public IETSize getCurrentSize( boolean bAt100Pct );

	/**
	 * Gets the current size of this compartment which is either the optimum size or a user defined size on a specified Device.
	 */   
   public IETSize getCurrentSize(TSTransform transform, boolean bAt100Pct );
   
   /**
    * Sets the current size of this compartment.
    */
   public void setCurrentSize( IETSize pNewSize );
   
   /**
    * Gets the last size used during drawing of this compartment.
    */
   public IETSize getVisibleSize(boolean bAt100Pct );
   
   /**
    * Sets the absolute size used by the tranform operations.
    */
   public void setTransformSize( IETSize pNewSize );
   
   /**
    * Sets the absolute size used by the tranform operations.
    */
   public void setTransformSize( int width, int height);
   
   /**
    * Adds a model element to this compartment
    */
   public void addModelElement( IElement pElement, int nIndex );
   
   /**
    * Sets the parent drawengine for this compartment
    */
   public void setEngine( IDrawEngine pEngine );
   
   /**
    * Gets the parent drawengine for this compartment
    */
   public IDrawEngine getEngine();
   
   /**
    * Creates a copy of this object.
    */
   public ICompartment clone( IDrawEngine pParentDrawEngine );
   
   /**
    * Right mouse button down event
    */
   public boolean handleRightMouseButton( MouseEvent pEvent );
   
   /**
    * Right mouse button down event
    */
   public boolean handleLeftMouseButton( MouseEvent pEvent );
   
   /**
    * Right mouse button down event
    */
   public boolean handleLeftMouseButtonDoubleClick( MouseEvent pEvent );
   
   
   public boolean handleLeftMouseButtonPressed(MouseEvent pEvent);
   
   
   /**
    * Gives each compartment an opportunity to set the mouse cursor
    * In C++ we just passed the point (in win scaled owner coordinates) into the compartment.
    * In Java we also need to pass in ISetCursorEvent in order to be able to set the cursor properly
    */
   public boolean handleSetCursor( IETPoint point, ISetCursorEvent event );
   
   /**
    * Initializes the compartment resources
    */
   public void initResources();
   
   /**
    * Determine the compartment's selected state
    */
   public boolean isSelected();
   
   /**
    * Determine the compartment's selected state
    */
   public void setSelected( boolean value );
   
   /**
    * Toggles the compartment's selected state
    */
   public void invertSelected();
   
   /**
    * Sets the compartment's selected state
    */
   public boolean selectExtended( IETRect rect );
   
   /**
    * This routine is called when the compartment needs to be saved to the IProductArchive
    */
   public IProductArchiveElement writeToArchive( IProductArchive pProductArchive, IProductArchiveElement pCompartmentElement );
   
   /**
    * This routine is called when the compartment needs to be restored from the IProductArchive.
    */
   public void readFromArchive( IProductArchive pProductArchive, IProductArchiveElement pCompartmentElement );
   
   /**
    * Contained compartments and engines use their parent's resources.
    */
   public int getParentResource();
   
   /**
    * Contained compartments and engines use their parent's resources.
    */
   public long setParentResource( /* long */ int pParentResource );
   
   /**
    * Does this compartment have a non-rectangular shape
    */
   public boolean getCompartmentHasNonRectangularShape();
   
   /**
    * Returns the shape of this compartment
    */
   public ETList< IETPoint > getCompartmentShape();
   
   /**
    * Returns the bounding rect for this compartment
    */
   public IETRect getBoundingRect();
   
   /**
    * The TS logical bounding rectangle for this compartment
    */
   public IETRect getLogicalBoundingRect();
   
   /**
    * Returns true when the input logical view point is within the bounds of the y upper and lower axis of the compartment
    */
   public boolean isPointInCompartmentYAxis( IETPoint pLogical );
   
   /**
    * Returns true when the input logical view point is within the bounds of the compartment
    */
   public boolean isPointInCompartment( IETPoint pLogical );
   
   /**
    * Returns true when the input logical view point is within the optimum size bounds of the compartment
    */
   public boolean isPointInOptimum( IETPoint pLogical );
   
   /**
    * Returns the offset within the draw engine's rectangle for the compartment in logical coordinates
    */
   public IETPoint getLogicalOffsetInDrawEngineRect();
   
   /**
    * Returns the offset within the draw engine's rectangle for the compartment in logical coordinates
    */
   public void setLogicalOffsetInDrawEngineRect( IETPoint value );
   
   /**
    * true if this node is currently selected and resizeable.
    */
   public boolean isResizing();
   
   /**
    * Called when a node is resized.  nodeResizeOriginator is a TSENodeResizeOriginator
    */
   public long nodeResized( int nodeResizeOriginator );
   
   /**
    * Moves the connector to the vertical location, in logical view coordinates
    */
   //public long moveConnector( TSDConnector pConnector, int nY, boolean bDoItNow, boolean bSetYOfAssociatedPiece );
   
   /**
    * Save the model element this compartment represents
    */
   public long saveModelElement();
   
   /**
    * returns the model element associated with this compartment
    */
   public IElement getModelElement();
   
   /**
    * returns the model element XMIID associated with this compartment
    */
   public String getModelElementXMIID();
   
   public void setModelElementXMIID(String newVal);
   
   /**
    * Is this compartment visible.
    */
   public boolean getVisible();
   
   /**
    * Is this compartment visible.
    */
   public void setVisible( boolean value );
   
   /**
    * Enables or disables the compartments context menu.
    */
   public boolean getEnableContextMenu();
   
   /**
    * Enables or disables the compartments context menu.
    */
   public void setEnableContextMenu( boolean value );
   
   /**
    * Handle a keydown event
    */
   public boolean handleKeyDown( int KeyCode, int Shift );
   
   /**
    * Handle a keydown event
    */
   public boolean handleCharTyped(char ch);
   
   /**
    * Handle a keyup event
    */
   public boolean handleKeyUp( int KeyCode, int Shift );
   
   /**
    * Handle dragging via the left mouse. Return FALSE if dragging is not to be continued.
    */
   public boolean handleLeftMouseBeginDrag( IETPoint pStartPos, IETPoint pCurrentPos, boolean bCancel );
   
   /**
    * Handle dragging via the left mouse. Return FALSE if dragging is not to be continued.
    */
   public boolean handleLeftMouseDrag( IETPoint pStartPos, IETPoint pCurrentPos );
   
   /**
    * Handle dropping via the left mouse.
    */
   public boolean handleLeftMouseDrop( IETPoint pCurrentPos, List pElements, boolean bMoving );
   
   /**
    * Is this compartment resizeable? (FALSE if height is fixed.)
    */
   public boolean isResizeable();
   
   /**
    * Can this compartment be collapsed?
    */
   public boolean isCollapsible();
   
   /**
    * Get/Set the collapsed state of the compartment
    */
   public boolean getCollapsed();
   
   /**
    * Get/Set the collapsed state of the compartment
    */
   public void setCollapsed( boolean value );
   
   /**
    * Place a specific type of decoration on the node at the specified location
    */
   public void addDecoration( String sDecorationType, IETPoint pLocation );
   
   /**
    * Indicate to the draw engine that it is being stretched
    */
   public long stretch( IStretchContext pStretchContext );
   
   /**
    * Restores the compartment size to its optimum size
    */
   public long clearStretch(IDrawInfo drawInfo);
   
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
    * Re-reads the model element data
    */
   public void reattach( String sID );
   
   /**
    * Is this compartment readonly?
    */
   public boolean getReadOnly();
   
   /**
    * Is this compartment readonly?
    */
   public void setReadOnly( boolean value );
   
   /**
    * Is this compartment valid (connected and in-synch with its model element)
    */
   public boolean validate( IElement pElement );
   
   /**
    * Returns the compartment responsible for handling keyboard input when none other is selected.
    */
   public ICompartment getDefaultCompartment();
   
   /**
    * Returns the desired size based on whether the scrollbar is visible.
    */
   public IETSize getDesiredSizeToFit();
   
   /**
    * Lays out the compartment, and its elements
    */
   public long layout( IETRect pCompartmentInDE );
   
   /**
    * Notification of a post load event.
    */
   public long postLoad();
   
   /**
    * Invokes the in-place editor.
    */
   public long editCompartment( boolean bNew, int nKeyCode, int nShift, int nPos );
   
//   /**
//    * Allows you to directly change how this compartments text draws
//    */
//   public void setStyle( int value );
   
   /**
    * Turns on and off text wrapping
    */
   public boolean getTextWrapping();
   
   /**
    * Turns on and off text wrapping
    */
   public void setTextWrapping( boolean value );
   
   /**
    * Turns on and off the horizontal centering of the text
    */
   public boolean getCenterText();
   
   /**
    * Turns on and off the horizontal centering of the text
    */
   public void setCenterText( boolean value );
   
   /**
    * Turns on and off the vertically centering of the text
    */
   public boolean getVerticallyCenterText();
   
   /**
    * Turns on and off the vertically centering of the text
    */
   public void setVerticallyCenterText( boolean value );
   
   /**
    * Retrieves the presentation elements graphically contained by this compartment.
    */
   public ETList< IPresentationElement > getContained();
   
   //	public String getStaticText();
   
   //	public void setName(String string);
   
   /**
    * Returns the stereotype name for the model element
    */
   public String getStereotypeText(IElement pElement);
   
   /** 
    * Retreives the font definition to use when rendering the component.
    */
   public String getFontString();
   
   /**
    * Set the font definition to use when rendering the component.
    */
   public void setFontString(String string);
   
   public ContextMenuActionClass createMenuAction(String text, String menuID);
   
   /**
    * Retrieves the actual font to use when compartment is rendered.  The returned 
    * font will be based on the zoom level.
    *
    * @param zoomLevel The zoom that is used to determine the size of the font.
    * @return The font.
    */
   public Font getCompartmentFont(double zoomLevel);

   /**
    * Retrieves the color of the font to use when rendering the compartment.
    */
   public Color getCompartmentFontColor();
   
}
