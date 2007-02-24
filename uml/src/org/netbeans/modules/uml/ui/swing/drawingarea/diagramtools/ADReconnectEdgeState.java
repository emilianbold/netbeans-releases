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

package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.util.List;

import javax.swing.SwingUtilities;

//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEConnector;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSEObjectUI;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;
import com.tomsawyer.editor.TSENode;
//import com.tomsawyer.editor.state.TSEReconnectEdgeState;
import com.tomsawyer.editor.tool.TSEReconnectEdgeTool;
//import com.tomsawyer.editor.state.TSESelectState;
import com.tomsawyer.editor.tool.TSESelectTool;
import com.tomsawyer.editor.ui.TSENodeUI;

import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine;
import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;

import org.netbeans.modules.uml.ui.support.relationshipVerification.AddEdgeEventDispatcher;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents;
import org.netbeans.modules.uml.ui.support.relationshipVerification.ReconnectEdgeEvents;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETEdgeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;

import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.ReconnectEdgeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.NoDropCursor;
import org.netbeans.modules.uml.core.support.Debug;

/*
 * 
 * @author KevinM
 *
 *	This class dispatches the IReconnectEdgeEvents via aggregation of the ReconnectEdgeEvents class.
 */
//public class ADReconnectEdgeState extends TSEReconnectEdgeState implements IReconnectEdgeEvents
public class ADReconnectEdgeState extends TSEReconnectEdgeTool implements IReconnectEdgeEvents
{
   protected IEdgeVerification m_IEdgeVerification;
   protected Cursor m_defaultCuror = null;
   protected Cursor m_noDropCursor;
   protected TSENode m_orginalSourceNode;
   protected TSENode m_orginalTargetNode;
   protected IReconnectEdgeEvents m_eventDispatcher = null;
   protected TSConstPoint m_lastMousePos;
   protected boolean m_reconnectedEdge = false;
   protected TSEConnector m_associatedConnector = null;
   /*
   * Constructor for ADReconnectEdgeState
   */
//   public ADReconnectEdgeState(TSEWindowInputState parentState)
   public ADReconnectEdgeState(TSEWindowInputTool parentState)
   {
      super(parentState);
      m_IEdgeVerification = null;
      m_noDropCursor = NoDropCursor.getCursor();
   }

   /*
    * Returns true if the user has the mouse over an edge clipping point on a incident node.
    */
   public boolean possibleSourceAt(TSConstPoint pt)
   {
      return super.possibleSourceAt(pt);
   }

   /*
    * Called by the super class just before the connectEdge is made, returns true to allow the reconnection.
    */
   public boolean possibleTargetAt(TSConstPoint pt)
   {
      boolean mouseOverNode = isPointOverNode(pt);

      if (mouseOverNode && verifyReconnectTarget(pt))
      {
         setCursor(m_defaultCuror);
         return super.possibleTargetAt(pt);
      }
      else if (mouseOverNode)
      {
         showNoDropCursor();
      }
      else
      {
         setCursor(m_defaultCuror);
      }
      return false;
   }

   /*
    * Displays the NoDrop Cursor.
    */
   protected void showNoDropCursor()
   {
      setCursor(m_noDropCursor);
   }

   /*
    * Called by the super class when its time to reconnect the edge, please see onMousedReleased.
    */
   public void connectEdge()
   {
      try
      {
         boolean reconnectingTarget = this.isReconnectingTarget();
         if (verifyReconnectTarget(m_lastMousePos))
         {
				if (m_associatedConnector != null)
				{
					if (reconnectingTarget)
					{
						setTargetConnector(m_associatedConnector);
					}
					else
					{
						setSourceConnector(m_associatedConnector);
					}
					
					TSENode target = getTargetNode();
					TSENode source = getSourceNode();

					ETPairT<List,List> bendPoints = getReconnectEdgeBendPoints();
		
					try {
						ETGenericEdgeUI ui = (ETGenericEdgeUI)getEdge().getUI();
						/*
						 * Reconnect the interactive Edge from the hidden node to the target node.
						 */
						ETEdge createdEdge = (ETEdge) getGraphWindow().reconnectEdge(getEdge(), reconnectingTarget ? target : source, m_associatedConnector, !reconnectingTarget, bendPoints.getParamOne(), bendPoints.getParamTwo());		
						
						if (createdEdge != null)
						{
							ui.setOwner(createdEdge);
							createdEdge.setUI(ui);
							
							setDefaultState();
						}
					} catch (Exception e) {
						// Just it ignore this exception its just an assertion.
					}					
				}
				else
				{
					super.connectEdge();
				}
				//
            m_associatedConnector = null;
         }
         else
         {
            cancelAction();
         }
      }
      catch (Exception e)
      {
         ETSystem.out.println(e.getMessage());
      }
   }

