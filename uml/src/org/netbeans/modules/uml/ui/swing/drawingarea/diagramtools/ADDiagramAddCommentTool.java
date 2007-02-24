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

import java.awt.Cursor;
import java.awt.Rectangle;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericEdgeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.RelationEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEdgeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * @author KevinM
 *
 */
public class ADDiagramAddCommentTool extends ADAddNodeEdgeTool
{

   // EndNodeShape types
   public static final int ENS_RECTANGLE = 0;
   public static final int ENS_ELLIPSE = 1;
   public static final int ENS_NONE = 2;

   // Data
   protected Cursor m_createNodeCursor;

   protected int m_EndNodeShape = ENS_NONE;
   protected long m_EndNodeShapeWidth = 0;
   protected long m_EndNodeShapeHeight = 0;

   protected boolean m_bMouseOverNode = false;
	protected ETGenericEdgeUI m_orginalEdgeUI = null;
	
   /**
    * 
    */
   public ADDiagramAddCommentTool()
   {
      super();
      loadCursors();
   }

   /// Verify that the found or source node is a comment
   protected boolean verifyBothAreOfSameType(IETNode pTargetNode, IETNode pSourceNode)
   {
      if (pTargetNode != null && pSourceNode != null)
      {
         boolean bFoundIsComment = isComment(pTargetNode);
         boolean bSourceIsComment = isComment(pSourceNode);

         // If the start node is not a comment then the end node must be a comment,
         // and visa versa.
         if ((bSourceIsComment && bFoundIsComment) || (!bSourceIsComment && !bFoundIsComment))
         {
            return false;
         }
         else
         {
            return true;
         }
      }
      return false;
   }

   /// Returns true for the node if it is a comment
   protected boolean isComment(IETNode pNode)
   {
      return pNode != null && TypeConversions.getElement((TSNode)pNode.getObject()) instanceof IComment;
   }

   /// Returns the IComment at either the start or end of the link
   protected IComment getCommentElement(IETNode pTargetNode, IETNode pSourceNode)
   {
      IElement pElement = getElement(pTargetNode, pSourceNode);
      return pElement instanceof IComment ? (IComment)pElement : null;
   }

