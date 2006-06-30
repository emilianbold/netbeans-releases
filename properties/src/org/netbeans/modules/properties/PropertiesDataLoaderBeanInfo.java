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


package org.netbeans.modules.properties;


import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import org.openide.ErrorManager;

import org.openide.loaders.MultiFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** BeanInfo for properties loader.
 *
 * @author Ian Formanek
 */
public final class PropertiesDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    /**
     * @return Returns an array of PropertyDescriptors
     * describing the editable properties supported by this bean. */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {

            PropertyDescriptor p2 = new PropertyDescriptor(
                "extensions", // NOI18N
                PropertiesDataLoader.class,
                "getExtensions", // NOI18N
                "setExtensions"); // NOI18N

            p2.setDisplayName(NbBundle.getBundle(PropertiesDataLoaderBeanInfo.class).getString("PROP_Ext"));
            p2.setShortDescription(NbBundle.getBundle(PropertiesDataLoaderBeanInfo.class).getString("HINT_Ext"));

            return new PropertyDescriptor[] {p2};
        } catch(IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            
            return null;
        }
    }

    /** @param type Desired type of the icon
     * @return returns the properties loader's icon */
    public Image getIcon(final int type) {
        if((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            return Utilities.loadImage("org/netbeans/modules/properties/propertiesObject.png"); // NOI18N
        } else {
            return Utilities.loadImage("org/netbeans/modules/properties/propertiesObject32.gif"); // NOI18N
        }
    }
}
