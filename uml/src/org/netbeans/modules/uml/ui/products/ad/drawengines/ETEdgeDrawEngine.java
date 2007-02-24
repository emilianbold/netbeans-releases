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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.Rectangle;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.drawing.TSPEdge;
import com.tomsawyer.drawing.TSPNode;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.editor.TSEColor;
//import com.tomsawyer.editor.TSEWindowInputState;
import com.tomsawyer.editor.TSEWindowInputTool;

import com.tomsawyer.editor.command.TSEReconnectEdgeCommand;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSERectangularUI;
import com.tomsawyer.editor.TSEConnector;
//import com.tomsawyer.editor.state.TSEReconnectEdgeState;
import com.tomsawyer.editor.tool.TSEReconnectEdgeTool;
import com.tomsawyer.graph.TSGraphObject;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.IteratorT;

import org.netbeans.modules.uml.ui.support.accessibility.UMLAccessibleRole;
import org.netbeans.modules.uml.ui.support.umltsconversions.RectConversions;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineArrowheadKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.DrawEngineLineKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETArrowHead;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ISupportEnums;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.MetaModelHelper;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.controls.drawingarea.GetHelper;
import org.netbeans.modules.uml.ui.controls.drawingarea.ISwapEdgeEndsAction;
import org.netbeans.modules.uml.ui.controls.drawingarea.SwapEdgeEndsAction;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.IADDrawEngineButtonHandler;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETArrowHeadFactory;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.ILabelPresentation;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;

/*
 * 
 * @author KevinM
 *
 */
public abstract class ETEdgeDrawEngine extends ETDrawEngine implements IEdgeDrawEngine
{
   protected static ETStrokeCache m_stokeCache = new ETStrokeCache();
   private int m_nBorderStringID = -1;

   public void doDraw(IDrawInfo pDrawInfo)
   {
      if (this.simpleDrawEdge(pDrawInfo, getLineKind()) == false)
      {
         drawEdge(pDrawInfo, this.getStartArrowKind(), this.getEndArrowKind(), getLineKind());
      }
   }

   protected IElement getNodeElement(TSENode tsNode)
   {
      IETGraphObjectUI nodeUI = tsNode != null ? (IETGraphObjectUI)tsNode.getUI() : null;
      return nodeUI != null ? nodeUI.getModelElement() : null;
   }

   /*
    * Returns the source Element
    */
   protected IElement getSourceElement()
   {
      return getNodeElement(getSourceNode());
   }

   /*
    * Returns the Target Element.
    */
   protected IElement getTargetElement()
   {
      return getNodeElement(getTargetNode());
   }

   /*
    * Returns a rectangle that bounds the source clipping point of this edge in logical units.
    */
   protected IETRect getSourcePtBounds()
   {
      TSConstPoint centerPt = getEdge().getSourceClippingPoint();
      double reconnectSenitivity = getGraphWindow().getPreferences().getDoubleValue(com.tomsawyer.editor.TSEPreferences.RECONNECT_EDGE_SENSITIVITY);
      TSRect rect = new TSRect();
      rect.setBoundsFromCenter(centerPt.getX(), centerPt.getY(), reconnectSenitivity, reconnectSenitivity);
      return RectConversions.newETRect(rect);
   }

   /*
    * Returns a rectangle that bounds the target clipping point of this edge in logical units.
    */
   protected IETRect getTargetPtBounds()
   {
      TSConstPoint centerPt = getEdge().getTargetClippingPoint();
      double reconnectSenitivity = getGraphWindow().getPreferences().getDoubleValue(com.tomsawyer.editor.TSEPreferences.RECONNECT_EDGE_SENSITIVITY);
      TSRect rect = new TSRect();
      rect.setBoundsFromCenter(centerPt.getX(), centerPt.getY(), reconnectSenitivity, reconnectSenitivity);
      return RectConversions.newETRect(rect);
   }

   /*
    * Returns true if the user clicked near the one of ends, pos must be in logical units.
    */
   protected boolean hitEndPoint(IETPoint pPos)
   {
      return getTargetPtBounds().contains(pPos) || getSourcePtBounds().contains(pPos);
   }

