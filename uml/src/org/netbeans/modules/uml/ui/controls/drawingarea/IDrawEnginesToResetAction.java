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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDelayedAction;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

/**
 * @author jingmingm
 *
 */
public interface IDrawEnginesToResetAction extends IDelayedAction
{
	public void init(IETGraphObject pETGraphObject);
	public void init2(IETGraphObject pETGraphObject, String sNewInitString);
	public void init3(IPresentationElement pPE);
	public void init4(IPresentationElement pPE, String sNewInitString);
	public IETGraphObject getETGraphObject();
	public void setETGraphObject(IETGraphObject pETGraphObject);
	public String getNewInitString();
	public void setNewInitString(String newVal);
	public boolean getDiscoverInitString();
	public void setDiscoverInitString(boolean bDiscoverInitString);
}



