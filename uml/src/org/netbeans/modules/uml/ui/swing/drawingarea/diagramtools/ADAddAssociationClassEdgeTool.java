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

import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IAssociationClassEventManager;
import org.netbeans.modules.uml.ui.support.relationshipVerification.IEdgeVerification;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETEdge;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNode;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditorEventBlocker;
import org.netbeans.modules.uml.ui.swing.propertyeditor.PropertyEditorEventBlocker;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEGraphWindow;
import com.tomsawyer.graph.TSEdge;
import com.tomsawyer.graph.TSNode;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 * @author KevinM
 *
 */
public class ADAddAssociationClassEdgeTool extends DiagramAddEdgeTool {

	public ADAddAssociationClassEdgeTool(TSEGraphWindow graphEditor) {
		super();
		this.setGraphWindow(graphEditor);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.swing.drawingarea.diagramtools.ADCreateEdgeState#postConnectEdge()
	 */
	protected void postConnectEdge() {
		createAssociationClassBridges((IETEdge) this.getCreatedEdge());
		//super.postConnectEdge();
	}
        
        public void postConnectEdge(IETEdge edge) {
		createAssociationClassBridges(edge);
	}
        
	protected IPropertyEditorEventBlocker createPropertyEditorEventBlocker() {
		return new PropertyEditorEventBlocker(getDrawingArea()); // new PropertyEditorBlocker();
	}

	/**
	 Creates the association bridges
	 *
	 @param pCreatedEdge [in] The edge that just got created.
	 */
         protected boolean createAssociationClassBridges(IETEdge pEdge) {
		if (pEdge == null)
			return false;
		IPropertyEditorEventBlocker pBlocker = createPropertyEditorEventBlocker();
              
		boolean hr = true;
		try {
			IDrawingAreaControl pControl = this.getDrawingArea();
			IDiagram pAxDiagram = pControl != null ? pControl.getDiagram() : null;

			// Create the property editor blocker which will block property editor events
			TSEEdge pCreatedEdge = pEdge instanceof TSEdge ? (TSEEdge) pEdge : null;

			pBlocker.disableEvents();
			// This is the initial edge created for the association class.  We need to make this
			// edge invisible and create a 3 new edges, 1 little node, and the association class
			//assert (pCreatedEdge &pControl &pBlocker);
			if (pControl != null && pCreatedEdge != null) {

				//CBlockFocusLoss focusBlocker(pGraphDisplay());
				IETPoint smallNodePoint;
				IETPoint associationClassPoint;

				// Get the from and to points
				TSConstPoint sourcePoint = pCreatedEdge.getSourcePoint();
				TSConstPoint targetPoint = pCreatedEdge.getTargetPoint();

				IETPoint pETPoint = pControl.getMidPoint(pCreatedEdge);
				//Point midPoint = PointConversions.ETPointToPoint(pETPoint);
				smallNodePoint = pETPoint;

				if (Math.abs(sourcePoint.getX() - targetPoint.getX()) > Math.abs(sourcePoint.getY() - targetPoint.getY())) {
					// horizontal alignment
					associationClassPoint = smallNodePoint;
					associationClassPoint.setY(associationClassPoint.getY() - 50);
				} else {
					// This is a virtical alignment
					associationClassPoint = smallNodePoint;
					associationClassPoint.setX(associationClassPoint.getX() - 50);
				}

				TSNode pAssociationClassifier = null;
				IETPoint associationClassCPoint = associationClassPoint;

				// Create the association
				IEdgeVerification pEdgeVerif = this.getVerification();
				IAssociationClass pCreatedAssociationClass = null;
				IETNode pSourceNode = pEdge.getFromNode();
				IETNode pTargetNode = pEdge.getToNode();

				if (pSourceNode != null && pTargetNode != null) {
					pCreatedAssociationClass = pEdgeVerif.createAssociationClassifierRelation(pSourceNode, pTargetNode);
				}

				if (pCreatedAssociationClass != null) {

					// Create the association class
					// Set the model element on the diagram so the post add logic will attach to this correctly
					// created IAssociationClass rather then create a new one.

					pControl.setModelElement(pCreatedAssociationClass);
					IETPoint pETLocation = associationClassCPoint;
					//PointConversions.newETPoint(associationClassCPoint);
					pAssociationClassifier = pControl.addNode("org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI AssociationClass", pETLocation, false, false);
					pControl.setModelElement(null);

					if (pAssociationClassifier != null) {
						IDrawEngine pEngine = TypeConversions.getDrawEngine(pAssociationClassifier);

						// Get the event manager and create the bridges

						IEventManager pEventManager = pEngine != null ? pEngine.getEventManager() : null;

						if (pEventManager instanceof IAssociationClassEventManager) {
							IAssociationClassEventManager pAssocClassEventManager = (IAssociationClassEventManager) pEventManager;
							// Create the rest of the bridges
							pAssocClassEventManager.createBridges((IETEdge)pCreatedEdge);
						}
					}
				}
			}
			pBlocker.enableEvents();
		} catch (Exception e) {
			e.printStackTrace();

			// Don't leave the property editor locked.
			if (pBlocker != null) {
				pBlocker.enableEvents();
			}
		}
		// Always return true from this routine so we don't throw and mess up the
		// tool state processing.
		return true;
	}
}
