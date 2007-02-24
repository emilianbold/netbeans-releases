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

import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.editor.TSEEdge;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.IReconnectEdgeContext;


public interface IEdgePresentation extends IProductGraphPresentation
{
	/*
	 *get the TS edge view this presentation element represents.
	 */
	public TSEEdgeUI getEdgeUI();

	/*
	 * get/Set the TS edge this presentation element represents.
	 */
	public TSEEdge getTSEdge();

	/*
	 * get/Set the TS edge this presentation element represents.
	 */
	public void setTSEdge(TSEEdge newVal);

	/*
	 * Discards all the bends.
	 */
	public void discardAllBends();

	/*
	 * Removes all the connectors.
	 */
	public boolean removeConnectors();

	/*
	 * Returns the from and to node..
	 */
	public ETPairT<IETGraphObject, IETGraphObject> getEdgeFromAndToNode();

	/**
	 * Returns the from node.
	 */
	public IElement getFromNode();

   /** 
    * Retrieves the graph object that edge originates from.
    */
   public IETGraphObject getFromGraphObject();
      
	/**
	 * Returns the to node
	 */
	public IElement getToNode();

   /** 
    * Retrieves the graph object that edge originates from.
    */
   public IETGraphObject getToGraphObject();
   
	/**
	 * Returns the from and to node if the element is an edge.
	 */
	public ETPairT<INodePresentation, INodePresentation> getEdgeFromAndToPresentationElement();

	/**
	 * Returns the from node if the eleement is an edge.
	 */
	public INodePresentation getEdgeFromPresentationElement();

	/**
	 * Returns the to node if the eleement is an edge.
	 */
	public INodePresentation getEdgeToPresentationElement();

	/**
	 * Returns the from and to element.  If bUseProjectData is true then we use the project data,
	 * otherwise we get the elements off the nodes.  They could be different if the diagram is
	 * not synched with the model
	 */
	public ETPairT<IElement, IElement> getEdgeFromAndToElement(boolean bUseProjectData);

	/*
	 * Returns the from element.  If bUseProjectData is true then we use the project data,
	 * otherwise we get the elements off the nodes.  They could be different if the diagram is
	 * not synched with the model
	 */
	public IElement getEdgeFromElement(boolean bUseProjectData);

	/*
	 * Returns the from element.  If bUseProjectData is true then we use the project data,
	 * otherwise we get the elements off the nodes.  They could be different if the diagram is
	 * not synched with the model
	 */
	public IElement getEdgeToElement(boolean bUseProjectData);

	/*
	 * Returns the from element.  Gets the elements off the nodes.  They could be different if the diagram is
	 * not synched with the model
	 */
	public IElement getEdgeFromElement(); /* boolean bUseProjectData = false */

	/*
	 * Returns the from element. Gets the elements off the nodes.  They could be different if the diagram is
	 * not synched with the model
	 */
	public IElement getEdgeToElement();  /* boolean bUseProjectData = false */

	/*
	 * Returns the from and to draw engines.
	 */
	public ETPairT<IDrawEngine, IDrawEngine> getEdgeFromAndToDrawEngines();

	/*
	 * Returns the from draw engine.
	 */
    public IDrawEngine getEdgeFromDrawEngine();

	/*
	 * Returns the to draw engine.
	 */
	public IDrawEngine getEdgeToDrawEngine();

	/*
	 * Returns the from and to draw engines if they have the specific ids.
	 */
	public ETPairT<IDrawEngine, IDrawEngine> getEdgeFromAndToDrawEnginesWithID(String sDrawEngineID);

	/*
	 *  Returns the from draw engine if they have the specific id.
	 */
	public IDrawEngine getEdgeFromDrawEngineWithID(String sDrawEngineID);

	/*
	 *  Returns the from draw engine if they have the specific id.
	 */
	public IDrawEngine getEdgeToDrawEngineWithID(String sDrawEngineID);

	/*
	 * Returns the node at the other end.
	 */
	public INodePresentation getOtherEnd(INodePresentation pOneNode);

	/*
	 * Is this item the from or to node, NodeEndKind
	 */
	public int getNodeEnd(IElement pElement);

	/*
	 * Is this item the from or to node, NodeEndKind
	 */
	public int getNodeEnd2(IAssociationEnd pAssociationEnd);

	/*
	 * Is this item the from or to node, NodeEndKind
	 */
	public int getNodeEnd3(String elementXMIID);
	
	/*
	 * Validate the edge ends.
	 * */
	public boolean validateLinkEnds();

	/*
	 * Try to reconnect the link to valid objects, return true if we successfully reconnected
	 * */
	public boolean reconnectLinkToValidNodes();

	/*
	 * Try to reconnect the link from pOldNode to pNewNode.  The context is IReconnectEdgeContext.
	 * */
	public boolean reconnectLink(IReconnectEdgeContext pContext);

	/*
	 * Autoroutes this edge using the current layout style.
	 * If bResetRoutingStyle is true then routine style will be set to RSK_AUTOMATIC
	 * */
	public boolean autoRoute(boolean bResetRoutingStyle);

//	/*
//	 * Returns the opposite routing style.  Used when toggling the routing style. RoutingStyleKind
//	 * */
//	public int getOppositeRoutingStyle();

	/*
	 * Returns the node nearest the point, including its distance from that node
	 * */
	public ETPairT<INodePresentation, Integer> getNodeNearestPoint(IETPoint pPoint);

//	/*
//	 * gets the routing style of this link
//	 * */
//	public int getRoutingStyle();
//
//	/*
//	 * Sets the routing style of this link RoutingStyleKind
//	 * */
//	public void setRoutingStyle(int nKind);

	/**
	 * Updates the reconnect connector flag on the context
	 */
	public void setReconnectConnectorFlag(IReconnectEdgeContext pContext);
}
