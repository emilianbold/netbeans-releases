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

import org.netbeans.modules.java.JavaDataLoader;
import org.openide.ErrorManager;

/** JSP/Servlet loader bean info.
*
* @author Jesse Glick
*/
public class JspServletDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (JavaDataLoader.class) };
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
            // [PENDING] a different icon would be good
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/web/core/resources/jspObject.gif"); // NOI18N
        } else {
            return org.openide.util.Utilities.loadImage ("org/netbeans/modules/web/core/resources/jspObject32.gif"); // NOI18N
        }
    }

}

