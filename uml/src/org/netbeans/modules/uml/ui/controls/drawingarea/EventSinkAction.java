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



package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;

/**
 * @author sumitabhk
 *
 */
public class EventSinkAction implements IEventSinkAction, IExecutableAction
{
	protected int m_Kind = EventSinkActionKind.ESAK_ELEMENTDELETED;
	protected INotificationTargets m_Targets = null;

	public EventSinkAction()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IEventSinkAction#getKind()
	 */
	public int getKind()
	{
		return m_Kind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IEventSinkAction#setKind(int)
	 */
	public void setKind(int value)
	{
		m_Kind = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IEventSinkAction#getTargets()
	 */
	public INotificationTargets getTargets()
	{
		return m_Targets;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IEventSinkAction#setTargets(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public void setTargets(INotificationTargets value)
	{
		m_Targets = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction#getDescription()
	 */
	public String getDescription()
	{
		String message = "CEventSinkAction : ";
		
		switch (m_Kind)
		{
		case EventSinkActionKind.ESAK_ELEMENTDELETED :
		   message += EventSinkActionKind.ESAK_ELEMENTDELETED;
		   break;
		case EventSinkActionKind.ESAK_ELEMENTMODIFIED :
		   message += EventSinkActionKind.ESAK_ELEMENTMODIFIED;
		   break;
		case EventSinkActionKind.ESAK_ELEMENTTRANSFORMED :
		   message += EventSinkActionKind.ESAK_ELEMENTTRANSFORMED;
		   break;
		default :
		   message += "Unknown";
		   break;
		}
		
		if (m_Targets != null)
		{
			int nKind = m_Targets.getKind();
			IElement pElement = m_Targets.getChangedModelElement();
			
			if (pElement != null)
			{
				// Output the element
				message += ",pElement=";
				message += pElement;
	
				String sElementType = pElement.getElementType();
				if (sElementType != null && sElementType.length() > 0)
				{
					message += ",pElement Type=";
					message += sElementType;
				}
	
				// Output the kind
				if (nKind == ModelElementChangedKind.MECK_ELEMENTMODIFIED)
				{
		   			message += ",nKind=MECK_ELEMENTMODIFIED";
				}
				else if (nKind == ModelElementChangedKind.MECK_TYPEMODIFIED)
				{
					message += ",nKind=MECK_TYPEMODIFIED";
				}
				else if (nKind == ModelElementChangedKind.MECK_FEATUREADDED)
				{
					message += ",nKind=MECK_FEATUREADDED";
				}
				else if (nKind == ModelElementChangedKind.MECK_ELEMENTADDEDTONAMESPACE)
				{
					message += ",nKind=MECK_ELEMENTADDEDTONAMESPACE";
				}
				else
				{
		   			message += ",nKind=";
					message += nKind;
				}
		 	}
	  	}
	  
		return message;
	}

	public void execute()
	{
	}

	public void execute(IDrawingAreaControl pControl)
	{
		if (pControl != null)
		{
			switch (m_Kind)
			{
		 		case EventSinkActionKind.ESAK_ELEMENTDELETED :
		 		{
					pControl.elementDeleted(m_Targets);
					break;
		 		}
		 		case EventSinkActionKind.ESAK_ELEMENTMODIFIED :
				{
					pControl.elementModified(m_Targets);
					break;
				}
		 		case EventSinkActionKind.ESAK_ELEMENTTRANSFORMED :
				{
					IElement pChangedME = m_Targets.getChangedModelElement();
					if (pChangedME != null && pChangedME instanceof IClassifier)
					{
						IClassifier pClassifier = (IClassifier)pChangedME;
						pControl.elementTransformed(pClassifier);
					}
					break;
				}
			}			
		}
	}
}


