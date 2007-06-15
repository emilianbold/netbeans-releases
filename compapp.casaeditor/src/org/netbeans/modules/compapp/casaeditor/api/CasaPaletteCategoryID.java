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
package org.netbeans.modules.compapp.casaeditor.api;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jsandusky
 */
public class CasaPaletteCategoryID {

    private List<CasaPaletteItemID> mItems;
    private String mCategoryName;
    
    
    public CasaPaletteCategoryID(String categoryName) {
        mCategoryName = categoryName;
    }
    
    public String getName() {
        return mCategoryName;
    }
    
    @Override
    public int hashCode() {
        // Only the id number should be used when determining equality.
        return mCategoryName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CasaPaletteCategoryID) {
            // Only the id number should be used when determining equality.
            return mCategoryName.equals(((CasaPaletteCategoryID) obj).mCategoryName);
        }
        return false;
    }
    
    public List<CasaPaletteItemID> getItems() {
        return mItems;
    }
    
    public void addItem(CasaPaletteItemID item) {
        if (mItems == null) {
            mItems = new ArrayList<CasaPaletteItemID>();
        }
        mItems.add(item);
    }
}
