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
