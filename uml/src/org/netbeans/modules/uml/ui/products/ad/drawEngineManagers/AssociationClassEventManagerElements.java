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


package org.netbeans.modules.uml.ui.products.ad.drawEngineManagers;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IAssociationClass;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.IEdgePresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.INodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author KevinM
 *
 */
public class AssociationClassEventManagerElements implements IBridgeElements {

	protected IAssociationClassEventManager bridgeManager;
	// Bridge Element data.
	protected IETGraphObject pSourceEdge = null;
	protected IETGraphObject pSmallNode = null;
	protected IETGraphObject pTargetEdge = null;
	protected IETGraphObject pDottedEdge = null;
	protected IETGraphObject pSourceNode = null;
	protected IETGraphObject pTargetNode = null;
	protected IETPoint pSourceNodePoint = null;
	protected IETPoint pTargetNodePoint = null;
	/**
	 * 
	 */
	public AssociationClassEventManagerElements(IAssociationClassEventManager manager) {
		super();
		bridgeManager = manager;
		if (bridgeManager != null && getBridgeMembers())
			getPoints();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getSourceEdge()
	 */
	public IETGraphObject getSourceEdge() {
		return pSourceEdge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getSmallNode()
	 */
	public IETGraphObject getSmallNode() {
		return pSmallNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getTargetEdge()
	 */
	public IETGraphObject getTargetEdge() {
		return pTargetEdge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getDottedEdge()
	 */
	public IETGraphObject getDottedEdge() {
		return pDottedEdge;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getSourceNode()
	 */
	public IETGraphObject getSourceNode() {
		return pSourceNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getSourceNodePoint()
	 */
	public IETPoint getSourceNodePoint() {
		return pSourceNodePoint;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getTargetNode()
	 */
	public IETGraphObject getTargetNode() {
		return pTargetNode;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.drawEngineManagers.IBridgeElements#getTargetNodePoint()
	 */
	public IETPoint getTargetNodePoint() {
		return pTargetNodePoint;
	}

	/*
	 * Populates call the members using the data from the manager.
	 */
	protected boolean getBridgeMembers() {
		boolean hr = true;
		try {

			// Calculate it
			if (bridgeManager.getParentETGraphObject() != null) {
				INodePresentation pAssociationNodePresentation = bridgeManager.getAssociationClassPE();
				if (pAssociationNodePresentation != null) {
					// Get all the edges and find the one that's an IAssociationEnd
					ETList < IETGraphObject > pAttachedEdges = pAssociationNodePresentation.getEdges(true, true);
					if (pAttachedEdges != null) {
						long count = pAttachedEdges.getCount();
						for (int i = 0; i < count; i++) {
							IETGraphObject pThisEdge = pAttachedEdges.item(i);
							if (pThisEdge != null) {
								IElement pElement = TypeConversions.getElement(pThisEdge);
								IAssociationClass pAssocClass = pElement instanceof IAssociationClass ? (IAssociationClass) pElement : null;
								if (pAssocClass != null) {
									// Found the small dashed line
									pDottedEdge = pThisEdge;
									break;
								}
							}
						}
					}

					// From the dashed line get the little round node
					if (pDottedEdge != null) {
						IEdgePresentation pEdgePE = TypeConversions.getEdgePresentation(pDottedEdge);
						if (pEdgePE != null) {
							IETGraphObject pFromNode = pEdgePE.getFromGraphObject();
							IETGraphObject pToNode = pEdgePE.getToGraphObject();

							if (pFromNode != null) {
								IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pFromNode);
								String sDrawEngineID = pDrawEngine != null ? pDrawEngine.getDrawEngineID() : null;

								if (sDrawEngineID != null && sDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
									// Found the dotted line
									pSmallNode = pFromNode;
								}
							}

							if (pToNode != null && pSmallNode == null) {
								IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pFromNode);
								String sDrawEngineID = pDrawEngine != null ? pDrawEngine.getDrawEngineID() : null;
								if (sDrawEngineID != null && sDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
									// Found the dotted line
									pSmallNode = pToNode;
								}
							}
						}
					}

					// Get the source and target edges defined as being association end #0 and #1.
					if (pSmallNode != null) {
						INodePresentation pNodePE = TypeConversions.getNodePresentation(pSmallNode);
						if (pNodePE != null) {
							pAttachedEdges = pNodePE.getEdges(true, true);

							if (pAttachedEdges != null) {
								long count = pAttachedEdges.getCount();
								for (int i = 0; i < count; i++) {
									IETGraphObject pThisEdge = pAttachedEdges.item(i);
									if (pThisEdge != null) {
										IElement pElement = TypeConversions.getElement(pThisEdge);
										IAssociationClass pAssocClass = pElement instanceof IAssociationClass ? (IAssociationClass) pElement : null;
										if (pAssocClass != null && pThisEdge != pDottedEdge) {
											if (pSourceEdge == null) {
												pSourceEdge = pThisEdge;
											} else if (pTargetEdge == null) {
												pTargetEdge = pThisEdge;
											}
										}
									}
								}
							}
						}
					}

					if (pSourceEdge != null) {
						IDrawEngine pFromDrawEngine = null;
						IDrawEngine pToDrawEngine = null;
						String sFromDrawEngineID = null;
						String sToDrawEngineID = null;
						IEdgePresentation pEdgePE = TypeConversions.getEdgePresentation(pSourceEdge);

						if (pEdgePE != null) {
							ETPairT < IDrawEngine, IDrawEngine > pDrawEngines = pEdgePE.getEdgeFromAndToDrawEngines();
							pFromDrawEngine = pDrawEngines != null ? pDrawEngines.getParamOne() : null;
							pToDrawEngine = pDrawEngines != null ? pDrawEngines.getParamTwo() : null;
						}
						
						if (pFromDrawEngine != null) {
							sFromDrawEngineID = pFromDrawEngine.getDrawEngineID();
						}
						
						if (pToDrawEngine != null) {
							sToDrawEngineID = pToDrawEngine.getDrawEngineID();
						}

						if (sToDrawEngineID != null && sToDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
							// The other node is the source node
							pSourceNode = TypeConversions.getETGraphObject(pFromDrawEngine);
						} else if (sFromDrawEngineID != null && sFromDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
							// The other node is the source node
							pSourceNode = TypeConversions.getETGraphObject(pToDrawEngine);
						}
					}

					if (pTargetEdge != null) {
						IDrawEngine pFromDrawEngine = null;
						IDrawEngine pToDrawEngine = null;
						String sFromDrawEngineID = null;
						String sToDrawEngineID = null;
						IEdgePresentation pEdgePE = TypeConversions.getEdgePresentation(pTargetEdge);

						if (pEdgePE != null) {
							ETPairT < IDrawEngine, IDrawEngine > pDrawEngines = pEdgePE.getEdgeFromAndToDrawEngines();
							pFromDrawEngine = pDrawEngines != null ? pDrawEngines.getParamOne() : null;
							pToDrawEngine = pDrawEngines != null ? pDrawEngines.getParamTwo() : null;
						}

						if (pFromDrawEngine != null) {
							sFromDrawEngineID = pFromDrawEngine.getDrawEngineID();
						}

						if (pToDrawEngine != null) {
							sToDrawEngineID = pToDrawEngine.getDrawEngineID();
						}

						if (sToDrawEngineID != null && sToDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
							// The other node is the source node
							pTargetNode = TypeConversions.getETGraphObject(pFromDrawEngine);
						} else if (sFromDrawEngineID != null && sFromDrawEngineID.equals("AssociationClassConnectorDrawEngine")) {
							// The other node is the source node
							pTargetNode = TypeConversions.getETGraphObject(pToDrawEngine);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hr;
	}

	private boolean getPoints() {
		return false;
	}
}
