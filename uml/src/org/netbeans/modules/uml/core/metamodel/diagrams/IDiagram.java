/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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



package org.netbeans.modules.uml.core.metamodel.diagrams;


import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.filesystems.FileLock;

public interface IDiagram extends IPresentationElement
{
   public static final int DK_UNKNOWN               = 0;
   public static final int DK_DIAGRAM               = 1;
   public static final int DK_ACTIVITY_DIAGRAM      = 2;
   public static final int DK_CLASS_DIAGRAM         = 4;
   public static final int DK_COLLABORATION_DIAGRAM = 8;
   public static final int DK_COMPONENT_DIAGRAM     = 16;
   public static final int DK_DEPLOYMENT_DIAGRAM    = 32;
   public static final int DK_SEQUENCE_DIAGRAM      = 64;
   public static final int DK_STATE_DIAGRAM         = 128;
   public static final int DK_USECASE_DIAGRAM       = 256;
   public static final int DK_ALL                   = 0xffff;

        /**
         * Notifies the diagram to refresh the node graphical object that 
         * is associated with the presentation element.
         * 
         * @param presentation The presentation element that needs to be refreshed.
         * @return true if the presenation element was found and refreshed.
         */
        public boolean refresh(IPresentationElement presentation,boolean resizetocontent);
      
	/**
	 * Saves the diagram.
	*/
	public void save() throws IOException;

//       /** 
//        * Allows the diagram to perform some cleanup before the diagram is actually
//        * closed. 
//        */
//       public void preClose();
   
	/**
	 * Is this diagram readonly?
	*/
	public boolean getReadOnly();

	/**
	 * Is this diagram readonly?
	*/
	public FileLock setReadOnly( boolean value );

	/**
	 * Saves the diagram as a BMP, EMF or JPG file.
	*/
	public void saveAsGraphic( String sFilename, /* SaveAsGraphicKind */ int nKind );

//	/**
//	 * Saves the diagram as a BMP, EMF or JPG file.  Use pMap to get the details of the graphic.
//	*/
//	public IGraphicExportDetails saveAsGraphic( String sFilename, /* SaveAsGraphicKind */ int nKind );
	
	public IGraphicExportDetails saveAsGraphic( String sFilename, /* SaveAsGraphicKind */ int nKind, double scale);

	/**
	 * Get/Set the name of this drawing.
	*/
	public String getName();

	/**
	 * Get/Set the name of this drawing.
	*/
	public void setName( String value );

	/**
	 * Get/Set the alias of this drawing.
	*/
	public String getAlias();

	/**
	 * Get/Set the alias of this drawing.
	*/
	public void setAlias( String value );

	/**
	 * Sets / Gets the name or alias of this element.
	*/
	public String getNameWithAlias();

//	/**
//	 * Sets / Gets the name or alias of this element.
//	*/
//	public void setNameWithAlias( String value );
//
	/**
	 * Retrieves the fully qualified name of the element. This will be in the form 'A::B::C'.
	*/
	public String getQualifiedName();

	/**
	 * Get the filename that this view is saved to
	*/
	public String getFilename();

//	/**
//	 * Set/Get the current layout style.
//	*/
//	public void setLayoutStyle( /* LayoutKind */ int value );
//
//	/**
//	 * Set/Get the current layout style.
//	*/
//	public int getLayoutStyle();
//
//	/**
//	 * Set/Get the current layout style.
//	*/
//	public void setLayoutStyleSilently( /* LayoutKind */ int value );
//
//	/**
//	 * Immediately sets the layout style.  It bypasses the delayed actions.
//	*/
//	public void immediatelySetLayoutStyle( /* LayoutKind */ int nLayoutStyle, boolean bSilent );
//
//	/**
//	 * Use a delayed action to perform the layout style, possibly ignoring containment
//	*/
//	public void delayedLayoutStyle( /* LayoutKind */ int nLayoutStyle, boolean bIgnoreContainment );
//
//	/**
//	 * Show/Close the image dialog..
//	*/
//	public void showImageDialog();
//
//	/**
//	 * Print preview this window
//	*/
//	public void printPreview( String sTitle, boolean bCanMoveParent );
//
//	/**
//	 * Shows the print setup dialog
//	*/
//	public void loadPrintSetupDialog();
//
//	/**
//	 * Prints this control
//	*/
//	public void printGraph( boolean bShowDialog );
//
//	/**
//	 * Is the diagram currently undergoing layout.
//	*/
//	public boolean getLayoutRunning();
//
//	/**
//	 * What is the current zoom of the diagram.
//	*/
//	public double getCurrentZoom();
//
//	/**
//	 * Get the extreme values for the zoom.
//	*/
//	public ETPairT<Double, Double> getExtremeZoomValues();
//
//	/**
//	 * Zoom the diagram.
//	*/
//	public void zoom( double nScaleFactor );
//
//	/**
//	 * Zoom in.
//	*/
//	public void zoomIn();
//
//	/**
//	 * Zoom out.
//	*/
//	public void zoomOut();
//
//	/**
//	 * Fit the current diagram to the window.
//	*/
//	public void fitInWindow();
//
//	/**
//	 * Show the custom zoom dialog.
//	*/
//	public void onCustomZoom();

