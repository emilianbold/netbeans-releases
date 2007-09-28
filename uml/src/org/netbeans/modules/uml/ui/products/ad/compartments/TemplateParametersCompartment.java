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
import java.awt.Font;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawEngine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IBehavioralFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.ETGenericNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import com.tomsawyer.editor.TSEColor;
import com.tomsawyer.editor.TSEFont;
import com.tomsawyer.editor.graphics.TSEGraphics;
//import com.tomsawyer.util.TSTransform;
import com.tomsawyer.editor.TSTransform;

/**
 * @author jingmingm
 *
 */
public class TemplateParametersCompartment extends ETEditableCompartment implements ITemplateParametersCompartment
{
	protected final int s_XOverLap = 10;
	protected final int s_YOverLap = 10;
	
	public TemplateParametersCompartment()
	{
		super();
                init();
	}

	public TemplateParametersCompartment(IDrawEngine pDrawEngine)
	{
		super(pDrawEngine);
                init();
	}
	
        private void init()
        {
            setReadOnly(true);
        }
	public String getCompartmentID()
	{
		return "TemplateParametersCompartment";
	}
	
	public void addModelElement(IElement pElement, int pIndex) 
	{
		super.addModelElement(pElement, pIndex);

		if (pElement != null && pElement instanceof IClassifier)
		{
			String name = getParameters(pElement);
			
			this.setName(name);
		}
	}
	
	public IETSize getOverlap()
	{
		IETSize retVal = new ETSize(s_XOverLap, s_YOverLap);
		return retVal;
	}
	
	public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
	{
		// Get ETEditableCompartment size at one hundred percent.
		IETSize retVal = super.calculateOptimumSize(pDrawInfo, true);

		// Now expand the size of the template parameters compartment to account for the fact that it draws offcenter
		//retVal.setSize(retVal.getWidth() + s_XOverLap, Math.max(retVal.getHeight() + s_YOverLap, 20));
		// J1369-It works fine for test bed but not on SUN1.
		int count = 1;
		IElement pElement = this.getModelElement();
		if(pElement != null && pElement instanceof IClassifier)
		{
			IClassifier pClassifier = (IClassifier)pElement;
			ETList<IParameterableElement> params = pClassifier.getTemplateParameters();
			if (params != null && params.size() > 0)
			{
				count = params.size();
			}
		}
		retVal.setSize(retVal.getWidth() + count*s_XOverLap, Math.max(retVal.getHeight() + s_YOverLap, 20));
		
		return bAt100Pct ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
	}
	
	public void draw(IDrawInfo pDrawInfo, IETRect pBoundingRect)
	{
		super.draw(pDrawInfo, pBoundingRect);
		
		TSEGraphics graphics = pDrawInfo.getTSEGraphics();
		
		IDrawEngine drawEngine = this.getEngine();
		ETGenericNodeUI parentUI = (ETGenericNodeUI) drawEngine.getParent();

		Font compartmentFont = getCompartmentFont(pDrawInfo.getFontScaleFactor());
		graphics.setFont(compartmentFont);
		parentUI.setFont(new TSEFont(compartmentFont));
		
		graphics.setColor(parentUI.getTextColor());
		String value = getParameters(this.getModelElement());
		
		if (value != null)
		{
			graphics.drawString(value,
									 (pBoundingRect.getIntX() + pBoundingRect.getIntWidth() / 2) - (graphics.getFontMetrics().stringWidth(value) / 2),
									 pBoundingRect.getIntY() + pBoundingRect.getIntHeight() /2 + 3);
		}
	}
	
	protected String getParameters(IElement pElement)
	{
		String parameters = "";
		if (pElement != null)
		{
			IClassifier pClassifier = (IClassifier)pElement;
			ETList<IParameterableElement> pParameters = pClassifier.getTemplateParameters();
			for (int i = 0; i < pParameters.size(); i++)
			{
				IParameterableElement pParameterableElement = pParameters.get(i);
				if (parameters.length() > 0)
				{
					parameters += ",";
				}
				String eleName = pParameterableElement.getName();
				if (eleName != null)
				{
					parameters += eleName;
				}
			} 
		}
		return parameters;
	}
	
	public void initResources()
	{
		// First setup our defaults in case the colors/fonts are not in the 
		// configuration file
		setResourceID("template", Color.BLACK);

		// Call the base interface so that the compartments get initialized.
		super.initResources();
	}
}

