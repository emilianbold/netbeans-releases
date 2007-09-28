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