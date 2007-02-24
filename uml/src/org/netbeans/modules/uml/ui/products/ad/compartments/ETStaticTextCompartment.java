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

import java.awt.Font;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.graphics.TSEGraphics;


/**
 * @author jingmingm
 *
 */
public class ETStaticTextCompartment extends ETCompartment implements IADStaticTextCompartment
{
	public final int STCK_ELLIPSE = 1;
	public final int STCK_RECTANGLE = 2;
	
	protected final String STATICTEXTCOMPARTMENTKIND_STRING = "StaticTextCompartmentKind";
	protected int m_Kind = 0;
	
	public int getKind()
	{
		return m_Kind;
	}
	
	public void setKind(int kind)
	{
		m_Kind = kind;
	}
	
	public String getCompartmentID()
	{
		return "ADStaticTextCompartment";
	}
	
	public void InitResources()
	{
		this.setName(" ");
	}
	
	public ETStaticTextCompartment()
	{
		super();
		this.init();
	}

	public ETStaticTextCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
		this.init();
	}

	private void init()
	{
		this.setFontString("Arial-plain-12");
		this.InitResources();
		this.setReadOnly(true);
		this.setHorizontalAlignment(IADCompartment.CENTER);
	}
	
	public IProductArchiveElement writeToArchive(IProductArchive pProductArchive, IProductArchiveElement pElement)
	{
		IProductArchiveElement retObj = super.writeToArchive(pProductArchive, pElement);
		if (retObj != null)
		{
			retObj.addAttributeLong(STATICTEXTCOMPARTMENTKIND_STRING, m_Kind);
		}
		return retObj;
	}

	public void readFromArchive( IProductArchive pProductArchive, IProductArchiveElement pParentElement)
	{
		super.readFromArchive(pProductArchive, pParentElement);
		m_Kind = (int)pParentElement.getAttributeLong(STATICTEXTCOMPARTMENTKIND_STRING);
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		IETSize retVal;
  
		String staticText = this.getName();
		if (staticText != null && staticText.length() > 0)
		{
			// Make sure that the name the correct.
			setName(staticText);
     
			retVal = super.calculateOptimumSize(pDrawInfo, bAt100Pct);
		}
		else
		{
			retVal = new ETSize(0, 0);
		}
  
		return retVal;   
	}
   
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
	{
		super.draw(pDrawInfo, pBoundingRect);

		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
                
                //XXX - Kris Richards - copied this from ETNameCompartment.draw()
                // turning the events off to stop stack overflow error. Issue 86570
                graphics.getGraphWindow().getGraph().setFireEvents(false);
                graphics.getGraphWindow().getGraphManager().getEventManager().setCoalescingPermanentlyDisabled(true);
                
		IDrawEngine drawEngine = this.getEngine();
		ETGenericNodeUI parentUI = (ETGenericNodeUI) drawEngine.getParent();

		// set the color of the pen to the text color
		graphics.setColor(parentUI.getTextColor());

		// if the scale used is 100% we do not need to reset the font
		TSEFont originalFont = parentUI.getFont();

		// Compartment specific font  (What does this do??
		parentUI.setFont(new TSEFont(this.getFontString()));

		Font compartmentFont = originalFont.getScaledFont(pDrawInfo.getFontScaleFactor());

		graphics.setFont(compartmentFont);

		String value = this.getName();

		if (this.isSelected())
		{
			graphics.setColor(TSEColor.darkBlue);
			graphics.fillRect(pBoundingRect.getIntX(), pBoundingRect.getIntY(), pBoundingRect.getIntWidth(), pBoundingRect.getIntHeight());
			graphics.setColor(TSEColor.white);
		}
		else
		{
			graphics.setColor(parentUI.getTextColor());
		}

		int left;
		if(this.getHorizontalAlignment()==IADCompartment.CENTER)
		{
			left = (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (graphics.getFontMetrics().stringWidth(value) / 2);
		}
		else
		{
			left = pBoundingRect.getIntX()+2;
		}

		int top;
		if(this.getVerticalAlignment()==IADCompartment.CENTER)
		{
			top = pBoundingRect.getIntY() + graphics.getFontMetrics().getHeight() - 2;
		}
		else
		{
			top = pBoundingRect.getIntY()+pBoundingRect.getIntHeight()-4;
		}
		
		graphics.drawString(value, left, top);
                
                //XXX - Kris Richards - copied this from ETNameCompartment.draw()
                // turning the events back on. They were turned off to stop stack overflow error.
                graphics.getGraphWindow().getGraph().setFireEvents(true);
                graphics.getGraphWindow().getGraphManager().getEventManager().setCoalescingPermanentlyDisabled(false); 
	}



    /////////////
    // Accessible
    /////////////


    AccessibleContext accessibleContext;
    
    public AccessibleContext getAccessibleContext() {
	if (accessibleContext == null) {
	    accessibleContext = new AccessibleETCompartment();
	}
	return accessibleContext;
    }
    
    
    public class AccessibleETEditableCompartment extends AccessibleETCompartment {
	
	public AccessibleRole getAccessibleRole() {
	    return AccessibleRole.TEXT;
	}
	
    }


}
