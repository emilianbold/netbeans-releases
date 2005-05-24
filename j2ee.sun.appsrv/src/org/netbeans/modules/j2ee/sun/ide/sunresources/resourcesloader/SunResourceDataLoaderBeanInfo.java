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
package org.netbeans.modules.j2ee.sun.ide.sunresources.resourcesloader;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import java.beans.IntrospectionException;

import org.openide.ErrorManager;
import org.openide.util.Utilities;

/** Description of {@link SunResourceDataLoader}.
 *
 * @author nityad
 */
public class SunResourceDataLoaderBeanInfo extends SimpleBeanInfo {
    
    // If you have additional properties:
    /*
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor myProp = new PropertyDescriptor("myProp", SunResourceDataLoader.class);
            myProp.setDisplayName(NbBundle.getMessage(SunResourceDataLoaderBeanInfo.class, "PROP_myProp"));
            myProp.setShortDescription(NbBundle.getMessage(SunResourceDataLoaderBeanInfo.class, "HINT_myProp"));
            return new PropertyDescriptor[] {myProp};
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
     */
    
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            // I.e. MultiFileLoader.class or UniFileLoader.class.
            return new BeanInfo[] {Introspector.getBeanInfo(SunResourceDataLoader.class.getSuperclass())};
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault().notify(ie);
            return null;
        }
    }
    
    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage("org/netbeans/modules/j2ee/sun/ide/resources/sun-cluster_16_pad.gif", true); //NOI18N
        } else {
            return Utilities.loadImage("org/netbeans/modules/j2ee/sun/ide/resources/sun-cluster_16_pad32.gif", true); //NOI18N
        }
    }
    
}
