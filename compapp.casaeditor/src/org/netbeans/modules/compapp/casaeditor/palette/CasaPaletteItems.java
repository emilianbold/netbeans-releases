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

import java.util.*;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteCategoryID;
import org.netbeans.modules.compapp.casaeditor.api.CasaPaletteItemID;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author rdara
 */
public class CasaPaletteItems extends Index.ArrayChildren {
   
    private CasaPaletteCategoryID mCategoryID;
    private Lookup mLookup;
    
   
    public CasaPaletteItems(CasaPaletteCategoryID categoryID, Lookup lookup) {
        this.mCategoryID = categoryID;
        mLookup = lookup;
    }

    protected List<Node> initCollection() {
        return addPaletteNodes();
    }

    private List<Node> addPaletteNodes() {
        List<Node> childrenNodes = new ArrayList<Node>();
        CasaPaletteItemID[] pluginItems = mCategoryID.getPlugin().getItemIDs(mCategoryID);
        if (pluginItems != null) {
            for (CasaPaletteItemID itemID : pluginItems) {
                childrenNodes.add(new CasaPaletteItemNode(itemID, mLookup));
            }
        }
        return childrenNodes;
    }
}
