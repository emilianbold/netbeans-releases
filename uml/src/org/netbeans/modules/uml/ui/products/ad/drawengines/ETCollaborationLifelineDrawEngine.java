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
import java.awt.Rectangle;
import java.util.Iterator;
import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ETLifelineNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.sequencediagram.ILifelineNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.IStereotypeCompartment;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IStickFigureCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ModelElementChangedKind;
import com.tomsawyer.editor.graphics.TSEGraphics;
import java.awt.GradientPaint;
import org.netbeans.modules.uml.common.ETStrings;


public class ETCollaborationLifelineDrawEngine extends ETNodeDrawEngine
{
	protected final int NODE_HEIGHT = 25;
	protected final int NODE_WIDTH  = 55;
	
	public String getElementType()
	{
		String type = super.getElementType();
		if (type == null)
		{
			type = new String("Lifeline");
		}
		return type;
	}
	
	public void initResources()
	{
		setFillColor("collaborationlifelinefill", 211, 227, 244);
                setLightGradientFillColor("collaborationlifelinelightgradientfill", 255, 255, 255);
		setBorderColor("collaborationlifelineborder", Color.BLACK);

		super.initResources();
	}

	public String getDrawEngineID() 
	{
		return "CollaborationLifelineDrawEngine";
	}
	
	public boolean isDrawEngineValidForModelElement()
	{
		String metaType = getMetaTypeOfElement();
		return metaType != null && metaType.equals("Lifeline");
	}
	
	public long readFromArchive(IProductArchive pProductArchive, IProductArchiveElement pParentElement)
        {
           long retVal = super.readFromArchive(pProductArchive, pParentElement);
           updateNameCompartmentRepresentsMetaType();
           return retVal;
        }
	
        /**
         * Initializes our compartments.
         *
         * @param pElement [in] The presentation element we are representing
         */
        public void initCompartments(IPresentationElement presEle)
        {
           // We may get here with no compartments.  This happens if we've been created
           // by the user.  If we read from a file then the compartments have been pre-created and
           // we just need to initialize them.
           int count = getNumCompartments();
           if (count == 0)
           {
              try
              {
                 createCompartments();
              }
              catch (Exception e)
              {
                 Log.stackTrace(e);
              }
           }
           else
           {
              String modEleType = getRepresentsMetaType();
              if (modEleType != null && modEleType.equals("Actor"))
              {
                 IStickFigureCompartment sfComp = getCompartmentByKind(IStickFigureCompartment.class);
                 if (sfComp == null)
                 {
                    createAndAddCompartment("StickFigureCompartment", -1);
                 }
              }
           }
           
           // Enhancement W6120:  Process the stereotype compartment
           IStereotypeCompartment stereoComp = getCompartmentByKind(IStereotypeCompartment.class);
           if (stereoComp != null)
           {
              stereoComp.setEngine(this);
              updateStereotypeCompartment();
           }
           
           ILifelineNameCompartment lifeComp = getCompartmentByKind(ILifelineNameCompartment.class);
           if (lifeComp != null)
           {
              IElement modEle = presEle.getFirstSubject();
              if (modEle != null)
              {
                 // Attach the model element to the compartment
                 lifeComp.addModelElement(modEle, -1);
              }
              setDefaultCompartment(lifeComp);
           }
           
           // Fix W2987:  Make sure the representing metatype is updated properly
           updateNameCompartmentRepresentsMetaType();
        }
   
	public void initCompartments()
	{
		ETLifelineNameCompartment lifelineName = new ETLifelineNameCompartment();
		lifelineName.setEngine(this);
		this.addCompartment(lifelineName);
	}
	
