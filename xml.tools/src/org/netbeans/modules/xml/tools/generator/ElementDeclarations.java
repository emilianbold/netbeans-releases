/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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
