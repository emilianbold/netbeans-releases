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