	public void createCompartments() throws ETException
        {
           IETGraphObjectUI parentUI =  this.getParent();
           boolean isActorLifeline = false;
           
           if (parentUI.getOwner() != null)
           {
              if (parentUI.getModelElement() != null)
              {
                 IElement element = parentUI.getModelElement();
                 
                 
                 try
                 {
                    // Fixed issue 82208, 82207
                    // get the flag which indicates if this lifeline is a actor lifeline.
                    // If the lifeline is an actor lifeline or representes an actor classifier,
                    // then add a stickFigureCompartment
                    if (element instanceof ILifeline)
                    {
                       isActorLifeline = ((ILifeline)element).getIsActorLifeline();
                    }
                    
                    String currentModelElementType = getRepresentsMetaType();
                    if(currentModelElementType.equals("Actor") || isActorLifeline)
                    {
                       createAndAddCompartment("StickFigureCompartment", 0);
                    }
                    else
                    {
                       createAndAddCompartment("StereotypeCompartment", 0);
                       updateStereotypeCompartment();
                    }
                    
                    ICompartment pLifelineNameCompartment = createAndAddCompartment("LifelineNameCompartment", 0);
                    setDefaultCompartment(pLifelineNameCompartment);
                    
                    updateNameCompartment();
                    updateNameCompartmentRepresentsMetaType();
                 }
                 catch (Exception e)
                 {
                    throw new ETException(ETStrings.E_CMP_CREATE_FAILED, e.getMessage());
                 }
              }
              else
              {
                 this.initCompartments();
              }
           }
        }
	
	public void doDraw(IDrawInfo pDrawInfo)
	{
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		IETGraphObjectUI parentUI = this.getParent();

		if (parentUI.getOwner() != null)
		{
			IETRect deviceRect = pDrawInfo.getDeviceBounds();
			
			int x = deviceRect.getLeft();
			int w = deviceRect.getIntWidth();
			int y = deviceRect.getTop();
			int h = deviceRect.getIntHeight();	
	
			//	Draw the compartments
			int yOff = 0;
			ICompartment pStickFigureCompartment = (ICompartment)getCompartmentByKind(IStickFigureCompartment.class);
			if (pStickFigureCompartment != null)
			{
				IETSize rectStickFigure = pStickFigureCompartment.calculateOptimumSize(pDrawInfo, false);
				yOff += rectStickFigure.getHeight();
				IETRect pETRect = (IETRect)deviceRect.clone();
				pETRect.setBottom(pETRect.getTop() + rectStickFigure.getHeight());
				if (rectStickFigure.getWidth() < w)
				{
					int xOff = w - rectStickFigure.getWidth();
					xOff = (int)(xOff/2);
					pETRect.inflate(-xOff, 0);
				}
				//graphics.setColor(Color.BLUE);
				//graphics.drawRect(pETRect.getLeft(), pETRect.getTop(), pETRect.getIntWidth(), pETRect.getIntHeight());
				//pETRect = new ETRect(transform.xToWorld(pETRect.getLeft()), transform.yToWorld(pETRect.getTop()), pETRect.getWidth(), pETRect.getHeight());
				pStickFigureCompartment.draw(pDrawInfo, pETRect);
			}
			
                        float centerX = (float) deviceRect.getCenterX();
                        GradientPaint paint = new GradientPaint(centerX, deviceRect.getBottom(), getBkColor(), centerX, deviceRect.getTop() + yOff, getLightGradientFillColor());
			GDISupport.drawRectangle(pDrawInfo.getTSEGraphics(), new Rectangle(x, y + yOff, w, h - yOff), getBorderBoundsColor(), paint);
			
			ICompartment pStereotypeCompartment = (ICompartment)getCompartmentByKind(IStereotypeCompartment.class);
			if (pStereotypeCompartment != null)
			{
				IETSize rectStereotype = pStereotypeCompartment.calculateOptimumSize(pDrawInfo, false);
				IETRect pETRect = new ETRect(x, y + yOff, w, rectStereotype.getHeight());
				pStereotypeCompartment.draw(pDrawInfo, pETRect);
				yOff += rectStereotype.getHeight();
			}
			
			ICompartment pNameCompartment = (ICompartment)getCompartmentByKind(ILifelineNameCompartment.class);
			if (pNameCompartment != null)
			{
				IETSize rectName = pNameCompartment.calculateOptimumSize(pDrawInfo, false);
				IETRect pETRect = new ETRect(x, y + yOff, w, rectName.getHeight());
				pNameCompartment.draw(pDrawInfo, pETRect);
				yOff += rectName.getHeight();
			}
		}
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		IETSize curVal = null, retVal = new ETSize(0, 0);
		Iterator iterator = this.getCompartments().iterator();			
		while (iterator.hasNext())
		{
			ICompartment compartment = (ICompartment) iterator.next();
			curVal = compartment.calculateOptimumSize(pDrawInfo, bAt100Pct);
			retVal.setHeight(retVal.getHeight() + curVal.getHeight());
			if (retVal.getWidth() < curVal.getWidth())
			{
				retVal.setWidth(curVal.getWidth());
			}
		}
		
		//retVal.setSize(Math.max(retVal.getWidth(), NODE_WIDTH), Math.max(retVal.getHeight(), NODE_HEIGHT));
			
		return retVal;
	}
	
