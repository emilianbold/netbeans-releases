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


package org.netbeans.modules.form;

import java.beans.*;
import java.awt.Image;

import org.netbeans.modules.java.JavaDataLoader;
import org.openide.util.Utilities;

/** Form data loader bean info.
 *
 * @author Ian Formanek
 */
public class FormDataLoaderBeanInfo extends SimpleBeanInfo {
    
    /** The icons for Form */
    private static String iconURL = "/org/netbeans/modules/form/resources/form.gif"; // NOI18N
    private static String icon32URL = "/org/netbeans/modules/form/resources/form32.gif"; // NOI18N    

    
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo(JavaDataLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace();
            return null;
        }
    }

    
    /** @param type Desired type of the icon
     * @return returns the Form loader's icon
     */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage(iconURL);
        } else {
            return Utilities.loadImage(icon32URL);
        }
    }

}
