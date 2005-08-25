/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DefaultBeanInfo.java
 *
 * Created on August 20, 2001, 3:42 PM
 */

package org.netbeans.modules.j2ee.sun.share.config.ui;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;


/**
 *
 * @author  gfink
 * @version 
 */
public class DefaultBeanInfo extends SimpleBeanInfo {

    final Class cls;
    
    /** Creates new DefaultBeanInfo */
    public DefaultBeanInfo(Class cls) {
        this.cls = cls;
    }
    
    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(cls);
    }
    
    public PropertyDescriptor[] getPropertyDescriptors() {
        Method[] methods = cls.getDeclaredMethods();
        Collection c = new Vector();
        for(int i = 0; i < methods.length; i++) {
            try {
            String method = methods[i].getName();
            if(method.startsWith("get") && methods[i].getParameterTypes().length == 0) { // NOI18N
                StringBuffer name = (new StringBuffer(method.substring(3)));
                name.setCharAt(0,Character.toLowerCase(method.charAt(3)));
                String propertyName = name.toString();
                PropertyDescriptor pd;
                if(methods[i].getReturnType().isArray())
                   pd = new IndexedPropertyDescriptor(propertyName,cls);
                else pd = new PropertyDescriptor(propertyName,cls);
//                System.err.println(pd.getName());
//                System.err.println(pd.getDisplayName());
//                System.err.println(pd.getPropertyType());
                c.add(pd);
            }
            } catch (Exception e) {
            }
        }
        PropertyDescriptor[] ret = new PropertyDescriptor[c.size()];
        c.toArray(ret);
        return ret;
    }
}
