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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.IteratorT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IDependency;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IUsage;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IGeneralization;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IImplementation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IRealization;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.diagramming.TSResizeControl;
import com.tomsawyer.diagramming.command.TSMoveGroupCommand;
import com.tomsawyer.drawing.TSDGraphManager;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.editor.TSEGraphManager;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEObjectUI;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.jnilayout.TSHandleLocation;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSRect;
import com.tomsawyer.drawing.geometry.TSRect;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.EdgeKindEnum;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

public class NodePresentation extends ProductGraphPresentation implements INodePresentation, MoveToFlags
{
    private TSENode mNode = null;
    
   /**
    * Creates the class element node.
    *
    * @param doc The document owner of the new node
    * @param parent The parent of the new node
    */
   public void establishNodePresence(Document doc, Node parent)
   {
      buildNodePresence("UML:NodePresentation", doc, parent);
   }

   /**
    *
    */
   public NodePresentation()
   {
      super();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getNodeView()
    */
   public TSENodeUI getNodeView()
   {
      return this.getUI() instanceof TSENodeUI ? (TSENodeUI)this.getUI() : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getTSNode()
    */
   public TSENode getTSNode()
   {
      //return this.isNode() ? (TSENode)this.getETGraphObject() : null;
       return mNode;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#setTSNode(null)
    */
   public void setTSNode(TSENode newVal)
   {
      //this.setUI(newVal != null ? (IETGraphObjectUI)newVal.getUI() : null);
       mNode = newVal;
   }
   
   public IETGraphObjectUI getUI()
   {
        TSENode node = getTSNode();
        
        IETGraphObjectUI retVal = null;
        
        if(node != null)
        {
            TSEObjectUI ui = node.getUI();
            if(ui instanceof IETGraphObjectUI)
            {
                retVal = (IETGraphObjectUI)ui;
            }
        }
        
        return retVal;
   }

   /**
    * Called to notify the node that a link has been added.
    *
    * @param edgeToBeDeleted The link about to be deleted
    * @param isFromNode <code>true</code> if this is the from node.
    */
   public void onPreDeleteLink(IEdgePresentation edgeToBeDeleted, boolean isFromNode)
   {
      IEventManager manager = getEventManager();
      if (manager != null)
      {
         manager.onPreDeleteLink(edgeToBeDeleted.getETGraphObject(), isFromNode);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#moveTo(long, long, long)
    */
   public void moveTo(int x, int y, int flags)
   {
      boolean bMoveInX = (flags & MTF_MOVEX) == MTF_MOVEX ? true : false;
      boolean bMoveInY = (flags & MTF_MOVEY) == MTF_MOVEY ? true : false;
      boolean deviceCoord = (flags & MTF_DEVICECOORD) == MTF_DEVICECOORD ? true : false;
      boolean bLogical = (flags & MTF_LOGICALCOORD) == MTF_LOGICALCOORD ? true : false;
      boolean bInvalidate = (flags & MTF_INVALIDATE) == MTF_INVALIDATE ? true : false;
      boolean bScrollView = (flags & MTF_SCROLLVIEW) == MTF_SCROLLVIEW ? true : false;

      TSENode node = getTSNode();
      IETGraphObjectUI ui = getUI();
      IDiagram diagram = getDiagram();

      if ((node != null) && (ui != null))
      {
         if (bInvalidate == true)
         {
            invalidate();
         }

         IDrawEngine engine = getDrawEngine();
         engine.onGraphEvent(IGraphEventKind.GEK_PRE_MOVE);

         //         TSConstRect oldBounds = node.getBounds();
         //         TSRect newBounds = new TSRect(oldBounds);

         // Determine the new location (Bounds).
         int moveX = x;
         int moveY = y;
         if (deviceCoord == true)
         {
            IETPoint convertedPoint = diagram.deviceToLogicalPoint(x, y);
            if (convertedPoint != null)
            {
					moveX = convertedPoint.getX();
					moveY = convertedPoint.getY();
            }
         }

         if (bMoveInX == true)
         {
            //            newBounds.setLeft(moveX);
            //            newBounds.setWidth(oldBounds.getWidth());
            node.setCenterX(moveX);
         }

         if (bMoveInY == true)
         {
            //            newBounds.setTop(moveY); 
            //            newBounds.setHeight(oldBounds.getHeight());
            node.setCenterY(moveY);
         }

         // Now check if niether bMoveInX and bMoveInY then default to 
         // moving in both the X and the Y direction.
         if ((bMoveInX == false) && (bMoveInY == false))
         {
            //            newBounds.setLeft(moveX);
            //            newBounds.setTop(moveY); 
            //            newBounds.setHeight(oldBounds.getHeight());
            //            newBounds.setWidth(oldBounds.getWidth());

            node.setCenterX(moveX);
            node.setCenterY(moveY);
         }

         //         if(bInvalidate == true)
         //         {
         //            invalidate();
         //         }

         //         IDrawEngine engine = getDrawEngine();
         //         engine.onGraphEvent(IGraphEventKind.GEK_PRE_MOVE);
         //         
         //         node.setBounds(newBounds);

         engine.onGraphEvent(IGraphEventKind.GEK_POST_MOVE);

         if (bInvalidate == true)
         {
            invalidate();
            IDrawingAreaControl control = ui.getDrawingArea();

            // Fix J961:  When moving a track car on an SQD,
            //            the presenation element must draw immediately.
            control.getGraphWindow().updateInvalidRegions(true);
         }
      }

   }

   /**
    * Moves this node using the TS command
    *
    * @param x [in] The new x center
    * @param x [in] The new y center
    */
   public void moveTo(int x, int y)
   {
      if (m_Node != null)
      {
         TSENode pTSENode = (TSENode)getTSNode();

         if (pTSENode != null)
         {
            TSEGraphManager pTSEGraphManager = (TSEGraphManager)pTSENode.getOwnerGraph().getOwnerGraphManager();

            if (pTSEGraphManager != null)
            {
               List graphs = new ArrayList();
               List nodeList = new ArrayList();

               graphs.add(pTSENode.getOwnerGraph());
               nodeList.add(pTSENode);

               TSConstPoint oldPoint = pTSENode.getCenter();
               TSConstPoint newPoint = new TSConstPoint(x, y);

               TSMoveGroupCommand cmdMove = new TSMoveGroupCommand(
                    graphs, nodeList, 
                    new ArrayList(), new ArrayList(), 
                    new ArrayList(), new ArrayList(), 
                    oldPoint, newPoint);

               pTSEGraphManager.getGraphWindow().transmit(cmdMove);
            }

         }
      }
   }

   /**
    * Redraw this node.
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#invalidate()
    */
   public void invalidate()
   {
      IETGraphObject node = getETGraphObject();

      if (node != null && node.isSelected())
      {
         IETRect bounds = new ETRectEx(node.getBounds());
         if (node.getETUI() instanceof IETNodeUI)
         {
            IETNodeUI nodeUI = (IETNodeUI)node.getETUI();           
				bounds.inflate(nodeUI.getGrappleSize() + 10);
         }
 
         IDrawEngine engine = getDrawEngine();
         if (engine != null)
         {
            engine.invalidateRect(bounds);
            return;
         }
      }
      super.invalidate();
   }

   /**
    * Returns the bounding rectangle for this node.
    * 
    * @return The bounding Rectangle
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getBoundingRect()
    */
   public IETRect getBoundingRect()
   {
   	IETGraphObject node = getETGraphObject();
	   return node != null ?  new ETRectEx(node.getBounds()) : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#resize(long, long, boolean)
    */
   public void resize(double cx, double cy, boolean bKeepUpperLeftPoint)
   {
      TSENode node = getTSNode();
      if (node != null)
      {
         if (bKeepUpperLeftPoint == false)
         {
            node.setWidth(cx);
            node.setHeight(cy);
         }
         
         else
         {
            node.setBounds(node.getLeft(), node.getTop(), node.getLeft() + cx, node.getTop() - cy);
         }

         // TS always tells the node it was resized interactively
         // when we do it programmatically we don't want that

         if (getDrawEngine() != null)
         {
            getDrawEngine().onResized();
         }

         // Update the orginal size.
         node.setOriginalSize(node.getWidth(), node.getHeight());
      }
   }
   
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#sizeToContents()
    */
   public void sizeToContents()
   {
      IDrawEngine engine = getDrawEngine();
      if (engine != null)
         engine.sizeToContents();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getEdges(boolean, boolean)
    */
   public ETList < IETGraphObject > getEdges(boolean bIncoming, boolean bOutgoing)
   {
      TSENode node = getTSNode();
      if (node != null && bIncoming && bOutgoing)
      {
         ETArrayList < IETGraphObject > edges = new ETArrayList < IETGraphObject > ();
         if (edges != null)
         {
            if (bIncoming == true)
               edges.addAll(node.inEdges());
            if (bOutgoing == true)
               edges.addAll(node.outEdges());
            return edges;
         }
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getEdgesWithEndPoint(boolean, boolean, org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation)
    */
   public ETList < IPresentationElement > getEdgesWithEndPoint(boolean bIncoming, boolean bOutgoing, INodePresentation pEndNodePresentation)
   {
      ETList < IETGraphObject > edges = getEdges(bIncoming, bOutgoing);
      if (edges != null)
      {
         try
         {

            ETList < IPresentationElement > edgesWithEndPoint = new ETArrayList < IPresentationElement > ();
            IteratorT < ETEdge > iter = new IteratorT < ETEdge > (edges);
            while (iter.hasNext())
            {
               ETEdge edge = iter.next();
               IEdgePresentation edgePresentation = (IEdgePresentation)edge.getPresentationElement();

               if (pEndNodePresentation == null
                  || (edgePresentation != null && (edgePresentation.getEdgeFromPresentationElement() == pEndNodePresentation || edgePresentation.getEdgeToPresentationElement() == pEndNodePresentation)))
               {
                  edgesWithEndPoint.add(edgePresentation);
               }
            }
            return edgesWithEndPoint;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getEdgesExitingContainer(boolean, boolean)
    */
   public ETList < IPresentationElement > getEdgesExitingContainer(boolean bIncoming, boolean bOutgoing)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getEdgesByType(int, boolean, boolean)
    */
   public ETList < IETGraphObject > getEdgesByType(int nEdgeKind, boolean bIncoming, boolean bOutgoing)
   {
      ETList < IETGraphObject > wantedElements = new ETArrayList < IETGraphObject > ();
      ETList < IETGraphObject > edges = getEdges(bIncoming, bOutgoing);

      if (edges != null)
      {

         try
         {
            IteratorT < IETGraphObject > iter = new IteratorT < IETGraphObject > (edges);

            while (iter.hasNext())
            {
               IETGraphObject edge = iter.next();
               IElement element = TypeConversions.getElement(edge);
               ;

               switch (nEdgeKind)
               {
                  case EdgeKindEnum.EK_ALL :
                     break;
                  case EdgeKindEnum.EK_REALIZATION :
                     if (!(element instanceof IRealization))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_ASSOCIATION :
                     if (!(element instanceof IAssociation))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_DEPENDENCY :
                     if (!(element instanceof IDependency))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_USAGE :
                     if (!(element instanceof IUsage))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_MESSAGE :
                     if (!(element instanceof IMessage))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_GENERALIZATION :
                     if (!(element instanceof IGeneralization))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_IMPLEMENTATION :
                     if (!(element instanceof IImplementation))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_INTERFACE :
                     if (!(element instanceof IInterface))
                     {
                        edge = null;
                     }
                     break;
                  case EdgeKindEnum.EK_NESTED_LINK :
                     IDrawEngine drawEngine = TypeConversions.getDrawEngine(edge);
                     if (drawEngine != null)
                     {
                        if (!drawEngine.getDrawEngineID().equals("NestedLinkDrawEngine"))
                        {
                           edge = null;
                        }
                     }
                     else
                     {
                        edge = null;
                     }
                     break;
               }

               if (edge != null)
               {
                  wantedElements.add(edge);
               }
            }
         }
         catch (InvalidArguments e)
         {
            e.printStackTrace();
         }
      }

      return wantedElements;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getEdgesWithDrawEngine(java.lang.String, boolean, boolean)
    */
   public ETList < IPresentationElement > getEdgesWithDrawEngine(String sDrawEngineID, boolean bIncoming, boolean bOutgoing)
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getLocation()
    */
   public IETRect getLocation()
   {
      try
      {
         return getDrawEngine().getBoundingRect();
      }
      catch (Exception e)
      {
         return null;
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getHeight()
    */
   public long getHeight()
   {
      TSENode node = getTSNode();
      return node != null ? (long)node.getHeight() : 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getWidth()
    */
   public long getWidth()
   {
      TSENode node = getTSNode();
      return node != null ? (long)node.getWidth() : 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getCenter()
    */
   public IETPoint getCenter()
   {
      TSENode node = getTSNode();
      return node != null ? new ETPoint((int)node.getCenter().getX(), (int)node.getCenter().getY()) : null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#PEsViaBoundingRect(boolean)
    */
   public ETList < IPresentationElement > getPEsViaBoundingRect(boolean bTouchingRect)
   {
   	if (this.getOwnerNode() != null)
   	{
			if (bTouchingRect)
				return buildListNodesTouchingBoundingRect();
			else
				return buildListNodesFullyContainedInBoundingRect();
   	}
   	return null;
   }

   protected ETList < IPresentationElement > buildListNodesFullyContainedInBoundingRect()
   {
      return getOwnerNode() != null ? buildListNodesViaRect(getBoundingRect(), false) : null;
   }

   protected ETList < IPresentationElement > buildListNodesTouchingBoundingRect()
   {
      return getOwnerNode() != null ? buildListNodesViaRect(getBoundingRect(), true) : null;
   }

   ETList < IPresentationElement > buildListNodesViaRect(IETRect rect, boolean bTouchingRect)
   {
      IDrawingAreaControl control = getDrawingArea();
      if (control != null)
      {
         ETList < IPresentationElement > list = control.getAllNodesViaRect(rect, bTouchingRect);

         if (list != null)
         {
            list.remove(this);
         }

         return list;
      }
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#PEsViaRect(boolean, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
    */
   public ETList < IPresentationElement > getPEsViaRect(boolean bTouchingRect, IETRect pRect)
   {
      return buildListNodesViaRect(pRect, bTouchingRect);
   }

   protected TSENode getOwnerNode()
   {
		return TypeConversions.getOwnerNode(getETGraphObject());
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#findNearbyElement(boolean, null, java.lang.String)
    */
   public IPresentationElement findNearbyElement(boolean bSearchOutsideOfBoundingRect, IElement pElementToFind, String sDrawEngineTypeToFind)
   {
      IPresentationElement foundPE = null;
      if (this.getOwnerNode() != null && getOwnerNode().getBounds() != null)
      {
         TSRect qualifierNodeRect = new TSRect(getOwnerNode().getBounds());

         TSConstPoint center = qualifierNodeRect.getCenter();
         qualifierNodeRect.setSize(qualifierNodeRect.getWidth() + 10, qualifierNodeRect.getHeight() + 10);
         qualifierNodeRect.setCenter(center);

         ETList < IPresentationElement > otherPEsWithinBoundingRect = getPEsViaBoundingRect(true);

         int count = 0;
         if (otherPEsWithinBoundingRect != null)
         {
            count = otherPEsWithinBoundingRect.getCount();
         }

         for (int index = 0; index < count; index++)
         {
            IPresentationElement thisPE = otherPEsWithinBoundingRect.item(index);
            IElement element = TypeConversions.getElement(thisPE);

            if (element != null)
            {
               if (pElementToFind.isSame(element))
               {
                  if (sDrawEngineTypeToFind != null)
                  {
                     IDrawEngine drawEngine = TypeConversions.getDrawEngine(thisPE);

                     if (drawEngine != null)
                     {
                        String sID = drawEngine.getDrawEngineID();

                        if (sID.compareTo(sDrawEngineTypeToFind) != 0)
                           continue;
                     }
                  }

                  foundPE = thisPE;
               }
            }
         }

         if (foundPE == null && bSearchOutsideOfBoundingRect)
         {
            IDrawingAreaControl control = getDrawingArea();

            if (control != null)
            {
               ETList < IPresentationElement > elementsOnDiagram = control.getAllItems2(pElementToFind);

               count = 0;

               if (elementsOnDiagram != null)
                  count = elementsOnDiagram.getCount();

               for (int index = 0; index < count; index++)
               {
                  IPresentationElement thisPE = elementsOnDiagram.item(index);
                  IElement element = TypeConversions.getElement(thisPE);

                  if (element != null)
                  {
                     if (pElementToFind.isSame(element))
                     {
                        if (sDrawEngineTypeToFind != null)
                        {
                           IDrawEngine drawEngine = TypeConversions.getDrawEngine(thisPE);
                           if (drawEngine != null)
                           {
                              String sID = drawEngine.getDrawEngineID();

                              if (sID.compareTo(sDrawEngineTypeToFind) != 0)
                                 continue;
                           }
                        }

                        foundPE = thisPE;
                     }
                  }
               }
            }
         }
      }

      return foundPE;
   }

   /**
    * Returns the node presentation that is this node presentation's graphical container
    *
    * @param pContainer [out,retval] The node presentation that is the graphical container
    *
    * @return HRESULT
    */
   public INodePresentation getGraphicalContainer()
   {
      INodePresentation pContainer = null;

      IPresentationElement cpThisPE = (IPresentationElement)this;

      if (cpThisPE != null)
      {
         ETList < IPresentationElement > cpTouchingOrContainedNodePEs = buildListNodesTouchingBoundingRect();

         if (cpTouchingOrContainedNodePEs != null)
         {
            IETRect cpRectThisBounding = getBoundingRect();

            if (cpRectThisBounding != null)
            {
               // Search the list for the "top" container
               // The list of touching nodes is sorted from bottom to top, 
               // so search the list in reverse order.

               double fMinContainerArea = 0; // FLT_MAX;

               IPresentationElement cpContainer = null;

               int lCnt = cpTouchingOrContainedNodePEs.size();

               for (int lIndx = (lCnt - 1); lIndx >= 0; lIndx--)
               {
                  IPresentationElement cpPE = cpTouchingOrContainedNodePEs.get(lIndx);

                  if (cpPE != cpThisPE)
                  {
                     IDrawEngine cpEngine = TypeConversions.getDrawEngine(cpPE);

                     if (cpEngine != null)
                     {

                        boolean bIsContainer = false;

                        bIsContainer = cpEngine.getIsGraphicalContainer();

                        if (bIsContainer)
                        {
                           IETRect cpRectBounding = cpEngine.getLogicalBoundingRect(false);

                           boolean bIsContained = false;

                           bIsContained = cpRectBounding.isContained(cpRectThisBounding);

                           if (bIsContained)
                           {
                              // The true container is the container with the smallest area
                              //Rect rectEngine;
                              //_VH(CRectConversions : : ETRectToRECT(cpRectBounding, rectEngine));

                              IETRect rectEngine = cpRectBounding;
                              //_VH(CRectConversions : : ETRectToRECT(cpRectBounding, rectEngine));

                              double fEngineArea = (double)rectEngine.getWidth() * -rectEngine.getHeight();

                              // ATLASSERT(fEngineArea > 0);

                              if (fEngineArea < fMinContainerArea)
                              {
                                 fMinContainerArea = fEngineArea;

                                 cpContainer = null;
                                 cpContainer = cpPE;
                              }
                           }
                        }
                     }
                  }
               }

               if (cpContainer != null)
               {
                  pContainer = (cpContainer instanceof INodePresentation) ? (INodePresentation)cpContainer : null;
               }
            }
         }
      }

      return pContainer;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#resizeToContain(org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation)
    */
   public void resizeToContain(INodePresentation pContained)
   {
      // TODO Auto-generated method stub

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#getLockEdit()
    */
   public boolean getLockEdit()
   {
      boolean pVal = false;
      IDrawEngine engine = this.getDrawEngine();
      INodeDrawEngine nodeEngine = (engine instanceof INodeDrawEngine) ? (INodeDrawEngine)engine : null;

      return nodeEngine != null ? nodeEngine.getLockEdit() : false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#lockEdit(boolean)
    */
   public void setLockEdit(boolean newVal)
   {
      IDrawEngine engine = this.getDrawEngine();
      INodeDrawEngine nodeEngine = (engine instanceof INodeDrawEngine) ? (INodeDrawEngine)engine : null;

      if (nodeEngine != null)
      {
         nodeEngine.setLockEdit(newVal);
      }
   }

   public ETList < IConnectedNode > getEdgeConnectedNodes()
   {
      ETList < IConnectedNode > nodes = new ETArrayList < IConnectedNode > ();

      ETList < IETGraphObject > etGraphObjects = getEdges(true, true);

      int count = etGraphObjects.getCount();

      for (int index = 0; index < count; ++index)
      {
         IETGraphObject etGraphObject = etGraphObjects.item(index);

         if (etGraphObject != null)
         {
            IEdgePresentation edgePE = TypeConversions.getEdgePresentation(etGraphObject);

            if (edgePE != null)
            {
               INodePresentation nodeAtOtherEndPresentation = edgePE.getOtherEnd(this);

               if (nodeAtOtherEndPresentation != null)
               {
                  IConnectedNode connectedNode = new ConnectedNode();
                  connectedNode.setIntermediateEdge(edgePE);
                  connectedNode.setNodeAtOtherEnd(nodeAtOtherEndPresentation);
                  nodes.add(connectedNode);
               }
            }
         }
      }

      return nodes;
   }
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation#resizeByHandle(int, int, int, boolean)
    */
   public void resizeByHandle(int dx, int dy, int handleLocation, boolean sendGraphEvents)
   {
      if (m_Node != null && (dx != 0 || dy != 0))
      {
         IDrawingAreaControl area = getDrawingArea();
         ADGraphWindow graphWindow = area.getGraphWindow();

         TSENode tseNode = (TSENode)getTSNode();

         if (tseNode != null && graphWindow != null)
         {
            IDrawEngine engine = null;
            if (sendGraphEvents)
               engine = TypeConversions.getDrawEngine(tseNode);

            if (m_Node != null && graphWindow != null)
            {
               //					graphWindow.setRedraw(false);

               TSConstRect rect = tseNode.getBounds();

               double originalX = 0;
               double originalY = 0;
               switch (handleLocation)
               {/*
                  case TSHandleLocation.TS_TOP_LEFT_HANDLE :
                     originalX = rect.getLeft();
                     originalY = rect.getTop();
                     break;

                  case TSHandleLocation.TS_TOP_CENTER_HANDLE :
                     originalX = rect.getCenter().getX();
                     originalY = rect.getTop();
                     break;

                  case TSHandleLocation.TS_TOP_RIGHT_HANDLE :
                     originalX = rect.getRight();
                     originalY = rect.getTop();
                     break;

                  case TSHandleLocation.TS_MID_LEFT_HANDLE :
                     originalX = rect.getLeft();
                     originalY = rect.getCenter().getY();
                     break;

                  case TSHandleLocation.TS_MID_RIGHT_HANDLE :
                     originalX = rect.getRight();
                     originalY = rect.getCenter().getY();
                     break;

                  case TSHandleLocation.TS_BOTTOM_LEFT_HANDLE :
                     originalX = rect.getLeft();
                     originalY = rect.getBottom();
                     break;

                  case TSHandleLocation.TS_BOTTOM_CENTER_HANDLE :
                     originalX = rect.getCenter().getX();
                     originalY = rect.getBottom();
                     break;

                  case TSHandleLocation.TS_BOTTOM_RIGHT_HANDLE :
                     originalX = rect.getRight();
                     originalY = rect.getBottom();
                     break;

                 */
                  default :
                     break;
               }

               TSResizeControl control = new TSResizeControl();
               control.init(tseNode, handleLocation);

               if (engine != null)
                  engine.onGraphEvent(IGraphEventKind.GEK_PRE_RESIZE);

               control.onStart();
               control.onDragTo(originalX + dx, originalY + dy);
               control.onDropAt(originalX + dx, originalY + dy);

               if (engine != null)
                  engine.onGraphEvent(IGraphEventKind.GEK_POST_RESIZE);
            }
         }
      }
   }

}
