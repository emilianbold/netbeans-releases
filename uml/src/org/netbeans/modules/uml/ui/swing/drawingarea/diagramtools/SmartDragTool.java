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


// Workfile. SmartDragTool.java
// Revision. 1
//   Author. treys
//     Date. Feb 3, 2004 2:28:59 PM
//  Modtime. Feb 3, 2004 2:28:59 PM

package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteractionOperand;
import org.netbeans.modules.uml.core.support.Debug;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADInteractionOperandCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.IADLifelineCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.LifelineConnectorLocation;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.lifelinepieces.LifelinePiece;
import org.netbeans.modules.uml.ui.products.ad.diagramengines.IADSequenceDiagEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.StretchContext;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.StretchContextType;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETHorzDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETHorzNoDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETVertDragCursor;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETVertNoDragCursor;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.drawing.TSDNode;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraph;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.tool.TSEMoveSelectedTool;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.graph.TSNode;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.util.TSObject;
import com.tomsawyer.drawing.geometry.TSPoint;
import com.tomsawyer.drawing.geometry.TSRect;

public class SmartDragTool extends TSEMoveSelectedTool {
	public static class DR // DragRestriction
        {
            /** No drag restrictions are placed to the drag operation. */
            public final static int NONE = 0;
            
            /** Only allow the user to move a graph object vertically. */
            public final static int VERTICAL_MOVE_ONLY = 1;
            
            /** Only allow the user to move a graph object horizontally. */
            public final static int HORIZONTAL_MOVE_ONLY = 2;
        }

	private final static boolean PREFERENCE_JUST_DRAG_MESSAGE = false;

	private boolean mDeep = false;
	private IDrawingAreaControl mDrawingAreaControl = null;
	private IStretchContext mStretchContext = null;
	private IETRect mRectRestrictedArea = null;
	private TSConstPoint m_LastXYPos = null;
	private TSEdge mDraggingEdge = null;
	private ETList < TSGraphObject > mResizeTSGraphObjects = new ETArrayList < TSGraphObject > ();
	private ETList < ConnectorInfo > mDraggingConnectors = new ETArrayList < ConnectorInfo > ();
	private ETList < TSGraphObject > mDraggingTSGraphObjects = new ETArrayList < TSGraphObject > ();
	private LifelinePiece mPiece = null;
	private boolean mDragPieceTop = false;
	private TSEGraph mGraph = null;
	private IADInteractionOperandCompartment m_OperandCompartment = null;
	private int m_DragRestriction = DR.NONE;
	private TSConstPoint mPreviousMouseMove = null;
	private boolean mDragAffectsSiblings = true;

	/**
	 * This constructor creates a new state for moving all selected objects in 
	 * the graph.
	 * 
	 * @param startPoint The location at which the move began.
	 * @param graph The graph that use to execute the tool.
	 * @param deep Whether or not to do a deep move.
	 */
	public SmartDragTool(TSConstPoint startPoint,IDrawingAreaControl graph,	boolean deep) {
		super(graph != null ? graph.getGraphWindow().getCurrentTool() : null, startPoint, false);
		
		mDrawingAreaControl = graph;
		setDeep(deep);
	}

	/**
	 * Sets the objects that will be resized by the operation.
	 */
	public void setResizeGraphObjects(ETList < TSGraphObject > objects) {
		//m_ResizeTSGraphObjects = objects;
		throw new RuntimeException("Method Not Implemented Yet.  Waiting for the AccordionTool.");
	}

	/**
	 * Adds a connector to be updated while the tool is dragging
	 * @param connector Tom Sawyer connector to be added to our internal list
	 * @param dragConnectorsGraphObject Flag indicating that the graphic object 
	 *                                  should be saved to the list of dragging 
	 *                                  objects
	 * @param verticalOffset
	 */
	public void addDraggingConnector(
		TSConnector connector,
		boolean dragConnectorsGraphObject,
		long verticalOffset) {
		if (connector != null) {
			ConnectorInfo info = new ConnectorInfo(connector, verticalOffset);

			mDraggingConnectors.add(info);

			// Add the connector's node to the vector of dragging Tom Sawyer graph objects
			TSGraphObject graphObject = connector.getOwner();

			addDraggingTSGraphObject(graphObject);
		}
	}

