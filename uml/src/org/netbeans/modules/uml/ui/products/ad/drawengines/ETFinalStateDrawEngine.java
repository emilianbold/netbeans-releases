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


/*
 * Created on Oct 29, 2003
 *
 */
package org.netbeans.modules.uml.ui.products.ad.drawengines;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;

import com.tomsawyer.editor.graphics.TSEGraphics;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.ReconnectEdgeCreateConnectorKind;
import org.netbeans.modules.uml.core.support.umlsupport.ETSize;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETSize;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.GDISupport;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IDrawInfo;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETNodeUI;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ILabelManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.TSLabelKind;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;

/**
 * @author jingmingm
 *
 */
public class ETFinalStateDrawEngine extends ETNodeDrawEngine
{
    public void initResources()
    {
        setFillColor("ellipsefill", 255, 51, 0);
        setLightGradientFillColor("ellipselightgradient", 255, 255, 255);
        setBorderColor("ellipseborder", Color.BLACK);
        
        super.initResources();
    }
    
    public String getElementType()
    {
        String type = super.getElementType();
        if (type == null)
        {
            type = new String("FinalState");
        }
        return type;
    }
    
    public void doDraw(IDrawInfo pDrawInfo)
    {
        TSEGraphics graphics = pDrawInfo.getTSEGraphics();
        
        IETNodeUI parentUI = (IETNodeUI)this.getParent();
        
        // draw yourself only if you have an owner
        if (parentUI.getOwner() != null)
        {
            if (!parentUI.isTransparent())
            {
                IETRect deviceRect = pDrawInfo.getDeviceBounds();
                // Draw outline
                graphics.setColor(getBorderBoundsColor());
                GDISupport.frameEllipse(graphics, deviceRect.getRectangle());
                
                // Draw center
                int h = deviceRect.getIntHeight();
                int w = deviceRect.getIntWidth();
                
                IETRect redCenter =(IETRect) deviceRect.clone();
                redCenter.deflateRect(w/4, h/4);
                float centerX = (float)redCenter.getCenterX();
                GradientPaint paint = new GradientPaint(centerX,
                        redCenter.getBottom(),
                        getBkColor(),
                        centerX,
                        redCenter.getTop(),
                        getLightGradientFillColor());
                
                graphics.setPaint(paint);
                GDISupport.fillEllipse(graphics,redCenter.getRectangle());
                graphics.setColor(getBorderBoundsColor());
                GDISupport.frameEllipse(graphics, redCenter.getRectangle());
            }
        }
    }
    
    public IETSize calculateOptimumSize(IDrawInfo pDrawInfo, boolean bAt100Pct)
    {
        IETSize retVal = new ETSize(16, 16);
        
        return bAt100Pct ? retVal : super.scaleSize(retVal, pDrawInfo != null ? pDrawInfo.getTSTransform() : getTransform());
    }
    
    public boolean setSensitivityAndCheck(String id, ContextMenuActionClass pClass)
    {
        boolean bFlag = handleStandardLabelSensitivityAndCheck(id, pClass);
        if (!bFlag)
        {
            if (id.equals("MBK_SHOW_FINALSTATE_NAME"))
            {
                ILabelManager labelMgr = getLabelManager();
                if (labelMgr != null)
                {
                    boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
                    pClass.setChecked(isDisplayed);
                    bFlag = isParentDiagramReadOnly() ? false : true;
                }
            }
            else
            {
                bFlag = super.setSensitivityAndCheck(id, pClass);
            }
        }
        
        return bFlag;
    }
    
    public boolean onHandleButton(ActionEvent e, String id)
    {
        boolean handled = handleStandardLabelSelection(e, id);
        if (!handled)
        {
            if (id.equals("MBK_SHOW_FINALSTATE_NAME"))
            {
                ILabelManager labelMgr = getLabelManager();
                if (labelMgr != null)
                {
                    boolean isDisplayed = labelMgr.isDisplayed(TSLabelKind.TSLK_NAME);
                    labelMgr.showLabel(TSLabelKind.TSLK_NAME, isDisplayed ? false : true);
                    invalidate();
                }
                handled = true;
            }
        }
        if (!handled)
        {
            handled = super.onHandleButton(e, id);
        }
        return handled;
    }
    
    public void onContextMenu(IMenuManager manager)
    {
        // Add the context menu items dealing with finalstate
        addFinalStateMenuItems(manager);
        
        // Add the stereotype label pullright
        addStandardLabelsToPullright(StandardLabelKind.SLK_STEREOTYPE, manager);
        
        super.onContextMenu(manager);
    }
    
    /**
     * Adds FinalState specific stuff.
     * *
     * @param pContextMenu [in] The context menu about to be displayed
     */
    protected void addFinalStateMenuItems(IMenuManager manager)
    {
        IMenuManager subMenu = manager.createOrGetSubMenu(loadString("IDS_LABELS_TITLE"), "");
        if (subMenu != null)
        {
            subMenu.add(createMenuAction(loadString("IDS_SHOW_FINALSTATENAME"), "MBK_SHOW_FINALSTATE_NAME"));
            //manager.add(subMenu);
        }
    }
    
    /**
     * This is the name of the drawengine used when storing and reading from the product archive
     *
     * @param sID A unique identifier for this draw engine.  Used when persisting to the etlp file.
     */
    public String getDrawEngineID()
    {
        return "FinalStateDrawEngine";
    }
    
    /**
     * Is this draw engine valid for the element it is representing?
     *
     * @param bIsValid[in] true if this draw engine can correctly represent the attached model element.
     */
    public boolean isDrawEngineValidForModelElement()
    {
        boolean isValid = false;
        
        // Make sure we're a control node
        // DecisionNode, FlowFinalNode, ForkNode, InitialNode, JoinNode, MergeNode &
        // ActivityFinalNode
        String metaType = getMetaTypeOfElement();
        if (metaType.equals("FinalState"))
        {
            isValid = true;
        }
        
        return isValid;
    }
    
    /**
     * Returns the metatype of the label manager we should use
     *
     * @param return The metatype in essentialconfig.etc that defines the label manager
     */
    public String getManagerMetaType(int nManagerKind)
    {
        return nManagerKind == MK_LABELMANAGER ? "FinalStateLabelManager" : "";
        //		String sManager = null;
        //		if (nManagerKind == MK_LABELMANAGER) {
        //			IElement pEle = getFirstModelElement();
        //			if (pEle != null && pEle instanceof IFinalState) {
        //				sManager = "FinalStateLabelManager";
        //			}
        //		}
        //		return sManager;
    }
    
    /**
     * During reconnection of an edge this flag is used to determine if a specified connector should be created
     */
    public int getReconnectConnector(IPresentationElement pEdgePE)
    {
        return ReconnectEdgeCreateConnectorKind.RECCK_DONT_CREATE;
    }
    
}


