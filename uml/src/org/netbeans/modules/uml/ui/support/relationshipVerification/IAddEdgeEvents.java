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



package org.netbeans.modules.uml.ui.support.relationshipVerification;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.common.generics.ETTripleT;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import com.tomsawyer.drawing.TSConnector;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

public interface IAddEdgeEvents {

    public IDiagram getParentDiagram();
    public void setParentDiagram(IDiagram pParentDiagram);
    public String getViewDescription();
    public void setViewDescription(String sViewDescription);

    /**
     * Fires the starting edge event out the dispatch interface.
     *
     * @param pNode [in] The node where the edge is beginning
     * @param rpoint [in] The current location of the cursor
     * @param ppConnector [out] If a connector is created it can be returned here and used by the edge create tool.
     * @param bCanceled [out] Set to TRUE to cancel the event
     */
    public ETTripleT<TSConnector, Integer, IETPoint> fireStartingEdgeEvent(IETNode pNode, TSConstPoint point);

    /**
     * Fires the creating bend event out the dispatch interface.
     *
     * @param point [in] The current location of the cursor
     * @param bAddBend [out] Set to FALSE to cancel the event
     */
    public boolean fireShouldCreateBendEvent (TSConstPoint point);

    /**
    * Fires the creating bend event out the dispatch interface.
    *
    * @param pStartNode [in] The node that is being disconnected
    * @param pNodeUnderMouse [in] The current node under the mouse
    * @param point [in] The current mouse location
    * @param bValid [in] Set to FALSE to indicate that pNodeUnderMouse is not a valid node for this edge
    */
	public boolean fireEdgeMouseMoveEvent(IETNode pStartNode,
                                                    IETNode pNodeUnderMouse,
                                                    TSConstPoint point);
	/**
	* Fires the finish edge event out the dispatch interface.
	*
	* @param pStartNode [in] The node that is beingdisconnected
	* @param pFinishNode [in] The new new
	* @param pStartConnector [in] The new connector
	* @param ppFinishConnector [out] To override the new connector pass a connector back in this variable.
	* @param point [in] The current position of the mouse
	* @param bCanceled [out] Set to FALSE to cancel the event
	*/
	public ETPairT < TSConnector, Integer > fireFinishEdgeEvent (IETNode pStartNode,
		IETNode pFinishNode,
		TSConnector pStartConnector,
		TSConstPoint point);

}
