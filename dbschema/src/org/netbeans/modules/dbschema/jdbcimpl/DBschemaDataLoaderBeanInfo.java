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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.awt.Image;
import java.beans.*;
import java.text.MessageFormat;

import org.openide.util.NbBundle;

public class DBschemaDataLoaderBeanInfo extends SimpleBeanInfo {

    static String getString (String key) {
        return NbBundle.getMessage(DBschemaDataLoaderBeanInfo.class, key);
    }

    static String getString (String key, Object o1) {
        return MessageFormat.format (getString (key), new Object[] { o1 });
    }

    static String getString (String key, Object o1, Object o2) {
        return MessageFormat.format (getString (key), new Object[] { o1, o2 });
    }
    
    static String getString (String key, Object o1, Object o2, Object o3) {
        return MessageFormat.format (getString (key), new Object[] { o1, o2, o3 });
    }
    
    static String getString (String key, Object o1, Object o2, Object o3, Object o4) {
        return MessageFormat.format (getString (key), new Object[] { o1, o2, o3, o4 });
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DBschemaDataLoader.class.getSuperclass ()) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) //NOI18N
                ie.printStackTrace ();
      
            return null;
        }
    }

    private static Image icon, icon32;
    
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("DBschemaDataIcon.gif"); //NOI18N
            
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("DBschemaDataIcon32.gif"); //NOI18N
            
            return icon32;
        }
    }

}
