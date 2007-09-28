/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
