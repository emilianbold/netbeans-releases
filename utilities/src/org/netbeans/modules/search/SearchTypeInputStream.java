/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author  Marian Petras
 */
class SearchTypeInputStream extends java.io.ObjectInputStream {
    
    /** Creates a new instance of SearchTypeInputStream */
    public SearchTypeInputStream(InputStream in) throws IOException {
        super(in);
    }
    
    /**
     */
    protected Class resolveClass(java.io.ObjectStreamClass objectStreamClass)
            throws IOException, ClassNotFoundException {
        try {
            return super.resolveClass(objectStreamClass);
        } catch (ClassNotFoundException ex) {
            String className = objectStreamClass.getName();
            Class searchTypeClass = Utils.searchTypeForName(className);
            if (searchTypeClass != null) {
                return searchTypeClass;
            } else {
                throw ex;
            }
        }
    }
    
}
