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

/**
 *
 * @author rdara
 */
public class CasaPaletteItemID {
    
    // All ID's generate themselves off of a rolling count.
    private static int mCount;
    private int mID = mCount++;
    
    private CasaPalettePlugin mPlugin;
    private String mCategoryName;
    private String mDisplayName;
    private String mIconFileBase;
    private Object mDataObject;
    
    
    public CasaPaletteItemID(
            CasaPalettePlugin plugin, 
            String categoryName, 
            String displayName, 
            String iconFileBase) {
        mPlugin = plugin;
        mCategoryName = categoryName;
        mDisplayName = displayName;
        mIconFileBase = iconFileBase;
    }
    
    public String getCategory() {
        return mCategoryName;
    }
    
    public String getDisplayName() {
        return mDisplayName;
    }
    
    public String getIconFileBase() {
        return mIconFileBase;
    }
    
    public Object getDataObject() {
        return mDataObject;
    }
    
    public void setDataObject(Object data) {
        mDataObject = data;
    }
    
    public int hashCode() {
        // Only the id number should be used when determining equality.
        return mID;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof CasaPaletteItemID) {
            // Only the id number should be used when determining equality.
            return mID == ((CasaPaletteItemID) obj).mID;
        }
        return false;
    }
    
    public CasaPalettePlugin getPlugin() {
        return mPlugin;
    }
}