   /*
    * Returns true if we are currently reconnecting this drawEngines parent UI's owner edge.
    */
   protected boolean isReconnecting()
   {
//      return this.getUI().getDrawingArea().getGraphWindow().getCurrentState() instanceof TSEReconnectEdgeState;
      return this.getUI().getDrawingArea().getGraphWindow().getCurrentTool() instanceof TSEReconnectEdgeTool;
   }

   /*
    * Returns true if we are currently reconnecting the Target Node of this parents UI owner edge.
    */
   protected boolean isReconnectingTarget()
   {
//      TSEReconnectEdgeState state = isReconnecting() ? (TSEReconnectEdgeState)this.getUI().getDrawingArea().getGraphWindow().getCurrentState() : null;
      TSEReconnectEdgeTool state = isReconnecting() ? (TSEReconnectEdgeTool)this.getUI().getDrawingArea().getGraphWindow().getCurrentTool() : null;
      if (state != null)
      {
         return !state.isReconnectingSource();
      }
      else
      {
         return false;
      }
   }

   /*
    * Hit testing function, pt must be in logical units.
    */
   public IETGraphObjectUI getUIAt(IETPoint pt)
   {
//      TSEWindowInputState state = (TSEWindowInputState)this.getUI().getDrawingArea().getGraphWindow().getCurrentState();
//      TSEWindowInputTool state = (TSEWindowInputTool)this.getUI().getDrawingArea().getGraphWindow().getCurrentState();
       TSEWindowInputTool tool = (TSEWindowInputTool)this.getUI().getDrawingArea().getGraphWindow().getCurrentTool();

//      TSEObject obj = state.getObjectAt(new TSConstPoint(pt.getX(), pt.getY()), null, this.getUI().getDrawingArea().getGraphWindow().getGraph());
       TSEObject obj = tool.getHitTesting().getGraphObjectAt(new TSConstPoint(pt.getX(), pt.getY()), this.getUI().getDrawingArea().getGraphWindow().getGraph(), true);
       
      /*
       *  We don't want to return ourselves.
       */
      if (getEdge() == obj)
      {
//         obj = state.getObjectAt(new TSConstPoint(pt.getX(), pt.getY()), getEdge(), this.getUI().getDrawingArea().getGraphWindow().getGraph());
           obj = tool.getHitTesting().getGraphObjectAt(new TSConstPoint(pt.getX(), pt.getY()), this.getUI().getDrawingArea().getGraphWindow().getGraph(), true);
      }
      return obj != null && obj.getUI() instanceof IETGraphObjectUI ? (IETGraphObjectUI)obj.getUI() : null;
   }

   public boolean handleLeftMouseButton(MouseEvent pEvent)
   {
      this.postInvalidate();
      return false;
   }

