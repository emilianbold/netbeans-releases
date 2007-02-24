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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Rectangle;
import java.util.Iterator;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSESolidObject;
import com.tomsawyer.editor.TSEFont;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSConstPoint;
import com.tomsawyer.drawing.geometry.TSConstPoint;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStickFigureCompartment;
import javax.swing.SwingConstants;
import org.netbeans.modules.uml.common.ETException;
import org.openide.ErrorManager;

public class ETActorDrawEngine extends ETNodeDrawEngine
{
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
	 */
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Actor");
		}
		return type;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.gui.products.ad.viewfactory.ETIDrawEngine#doDraw()
	 */
	public void drawContents(IDrawInfo pDrawInfo)
	{
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		
		IETGraphObjectUI parentUI = this.getParent();
		
		// draw yourself only if you have an owner
		if (parentUI.getOwner() != null)
		{						
			IStickFigureCompartment pStickFigureCompartment = getCompartmentByKind(IStickFigureCompartment.class);
			INameListCompartment  pNameCompartment = getCompartmentByKind(INameListCompartment.class);
			if (pStickFigureCompartment != null && pNameCompartment != null)
			{
				IETSize nameSize = pNameCompartment.calculateOptimumSize(pDrawInfo, false);
				IETRect nameRect = (IETRect)pDrawInfo.getDeviceBounds().clone();
				nameRect.setTop(nameRect.getBottom() - nameSize.getHeight());
				pNameCompartment.draw(pDrawInfo, nameRect);
                                pNameCompartment.setResizeToFitCompartments(true);
				
//                                IETSize stickFigureSize = pStickFigureCompartment.calculateOptimumSize(pDrawInfo,false);                                 
				IETRect stickfigureRect = (IETRect)pDrawInfo.getDeviceBounds();
				stickfigureRect.setBottom(stickfigureRect.getBottom() - nameSize.getHeight());
                                pStickFigureCompartment.draw(pDrawInfo, stickfigureRect);                                
			}
		}	
	}
	

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement)
	{
		// We may get here with no compartments.  This happens if we've been created
		// by the user.  If we read from a file then the compartments have been pre-created and
		// we just need to initialize them.
		int numCompartments = getNumCompartments();
		if (numCompartments == 0)
		{
			try
			{
				createCompartments();
			}
			catch (Exception e)
			{
			}
		}

		IElement pModelElement = pElement.getFirstSubject();
		if (pModelElement != null)
		{
			String sElementType = pModelElement.getElementType();
			INameListCompartment pNameListCompartment = getCompartmentByKind(INameListCompartment.class);
			if (pNameListCompartment != null)
			{
				pNameListCompartment.attach(pModelElement);

				if (sElementType != null && sElementType.equals("PartFacade"))
				{
					// Disable the package import compartment if we have a part facade
					pNameListCompartment.setPackageImportCompartmentEnabled(false);
				}
				setName(pNameListCompartment);
			}
		}
	}
		
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
	 */
	public void createCompartments() throws ETException 
	{
		IETGraphObjectUI parentUI =  this.getParent();

		if (parentUI != null && parentUI.getOwner() != null) 
		{
			createAndAddCompartment("StickFigureCompartment");
			if (parentUI.getModelElement() != null)
			{
				IElement element = parentUI.getModelElement();
				createAndAddCompartment("ADClassNameListCompartment");
				INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
				if (pNameCompartment != null)
				{
					pNameCompartment.attach(element);
					setDefaultCompartment(pNameCompartment);
					pNameCompartment.setSelected(true);
					setName(pNameCompartment);
				}
				// Create the default compartments
			} else {
				ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
				newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
				this.addCompartment(newClassNameList);	
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		IETSize nameVal = null, retVal = null;
		Iterator<ICompartment> iterator = this.getCompartments().iterator();			
		while (iterator.hasNext())
		{
			ICompartment compartment = iterator.next();
			if (compartment instanceof ETClassNameListCompartment)
			{
				// We will scale the entire thing below.
				nameVal = compartment.calculateOptimumSize(pDrawInfo, true);
				break;
			}
		}
		
		retVal = nameVal == null ?  new ETSize(40, 60) :  new ETSize(Math.max(nameVal.getWidth(), 40), nameVal.getHeight() + 60);
		return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() 
	{
		return "ActorDrawEngine";
	}
	
	private void setName(INameListCompartment pCompartment)
	{
		String text = "";
		if (pCompartment != null)
		{
			IElement pEle = getFirstModelElement();
			if (pEle instanceof IPartFacade)
			{
				text = "<<role>>";
			}
		}
		
		pCompartment.addStaticText(text);
	}

	public void sizeToContents()
	{
		sizeToContentsWithMin(40, 70, false, false);
	}
	
	public boolean isDrawEngineValidForModelElement()
	{
		boolean bIsValid = false;
		try
		 {
			String currentMetaType = getMetaTypeOfElement();
			if (currentMetaType != null && (currentMetaType.equals("Actor") || currentMetaType.equals("PartFacade")))
			{
				bIsValid = true;
			}
		 }
		 catch (Exception e)
		 {
			e.printStackTrace();
		 }
		 return bIsValid;
	}
	
	public String getDrawEngineMatchID()
	{
		IElement pElement = getFirstModelElement();
		if (pElement instanceof IPartFacade)
		{
			return "ActorDrawEngine PartFacade";
		}
		else
		{
			return super.getDrawEngineMatchID();
		}
	}
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine#getBkColor()
    */
   public Color getBkColor()
   {      
   		
      return this.getGraphWindow() != null ? this.getGraphWindow().getBackground() : Color.WHITE;
   }

   protected void setPadlockLocation(IDrawInfo pDrawInfo, Padlock padlock) {
       TSTransform tsTransform = pDrawInfo.getTSTransform();
       //
       IETRect deviceRect = pDrawInfo.getDeviceBounds();
       TSConstRect worldRect = tsTransform.boundsToWorld(deviceRect.getRectangle());
       //
       padlock.setOriginalPoint(SwingConstants.CENTER);
       padlock.setLocation(worldRect.getCenterX() - 2d, worldRect.getCenterY() - 2d);
   }
   
}
