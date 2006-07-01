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

package org.netbeans.core.projects;

import java.awt.Image;
import java.beans.*;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.openide.util.Exceptions;

import org.openide.util.Utilities;

/** Object that provides beaninfo for a SystemFileSystem.
*
* @author Ian Formanek
* // JST-PENDING this bean info in not in right place...
*/
public class SystemFileSystemBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor descr = new BeanDescriptor (SystemFileSystem.class);
        descr.setValue ("helpID", SystemFileSystem.class.getName ()); // NOI18N
        return descr;
    }
    
    /** Provides the LocalFileSystem's icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return Utilities.loadImage("org/netbeans/core/resources/systemFS.gif"); // NOI18N
        else
            return null;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        BeanInfo[] beanInfos = new BeanInfo [1];
        beanInfos[0] = new org.netbeans.beaninfo.FileSystemBeanInfo ();
        return beanInfos;
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[1];
            desc[0] = new PropertyDescriptor ("propagateMasks", SystemFileSystem.class, "getPropagateMasks", null);
            desc[0].setHidden (true);
    	    return desc;
        } catch (IntrospectionException ie) {
	    Exceptions.printStackTrace(ie);
	    return null;
        }
    }
}
