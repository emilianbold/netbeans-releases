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

package org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETPointEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import com.tomsawyer.drawing.TSConnector;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
import org.netbeans.modules.uml.ui.swing.drawingarea.cursors.ETCreateNodeCursor;

/**
 * @author KevinM
 *  Common base class for edge tools that need to create there end nodes.
 */
public abstract class ADAddNodeEdgeTool extends ADCreateEdgeState {
	protected Cursor m_createNodeCursor = ETCreateNodeCursor.getCursor();
	protected String m_SingleClickNodeDescription = null;

	/*
	 * Abstract class, protected constructor.
	 */
	protected ADAddNodeEdgeTool() {
		super();
		loadCursors();
	}

	/// If the user does a mouseup/mousedown without a node then this node is created.  No link is created.
	public String setSingleClickNodeDescription(String singleClickNodeDescription) {
		return m_SingleClickNodeDescription = singleClickNodeDescription;
	}

	public String getSingleClickNodeDescription() {
		return m_SingleClickNodeDescription;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#onMousePressOnGraph(java.awt.event.MouseEvent)
	 */
	protected boolean onMousePressOnGraph(MouseEvent pEvent) {
		if (this.getHiddenNode() == null) {
			IETNode commentNode = createNode(getNonalignedWorldPoint(pEvent));
			if (commentNode != null) {
				this.resetState();
				//this.getGraphWindow().switchState(this);
                                this.getGraphWindow().switchTool(this);
				return false;
			}
		}
		return super.onMousePressOnGraph(pEvent);
	}

	protected IETNode createNode(TSConstPoint pt) {
		try { 
                    this.getGraphWindow().deselectAll(false);
			IETNode interfaceNode = (IETNode) this.getDrawingArea().addNode(getSingleClickNodeDescription(), new ETPointEx(pt), interactiveEdge != null ? this.interactiveEdge.isSelected() : true, false);

			if (interfaceNode != null) {
				// Fire the Events.
				try {
					ADCreateNodeState createNodeState = new ADCreateNodeState();

					createNodeState.setGraphWindow(this.getGraphWindow());
               
               // Since the node is not being created interactively we do not 
               // want to notify the drawing area.  The addNode call has taken
               // care of everything.
					createNodeState.postCreateObj(interfaceNode, false);
					createNodeState.cancelAction();
					createNodeState.stopMouseInput();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return interfaceNode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#onMousePressAddPathNode(java.awt.event.MouseEvent)
	 */
	protected boolean onMousePressAddPathNode(MouseEvent pEvent) {
		TSConstPoint pt = this.getNonalignedWorldPoint(pEvent);
		boolean connectedEdge = connectNode(createNode(pt), pt);

		return connectedEdge ? false : super.onMousePressAddPathNode(pEvent);
	}

	/**
	 * Connects the Interactive (Rubber Band Edge) to an existing comment node or one that was created.
	 * @returns true if the interactive edge was retargeted.
	 */
	protected boolean connectNode(IETNode pToNode, TSConstPoint mousePoint) {
		if (pToNode != null) {
			m_toNode = (ETNode) pToNode.getObject();
         
         ETPairT < TSConnector, Boolean > value = canConnectEdge(mousePoint);
			if (value.getParamTwo().booleanValue() == true) {
				// fires all the edge creation events
				connectEdge(value.getParamOne());

				// Remove our temp node and edge.
				deleteHiddenNode();
				resetState();
				return true;
			}
		}
		return false;
	}

	/// Returns the IComment at either the start or end of the link
	protected IElement getElement(IETNode pTargetNode, IETNode pSourceNode) {
		if (pTargetNode == null || pSourceNode == null)
			return null;

		IElement pElement = null;

		try {
			IDrawingAreaControl pControl = getDrawingArea();
			if (pControl != null) {
				IElement pToElement = TypeConversions.getElement(pTargetNode);
				IElement pFromElement = TypeConversions.getElement(pSourceNode);

				// Make sure the to and from elements aren't the same
				boolean bisSame = true;
				if (pToElement != null && pFromElement != null) {
					bisSame = pFromElement.isSame(pToElement);
				}

				if (bisSame == false) {
					String sToElementType = pToElement != null ? pToElement.getElementType() : null;
					String sFromElementType = pFromElement != null ? pFromElement.getElementType() : null;

					if (sToElementType != null && sToElementType.equals(getExpectedElementType())) {
						pElement = pToElement;
					} else if (sFromElementType != null && sFromElementType.equals(getExpectedElementType())) {
						pElement = pFromElement;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pElement;
	}

	/*
	 * Cursor api's
	 */
	protected void showCreateNodeCursor() {
		setCursor(m_createNodeCursor);
	}

	protected void showCreateEdgeCursor() {
		showCreateRelationCursor();
	}

	protected void loadCursors() {
		m_createNodeCursor = ETCreateNodeCursor.getCursor();
		setDefaultCursor(m_createNodeCursor);
	}

	protected abstract String getExpectedElementType();
}
