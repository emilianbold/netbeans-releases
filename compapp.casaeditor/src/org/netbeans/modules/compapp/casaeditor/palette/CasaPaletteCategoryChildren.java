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

package org.netbeans.modules.compapp.casaeditor.palette;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteCategoryID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPalettePlugin;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author rdara
 */
public class CasaPaletteCategoryChildren extends Children.Keys {
    
    private Lookup mLookup;
    
    public CasaPaletteCategoryChildren(Lookup lookup) {
        mLookup = lookup;
    }
    
    
    protected Node[] createNodes(Object key) {
        CasaPaletteCategoryID categoryID = (CasaPaletteCategoryID) key;
        return new Node[] { new CasaPaletteCategoryNode(categoryID, mLookup) };
    }
    
    protected void addNotify() {
        super.addNotify();
        
        java.util.Map<String,CasaPaletteCategoryID> categoryMap = new HashMap<String,CasaPaletteCategoryID>();
        List<CasaPaletteCategoryID> categories = new ArrayList<CasaPaletteCategoryID>();
        Collection<? extends CasaPalettePlugin> plugins = CasaPalette.getPlugins();
        for (CasaPalettePlugin plugin : plugins) {
            for (CasaPaletteItemID itemID : plugin.getItemIDs()) {
                String categoryName = itemID.getCategory();
                CasaPaletteCategoryID categoryID = categoryMap.get(categoryName);
                if (categoryID == null) {
                    categoryID = new CasaPaletteCategoryID(categoryName);
                    categoryMap.put(categoryName, categoryID);
                    categories.add(categoryID);
                }
                categoryID.addItem(itemID);
            }
        }

        setKeys(categories);
    }
}
