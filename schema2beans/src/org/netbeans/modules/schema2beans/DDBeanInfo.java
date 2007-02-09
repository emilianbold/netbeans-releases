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

package org.netbeans.modules.schema2beans;

import java.beans.*;
import java.util.*;
import java.lang.reflect.*;

public class DDBeanInfo extends SimpleBeanInfo {

    private static final String BEANINFO = "BeanInfo";	// NOI18N

    private PropertyDescriptor[] properties = null;

    private boolean propertiesInited = false;
    
    private void initProperties() {
	ArrayList 	al = new ArrayList();
	String    	classname = null;
	BeanInfo	bi;

	try {
	    classname = this.getClass().getName();
	    if (!classname.endsWith(BEANINFO)) {
		return;
	    }
	    classname = classname.substring(0,(classname.length() - 
					       BEANINFO.length()));
	    bi = Introspector.getBeanInfo(Class.forName(classname));
	} catch (ClassNotFoundException e) {
	    System.err.println("Class name = " + classname);	// NOI18N
	    return;
	} catch (IntrospectionException e) {
	    Thread.dumpStack();
	    return;
	}

	PropertyDescriptor[] pd = bi.getPropertyDescriptors();
	Method m = null;
	for (int i=0;i<pd.length; i++) {
	    Class c = null;
	    if (pd[i] instanceof IndexedPropertyDescriptor) {
		IndexedPropertyDescriptor ipd = 
		    (IndexedPropertyDescriptor)pd[i];
		c = ipd.getIndexedPropertyType();
	    } else {
		c = pd[i].getPropertyType();
	    }
	    // Check for the following:
	    // 1: Does the metohd have a return type that implements 
	    //	the DDNode interface?
	    // 2: Is it a class in java.lang? but not getClass 
	    //	which is inherited from Object
	    // 3: Is it a primitive java type? This would have no "." 
	    //	chars in it.
	    if (c != null) {
		if (BaseBean.class.isAssignableFrom(c) ||
		    (c.getName().startsWith("java.lang.") 
		     && !c.getName().equals("java.lang.Class")) // NOI18N
		    || (c.getName().indexOf(".") < 0)) {	// NOI18N

		    al.add(pd[i]);
		}
	    }
	}
	properties = (PropertyDescriptor[])al.toArray(new PropertyDescriptor[al.size()]);
	
    }
    
    
    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
	if (!propertiesInited) {
	    // Avoid recursion here
	    propertiesInited = true;
	    initProperties();
	}
	return properties;
    }
    
}
