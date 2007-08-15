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

import java.awt.Frame;
import java.awt.Point;
import java.awt.dnd.DropTargetDropEvent;
import java.util.List;
import javax.swing.JToolBar;

import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphManager;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.util.TSObject;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IBroadcastAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.ICoreRelationshipDiscovery;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidation;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramValidationResult;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphicExportDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.ui.controls.drawingarea.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETCompartment;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETLabel;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;

public interface IDrawingAreaControl {
    public static int SOK_MOVEFORWARD = 0;
    public static int SOK_MOVETOFRONT = 1;
    public static int SOK_MOVEBACKWARD = 2;
    public static int SOK_MOVETOBACK = 3;
    
    public void onMoveForward() ;
    
    public void onMoveToFront() ;
    
    public void onMoveBackward() ;
    
    public void onMoveToBack() ;
    
    /**
     * Returns the IDiagram that represents this ActiveX control
     */
    public IDiagram getDiagram();
    
    /**
     * Is this IDiagram representing this IAxDrawingAreaControl?
     */
    public boolean isSame( IDiagram pDiagram );
    
    /**
     * Handles keystrokes, including VK_DELETE which does deletions from the GET.
     */
    public boolean handleKeyDown( int nKeyCode, int nShift, boolean bAskUserAboutDelete );
    
    /**
     * Returns the DiagramEngine that controls this diagram
     */
    public IDiagramEngine getDiagramEngine();
    
    /**
     * Returns the IAxTrackBar, if this diagram has one
     */
    public Object getTrackBar();
    
    /**
     * Returns the IProxyDiagram that represents this ActiveX control
     */
    public IProxyDiagram getProxyDiagram();
    
    /**
     * Save this diagram.
     */
    public void save();
    
    /**
     * Allows the drawing area control to perform some cleanup before the diagram
     * is actually closed.
     */
    public void preClose();
    
    /**
     * Is this diagram readonly?
     */
    public boolean getReadOnly();
    
    /**
     * Is this diagram readonly?
     */
    public void setReadOnly( boolean value );
    
    /**
     * Saves the diagram as a BMP, EMF or JPG file.  Use pMap to get the details of the graphic.
     */
    public boolean saveAsGraphic( String sFilename, /* SaveAsGraphicKind */ int nKind );
    
    /**
     * Saves the diagram as a BMP, EMF or JPG file.
     */
    public IGraphicExportDetails saveAsGraphic2( String sFilename, /* SaveAsGraphicKind */ int nKind );
    
    public IGraphicExportDetails saveAsGraphic2( String sFilename, /* SaveAsGraphicKind */ int nKind , double scale);
    
    /**
     * Get the filename that this view is saved to
     */
    public String getFilename();
    
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
    
    /**
     * Sets / Gets the name or alias of this element.
     */
    public void setNameWithAlias( String value );
    
    /**
     * Retrieves the fully qualified name of the element. This will be in the form 'A::B::C'.
     */
    public String getQualifiedName();
    
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
    
    /**
     * Sets / Gets the documentation for this diagram.
     */
    public String getDocumentation();
    
    /**
     * Sets / Gets the documentation for this diagram.
     */
    public void setDocumentation( String value );
    
    /**
     * Sets the ModelElement member responsible for hooking up a newly created node/edge to the meta data
     */
    public IElement getModelElement();
    
    /**
     * Sets the ModelElement member responsible for hooking up a newly created node/edge to the meta data
     */
    public void setModelElement( IElement value );
    
    /**
     * Gets the drawing area toplevel project
     */
    public IProject getProject();
    
    /**
     * Load this diagram from a .etl file.
     */
    public int load( String sFilename );
    
    /**
     * Set the view description.  This will determine the type of node that gets created.
     */
    public String getNodeDescription();
    
    /**
     * Set the view description.  This will determine the type of node that gets created.
     */
    public void setNodeDescription( String value );
    
//	/**
//	 * Changes the node description without changing the current tool.
//	*/
//	public void changeNodeDescription( String newVal );
    
    /**
     * Set the view description for edges.  This will determine the type of node that gets created.
     */
    public String getEdgeDescription();
    
    /**
     * Set the view description for edges.  This will determine the type of node that gets created.
     */
    public void setEdgeDescription( String value );
    