	/**
	 * Gets the drawing area namespace
	*/
	public INamespace getNamespace();

	/**
	 * Gets the drawing area namespace
	*/
	public void setNamespace( INamespace value );

	/**
	 * Returns the the namespace to use when elements are created on the diagram.  Usually this is the same as the namespace of the diagram
	*/
	public INamespace getNamespaceForCreatedElements();

//	/**
//	 * Load this diagram from a .etl file.
//	*/
//	public int load( String sFilename );

//	/**
//	 * Puts the drawing area into a certain mouse mode
//	*/
//	public void enterMode( /* DrawingToolKind */ int nDrawingToolKind );
//
//	/**
//	 * Puts the drawing area into a specific mode based on the sButtonID.  sButtonID is a key in the PresentationTypes.etc file.
//	*/
//	public void enterModeFromButton( String sButtonID );
//
//	/**
//	 * Invalidates the drawing area
//	*/
//	public void refresh( boolean bPostMessage /*=false*/ );
//
//	/**
//	 * Cut the selected objects
//	*/
//	public void cut();
//
//	/**
//	 * Copy the selected objects
//	*/
//	public void copy();
//
//	/**
//	 * Paste the selected objects
//	*/
//	public void paste();
//
//	/**
//	 * Clears the clipboard of selected objects
//	*/
//	public void clearClipboard();
//
//	/**
//	 * Deletes the selected objects
//	*/
//	public void deleteSelected( boolean bAskUser );
//
//	/**
//	 * Are there items on the clipbaord
//	*/
//	public void itemsOnClipboard( boolean bItemsOnClipboard );

	/**
	 * Select all presentation elements
	*/
	public void selectAll( boolean bSelect );

//	/**
//	 * Select all similar presentation elements
//	*/
//	public void selectAllSimilar();
//
//	/**
//	 * Transforms a rect from logical coordinates to device coordinates.
//	*/
//	public IETRect logicalToDeviceRect( IETRect rcLogical );
//
//	/**
//	 * Transforms a point from logical coordinates to device coordinates.
//	*/
//	public IETPoint logicalToDevicePoint( IETPoint ptLogical );
//
//	/**
//	 * Transforms a rect from device coordinates to logical coordinates.
//	*/
//	public IETRect deviceToLogicalRect( IETRect rcDevice );
//
//	/**
//	 * Transforms a point from device coordinates to logical coordinates.
//	*/
//	public IETPoint deviceToLogicalPoint( IETPoint ptDevice );
//   
//   /**
//    * Transforms a point from device coordinates to logical coordinates.
//   */
//   public IETPoint deviceToLogicalPoint( int x, int y );

	/**
	 * Centers the drawing area on the presentation element.
	*/
	public void centerPresentationElement( IPresentationElement pPresentationElement, boolean bSelectIt, boolean bDeselectAllOthers );

	/**
	 * Centers the drawing area on the presentation element.
	*/
	public void centerPresentationElement( String sXMIID, boolean bSelectIt, boolean bDeselectAllOthers );
        
        /**
         * Centers the diagram on a specified point.
         * 
         * @param scenePoint The point in scene coordinates.
         */
        public void centerPoint(Point scenePoint);
        
        /**
         * Centers the diagram on a specified rectangle
         * 
         * @param sceneRect The rectangle in scene coordinates.
         */
        public void centerRectangle(Rectangle sceneRect);

//	/**
//	 * Does the stacking command nStackingCommand make sense?  Used for update of stacking order buttons.
//	*/
//	public boolean isStackingCommandAllowed( /* StackingOrderKind */ int nStackingCommand );
//
//	/**
//	 * Execute this stacking command.
//	*/
//	public void executeStackingCommand( /* StackingOrderKind */ int nStackingCommand );

	/**
	 * Does this graph have edges, labels or nodes?
	*/
	public void hasGraphObjects( boolean bHasObjects );

	/**
	 * Get/Set the type of this drawing.
	*/
	public int getDiagramKind();
//
//	/**
//	 * Get/Set the type of this drawing.
//	*/
//	public void setDiagramKind( /* DiagramKind */ int value );

