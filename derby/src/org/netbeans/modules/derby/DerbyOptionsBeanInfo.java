/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.derby;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DerbyOptionsBeanInfo extends SimpleBeanInfo {
    
    public DerbyOptionsBeanInfo() {
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] descriptors = new PropertyDescriptor[2];
            descriptors[0] = new PropertyDescriptor(DerbyOptions.PROP_DERBY_LOCATION, DerbyOptions.class);
            descriptors[0].setDisplayName(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "LBL_DerbyLocation"));
            descriptors[0].setShortDescription(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "HINT_DerbyLocation"));
            descriptors[1] = new PropertyDescriptor(DerbyOptions.PROP_DERBY_SYSTEM_HOME, DerbyOptions.class);
            descriptors[1].setDisplayName(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "LBL_DatabaseLocation"));
            descriptors[1].setShortDescription(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "HINT_DatabaseLocation"));
            return descriptors;
        } catch (IntrospectionException ex) {
            ErrorManager.getDefault().notify(ex);
            return new PropertyDescriptor[0];
        }
    }
}
