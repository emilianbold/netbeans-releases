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

package org.netbeans.modules.web.core.jsploader;

import java.beans.*;
import java.awt.Image;

import org.openide.ErrorManager;
import org.openide.loaders.MultiFileLoader;

/** JSP/Servlet loader bean info.
*
* @author Jesse Glick
*/
public class JspServletDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
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

