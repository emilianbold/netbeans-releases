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



package org.netbeans.modules.uml.ui.support.viewfactorysupport;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.applicationmanager.IGraphPresentation;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;
import com.tomsawyer.drawing.TSLabel;
import com.tomsawyer.editor.TSEEdge;
import com.tomsawyer.editor.TSEEdgeLabel;
import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSENodeLabel;
import com.tomsawyer.editor.TSEObject;
import com.tomsawyer.editor.ui.TSEEdgeUI;
import com.tomsawyer.editor.ui.TSELabelUI;
import com.tomsawyer.editor.ui.TSENodeUI;
import com.tomsawyer.graph.TSGraphObject;
import com.tomsawyer.util.TSObject;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

public class LabelDrawEngine extends ETDrawEngine implements ILabelDrawEngine {

	protected int m_nFillStringID = -1;
	protected int m_nBorderStringID = -1;

	// This is the name of the drawengine used when storing and reading from the product archive
	public String getDrawEngineID() {
		return "LabelDrawEngine";
	}

	// Returns the parent TSLabel, if this object is a Label.
	public TSLabel getParentLabel() {
		TSLabel retValue = null;

		TSELabelUI pLabelView = this.getParentLabelView();
		if (pLabelView != null) {
			retValue = (TSLabel) pLabelView.getOwner();
		}
		return retValue;
	}

	// Returns the parent TSNodeView, if this object is a node.
	public TSELabelUI getParentLabelView() {
		IETGraphObjectUI ui = this.getParent();
		return ui instanceof TSELabelUI ? (TSELabelUI)ui : null;
	}

	/**
	 * Converts the parent TSLabelView (if it is a Label) to a TSELabelView and returns a TSELabelView*.
	 *
	 * @return The TSELabelView this draw engine is drawing
	 */
	public TSELabelUI getOwnerLabelView() {

		return this.getParentLabelView();
	}

	// Returns the owner edge label
	public TSEEdgeLabel getOwnerEdgeLabel() {

		TSEEdgeLabel retValue = null;

		if (this.getOwnerLabelView() != null) {
			TSEObject owner = this.getOwnerLabelView().getOwner();

			if (owner instanceof TSEEdgeLabel) {
				retValue = (TSEEdgeLabel)owner;
			}
		}

		return retValue;
	}

	// Returns the owner node label
	public TSENodeLabel getOwnerNodeLabel() {

		TSENodeLabel retValue = null;

		if (this.getOwnerLabelView() != null) {
			TSEObject owner = this.getOwnerLabelView().getOwner();

			if (owner != null && owner instanceof TSENodeLabel) {
				retValue = (TSENodeLabel) this.getOwnerLabelView().getOwner();
			}
		}

		return retValue;
	}

	// Returns the bounding box for the owner of the label
	public IETRect getOwnerBoundingRect() {
		IETRect retValue = null;

		if (this.getOwnerEdge() != null) {
			retValue = new ETRectEx(this.getOwnerEdge().getBounds());
		} else if (this.getOwnerNode() != null) {
			retValue = new ETRectEx(this.getOwnerNode().getBounds());
		} else {
			retValue = new ETRect(0, 0);
		}

		return retValue;
	}

	public IETRect getLogicalBoundingRect() {

		IETRect retValue = null;

		if (this.getOwnerEdgeLabel() != null) {

			retValue = new ETRectEx(this.getOwnerEdgeLabel().getBounds());

		} else if (this.getOwnerNodeLabel() != null) {

			retValue = new ETRectEx(this.getOwnerNodeLabel().getBounds());

		} else {
			retValue = new ETRect(0, 0);
		}
		return retValue;
	}

	// is the parent node currently selected?
	public boolean isSelected() {

		boolean retValue = false;

		if (this.getOwnerEdgeLabel() != null && this.getOwnerEdgeLabel().isSelected()) {

			retValue = true;

		} else if (this.getOwnerNodeLabel() != null && this.getOwnerNodeLabel().isSelected()) {

			retValue = true;
		}
		return retValue;
	}

	// Resizes the label
	public void resize(IETSize size) {

		TSEEdgeLabel pEdgeLabel = this.getOwnerEdgeLabel();
		TSENodeLabel pNodeLabel = this.getOwnerNodeLabel();

		if (pEdgeLabel != null) {
			
			pEdgeLabel.setWidth(size.getWidth());
			pEdgeLabel.setHeight(size.getHeight());

		} else if (pNodeLabel != null) {

			pNodeLabel.setWidth(size.getWidth());
			pNodeLabel.setHeight(size.getHeight());

		}
	}

