/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

