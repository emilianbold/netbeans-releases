/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

/** 
 * A {@link RADProperty} which has no real target bean.
 *
 * @author Tomas Stupka
 */
public class FakeRADProperty extends RADProperty {
    
    /** Creates a new instance of FakeRADProperty */
    public FakeRADProperty(RADComponent comp, FakePropertyDescriptor desc) throws IntrospectionException {
        super(comp, desc);
        setAccessType(NORMAL_RW);                
    }

    String getWholeSetterCode() {
        return "";
    }                  
       
    public Object getTargetValue() throws IllegalAccessException,
                                          InvocationTargetException {
        return null; // there is no real target
    }
    
    public void setTargetValue(Object value) throws IllegalAccessException,
                                                 IllegalArgumentException,
                                                 InvocationTargetException {
    
        // there is no real target
    }    

}
