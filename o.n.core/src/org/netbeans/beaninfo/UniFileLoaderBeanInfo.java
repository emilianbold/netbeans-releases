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

package org.netbeans.beaninfo;

import java.beans.*;

import org.openide.loaders.*;
import org.openide.util.NbBundle;

/** BeanInfo for {@link UniFileLoader}. */
public class UniFileLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor extensions = new PropertyDescriptor ("extensions", UniFileLoader.class); // NOI18N
            extensions.setDisplayName (NbBundle.getBundle (UniFileLoaderBeanInfo.class).getString ("PROP_UniFileLoader_extensions"));
            extensions.setShortDescription (NbBundle.getBundle (UniFileLoaderBeanInfo.class).getString ("HINT_UniFileLoader_extensions"));
            return new PropertyDescriptor[] { extensions };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

}

/*
 * Log
 *  3    Gandalf   1.2         1/13/00  Jaroslav Tulach I18N
 *  2    Gandalf   1.1         11/25/99 Jesse Glick     representationClass 
 *       expert loader property.
 *  1    Gandalf   1.0         11/3/99  Jesse Glick     
 * $
 */
