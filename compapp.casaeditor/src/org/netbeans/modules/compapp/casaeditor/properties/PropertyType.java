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
package org.netbeans.modules.compapp.casaeditor.properties;

import java.awt.Color;
import org.openide.util.NbBundle;


/**
 *
 * @author nk160297
 */
public enum PropertyType {
    
    NAME(String.class),
    COLOR(Color.class);

    private Class<?> myClass;
    private String myDisplayName;
    private Class myPropertyEditorClass;

    PropertyType(Class aClass) {
        this(aClass, null);
    }
    
    PropertyType(Class<?> aClass, Class propertyEditorClass) {
        this.myClass = aClass;
        this.myPropertyEditorClass = propertyEditorClass;
    }
    
    public Class<?> getPropertyClass() {
        return myClass;
    }
    
    public String getDisplayName() {
        if (myDisplayName == null) {
            try {
                myDisplayName = NbBundle.getMessage(PropertyType.class, 
                        this.toString());
            } catch (Exception ex) {
                myDisplayName = name();
            }
        }
        return myDisplayName;
    }
    
    public Class getPropertyEditorClass() {
        return myPropertyEditorClass;
    }
}
