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

    
    //----------------------------------------------------------------------    
    
    
    /**
     * This method is intended for use when generating Java code to set
     * the value of the property.  It should return a fragment of Java code
     * that can be used to initialize a variable with the current property
     * value.
     * <p>
     * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
     *
     * @return A fragment of Java code representing an initializer for the
     *   	current value.
     */
    public String getJavaInitializationString() {
        String gat = getAsText();
        if (gat.equals("True")) return "java.lang.Boolean.TRUE"; // NOI18N
        else return "java.lang.Boolean.FALSE"; // NOI18N
    }

}
