/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.*;

/**
 * Editor for property of type java.lang.Boolean
 *
 * @author  Josef Kozak
 */
public class BooleanEditor extends WrappersEditor {
    
    public BooleanEditor() {
        super(java.lang.Boolean.TYPE);
    }
    
    public String getJavaInitializationString() {
        Boolean val = (Boolean) getValue();
        return Boolean.TRUE.equals(val) ? "java.lang.Boolean.TRUE" :
            "java.lang.Boolean.FALSE";
    }
}