    /**
     * Changes the edge description without changing the current tool.
     */
    public void changeEdgeDescription( String newVal );
    
    /**
     * Puts the drawing area into a certain mouse mode
     */
    public void enterMode( /* DrawingToolKind */ int nDrawingToolKind );
    
    /**
     * Get/Set the locked state for the current mode.
     */
    public boolean getModeLocked();
    
    /**
     * Get/Set the locked state for the current mode.
     */
    public void setModeLocked( boolean value );
    
    /**
     * Used to set sticky buttons.  The user of the diagram can set/get this to determine when to lock/unlock the current tool.
     */
    public int getLastSelectedButton();
    
    /**
     * Used to set sticky buttons.  The user of the diagram can set/get this to determine when to lock/unlock the current tool.
     */
    public void setLastSelectedButton( int value );
    
    /**
     * Puts the drawing area into a specific mode based on the sButtonID.  sButtonID is a key in the PresentationTypes.etc file.
     */
    public void enterModeFromButton( String sButtonID );
    
    /**
     * Set/Get the current layout style.
     */
    public void setLayoutStyle( /* LayoutKind */ int value );
    
    /**
     * Set/Get the current layout style.
     */
    public int getLayoutStyle();
    
    /**
     * Set/Get the current layout style - this one does the layout without asking the user if its ok.
     */
    public void setLayoutStyleSilently( /* LayoutKind */ int value );
    
    /**
     * Use a delayed action to perform the layout style, possibly ignoring containment
     */
    public void delayedLayoutStyle( /* LayoutKind */ int nLayoutStyle, boolean bIgnoreContainment );
    
    /**
     * Immediately sets the layout style.  It bypasses the delayed actions.
     */
    public void immediatelySetLayoutStyle( /* LayoutKind */ int nLayoutStyle, boolean bSilent );
    
    /**
     * Is the layout properties window open.
     */
    public boolean getIsLayoutPropertiesDialogOpen();
    
    /**
     * Show/Close the layout property window.
     */
    public void layoutPropertiesDialog( boolean bShow );
    
    /**
     * Is the graph preferences window open.
     */
    public boolean getIsGraphPreferencesDialogOpen();
    
    /**
     * Show/Close the graph preferences window.
     */
    public void graphPreferencesDialog( boolean bShow );
    
    /**
     * Is the overview window open.
     */
    public boolean getIsOverviewWindowOpen();
    
    /**
     * Show/Hide the overview window.
     */
    public void overviewWindow( boolean bShowIt );
    
    /**
     * Returns the window rect of the overview window, if it is open.
     */
    public IETRect getOverviewWindowRect();
    
    /**
     * Sets the window rect of the overview window, if it is open.
     */
    public void setOverviewWindowRect( int nLeft, int nTop, int nWidth, int nHeight );
    
    /**
     * Show/Close the image dialog..
     */
    public void showImageDialog();
    
    /**
     * Print preview this window
     */
    public void printPreview( String sTitle, boolean bCanMoveParent );
    
    /**
     * During draw the edges/links override the zoom - this happens during print preview and the overview window.
     */
    public void setOnDrawZoom( double nOnDrawZoom );
    
    /**
     * Tells the drawing area that on draw has ended.
     */
    public void endOnDrawZoom();
    
    /**
     * Shows the print setup dialog
     */
    public void loadPrintSetupDialog();
    
    /**
     * Prints this control
     */
    public void printGraph( boolean bShowDialog );
    
    /**
     * Is the diagram currently undergoing layout.
     */
    public boolean getLayoutRunning();
    
    /**
     * What is the current zoom of the diagram.
     */
    public double getCurrentZoom();
    
    /**
     * Get the extreme values for the zoom.
     */
    public ETPairT<Double, Double> getExtremeZoomValues();
    
    /**
     * Zoom the diagram.
     */
    public void zoom( double nScaleFactor );
    
    /**
     * Zoom in.
     */
    public void zoomIn();
    
    /**
     * Zoom out.
     */
    public void zoomOut();
    
    /**
     * Fit the current diagram to the window.
     */
    public void fitInWindow();
    
    /**
     * Go to the parent graph
     */
    public void goToParentGraph();
    
