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
 * Base64Test.java
 * JUnit based test
 *
 * Created on April 5, 2005, 1:21 PM
 */
package org.netbeans.mobility.antext;

import java.util.Arrays;
import java.util.Random;
import junit.framework.*;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author misk
 */
public class Base64Test extends NbTestCase {
    
    final byte[] test0 = new byte[] { };
    final byte[] test1 = new byte[] { 0x55 };
    final byte[] test2 = new byte[] { (byte)0xff, 0x0f };
    final byte[] test3 = new byte[] { (byte)0xaa, 0x00, 0x55 };
    final byte[] test4 = new byte[] { (byte)0xff, (byte)0xf0, 0x0f, 0x5a };
    
    
    public Base64Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    static Test suite() {
        TestSuite suite = new TestSuite(Base64Test.class);
        
        return suite;
    }

    /**
     * Test of encode method, of class org.netbeans.mobility.antext.Base64.
     */
    public void testEncode() {
        
        assertEquals( "Correct pattern", "", Base64.encode( test0 ));       // NOI18N
        assertEquals( "Correct pattern", "VQ==", Base64.encode( test1 ));   // NOI18N
        assertEquals( "Correct pattern", "/w8=", Base64.encode( test2 ));   // NOI18N
        assertEquals( "Correct pattern", "qgBV", Base64.encode( test3 ));   // NOI18N
        assertEquals( "Correct pattern", "//APWg==", Base64.encode( test4 ));   // NOI18N
    }

    /**
     * Test of decode method, of class org.netbeans.mobility.antext.Base64.
     */
    public void testDecode() {
        
        assertTrue( "Correct pattern", Arrays.equals( new byte[] {}, Base64.decode( "" )));
        
        assertTrue( "Correct pattern", Arrays.equals( test0, Base64.decode( "====" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test1, Base64.decode( "VQ==" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test2, Base64.decode( "/w8=" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test3, Base64.decode( "qgBV" )));     // NOI18N
        assertTrue( "Correct pattern", Arrays.equals( test4, Base64.decode( "//APWg==" ))); // NOI18N
    }
    
    /**
     * Test encode and decode method on random generated data
     */
    public void testEncodeDecode() {
        byte[] test = new byte[1024];
        long seed = System.currentTimeMillis();
        Random random = new Random( seed );
        
        for( int i = 0; i < 1024; i++ ) {
            test[i] = (byte)random.nextInt( 256 );
        }
        String testResult = Base64.encode( test );
        assertTrue( "Correct pattern on seed " + seed, 
                Arrays.equals( test, Base64.decode( testResult ))); 
    }
}
