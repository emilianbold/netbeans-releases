/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
