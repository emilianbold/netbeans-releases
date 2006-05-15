/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class JDKSelectionPanelBeanInfo extends SimpleBeanInfo {
    
    private PropertyDescriptor[] pds = null;
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        if (pds == null) {
            try {
                Class panelClass = org.netbeans.installer.JDKSelectionPanel.class;
                pds = new PropertyDescriptor[1];
                pds[0] = new PropertyDescriptor("jdkHome",panelClass);
            } catch (Exception e) {
                throw new Error();
            }
        }
        return pds;
    }
}

