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
package org.netbeans.modules.mashup.db.ui;

import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.logger.LogUtil;
import org.openide.ErrorManager;
import org.openide.util.Utilities;

/**
 *
 * @author karthikeyan s
 */
public class AxionDBConfigurationBeanInfo extends SimpleBeanInfo {

    private static transient final Logger mLogger = LogUtil.getLogger(AxionDBConfigurationBeanInfo.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /**
     * Creates a new instance of AxionDBConfigurationBeanInfo
     */
    public AxionDBConfigurationBeanInfo() {
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] descriptors = new PropertyDescriptor[2];
            descriptors[0] = new PropertyDescriptor(AxionDBConfiguration.PROP_LOC, AxionDBConfiguration.class);
            String nbBundle1 = mLoc.t("PRSR001: Mashup Database Location");
            descriptors[0].setDisplayName(Localizer.parse(nbBundle1));
            String nbBundle2 = mLoc.t("PRSR001: The directory where the Mashup Database should be created.");
            descriptors[0].setShortDescription(Localizer.parse(nbBundle2));
            descriptors[1] = new PropertyDescriptor(AxionDBConfiguration.PROP_DRV_LOC, AxionDBConfiguration.class);
            String nbBundle3 = mLoc.t("PRSR001: MashupDB Driver Location");
            descriptors[1].setDisplayName(Localizer.parse(nbBundle3));
            String nbBundle4 = mLoc.t("PRSR001: The directory where the MashupDB Driver(MashupDB.zip) is located.");
            descriptors[1].setShortDescription(Localizer.parse(nbBundle4));
            return descriptors;
        } catch (IntrospectionException ex) {
            ErrorManager.getDefault().notify(ex);
            return new PropertyDescriptor[0];
        }
    }

    public Image getIcon(int type) {
        Image image = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            image = Utilities.loadImage("org/netbeans/modules/sql/framework/ui/resources/images/DatabaseProperties.png"); // NOI18N
        }

        return image != null ? image : super.getIcon(type);
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(AxionDBConfiguration.class);
        descriptor.setName("Mashup Database Configuration");
        return descriptor;
    }
}
