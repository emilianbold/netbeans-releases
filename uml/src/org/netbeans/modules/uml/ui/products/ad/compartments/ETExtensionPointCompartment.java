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
 * Created on Dec 3, 2003
 *
 */
package org.netbeans.modules.uml.ui.products.ad.compartments;

import java.awt.Point;

import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;

/**
 * @author jingmingm
 *
 */
public class ETExtensionPointCompartment extends ETNameCompartment implements IADExtensionPointCompartment
{
	public ETExtensionPointCompartment()
	{
		super();
		this.init();
	}

	public ETExtensionPointCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.setFontString("Arial-bold-12");
		this.InitResources();
	}

	public void InitResources() {
		this.setName(PreferenceAccessor.instance().getDefaultElementName());
	}
	
	public String getCompartmentID()
	{
		return "ADExtensionPointCompartment";
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		String oldName = getName();
		if (oldName == null || oldName.length() == 0)
		{
			setName(" ");
		}
		IETSize retVal=super.calculateOptimumSize(pDrawInfo, bAt100Pct);
		setName(oldName);
		return retVal;
	}
	
	public long editCompartment(boolean bNew, int nKeyCode, int nShift, int nPos)
	{
		if (m_boundingRect.getIntWidth() == 0)
		{
			IDrawEngine pDrawEngine = this.getEngine();
			ICompartment comp = (ICompartment)((ETNodeDrawEngine)pDrawEngine).getCompartmentByKind(IADExtensionPointListCompartment.class);
			m_boundingRect = comp.getBoundingRect();
			if (m_boundingRect.getIntHeight() == 0)
			{
				m_boundingRect.setBottom(m_boundingRect.getTop() - 16);
			}
		}

		return super.editCompartment(bNew, nKeyCode, nShift, nPos);
	}
}