    /**
     * Do we have a parent graph?
     */
    public boolean getHasParentGraph();
    
    /**
     * Bring up the custom zoom dialog
     */
    public void onCustomZoom();
    
    /**
     * Does this diagram have selected nodes?
     */
    public boolean getHasSelected( boolean bDeep );
    
    /**
     * Does this diagram have selected edges?
     */
    public boolean getHasSelectedEdges( boolean bDeep );
    
    /**
     * Does this diagram have selected labels?
     */
    public boolean getHasSelectedLabels( boolean bDeep );
    
    /**
     * Does this diagram have selected nodes?
     */
    public boolean getHasSelectedNodes( boolean bDeep );
    
    /**
     * Get the raw TS graph editor
     */
    //public TSGraphEditor getGraphEditor();
    
    /**
     * Go to the child graph
     */
    public void goToChildGraph( TSNode pNode );
    
    /**
     * Go to the child graph
     */
    public void deleteChildGraph( TSGraphObject pGraphObject );
    
    /**
     * Do we have a child graph?
     */
    public boolean getHasChildGraph( TSNode pNode );
    
    /**
     * Unfolds the specified node
     */
    public void unfoldNode( TSNode pCurrentNode );
    
    /**
     * Hide/Show the grid
     */
    public boolean getShowGrid();
    
    /**
     * Hide/Show the grid
     */
    public void setShowGrid( boolean value );
    
    /**
     * Sets/Gets the current grid size
     */
    public int getGridSize();
    
    /**
     * Sets/Gets the current grid size
     */
    public void setGridSize( int value );
    
    /**
     * Sets/Gets the type of grid this is being displayed
     */
    public int getGridType();
    
    /**
     * Sets/Gets the type of grid this is being displayed
     */
    public void setGridType( /* GridKind */ int value );
    
    /**
     * Invalidates the drawing area
     */
    public void refresh( boolean bPostMessage );
    
    /**
     * Invalidates a portion of the drawing area
     */
    public void refreshRect( IETRect pRefreshRect );
    
    /**
     * Posts an invalidate event on the drawing area
     */
    public void postInvalidate( IPresentationElement pPresentationElement );
    
    /**
     * Cut the selected objects
     */
    public void cut();
    
    /**
     * Copy the selected objects
     */
    public void copy();
    
    /**
     * Paste the selected objects
     */
    public void paste();
    
    /**
     * Do a cross diagram paste
     */
    public void crossDiagramPaste(java.awt.Point location);
    
    /**
     * Is this element type allowed on this diagram?
     */
    public boolean isAllowedOnDiagram( IElement pElement );
    
    /**
     * Clears the clipboard of selected objects
     */
    public void clearClipboard();
    
    /**
     * Deletes the selected objects
     */
    public void deleteSelected( boolean bAskUser );
    
    /**
     * Are there items on the clipbaord
     */
    public boolean itemsOnClipboard();
    
    /**
     * Select all nodes,labels and edges
     */
    public void selectAll( boolean bSelect );
    
    /**
     * Select all similar presentation elements
     */
    public void selectAllSimilar();
    
    /**
     * Does this graph have nodes?
     */
    public long hasNodes( boolean bHasNodes );
    
    /**
     * Does this graph have labels?
     */
    public long hasLabels( boolean bHasLabels );
    
    /**
     * Does this graph have edges?
     */
    public long hasEdges( boolean bHasEdges );
    
    /**
     * Does this graph have edges, labels or nodes?
     */
    public long hasGraphObjects( boolean bHasObjects );
    
    /**
     * Get/Set the type of this drawing.
     */
    public int getDiagramKind();
    
    /**
     * Get/Set the type of this drawing.
     */
    public void setDiagramKind( /* DiagramKind */ int value );
    
    /**
     * Get/Set the type of this drawing.
     */
    public String getDiagramKind2();
    
    /**
     * Get/Set the type of this drawing.
     */
    public void setDiagramKind2( String value );
    
    /**
     * Initialize a newly created diagram.  This adds the diagram to the current IWorkspace.
     */
    public void initializeNewDiagram( INamespace pNamespace, String sName, /* DiagramKind */ int pKind );
    
