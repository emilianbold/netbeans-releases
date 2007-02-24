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
 * Created on Mar 3, 2004
 *
 */
package org.netbeans.modules.uml.designpattern;

//import org.netbeans.modules.uml.core.addinframework.IAddIn;
import org.netbeans.modules.uml.core.coreapplication.DesignCenterManager;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterSupport;



/**
 * @author sumitabhk
 *
 */
public class ADDesignCenterManager extends DesignCenterManager implements IADDesignCenterManager
{

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.designcenterdefaultengine.IADDesignCenterManager#getRequirementsManager()
	 */
	public Object getRequirementsManager() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.designcenterdefaultengine.IADDesignCenterManager#getDesignPatternCatalog()
	 */
	public IDesignPatternCatalog getDesignPatternCatalog()
	{
		IDesignPatternCatalog pCatalog = null;
		IDesignCenterSupport pAddin = getDesignCenterAddIn("org.netbeans.modules.uml.ui.products.ad.addesigncentergui.DesignPatternCatalog");
		if (pAddin != null && pAddin instanceof IDesignPatternCatalog)
		{
			pCatalog = (IDesignPatternCatalog)pAddin;
		}
		return pCatalog;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.products.ad.designcenterdefaultengine.IADDesignCenterManager#getMacroCatalog()
	 */
	public Object getMacroCatalog()
	{
		// TODO Auto-generated method stub
		return null;
	}

}



