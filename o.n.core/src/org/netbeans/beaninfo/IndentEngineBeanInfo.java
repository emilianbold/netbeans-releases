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

package org.netbeans.beaninfo;

import java.awt.Image;

import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.text.IndentEngine;
import org.openide.util.Utilities;

/** Object that provides beaninfo for all indentation engines.
*
* @author Jaroslav Tulach
*/
public class IndentEngineBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor (org.openide.text.IndentEngine.class);
        ResourceBundle bundle = NbBundle.getBundle(IndentEngineBeanInfo.class);
        descr.setDisplayName (bundle.getString ("LAB_IndentEngine")); // NOI18N
        descr.setShortDescription (bundle.getString ("HINT_IndentEngine")); // NOI18N
        descr.setValue ("helpID", "editing.indentation"); // NOI18N
        descr.setValue("global", Boolean.TRUE); // NOI18N
        
        return descr;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /**
    * Return the icon
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("/org/netbeans/core/resources/indentEngines.gif"); // NOI18N
        else
            return Utilities.loadImage("/org/netbeans/core/resources/indentEngines.gif"); // NOI18N
    }
}
