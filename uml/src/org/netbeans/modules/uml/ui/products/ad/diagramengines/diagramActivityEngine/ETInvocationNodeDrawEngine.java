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



package org.netbeans.modules.uml.ui.products.ad.diagramengines.diagramActivityEngine;

import java.awt.Color;

import org.netbeans.modules.uml.common.ETException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.ui.products.ad.ADDrawEngines.ADNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.compartments.IADNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import java.awt.GradientPaint;

/**
 * @author KevinM
 *
 */
public class ETInvocationNodeDrawEngine extends ADNodeDrawEngine implements IInvocationNodeDrawEngine {

	public static final int MIN_NAME_SIZE_X = 40;
	public static final int MIN_NAME_SIZE_Y = 20;
	public static final int MIN_NODE_WIDTH  = 20;
	public static final int MIN_NODE_HEIGHT = 20;
	public static final int NODE_WIDTH  = 100;
	public static final int NODE_HEIGHT = 100;	
	/**
	 * 
	 */
	public ETInvocationNodeDrawEngine() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#doDraw(org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo)
	 */
	public void drawContents(IDrawInfo pDrawInfo) {
		if (pDrawInfo == null)
			return;

		// draw our frame

		// Draw a rounded rectangle around the entire node
		//
		//      /--------\   
		//     /          \    
		//    |            |  
		//    |            | 
		//    |    Name    | 
		//    |            | 
		//     \           / 
		//      \--------/
		//

		//CRect boundingRect = CTypeConversions::GetBoundingRect( pInfo );

		IETRect deviceBounds = pDrawInfo.getDeviceBounds();
                float centerX = (float) deviceBounds.getCenterX();
                GradientPaint paint = new GradientPaint(centerX, deviceBounds.getBottom(), getBkColor(), centerX, deviceBounds.getTop(), getLightGradientFillColor());
		GDISupport.drawRoundRect(pDrawInfo.getTSEGraphics(), deviceBounds.getRectangle(), pDrawInfo.getOnDrawZoom(), getBorderBoundsColor(), paint);

		// Draw each compartment
		handleNameListCompartmentDraw(pDrawInfo, deviceBounds, MIN_NAME_SIZE_X,MIN_NAME_SIZE_Y, false, 0);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#getDrawEngineID()
	 */
	public String getDrawEngineID() {
		return "InvocationNodeDrawEngine";
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#onContextMenu(org.netbeans.modules.uml.ui.products.ad.application.IMenuManager)
	 */
	public void onContextMenu(IMenuManager manager) {
		// TODO Auto-generated method stub
		super.onContextMenu(manager);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#sizeToContents()
	 */
	public void sizeToContents() {
		sizeToContentsWithMin(NODE_WIDTH,NODE_HEIGHT, false,true);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#createCompartments()
	 */
	public void createCompartments() throws ETException {
		try
		 {
			clearCompartments();

			createAndAddCompartment("ADNameListCompartment");
		 }
		 catch (Exception e)
		 {
			e.printStackTrace();
		 }
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initCompartments(org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement)
	 */
	public void initCompartments(IPresentationElement pElement) {
		try
		{
		   // We may get here with no compartments.  This happens if we've been created
		   // by the user.  If we read from a file then the compartments have been pre-created and
		   // we just need to initialize them.
		   if (getNumCompartments() == 0)
		   {
			  createCompartments();
		   }

		   IElement pModelElement = pElement != null ? pElement.getFirstSubject() : null;
		   if (pModelElement != null)
		   {
			  INameListCompartment pNameCompartment =
					getCompartmentByKind(INameListCompartment.class);

			  if (pNameCompartment != null)
			  {
				 pNameCompartment.attach(pModelElement);

				 // Make sure this node resizes to fit its compartments
				 pNameCompartment.setResizeToFitCompartments( true);
             
             IADNameCompartment nameCompartment = pNameCompartment.getNameCompartment();
             if(nameCompartment != null)
             {
                nameCompartment.setTextWrapping(true);
             }
			  }
		   }
		}
		catch (Exception e)
		{
		   e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#initResources()
	 */
	public void initResources() {
		setFillColor("invocationnodefill", 50,199,199);
                setLightGradientFillColor("invocationnodelightgradientfill", 191, 235, 235);
		setBorderColor("invocationnodeborder", Color.BLACK);
		
		super.initResources();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#isDrawEngineValidForModelElement()
	 */
	public boolean isDrawEngineValidForModelElement() {
		try
		 {
			// Make sure we're an invocation node
			// ActivityInvocation
			return getMetaTypeOfElement().equals("InvocationNode");
		 }
		 catch (Exception e)
		 {
			e.printStackTrace();
		 }
		 return false;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementDeleted(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
	public long modelElementDeleted(INotificationTargets pTargets) {
		return super.handleNameListModelElementDeleted(pTargets);		
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#modelElementHasChanged(org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets)
	 */
//J1241-handleNameListModelElementHasChanged sould be done in super. Lost events 
//	public long modelElementHasChanged(INotificationTargets pTargets) {
//		// TODO Auto-generated method stub
//		return super.handleNameListModelElementHasChanged(pTargets);
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine#setSensitivityAndCheck(org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu, org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem, int)
	 */
	public boolean setSensitivityAndCheck(IProductContextMenu pContextMenu, IProductContextMenuItem pMenuItem, int buttonKind) {
		// TODO Auto-generated method stub
		return super.setSensitivityAndCheck(pContextMenu, pMenuItem, buttonKind);
	}
}

