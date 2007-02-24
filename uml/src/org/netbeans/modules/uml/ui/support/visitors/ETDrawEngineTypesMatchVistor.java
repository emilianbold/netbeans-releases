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
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;

/**
 * @author KevinM
 * Used to build lists of drawEngines that match the type drawEngineType.
 */
public class ETDrawEngineTypesMatchVistor implements IETGraphObjectVisitor
{
	protected String engineType;
	protected ETList < IPresentationElement > presentationElements;

	public ETDrawEngineTypesMatchVistor(ETList < IPresentationElement > pPES, final String drawEngineType)
	{
		engineType = drawEngineType;
		presentationElements = pPES;
	}
	
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.visitors.IETGraphObjectVisitor#visit(org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject)
    */
   public boolean visit(IETGraphObject object)
   {
     	IDrawEngine drawEngine = object.getEngine();
		if (drawEngine != null)
		{
			String sID = drawEngine.getDrawEngineID();
			if (sID != null && sID.equals(engineType) && object.getPresentationElement() != null)
			{
				presentationElements.add(object.getPresentationElement());
			}				
		}
      return true;
   }

}
