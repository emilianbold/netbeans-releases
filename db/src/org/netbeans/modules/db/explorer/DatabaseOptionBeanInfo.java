/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

import java.beans.*;
import java.awt.*;
import java.sql.Connection;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** A BeanInfo for DatabaseOption
*/
public class DatabaseOptionBeanInfo extends SimpleBeanInfo {
    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    private static Image image = null;
    private static Image image32 = null;

    static {
        try {
            desc = new PropertyDescriptor[] {
                new PropertyDescriptor("debugMode", DatabaseOption.class), //NOI18N
                new PropertyDescriptor("fetchLimit", DatabaseOption.class), //NOI18N
                new PropertyDescriptor("fetchStep", DatabaseOption.class), //NOI18N
                new PropertyDescriptor("autoConn", DatabaseOption.class) //NOI18N
            };

            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
            desc[0].setDisplayName(bundle.getString("PROP_DEBUG_MODE")); //NOI18N
            desc[0].setShortDescription(bundle.getString ("HINT_DEBUG_MODE")); //NOI18N
            desc[1].setDisplayName(bundle.getString("PROP_FETCH_LIMIT")); //NOI18N
            desc[1].setShortDescription(bundle.getString ("HINT_FETCH_LIMIT")); //NOI18N
            desc[2].setDisplayName(bundle.getString("PROP_FETCH_STEP")); //NOI18N
            desc[2].setShortDescription(bundle.getString ("HINT_FETCH_STEP")); //NOI18N
            desc[3].setDisplayName(bundle.getString("PROP_SAMPLE_AUTO_CONN")); //NOI18N
            desc[3].setShortDescription(bundle.getString ("HINT_SAMPLE_AUTO_CONN")); //NOI18N
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors ()
    {
        return desc;
    }

    public Image getIcon(int type)
    {
        if (type == BeanInfo.ICON_COLOR_16x16) {
            if (image == null) image = Toolkit.getDefaultToolkit().getImage(DatabaseOptionBeanInfo.class.getResource ("/org/netbeans/modules/db/resources/optionIcon.gif")); //NOI18N
            return image;
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            if (image32 == null) image32 = Toolkit.getDefaultToolkit().getImage(DatabaseOptionBeanInfo.class.getResource ("/org/netbeans/modules/db/resources/optionIcon32.gif")); //NOI18N
            return image32;
        }

        return super.getIcon(type);
    }
}
