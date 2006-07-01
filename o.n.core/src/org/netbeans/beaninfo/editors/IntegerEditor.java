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

package org.netbeans.beaninfo.editors;

import java.beans.*;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.ExPropertyEditor;

/**
 * Editor for property of type java.lang.Integer
 *
 * @author  Josef Kozak
 */
public class IntegerEditor extends WrappersEditor {

    public IntegerEditor() {
        super(java.lang.Integer.TYPE);
    }

    public String getJavaInitializationString() {
    	return "new java.lang.Integer(" + pe.getJavaInitializationString() + ")"; // NOI18N
    }

    public void setAsText(String text) throws IllegalArgumentException {
        super.setAsText (text.trim());
    }    
}


