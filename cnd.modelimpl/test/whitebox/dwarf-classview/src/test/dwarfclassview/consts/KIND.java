/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package test.dwarfclassview.consts;

import java.util.HashMap;

public enum KIND {
    PROJECT     ("project"), // NOI18N
    OPERATOR    ("operator"), // NOI18N
    FIELD       ("field"), // NOI18N
    VARIABLE    ("variable"), // NOI18N
    CLASS       ("class"), // NOI18N
    NAMESPACE   ("namespace"), // NOI18N
    MEMBER      ("member"), // NOI18N
    TYPEDEF     ("typedef"), // NOI18N
    ENUM        ("enum"), // NOI18N
    UNION       ("union"), // NOI18N
    CONSTRUCTOR ("constructor"), // NOI18N
    DESTRUCTOR  ("destructor"), // NOI18N
    METHOD      ("method"), // NOI18N
    FUNCTION    ("function"), // NOI18N
    ENUMITEM    ("enum_item"), // NOI18N
    UNHANDLED_KIND("UNHANDLED_KIND"); // NOI18N
    
    private static final HashMap<String, KIND> hashmap = new HashMap<String, KIND>();
    private final String value;
    
    static {
        for (KIND elem : KIND.values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    KIND(String value) {
        this.value = value;
    }
    
    public static KIND get(String val) {
        return hashmap.get(val);
    }
    
    public String value() {
        return value;
    }    
}