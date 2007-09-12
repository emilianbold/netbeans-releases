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