	//**************************************************
	// TSEMoveSelectedState overrides
	//**************************************************

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMousePressed(java.awt.event.MouseEvent)
	 */
	public void onMousePressed(MouseEvent e) {
		TSPoint pt = new TSPoint(getAlignedWorldPoint(e));
		//setPreviousMouseMovePoint(pt);
		setLastXYPos(pt);

		IStretchContext context = new StretchContext();
		setStretchContext(context);

		IETPoint tempPoint = new ETPointEx(getStartPoint());
		context.setStartPoint(tempPoint);
		context.setFinishPoint(tempPoint);

		// Need to ask the nodes if there should be any restrictions of movement 
		// during the stretch.  Therefore, we prepare an un-restricted rectangle 
		// and pass that into the start of the stretch.  The nodes can then modify
		// the stretch restrictions.
		ETRect rectRestrictedArea =
			new ETRect(
				Long.MIN_VALUE,
				Long.MAX_VALUE,
				Long.MAX_VALUE,
				Long.MIN_VALUE);

		context.setRestrictedArea(rectRestrictedArea);

		informResizeDrawEnginesAboutStretch();

		IETRect rect = context.getRestrictedArea();
		IETRect startingRect =
			new ETRect(
				Long.MIN_VALUE,
				Long.MAX_VALUE,
				Long.MAX_VALUE,
				Long.MIN_VALUE);

		if (rectRestrictedArea.equals(startingRect) == true) {
			// No restrictions, so make sure our member variable is empty
			// Fix W2941: Any restrictions should have been set via put_RestrictedArea()
		} else {
			setRestrictedArea(rectRestrictedArea);
		}

		setDragAffectsSiblings(e.isControlDown());
	}

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowInputState#onMouseReleased(java.awt.event.MouseEvent)
	 */
   public void onMouseReleased( MouseEvent e )
   {
      // Fix J2531:  We don't want to process this event unless it was the
      //             left mouse that was released
      if( e.getButton() != MouseEvent.BUTTON1 )
      {
         return;
      }
      
      // We should never have a NULL drawing area control.  Just a sanity check.
      IDrawingAreaControl ctrl = getDrawingAreaControl();
      if (ctrl != null)
      {
         // I (?) want to trick the super class to act as if the mouse was released
         // at the restricted point.  The super class is expecting the point
         // to be in device coordinates so first translate the restricted point
         // to device coordinates.

         TSConstPoint ptLastXYPos = restrictPoint( getLastXYPos() );
         IETPoint localXY = ctrl.logicalToDevicePoint(new ETPointEx(ptLastXYPos));
         
         // We want to restrict the location of the event, but not change the event itself
         MouseEvent newEvent = e;
         if( !ptLastXYPos.equals( getLastXYPos() ))
         {
            newEvent = new MouseEvent( e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(),
                                       localXY.getX(), localXY.getY(),
                                       e.getClickCount(), e.isPopupTrigger(), e.getButton() );
         }
         super.onMouseReleased( newEvent );

         ETList < IETGraphObject > graphObjects = ctrl.getSelected3();
         long count = 0;

         if (graphObjects != null)
         {
            for (Iterator < IETGraphObject > iter = graphObjects.iterator(); iter.hasNext();)
            {
               IETGraphObject graphObject = iter.next();

               if (graphObject != null)
               {
                  graphObject.onGraphEvent(IGraphEventKind.GEK_POST_SMARTDRAW_MOVE);
               }
            }
         }

         updateResizeNodes(StretchContextType.SCT_FINISH, ptLastXYPos);
         sendDraggingGraphObjectsMoveEvent();
         updateAssociatedCombinedFragment();

         getGraphWindow().invalidate();
         endCurrentOperation();

         //         finalizeState();

         // Fix J961:  This call ensures that the trackbar gets updated properly.
         //            In C++ the TS event mechanism is much more robust.  We have to
         //            simulate that event machanism by sending this post move event from the tool.
         // Send event to drawing area control
         if( graphObjects != null )
         {
            ctrl.onGraphEvent(IGraphEventKind.GEK_POST_MOVE, new ETPointEx(getStartPoint()), localXY, graphObjects);
         }
      }
   }

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowState#init()
	 */
	protected void init() {
		// Most of the C++ code was a copy of the Tom Sawyers implementation
		// of the move tool ( I do not know where they got the code).  I do
		// not think we can rely on the supers implementation of this tool.  
		super.init();

		setLastXYPos(getStartPoint());
	}

