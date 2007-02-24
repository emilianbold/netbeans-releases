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


/*
 * Created on Jan 26, 2004
 *
 */
package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductGraphPresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author jingmingm
 *
 */
public class TransformAction implements IExecutableAction, ITransformAction
{
	protected String m_NewElementType = null;
	protected IPresentationElement m_PresentationElement = null;
	
	public String getDescription()
	{
		return "TransformAction : ";
	}
	
	public String getNewElementType()
	{
		return m_NewElementType;
	}
	
	public void  setNewElementType(String newVal)
	{
		m_NewElementType = newVal;
	}
	
	public IPresentationElement get_PresentationElement()
	{
		return m_PresentationElement;
	}
	
	public void setPresentationElement(IPresentationElement newVal)
	{
		m_PresentationElement = newVal;
	}
	
	public void execute()
	{
	}
	
	public void execute(IDrawingAreaControl pControl)
	{
		pControl.setIsDirty(true);

		IElement pElement = TypeConversions.getElement(m_PresentationElement);
		IProductGraphPresentation pGraphPresentation = (IProductGraphPresentation)m_PresentationElement;

		if (pElement != null && m_NewElementType != null && m_NewElementType.length() > 0)
		{
			String sElementType = pElement.getElementType();
			if (pGraphPresentation != null)
			{
				pGraphPresentation.invalidate();
			}

			if (sElementType.equals("Aggregation") && m_NewElementType.equals("Association"))
			{
				// Going from an Aggregation to an Association
				if (pElement instanceof IAggregation)
				{
					IAggregation pAggregation = (IAggregation)pElement;
					IAssociation pCreatedItem = pAggregation.transformToAssociation();
				}
			}
			else if ((sElementType.equals("Aggregation") || sElementType.equals("Association")) &&
					(m_NewElementType.equals("Aggregation") || m_NewElementType.equals("Composite Aggregation")))
			{
				// Going from an Association to an Association (might be composite)
				if (pElement instanceof IAssociation)
				{
					IAssociation pAssociation = (IAssociation)pElement;
					IAggregation pCreatedItem = pAssociation.transformToAggregation(false);

					if (pCreatedItem != null && m_NewElementType.equals("Composite Aggregation"))
					{
						pCreatedItem.setIsComposite(true);
					}
				}
			}
			else
			{
				if (pElement instanceof IClassifier)
				{
					IClassifier pClassifier = (IClassifier)pElement;
					IClassifier pNewForm = pClassifier.transform(m_NewElementType);
				}
			}
		}
		// Update the window.
		pControl.refresh(true);
	}
}