   /*
    * Returns the Edge Verification interface.
    */
   protected IEdgeVerification getVerification()
   {
      if (m_IEdgeVerification == null)
      {
         CreationFactoryHelper helper = new CreationFactoryHelper();
         m_IEdgeVerification = helper.getEdgeVerification();
      }
      return m_IEdgeVerification;
   }

   /*
    * Returns the Rubberbands Drawengine interface.
    */
   protected IEdgeDrawEngine getDrawEngine()
   {
      TSEEdge edge = getEdge();
      if (edge != null && edge.getUI() instanceof IETGraphObjectUI)
      {
         IETGraphObjectUI ui = (IETGraphObjectUI) edge.getUI();
         if (ui != null && ui.getDrawEngine() instanceof IEdgeDrawEngine)
         {
            return (IEdgeDrawEngine) ui.getDrawEngine();
         }
      }
      return null;
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.state.TSEBuildEdgeState#initBuildEdge()
    */
   protected void initBuildEdge()
   {
		m_associatedConnector = null;
      m_reconnectedEdge = false;
      m_orginalTargetNode = (TSENode) getEdge().getTargetNode();
      m_orginalSourceNode = (TSENode) getEdge().getSourceNode();

      if (fireReconnectEdgeStart(this.getReconnectContext()) == true)
      {
         // Flash the ghost busters sign.
         this.showNoDropCursor();
         this.cancelAction();
         return;
      }
      else
      {
         super.initBuildEdge();
         m_defaultCuror = getDefaultCursor();

         if (!getAllowReconnection())
         {
            this.cancelAction();
            return;
         }
      }
   }

   /*
    * Returns true if we're allowed to reconnect this ModelElement type and the drawing isn't readonly.
    */
   protected boolean getAllowReconnection()
   {
      IEdgeDrawEngine drawEngine = getDrawEngine();
      return drawEngine != null && drawEngine.getAllowReconnection() && verifyReconnectStart();
   }

   /*
    * Returns true if we can start the reconnect process with this Model element type.
    */
   protected boolean verifyReconnectStart()
   {
      return getVerification().verifyReconnectStart(this.isReconnectingTarget() ? this.getTargetElement() : this.getSourceElement(), getDrawEngine().getElementType());
   }

   /*
    * Returns the orginal source node's UI.
    */
   protected IETGraphObjectUI getSourceUI()
   {
      return m_orginalSourceNode != null ? (IETGraphObjectUI) this.m_orginalSourceNode.getUI() : null;
   }

   /*
    * Returns the orginal target node's UI
    */
   protected IETGraphObjectUI getTargetUI()
   {
      return m_orginalTargetNode != null ? (IETGraphObjectUI) this.m_orginalTargetNode.getUI() : null;
   }

   /*
    * Returns true if the object under the logical world pt is a valid reconnection target.
    */
   protected boolean verifyReconnectTarget(TSConstPoint pt)
   {
      return verifyReconnectTarget(getUIAt(pt));
   }

   /*
    * Returns true if the object at the mouse event is a valid reconnection target.
    */
   protected boolean verifyReconnectTarget(MouseEvent pEvent)
   {
      return verifyReconnectTarget(getUIAt(pEvent));
   }

   /*
    * Returns true if the ui' model element is valid reconnection target.
    */
   protected boolean verifyReconnectTarget(IETGraphObjectUI hitUI)
   {
      if (hitUI == null)
         return false;

      boolean verified = false;
      IElement fromElement;
      IElement toElement;

      /* Figure out which Model Elements depending if we are reconnecting our target
      * or source node.
      */
      if (isReconnectingTarget())
      {
         fromElement = getSourceElement();
         toElement = hitUI.getModelElement();
      }
      else
      {
         fromElement = hitUI.getModelElement();
         toElement = getTargetElement();
      }

      if (fromElement != null && toElement != null && getDrawEngine() != null)
      {
         return getVerification().verifyFinishNode(fromElement, toElement, getDrawEngine().getElementType());
      }
      return false;
   }

   /*
    * Returns true if the current target node is valid reconnection target.
    */
   protected boolean verifyReconnectTarget()
   {
      // We don't want to use the orginal nodes for this one we want the final UI's.
      return verifyReconnectTarget(this.isReconnectingTarget() ? (IETGraphObjectUI) super.getTargetNode().getUI() : (IETGraphObjectUI) super.getSourceNode().getUI());
   }

   /*
    * Helper function that returns the model element given a node.
    */
   protected IElement getNodeElement(TSENode tsNode)
   {
      IETGraphObjectUI nodeUI = tsNode != null ? (IETGraphObjectUI) tsNode.getUI() : null;
      return nodeUI != null ? nodeUI.getModelElement() : null;
   }

   /*
    * Returns the orginal source nodes model element. 
    */
   protected IElement getSourceElement()
   {
      return getNodeElement(m_orginalSourceNode);
   }

   /*
    * Returns the orginal target nodes model element. 
    */
   protected IElement getTargetElement()
   {
      return getNodeElement(m_orginalTargetNode);
   }

   /*
    * Copied from the CreateEdgeState, if you can't create bends durning create edge you might not be able to durning reconnect.
    * Give the listeners another chance to control the reconnect process.
    */
   protected boolean fireShouldCreateBendEvent(TSConstPoint point)
   {
      AddEdgeEventDispatcher dispatch = new AddEdgeEventDispatcher(getDrawingArea().getDrawingAreaDispatcher(), getDrawingArea().getDiagram(), "");
      return dispatch.fireShouldCreateBendEvent(point);
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowInputState#onMousePressed(java.awt.event.MouseEvent)
    */
   public void onMousePressed(MouseEvent pEvent)
   {
      m_lastMousePos = getNonalignedWorldPoint(pEvent);

      boolean eventHandled = false;
      boolean isLeftBtn = isLeftMouseEvent(pEvent);

      // Give the drawEngine first crack.
      IEdgeDrawEngine drawEngine = getDrawEngine();
      if (drawEngine != null && isLeftBtn)
      {
         if (this.isDragInProgress())
         {
            eventHandled = drawEngine.handleLeftMouseBeginDrag(getETPoint(m_lastMousePos), getETPoint(m_lastMousePos));
         }
         else
         {
            eventHandled = drawEngine.handleLeftMouseButton(pEvent);
         }
      }

      if (!eventHandled)
      {
         if (pEvent.isPopupTrigger() || !isLeftBtn)
         {
            // Kill the reconnection process.
            this.cancelAction();
         }

         if (!isReconnecting())
         {
            super.onMousePressed(pEvent); // Start the connection process.
         }
         else
         {
            // Check for bends, if the don't hit a graph object.
            if (!isMouseOverNode(pEvent) && isLeftBtn && fireShouldCreateBendEvent(m_lastMousePos))
            {
               super.onMousePressed(pEvent);
            }
            else if (verifyReconnectTarget(pEvent) && finishReconnection(pEvent))
            {
               super.onMousePressed(pEvent);
            }
         }
      }
   }

   /*
    * Returns true if the mouse is currently over a node.
    */
   protected boolean isMouseOverNode(MouseEvent pEvent)
   {
      return isPointOverNode(getNonalignedWorldPoint(pEvent));
   }

   /*
    * Returns true if the point is currently over a node.
    */
   protected boolean isPointOverNode(TSConstPoint pt)
   {
      IETGraphObjectUI ui = getUIAt(pt);
      return ui != null && ui instanceof TSENodeUI;
   }

   /*
    * Returns the visual cursor that should be displayed at this point.
    */
   public Cursor cursorFromPoint(TSConstPoint pt)
   {
      boolean mouseOverNode = isPointOverNode(pt);
      if (mouseOverNode && verifyReconnectTarget(pt))
      {
         return m_defaultCuror;
      }
      else if (mouseOverNode)
      {
         return m_noDropCursor;
      }
      else
      {
         return m_defaultCuror;
      }
   }

   /*
    * Changes the cursor depending if we can connect at the current mouse position.
    */
   public void onMouseMoved(MouseEvent pEvent)
   {
      TSConstPoint pt = getNonalignedWorldPoint(pEvent);
      m_lastMousePos = pt;
      //setCursor(cursorFromPoint(pt));

      IReconnectEdgeContext pContext = getReconnectContext(pt);

      boolean cancel = fireReconnectEdgeMouseMove(pContext);
      if (cancel)
      {
         if (getDisconnectedFromNode() != null && getDisconnectedFromNode().getETUI() != getUIAt(pt))
            this.showNoDropCursor();
         // else they are trying to return the edge to the orginal target.
      }
      else
      {
			super.onMouseMoved(pEvent);
			this.setCursor(m_defaultCuror);
			refresh();
      }

      if (!cancel)
      {
         this.setCursor(m_defaultCuror);
      }
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowInputState#onMouseDragged(java.awt.event.MouseEvent)
    */
   public void onMouseDragged(MouseEvent pEvent)
   {
      boolean eventHandled = false;
      m_lastMousePos = getNonalignedWorldPoint(pEvent);
      IEdgeDrawEngine drawEngine = getDrawEngine();
      setCursor(cursorFromPoint(m_lastMousePos));
      if (drawEngine != null)
      {
         IETPoint pt = getETPoint(pEvent);
         eventHandled = drawEngine.handleLeftMouseDrag(pt, pt);
      }

      if (!eventHandled)
      {
         super.onMouseDragged(pEvent);
      }
      else
	   {
			refresh();    	
	   }
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowInputState#onMouseClicked(java.awt.event.MouseEvent)
    */
   public void onMouseClicked(MouseEvent pEvent)
   {
      m_lastMousePos = getNonalignedWorldPoint(pEvent);
      boolean eventHandled = false;

      IEdgeDrawEngine drawEngine = getDrawEngine();

      if (drawEngine != null)
      {
         if (isLeftMouseEvent(pEvent))
         {
            eventHandled = pEvent.getClickCount() == 2 ? drawEngine.handleLeftMouseButtonDoubleClick(pEvent) : drawEngine.handleLeftMouseButton(pEvent);
         }
         else if (isRightMouseEvent(pEvent))
         {
            eventHandled = drawEngine.handleRightMouseButton(pEvent);
         }

         refresh();
      }

      if (!eventHandled)
      {
         super.onMouseClicked(pEvent);
      }
   }

   /*
    * Conversion function
    */
   protected IETPoint getETPoint(TSConstPoint point)
   {
      return point != null ? new ETPointEx(point) : new ETPoint(0, 0);
   }

   /*
    * Returns the mouse position for the event or the transformed into logical space
    */
   protected IETPoint getETPoint(MouseEvent pEvent, boolean transform)
   {
      return !transform ? getETPoint(pEvent) : getETPoint(getNonalignedWorldPoint(pEvent));
   }

   /*
    * Returns the mouse position for the event
    */
   protected IETPoint getETPoint(MouseEvent pEvent)
   {
      Point mousePos = pEvent.getPoint();
      return new ETPoint(mousePos.x, mousePos.y);
   }

   protected boolean isLeftMouseEvent(MouseEvent pEvent)
   {
      return SwingUtilities.isLeftMouseButton(pEvent);
   }

   protected boolean isRightMouseEvent(MouseEvent pEvent)
   {
      return !isLeftMouseEvent(pEvent);
   }

   /*
    *  (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowInputState#onMouseReleased(java.awt.event.MouseEvent)
    */
   public void onMouseReleased(MouseEvent pEvent)
   {
      boolean eventHandled = false;
      IEdgeDrawEngine drawEngine = getDrawEngine();
      if (drawEngine != null && isLeftMouseEvent(pEvent))
      {
         if (pEvent.isControlDown())
         {
            eventHandled = drawEngine.handleLeftMouseDrop(getETPoint(pEvent), null, false);
         }
         else
         {
            eventHandled = drawEngine.handleLeftMouseDrop(getETPoint(pEvent), null, true);
         }

         refresh();
      }

      if (!eventHandled)
      {
         eventHandled = !this.finishReconnection(pEvent);
      }

      if (!eventHandled)
      {
         super.onMouseReleased(pEvent);
         //	 reset.
         m_reconnectedEdge = false;
      }
      else
      {
         if (getDisconnectedFromNode() != null && getDisconnectedFromNode().getETUI() == getUIAt(pEvent))
         {
            // They are trying to return the edge to the orginal node, just cancel out.
            this.cancelAction();
         }
         else
         {
            this.showNoDropCursor();
         }
      }

   }

   /*
    * Returns true if we are reconnecting the target node of this edge.
    */
   protected boolean isReconnectingTarget()
   {
      return !this.isReconnectingSource();
   }

   /*
    * Hit testing function.
    */
   protected IETGraphObjectUI getUIAt(TSConstPoint pt)
   {
//      TSEObject obj = this.getObjectAt(pt, null, this.getGraphWindow().getGraph());
       TSEObject obj = this.getHitTesting().getGraphObjectAt(pt, this.getGraphWindow().getGraph(), true);
        if (obj instanceof TSEConnector) {
           obj = (TSEObject)((TSEConnector) obj).getOwner();
        }

      /*
       *  We don't want to return ourselves.
       */
      if (getEdge() == obj)
      {
//         obj = this.getObjectAt(pt, getEdge(), this.getGraphWindow().getGraph());
         obj = this.getHitTesting().getGraphObjectAt(pt, this.getGraphWindow().getGraph(), true);


      }
      return obj != null && obj.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI) obj.getUI() : null;
   }

   /*
    * Hit testing function.
    */
   protected IETGraphObjectUI getUIAt(MouseEvent pEvent)
   {
      return getUIAt(getNonalignedWorldPoint(pEvent));
   }

   /*
    * Used by the reconnect context, returns the current presentation element for this edge
    */
   protected IEdgePresentation getIEdgePresentation()
   {
      if (this.getDrawEngine() instanceof ETEdgeDrawEngine)
      {
         ETEdgeDrawEngine de = (ETEdgeDrawEngine) this.getDrawEngine();
         return de.getIEdgePresentation();
      }
      else
      {
         return null;
      }
   }

   /*
    * Returns the Anchored Node used by the Reconnect edge Events, the node returned depends if 
    * we are reconnecting the source or target end of the edge.
    */
   protected IETNode getAnchoredNode()
   {
      return isReconnectingTarget() ? (IETNode) m_orginalSourceNode : (IETNode) m_orginalTargetNode;
   }

   /*
    * Returns the opposite end from the AnchoredNode, the node that we just disconnected from.
    */
   protected IETNode getDisconnectedFromNode()
   {
      return isReconnectingTarget() ? (IETNode) m_orginalTargetNode : (IETNode) m_orginalSourceNode;
   }

   /*
    * Returns an IReconnectEdgeContext, used to fire the reconnection events.
    */
   protected IReconnectEdgeContext getReconnectContext(TSConstPoint pt)
   {
      IETEdge edge = getIETEdge();

      IReconnectEdgeContext reconnectContext = new ReconnectEdgeContext();
      reconnectContext.setEdge(edge);
      reconnectContext.setReconnectTarget(isReconnectingTarget());
      reconnectContext.setAnchoredNode(getAnchoredNode());

      reconnectContext.setLogicalPoint(getETPoint(pt));

      if (edge != null)
         reconnectContext.setPreConnectNode(isReconnectingTarget() ? edge.getToNode() : edge.getFromNode());

      IETGraphObjectUI hitUI = getUIAt(pt);
      if (hitUI != null)
      {
         TSEObject ProposedEnd = hitUI.getDrawEngine().getParent().getOwner();

         if (ProposedEnd instanceof TSENode)
         {
            reconnectContext.setProposedEndNode((IETNode) ProposedEnd);
         }
         else if (ProposedEnd instanceof TSEConnector)
         {
            TSEConnector connector = (TSEConnector) ProposedEnd;
            reconnectContext.setProposedEndNode((IETNode) connector.getOwner());
         }
      }
      return reconnectContext;
   }

   protected TSConstPoint getEndPoint()
   {
      ETEdgeDrawEngine de = (ETEdgeDrawEngine) this.getDrawEngine();
      if (de != null)
      {
         ETPairT < TSConstPoint, TSConstPoint > lineSegment;
         if (this.isReconnectingTarget())
         {
            lineSegment = de.getFromLineSegment();
            return lineSegment != null ? lineSegment.getParamTwo() : null;
         }
         else
         {
            lineSegment = de.getToLineSegment();
            return lineSegment != null ? lineSegment.getParamOne() : null;
         }
      }
      return null;
   }

   protected TSConstPoint getStartingPoint()
   {
      ETEdgeDrawEngine de = (ETEdgeDrawEngine) this.getDrawEngine();
      if (de != null)
      {
         ETPairT < TSConstPoint, TSConstPoint > lineSegment;
         if (this.isReconnectingTarget())
         {
            lineSegment = de.getToLineSegment();
            return lineSegment != null ? lineSegment.getParamOne() : null;
         }
         else
         {
            lineSegment = de.getFromLineSegment();
            return lineSegment != null ? lineSegment.getParamTwo() : null;
         }
      }
      return null;
   }

   /*
    * 
    */
   protected IReconnectEdgeContext getReconnectContext()
   {
      TSConstPoint pt = getStartingPoint();

      return pt != null ? getReconnectContext(pt) : null;
   }

   /*
    * Returns an IReconnectEdgeContext, used to fire the reconnection events.
    */
   protected IReconnectEdgeContext getReconnectContext(MouseEvent pEvent)
   {
      return getReconnectContext(getNonalignedWorldPoint(pEvent));
   }

   /*
    * Returns true if the Edge was reconnected.
    */
   public boolean finishReconnection(TSConstPoint atPt)
   {
      boolean connected = false;
      IReconnectEdgeContext pContext = getReconnectContext(atPt);
      boolean cancel = m_reconnectedEdge == false ? fireReconnectEdgeFinish(pContext) : false;
      if (cancel)
      {
         //showNoDropCursor();
         //this.cancelAction();
         connected = false;
      }
      else
      {
         if (pContext.getAssociatedConnector() != null && pContext.getEdge() != null)
         {
            m_associatedConnector = (TSEConnector) pContext.getAssociatedConnector();
         }
         connected = true;
      }

      return connected;
   }

   /*
    * Gets called when the mouse is released only if the drawEngine didn't handle this event.
    */
   public boolean finishReconnection(MouseEvent pEvent)
   {
      boolean handled;

      if (verifyReconnectTarget(pEvent))
      {
         handled = this.finishReconnection(this.getNonalignedWorldPoint(pEvent));
      }
      else
      {
         handled = false;
      }

      if (handled)
      {
         pEvent.consume();
      }

      return handled;
   }

   /*
    *  Repaints the window.
   */
   protected void refresh()
   {
      getGraphWindow().drawGraph();
      getGraphWindow().fastRepaint();
   }

   /*
    * Returns the Rubberband edge cast to and IETEdge interface.
    */
   public IETEdge getIETEdge()
   {
      TSEEdge edge = getEdge();
      return edge instanceof IETEdge ? (IETEdge) edge : null;
   }

   /*
    * Returns the DrawingArea Control.
    */
   protected IDrawingAreaControl getDrawingArea()
   {
      ADGraphWindow graphWindow = getGraphWindow() instanceof ADGraphWindow ? (ADGraphWindow) getGraphWindow() : null;
      return graphWindow != null ? graphWindow.getDrawingArea() : null;
   }

   /*
    * Returns the IReconnectEdgeEvents Event Dispatcher.
    */
   protected IReconnectEdgeEvents getEventDispatcher()
   {
      if (m_eventDispatcher == null)
         m_eventDispatcher = new ReconnectEdgeEvents(getDrawingArea().getDrawingAreaDispatcher(), getDrawingArea().getDiagram());

      return m_eventDispatcher;
   }

   /* (non-Javadoc)
    * @see com.tomsawyer.editor.TSEWindowState#cancelAction()
    */
   public void cancelAction()
   {
      try
      {
         // Return to out parent state.
         this.stopMouseInput();
         super.cancelAction();
      }
      catch (Exception e)
      {
         //finalizeState();
          finalizeTool();
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#createReconnectEdgeContext(com.tomsawyer.util.TSConstPoint, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge, boolean, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode, org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode)
    */
   public IReconnectEdgeContext createReconnectEdgeContext(TSConstPoint point, IETEdge pEdge, boolean bReconnectTarget, IETNode pAnchoredNode, IETNode pPreConnectNode, IETNode pProposedEndNode)
   {
      IReconnectEdgeContext reconnectContext = new ReconnectEdgeContext();
      reconnectContext.setEdge(pEdge);
      reconnectContext.setReconnectTarget(bReconnectTarget);
      reconnectContext.setAnchoredNode(pAnchoredNode);
      reconnectContext.setPreConnectNode(pPreConnectNode);
      reconnectContext.setProposedEndNode(pProposedEndNode);
      return reconnectContext;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#fireReconnectEdgeFinish(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
    */
   public boolean fireReconnectEdgeFinish(IReconnectEdgeContext pContext)
   {
      return this.getEventDispatcher().fireReconnectEdgeFinish(pContext);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#fireReconnectEdgeMouseMove(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
    */
   public boolean fireReconnectEdgeMouseMove(IReconnectEdgeContext pContext)
   {
      return this.getEventDispatcher().fireReconnectEdgeMouseMove(pContext);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#fireReconnectEdgeStart(org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext)
    */
   public boolean fireReconnectEdgeStart(IReconnectEdgeContext pContext)
   {
      return this.getEventDispatcher().fireReconnectEdgeStart(pContext);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#getParentDiagram()
    */
   public IDiagram getParentDiagram()
   {
      return getEventDispatcher().getParentDiagram();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.relationshipVerification.IReconnectEdgeEvents#setParentDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram)
    */
   public void setParentDiagram(IDiagram pParent)
   {
      getEventDispatcher().setParentDiagram(pParent);
   }

   protected ETPairT < List, List > getReconnectEdgeBendPoints()
   {
      if (getEdge() != null)
      {
         java.util.List bendPoints = this.getEdge().bendPoints();
         java.util.List newBendPoints = new ETArrayList < TSConstPoint > ();
         IteratorT < TSConstPoint > iter = new IteratorT < TSConstPoint > (bendPoints);
         while (iter.hasNext())
         {
            newBendPoints.add((TSConstPoint) iter.next().clone());
         }
         return new ETPairT < List, List > (bendPoints, newBendPoints);
      }
      else
         return new ETPairT < List, List > ();
   }
   
   
	protected void setDefaultState() {
		try {
			// We need to recreate this, becuase we don't call the super.connectEdge() when dealing with connectors.
			// and we have drawing problems the second time this tool is used.
                        if (this.getParentTool() instanceof TSESelectTool)
			{
				//TSESelectState pSelect = (TSESelectState)getParentState();
				TSESelectTool pSelect = (TSESelectTool)getParentTool();
//				pSelect.setReconnectEdgeState(new ADReconnectEdgeState(pSelect));
                                pSelect.setReconnectEdgeTool(new ADReconnectEdgeState(pSelect));
			}
			
			((ADGraphWindow) this.getGraphWindow()).getDrawingArea().switchToDefaultState();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    //JM: Fix for Bug#6263430
    public void addDirtyRegion(TSEObject object) {       
       if (object == null) 
       {
           //Debug.out.println("addDirtyRegion ==  object null.. so do nothing..");
       }
       else 
       {
           super.addDirtyRegion(object);
       }
    }
}
