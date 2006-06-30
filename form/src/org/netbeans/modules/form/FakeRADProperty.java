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