	/* (non-Javadoc)
         * @see com.tomsawyer.editor.TSEWindowInputState#onMouseDragged(java.awt.event.MouseEvent)
         */
        public void onMouseDragged(MouseEvent e)
        {
            TSConstPoint mouseLogicalPoint = getAlignedWorldPoint(e);
//            TSConstPoint mouseLogicalPoint = new TSConstPoint(e.getX(), e.getY());
            
            // We should never have a NULL drawing area control.  Just a sanity check.
            IDrawingAreaControl ctrl = getDrawingAreaControl();
            if (ctrl != null)
            {
                
                // I want to trick the super class to act as if the mouse was released
                // at the restricted point.  The super class is expecting the point
                // to be in device coordinates so first translate the restricted point
                // to device coordinates.
                TSConstPoint logicalPoint = restrictPoint(mouseLogicalPoint);
                boolean cancelMouseMove = logicalPoint.equals(mPreviousMouseMove);
                mPreviousMouseMove = logicalPoint;
                
                if (cancelMouseMove == false)
                {
                    // We want to restrict the location of the event, but not change the event itself
                    IETPoint localXY = ctrl.logicalToDevicePoint(new ETPointEx(logicalPoint));
                    MouseEvent newEvent = new MouseEvent(e.getComponent(),e.getID(),e.getWhen(),e.getModifiers(),
                            localXY.getX(),localXY.getY(),e.getClickCount(),e.isPopupTrigger(),e.getButton());
                    
                    updateDraggingGraphObjects(logicalPoint);
                    updateResizeNodes(StretchContextType.SCT_STRETCHING,logicalPoint);
                    
                    super.onMouseDragged(newEvent);
                    
                    ctrl.refresh(true);
                    
                    setLastXYPos(mouseLogicalPoint);
                }
            }
        }

	/* (non-Javadoc)
	 * @see com.tomsawyer.editor.TSEWindowState#cancelAction()
	 */
	public void cancelAction() {
		super.cancelAction();
		endCurrentOperation();
	}

	//**************************************************
	// Helper Methods
	//**************************************************

	protected TSPoint restrictPoint(final TSConstPoint tsPoint) {
		TSPoint tsptLogical = tsPoint != null ? new TSPoint(tsPoint) : new TSPoint(0,0);

		TSConstPoint startPoint = getStartPoint();
		Cursor resource = null;
		Cursor invalidResource = null;

		switch (getDragRestrictionType()) {
			case DR.NONE :
				// do nothing
				break;

			case DR.VERTICAL_MOVE_ONLY :
				tsptLogical.setX(startPoint.getX());
				resource = ETHorzDragCursor.getCursor();
				invalidResource = ETHorzNoDragCursor.getCursor();
				break;

			case DR.HORIZONTAL_MOVE_ONLY :
				tsptLogical.setY(startPoint.getY());
				resource = ETVertDragCursor.getCursor();
				invalidResource = ETVertNoDragCursor.getCursor();
				break;

			default :
				Debug.assertFalse(false, "did we add another restriction");
				break;
		}

		IETRect restrictedArea = getRestrictedArea();
		// Make sure the point is restricted to the restriced area, if necessary
		if ((restrictedArea != null)
			&& (restrictedArea.contains((int) tsptLogical.getX(), (int) tsptLogical.getY())
				== false)) {
			if (tsptLogical.getX() < restrictedArea.getLeft()) {
				tsptLogical.setX(restrictedArea.getLeft());
				resource = invalidResource;
			}
			if (tsptLogical.getX() > restrictedArea.getRight()) {
				tsptLogical.setX(restrictedArea.getRight());
				resource = invalidResource;
			}
			if (tsptLogical.getY() > restrictedArea.getTop()) {
				tsptLogical.setY(restrictedArea.getTop());
				resource = invalidResource;
			}
			if (tsptLogical.getY() < restrictedArea.getBottom()) {
				tsptLogical.setY(restrictedArea.getBottom());
				resource = invalidResource;
			}
		}

		if (getDraggingEdge() != null && resource != null) {
			setCursor(resource);
		}

		return tsptLogical;
	}

