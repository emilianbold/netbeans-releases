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

package org.netbeans.modules.web.core.jsploader;

import java.beans.*;
import java.awt.Image;
import org.openide.ErrorManager;

import org.openide.loaders.UniFileLoader;

/** JSP loader bean info.
*
* @author Petr Jiricka
*/
public class JspLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault ().notify (ie);
            return null;
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            return org.openide.util.Utilities.loadImage ("org/netbeans/modules/web/core/resources/jspObject.gif"); // NOI18N
        } else {
            return org.openide.util.Utilities.loadImage ("org/netbeans/modules/web/core/resources/jspObject32.gif"); // NOI18N
        }
    }

}

