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
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** returns Bean Descriptors of XTestExecutor class
     * @return BeanDescriptor */    
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (XTestExecutor.class);
        desc.setDisplayName ("XTest Execution");
        desc.setShortDescription ("XTest Execution");
        desc.setValue ("version", "1.1");
        return desc;
    }

    /** returns Bean Descriptors of XTestExecutor properties
     * @return array of BeanDescriptor */    
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] properties=new PropertyDescriptor[7];
            properties[0]=new PropertyDescriptor("netbeansHome", XTestExecutor.class);
            properties[0].setDisplayName("Netbeans Home");
            properties[0].setShortDescription("Netbeans IDE Home Directory (different than current).");
            properties[0].setValue("files",new Boolean(false));
            properties[0].setPreferred(true);
            properties[1]=new PropertyDescriptor("attributes", XTestExecutor.class);
            properties[1].setDisplayName("Attributes");
            properties[1].setShortDescription("Test Bag Attributes");
            properties[2]=new PropertyDescriptor("jellyHome", XTestExecutor.class);
            properties[2].setDisplayName("Jelly Home");
            properties[2].setShortDescription("Jelly Home Directory");
            properties[2].setValue("files",new Boolean(false));
            properties[3]=new PropertyDescriptor("jemmyHome", XTestExecutor.class);
            properties[3].setDisplayName("Jemmy Home");
            properties[3].setShortDescription("Jemmy Home Directory");
            properties[3].setValue("files",new Boolean(false));
            properties[4]=new PropertyDescriptor("testType", XTestExecutor.class);
            properties[4].setDisplayName("Test Type");
            properties[4].setShortDescription("Test Type");
            properties[5]=new PropertyDescriptor("xtestHome", XTestExecutor.class);
            properties[5].setDisplayName("XTest Home");
            properties[5].setShortDescription("XTest Home Directory");
            properties[5].setValue("files",new Boolean(false));
            properties[6]=new PropertyDescriptor("showResults", XTestExecutor.class);
            properties[6].setDisplayName("Show Results");
            properties[6].setShortDescription("Start Browser with Results");
            return properties;
        } catch( IntrospectionException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                e.printStackTrace ();
            return null;
        }
    }

    /** return icon of XTestExecutor
     * @param type int icon type
     * @return Image XTestIcon */    
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("org/netbeans/modules/testtools/XTestIcon.gif");
        } else {
            return null;
        }
    }
 
}