	/**
	 * Ends the current operation by clearing our vectors and whacking the move 
	 * control as necessary
	 */
	protected void endCurrentOperation() {
		setRestrictedArea(new ETRect(0, 0, 0, 0));

		// Fix W2875:  This cleans up the dirty pointer problem.
		setDraggingEdge(null);

		mResizeTSGraphObjects.clear();
		mDraggingConnectors.clear();
		mDraggingTSGraphObjects.clear();

		setPiece(null);
		setDragPieceTop(true);
	}

	/**
	 * @param i
	 * @param lastXY
	 */
	protected void updateResizeNodes(int contextType, TSConstPoint lastXY) {
		IStretchContext context = getStretchContext();
		if (context != null) {
			context.setType(contextType);
			context.setFinishPoint(new ETPointEx(lastXY));
		}

		informResizeDrawEnginesAboutStretch();
	}

	/**
	 * Tell the \a m_vecResizeTSGraphObjects that their node has been stretched, 
	 * using the member stretch context
	 */
	protected void informResizeDrawEnginesAboutStretch() {
		IStretchContext context = getStretchContext();
		for (Iterator < TSGraphObject > iter = mResizeTSGraphObjects.iterator();
			iter.hasNext();
			) {
			TSGraphObject object = iter.next();

			if (object != null) {
				IDrawEngine engine = TypeConversions.getDrawEngine(object);
				if (engine instanceof INodeDrawEngine) {
					INodeDrawEngine nodeEngine = (INodeDrawEngine) engine;
					nodeEngine.stretch(context);
				}
			}
		}
	}

	/**
	 * 
	 */
	protected void sendDraggingGraphObjectsMoveEvent() {
		for (Iterator < TSGraphObject > iter =
			mDraggingTSGraphObjects.iterator();
			iter.hasNext();
			) {
			TSGraphObject object = iter.next();
			if (object != null) {
				IDrawEngine engine = TypeConversions.getDrawEngine(object);
				if (engine != null) {
					engine.onGraphEvent(
						IGraphEventKind.GEK_POST_SMARTDRAW_MOVE);
				}
			}
		}
	}

	/**
	 * @param logicalPoint
	 */
	protected void updateDraggingGraphObjects(TSConstPoint logicalPoint) {
		double lLogicalY = logicalPoint.getY();
		updateConnectors((long) lLogicalY);
		updatePiece((int) lLogicalY);

		if (getGraphWindow() != null) {
			getGraphWindow().invalidate();
		}
	}

	/**
	 * Moves connectors within their compartments, using m_vecDraggingConnectors
	 * @param lLogicalY Current vertical mouse location in Tom Sawyer draw 
	 *                  area logical coordinates
	 */
	protected void updateConnectors(long lLogicalY) {
		try {
			for (Iterator < ConnectorInfo > iter = mDraggingConnectors.iterator(); iter.hasNext(); ) {
				ConnectorInfo connector = iter.next();
				connector.moveConnector(lLogicalY);
			}
		} catch (Throwable e) {
			// UPDATE figure out why the list went bad
			// our list went bad
			mDraggingConnectors.clear();
		}
	}

	/**
	 * Moves the lifeline piece, for message-to-self
	 * @param lLogicalY [in] Current vertical mouse location in Tom Sawyer draw area logical coordinates
	 */
	protected void updatePiece(final int lLogicalY) {
		LifelinePiece piece = getPiece();
		if (piece != null) {
			if (isDragPieceTop() == true) {
				piece.setLogicalTop(lLogicalY);
			} else {
				piece.setLogicalBottom(lLogicalY);
			}
		}
	}

	/**
	 * Make the all the graph objects visible/not visible
	 * @param bVisible [in] Flag indicating to set the graph object visble (true) or not visble (false)
	 */
	protected void showAllTSGraphObjects() {
		showAllTSGraphObjects(true);
	}

