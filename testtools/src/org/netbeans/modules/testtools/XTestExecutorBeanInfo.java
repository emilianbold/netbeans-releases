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

import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanDescriptor;
import java.awt.Image;
import org.openide.util.Utilities;
import org.openide.execution.Executor;
import java.beans.PropertyEditorSupport;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class XTestExecutorBeanInfo extends SimpleBeanInfo {

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

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (XTestExecutor.class);
        desc.setDisplayName ("XTest Execution");
        desc.setShortDescription ("XTest Execution");
        desc.setValue ("version", "1.1");
        return desc;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor[] properties=new PropertyDescriptor[7];
            properties[0]=new PropertyDescriptor("netbeansHome", XTestExecutor.class);
            properties[0].setDisplayName("Netbeans Home");
            properties[0].setShortDescription("Netbeans IDE Home Directory (different than current).");
            properties[0].setValue("files",new Boolean(false));
            properties[0].setPreferred(true);
            properties[1]=new PropertyDescriptor("windowSystem", XTestExecutor.class);
            properties[1].setDisplayName("Window System");
            properties[1].setShortDescription("Window System mode (SDI or MDI).");
            properties[1].setPropertyEditorClass(ChoiceEditor.class);
            properties[2]=new PropertyDescriptor("attributes", XTestExecutor.class);
            properties[2].setDisplayName("Attributes");
            properties[2].setShortDescription("Test Bag Attributes");
            properties[3]=new PropertyDescriptor("jellyHome", XTestExecutor.class);
            properties[3].setDisplayName("Jelly Home");
            properties[3].setShortDescription("Jelly Home Directory");
            properties[3].setValue("files",new Boolean(false));
            properties[4]=new PropertyDescriptor("jemmyHome", XTestExecutor.class);
            properties[4].setDisplayName("Jemmy Home");
            properties[4].setShortDescription("Jemmy Home Directory");
            properties[4].setValue("files",new Boolean(false));
            properties[5]=new PropertyDescriptor("testType", XTestExecutor.class);
            properties[5].setDisplayName("Test Type");
            properties[5].setShortDescription("Test Type");
            properties[6]=new PropertyDescriptor("xtestHome", XTestExecutor.class);
            properties[6].setDisplayName("XTest Home");
            properties[6].setShortDescription("XTest Home Directory");
            properties[6].setValue("files",new Boolean(false));
            return properties;
        } catch( IntrospectionException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                e.printStackTrace ();
            return null;
        }
    }

    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage ("org/apache/tools/ant/module/resources/AntIcon.gif");
        } else {
            return null;
        }
    }
 
        
    final public static class ChoiceEditor extends PropertyEditorSupport {
        /** Display Names for alignment. */
        private static String[] names = new String[] {"","sdi","mdi"}; 

        /** @return names of the possible directions */
        public String[] getTags() {
            return names;
        }

        /** @return text for the current value */
        public String getAsText() {
            int value =((Integer)getValue()).intValue();
            if ((value < 0) ||(value > 2)) return null;
            return names [value];
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            for (int i = 0; i <= 2; i++) {
                if (names[i].equals(str)) {
                    setValue(new Integer(i));
                    return;
                }
            }
        }

    }


}