    /**
     * Alerts the nodes attached to this model element of changes.
     */
    public void elementModified( INotificationTargets pTargets );
    
    /**
     * Alerts the nodes attached to this model element of changes.
     */
    public void elementDeleted( INotificationTargets pTargets );
    
    /**
     * Alerts the nodes attached to this model element of the transformation.
     */
    public void elementTransformed( IClassifier classifier );
    
    /**
     * Returns the HWND to the GET.
     */
    public int getWindowHandle();
    
    /**
     * Returns the HWND to the drawing area.
     */
    public int getDiagramWindowHandle();
    
    public ADGraphWindow getGraphWindow();
    
    /**
     * Inverts the selected objects.
     */
    public void invertSelection();
    
    /**
     * Returns a list of the selected items.
     */
    public ETList<IPresentationElement> getSelected();
    
    /**
     * Returns a list of the selected items (nodes and edges).
     */
    public ETList<IETGraphObject> getSelected2();
    
    /**
     * Returns a list of the selected items (nodes, edges and labels).
     */
    public ETList<IETGraphObject> getSelected3();
    
    /**
     * Returns a list of the selected items as IETElements.
     */
    public ETList<IElement> getSelected4();
    
    /**
     * Returns a list of the selected items that are of the indicated type (ie Class).
     */
    public ETList<IPresentationElement> getSelectedByType( String sType );
    
    /**
     * Returns a list of the selected labels.
     */
    public ETList<IETLabel> getSelectedLabels();
    
    /**
     * Sorts the nodes left to right.  Edges are ignored.
     */
    public ETList < IPresentationElement > sortNodesLeftToRight( ETList < IPresentationElement >  pUnsortedList );
    
    /**
     * Returns a list of all the node presentation elements that represent the IElement.
     */
    public ETList < IPresentationElement > getAllNodeItems( IElement pModelElement );
    
    /**
     * Returns a list of all the items.
     */
    public ETList<IPresentationElement> getAllItems();
    
    /**
     * Returns a list of all the items that represent the IElement.
     */
    public ETList<IPresentationElement> getAllItems2( IElement pModelElement );
    
    /**
     * Returns a list of all the model elements on the diagram.
     */
    public ETList<IElement> getAllItems3();
    
    /**
     * Returns a list of all the product elements on the diagram.
     */
    public ETList<IETGraphObject> getAllItems4();
    
    /**
     * Returns a list of all the product labels on the diagram.
     */
    public ETList<IETLabel> getAllItems5();
    
    /**
     * Returns a list of all the product graph objects on the diagram.
     */
    public ETList <IETGraphObject> getAllItems6();
    
    public ETList<IPresentationElement> getAllItems(String topLevelId, String meid);
    
    /**
     * Select all the objects on the diagram that are of the indicated type (ie Class)
     */
    public ETList < IPresentationElement > getAllByType( String sType );
    
    /**
     * Select all the objects on the diagram that are of the indicated type (ie Class)
     */
    public ETList < IElement > getAllElementsByType( String sType );
    
    /**
     * Select all the objects on the diagram that are of the indicated draw engine type (ie ClassDrawEngine)
     */
    public ETList < IPresentationElement > getAllElementsByDrawEngineType( String sType );
    
    /**
     * Returns all the graph objects.
     */
    public ETList < TSObject > getAllGraphObjects();
    
    /**
     * Returns all the node presentation elements contained in or touching the input rectangle.
     */
    public ETList < IPresentationElement > getAllNodesViaRect( IETRect pRect, boolean bTouchingRect );
    
    /**
     * Returns all the edge presentation elements contained in or touching the input rectangle.
     */
    public ETList < IPresentationElement > getAllEdgesViaRect( IETRect pRect, boolean bTouchingRect );
    
    /**
     * Returns true if the model element is displayed in the diagram.
     */
    public boolean getIsDisplayed( IElement pModelElement );
    
    /**
     * Transforms a rect from logical coordinates to device coordinates.
     */
    public IETRect logicalToDeviceRect( IETRect pLogical );
    
    /**
     * Transforms a rect from logical coordinates to device coordinates.
     */
    public IETRect logicalToDeviceRect(double left, double top, double right, double bottom);
    