	/**
	 * Make the all the graph objects visible/not visible
	 * @param bVisible [in] Flag indicating to set the graph object visble (true) or not visble (false)
	 */
	protected void showAllTSGraphObjects(boolean bVisible) {
		showAllTSGraphObjects(mResizeTSGraphObjects, bVisible);
		showAllTSGraphObjects(mDraggingTSGraphObjects, bVisible);
	}

	/**
	 * Make all the graph objects visible/not visible
	 * @param rvecTSGraphObjects [in] The vector of Tom Sawyer graph objects being show/hidden
	 * @param bVisible [in] Flag indicating to set the graph object visble (true) or not visble (false)
	 */
	protected void showAllTSGraphObjects(
		ETList < TSGraphObject > vecTSGraphObjects,
		boolean bVisible) {
		final boolean vbVisible = bVisible;

		for (Iterator < TSGraphObject > iter = vecTSGraphObjects.iterator();
			iter.hasNext();
			) {
			TSGraphObject object = iter.next();
			if (object instanceof TSNode) {
				TSNode node = (TSNode) object;
				node.setVisible(vbVisible);
			} else if (object instanceof TSEdge) {
				TSEdge edge = (TSEdge) object;
				edge.setVisible(vbVisible);
			}
		}
	}

	/**
	 * Invalidates all the graph objects' bounding rectangles
	 */
	protected void invalidateAllTSGraphObjects() {
		invalidateAllTSGraphObjects(mResizeTSGraphObjects);
		invalidateAllTSGraphObjects(mDraggingTSGraphObjects);
	}

	/**
	 * Invalidates the specified vector of graph objects' bounding rectangles
	 */
	protected void invalidateAllTSGraphObjects(
		ETList < TSGraphObject > vecTSGraphObjects) {
		for (Iterator < TSGraphObject > iter = vecTSGraphObjects.iterator();
			iter.hasNext();
			) {
			IDrawEngine engine = TypeConversions.getDrawEngine(iter.next());
			if (engine != null) {
				engine.invalidate();
			}
		}
	}

	/**
	 * Calculates the rectangle that encompasses all the graph objects' veiw bounds rectangles
	 */
	protected TSRect calculateTSGraphObjectsRect() {
		TSRect retVal = new TSRect(0, 0, 0, 0);

		retVal.merge(GetHelper.calculateTSGraphObjectsRect(mResizeTSGraphObjects));
		retVal.merge(GetHelper.calculateTSGraphObjectsRect(mDraggingTSGraphObjects));

		return retVal;
	}

	/**
	 * Adds all TS graph objects that can be converted to the specified interface
	 * to the dragging list
	 * @param graphObjects List of Tom Sawyer graph objects
	 * @param type The type for the interface that the TS graph object
	 *             must support to be added to the list.
	 */
	protected void addGraphObjectsToDraggingList(
		ETList < TSGraphObject > graphObjects,
		Class type) {
		if (graphObjects != null) {
			for (Iterator < TSGraphObject > iter = graphObjects.iterator();
				iter.hasNext();
				) {
				TSGraphObject object = iter.next();

				if (type.isInstance(object) == true) {
					addDraggingTSGraphObject(object);
				}
			}
		}
	}

	/**
	 * Adds a specific graph object to the list of graph objects that get updated as movement occurs.
	 */
	public void addDraggingTSGraphObject(TSGraphObject graphObject) {
		addDraggingTSGraphObject(graphObject, false);
	}

