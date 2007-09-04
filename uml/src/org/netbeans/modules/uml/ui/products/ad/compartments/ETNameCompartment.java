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

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETNode;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.ui.TSEAnnotatedUI;
import com.tomsawyer.util.TSProperty;
import com.tomsawyer.drawing.geometry.TSConstRect;
import com.tomsawyer.editor.TSTransform;

public class ETNameCompartment extends ETEditableCompartment implements IADNameCompartment {

        boolean drawInsertionPoint = false;

	public ETNameCompartment() {
		super();
	}

	public ETNameCompartment(IDrawEngine pDrawEngine) {
		super(pDrawEngine);
	}

	protected void drawMultiline(IDrawInfo pDrawInfo, IETRect pBoundingRect, final String value) {
		if (value == null || value.length() == 0)
			return;

		IDrawEngine drawEngine = this.getEngine();
		if (!drawEngine.getParent().getTSObject().isNode())
			return;	// We only support nodes.
			
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		ETGenericNodeUI parentUI = (ETGenericNodeUI) drawEngine.getParent();
		IETGraphObject node = (IETGraphObject) parentUI.getTSObject();
		parentUI.setFormattingEnabled(true);
		ETNode etNode = (ETNode) node;

                TSConstRect logicalRect = pDrawInfo.getTSTransform().boundsToWorld(pBoundingRect.getRectangle());
                TSConstRect orginalBounds = etNode.getBounds();
                
                // Change the node bounds to wrap the compartment, so multiline text works.
                etNode.setBounds(logicalRect);
                node.setText(value);
                
                TSTransform transform = graphics.getTSTransform();
                
                // set the color of the pen to the text color
                graphics.setColor(parentUI.getTextColor());
                
                // if the scale used is 100% we do not need to reset the font
                Font originalFont = graphics.getFont();
                
                // Compartment specific font
                Font compartmentFont = getCompartmentFont(1.0);
                graphics.setFont(compartmentFont);
                parentUI.setFont(new TSEFont(compartmentFont));


		if (this.isSelected()) {
			graphics.setColor(TSEColor.darkBlue);
			graphics.fillRect(pBoundingRect.getIntX(), pBoundingRect.getIntY() + 2, pBoundingRect.getIntWidth(), pBoundingRect.getIntHeight());
			graphics.setColor(TSEColor.white);
		} else {
			graphics.setColor(getCompartmentFontColor());
		}

		if (this.drawInsertionPoint) {
			graphics.drawRect(pBoundingRect.getIntX(), pBoundingRect.getIntY(), pBoundingRect.getIntWidth(), 1);
			this.drawInsertionPoint = false;
		}

		int left = 0;
		if (this.getHorizontalAlignment() == IADCompartment.CENTER && graphics.getFontMetrics() != null) {
			left = (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (graphics.getFontMetrics().stringWidth(value) / 2);
		} else {
			left = pBoundingRect.getIntX() + 2;
		}

		int top = 0;

		if (this.getVerticalAlignment() == IADCompartment.CENTER && graphics.getFontMetrics() != null) {
			top = pBoundingRect.getIntY() + graphics.getFontMetrics().getHeight() - 2;
		} else {
			top = pBoundingRect.getIntY() + (int) (pBoundingRect.getIntHeight());
		}
		
		//String formatted = parentUI.getFormattedText();
		//Fix for bug # 6318508
		TSEColor tsColor = new TSEColor(graphics.getColor());
		parentUI.setProperty(new TSProperty(TSEAnnotatedUI.TEXT_COLOR, tsColor));
		parentUI.drawText(graphics);
		//graphics.drawString(formatted, left, top);
		etNode.setBounds(orginalBounds);
	}

	protected void drawSingleLine(IDrawInfo pDrawInfo, IETRect pBoundingRect, final String value)
	{
		if (value == null || value.length() == 0)
			return;

		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		TSTransform transform = graphics.getTSTransform();

		// if the scale used is 100% we do not need to reset the font
		Font originalFont = graphics.getFont();

		// Compartment specific font
		Font compartmentFont = getCompartmentFont(pDrawInfo.getFontScaleFactor());
		graphics.setFont(compartmentFont);

                // 114303, below logic seems unnecessary
//		if (this.isSelected()) {
//			graphics.setColor(TSEColor.darkBlue);
//			graphics.fillRect(pBoundingRect.getIntX(), pBoundingRect.getIntY() + 2, pBoundingRect.getIntWidth(), pBoundingRect.getIntHeight());
//			graphics.setColor(TSEColor.white);
//		} else {
			graphics.setColor(getCompartmentFontColor());
//		}

		if (this.drawInsertionPoint) {
			graphics.drawRect(pBoundingRect.getIntX(), pBoundingRect.getIntY(), pBoundingRect.getIntWidth(), 1);
			this.drawInsertionPoint = false;
		}

		int left = 0;
		if (this.getHorizontalAlignment() == IADCompartment.CENTER && graphics.getFontMetrics() != null) {
			left = (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (graphics.getFontMetrics().stringWidth(value) / 2);
		} else {
			left = pBoundingRect.getIntX() + 2;
		}

		int top = 0;

		if (this.getVerticalAlignment() == IADCompartment.CENTER && graphics.getFontMetrics() != null) {
			top = pBoundingRect.getIntY() + graphics.getFontMetrics().getHeight() - 2;
		} else {
			top = pBoundingRect.getIntY() + (int) (pBoundingRect.getIntHeight());
		}

		AttributedString text = new AttributedString(value, compartmentFont.getAttributes());
		
		if (m_bIsStatic)
		{
			text.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
		}

		if (m_bIsAbstract)
		{
			text.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
		}


		AttributedCharacterIterator iter = text.getIterator();

		graphics.drawString(iter, left, top);
		graphics.setFont(originalFont);
		
	}
	
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect) {
            super.draw(pDrawInfo, pBoundingRect);
            
            if (this.getTextWrapping()) {
                //Jyothi: Turn the TS events off before drawMultiline call and turn it back on after.. to avoid the stackoverflow exception. Fix for Bug#6263255
                TSEGraphics graphics = pDrawInfo.getTSEGraphics();     
                graphics.getGraphWindow().getGraph().setFireEvents(false);
                graphics.getGraphWindow().getGraphManager().getEventManager().setCoalescingPermanentlyDisabled(true);
                
                this.drawMultiline(pDrawInfo, pBoundingRect, getName());
                graphics.getGraphWindow().getGraph().setFireEvents(true);
                graphics.getGraphWindow().getGraphManager().getEventManager().setCoalescingPermanentlyDisabled(false); 
                
            } else {
                this.drawSingleLine(pDrawInfo, pBoundingRect, getName());
            }
        }

	/**
	 * This is the name of the drawengine used when storing and reading from the product archive.
	 *
	 * @param sID[out,retval] The unique name for this compartment.  Used when reading and writing the
	 * product archive (etlp file).
	 */
	public String getCompartmentID() {
		return "ADNameCompartment";
	}

	/**
	 * Adds a model element to this compartment.
	 *
	 * @param pElement [in] The model element to be added
	 * @param nIndex [in] Where should the new compartment be created in the list of the current compartments?
	 */
	public void addModelElement(IElement pElement, int pIndex) {
		super.addModelElement(pElement, pIndex);

		// As a fallback make sure we return something if it's an INamedElement - use
		// the name
		String name = getName();
		if (name == null || name.length() == 0) {
			if (pElement != null && pElement instanceof INamedElement) {
				name = ((INamedElement) pElement).getNameWithAlias();
				if (name != null && name.length() > 0) {
					setName(name);
				} else {
					setName("");
				}
			}
		}

		updateAbstractStatic();
	}

	protected void updateAbstractStatic() {
		IElement modEle = getModelElement();
		boolean isStatic = false;
		boolean isAbstract = false;

		if (modEle != null) {
			if (modEle instanceof IClassifier) {
				isAbstract = ((IClassifier) modEle).getIsAbstract();
			} else {
				if (modEle instanceof IBehavioralFeature) {
					isAbstract = ((IBehavioralFeature) modEle).getIsAbstract();
				}
			}

			if (modEle instanceof IFeature) {
				isStatic = ((IFeature) modEle).getIsStatic();
			}

			if (m_bIsStatic != (isStatic == true)) {
				m_bIsStatic = (isStatic == true);
			}

			if (m_bIsAbstract != (isAbstract == true)) {
				m_bIsAbstract = (isAbstract == true);
			}

		}
	}

	public void initResources() {
		// First setup our defaults in case the colors/fonts are not in the 
		// configuration file
		m_nNameFontStringID = m_ResourceUser.setResourceStringID(m_nNameFontStringID, "name", 0);

		// Now call the base class so it can setup any string ids we haven't already set
		super.initResources();
	}


}
