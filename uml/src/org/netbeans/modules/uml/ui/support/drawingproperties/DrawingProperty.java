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
 * Created on Feb 4, 2004
 *
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationResourceMgr;

/**
 * @author jingmingm
 *
 */
public class DrawingProperty implements IDrawingProperty, Comparable
{
	protected IDrawingPropertyProvider m_DrawingPropertyProvider = null;
	protected String m_DrawEngineName = "";
	protected String m_ResourceName = "";
 
	public IDrawingPropertyProvider getDrawingPropertyProvider()
	{
		return m_DrawingPropertyProvider;
	}

	public void setDrawingPropertyProvider(IDrawingPropertyProvider newVal)
	{
		m_DrawingPropertyProvider = newVal;
	}

	public String getDrawEngineName()
	{
		return m_DrawEngineName;
	}

	public void setDrawEngineName(String newVal)
	{
		m_DrawEngineName = newVal;
	}

	public String getResourceName()
	{
		return m_ResourceName;
	}

	public void setResourceName(String newVal)
	{
		m_ResourceName = newVal;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty#getResourceType()
	 */
	public String getResourceType()
	{
		return null;
	}

	public boolean isSame(IDrawingProperty pOther)
	{
		boolean bSame = false;

		if (pOther != null)
		{
			String sDrawEngineName = pOther.getDrawEngineName();
			String sResourceName = pOther.getResourceName();
			String sResourceType = pOther.getResourceType();
			String sThisDrawEngineName = getDrawEngineName();
			String sThisResourceName = getResourceName();
			String sThisResourceType = getResourceType();

			if (sDrawEngineName.equals(sThisDrawEngineName) &&
				sResourceName.equals(sThisResourceName) &&
				sResourceType.equals(sThisResourceType))
			{
				bSame = true;
			}
		}

		return bSame;
	}

	public ETPairT<String, String> getDisplayName()
	{
		ETPairT<String, String> retVal = null;
		
		if (m_DrawEngineName.length() > 0 && m_ResourceName.length() > 0)
		{
			IPresentationResourceMgr pResourceMgr = ProductHelper.getPresentationResourceMgr();
			if (pResourceMgr != null)
			{
				retVal = pResourceMgr.getDisplayName(m_DrawEngineName, m_ResourceName);
			}
		}
		
		return retVal;
	}

	public boolean isAdvanced()
	{
		boolean bAdvanced = false;
      
		if (m_DrawEngineName.length() > 0 && m_ResourceName.length() > 0)
		{
			IPresentationResourceMgr pResourceMgr = ProductHelper.getPresentationResourceMgr();
			if (pResourceMgr != null)
			{
				bAdvanced = pResourceMgr.isAdvanced(m_DrawEngineName, m_ResourceName);
		   }
		}
		
		return bAdvanced;
	}
	
	public String toString()
	{
		String displayName = getResourceName();
		IPresentationResourceMgr pMgr = ProductHelper.getPresentationResourceMgr();
		if (pMgr != null)
		{
			// Convert that name to something more reasonable
			ETPairT<String, String> val = pMgr.getDisplayName(getDrawEngineName(), getResourceName());
			if (val != null)
			{
				displayName = val.getParamOne();
			}

			if (displayName == null || displayName.length() == 0)
			{
				displayName = getResourceName();
			}
		}
		return displayName;
	}

	public int compareTo(Object o)
   {
      return this.toString().compareTo(o.toString());
   }
}



