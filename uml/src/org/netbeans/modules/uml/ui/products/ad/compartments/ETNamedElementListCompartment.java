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


//	 $Date$
package org.netbeans.modules.uml.ui.products.ad.compartments;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

public class ETNamedElementListCompartment extends ETListCompartment implements IADNamedElementListCompartment {

	public ETNamedElementListCompartment() {
		super();
	}

	public ETNamedElementListCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
	}

	/**
	 * Initialize this compartment, make it visible, then edit
	 *
	 * @param pParentDrawEngine [in] The parent drawengine for this compartment
	 * @param pElement [in] The IElement belonging to the compartment pCompartment
	 * @param pCompartment [in] The newly created compartment
	 * @param bRedrawNow [in] true to redraw the engine now
	 */
	public void finishAddCompartment(IDrawEngine pParentDrawEngine, IElement pElement, 
									 ICompartment pCompartment, boolean bRedrawNow)
	{
		// attach transition to the compartment
		pCompartment.addModelElement(pElement, -1);
		
		// pumping message allows RT to fire
		pumpMessages();
		
		// force re-paint after insertion
		if (bRedrawNow)
		{
			// clear all selected compartments
			pParentDrawEngine.selectAllCompartments(false);
			
			// select the new compartment
			pCompartment.setSelected(true);
			
			// make sure it's visible and anchored
			ensureVisible(pCompartment, true);
			pParentDrawEngine.setAnchoredCompartment(pCompartment);
			
			// this causes the message pump to run, otherwise the following call to the edit control will
			// load over dead air
			redrawNow();
		}
		
		// edit the compartment
		if (pCompartment instanceof IADNameCompartment)
		{
			pCompartment.editCompartment(true, 0, 0, 0);
		}
		
		// notify drawing it need updating
		setIsDirty();
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADNamedElementListCompartment";
	}
}
