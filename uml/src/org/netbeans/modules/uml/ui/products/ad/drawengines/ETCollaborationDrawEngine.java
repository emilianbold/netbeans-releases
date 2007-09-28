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



package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
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
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.products.ad.viewfactory.IETGraphObjectUI;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETBoxCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ETClassNameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.TemplateParametersCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.INotificationTargets;
import org.netbeans.modules.uml.ui.products.ad.compartments.INameListCompartment;
import org.netbeans.modules.uml.ui.products.ad.compartments.ITemplateParametersCompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.common.ETException;

/*
 * @author TreyS
 *
 */
public class ETCollaborationDrawEngine extends ETNodeDrawEngine
{
    protected final int MIN_NAME_SIZE_X = 40;
    protected final int MIN_NAME_SIZE_Y = 20;
    protected final int MIN_NODE_WIDTH  = 80;
    protected final int MIN_NODE_HEIGHT = 20;
    
    public String getElementType()
    {
        String type = super.getElementType();
        if (type == null)
        {
            type = new String("Collaboration");
        }
        return type;
    }
    
    public void initResources()
    {
        setFillColor("collaborationfill", 103,190,103);
        setLightGradientFillColor("collaborationlightgradient", 103,237,213);
        setBorderColor("collaborationborder", Color.BLACK);
        
        super.initResources();
    }
    
    public void initCompartments()
    {
        ETClassNameListCompartment newClassNameList = new ETClassNameListCompartment(this);
        newClassNameList.addCompartment(new ETClassNameCompartment(this), -1, false);
        
        newClassNameList.setResizeToFitCompartments(true);
        
        this.addCompartment(newClassNameList);
    }
    
