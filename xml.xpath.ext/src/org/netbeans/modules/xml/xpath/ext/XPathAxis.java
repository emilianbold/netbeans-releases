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

package org.netbeans.modules.xml.xpath.ext;

/**
 * Be aware the following:
 *  - The "." is not a short form of self axis!
 *  - The ".." is not a short form of parent axis! 
 * They are not the axis at all but rather the abbreviated location steps.
 * 
 * @author nk160297
 */
public enum XPathAxis {
    SELF(null, "self"),  // NOI18N  
    CHILD("", "child"),  // NOI18N 
    PARENT(null, "parent"),  // NOI18N 
    ANCESTOR(null, "ancestor"),  // NOI18N 
    ATTRIBUTE("@", "attribute"),  // NOI18N 
    NAMESPACE(null, "namespace"),  // NOI18N 
    PRECEDING(null, "preceding"),  // NOI18N 
    FOLLOWING(null, "following"),  // NOI18N 
    DESCENDANT(null, "descendant"),  // NOI18N 
    ANCESTOR_OR_SELF(null, "ancestor-or-self"),  // NOI18N 
    DESCENDANT_OR_SELF(null, "descendant-or-self"),  // NOI18N 
    FOLLOWING_SIBLING(null, "following-sibling"),  // NOI18N 
    PRECEDING_SIBLING(null, "preceding-sibling");  // NOI18N
    
    private String mAbbreviatedName;
    private String mFullName;
    
    XPathAxis(String abbreviatedName, String fullName) {
        mAbbreviatedName = abbreviatedName;
        mFullName = fullName;
    }
    
    public String getShortForm() {
        if (mAbbreviatedName != null) {
            return mAbbreviatedName;
        } else {
            return mFullName + "::";
        }
    }
    
    public String getLongForm() {
        if (mFullName != null) {
            return mFullName + "::";
        } else {
            return mAbbreviatedName;
        }
    }
    
    public String getAbbreviatedName() {
        return mAbbreviatedName;
    }

    public String getFullName() {
        return mFullName;
    }
}
