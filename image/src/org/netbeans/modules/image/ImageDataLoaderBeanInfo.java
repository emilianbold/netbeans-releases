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

package org.netbeans.modules.image;

import java.beans.*;
import java.awt.Image;

import org.openide.loaders.UniFileLoader;

/** Image data loader bean info.
*
* @author Dafe Simonek
*/
public class ImageDataLoaderBeanInfo extends SimpleBeanInfo {

    /** Icons for image data loader. */
    private static Image icon;
    private static Image icon32;

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/image/imageObject.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/image/imageObject32.gif"); // NOI18N
            return icon32;
        }
    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/16/00  Jesse Glick     
 *  8    Gandalf   1.7         1/5/00   Ian Formanek    NOI18N
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         4/13/99  Jesse Glick     Clean-ups of comments 
 *       and such for public perusal.
 *  4    Gandalf   1.3         3/22/99  Ian Formanek    Icons moved from 
 *       modules/resources to this package
 *  3    Gandalf   1.2         2/16/99  David Simonek   
 *  2    Gandalf   1.1         1/22/99  Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
