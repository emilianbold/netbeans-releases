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

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;

public class ETClassNameCompartment extends ETNameCompartment implements IADClassNameCompartment 
{

	public ETClassNameCompartment() {
		super();
		this.init();
	}

	public ETClassNameCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.setFontString("Arial-bold-16");
		this.m_singleClickSelect = false;
		this.initResources();
	}

	public void initResources()
	{
		if (m_name == null)
		{		
			this.setName(PreferenceAccessor.instance().getDefaultElementName());
		}
		
		setResourceID("name", Color.BLACK);

		
		// Now call the base class so it can setup any string ids we haven't already set
		super.initResources();
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADClassNameCompartment";
	}
	

	// Name compartments do not need to be highlighted. Override this to allow the parent drawengine to be dragged
	public boolean handleLeftMouseButtonPressed(MouseEvent pEvent) {
		return false;
	}
	
	/*Added by Smitha- Fix for bug # 6253669*/
	public void addModelElement(IElement pElement, int pIndex)
	{
		super.addModelElement(pElement, pIndex);
		// As a fallback make sure we return something if it's an INamedElement - use
		// the name		
			if (pElement != null && pElement instanceof INamedElement && !(pElement instanceof ILifeline)) 
			{
				String name = ((INamedElement) pElement).getNameWithAlias();					
				if (name != null && name.length() > 0)
				{
					setName(name);
				} 
			}	
	}
	/*Added by Smitha*/
}

	