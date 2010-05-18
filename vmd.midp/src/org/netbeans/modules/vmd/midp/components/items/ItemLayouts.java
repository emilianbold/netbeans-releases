/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
