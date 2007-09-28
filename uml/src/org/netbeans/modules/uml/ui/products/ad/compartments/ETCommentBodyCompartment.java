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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingPropertyProvider;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.drawingproperties.IDrawingProperty;

/**
 * @author KevinM
 */
public class ETCommentBodyCompartment extends ETNameCompartment implements IADCommentBodyCompartment {

	public ETCommentBodyCompartment() {
		super();
		// Turn on text wrapping and centering
		setTextWrapping(true);
		setCenterText(true);
		m_singleClickSelect = false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#addModelElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement, int)
	 */
	public void addModelElement(IElement pElement, int pIndex) {
		try {
			super.addModelElement(pElement, pIndex);			
			
			String sName;

			if (pElement instanceof IComment) {
				IComment pComment = (IComment) pElement;
				sName = pComment.getBody();
			} else
				sName = null;

			setName(sName != null && sName.length() > 0 ? sName : "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#getCompartmentID()
	 */
	public String getCompartmentID() {
		return "ADCommentBodyCompartment";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) {
		IETSize retVal = null;

		if (getShowName() == false) {
			retVal = new ETSize(0, 0);
			// set size
			internalSetOptimumSize(retVal);

			// scale it for return
			retVal = getOptimumSize(bAt100Pct);
		} else {
			retVal = super.calculateOptimumSize(pDrawInfo, bAt100Pct);
		}

		return retVal;
	}

	/*
	 * Returns the String in the comment body,
	 */
	protected String getBodyText() {
		IElement element = this.getModelElement();
		if (element instanceof IComment) {
			IComment comment = (IComment) element;
			return comment.getBody();
		} else
			return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#draw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, org.netbeans.modules.uml.core.support.umlsupport.IETRect)
	 */
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect) {
		// Make sure we don't draw selected
		// this.setSelected(false); this breaks the inplace text editor.
		super.draw(pDrawInfo, pBoundingRect);	
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment#handleLeftMouseButtonDoubleClick(java.awt.event.MouseEvent)
	 */
	public boolean handleLeftMouseButtonDoubleClick(MouseEvent pEvent) {

		boolean bHandled = false;
		try {
			bHandled = super.handleLeftMouseButtonDoubleClick(pEvent);

			// is mouse over us? if so we enter in place editing mode
			if (!bHandled && isMouseInBoundingRect(pEvent, bHandled)) {
				//int x = rPoint.x - getTransform().getWinScaledOwnerRect().getLeft();
				editCompartment(false, 0, 0, pEvent.getX());
				bHandled = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bHandled;
	}

	public void initResources()
	{
		// First setup our defaults in case the colors/fonts are not in the 
		// configuration file
		setResourceID("commentfont", Color.BLACK);
		
		// Now call the base class so it can setup any string ids we haven't already set
		super.initResources();
	}

	
	public boolean handleLeftMouseBeginDrag(IETPoint pStartPos, IETPoint pCurrentPos, boolean bCancel)
	{
		boolean basedHandled = super.handleLeftMouseBeginDrag(pStartPos, pCurrentPos, bCancel);
		if (basedHandled == true && !isEditing())
		{
			return false;
		}
		return basedHandled;
	}
	
//	Fix for bug # 6318508 
	public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
	{		
		String engineID = getEngine().getDrawEngineID();
		String resourceName = "";
		IDrawingPropertyProvider pDrawingPropertyProvider = (IDrawingPropertyProvider)this;
		ETList < IDrawingProperty > pDrawingProperties = pDrawingPropertyProvider.getDrawingProperties();		
		IDrawingProperty pDrawingProperty = null;
		for (int i = 0; i < pDrawingProperties.size(); i++)
		{			
			String compID = getCompartmentID();			
			if(compID != null && compID.equals("ADCommentBodyCompartment"))
			{
				pDrawingProperty = pDrawingProperties.get(i);
				if (pDrawingProperty.getResourceType().equals("color"))
				{
					resourceName = pDrawingProperty.getResourceName();
//					break;
				}				
				super.saveColor(engineID, resourceName, nColor);				
				pDrawingPropertyProvider.invalidateProvider();
			}
		}		
	}	
	
//	Fix for bug # 6267806
	public void saveFont(  String sDrawEngineName,
						   String sResourceName,
						   String sFaceName,
						   int nHeight,
						   int nWeight,
						   boolean bItalic,
						   int nColor)
	{		
		String engineID = getEngine().getDrawEngineID();
		String resourceName = "";
		IDrawingPropertyProvider pDrawingPropertyProvider = (IDrawingPropertyProvider)this;
		ETList < IDrawingProperty > pDrawingProperties = pDrawingPropertyProvider.getDrawingProperties();		
		IDrawingProperty pDrawingProperty = null;
		for (int i = 0; i < pDrawingProperties.size(); i++)
		{			
			String compID = getCompartmentID();			
			if(compID != null && compID.equals("ADCommentBodyCompartment"))
			{
				pDrawingProperty = pDrawingProperties.get(i);
				if (pDrawingProperty.getResourceType().equals("font"))
				{
					resourceName = pDrawingProperty.getResourceName();
//					break;
				}				
				super.saveFont(engineID, resourceName, sFaceName, nHeight, nWeight, bItalic, nColor);				
				pDrawingPropertyProvider.invalidateProvider();
			}
		}		
	}

}