	/**
	 * Get/Set the type of this drawing.
	*/
	public String getDiagramKindAsString();
//
//	/**
//	 * Get/Set the type of this drawing.
//	*/
//	public void setDiagramKind2( String value );
//
//	/**
//	 * Initialize a newly created diagram.  This adds the diagram to the current IWorkspace.
//	*/
//	public void initializeNewDiagram( INamespace pNamespace, String sName, /* DiagramKind */ int pKind );

//	/**
//	 * Inverts the selected objects.
//	*/
//	public void invertSelection();
//
	/**
	 * Returns a list of the selected items.
	*/
	public ETList<IPresentationElement> getSelected();
//
//	/**
//	 * Returns a list of the selected items that are of the indicated type (ie Class).
//	*/
//	public ETList<IPresentationElement> getSelectedByType( String bstrType );
//
//	/**
//	 * Returns the HWND to the drawing area.
//	*/
//	public Frame getWindowHandle();
//
	/**
	 * Returns a list of all the items.
	*/
	public ETList<IPresentationElement> getAllItems();

	/**
	 * Returns a list of all the items that represent the IElement.
	*/
	public ETList<IPresentationElement> getAllItems( IElement pModelElement );

	/**
	 * Returns a list of all the model elements on the diagram.
	*/
	public ETList<IElement> getModelElements();

	/**
	 * Select all the objects on the diagram that are of the indicated type
         * 
         * @param type The type of the model element.
         * @return  The list of presentation elements that represent the specified
         *          model element type.
	*/
	public ETList<IPresentationElement> getAllByType( String type );
//
//	/**
//	 * Is the layout properties window open.
//	*/
//	public boolean getIsLayoutPropertiesDialogOpen();
//
//	/**
//	 * Show/Close the layout property window.
//	*/
//	public void layoutPropertiesDialog( boolean bShow );
//
//	/**
//	 * Is the graph preferences window open.
//	*/
//	public boolean getIsGraphPreferencesDialogOpen();
//
//	/**
//	 * Show/Close the graph preferences window.
//	*/
//	public void graphPreferencesDialog( boolean bShow );
//
//	/**
//	 * Is the overview window open.
//	*/
//	public boolean getIsOverviewWindowOpen();
//
//	/**
//	 * Show/Hide the overview window.
//	*/
//	public void overviewWindow( boolean bShowIt );
//
//	/**
//	 * Returns the window rect of the overview window, if it is open.
//	*/
//	public Rectangle getOverviewWindowRect( int pLeft, int pTop, int pWidth, int pHeight );
//
//	/**
//	 * Sets the window rect of the overview window, if it is open.
//	*/
//	public void setOverviewWindowRect( int nLeft, int nTop, int nWidth, int nHeight );
//
//	/**
//	 * Are the tooltips enabled?
//	*/
//	public boolean getAreTooltipsEnabled();
//
//	/**
//	 * Enable/Disable tooltips.
//	*/
//	public void setEnableTooltips( boolean bEnable );
//
//	/**
//	 * Does this diagram have selected nodes?
//	*/
//	public boolean getHasSelected( boolean bDeep );
//
//	/**
//	 * Does this diagram have selected nodes?
//	*/
//	public boolean getHasSelectedNodes( boolean bDeep );
//
//	/**
//	 * Hide/Show the grid
//	*/
//	public boolean getShowGrid();
//
//	/**
//	 * Hide/Show the grid
//	*/
//	public void setShowGrid( boolean value );
//
//	/**
//	 * Sets/Gets the current grid size
//	*/
//	public int getGridSize();
//
//	/**
//	 * Sets/Gets the current grid size
//	*/
//	public void setGridSize( int value );
//
//	/**
//	 * Sets/Gets the type of grid this is being displayed
//	*/
//	public int getGridType();
//
//	/**
//	 * Sets/Gets the type of grid this is being displayed
//	*/
//	public void setGridType( /* GridKind */ int value );
//
//	/**
//	 * Returns the two phase commit object for the diagram.
//	*/
//	public ITwoPhaseCommit getTwoPhaseCommit();
//
//	/**
//	 * Get/Set the locked state for the current mode.
//	*/
//	public boolean getModeLocked();
//
//	/**
//	 * Get/Set the locked state for the current mode.
//	*/
//	public void setModeLocked( boolean value );
//
//	/**
//	 * Used to set sticky buttons.  The user of the diagram can set/get this to determine when to lock/unlock the current tool.
//	*/
//	public int getLastSelectedButton();
//
//	/**
//	 * Used to set sticky buttons.  The user of the diagram can set/get this to determine when to lock/unlock the current tool.
//	*/
//	public void setLastSelectedButton( int value );
//
//	/**
//	 * Validates the diagram.
//	*/
//	public IDiagramValidationResult validateDiagram( boolean bOnlySelectedElements, IDiagramValidation pDiagramValidation );
//
//	/**
//	 * Sync the selected (or all) elements.
//	*/
//	public void syncElements( boolean bOnlySelectedElements );
//
//	/**
//	 * Causes the diagram to take focus.
//	*/
//	public void setFocus();
//
//	/**
//	 * Try to reconnect the link from pOldNode to pNewNode.
//	*/
//	public boolean reconnectLink( IPresentationElement pLink, IPresentationElement pFromNode, IPresentationElement pToNode );
//
//	/**
//	 * Resizes the elements (selected or all) to their contents.
//	*/
//	public void sizeToContents( boolean bJustSelectedElements );
//
//	/**
//	 * Posts a delayed action to the diagram.  Use when you may be in a dangerous callstack to perform the necessary action
//	*/
//	public void postDelayedAction( IDelayedAction pAction );

