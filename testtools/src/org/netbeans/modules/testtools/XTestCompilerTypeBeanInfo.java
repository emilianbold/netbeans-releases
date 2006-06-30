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

package org.netbeans.modules.testtools;

/*
 * XTestCompilerTypeBeanInfo.java
 *
 * Created on April 29, 2002, 10:54 AM
 */

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.BeanDescriptor;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

import org.openide.util.Utilities;
import org.openide.compiler.CompilerType;
import org.openide.util.NbBundle;

/** Bena Info class for XTestCompilerType
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestCompilerTypeBeanInfo extends SimpleBeanInfo {

    /** returns Bean Infor of ancestor class
     * @return BeanInfo */    
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo(CompilerType.class)
                   };
        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    /** returns Bean Descriptor of XTestCompilerType
     * @return BeanDescriptor */    
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (XTestCompilerType.class);
        desc.setDisplayName (NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "Compilation_Name")); // NOI18N
        desc.setShortDescription (NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "Compilation_Description")); // NOI18N
        desc.setValue ("version", "1.1"); // NOI18N
        return desc;
    }

    /** returns array of Property Descriptors of XTestCompilerType properties
     * @return array of PropertyDescriptors */    
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] properties=new PropertyDescriptor[5];
            properties[0]=new PropertyDescriptor("netbeansHome", XTestCompilerType.class); // NOI18N
            properties[0].setDisplayName(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "NetbeansHomeName")); // NOI18N
            properties[0].setShortDescription(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "NetbeansHomeDescription")); // NOI18N
            properties[0].setValue("files",new Boolean(false)); // NOI18N
            properties[0].setPreferred(true);
            properties[1]=new PropertyDescriptor("jellyHome", XTestCompilerType.class); // NOI18N
            properties[1].setDisplayName(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "JellyHomeName")); // NOI18N
            properties[1].setShortDescription(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "JellyHomeDescription")); // NOI18N
            properties[1].setValue("files",new Boolean(false)); // NOI18N
            properties[2]=new PropertyDescriptor("jemmyHome", XTestCompilerType.class); // NOI18N
            properties[2].setDisplayName(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "JemmyHomeName")); // NOI18N
            properties[2].setShortDescription(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "JemmyHomeDescription")); // NOI18N
            properties[2].setValue("files",new Boolean(false)); // NOI18N
            properties[3]=new PropertyDescriptor("testType", XTestCompilerType.class); // NOI18N
            properties[3].setDisplayName(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "TestTypeName")); // NOI18N
            properties[3].setShortDescription(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "TestTypeDescription")); // NOI18N
            properties[4]=new PropertyDescriptor("xtestHome", XTestCompilerType.class); // NOI18N
            properties[4].setDisplayName(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "XTestHomeName")); // NOI18N
            properties[4].setShortDescription(NbBundle.getMessage(XTestCompilerTypeBeanInfo.class, "XTestHomeDescription")); // NOI18N
            properties[4].setValue("files",new Boolean(false)); // NOI18N
            return properties;
        } catch( IntrospectionException e) {
            org.openide.ErrorManager.getDefault().notify(e);
            e.printStackTrace();
            return null;
        }
    }

    /** returns XTestIcon
     * @param type int icon type
     * @return Image XTestIcon */    
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("org/netbeans/modules/testtools/XTestCompIcon.gif"); // NOI18N
        } else {
            return null;
        }
    }
    
}
