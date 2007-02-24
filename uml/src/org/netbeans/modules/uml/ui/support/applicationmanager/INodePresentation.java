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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.editor.TSENode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

public interface INodePresentation extends IProductGraphPresentation
{
	/*
	 * Get the TS node view this presentation element represents.
	 */
	public TSENodeUI getNodeView();

	/*
	 *  Get the TS node this presentation element represents.
	 */
	public TSENode getTSNode();

	/*
	 * Get/Set the TS node this presentation element represents.
	 */
	public void setTSNode(TSENode newVal);

	/**
	 * Moves this node to the logical x and y points.  If neither the X or Y 
    * coordiate is specified for the move (via the flags parameter) then both
    * are moved.  
	 * 
    * @param flags An OR value from the MoveToFlags interface.
    *              <code>example (ie MTF_MOVEX | MTF_MOVEY | MTF_LOGICALCOORD) <code>
    *              <br>
    *              If niether <code>MTF_LOGICALCOORD</code> or 
    *              <code>MTF_DEVICECOORD</code> is spcified the call defaults 
    *              to <code>MTF_LOGICALCOORD</code>.
    * @see MoveToFlags
	 */
	public void moveTo(int x, 
                      int y,
                      int flags);


	/**
	 * Moves this node using the TS command
	 *
	 * @param x [in] The new x center
	 * @param x [in] The new y center
	 */
	public void moveTo(int x, int y);

	/*
	 * Resizes this node
	 */
	public void resize(double cx, double cy, boolean bKeepUpperLeftPoint);

	/*
	 * Resizes the node using one of the TS handles
	 */
	public void resizeByHandle( int dx, int dy, int handleLocation, boolean sendGraphEvents);

	/*
	 * Resizes the node to fit the contents
	 */
	public void sizeToContents();

   /**
    * Called to notify the node that a link has been added.
    *
    * @param edgeToBeDeleted The link about to be deleted
    * @param isFromNode <code>true</code> if this is the from node.
    */
   public void onPreDeleteLink(IEdgePresentation edgeToBeDeleted, boolean isFromNode);
   
	/*
	 * Returns the incoming and/or the outgoing edges
	 */
	public ETList<IETGraphObject> getEdges(boolean bIncoming, boolean bOutgoing);

	/*
	 *  Returns the nodes connected to this node via an edge
	 */
	public ETList<IConnectedNode> getEdgeConnectedNodes();

	/*
	 * Returns the incoming and/or the outgoing edges with pEndNodePresentation at the other end
	 */
	public ETList<IPresentationElement> getEdgesWithEndPoint(boolean bIncoming, boolean bOutgoing, INodePresentation  pEndNodePresentation);

	/*
	 * Returns the incoming and/or the outgoing edges that go out of the container this node is in.
	 */
	public ETList<IPresentationElement> getEdgesExitingContainer(boolean bIncoming, boolean bOutgoing);

	/*
	 * Returns the incoming and/or the outgoing edges by type, EdgeKind nEdgeKind
	 */
	public ETList < IETGraphObject > getEdgesByType(int nEdgeKind, boolean bIncoming, boolean bOutgoing);

	/*
	 * Returns the incoming and/or the outgoing edges that have this specific draw engine
	 */
	public ETList<IPresentationElement> getEdgesWithDrawEngine(String sDrawEngineID, boolean bIncoming, boolean bOutgoing);

	/*
	 * Returns location information for this node
	 */
	public IETRect getLocation();
	public long getHeight();
	public long getWidth();
	public IETPoint getCenter();

	/*
	 * Returns the presentation elements that are touching (or fully contained in) this nodes bounding rectangle
	 */
	public ETList<IPresentationElement> getPEsViaBoundingRect(boolean bTouchingRect);

	/*
	 * Returns the presentation elements that are touching (or fully contained in) the input rectangle
	 */
	public ETList<IPresentationElement> getPEsViaRect(boolean bTouchingRect, IETRect  pRect);

	/*
	 * Returns the first presentation element that is nearby thats of this type
	 */
	public IPresentationElement findNearbyElement(boolean bSearchOutsideOfBoundingRect,
					IElement  pElementToFind,
					String sDrawEngineTypeToFind);

	/*
	 * Returns the node presentation that is this node presentation's graphical container
	 */
	public INodePresentation getGraphicalContainer();

	/*
	 *  Resize this node so that the input node is contained within this node
	 */
	public void resizeToContain(INodePresentation  pContained);

	/*
	 * Locks editing so double clicks dont activate the edit control.
	 */
	public boolean getLockEdit();

	/*
	 *  Locks editing so double clicks dont activate the edit control.
	 */
	public void setLockEdit(boolean newVal);

}
