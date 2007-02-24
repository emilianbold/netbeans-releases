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
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAttribute;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;

public class ETClassAttributeCompartment extends ETNameCompartment implements IADClassAttributeCompartment
{

	
	public ETClassAttributeCompartment() {
		super();
		this.init();
	}

	public ETClassAttributeCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
		this.init();
	}

	private void init() {
		this.setFontString("Arial-plain-12");
		this.setName(" ");
		this.initResources();
	}

	public void initResources()
	{
		this.setHorizontalAlignment(IADEditableCompartment.LEFT);
		
		setResourceID("publicattribute", Color.BLACK);
		setDefaultColor("privateattribute", Color.BLACK);
		setDefaultColor("protectedattribute", Color.BLACK);
		setDefaultColor("packageattribute", Color.BLACK );
		
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
		return "ADClassAttributeCompartment";
	}
	
	public void addModelElement(IElement pElement, int nIndex)
	{
		// Call the base class first
		super.addModelElement( pElement, nIndex );
	
		// See if this attribute is a primary key
		if (pElement instanceof IAttribute)
		{
			IAttribute  pAttribute = (IAttribute)pElement;
			boolean bIsPrimaryKey = pAttribute.getIsPrimaryKey();
			int nKind = pAttribute.getVisibility();
	
			// Update the name if we have a primary key attribute
			String sName = pAttribute.getName();
			if (bIsPrimaryKey)
			{
				sName += "{PK}";
				setName(sName);
			}
			
			switch (nKind)
			{
				case IVisibilityKind.VK_PUBLIC : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("publicattribute");
				}
				break;
				case IVisibilityKind.VK_PROTECTED : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("protectedattribute");
				}
				break;
				case IVisibilityKind.VK_PRIVATE : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("privateattribute");
				}
				break;
				case IVisibilityKind.VK_PACKAGE : 
				{
					m_nNameFontStringID = m_ResourceUser.getResourceMgr().getStringID("packageattribute");
				}
				break;
			 }
		 }
	}


}
