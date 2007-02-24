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
import java.awt.GradientPaint;
import java.awt.Shape;
import java.awt.Rectangle;
import java.util.Iterator;
import com.tomsawyer.editor.graphics.TSEGraphics;
import com.tomsawyer.editor.TSESolidObject;
//import com.tomsawyer.util.TSConstRect;
import com.tomsawyer.drawing.geometry.TSConstRect;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.common.ETException;

public class ETDerivationClassifierDrawEngine extends ETNodeDrawEngine {
	protected final int NODE_HEIGHT = 30;
	protected final int NODE_WIDTH = 60;
	protected final String STATIC_TEXT_FONT = "Arial-12";

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources() {
		setFillColor("derivationclassifierfill", 251, 233, 126);
        setLightGradientFillColor("derivationclassifierlightgradientfill", 254, 254, 254);
		setBorderColor("derivationclassifierborder", Color.BLACK);

		super.initResources();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getElementType()
	 */
	public String getElementType() {
		String type = super.getElementType();
		if (type == null) {
			type = new String("DerivationClassifier");
		}
		return type;
	}

	public void initCompartments() {
		ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
		newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);

		this.addCompartment(newClassNameList);
	}

        /**
	 * Initializes our compartments by attaching modelelements to each. Previously existing compartments remain,
	 * so if a compartment already exists it is reattached, if not one is created.
	 *
	 * @param pElement [in] The presentation element we are representing
	 */
	public void initCompartments(IPresentationElement presEle)
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
                catch(Exception e)
                {
                }
            }
            
            IElement pModelElement = presEle.getFirstSubject();
            if (pModelElement != null)
            {
                // Tell the name compartment about the model element it should display
                INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
                if (pNameCompartment != null)
                {
                    pNameCompartment.attach(pModelElement);
                }
            }
        }

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
	 */
	public void createCompartments() throws ETException {
		ETGenericNodeUI parentUI = (ETGenericNodeUI) this.getParent();

		if (parentUI.getOwnerNode() != null) {
			if (parentUI.getModelElement() != null) {
				IElement element = parentUI.getModelElement();
				createAndAddCompartment("ADClassNameListCompartment");

				INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
				if (pNameCompartment != null) {
					pNameCompartment.attach(element);
					setDefaultCompartment(pNameCompartment);
				}
			} else {
				this.initCompartments();
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void doDraw(IDrawInfo pDrawInfo) {
		IETGraphObjectUI parentUI = (IETGraphObjectUI) this.getParent();

		if (parentUI.getOwner() != null) {	
			TSEGraphics graphics = pDrawInfo.getTSEGraphics();		
			
			IETRect deviceBounds = (IETRect)pDrawInfo.getDeviceBounds();			
			
			int x = deviceBounds.getLeft();
			int w = deviceBounds.getIntWidth();
			int y = deviceBounds.getTop();
			int h = deviceBounds.getIntHeight();

			// Background
            float centerX = (float)deviceBounds.getCenterX();
            GradientPaint paint = new GradientPaint(centerX,
                         deviceBounds.getBottom(),
                         getBkColor(),
                         centerX,
                         deviceBounds.getTop(),
                         getLightGradientFillColor());
    
            GDISupport.drawRectangle(graphics,deviceBounds.getRectangle(), getBorderBoundsColor(), paint);
			
			//	Draw the compartments
			Iterator iterator = this.getCompartments().iterator();
			IETRect compartmentDrawRect = new ETRect(x, y, w, h);
			if (iterator.hasNext()) {
				IListCompartment compartment = (IListCompartment) iterator.next();
				if (compartment instanceof ETClassNameListCompartment) {
					compartment.draw(pDrawInfo, compartmentDrawRect);
				}
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#calculateOptimumSize(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo, boolean)
	 */
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct) {
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		TSTransform transform = graphics.getTSTransform();
		ETGenericNodeUI parentUI = (ETGenericNodeUI) this.getParent();

		// Get compartment size
		IETSize retVal = null;
		Iterator < ICompartment > iterator = getCompartments().iterator();
		while (iterator.hasNext()) {
			ICompartment compartment = iterator.next();
			if (compartment instanceof ETClassNameListCompartment) {
				retVal = compartment.calculateOptimumSize(pDrawInfo, true);
				break;
			}
		}

		// Calculate size and return
		if (retVal != null) {			
			retVal.setSize(Math.max(retVal.getWidth(), NODE_WIDTH), Math.max(retVal.getHeight(),NODE_HEIGHT));
		}

		return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, transform);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "DerivationClassifierDrawEngine";
	}

	/*
	 *  (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementHasChanged(INotificationTargets pTargets) {
		try {
			this.clearCompartments();
			this.createCompartments();
		} catch (Exception e) {
		}
		return super.modelElementHasChanged(pTargets);
	}
}
