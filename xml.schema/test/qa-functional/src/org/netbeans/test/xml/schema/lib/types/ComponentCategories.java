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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.test.xml.schema.lib.types;

/**
 *
 * @author ca@netbeans.org
 */
public enum ComponentCategories {
    ATTRIBUTES(1),
    ATTRIBUTE_GROUPS(2),
    COMPLEX_TYPES(3),
    ELEMENTS(4),
    GROUPS(5),
    REFERENCED_SCHEMAS(6),
    SIMPLE_TYPES(7);
    
    private int val;
    
    ComponentCategories(int val) {
        this.val = val;
    }
    
    public int getValue() {
        return val;
    }
}
