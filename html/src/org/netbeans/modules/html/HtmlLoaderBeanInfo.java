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

package org.netbeans.modules.html;

import java.beans.*;
import java.awt.Image;

import org.openide.TopManager;
import org.openide.loaders.UniFileLoader;
import org.openide.util.Utilities;

/** Html data loader bean info.
*
* @author Dafe Simonek
*/
public class HtmlLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (UniFileLoader.class) };
        } catch (IntrospectionException ie) {
	    TopManager.getDefault().getErrorManager().notify(ie);
            return null;
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon(final int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
	    return Utilities.loadImage("org/netbeans/modules/html/htmlLoader.gif"); // NOI18N
        } else {
	    return Utilities.loadImage ("org/netbeans/modules/html/htmlLoader32.gif"); // NOI18N
        }
    }

}