	// Moves the label
	public void moveTo(double x, double y) {

		TSEEdgeLabel pEdgeLabel = this.getOwnerEdgeLabel();
		TSENodeLabel pNodeLabel = this.getOwnerNodeLabel();

		if (pEdgeLabel != null) {
			pEdgeLabel.setLocalCenter(x, y);

		} else if (pNodeLabel != null) {

			pNodeLabel.setLocalCenter(x, y);
		}

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelDrawEngine#getParentETLabel()
	 */
	// Gets the IETLabel that the TSObjectView implements (if it does)
	public IETLabel getParentETLabel() {
		IETLabel retValue = null;

		TSELabelUI pLabelView = this.getParentLabelView();

		if (pLabelView != null) {
			retValue = TypeConversions.getETLabel((TSObject) pLabelView.getOwner());
		}
		return retValue;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ETTransform#getOwnerGraphObject()
	 */
	public TSGraphObject getOwnerGraphObject() {

		TSGraphObject retValue = null;

		retValue = (TSGraphObject) this.getOwnerNodeLabel();

		if (retValue == null) {
			retValue = (TSGraphObject) this.getOwnerEdgeLabel();
		}

		return retValue;
	}

	//Returns the owner edge
	public TSEEdge getOwnerEdge() {
		TSEEdge retValue = null;

		TSEEdgeLabel pEdgeLabel = this.getOwnerEdgeLabel();

		if (pEdgeLabel != null) {

			retValue = (TSEEdge) pEdgeLabel.getOwner();
		}
		return retValue;
	}

	//Returns the owner label
	public TSENode getOwnerNode() {
		TSENode retValue = null;

		TSENodeLabel pNodeLabel = this.getOwnerNodeLabel();

		if (pNodeLabel != null) {

			retValue = (TSENode) pNodeLabel.getOwner();
		}
		return retValue;
	}

	//Returns the owner label view
	public TSENodeUI getOwnerNodeView() {

		TSENodeUI retValue = null;

		TSENode pNode = this.getOwnerNode();

		if (pNode != null) {
			retValue = pNode.getNodeUI();
		}
		return retValue;
	}

	//Returns the owner edge view
	public TSEEdgeUI getOwnerEdgeView() {

		TSEEdgeUI retValue = null;

		TSEEdge pEdge = this.getOwnerEdge();

		if (pEdge != null) {
			retValue = pEdge.getEdgeUI();
		}
		return retValue;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDiagram()
	 */
	public IDiagram getDiagram() {
		return super.getDiagram();
	}

	// Gets the IDrawEngine for the owner of this label
	public IDrawEngine getParentDrawEngine() {

		IDrawEngine retValue = null;

		ITSGraphObject pParentETElement = this.getParentETElement();

		if (pParentETElement != null) {
			retValue = pParentETElement.getETUI().getDrawEngine();
		}

		return retValue;
	}

	// When VK_DELETE is received by the diagram this function is called to 
	// affect the model, not just a deletion of a presentation element.
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#affectModelElementDeletion()
	 */
	public void affectModelElementDeletion() {
		IETLabel label = getParentETLabel();
		if (label != null) {
			// Only message connector labels should delete their model elements
			int labelKind = label.getLabelKind();
			if (labelKind == TSLabelKind.TSLK_MESSAGECONNECTOR_OPERATION_NAME) {
				super.affectModelElementDeletion();
			}
		}
	}

	// Invalidate the view object
	public long invalidate() {

		IPresentationElement pElement = this.getPresentationElement();

		IGraphPresentation pGraphPresentation = (IGraphPresentation) pElement;
		if (pGraphPresentation != null) {
			pGraphPresentation.invalidate();
		}

		return 0;
	}

	// Not used
	public void reposition() {

	}

	// Not used
	public String getText() {
		return null;
	}

	// Not used
	public void setText(String value) {

	}

	public boolean isIconLabel() {
		boolean retValue = false;

		int nLabelKind = TSLabelKind.TSLK_UNKNOWN;

		IETLabel pParentETLabel = this.getParentETLabel();

		if (pParentETLabel != null) {
			nLabelKind = pParentETLabel.getLabelKind();
		}

		if (nLabelKind == TSLabelKind.TSLK_ICON_LABEL) {
			retValue = true;
		}

		return retValue;
	}

	// IDrawingPropertyProvider override.
	public void setDrawingProperty(IDrawingProperty pProperty) {

		// call the base class
		//super.setDrawingProperty(pProperty);

		// Delay the size to contents so that CPropertyContainer::Save
		// has time to tell the compartments to inherit from this draw engine.
		//this.delayedSizeToContents();

	}

	/**
	 * This is the string to be used when creating presentation elements.
	*/
	public String getPresentationType() {
		return new String("LabelPresentation");
	}

	public void doDraw(IDrawInfo pInfo) {

	}

	// Not used
	public boolean handleLeftMouseButton(MouseEvent pEvent) {
		return false;
	}

	// Not used
	public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent) {
		return false;
	}

	// Not used
	public boolean handleRightMouseButton(MouseEvent pEvent) {
		return false;
	}

	// Not used
	public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos) {
		return false;
	}

	// Not used
	public boolean handleLeftMouseDrag(IETPoint pStartPos, IETPoint pCurrentPos) {
		return false;
	}

	// Not used
	public boolean handleLeftMouseDrop(IETPoint pCurrentPos, List pElements, boolean bMoving) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) {

		IETSize returnSize = new ETSize(0, 0);

		List compartments = getCompartments();

		if (compartments != null && compartments.size() > 0) {

			ICompartment prevCompartment = null;
			IETSize prevSize = null;

			for (Iterator iter = compartments.iterator(); iter.hasNext();) {

				ICompartment curCompartment = (ICompartment) iter.next();

				if (curCompartment != null) {
					// Since the default assumption is that
					// all the compartments are stacked top to bottom,
					// set the logical offset with the left side being zero.
					IETPoint pointLogiclaOffset = new ETPoint(0, returnSize.getHeight());
					curCompartment.setLogicalOffsetInDrawEngineRect(pointLogiclaOffset);

					// Make sure all the but last compartment have a fixed height.
					//if ((prevCompartment != null) && (prevSize != null)) 
					if (prevCompartment != null) {
						prevCompartment.setTransformSize(ICompartment.EXPAND_TO_NODE, prevSize.getHeight());
					}

					prevSize = null;
					prevSize = curCompartment.calculateOptimumSize(pDrawInfo, true);

					int maxWidth = returnSize.getWidth();

					if (prevSize != null) {
						returnSize.setSize(Math.max(maxWidth, prevSize.getWidth()), returnSize.getHeight() + prevSize.getHeight());
					}
				}

				prevCompartment = curCompartment;
			}

		} else {
			returnSize = new ETSize(40, 20);
		}
		
		TSTransform transform = pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform();
		return bAt100Pct || returnSize == null ? returnSize : scaleSize(returnSize, transform);
	}

}