	/**
	 * Receives notification of a broadcast.  Used by the IProxyDiagramManager to broadcast functions to all open views.
	*/
	public void receiveBroadcast( IBroadcastAction pAction );

	/**
	 * IsDirty is true when there is data that needs to be saved
	*/
	public boolean isDirty();

//	/**
//	 * IsDirty is true when there is data that needs to be saved
//	*/
//	public void setIsDirty( boolean value );

	/**
	 * Is this diagram the same diagram as the one passed in?
	*/
	public boolean isSame( IDiagram pDiagram );

	/**
	 * Returns the presentation element on the diagram with the specified xml id
	*/
	public IPresentationElement findPresentationElement( String sXMLID );

	/**
	 * Returns the relationship discovery object
	*/
	public ICoreRelationshipDiscovery getRelationshipDiscovery();

//	/**
//	 * Processes all the diagram messages
//	*/
//	public void pumpMessages( boolean bJustDrawingMessages );

	/**
	 * Adds an associated diagram
	*/
	public void addAssociatedDiagram( String sDiagramXMIID );

	/**
	 * Adds an associated diagram
	*/
	public void addAssociatedDiagram2( IProxyDiagram pDiagram );

	/**
	 * Removes an associated diagram
	*/
	public void removeAssociatedDiagram( String sDiagramXMIID );

	/**
	 * Removes an associated diagram
	*/
	public void removeAssociatedDiagram2( IProxyDiagram pDiagram );

	/**
	 * Returns the associated diagrams
	*/
	public ETList<IProxyDiagram> getAssociatedDiagrams();

	/**
	 * Is this an associated diagram?
	*/
	public boolean isAssociatedDiagram( String sDiagramXMIID );

	/**
	 * Is this an associated diagram?
	*/
	public boolean isAssociatedDiagram2( IProxyDiagram pDiagram );

	/**
	 * Adds an associated model element
	*/
	public void addAssociatedElement( String sTopLevelElementXMIID, String sModelElementXMIID );

	/**
	 * Adds an associated model element
	*/
	public void addAssociatedElement2( IElement pElement );

	/**
	 * Removes an associated model element
	*/
	public void removeAssociatedElement( String sTopLevelElementXMIID, String sModelElementXMIID );

	/**
	 * Removes an associated model element
	*/
	public void removeAssociatedElement2( IElement pElement );

	/**
	 * Returns the associated model elements
	*/
	public ETList<IElement> getAssociatedElements();

	/**
	 * Is this an associated element?
	*/
	public boolean isAssociatedElement( String sModelElementXMIID );

	/**
	 * Is this an associated element?
	*/
	public boolean isAssociatedElement2( IElement pElement );
        
        public void setNotify(boolean val);
        
        public boolean getNotify();

//   /**
//    * This method sets wheter the graph should be updated automatically or on
//    * request.
//    * 
//    * @param value <code>true</code> if the boudns should update automatcially.
//    */
//   public void setAutoUpdateBounds(boolean value);
//
//	/*
//	 * Returns if the displaywindow should be allowed to redraw.
//	 */   
//	public boolean getAllowRedraw();
//	
//	/*
//	 * Set if window should process draw events or ignore them.
//	 */
//	public void setAllowRedraw(boolean allow);
//
//	/*
//	 * Set when the diagrma is creating itself from selected elements.
//	 */
//	public void setPopulating(boolean busy);
//	
//	/*
//	 * Returns if the is busy populating
//	 */
//	public boolean getPopulating();
}
