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

package org.netbeans.modules.vmd.midp.components.items;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.vmd.midp.components.general.Bitmask;
import org.netbeans.modules.vmd.midp.propertyeditors.Bundle;

/**
 *
 * @author Karol Harezlak
 */
public final class ItemLayouts extends Bitmask {

    private List<BitmaskItem> bitmaskItems;

    public ItemLayouts(int bitmask) {
        super(bitmask);
    }

    public List<BitmaskItem> getBitmaskItems() {
        if (bitmaskItems == null) {
            bitmaskItems = new ArrayList<BitmaskItem>(14);
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_DEFAULT, Bundle.getMessage("LBL_ITEMLAYOUTPE_GEN_DEFAULT"),"LAYOUT_DEFAULT"));  // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_LEFT, Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_LEFT"),"LAYOUT_LEFT"));  // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_RIGHT, Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_RIGHT"),"LAYOUT_RIGHT")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_CENTER, Bundle.getMessage("LBL_ITEMLAYOUTPE_HOR_CENTER"),"LAYOUT_CENTER")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_TOP, Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_TOP"),"LAYOUT_TOP")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_BOTTOM , Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_BOTTOM"),"LAYOUT_BOTTOM ")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_VCENTER, Bundle.getMessage("LBL_ITEMLAYOUTPE_VER_CENTER"),"LAYOUT_VCENTER")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_BEFORE, Bundle.getMessage("VALUE_LAYOUT_NEWLINE_BEFORE"),"LAYOUT_NEWLINE_BEFORE")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_NEWLINE_AFTER, Bundle.getMessage("LBL_ITEMLAYOUTPE_NL_AFTER"),"LAYOUT_NEWLINE_AFTER")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_SHRINK, Bundle.getMessage("PNL_ITEMLAYOUTPE_SHRINK"),"LAYOUT_SHRINK")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_VSHRINK, Bundle.getMessage("LBL_ITEMLAYOUTPE_SH_HORIZONTAL"),"LAYOUT_VSHRINK")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_EXPAND, Bundle.getMessage("PNL_ITEMLAYOUTPE_EXPAND"),"LAYOUT_EXPAND")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_VEXPAND, Bundle.getMessage("LBL_ITEMLAYOUTPE_EX_VERTICAL"), "LAYOUT_VEXPAND")); // NOI18N
            bitmaskItems.add(new BitmaskItem(ItemCD.VALUE_LAYOUT_2, Bundle.getMessage("LBL_ITEMLAYOUTPE_GEN_MIDP2"), "LAYOUT_2")); // NOI18N
        }
        
        return bitmaskItems;
    }
}
