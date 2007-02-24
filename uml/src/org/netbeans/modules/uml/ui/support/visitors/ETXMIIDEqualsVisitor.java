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



package org.netbeans.modules.uml.ui.support.visitors;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TypeConversions;

/**
 * @author KevinM
 *
 * Search the diagram for the matching XMIID.
 */
public class ETXMIIDEqualsVisitor implements IETGraphObjectVisitor
{
	// The element found.
	protected IPresentationElement m_foundElement = null;
	
	// The XMI ID to compare with.
	protected String xmiid;
	
	/*
	 * Search the graph for the matching XMIID.
	 */
	public ETXMIIDEqualsVisitor(final String eleXMIID)
	{
		xmiid = eleXMIID;
	}
	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.visitors.IETGraphObjectVisitor#visit(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
    */
   public boolean visit(IETGraphObject object)
   {
		IPresentationElement pEle = TypeConversions.getPresentationElement(object);
 		if (pEle != null)
 		{
 			String eleXMIID = pEle.getXMIID();
			if (eleXMIID != null && eleXMIID.equals(xmiid))
			{
				m_foundElement = pEle;
				return false;
			}
 		}
 		return true;
   }
 
 	public IPresentationElement getFoundPresentation()
 	{
 		return m_foundElement;
 	}
}
