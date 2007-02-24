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
 * Created on Jan 27, 2004
 *
 */
package org.netbeans.modules.uml.ui.controls.drawingarea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ISynchStateKind;
import org.netbeans.modules.uml.ui.support.CreationFactoryHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

/**
 * @author jingmingm
 *
 */
public class DrawEnginesToResetAction implements IDrawEnginesToResetAction, IExecutableAction
{
	protected IETGraphObject m_ETGraphObject = null;
	protected String m_NewInitString ="";
	protected boolean m_DiscoverInitString = true;
	
	public void init(IETGraphObject pETGraphObject)
	{
		m_ETGraphObject = pETGraphObject;
		m_DiscoverInitString  = true;
	}
	
	public void init2(IETGraphObject pETGraphObject, String sNewInitString)
	{
		m_ETGraphObject = pETGraphObject;
		m_NewInitString  = sNewInitString;
		m_DiscoverInitString = false;
	}
	
	public void init3(IPresentationElement pPE)
	{
		m_ETGraphObject = TypeConversions.getETGraphObject(pPE);
		m_DiscoverInitString  = true;
	}
	
	public void init4(IPresentationElement pPE, String sNewInitString)
	{
		m_ETGraphObject = TypeConversions.getETGraphObject(pPE);
		m_NewInitString  = sNewInitString;
		m_DiscoverInitString = false;
	}
	
	public IETGraphObject getETGraphObject()
	{
		return m_ETGraphObject;
	}
	
	public void setETGraphObject(IETGraphObject pETGraphObject)
	{
		m_ETGraphObject = pETGraphObject;
	}
	
	public String getNewInitString()
	{
		return m_NewInitString;
	}
	
	public void setNewInitString(String newVal)
	{
		m_NewInitString = newVal;
	}
	
	public boolean getDiscoverInitString()
	{
		return m_DiscoverInitString;
	}
	
	public void setDiscoverInitString(boolean bDiscoverInitString)
	{
		m_DiscoverInitString = bDiscoverInitString;
	}

	public String getDescription()
	{
		String message = "CDrawEnginesToResetAction : m_ETGraphObject.p=" + m_ETGraphObject;

		if (m_NewInitString != null && m_NewInitString.length() > 0)
		{
		   message += ",m_NewInitString=";
		   message += m_NewInitString;
		}

		message += ",m_DiscoverInitString=";
		message += (m_DiscoverInitString)? "1" : "0";

		return message;
	}

	public void execute()
	{
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.controls.drawingarea.IExecutableAction#execute(org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl)
	 */
	public void execute(IDrawingAreaControl pControl)
	{
		if (pControl != null)
		{
			pControl.setIsDirty(true);
		}

		String sNewInitString = getNewInitString();
		boolean bDiscoverInitString = getDiscoverInitString();

		if (m_ETGraphObject != null)
		{
			if (bDiscoverInitString)
			{
				IPresentationTypesMgr pPresentationTypesMgr = CreationFactoryHelper.getPresentationTypesMgr();
				IElement pElement = TypeConversions.getElement(m_ETGraphObject);
				
				// Find the init string from the presentation types file
				if (pPresentationTypesMgr != null && pElement != null)
				{
					int nDiagramKind = pControl.getDiagramKind();
					String sInitString = pPresentationTypesMgr.getMetaTypeInitString(pElement, nDiagramKind);
					if (sInitString != null && sInitString.length() > 0)
					{
						m_ETGraphObject.resetDrawEngine(sInitString);
						m_ETGraphObject.setSynchState(ISynchStateKind.SSK_IN_SYNCH_SHALLOW);
					}
				}
			}
			else
			{
				m_ETGraphObject.resetDrawEngine(sNewInitString);
			}
		}		
	}
}



