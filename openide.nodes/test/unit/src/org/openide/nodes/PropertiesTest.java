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
