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
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IGraphEventKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.controls.drawingarea.UIDiagram;
import org.netbeans.modules.uml.ui.support.SynchStateKindEnum;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IEventManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

import com.tomsawyer.editor.TSENode;
import com.tomsawyer.editor.TSEEdge;

/*
 * 
 * @author KevinM
 *
 */
public abstract class ProductGraphPresentation extends GraphPresentation implements IProductGraphPresentation {

	protected int m_SynchState = SynchStateKindEnum.SSK_UNKNOWN_SYNCH_STATE;

	/**
	 * 
	 */
	public ProductGraphPresentation() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getElement()
	 */
	public IElement getElement() {
		return getModelElement();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#isModelElement(null)
	 */
	public boolean isModelElement(IElement pQueryItem) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#invalidate()
	 */
	public void invalidate() {
		if (this.getUI() != null) {
			//m_ui.getDrawEngine().invalidateRect(this.getBoundingRect());
			this.getUI().getDrawEngine().invalidate();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getName()
	 */
	public String getName() {
		return this.getUI() != null ? this.getUI().getOwner().getText() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#setName(java.lang.String)
	 */
	public void setName(String name) {
		if (this.getUI() != null)
		this.getUI().getOwner().setTag(name);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getBoundingRect()
	 */
	public IETRect getBoundingRect() {
		return this.getUI() != null ? new ETRectEx(this.getUI().getTSObject().getBounds()) : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#viewBoundingRect()
	 */
	public IETRect viewBoundingRect() {
		return this.getUI() != null ? new ETRectEx(this.getUI().getBounds()) : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getDiagram()
	 */
	public IDiagram getDiagram() {
		return this.getUI() != null ? this.getUI().getDrawEngine().getDiagram() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getSynchState()
	 */
	public int getSynchState() {
		return m_SynchState;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#setSynchState(int)
	 */
	public void setSynchState(int SynchStateKind) {
		m_SynchState = SynchStateKind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#performDeepSynch()
	 */
	public boolean performDeepSynch() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getLabelManager()
	 */
	public ILabelManager getLabelManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#getEventManager()
	 */
	public IEventManager getEventManager() {
		IDrawEngine engine = getDrawEngine();
		return engine != null ? engine.getEventManager() : null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation#selectAllLabels(boolean)
	 */
	public void selectAllLabels(boolean bSelect) {
		if (isNode()) {
			TSENode object = (TSENode) this.getUI().getOwner();
			if (object != null)
				object.setLabelsSelected(bSelect);
		} else if (isEdge()) {
			TSEEdge object = (TSEEdge) this.getUI().getOwner();
			if (object != null)
				object.setLabelsSelected(bSelect);
		}
	}

	/**
	 * Reconnects a presentation element to a new model element
	 *
	 * @param pNewModelElement [in] The new model element it should be attached to
	 */
		public boolean reconnectPresentationElement(IElement pNewModelElement) 
		{
			if (pNewModelElement != null)
			{
				if (this instanceof IPresentationElement)
				{
					IPresentationElement pThisPE = (IPresentationElement)this;
					if (pThisPE instanceof IGraphPresentation)
					{
						IGraphPresentation pGraphPresentation = (IGraphPresentation)pThisPE;
						IDrawEngine pDrawEngine = TypeConversions.getDrawEngine(pGraphPresentation);
						IDrawingAreaControl pControl = getDrawingArea();

						if (pGraphPresentation != null && pDrawEngine != null && pControl != null)
						{
							// Here's the reparenting code
							IElement pCurrentModelElement = pGraphPresentation.getFirstSubject();
							if (pCurrentModelElement != null)
							{
								// For this case the order is critical, otherwise subject is not removed
								pGraphPresentation.removeSubject(pCurrentModelElement);
								pCurrentModelElement.removePresentationElement(pGraphPresentation);
							}
							pGraphPresentation.addSubject(pNewModelElement);
							pNewModelElement.addPresentationElement(pGraphPresentation);
	
							pDrawEngine.initCompartments(pGraphPresentation);
							pDrawEngine.initResources();
	
							// Resize this node
							pDrawEngine.delayedSizeToContents();	
							// Reset the labels
							pDrawEngine.delayedDeleteAndReinitializeAllLabels();
	
							// Tell the draw engine it was reconnected so it can handle any various links
							// that it wants to reparent as well
							pDrawEngine.onGraphEvent(IGraphEventKind.GEK_NAME_COLLISION_REPARENTED);
						}
					}
				}
			}
			return true;
		}

	protected IDrawingAreaControl getDrawingArea() {
		IDiagram diagram = getDiagram();
		if (diagram != null && diagram instanceof UIDiagram) {
			UIDiagram uiDiagram = (UIDiagram) diagram;

			if (uiDiagram != null) {
				return uiDiagram.getDrawingArea();
			}
		}
		return null;
	}
}