    /**
     * Transforms a point from logical coordinates to device coordinates.
     */
    public IETPoint logicalToDevicePoint( IETPoint pLogical );
    
    /**
     * Transforms a point from logical coordinates to device coordinates.
     */
    public IETPoint logicalToDevicePoint(double x, double y);
    
    /**
     * Transforms a rect from device coordinates to logical coordinates.
     */
    public IETRect deviceToLogicalRect( IETRect pDevice );
    
    /**
     * Transforms a rect from device coordinates to logical coordinates.
     */
    public IETRect deviceToLogicalRect(double left, double top, double width, double height);
    
    /**
     * Transforms a point from device coordinates to logical coordinates.
     */
    public IETPoint deviceToLogicalPoint( IETPoint pDevice );
    
    /**
     * Transforms a point from device coordinates to logical coordinates.
     */
    public IETPoint deviceToLogicalPoint(int x, int y);
    
    /**
     * Centers the drawing area on the presentation element.
     */
    public void centerPresentationElement( IPresentationElement pPresentationElement, boolean bSelectIt, boolean bDeselectAllOthers );
    
    /**
     * Centers the drawing area on the presentation element with this XMIID.
     */
    public void centerPresentationElement2( String sXMIID, boolean bSelectIt, boolean bDeselectAllOthers );
    
    /**
     * Does the stacking command nStackingCommand make sense?  Used for update of stacking order buttons.
     */
    public boolean isStackingCommandAllowed( int pStackingCommand);
    
    /**
     * Execute this stacking command.
     */
    public void executeStackingCommand( int pStackingCommand, boolean pRedraw );
    
    /**
     * Execute this stacking command on just this object.
     */
    public void executeStackingCommand( IPresentationElement pPresentationElement, int pStackingCommand, boolean pRedraw );
    
    /**
     * Execute this stacking command on this list of objects.
     */
    public void executeStackingCommand( ETList < IPresentationElement > pPresentationElements, int pStackingCommand, boolean pRedraw );
    
    /**
     * Executes the Relationship Disovery Command.
     */
    public void executeRelationshipDiscovery();
    
    /**
     * IsDirty is true when there is data that needs to be saved
     */
    public boolean getIsDirty();
    
    /**
     * IsDirty is true when there is data that needs to be saved
     */
    public void setIsDirty( boolean value );
    
    /**
     * Returns the presentation element on the drawing area control with the specified xml id
     */
    public IPresentationElement findPresentationElement( String sXMLID );
    
    /**
     * Creates a Tom Sawyer drawing tool
     */
    //public TSTool createTool( String sTool );
    
    /**
     * Sets / Gets the current Tom Sawyer drawing tool
     */
    //public TSTool getRootTool();
    
    /**
     * Sets / Gets the current Tom Sawyer drawing tool
     */
    //public void setRootTool( TSTool value );
    
    /**
     * Returns the presentation types mgr
     */
    public IPresentationTypesMgr getPresentationTypesMgr();
    
    /**
     * Get the current graph object at the mouse location
     */
    public TSGraphObject getGraphObjectAtMouse( int logicalX, int logicalY );
    
    /**
     * Are the tooltips enabled?
     */
    public boolean getAreTooltipsEnabled();
    
    /**
     * Enable/Disable tooltips.
     */
    public void setEnableTooltips( boolean bEnable );
    
    /**
     * Tells the product element to reinitialize the draw engine based on the init string.  Used for IInterface going from lollypop to class.
     */
    public void resetDrawEngine( IETGraphObject pETElement, String sNewInitString );
    
    /**
     * Tells the product element to reinitialize the draw engine.  The init string is pulled from the presentationtypesmgr
     */
    public void resetDrawEngine2( IETGraphObject pETElement );
    
    /**
     * Removes these presentation elements.
     */
    public void removeElements( ETList<IPresentationElement> pItemsToRemove );
    
    /**
     * Selects the graph object and fires the events.  Normal select routines don't fire the events.
     */
    public void selectAndFireEvents( TSGraphObject pGraphObject, boolean bSelect, boolean bDeselectAllOthers );
    
    /**
     * Posts the select event
     */
    public void postSelectEvent();
    
