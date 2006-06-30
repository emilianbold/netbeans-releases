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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.generator;

import java.util.*;

import org.netbeans.tax.*;

/**
 * Maps element declaration name => Entry.
 *
 * @author  Petr Kuzel
 * @version
 */
public class ElementDeclarations extends HashMap {

    /** Serial Version UID */
    private static final long serialVersionUID =2385299250969298335L;

    /**
     * Creates new ElementDeclarations from TreeElementDecl iterator.
     */
    public ElementDeclarations(Iterator it) {
        if (it == null) return;
        while (it.hasNext()) {
            TreeElementDecl next = (TreeElementDecl) it.next();
            put(next.getName(), new Entry(next.allowText(), next.allowElements()));
        }
    }
    
    /**
     * Get Entry by declaration name.
     */
    public final Entry getEntry(String element) {
        return (Entry) get(element);
    }
    
    /**
     * Entry represents one value keyed by element declaration name. 
     */
    public static class Entry {

        public static final int EMPTY = 0;
        public static final int DATA = 1;
        public static final int CONTAINER = 2;
        public static final int MIXED = 3;
        
        private int type;
        
        public Entry(boolean at, boolean ae) {
            type = at ? DATA : 0;
            type += ae ? CONTAINER : 0;
        }

        public int getType() {
            return type;
        }
    }
}