   public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      return false;
   }

   public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent)
   {
      this.postInvalidate();
      return false;
   }

   public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos)
   {
      this.postInvalidate();
      return !getAllowReconnection();
   }

   public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving)
   {
      return false;
   }

   public boolean handleRightMouseButton(MouseEvent pEvent)
   {
      return false;
   }

   public boolean getAllowReconnection()
   {
      return !getReadOnly();
   }

   public TSEColor getColor()
   {
      return new TSEColor(getColor(m_nBorderStringID));
   }

   public TSEColor getSelectedColor()
   {
      return getEdgeUI().getHighlightedColor();
   }

   /*
    * Returns the color depending on the state, selected or not.
    */
   public TSEColor getStateColor()
   {
      return getEdge().isSelected() ? getSelectedColor() : getColor();
   }

   protected IETNode getNodeFromID(int nodeID)
   {
      if (this.getEdge() != null)
      {
         if (getSourceNode().getClass().hashCode() == nodeID)
            return (IETNode)getSourceNode();
         else if (getTargetNode().getClass().hashCode() == nodeID)
            return (IETNode)getTargetNode();
      }
      return null;
   }

   /*
    * ParamOne is the list of TSPNodes, and ParamTwo is the TSPEdges
    */
   protected ETPairT < List, List > getPathNodeAndEdges()
   {
      ETList < TSPEdge > pathEdges = new ETArrayList < TSPEdge > ();
      ETList < TSPNode > pathNodes = new ETArrayList < TSPNode > ();

      java.util.List pEdges = getEdge().pathEdges();
      java.util.List pNodes = getEdge().pathNodes();
      java.util.List newBendPoints = new ETArrayList < TSConstPoint > ();
      IteratorT < TSPEdge > pathEdgeIter = new IteratorT < TSPEdge > (pEdges);
      while (pathEdgeIter.hasNext())
      {
         pathEdges.add(pathEdgeIter.next());
      }

      IteratorT < TSPNode > pathNodeIter = new IteratorT < TSPNode > (pNodes);
      while (pathNodeIter.hasNext())
      {
         pathNodes.add(pathNodeIter.next());
      }

      return new ETPairT < List, List > (pathNodes, pathEdges);
   }

   /*
    * Used by swapEdgeEnds, we need to reverse the pathDigraph, so the order is correct.
    * ParamOne is the Original PathNode locations,
    * ParamTwo is the Reversed PathNode locations, used to reroute the path.
    */
   protected ETPairT < List, List > getReconnectEdgeBendPoints()
   {
      java.util.List bendPoints = getEdge().bendPoints();
      java.util.List newBendPoints = new ETArrayList < TSConstPoint > ();
      IteratorT < TSConstPoint > iter = new IteratorT < TSConstPoint > (bendPoints);

      while (iter.hasNext())
      {
         newBendPoints.add((TSConstPoint)iter.next().clone());
      }
      GetHelper.reverseList(this.getGraphWindow(), newBendPoints);
      return new ETPairT < List, List > (bendPoints, newBendPoints);
   }

   public boolean setEdgeEnds(IETNode newSourceNode, IETNode newTargetNode)
   {
      IETNode oldSourceNode = (IETNode)this.getSourceNode(); //getNodeFromID(nNewSourceEndID);
      IETNode oldTargetNode = (IETNode)this.getTargetNode(); //getNodeFromID(nNewTargetEndID);

      TSEEdge edge = this.getEdge();
      if (edge != null)
      {
         ETPairT < List, List > newPath = getReconnectEdgeBendPoints();
         // Swap the nodes.
         edge.setTargetNode((TSENode)newTargetNode);
         edge.setSourceNode((TSENode)newSourceNode);

         // Reroute the pathdigraph, we have reversed the edge.
         if (newSourceNode == oldTargetNode && newTargetNode == oldSourceNode)
         {
            getEdge().reroute(newPath.getParamTwo());
            TSEEdgeUI edgeUI = (TSEEdgeUI)getEdge().getUI();

            // Turn off the arrowheads or is shows up when you reconnect it.
            if (edgeUI != null)
               edgeUI.setArrowType(TSEEdgeUI.NO_ARROW);
         }
         return true;
      }
      return false;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#swapEdgeEnds(int, int)
    */
   public long swapEdgeEnds(int nNewSourceEndID, int nNewTargetEndID)
   {
      IETNode newSourceNode = (IETNode)this.getTargetNode(); //getNodeFromID(nNewSourceEndID);
      IETNode newTargetNode = (IETNode)this.getSourceNode(); //getNodeFromID(nNewTargetEndID);

      return setEdgeEnds(newSourceNode, newTargetNode) ? 1 : 0;
   }

   /**
    * Posts and invalidate to the drawing area
    */
   protected void postSwapEdgeEnds()
   {
      try
      {
         TSEEdge pEdge = this.getEdge();

         if (pEdge != null)
         {
            IETNode pSourceNode = (IETNode)this.getSourceNode();
            IETNode pTargetNode = (IETNode)this.getTargetNode();

            if (pTargetNode != null && pSourceNode != null)
            {
               // TO DO add an interfaces for getID, that returns the hash code.
               int nSourceID = pTargetNode.getClass().hashCode(); //pSourceNode.getID();
               int nTargetID = pSourceNode.getClass().hashCode(); // getID();

               ISwapEdgeEndsAction pAction = new SwapEdgeEndsAction();
               if (pAction != null)
               {
                  pAction.setEdgeToSwap((IETEdge)pEdge);
                  pAction.setNewTargetEndID(nSourceID);
                  pAction.setNewSourceEndID(nTargetID);

                  IDrawingAreaControl pDA = this.getDrawingArea();

                  if (pDA != null)
                  {
                     pDA.postDelayedAction(pAction);
                     postInvalidate();
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   //	public void selectAllCompartments(boolean bSelected) {
   //		// TODO Auto-generated method stub
   //
   //	}
   //
   //	public List getCompartments() {
   //		return null;
   //	}
   //
   //	public List getSelectedCompartments() {
   //		return null;
   //	}

   public boolean simpleDrawEdge(IDrawInfo pInfo, int nLineKind)
   {
      boolean bDidDraw = false;
      try
      {
         if (pInfo != null)
         {
            // Get the zoom level and if it's below 25% go ahead and draw just a line
            if (pInfo.getOnDrawZoom() < .30f)
            {
               // Don't draw at this zoom level, just draw a line
               bDidDraw = drawEdge(pInfo, DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD, DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD, nLineKind);
            }
            else
            {
               bDidDraw = false;
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         bDidDraw = false;
      }
      return bDidDraw;
   }

   /*
    * Returns the first line segment used for the arrow head rotation angle
    */
   public ETPairT < TSConstPoint, TSConstPoint > getFromLineSegment()
   {
      TSEEdge pEdge = getEdge();
      // assume no bends
      List pathNodes = pEdge.pathNodes();
      int numberOfPathNodes = pathNodes.size();

      TSConstPoint fromPt = pEdge.getTargetClippingPoint();
      TSConstPoint toPt = pEdge.getSourceClippingPoint();

      ETPairT < TSConstPoint, TSConstPoint > pPoints = new ETPairT(fromPt, toPt);
      java.util.List bendPoints = pEdge.bendPoints();
      if (bendPoints != null && bendPoints.size() >= 1)
      {
         // Get the position of the first path node.
         pPoints.setParamOne((TSConstPoint)bendPoints.get(0));
      }
      return pPoints;
   }

   /*
    * Returns the last line segment used for the arrow head rotation angle
    */
   public ETPairT < TSConstPoint, TSConstPoint > getToLineSegment()
   {
      TSEEdge pEdge = getEdge();

      // assume no bends
      TSConstPoint fromPt = pEdge.getSourceClippingPoint();
      TSConstPoint toPt = pEdge.getTargetClippingPoint();

      ETPairT < TSConstPoint, TSConstPoint > pPoints = new ETPairT(fromPt, toPt);
      java.util.List bendPoints = pEdge.bendPoints();
      if (bendPoints != null && bendPoints.size() >= 1)
      {
         // Get the position of the last path node.
         pPoints.setParamOne((TSConstPoint)bendPoints.get(bendPoints.size() - 1));
      }
      return pPoints;
   }

   public TSEEdgeUI getEdgeUI()
   {
      return (TSEEdgeUI)this.getParent();
   }

   public TSEEdge getEdge()
   {
      TSEEdge retVal = null;
      
      if(this.getParent() != null)
      {
         IETGraphObjectUI ui = getParent();
         if(ui != null)
         {
            if(ui.getOwner() instanceof TSEEdge)
            {
               retVal = (TSEEdge)ui.getOwner();
            }
         }
      }
      
      return retVal;
   }

   public TSGraphObject getOwnerGraphObject()
   {
      return getEdge();
   }

   TSENode getSourceNode()
   {
      return (TSENode)getEdge().getSourceNode();
   }

   TSENode getTargetNode()
   {
      return (TSENode)getEdge().getTargetNode();
   }

   IETRect getSourceNodeLogicalBounds()
   {
      return new ETRectEx(getSourceNodeBounds());
   }

   IETRect getTargetNodeLogicalBounds()
   {
      return new ETRectEx(getTargetNodeBounds());
   }

   TSConstRect getSourceNodeBounds()
   {
      return getSourceNode().getBounds();
   }

   TSConstRect getTargetNodeBounds()
   {
      return getTargetNode().getBounds();
   }

   /*
    * Draws an arrow head for a line segment.
    */
   protected boolean drawArrowHead(IDrawInfo pInfo, int nArrowheadKind, ETPairT < TSConstPoint, TSConstPoint > segment)
   {
      return drawArrowHead(pInfo, nArrowheadKind, segment, getStateColor());
   }

   protected boolean drawArrowHead(IDrawInfo pInfo, int nArrowheadKind, ETPairT < TSConstPoint, TSConstPoint > segment, TSEColor color)
   {
      if (nArrowheadKind == DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD)
      {
         return true; // success.
      }

      try
      {
         return drawArrowHead(pInfo, createArrowHead(nArrowheadKind), segment, color);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return false;
      }
   }

   protected boolean drawArrowHead(IDrawInfo pInfo, IETArrowHead pArrowHead, ETPairT < TSConstPoint, TSConstPoint > segment, TSEColor color)
   {
      return pArrowHead != null && pArrowHead.draw(pInfo, segment.getParamOne(), segment.getParamTwo(), getStateColor());
   }

   /*
    * Arrowhead factory.
    */
   protected IETArrowHead createArrowHead(int nArrowheadKind)
   {
      return ETArrowHeadFactory.create(nArrowheadKind);
   }

   /*
    * Drawing data.
    */

   protected int getPenWidth()
   {
      return 1;
   }

   protected int getLineKind()
   {
      return DrawEngineLineKindEnum.DELK_SOLID;
   }

   protected int getStartArrowKind()
   {
      return DrawEngineArrowheadKindEnum.DEAK_NO_ARROWHEAD;
   }

   protected int getEndArrowKind()
   {
      return DrawEngineArrowheadKindEnum.DEAK_FILLED;
   }

   /*
    * Returns the Pen or Stroke used to draw the Path Digraph.
    */
   protected Stroke getLineStroke(int nLineKind, float width)
   {
      return m_stokeCache.getStroke(nLineKind, width);
   }

   /*
    * Draws the Path Diagraph (LineSegments) and any visible path nodes.
    */
   protected void drawPathDigraph(IDrawInfo pInfo, IETArrowHead pFromArrow, IETArrowHead pToArrow)
   {
      getEdgeUI().drawPath(pInfo.getTSEGraphics());
   }

   /*
    * Main drawing function
    */
   public boolean drawEdge(IDrawInfo pInfo, int nStartArrowheadKind, int nEndArrowheadKind, int nLineKind)
   { // DrawEngineLineKindEnum
      TSEEdge pEdge = getEdge();
      if (pEdge == null)
      {
         return false;
      }

      TSEEdgeUI edgeUI = (TSEEdgeUI)pEdge.getUI();

      TSEColor stateColor = this.getStateColor();
      TSEGraphics dc = pInfo.getTSEGraphics();

      dc.setColor(stateColor);

      int penWidth = getPenWidth();

      // Select the pen, but save off the current one first.
      Stroke prevPen = dc.getStroke();
      dc.setStroke(this.getLineStroke(nLineKind, penWidth));

      IETArrowHead pFromArrow = createArrowHead(nStartArrowheadKind);
      IETArrowHead pToArrow = createArrowHead(nEndArrowheadKind);

      drawPathDigraph(pInfo, pFromArrow, pToArrow); // Draw the path diagraph.

      // Arrows always have a solid Stroke.
      if (nLineKind != DrawEngineLineKindEnum.DELK_SOLID)
      {
         dc.setStroke(getLineStroke(DrawEngineLineKindEnum.DELK_SOLID, penWidth));
      }

      // Draw the Arrow Heads
      drawArrowHead(pInfo, pFromArrow, this.getFromLineSegment(), stateColor);
      drawArrowHead(pInfo, pToArrow, this.getToLineSegment(), stateColor);

      // Restore the previous pen.
      pInfo.getTSEGraphics().setStroke(prevPen);

      return true;
   }

   public String getPresentationType()
   {
      return "EdgePresentation"; // TODO Override this in derived classes for association's etc.
   }

   public IEdgePresentation getIEdgePresentation()
   {
      return getPresentation() instanceof IEdgePresentation ? (IEdgePresentation)getPresentation() : null;
   }

   /*
    * Returns a Transformed IETPoint it graph based world coordinance.
   */
   protected IETPoint getETPoint(MouseEvent pEvent)
   {
//      TSEWindowInputState state = (TSEWindowInputState)this.getUI().getDrawingArea().getGraphWindow().getCurrentState();
      TSEWindowInputTool state = (TSEWindowInputTool)this.getUI().getDrawingArea().getGraphWindow().getCurrentTool();

      return new ETPointEx(state.getNonalignedWorldPoint(pEvent));
   }

   public String getMetaTypeInitString()
   {
      return null;
   }

   /**
    * Adds a stereotype and or name label pullright to the context menu
    *
    * @param nKind [in] The kind of standard label to add
    * @param pContextMenu[in] The context menu about to be displayed
    */
   public void addStandardLabelsToPullright(int kind, IMenuManager manager)
   {
      if (kind == StandardLabelKind.SLK_STEREOTYPE || kind == StandardLabelKind.SLK_ALL)
      {
         addStereotypeLabelPullright(this, manager);
      }
      if (kind == StandardLabelKind.SLK_NAME || kind == StandardLabelKind.SLK_ALL)
      {
         IElement pEle = getFirstModelElement();
         if (pEle != null && pEle instanceof INamedElement)
         {
            addNameLabelPullright(this, manager);
         }
      }
   }

   /**
    * Handles the stereotype and name sensitivity and check
    *
    * @param pContextMenu [in] The parent context menu that was displayed.
    * @param pMenuItem [in] The button that the sensitivity is being requested for
    * @param buttonKind [in] The ID of the button above.  This ID is the one used when creating the button.
    * @param bHandled [out] true if the button happened to be a stereotype and we set its state
    */
   protected boolean handleStandardLabelSensitivityAndCheck(String id, ContextMenuActionClass pClass)
   {
      boolean handled = false;
      ILabelManager labelMgr = getLabelManager();
      boolean isReadOnly = isParentDiagramReadOnly();
      if (id.equals("MBK_SHOW_STEREOTYPE"))
      {
         boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);
         pClass.setChecked(isDisplayed);

         handled = isReadOnly ? false : true;
      }
      else if (id.equals("MBK_SHOW_NAME_LABEL"))
      {
         boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
         pClass.setChecked(isDisplayed);

         handled = isReadOnly ? false : true;
      }
      return handled;
   }

   /**
    * Handles the stereotype and name selections
    *
    * @param pContextMenu[in] The context menu that was displayed to the user
    * @param pMenuItem[in] The menu that was just selected
    * @param bHandled[out] true if the stereotype selection was handled
    */
   protected boolean handleStandardLabelSelection(ActionEvent e, String id)
   {
      boolean handled = false;
      IDrawingAreaControl pDiagram = getDrawingArea();
      ILabelManager labelMgr = getLabelManager();
      if (pDiagram != null)
      {
         if (id.equals("MBK_SHOW_STEREOTYPE"))
         {
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);
               labelMgr.showLabel(TSLabelKind.TSLK_STEREOTYPE, isDisplayed ? false : true);
            }
            pDiagram.refresh(false);
            handled = true;
         }
         else if (id.equals("MBK_SHOW_NAME_LABEL"))
         {
            if (labelMgr != null)
            {
               boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
               labelMgr.showLabel(TSLabelKind.TSLK_NAME, isDisplayed ? false : true);
               if (!isDisplayed)
               {
                  IPresentationElement pPE = labelMgr.getLabel(TSLabelKind.TSLK_NAME);
                  if (pPE != null && pPE instanceof ILabelPresentation)
                  {
                     pDiagram.postEditLabel((ILabelPresentation)pPE);
                  }
               }
            }
            pDiagram.refresh(false);
            handled = true;
         }
      }
      return handled;
   }

   public String getDrawEngineID()
   {
      return "EdgeDrawEngine";
   }

   /**
    * Handles the stereotype sensitivity and check
    *
    * @param pContextMenu [in] The parent context menu that was displayed.
    * @param pMenuItem [in] The button that the sensitivity is being requested for
    * @param buttonKind [in] The ID of the button above.  This ID is the one used when creating the button.
    * @param bHandled [out] true if the button happened to be a stereotype and we set its state
    */
   public boolean handleStereotypeSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind)
   {
      boolean bHandled = false;
      try
      {

         ILabelManager pLabelManager = getLabelManager();
         switch (buttonKind)
         {
            case IADDrawEngineButtonHandler.MBK_SHOW_STEREOTYPE :
               {
                  // Set the check state
                  boolean bIsDisplayed = pLabelManager.isDisplayed(TSLabelKind.TSLK_STEREOTYPE);
                  pMenuItem.setChecked(bIsDisplayed);

                  // Set the sensitivities
                  pMenuItem.setSensitive(parentDiagramIsReadOnly() ? false : true);

                  bHandled = true;
               }
               break;
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return bHandled;
   }

   /*
    * Return a pointer to the IEdgePresentation.
    */
   public IEdgePresentation getEdgePresentationElement()
   {
      IPresentationElement pe = getPresentationElement();
      if (pe instanceof IEdgePresentation)
      {
         return (IEdgePresentation)pe;
      }
      return null;
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#verifyEdgeEnds()
    */
   public void verifyEdgeEnds()
   {
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine#getLineColor()
    */
   public Color getLineColor()
   {
      return this.getColor().getColor();
   }

   public int setLineColor(String resourceName, int r, int g, int b)
   {
      return setLineColor(resourceName, new Color(r, g, b));
   }

   public int setLineColor(String resourceName, Color color)
   {
      if (color != null)
      {
         m_nBorderStringID = m_ResourceUser.setResourceStringID(m_nBorderStringID, resourceName, color.getRGB());
         TSEEdgeUI ui = m_nBorderStringID >= 0 && getUI() instanceof TSEEdgeUI ? (TSEEdgeUI)getUI() : null;
         if (ui != null)
         {
            ui.setLineColor(new TSEColor(getColor(m_nBorderStringID)));
         }
      }

      return m_nBorderStringID;
   }

   /*
    * Hides the m_resourceUser lookup.
    */
   public Color getColor(int colorID)
   {
      return new Color(m_ResourceUser.getCOLORREFForStringID(colorID));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#performDeepSynch()
    */
   public long performDeepSynch()
   {
      long retVal = 0;
      try
      {
         retVal = super.performDeepSynch();

         // Since we're an edge call a routine to verify that the Target and Source
         // nodes are correct so that when we right click the parent/child relationships 
         // are correct.
         this.verifyEdgeEnds();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#postLoad()
    */
   public long postLoad()
   {

      //         //NL Currently label info is not read from the archive. As a result the labels get deleted in 
      //         // postLoadVerification because there not loaded with presentation element.
      //         // Until readFromArchive is complete, the following provides a workaround for
      //         // initializing the labels with PEs and attaching them to their parent edge model elements
      //         //
      //         ILabelManager labelMgr = getLabelManager();
      //         if (labelMgr != null)
      //         {
      //            labelMgr.resetLabels();
      //         }

      return 0;

   }


    /////////////
    // Accessible
    /////////////

    AccessibleContext accessibleContext;

    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETEdgeDrawEngine();
	} 
	return accessibleContext;
    }


    public class AccessibleETEdgeDrawEngine extends AccessibleETDrawEngine {

	public String getAccessibleDescription() {
	    if (getIEdgePresentation() != null) {
		IDrawEngine toEng = getIEdgePresentation().getEdgeToDrawEngine();
		IDrawEngine fromEng = getIEdgePresentation().getEdgeFromDrawEngine();
		
		if (toEng != null && toEng instanceof Accessible
		    && fromEng != null && fromEng instanceof Accessible) 
		{
		    AccessibleContext toCtx = ((Accessible)toEng).getAccessibleContext();
		    AccessibleContext fromCtx = ((Accessible)fromEng).getAccessibleContext();
		    if (toCtx != null && fromCtx != null) 
		    {
			// TBD!!! to resource-bundle-ize
			return getAccessibleName() 
			    + " from " + fromCtx.getAccessibleName()
			    + " to " + toCtx.getAccessibleName();
		    }
		}
	    }
	    return super.getAccessibleDescription();
	}

	public AccessibleRole getAccessibleRole() {
	    return UMLAccessibleRole.UML_EDGE;
	}


	////////////////////////////////
	// interface AccessibleComponent
	////////////////////////////////

	public java.awt.Color getForeground() {
	    return getLineColor();
	}

	public void setForeground(java.awt.Color color) {
	    ;
	}
 
    }


}
