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



package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

/**
 * @author KevinM
 *
 */
public class ETCommentNameCompartment extends ETNameCompartment implements IADCommentNameCompartment {

	/**
	 *
	 */
	public ETCommentNameCompartment() {
		super();
	}

	/**
	 * @param pDrawEngine
	 */
	public ETCommentNameCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
	}

	public String getCompartmentID() {
		return "ADCommentNameCompartment";
	}
	
	
	public void addModelElement(IElement pElement, int nIndex) {

		try {
			
			super.addModelElement(pElement, nIndex);

			// Comments are messed up as far as the translator is concerned.  Until we
			// get more context to the translator we have to get name - otherwise we
			// always get the body from the translator
			if (pElement instanceof INamedElement) {
				INamedElement pNamedElement = (INamedElement) pElement;
				
				// Check for alias
				String sName = pNamedElement.getNameWithAlias();		
				setName(sName != null && sName.length() > 0 ? sName : "");
			}
			updateAbstractStatic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