   /// Annotates the correct element
   protected void annotateElement(IETEdge pCreatedEdge, IETNode pTargetNode, IETNode pSourceNode)
   {
      try
      {
         if (pCreatedEdge != null && pTargetNode != null)
         {
            IPresentationElement pEdgePresentationElement = TypeConversions.getPresentationElement(pCreatedEdge);
            IPresentationElement pNodePresentationElement = TypeConversions.getPresentationElement(pTargetNode);
            IPresentationElement pSourceNodePresentationElement = TypeConversions.getPresentationElement(pSourceNode);

            if (pEdgePresentationElement != null && pNodePresentationElement != null && pSourceNodePresentationElement != null)
            {
               IElement pElement = TypeConversions.getElement(pTargetNode);
               IElement pSourceElement = TypeConversions.getElement(pSourceNode);
               IComment pToComment = pElement instanceof IComment ? (IComment)pElement : null;
               IComment pSourceComment = pSourceElement instanceof IComment ? (IComment)pSourceElement : null;

               // add the edge presentation to as a child to the comment (either the from or to node)
               if (pToComment != null)
               {
                  pNodePresentationElement.addElement(pEdgePresentationElement);

                  INamedElement pNamedElement = pSourceElement instanceof INamedElement ? (INamedElement)pSourceElement : null;
                  if (pNamedElement != null)
                     pToComment.addAnnotatedElement(pNamedElement);
               }
               else if (pSourceComment != null)
               {
                  pSourceNodePresentationElement.addElement(pEdgePresentationElement);

                  INamedElement pNamedElement = pElement instanceof INamedElement ? (INamedElement)pElement : null;
                  if (pNamedElement != null)
                     pSourceComment.addAnnotatedElement(pNamedElement);
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public long getEndNodeShapeWidth()
   {
      return m_EndNodeShapeWidth;
   };

   public long getEndNodeShapeHeight()
   {
      return m_EndNodeShapeHeight;
   };

   public int getEndNodeShape()
   {
      return m_EndNodeShape;
   };

   /*
    * Sets the end shape sytle, width and height.
    */
   public void drawEndNodeAsEllipse(long width, long height)
   {
      m_EndNodeShape = ENS_ELLIPSE;
      m_EndNodeShapeWidth = width;
      m_EndNodeShapeHeight = height;
   }

   /*
    * Sets the end shape sytle, width and height.
    */
   public void drawEndNodeAsRectangle(long width, long height)
   {
      m_EndNodeShape = ENS_RECTANGLE;
      m_EndNodeShapeWidth = width;
      m_EndNodeShapeHeight = height;
   }

   /*
    * 
    * @author KevinM
    *
    * Extened UI that draws the end shapes.
    */
   public class ETCommentEdgeUI extends RelationEdge
   {
   	// Exposed for serialization,
   	public ETCommentEdgeUI()
   	{
   		super();
   	}
   	
      ETCommentEdgeUI(RelationEdge parentUI)
      {
         this.copy(parentUI);
      }

      public void draw(TSEGraphics graphics)
      {
         if (m_bMouseOverNode == false && getEndNodeShape() != ADDiagramAddCommentTool.ENS_NONE && getEndNodeShapeWidth() > 0 && getEndNodeShapeHeight() > 0)
         {

            Rectangle rect = getEndShapeDeviceBounds(graphics);
            // Draw the ellipse or the rect depending on our parent objects state.
            if (rect != null)
            {
               switch (getEndNodeShape())
               {
                  case ADDiagramAddCommentTool.ENS_ELLIPSE :
                     {
                        graphics.setColor(getStateColor());
                        GDISupport.frameEllipse(graphics.getGraphics(), rect);
                        break;
                     }
                  case ADDiagramAddCommentTool.ENS_RECTANGLE :
                     {
                        graphics.setColor(getStateColor());
                        GDISupport.frameRectangle(graphics.getGraphics(), rect);
                        break;
                     }
               }
            }
         }
         super.draw(graphics);
      }

      // Returns an iterface to our edge drawEngine.
      public IEdgeDrawEngine getDrawEngine()
      {
         return super.getDrawEngine() instanceof IEdgeDrawEngine ? (IEdgeDrawEngine)super.getDrawEngine() : null;
      }

      TSEColor getStateColor()
      {
         IEdgeDrawEngine drawEngine = getDrawEngine();
         return drawEngine != null ? drawEngine.getStateColor() : getLineColor();
      }

      // Returns the device retangle used to draw the shape.
      protected Rectangle getEndShapeDeviceBounds(TSEGraphics graphics)
      {
         return getIteractiveCommentDrawingRect(graphics);
      }
   } // End ETCommentEdgeUI

   // Override the base class so we can create a custom Edge UI to draw the end shape.
   protected ETGenericEdgeUI createEdgeUI()
   {
		m_orginalEdgeUI = super.createEdgeUI();
      if (m_orginalEdgeUI != null)
      {
         ETCommentEdgeUI commentEdgeUI = new ETCommentEdgeUI((RelationEdge)m_orginalEdgeUI);
         return commentEdgeUI;
      }
      else
         return m_orginalEdgeUI;
   }

   //	Returns the device retangle used to draw the shape.

   public Rectangle getIteractiveCommentDrawingRect(TSEGraphics graphics)
   {
      IETNode targetNode = this.getHiddenNode();
      if (targetNode != null)
      {
         TSConstRect hiddenNodeBounds = targetNode.getBounds();
         Rectangle endShapeRect =
            new Rectangle(
               graphics.getTSTransform().xToDevice(hiddenNodeBounds.getLeft()),
               graphics.getTSTransform().yToDevice(hiddenNodeBounds.getTop()),
               graphics.getTSTransform().widthToDevice((double)this.getEndNodeShapeWidth()),
               graphics.getTSTransform().heightToDevice(this.getEndNodeShapeHeight()));

         return endShapeRect;
      }
      return null;
   }

   // Override the base, if we are not over a node, by default to go into add node mode.
   protected void onMouseMovedShowCreateRelationCursor(TSConstPoint mousePoint, boolean overNode)
   {
      m_bMouseOverNode = overNode;

      if (getHiddenNode() == null && overNode == false)
      {
         // We haven't started creating an edge and we're not over a node.
         this.showCreateNodeCursor();
      }
      else if (getHiddenNode() == null && overNode)
      {
         // We haven't started creating an edge and we are over a node.
         showCreateRelationCursor();
      }
      else if (overNode && verifyBothAreOfSameType((IETNode)getObject(mousePoint), (IETNode)this.getSourceNode()) == false)
      {
         // If the start node is not a comment then the end node must be a comment,
         // and visa versa.
         this.showNoDropCursor();
      }
      else if (getHiddenNode() != null && overNode)
         showCreateRelationCursor();
      else
         showCreateNodeCursor();

   }

   /*
    * onVerifyMouseMove(TSConstPoint worldPt)
    */
   protected boolean onVerifyMouseMove(TSConstPoint worldPt)
   {
      boolean rc = false;
      ETNode hitNode = getObject(worldPt);

      if (hitNode != null && getVerification() != null)
      {
         if (getVerification().verifyFinishNode(getNodeElement(getSourceNode()), getNodeElement(hitNode), getEdgeMetaType()))
         {
            onMouseMovedShowCreateRelationCursor(worldPt, true);
            rc = this.fireEdgeMouseMoveEvent(hitNode, worldPt);
         }
      }
      else
         onMouseMovedShowCreateRelationCursor(worldPt, hitNode != null);
      return rc;
   }

   /**
    * Connects the Interactive (Rubber Band Edge) to an existing comment node or one that was created.
    * @returns true if the interactive edge was retargeted.
    */
   protected boolean connectCommentNode(IETNode commentNode, TSConstPoint mousePoint)
   {
      if (commentNode != null)
      {
         m_toNode = (ETNode)commentNode.getObject();

         ETPairT < TSConnector, Boolean > value = canConnectEdge(mousePoint);
         if (value.getParamTwo().booleanValue() == true)
         {
            IComment pCommentElement = this.getCommentElement(commentNode, (IETNode)this.getSourceNode());
            if (pCommentElement != null)
            {
               this.getDrawingArea().setModelElement(pCommentElement);

               
	
					// fires all the events, reconnecting the comment to
               connectEdge(value.getParamOne());
               
					// Remove the temp UI that is used to draw the ends while dragging or
					// we have serialization problems from the internal class.
					if (m_orginalEdgeUI != null && this.getCreatedEdge() != null)
					{
						getCreatedEdge().setUI(m_orginalEdgeUI);
						m_orginalEdgeUI = null;
					}
						
               annotateElement((IETEdge)this.getCreatedEdge(), (IETNode)this.getTargetNode(), (IETNode)this.getSourceNode());
               this.getDrawingArea().setModelElement(null);

               // Remove our temp node and edge.
               deleteHiddenNode();
               resetState();
               return true;
            }
         }
      }
      return false;
   }

   protected boolean connectNode(IETNode pNode, TSConstPoint mousePoint)
   {
      return connectCommentNode(pNode, mousePoint);
   }

   protected IETNode createNode(TSConstPoint pt)
   {
      return super.createNode(pt);
   }

   /*
    *  (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#onMousedClickNode(org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode, com.tomsawyer.util.TSConstPoint)
    */
   protected boolean onMousedClickNode(ETNode hitNode, TSConstPoint mousePoint)
   {
      if (isComment(hitNode) && this.getHiddenNode() != null)
         return connectCommentNode(hitNode, mousePoint);
      else
         return super.doMouseClickedNode(hitNode, mousePoint);
   }

   protected String getExpectedElementType()
   {
      return "Comment";
   }
}
