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

public enum NodeATTR {
    FILE        ("fileName"), // NOI18N
    LINE        ("lineNumber"), // NOI18N
    DISPLNAME   ("displayedName"), // NOI18N
    QNAME       ("qualifiedName"), // NOI18N
    NAME        ("name"), // NOI18N
    TYPE        ("type"), // NOI18N
    PARAMS      ("params"), // NOI18N
    DWARFINFO   ("dwarfInfo"); // NOI18N
    
    private static final HashMap<String, NodeATTR> hashmap = new HashMap<String, NodeATTR>();
    private final String value;
    
    static {
        for (NodeATTR elem : NodeATTR.values()) {
            hashmap.put(elem.value, elem);
        }
    }
    
    NodeATTR(String value) {
        this.value = value;
    }
    
    public static NodeATTR get(String val) {
        return hashmap.get(val);
    }
    
    public String value() {
        return value;
    }    
}