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

import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import com.tomsawyer.drawing.TSConnector;


public interface IReconnectEdgeContext extends IEdgeEventContext
{
   /**
    * Should the reconnect create a new connector or default to pointing to the center of the node?
    * @param nReconnect ReconnectEdgeCreateConnectorKind, which is the type of reconnection
    */
   public void setReconnectConnector(int nReconnect);

   /**
    * Should the reconnect create a new connector or default to pointing to the center of the node?
    * @return ReconnectEdgeCreateConnectorKind, which is the type of reconnection
    */
   public int getReconnectConnector();

	/**
	 * The edge being reconnected.
	*/
	public IETEdge getEdge();

	/**
	 * The edge being reconnected.
	*/
	public void setEdge( IETEdge value );

	/**
	 * Indicates the target end of the edge is being reconnected
	*/
	public boolean getReconnectTarget();

	/**
	 * Indicates the target end of the edge is being reconnected
	*/
	public void setReconnectTarget( boolean value );

	/**
	 * The node that is not being reconnected.
	*/
	public IETNode getAnchoredNode();

	/**
	 * The node that is not being reconnected.
	*/
	public void setAnchoredNode( IETNode value );

	/**
	 * The node that the reconnect end is on.
	*/
	public IETNode getPreConnectNode();

	/**
	 * The node that the reconnect end is on.
	*/
	public void setPreConnectNode( IETNode value );

	/**
	 * The proposed end node.
	*/
	public IETNode getProposedEndNode();

	/**
	 * The proposed end node.
	*/
	public void setProposedEndNode( IETNode value );

	/**
	 * The coneector to attach to the edge when finished
	*/
	public TSConnector getAssociatedConnector();

	/**
	 * The coneector to attach to the edge when finished
	*/
	public void setAssociatedConnector( TSConnector value );
   
   /**
    * The logical point of the event.
    */
   public void setLogicalPoint( IETPoint point );
   
   /**
    * The logical point of the event.
    */
   public IETPoint getLogicalPoint();
}
