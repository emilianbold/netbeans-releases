/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web;

import java.beans.*;
import java.awt.Image;
import org.openide.ErrorManager;

import org.openide.loaders.MultiFileLoader;
import org.openide.util.Utilities;

/** Bean info for the deployment descriptor loader.
*
* @author Petr Jiricka
*/
public class DDDataLoaderBeanInfo extends SimpleBeanInfo {

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (MultiFileLoader.class) };
        } catch (IntrospectionException ie) {
            ErrorManager.getDefault ().notify (ErrorManager.EXCEPTION, ie);
            return null;
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Image loader's icon
    */
    public Image getIcon(final int type) {
        return Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"); // NOI18N
    }

}

