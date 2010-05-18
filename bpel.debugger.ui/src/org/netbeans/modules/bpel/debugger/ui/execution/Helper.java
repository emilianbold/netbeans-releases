/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.execution;

import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.psm.PsmEntity;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.debugger.ui.util.HtmlUtil;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import static org.netbeans.modules.bpel.debugger.ui.execution.Constants.*;

/**
 *
 * @author Kirill Sorokin
 */
public final class Helper {
    
    private Helper() {
        // Does nothing
    }
    
    public static String getDisplayName(
            final Object object,
            final boolean showHtml) throws UnknownTypeException {
        
        if (object instanceof PsmEntity) {
            final PsmEntity psmEntity = (PsmEntity) object;
            final String label = makeLabel(
                    psmEntity.getTag(), psmEntity.getName());
            
            if (showHtml) {
                return HtmlUtil.toHtml(
                        label, false, false, NOT_YET_EXECUTED);
            } else {
                return label;
            }
        }
        
        if (object instanceof PemEntity) {
            final PemEntity pemEntity = (PemEntity) object;
            final PsmEntity psmEntity = pemEntity.getPsmEntity();
            
            String label = makeLabel(psmEntity.getTag(), psmEntity.getName());
            
            if (psmEntity.getParent() != null && 
                    psmEntity.getParent().isLoop()) {
                label = label + " [" + pemEntity.getIndex() + "]";
            }
            
            if (showHtml) {
                label = HtmlUtil.highlight(
                        label, isBold(pemEntity), false, getColor(pemEntity));
                
                return HtmlUtil.html(label);
            } else {
                return label;
            }
        }
        
        throw new UnknownTypeException(object);
    }
    
    public static String getIconBase(
            final Object object) throws UnknownTypeException {
        
        if (object instanceof PsmEntity) {
            final PsmEntity psmEntitiy = (PsmEntity)object;
            final String tag = psmEntitiy.getTag();
            
            String icon = myIconByTag.get(tag);
            if (icon == null) {
                icon = ICON_UNKNOWN;
            }
            
            return PSM_ICON_BASE + icon;
        } 
        
        if (object instanceof PemEntity) {
            final PemEntity pemEntity = (PemEntity)object;
            final String tag = pemEntity.getPsmEntity().getTag();
            
            String icon = myIconByTag.get(tag);
            if (icon == null) {
                icon = ICON_UNKNOWN;
            }
            
            return PEM_ICON_BASE + icon;
        }
        
        throw new UnknownTypeException(object);
    }
}
