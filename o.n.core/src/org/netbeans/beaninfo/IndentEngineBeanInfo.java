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

package org.netbeans.beaninfo;

import java.awt.Image;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.Exceptions;

import org.openide.util.NbBundle;
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
        descr.setValue("global", Boolean.TRUE); // NOI18N
        
        return descr;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
            Exceptions.printStackTrace(ie);
            return null;
        }
    }

    /**
    * Return the icon
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("org/netbeans/core/resources/indentEngines.gif"); // NOI18N
        else
            return Utilities.loadImage("org/netbeans/core/resources/indentEngines.gif"); // NOI18N
    }
}
