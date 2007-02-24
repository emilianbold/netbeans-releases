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

/*
 * PaletteElement.java
 *
 * Created on March 18, 2005, 10:08 AM
 */

package org.netbeans.modules.uml.palette.model;

import java.awt.Image;
import java.beans.BeanInfo;
import org.openide.util.NbBundle;


/**
 *
 * @author Thuy
 */
public class PaletteElement implements ModelingPaletteNodeDescriptor {
    private String name;
    private Image icon16;
    private Image icon32;
    private String toolTipKey;
    private String buttonID;

    /** Creates a new instance of PaletteElement */
    public PaletteElement(String name, String toolTipKey, Image smallIcon, Image bigIcon, String buttonID) {
        this.name = name;
        this.icon16 = smallIcon;
        this.icon32 = bigIcon;
        this.toolTipKey = toolTipKey;
        this.buttonID = buttonID;
    }
    
    
    public Image getIcon(int type) {
        Image icon = null;
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            icon = icon16;
        } else if (type == BeanInfo.ICON_COLOR_32x32 || type == BeanInfo.ICON_MONO_32x32) {
            icon = icon32;
        }    
        return icon;
    }
    
    
    public String getTooltip() {
        String val = NbBundle.getMessage(org.netbeans.modules.uml.palette.ui.ModelingPalette.class, toolTipKey);
        return (val != null ? val : "");
//        return toolTipKey;
    }
    
    public String getButtonID() {
        return buttonID;
    }
    
    public String getDisplayName() {
        return name;
    }
    
    public String getName() {
        return name;
    }
}
