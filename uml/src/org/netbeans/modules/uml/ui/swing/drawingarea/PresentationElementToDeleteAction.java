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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.DelayedAction;
import org.netbeans.modules.uml.ui.support.PresentationReferenceHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAssociationEdgePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ITSGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import com.tomsawyer.graph.TSGraphObject;

/**
 * @author KevinM
 *
 */
public class PresentationElementToDeleteAction extends DelayedAction implements IPresentationElementToDeleteAction {

	protected IPresentationElement m_presentationElement = null;
	protected TSGraphObject m_graphObject = null;

	public PresentationElementToDeleteAction(IPresentationElement pe)
	{
		super();
		setPresentationElement(pe);
	}
	
	public PresentationElementToDeleteAction(TSGraphObject graphObj)
	{
		super();
		setGraphObject(graphObj);
	}
	
	public PresentationElementToDeleteAction()
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction#getPresentationElement()
	 */
	public IPresentationElement getPresentationElement() {
		return m_presentationElement;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction#setPresentationElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void setPresentationElement(IPresentationElement pPE) {
		m_presentationElement = pPE;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction#getGraphObject()
	 */
	public TSGraphObject getGraphObject() {
		return m_graphObject;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IPresentationElementToDeleteAction#setGraphObject(null)
	 */
	public void setGraphObject(TSGraphObject graphObject) {
		m_graphObject = graphObject;

	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#getDescription()
	 */
	public String getDescription() {

		try {
			String message = new String("PresentationElementToDeleteAction : m_GraphObject=");
			message.concat(m_graphObject != null ? m_graphObject.toString() : "0");
			message.concat("m_PresentationElement=");
			message.concat(m_presentationElement != null ? m_presentationElement.toString() : "0");

			return message;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IExecutableAction#execute(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
	 */
	public void execute(IDrawingAreaControl pControl) {
		if (getPresentationElement() != null)
			deletePresentationElement(pControl, getPresentationElement());
		else if (getGraphObject() != null)
		{
			IPresentationElement pe = TypeConversions.getPresentationElement(getGraphObject());
			if (pe != null)
			{
				deletePresentationElement(pControl, pe);
			}
			else if (getGraphObject() != null)
			{
				this.deleteGraphObject(pControl, getGraphObject());
			}
		}
		// Null these guys they have been discarded.
		this.setGraphObject(null);
		this.setPresentationElement(null);
	}

	/*
	 * Deletes a presentation element and all its children.
	 */
	protected void deletePresentationElement(IDrawingAreaControl pControl, IPresentationElement pPresentationElement) {
		if (pPresentationElement != null) {
			ETList < IPresentationElement > pPEsToDelete = new ETArrayList < IPresentationElement>();

			pPEsToDelete.add(pPresentationElement);

			// Also delete any objects that are connected via the presentation reference relationship
			ETList < IPresentationElement > pChildren = PresentationReferenceHelper.getAllReferredElements(pPresentationElement);

			if (pChildren != null) {
				pPEsToDelete.addThese(pChildren);
			}

			// If this PE is attached to any qualifiers make sure to whack them as
			if (pPresentationElement instanceof IAssociationEdgePresentation) {
				IAssociationEdgePresentation  pAssociationPE = (IAssociationEdgePresentation)pPresentationElement;
				IPresentationElement pSourcePE = pAssociationPE.getSourceQualifier();
				IPresentationElement pTargetPE = pAssociationPE.getTargetQualifier();

				if (pSourcePE != null) {
					pPEsToDelete.addIfNotInList(pSourcePE);
				}

				if (pTargetPE != null) {
					pPEsToDelete.addIfNotInList(pTargetPE);
				}
			}
			
			pControl.removeElements(pPEsToDelete);
		}
	}
	
	/*
	 * Deletes a graphObject.
	 */
	protected void deleteGraphObject(IDrawingAreaControl pControl, TSGraphObject graphobj)
	{
		if (graphobj != null)
		{
			ITSGraphObject objToDelete = (ITSGraphObject)getGraphObject();
			objToDelete.delete();
		}
	}
}