	/**
	 * Adds a specific graph object to the list of graph objects that get updated as movement occurs.
	 */
	public void addDraggingTSGraphObject(
		TSGraphObject graphObject,
		boolean setInvisible) {
		if ((graphObject != null) && (mDraggingEdge != null)) {
			boolean addGraphObject = true;

			// Check to see if the graph object is already in the list
			for (Iterator < TSGraphObject > iter = mDraggingTSGraphObjects.iterator(); iter.hasNext();) {
				TSGraphObject object = iter.next();
				if (graphObject.equals(object) == true) {
					addGraphObject = false;
					break;
				}
			}

			if (addGraphObject) {
				mDraggingTSGraphObjects.add(graphObject);

				if (setInvisible == true) {
					if (graphObject instanceof TSNode) {
						TSNode node = (TSNode) graphObject;
						node.setVisible(false);
					} else if (graphObject instanceof TSEdge) {
						TSEdge edge = (TSEdge) graphObject;
						if (edge != null) {
							edge.setVisible(false);
						}
					}

					//               TSRect invalidRect = getGraph().getSelectedObjectsBounds();
					//            
					////               invalidRect |= this->initialInvalidRect();
					//
					//               // Add all the draw engine nodes' rectangles
					//               invalidRect.merge(calculateTSGraphObjectsRect());
					//
					//               // ####PM Quick hack, Inflate the rectangle by 5
					//               invalidRect.setBunds(invalidRect.getLeft() - 5,
					//                                    invalidRect.getTop() + 5,
					//                                    invalidRect.getRight() + 5,
					//                                    invalidRect.getBottom() - 5);
					//
					////               this->initialDrawDragged(&invalidRect);
				}
			}
		}
	}

	/** 
	 * Adds all TS graph objects that can be converted to the specified interface
	 * to the dragging list
	 */
	protected void updateAssociatedCombinedFragment() {
		IADInteractionOperandCompartment compartment = getOperandCompartment();
		if (compartment != null) {
			compartment.expandToIncludeCoveredItems();

			//_VH(m_pOperandCompartment - > Release());
			setOperandCompartment(null);
		}
	}

	/**
	 * Prepare for dragging a specific edge
	 */
	public void setDraggingEdge(TSEEdge edge, long lVerticalOffset) {
		if (edge == null) {
			throw new NullPointerException("The specified edge is null");
		}

		//m_bDragAffectsSiblings = (GetAsyncKeyState( VK_CONTROL ) != FALSE);
		setDragAffectsSiblings(false);

		final boolean bJustDragMessage =
			isDragAffectsSiblings() || PREFERENCE_JUST_DRAG_MESSAGE;

		// Fix W2875:  There was a dirty pointer that should be NULL all the time here.
		Debug.assertNull(mDraggingEdge);
		mDraggingEdge = edge;

		mDraggingConnectors.clear();
		mDraggingTSGraphObjects.clear();

		// Removed the source connector to fix the jittering problem while resizing, 
		// see ADLifelineCompartmentImpl::MoveConnector() for a supporting fix.

		TSConnector sourceConnector = edge.getSourceConnector();
		addDraggingConnector(
			sourceConnector,
			bJustDragMessage,
			lVerticalOffset);

		// Track the interaction operand, if it exists
		if (getDrawingAreaControl() != null) {
			IDiagramEngine diagramEngine =
				getDrawingAreaControl().getDiagramEngine();

			if (diagramEngine instanceof IADSequenceDiagEngine) {
				IADSequenceDiagEngine sqdEngine =
					(IADSequenceDiagEngine) diagramEngine;
				ETPairT < IInteractionOperand,
					ICompartment > info =
						sqdEngine.getEdgesInteractionOperand((IETEdge) edge);

				IInteractionOperand operand = info.getParamOne();
				ICompartment compartment = info.getParamTwo();
				if (compartment != null) {
					setOperandCompartment(null);
					if (compartment
						instanceof IADInteractionOperandCompartment) {
						setOperandCompartment(
							(IADInteractionOperandCompartment) compartment);

					}
				}
			}
		}

		if (bJustDragMessage) {
			// Add the edge (after the connectors' nodes above) to the
			// vector of dragging Tom Sawyer graph objects
			addDraggingTSGraphObject(edge);
		} else {
			TSNode fromNode = edge.getSourceNode();
			TSNode toNode = edge.getTargetNode();

			addDraggingTSGraphObject(edge);
			addDraggingTSGraphObject(fromNode);
			addDraggingTSGraphObject(toNode);
		}
	}