    /**
     * Validates the diagram.  If pDiagramValidation is null then a default will get created.  pResult may also be null if no results are wanted.
     */
    public IDiagramValidationResult validateDiagram( boolean bOnlySelectedElements, IDiagramValidation pDiagramValidation );
    
    /**
     * Sync the selected (or all) elements.
     */
    public void syncElements( boolean bOnlySelectedElements );
    
    /**
     * Causes the diagram to take focus.
     */
    public void setFocus();
    
    /**
     * Returns true if the diagram is the focus owner; false otherwise.
     */
    public boolean isFocused();
    
    /**
     * Try to reconnect the link from pOldNode to pNewNode.
     */
    public boolean reconnectLink( IPresentationElement pLink, IPresentationElement pOldNode, IPresentationElement pNewNode );
    
    /**
     * Transforms the IETElement.  Use this call to post the event if you happen to be on the IETElement that is to be transformed.
     */
    public void transform( IETGraphObject pETElement, String sToElement );
    
    /**
     * Posts a simple action type
     */
    public void postSimpleDelayedAction( /* _SimpleActionKind */ int nKind );
    
    /**
     * Posts a simple presentation action type
     */
    public void postSimplePresentationDelayedAction( IPresentationElement pPE, /* _SimplePresentationActionKind */ int nKind );
    
    /**
     * Posts a simple presentation action type
     */
    public void postSimplePresentationDelayedAction( ETList< IPresentationElement > pPEs, /* _SimplePresentationActionKind */ int nKind );
    
    /**
     * Posts a delayed action to the diagram.  Use when you may be in a dangerous callstack to perform the necessary action
     */
    public void postDelayedAction( IDelayedAction pAction );
    
    public void postAddObject(ITSGraphObject graphObj, boolean resize);
    
    /**
     * Receives notification of a broadcast.  Used by the IProxyDiagramManager to broadcast functions to all open views.
     */
    public void receiveBroadcast( IBroadcastAction pAction );
    
    /**
     * Resizes elements with custom dimensions.
     */
    public boolean resizeDimensions();
    
    /**
     * Align selected elements to the left position of the first selected element.
     */
    public boolean alignLeft();
    
    /**
     * Align selected elements horizontally to the center position of the first selected element.
     */
    public boolean alignHorizontalCenter();
    
    /**
     * Align selected elements to the right position of the first selected element.
     */
    public boolean alignRight();
    
    /**
     * Align selected elements to the top position of the first selected element.
     */
    public boolean alignTop();
    
    /**
     * Align selected elements vertically to the cener position of the first selected element.
     */
    public boolean alignVerticalCenter();
    
    /**
     * Align selected elements to the bottom position of the first selected element.
     */
    public boolean alignBottom();

    
// disabled - feature to be added with Meteora
//    /**
//     * Distribute selected elements horizontally based on the left edge position 
//     * of the left and right-most selected elements.
//     */
//    public boolean distributeLeftEdge();
//    
//    /**
//     * Distribute selected elements horizontally based on the center position 
//     * of the left and right-most selected elements.
//     */
//    public boolean distributeHorizontalCenter();
//    
//    /**
//     * Distribute selected elements horizontally based on the right edge position 
//     * of the left and right-most selected elements.
//     */
//    public boolean distributeRightEdge();
//    
//    /**
//     * Distribute selected elements vertically based on the top edge position 
//     * of the top and bottom-most selected elements.
//     */
//    public boolean distributeTopEdge();
//    
//    /**
//     * Distribute selected elements vertically based on the center position 
//     * of the top and bottom-most selected elements.
//     */
//    public boolean distributeVerticalCenter();
//    
//    /**
//     * Distribute selected elements vertically based on the bottom edge position 
//     * of the top and bottom-most selected elements.
//     */
//    public boolean distributeBottomEdge();
        
    /**
     * Resizes the elements (selected or all) to their contents.
     */
    public void sizeToContents( boolean bJustSelectedElements );
    
    /**
     * Gets the mid point of the edge.
     */
    public IETPoint getMidPoint(TSEdge pEdge);
    
