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

package org.openide.explorer.propertysheet;

import java.awt.IllegalComponentStateException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import org.netbeans.junit.NbTestCase;

/** A test of a property model.
 */
public class DefaultPropertyModelTest extends NbTestCase {

    public DefaultPropertyModelTest(String name) {
        super(name);
    }

    public void testLookupOfAPropertyReadOnlyProperty() throws Exception {
        Object obj = new Object();
        DefaultPropertyModel model = new DefaultPropertyModel(obj, "class");
        
        
        assertEquals("Calls the get method", model.getValue(), obj.getClass());
    }
    
    public void testLookupOfAPropertyReadWriteProperty() throws Exception {
        ServerSocket obj = new ServerSocket(0);
        
        DefaultPropertyModel model = new DefaultPropertyModel(obj, "soTimeout");
        
        
        assertEquals("Calls the get method", model.getValue(), new Integer(obj.getSoTimeout()));
        
        model.setValue(new Integer(100));
        
        assertEquals("Value change", 100, obj.getSoTimeout());
        assertEquals("Model updated", model.getValue(), new Integer(obj.getSoTimeout()));
    }
    
    //
    // Test of explicit beaninfo
    //
    
    public void testUsageOfExplicitPropertyDescriptor() throws Exception {
        PropertyDescriptor pd = new PropertyDescriptor(
                "myProp", this.getClass(),
                "getterUsageOfExplicitPropertyDescriptor",
                "setterUsageOfExplicitPropertyDescriptor"
                );
        
        DefaultPropertyModel model = new DefaultPropertyModel(this, pd);
        
        assertEquals("Getter returns this", model.getValue(), this);
        
        String msgToThrow = "msgToThrow";
        try {
            model.setValue(msgToThrow);
            fail("Setter should throw an exception");
        } catch (InvocationTargetException ex) {
            // when an exception occurs it should throw InvocationTargetException
            assertEquals("The right message", msgToThrow, ex.getTargetException().getMessage());
        }
    }
    
    public Object getterUsageOfExplicitPropertyDescriptor() {
        return this;
    }
    
    public void setterUsageOfExplicitPropertyDescriptor(Object any) {
        throw new IllegalComponentStateException(any.toString());
    }
    
    //
    // End of explicit beaninfo
    //
}

