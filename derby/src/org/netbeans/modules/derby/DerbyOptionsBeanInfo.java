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

package org.netbeans.modules.derby;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

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
    
    public Image getIcon(int type)
    {
        Image image = null;
        
        if (type == BeanInfo.ICON_COLOR_16x16) {
            image = Utilities.loadImage("org/netbeans/modules/derby/resources/optionsIcon16.png"); // NOI18N
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            image = Utilities.loadImage("org/netbeans/modules/derby/resources/optionsIcon32.png"); // NOI18N
        }
        
        return image != null ? image : super.getIcon(type);
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(DerbyOptions.class);
        descriptor.setName(NbBundle.getMessage(DerbyOptionsBeanInfo.class, "LBL_DerbyOptions"));
        return descriptor;
    }
}