	/**
	 * Prepare for dragging a specific lifeline piece, used for message-to-self
	 *
	 * @param pPiece[in] A lifeline piece to be dragged
	 * @param bDragTop[in] When true drag the top of the piece, moving the piece.
	 *                     When false drag the bottom of the piece, resizing the piece.
	 *
	 * @return HRESULT
	 */
	public void setDraggingPiece(LifelinePiece piece, boolean bDragTop) {
		if (piece != null) {
			setPiece(null);
			setPiece(piece);
			setDragPieceTop(bDragTop);

			// Add the node containing the piece to the
			// vector of dragging Tom Sawyer graph objects
			IADLifelineCompartment compartment = mPiece.getParentCompartment();
			if (compartment != null) {
				TSObject tsObject = TypeConversions.getTSObject(compartment);
				if (tsObject instanceof TSGraphObject) {
					addDraggingTSGraphObject((TSGraphObject) tsObject);
				}
			}

			// Add the edges connected to the piece
			for (int iIndx = LifelineConnectorLocation.LCL_TOPLEFT;
				iIndx <= LifelineConnectorLocation.LCL_BOTTOMLEFT;
				iIndx++) {

				TSEdge tsEdge = piece.getAttachedEdge(iIndx);
				if (tsEdge != null) {
					addDraggingTSGraphObject(tsEdge);
				}
			}
		}
	}
	//**************************************************
	// Data Access Methods
	//**************************************************

	/**
	 * @return
	 */
	public boolean isDeep() {
		return mDeep;
	}

	/**
	 * @param b
	 */
	public void setDeep(boolean b) {
		mDeep = b;
	}

	/**
	 * @return
	 */
	public IDrawingAreaControl getDrawingAreaControl() {
		return mDrawingAreaControl;
	}

	/**
	 * @param control
	 */
	public void setDrawingAreaControl(IDrawingAreaControl control) {
		mDrawingAreaControl = control;
	}

	/**
	 * @return
	 */
	public IStretchContext getStretchContext() {
		return mStretchContext;
	}

	/**
	 * @param context
	 */
	public void setStretchContext(IStretchContext context) {
		mStretchContext = context;
	}

	/**
	 * @return
	 */
	public IETRect getRestrictedArea() {
		return mRectRestrictedArea;
	}

	/**
	 * @param rect
	 */
	public void setRestrictedArea(IETRect rect) {
		mRectRestrictedArea = rect;
	}

	/**
	 * @return
	 */
	public TSConstPoint getLastXYPos() {
		return m_LastXYPos;
	}

	/**
	 * @param point
	 */
	public void setLastXYPos(TSConstPoint point) {
		m_LastXYPos = point;
	}

	/**
	 * @return
	 */
	public TSEdge getDraggingEdge() {
		return mDraggingEdge;
	}

	/**
	 * @param edge
	 */
	public void setDraggingEdge(TSEdge edge) {
		mDraggingEdge = edge;
	}

	/**
	 * @return
	 */
	public LifelinePiece getPiece() {
		return mPiece;
	}

	/**
	 * @param piece
	 */
	public void setPiece(LifelinePiece piece) {
		mPiece = piece;
	}

	/**
	 * @return
	 */
	public boolean isDragPieceTop() {
		return mDragPieceTop;
	}

	/**
	 * @param b
	 */
	public void setDragPieceTop(boolean b) {
		mDragPieceTop = b;
	}

	/**
	 * @return
	 */
	public TSEGraph getGraph() {
		return mGraph;
	}

	/**
	 * @param graph
	 */
	public void setGraph(TSEGraph graph) {
		mGraph = graph;
	}

	/**
	 * @return
	 */
	public IADInteractionOperandCompartment getOperandCompartment() {
		return m_OperandCompartment;
	}

	/**
	 * @param compartment
	 */
	public void setOperandCompartment(IADInteractionOperandCompartment compartment) {
		m_OperandCompartment = compartment;
	}

	/**
	 * @return
	 */
	public int getDragRestrictionType() {
		return m_DragRestriction;
	}

	/**
	 * @param i
	 */
	public void setDragRestrictionType(int i) {
		m_DragRestriction = i;
	}

	/**
	 * @return
	 */
	public boolean isDragAffectsSiblings() {
		return mDragAffectsSiblings;
	}

	/**
	 * @param b
	 */
	public void setDragAffectsSiblings(boolean b) {
		mDragAffectsSiblings = b;
	}

}
