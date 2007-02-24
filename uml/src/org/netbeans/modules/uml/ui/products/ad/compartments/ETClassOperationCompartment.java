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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.primitivetypes.IVisibilityKind;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

/**
 * @author Embarcadero Technologies Inc.
 *
 *
 */
public class ETClassOperationCompartment extends ETNameCompartment implements IADClassOperationCompartment
{

	/**
	 * 
	 */
	public ETClassOperationCompartment() {
		super();
		this.init();
	}

	public ETClassOperationCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.setFontString("Arial-plain-12");
		this.setName(" ");
		this.initResources();
	}

	public void initResources() {
		this.setHorizontalAlignment(IADEditableCompartment.LEFT);
		
		setResourceID("publicoperation", Color.BLACK);
		setDefaultColor("privateoperation", Color.BLACK);
		setDefaultColor("protectedoperation", Color.BLACK);
		setDefaultColor("packageoperation", Color.BLACK );
		super.initResources();
	}

	public boolean handleLeftMouseDrag(IETPoint startPos, IETPoint currPos) {
		boolean retValue = false;
		if (this.isPointInCompartment(currPos)){
		  this.drawInsertionPoint = true;
		  retValue = true;			
		}else{
			this.drawInsertionPoint = false;			
		}
		return retValue;
	}

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID()
	{
		return "ADClassOperationCompartment";
	}
	
	public void addModelElement(IElement pElement, int nIndex)
	{
		// Call the base class first
		super.addModelElement( pElement, nIndex );

		// See if this attribute is a primary key
		if (pElement instanceof IOperation)
		{
			IOperation  pOperation = (IOperation)pElement;
			int nKind = pOperation.getVisibility();

			switch (nKind)
			{
				case IVisibilityKind.VK_PUBLIC : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("publicoperation");
				}
				break;
				case IVisibilityKind.VK_PROTECTED : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("protectedoperation");
				}
				break;
				case IVisibilityKind.VK_PRIVATE : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("privateoperation");
				}
				break;
				case IVisibilityKind.VK_PACKAGE : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("packageoperation");
				}
				break;
			}
		}
	}
	

}