    public void createCompartments() throws ETException
    {
        IETGraphObjectUI parentUI =  this.getParent();
        
        if (parentUI.getOwner() != null)
        {
            if (parentUI.getModelElement() != null)
            {
                IElement element = parentUI.getModelElement();
                createAndAddCompartment("ADClassNameListCompartment");
                
                INameListCompartment pNameCompartment = getCompartmentByKind(INameListCompartment.class);
                if (pNameCompartment != null)
                {
                    pNameCompartment.attach(element);
                    pNameCompartment.setResizeToFitCompartments(true);
                    setDefaultCompartment(pNameCompartment);
                }
                
                if (element instanceof IClassifier)
                {
                    IClassifier pClassifier = (IClassifier)element;
                    ETList<IParameterableElement> pParameters = pClassifier.getTemplateParameters();
                    if (pParameters != null && pParameters.size() > 0)
                    {
                        TemplateParametersCompartment pTemplateParametersCompartment = new TemplateParametersCompartment();
                        ICompartment pCompartment = (ICompartment)pTemplateParametersCompartment;
                        pCompartment.setEngine(this);
                        pCompartment.addModelElement(element, -1);
                        this.addCompartment(pCompartment);
                    }
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
            IETRect nameRect = null, templateRect = null;
            ICompartment pTemplateParametersCompartment = (ICompartment)getCompartmentByKind(ITemplateParametersCompartment.class);
            if (pTemplateParametersCompartment != null)
            {
                IElement pElement = this.getFirstModelElement();
                if (pElement != null && pElement instanceof IClassifier)
                {
                    IClassifier pClassifier = (IClassifier)this.getFirstModelElement();
                    if (pClassifier.getTemplateParameters().size() == 0)
                    {
                        this.removeCompartment(pTemplateParametersCompartment, true);
                        pTemplateParametersCompartment = null;
                    }
                }
            }
            ICompartment pNameListCompartment = (ICompartment)getCompartmentByKind(INameListCompartment.class);
            int templateHeight = 0;
            if (pTemplateParametersCompartment == null)
            {
                nameRect = new ETRect(x, y, w, h);
            }
            else
            {
                IETSize templateSize = pTemplateParametersCompartment.calculateOptimumSize(pDrawInfo, false);
                templateHeight = templateSize.getHeight();
                templateRect = new ETRect(x + 10, y, w - 10, templateSize.getHeight());
                nameRect = new ETRect(x, y + templateSize.getHeight() - 10, w - 10, h - templateSize.getHeight() + 10);
            }
            
            if (pNameListCompartment != null)
            {
                float nameCenterX = (float)deviceRect.getCenterX();
                float gradiantTopX = deviceRect.getTop();
                if(templateRect != null)
                {
                    gradiantTopX = gradiantTopX + templateHeight;
                }
                GradientPaint namePaint = new GradientPaint(nameCenterX,
                        deviceRect.getBottom(),
                        getBkColor(),
                        nameCenterX,
                        gradiantTopX,
                        getLightGradientFillColor());
                
                Rectangle rect = new Rectangle(nameRect.getLeft(), nameRect.getTop(), nameRect.getIntWidth(), nameRect.getIntHeight());
                GDISupport.drawDashedEllipse(graphics.getGraphics(), rect, getBorderBoundsColor(), namePaint);
                if (pTemplateParametersCompartment != null)
                {
                    nameRect.setTop(nameRect.getTop() + 10);
                }
                pNameListCompartment.draw(pDrawInfo, nameRect);
            }
            if (pTemplateParametersCompartment != null)
            {
                Rectangle rect = new Rectangle(templateRect.getLeft(), templateRect.getTop(), templateRect.getIntWidth(), templateRect.getIntHeight());
                
                float templateCenterX = (float)deviceRect.getCenterX();
                GradientPaint templatePaint = new GradientPaint(templateCenterX,
                        deviceRect.getTop() + templateHeight,
                        getBkColor(),
                        templateCenterX,
                        deviceRect.getTop(),
                        getLightGradientFillColor());
                GDISupport.drawDashedRectangle(graphics.getGraphics(), rect, getBorderBoundsColor(), templatePaint);
                //GDISupport.drawDashedRectangle(graphics.getGraphics(), rect, getBorderBoundsColor(), getBkColor());
                //templateRect.setTop(templateRect.getTop() - 10);
                pTemplateParametersCompartment.draw(pDrawInfo, templateRect);
            }
        }
    }
    
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        IETSize retVal = null, nameRect = null, templateRect = null;;
        ICompartment pTemplateParametersCompartment = (ICompartment)getCompartmentByKind(ITemplateParametersCompartment.class);
        ICompartment pNameListCompartment = (ICompartment)getCompartmentByKind(INameListCompartment.class);
        
        if (pNameListCompartment != null)
        {
            nameRect = pNameListCompartment.calculateOptimumSize(pDrawInfo, true);
        }
        if (pTemplateParametersCompartment != null)
        {
            templateRect = pTemplateParametersCompartment.calculateOptimumSize(pDrawInfo, true);
        }
        
        if (nameRect == null)
        {
            retVal = new ETSize(60, 40);
        }
        else
        {
            if (templateRect == null)
            {
                retVal = nameRect;
            }
            else
            {
                retVal = new ETSize(Math.max(nameRect.getWidth(), templateRect.getWidth()) + 10, nameRect.getHeight() + templateRect.getHeight() + 10);
            }
        }
        
        if (retVal != null)
            retVal.setSize(Math.max(retVal.getWidth(), MIN_NODE_WIDTH), Math.max(retVal.getHeight(), MIN_NODE_HEIGHT));
        
        return bAt100Pct || retVal == null ? retVal : scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
    }
    
    public String getDrawEngineID()
    {
        return "CollaborationDrawEngine";
    }
    
    public boolean isDrawEngineValidForModelElement()
    {
        boolean valid = false;
        String metaType = getMetaTypeOfElement();
        if (metaType.equals("Collaboration"))
        {
            valid = true;
        }
        
        return valid;
    }
    
    public void sizeToContents()
    {
        sizeToContentsWithMin(MIN_NODE_WIDTH, MIN_NODE_HEIGHT);
    }
    
    public String getResizeBehavior()
    {
        return "PSK_RESIZE_EXPANDONLY";
    }
    
    public long modelElementHasChanged(INotificationTargets pTargets)
    {
        try
        {
            this.clearCompartments();
            this.createCompartments();
        }
        catch (Exception e)
        {
        }
        return super.modelElementHasChanged(pTargets);
    }
}