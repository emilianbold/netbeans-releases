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
 * XTestExecutorBeanInfo.java
 *
 * Created on April 29, 2002, 10:54 AM
 */

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import java.beans.BeanDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.beans.IntrospectionException;

import org.openide.util.Utilities;
import org.openide.execution.Executor;
import org.openide.util.NbBundle;

/** Bean Info class for XTestExecutor
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class XTestExecutorBeanInfo extends SimpleBeanInfo {

    /** returns Bean Info of ancestor class
     * @return BeanInfo */    
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo(Executor.class)
                   };
        } catch (IntrospectionException ie) {
            org.openide.ErrorManager.getDefault().notify(ie);
            return null;
        }
    }

    /** returns Bean Descriptors of XTestExecutor class
     * @return BeanDescriptor */    
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (XTestExecutor.class);
        desc.setDisplayName (NbBundle.getMessage(XTestExecutorBeanInfo.class, "XTestExecutionName")); // NOI18N
        desc.setShortDescription (NbBundle.getMessage(XTestExecutorBeanInfo.class, "XTestExecutionDescription")); // NOI18N
        desc.setValue (NbBundle.getMessage(XTestExecutorBeanInfo.class, "version"), "1.1"); // NOI18N
        return desc;
    }

    /** returns Bean Descriptors of XTestExecutor properties
     * @return array of BeanDescriptor */    
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] properties=new PropertyDescriptor[7];
            properties[0]=new PropertyDescriptor("netbeansHome", XTestExecutor.class); // NOI18N
            properties[0].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "NetbeansHomeName")); // NOI18N
            properties[0].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "NetbeansHomeDescription")); // NOI18N
            properties[0].setValue("files",new Boolean(false)); // NOI18N
            properties[0].setPreferred(true);
            properties[1]=new PropertyDescriptor("attributes", XTestExecutor.class); // NOI18N
            properties[1].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "ExecutionAttributesName")); // NOI18N
            properties[1].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "ExecutionAttributesDescription")); // NOI18N
            properties[2]=new PropertyDescriptor("jellyHome", XTestExecutor.class); // NOI18N
            properties[2].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "JellyHomeName")); // NOI18N
            properties[2].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "JellyHomeDescription")); // NOI18N
            properties[2].setValue("files",new Boolean(false)); // NOI18N
            properties[3]=new PropertyDescriptor("jemmyHome", XTestExecutor.class); // NOI18N
            properties[3].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "JemmyHomeName")); // NOI18N
            properties[3].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "JellyHomeDescription")); // NOI18N
            properties[3].setValue("files",new Boolean(false)); // NOI18N
            properties[4]=new PropertyDescriptor("testType", XTestExecutor.class); // NOI18N
            properties[4].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "TestTypeName")); // NOI18N
            properties[4].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "TestTypeDescription")); // NOI18N
            properties[5]=new PropertyDescriptor("xtestHome", XTestExecutor.class); // NOI18N
            properties[5].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "XTestHomeName")); // NOI18N
            properties[5].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "XTestHomeDescription")); // NOI18N
            properties[5].setValue("files",new Boolean(false)); // NOI18N
            properties[6]=new PropertyDescriptor("showResults", XTestExecutor.class); // NOI18N
            properties[6].setDisplayName(NbBundle.getMessage(XTestExecutorBeanInfo.class, "ShowResultsName")); // NOI18N
            properties[6].setShortDescription(NbBundle.getMessage(XTestExecutorBeanInfo.class, "ShowResultsDescription")); // NOI18N
            return properties;
        } catch( IntrospectionException e) {
            org.openide.ErrorManager.getDefault().notify(e);
            return null;
        }
    }

    /** return icon of XTestExecutor
     * @param type int icon type
     * @return Image XTestIcon */    
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("org/netbeans/modules/testtools/XTestExecIcon.gif"); // NOI18N
        } else {
            return null;
        }
    }
 
}
