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


package org.netbeans.modules.pdf;


import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;

import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


/** BeanInfo for <code>PDFDataLoader</code>.
 *
 * @author Jesse Glick
 */
public class PDFDataLoaderBeanInfo extends SimpleBeanInfo {

    /** Gets additional bean infos. Overrides superclass method. */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** Gets icon. Overrides superclass method. */
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage("/org/netbeans/modules/pdf/PDFDataIcon.gif"); // NOI18N
        } else {
            return Utilities.loadImage("/org/netbeans/modules/PDFDataIcon32.gif"); // NOI18N
        }
    }

}
