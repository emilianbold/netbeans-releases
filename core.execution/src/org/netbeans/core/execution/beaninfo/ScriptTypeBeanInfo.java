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

package org.netbeans.core.execution.beaninfo;

import java.awt.Image;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openide.execution.ScriptType;
import org.openide.util.Utilities;

/** BeanInfo for ScriptType.
 * @author David Strupl
 */
public class ScriptTypeBeanInfo extends SimpleBeanInfo {
    
    public BeanDescriptor getBeanDescriptor () {
        return new BeanDescriptor(ScriptType.class);
    } 

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
            Logger.global.log(Level.WARNING, null, ie);
            return null;
        }
    }
    
    /**
    * Return the icon
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("org/netbeans/core/resources/scriptTypes.gif"); // NOI18N
        else
            return Utilities.loadImage("org/netbeans/core/resources/scriptTypes32.gif"); // NOI18N
    }

}
