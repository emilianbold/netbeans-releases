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
package com.sun.jsfcl.std.reference;

import java.util.List;
import java.util.StringTokenizer;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author eric
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StyleClassesReferenceDataDefiner extends ReferenceDataDefiner {

    public void addBaseItems(List list) {

        list.add(newItem(
            "", //NOI18N
            null,
            true,
            false));
    }

    public boolean canAddRemoveItems() {
        return false;
    }
    
    public boolean canOrderItems(){
        return false;
    }

    public boolean isValueAString() {

        return true;
    }

    public boolean definesDesignPropertyItems() {
        return true;
    }

    /**
     *  Get the Style Class List stored in the DesignContext Context Data
     *  The data is stored as [class1, class2, class2]. So the chars '['
     *  and ']' should be trimmed
     **/
    public void addDesignPropertyItems(DesignProperty liveProperty, List list) {
        DesignContext liveContext = liveProperty.getDesignBean().getDesignContext();
        String StyleClassList = (String)liveContext.getContextData(Constants.ContextData.
            CSS_STYLE_CLASS_DESCRIPTORS);
        if (StyleClassList != null) {
            StringTokenizer st = new StringTokenizer(StyleClassList, ","); //NOI18N
            while (st.hasMoreTokens()) {
                String className = trimChars(st.nextToken(), "[] ."); //NOI18
                list.add(newItem(
                    className,
                    className,
                    true,
                    false)
                    );
            }
        }
    }

    private String trimChars(String str, String toTrim) {
        boolean trim = false;
        char[] strChars = str.toCharArray();
        char[] trimChars = toTrim.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < strChars.length; i++) {
            for (int j = 0; j < trimChars.length; j++) {
                if (strChars[i] == trimChars[j]) {
                    trim = true;
                    break;
                }
            }
            if (!trim) {
                buffer.append(strChars[i]);
            }
            trim = false;
        }
        return buffer.toString();
    }
}
