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

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */
package org.netbeans.modules.dm.virtual.db.ui;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class AxionDBConfigurationBeanInfo extends SimpleBeanInfo {

    public AxionDBConfigurationBeanInfo() {
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] descriptors = new PropertyDescriptor[2];
            descriptors[0] = new PropertyDescriptor(AxionDBConfiguration.PROP_LOC, AxionDBConfiguration.class);
            descriptors[0].setDisplayName(NbBundle.getMessage(AxionDBConfigurationBeanInfo.class, "LBL_AxionDatabaseLocation"));
            descriptors[0].setShortDescription(NbBundle.getMessage(AxionDBConfigurationBeanInfo.class, "TIP_AxionDatabaseLocation"));
            descriptors[1] = new PropertyDescriptor(AxionDBConfiguration.PROP_DRV_LOC, AxionDBConfiguration.class);
            descriptors[1].setDisplayName(NbBundle.getMessage(AxionDBConfigurationBeanInfo.class, "LBL_AxionDriverLocation"));
            descriptors[1].setShortDescription(NbBundle.getMessage(AxionDBConfigurationBeanInfo.class, "TIP_AxionDriverLocation"));
            return descriptors;
        } catch (IntrospectionException ex) {
            ErrorManager.getDefault().notify(ex);
            return new PropertyDescriptor[0];
        }
    }

    @Override
    public Image getIcon(int type) {
        Image image = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            image = Utilities.loadImage("org/netbeans/modules/dm/virtual/db/ui/resources/images/DatabaseProperties.png"); // NOI18N
        }

        return image != null ? image : super.getIcon(type);
    }

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(AxionDBConfiguration.class);
        descriptor.setName(NbBundle.getMessage(AxionDBConfigurationBeanInfo.class, "LBL_AxionConf"));
        return descriptor;
    }
}
