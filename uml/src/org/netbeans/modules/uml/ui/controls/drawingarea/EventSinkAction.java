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