    /**
     * Adds a node to the diagram.
     *
     * @param nodeInitString The initialization string for the node.
     * @param location The center location for the node.
     * @param bSelect Should we select this new graph object?
     * @param bDeselectAllOthers Should we deselect all other objects?
     * @param pElementToAssignToNode The element to assign the node to
     * @return The created node.  NULL if no node is created.
     */
    public ETNode addNode(IElement element, IETPoint pt) throws ETException;
    
    
    /**
     * Adds a node to the diagram.  The type of the node is specified by the
     * metatype name.
     *
     * @param metaDataType The type of node to create.
     * @param location The location of the new node.
     * @param bSelect <code>true</code> if the diagram is to be selected.
     * @param bDeselectAllOthers <code>true</code> if all selected nodes are to
     *                           be deselected.
     * @return The new node.
     */
    public ETNode addNodeForType(String metaDataType,
            IETPoint location,
            boolean  bSelect,
            boolean  bDeselectAllOthers )
            throws ETException;
    /**
     * Adds a node to the diagram.
     */
    public TSNode addNode( String   nodeInitString,
            IETPoint location,
            boolean  bSelect,
            boolean  bDeselectAllOthers)
            throws ETException;
    
    /**
     * Adds a node to the diagram and assigns that node to the incoming IElement.
     */
    public TSNode addNode( String   nodeInitString,
            IETPoint location,
            boolean  bSelect,
            boolean  bDeselectAllOthers,
            IElement pElementToAssignToNode)
            throws ETException;
    
    /**
     * Adds an edge to the diagram.  The type of the edge is specified by the
     * metatype name.
     *
     * @param metaType The type of node to create.
     * @param sourceNode The node that starts the edge.
     * @param targetNode The node that ends the edge.
     * @param bSelect <code>true</code> if the diagram is to be selected.
     * @param bDeselectAllOthers <code>true</code> if all selected nodes are to
     *                           be deselected.
     * @return The new node.
     */
    public ETEdge addEdgeForType(String metaType,
            ETNode sourceNode,
            ETNode targetNode,
            boolean bSelected,
            boolean bDeselectAllOthers) throws ETException;
    /**
     * Adds an edge to the diagram.
     */
    public TSEdge addEdge( String edgeInitString,
            TSNode  pSourceNode,
            TSNode  pTargetNode,
            boolean bSelect,
            boolean bDeselectAllOthers) throws ETException;
    
    /**
     * Adds an edge to the diagram.
     *
     * @param edgeInitString The initialization string for the edge.
     * @param pSourceNode The source for the edge.
     * @param pTargetNode The target for the edge.
     * @param bSelect Should we select this new graph object?
     * @param bDeselectAllOthers Should we deselect all other objects?
     * @param pElementToAssignToEdge The element to assign to the edge
     * @return The created edge.  <code>null</code> if no edge is created.
     */
    public TSEdge addEdge( String edgeInitString,
            TSNode  pSourceNode,
            TSNode  pTargetNode,
            boolean bSelect,
            boolean bDeselectAllOthers,
            IElement elementToAssignToEdge) throws ETException;
    
    /**
     * Posts a message to delete this IPresentationElement.
     */
    public void postDeletePresentationElement( IPresentationElement pPE );
    
    /**
     * Posts a message to delete this graph object.
     */
    public void postDeletePresentationElement( TSGraphObject pGraphObject );
    
    /**
     * Returns the current graph.
     */
    public TSEGraph getCurrentGraph();
    
    /**
     * Returns the current graph manager.
     */
    public TSGraphManager getCurrentGraphManager();
    
    /**
     * Begins dragging all selected elements on the graphObject, or all selected elements on all selected graphObjects if NULL.
     */
    public void beginOLEDrag( TSGraphObject graphObject );
    
    /**
     * Adds an item to the ETL read/write list.
     */
    public void addReadWriteItem( IETGraphObject pItem );
    
    /**
     * Unhide 'num' number of parent levels for the input node.  If -1 then we ask the user.
     */
    public void unhide( IPresentationElement pPE, int numLevels, boolean bChildren );
    
    /**
     * Hide 'num' number of children levels for the input node.  If -1 then we ask the user.
     */
    public void hide( IPresentationElement pPE, int numLevels, boolean bChildren );
    
    /**
     * Does this node have children?
     */
    public boolean hasChildren( IPresentationElement pPE, boolean bHidden );
    
    /**
     * Does this node have parents?
     */
    public boolean hasParents( IPresentationElement pPE, boolean bHidden );
    
