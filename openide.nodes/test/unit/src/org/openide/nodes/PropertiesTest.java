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

package org.openide.nodes;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;



/** 
 * @author Some Czech
 */
public class PropertiesTest extends NbTestCase {
    
    public PropertiesTest(String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PropertiesTest.class));
    }

    public void testReflection() throws Exception {
        
        Node.Property np = null;
        
        // Test normal property
        TestBean tb = new TestBean();
        np = new PropertySupport.Reflection( tb, int.class, "number" );        
        assertEquals( "Value", np.getValue(), new Integer( 1 ) ); 
        
        // Test setter only of type String
        NoSuchMethodException thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, String.class, "setterOnlyString" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        // Test setter only of type boolean
        thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, boolean.class, "setterOnlyBoolean" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        // Test no boolean with is
        thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, long.class, "isSetLong" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        
        // Test get/set boolean
        np = new PropertySupport.Reflection( tb, boolean.class, "getSetBoolean" );        
        assertEquals( "Value", np.getValue(), Boolean.TRUE ); 
                
        // Test is/set boolean
        np = new PropertySupport.Reflection( tb, boolean.class, "isSetBoolean" );        
        assertEquals( "Value", np.getValue(), Boolean.TRUE ); 
        
    }
    
    public static class TestBean {
        
        public int getNumber() {
            return 1;
        }
        
        public void setNumber( int number ) {
        }
        
        public void setSetterOnlyString( String text ) {
        }
        
        public void setSetterOnlyBoolean( boolean value ) {
        }
        
        public long isIsSetLong() {
            return 10L;
        }
        
        public void setIsSetLong( long value ) {
        }
        
        public boolean getGetSetBoolean() {
            return true;
        }
        
        public void setGetSetBoolean( boolean value ) {
        }
        
        
        public boolean isIsSetBoolean() {
            return true;
        }
        
        public void setIsSetBoolean( boolean value ) {
        }
    }
    

}
