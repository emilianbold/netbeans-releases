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

package org.netbeans.modules.db.sql.loader;

import java.beans.*;
import java.awt.Image;

import org.openide.ErrorManager;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** 
 *
 * @author Jesse Beaumont
 */
public class SQLDataLoaderBeanInfo extends SimpleBeanInfo {
    
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo(UniFileLoader.class) };
        } catch (IntrospectionException ie) {
	    ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    public Image getIcon(int type) {
        if (type == java.beans.BeanInfo.ICON_COLOR_16x16 ||
                type == java.beans.BeanInfo.ICON_MONO_16x16) {
	    return Utilities.loadImage("org/netbeans/modules/db/sql/loader/resources/sql16.gif");
        } else {
	    return Utilities.loadImage ("org/netbeans/modules/db/sql/loader/resources/sql16.gif");
        }
    }
}
