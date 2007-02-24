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


/*
 * Created on Dec 16, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.Cursor;
import java.awt.Rectangle;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.drawing.TSConnector;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;

/**
 * @author jingmingm
 *
 */
public class DiagramAddPartFacadeTool extends ADAddNodeEdgeTool
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
	
	public DiagramAddPartFacadeTool()
	{
		super();
		loadCursors();
	}

        public DiagramAddPartFacadeTool(TSEGraphWindow graphEditor) 
        {
		super();
		this.setGraphWindow(graphEditor);
	}
        
	/// Verify that the found or source node is a part facade
	protected boolean verifyBothAreOfSameType(IETNode pTargetNode, IETNode pSourceNode)
	{
		if (pTargetNode != null && pSourceNode != null)
		{
			boolean bFoundIsPartFacade = isPartFacade(pTargetNode);
			boolean bSourceIsPartFacade = isPartFacade(pSourceNode);

			// If the start node is not a part facade then the end node must be a part facade,
			// and visa versa.
			if ((bSourceIsPartFacade && bFoundIsPartFacade) || (!bSourceIsPartFacade && !bFoundIsPartFacade))
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

	/// Returns true for the node if it is a PartFacade
	protected boolean isPartFacade(IETNode pNode)
	{
		return pNode != null && TypeConversions.getElement((TSNode) pNode.getObject()) instanceof IPartFacade;
	}

	/// Returns the IPartFacade at either the start or end of the link
	protected IElement getElement(IETNode pTargetNode, IETNode pSourceNode)
	{
		if (pTargetNode == null || pSourceNode == null)
			return null;

		IElement pElement = null;
		
		try
		{
			IDrawingAreaControl pControl = getDrawingArea();
			if (pControl != null)
			{
				IElement pToElement = TypeConversions.getElement(pTargetNode);
				IElement pFromElement = TypeConversions.getElement(pSourceNode);

				// Make sure the to and from elements aren't the same
				boolean bisSame = true;
				if (pToElement != null && pFromElement != null)
				{
					bisSame = pFromElement.isSame(pToElement);
				}

				if (bisSame == false)
				{
					String sToElementType = pToElement != null ? pToElement.getElementType() : null;
					String sFromElementType = pFromElement != null ? pFromElement.getElementType() : null;

					if (sToElementType != null && sToElementType.equals(getExpectedElementType()))
					{
						pElement = pToElement;
					}
					else if (sFromElementType != null && sFromElementType.equals(getExpectedElementType()))
					{
						pElement = pFromElement;
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return pElement;
 	}
  
	/// Returns the IPartFacade at either the start or end of the link
	public IPartFacade getPartFacadeElement(IETNode pTargetNode, IETNode pSourceNode)
	{
		IElement pElement = getElement(pTargetNode, pSourceNode);
		return pElement instanceof IPartFacade ? (IPartFacade) pElement : null;
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
					IPartFacade pToPartFacade = pElement instanceof IPartFacade ? (IPartFacade) pElement : null;
					IPartFacade pSourcePartFacade = pSourceElement instanceof IPartFacade ? (IPartFacade) pSourceElement : null;

					// add the edge presentation to as a child to the collaboration (either the from or to node)
					if (pToPartFacade != null)
					{
						IParameterableElement pParameterableElement = pToPartFacade instanceof IParameterableElement ? (IParameterableElement) pToPartFacade : null;
						IConnectableElement pConnectableElement = pToPartFacade instanceof IConnectableElement ? (IConnectableElement) pToPartFacade : null;
						if (pParameterableElement != null && pConnectableElement != null)
						{
							pNodePresentationElement.addElement(pEdgePresentationElement);
							
							ICollaboration pCollaboration = pSourceElement instanceof ICollaboration ? (ICollaboration) pSourceElement : null;
							if (pCollaboration != null)
							{
								pCollaboration.addTemplateParameter(pParameterableElement);
								pCollaboration.addRole(pConnectableElement);
							}
						}
					}
					else if (pSourcePartFacade != null)
					{
						IParameterableElement pParameterableElement = pSourcePartFacade instanceof IParameterableElement ? (IParameterableElement) pSourcePartFacade : null;
						IConnectableElement pConnectableElement = pSourcePartFacade instanceof IConnectableElement ? (IConnectableElement) pSourcePartFacade : null;
						if (pParameterableElement != null && pConnectableElement != null)
						{
							pSourceNodePresentationElement.addElement(pEdgePresentationElement);
							
							ICollaboration pCollaboration = pElement instanceof ICollaboration ? (ICollaboration) pElement : null;
							if (pCollaboration != null)
							{
								pCollaboration.addTemplateParameter(pParameterableElement);
								pCollaboration.addRole(pConnectableElement);
							}
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


	//	Returns the device retangle used to draw the shape.
	
	public Rectangle getIteractiveCommentDrawingRect(TSEGraphics graphics)
	{
		IETNode targetNode = this.getHiddenNode();
		if (targetNode != null) {
			TSConstRect hiddenNodeBounds = targetNode.getBounds();
			Rectangle endShapeRect =
				new Rectangle(
					graphics.getTSTransform().xToDevice(hiddenNodeBounds.getLeft()),
					graphics.getTSTransform().yToDevice(hiddenNodeBounds.getTop()),
					graphics.getTSTransform().widthToDevice((double) this.getEndNodeShapeWidth()),
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
		else if (overNode &&
			verifyBothAreOfSameType((IETNode) getObject(mousePoint), (IETNode) this.getSourceNode()) == false) 
		{
			// If the start node is not a part facade then the end node must be a part facade,
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
		 
		if (hitNode != null && getVerification() != null) {
			if (getVerification().verifyFinishNode(getNodeElement(getSourceNode()), getNodeElement(hitNode), getEdgeMetaType())) {
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
	protected boolean connectPartFacadeNode(IETNode partFacadeNode, TSConstPoint mousePoint)
	{
		if (partFacadeNode != null)
		{
			m_toNode = (ETNode)partFacadeNode.getObject();
			
			ETPairT < TSConnector, Boolean > value = canConnectEdge(mousePoint);
			if (value.getParamTwo().booleanValue() == true)
			{
				IPartFacade pPartFacade = this.getPartFacadeElement(partFacadeNode, (IETNode) this.getSourceNode());
				if (pPartFacade != null)
				{
					this.getDrawingArea().setModelElement(pPartFacade);					
					// fires all the events, reconnecting the comment to 
					connectEdge();
					annotateElement((IETEdge) this.getCreatedEdge(), (IETNode) this.getTargetNode(), (IETNode) this.getSourceNode());
					this.getDrawingArea().setModelElement(null);

					// Remove our temp node and edge.
					deleteHiddenNode();
					resetState();
					
					// Set selection
					this.getDrawingArea().selectAll(false);
					//this.getDrawingArea().getGraphWindow().selectObject(getTargetNode(), true);
					this.getDrawingArea().getGraphWindow().selectObject((ETNode)partFacadeNode.getObject(), true);
					
					return true;
				}
			}
			else
			{
				// Remove our temp node and edge.
				deleteHiddenNode();
				resetState();
			}
		}
		return false;
	}

        
        // this method is called when connecting partfacade node via keyboard
        public void doAnnotation(IETNode targetNode, IETNode sourceNode, IETEdge edge)
        {
            annotateElement(edge, targetNode, sourceNode);
        }
        
	protected boolean connectNode(IETNode pNode, TSConstPoint mousePoint)
	{
		return connectPartFacadeNode(pNode, mousePoint);
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
		if (this.getSourceNode() != null)
		{
			return connectPartFacadeNode(hitNode, mousePoint);
		}
		else
		{
			return super.doMouseClickedNode(hitNode, mousePoint);
		}

		/*
		if (isPartFacade(hitNode) && this.getHiddenNode() != null)
			return super.onMousedClickNode(hitNode, mousePoint);
		else
			return super.doMouseClickedNode(hitNode, mousePoint);
		*/
	}
	
	protected String getExpectedElementType()
	{
		return "PartFacade";
	}
}