    /**
     * Let folks know that tooltips are about to be displayed.
     */
    public void fireTooltipEvent( IPresentationElement pPE, IToolTipData pTooltip );
    
    /**
     * Begins edit on a specific label
     */
    public void postEditLabel( ILabelPresentation pPE );
    
    /**
     * Begins edit on a specific label
     */
    public void pumpMessages( boolean bJustDrawingMessages );
    
    /**
     * Fires an event out the interface saying that a context menu button has been selected.
     */
    public void fireDrawingAreaContextMenuSelected( IProductContextMenu contextMenu, IProductContextMenuItem selectedItem );
    
    /**
     * The drawing area can cache up preferences.  Call this to get a cached value (fast) or hit the preference manager if not cached(slower).
     */
    public String getPreferenceValue( String sPath, String sName );
    
    /**
     * One or more drawing preferences has been changed, update the diagram with the new preference values.
     */
    public boolean preferencesChanged( IPropertyElement[] pProperties );
    
    /**
     * Allows the diagram engines the ability to change the element being dropped or created
     */
    public IElement processOnDropElement( IElement pElementBeingDropped );
    
    /**
     * Sizes the presentation element, and its track bar car
     */
    public void sizeToContentsWithTrackBar( IPresentationElement pElement );
    
    /**
     * Adds a presentation element to the track bar
     */
    public void addPresentationElementToTrackBar( IPresentationElement pElement );
    
    /**
     * Returns the relationship discovery object that's appropriate for this diagram
     */
    public ICoreRelationshipDiscovery getRelationshipDiscovery();
    
    /**
     * Kills the tooltip if it happens to be up.  Used during the edit control.
     */
    public void killTooltip();
    
    /**
     * Begin edit context
     */
    public void beginEditContext( ICompartment pCompartment );
    
    /**
     * End edit context
     */
    public void endEditContext();
    
    /**
     * Ask the user what to do about a name collision
     */
    public void questionUserAboutNameCollision( ICompartment pCompartmentBeingEdited, INamedElement pElement, String sProposedName, ETList<INamedElement> pCollidingElements, IResultCell pCell );
    
    /**
     * Forces a deep sync on these elements
     */
    public void handleDeepSyncBroadcast( IElement[] pElements, boolean bSizeToContents );
    
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
    
    /**
     * This method just fires the selection notification it doesn't change the state of the graph object.
     */
    public void fireSelectEvent(TSGraphObject pGraphObject);
    
    /**
     * This method just fires the deslection notification it doesn't change the state of the graph object.
     */
    public void fireUnselectEvent(TSGraphObject pGraphObject);
    
    public Frame getOwnerFrame();
    
    
    public void onGraphEvent( int pGraphEventKind, IETPoint pStartPoint,  IETPoint pEndPoint, ETList <IETGraphObject> affectedObjects );
    
    public void setEditCompartment(ETCompartment editCtrl);
    
    public IProductArchive getProductArchive();
    public ADDrawingAreaResourceBundle getResources();
    
    
    public boolean isAutoFitInWindow();
    public void switchToDefaultState();
    public void onInteractiveObjCreated(TSEObjectUI ui);
    public void onDrop(DropTargetDropEvent event, TSEObject graphObject);
    public void fireSelectEvent(List pSelectedGraphObjs);
    public IDrawingAreaEventDispatcher getDrawingAreaDispatcher();
    
    public JToolBar getToolbar();
    public void setShowDefaultToolbar(boolean bShow);
    public void updateSecondaryWindows();
    
        /*
         * Set when the diagrma is creating itself from selected elements.
         */
    public void setPopulating(boolean busy);
    
        /*
         * Returns if the is busy populating
         */
    public boolean getPopulating();
    
    /** Adds listener to listen when drawing area changes to 'select' state. */
    public void addDrawingAreaToolSelectionSink(IDrawingAreaSelectStateEnteredSink drawingAreaSelectStateEnteredListener);
    
    /*
     * Allows the palette code to set users' selected button on the palette
     */
    public void setSelectedPaletteButton(String buttonId);
    
        /*
         * Returns the selected button on the palette
         */
    public String getSelectedPaletteButton();
}