	protected ILifeline getLifeline()
	{
		IElement pElement = this.getFirstModelElement();
		return pElement != null? (ILifeline)pElement : null;
	}
	
	protected IClassifier getRepresentingClassifier()
	{
		ILifeline lifeline = getLifeline();
		return lifeline != null? lifeline.getRepresentingClassifier() : null;
	}
	
	protected String getRepresentsMetaType()
	{
		String retVal = "";

		// Fixed issue 82208, 82207
                // get the flag which indicates if this lifeline is a actor lifeline.
                // If the lifeline is an actor lifeline or representes an actor classifier,
                // then add a stickFigureCompartment to the lifeline      
		IClassifier classifier = getRepresentingClassifier();
		if(classifier != null)
		{
			retVal = classifier.getElementType();
		}
      
		if((retVal == null) || (retVal.length() <= 0))
		{
			retVal = "Class";
         
			String initStr = getInitializationString();
			int delimiter = initStr.indexOf(' ');
			if (delimiter > 0)
			{
				initStr = initStr.substring(delimiter + 1);
				if((initStr != null) && (initStr.equals("Actor") == true))
				{
					retVal = initStr;
				}
			}
		}
      
		return retVal;
	}
	
	protected void updateStereotypeCompartment()
	{
		ICompartment pStereotypeCompartment = (ICompartment)getCompartmentByKind(IStereotypeCompartment.class);
		if (pStereotypeCompartment != null)
		{
			IElement pElement = this.getFirstModelElement();
			if (pElement != null)
			{
				pStereotypeCompartment.addModelElement(pElement, -1);
			}
		}
	}
	
	protected void updateNameCompartment()
	{
		ICompartment pNameCompartment = (ICompartment)getCompartmentByKind(ILifelineNameCompartment.class);
		if (pNameCompartment != null)
		{
			IElement pElement = this.getFirstModelElement();
			if (pElement != null)
			{
				pNameCompartment.addModelElement(pElement, -1);
			}
		}
	}
	
	protected void updateNameCompartmentRepresentsMetaType()
	{
		ILifelineNameCompartment pNameCompartment = getCompartmentByKind(ILifelineNameCompartment.class);
		if (pNameCompartment != null)
		{
			pNameCompartment.setRepresentsMetaType(this.getRepresentsMetaType());		
		}
	}
	
	public long modelElementHasChanged(INotificationTargets pTargets)
	{
		long retVal = super.modelElementHasChanged(pTargets);
		if (pTargets != null)
		{
			int nKind = pTargets.getKind();
			switch( nKind )
			{
				case ModelElementChangedKind.MECK_REPRESENTINGCLASSIFIERCHANGED:
				// Enhancement W6120:  Process the stereotype compartment
				updateStereotypeCompartment();
				// fall through

				case ModelElementChangedKind.MECK_STEREOTYPEAPPLIED:
				case ModelElementChangedKind.MECK_STEREOTYPEDELETED:
				delayedSizeToContents();
				break;

				default:
				// do nothing
				break;
			}
		}

		return retVal;
	}
}
