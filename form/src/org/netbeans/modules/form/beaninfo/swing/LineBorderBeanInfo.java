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

package org.netbeans.modules.form.beaninfo.swing;

import java.beans.*;
import javax.swing.border.LineBorder;

public class LineBorderBeanInfo extends BISupport {
    
    public LineBorderBeanInfo() {
        super("lineBorder", javax.swing.border.LineBorder.class); // NOI18N
    }

    protected PropertyDescriptor[] createPropertyDescriptors() throws IntrospectionException {
        return new PropertyDescriptor[] {
            createRO(LineBorder.class, "lineColor"), // NOI18N
            createRO(LineBorder.class, "thickness"), // NOI18N
            createRO(LineBorder.class, "roundedCorners"), // NOI18N
        };
    }    
}
