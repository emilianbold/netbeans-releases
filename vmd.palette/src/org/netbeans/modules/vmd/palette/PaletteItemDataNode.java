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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.vmd.palette;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Anton Chechel
 */
public class PaletteItemDataNode extends DataNode {
    private static Image errorBadge;
    
    private PaletteItemDataObject obj;
    private Lookup lookup;
    private boolean isValid = true;
    private boolean needCheck = true;
    
    static {
        errorBadge = Utilities.loadImage("org/netbeans/modules/vmd/palette/resources/error-badge.gif"); // NOI18N
    }
    
    public PaletteItemDataNode(PaletteItemDataObject obj) {
        super(obj, Children.LEAF, Lookups.singleton(obj));
        this.obj = obj;
        lookup = Lookups.singleton(this);
    }
    
    public String getDisplayName() {
        return obj.getDisplayName();
    }
    
    public String getShortDescription() {
        return obj.getToolTip();
    }
    
    public Image getIcon(int type) {
        if (needCheck) {
            PaletteMap.getInstance().checkValidity(getProjectType(), lookup);
        }
        
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            String iconPath = obj.getIcon();
            Image icon = null;
            if (iconPath != null) {
                icon = Utilities.loadImage(iconPath);
            }
            if (icon == null) {
                icon = super.getIcon(type);
            }
            if (!isValid) {
                icon = Utilities.mergeImages(icon, errorBadge, errorBadge.getWidth(null), errorBadge.getHeight(null));
            }
            return icon;
        }
        
        String iconPath = obj.getBigIcon();
        Image icon = null;
        if (iconPath != null) {
            icon = Utilities.loadImage(iconPath);
        }
        if (icon == null) {
            icon = super.getIcon(type);
        }
        if (!isValid) {
            icon = Utilities.mergeImages(icon, errorBadge, errorBadge.getWidth(null), errorBadge.getHeight(null));
        }
        return icon;
    }
    
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    
    String getProjectType() {
        return obj.getProjectType();
    }
    
    String getProducerID() {
        return obj.getProducerID();
    }
    
    void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    void setNeedCheck(boolean needCheck) {
        this.needCheck = needCheck;
    }

    public boolean canRename() {
        return false;
    }

    public boolean canDestroy() {
        return false;
    }

    public void destroy() throws IOException {
        super.destroy();
    }

    public boolean canCopy() {
        return false;
    }

    public boolean canCut() {
        return false;
    }
}
