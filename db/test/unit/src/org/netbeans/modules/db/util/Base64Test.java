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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

public class Base64Test extends TestCase {
    
    public Base64Test(String testName) {
        super(testName);
    }
    
    public void testEncodeBytes() throws Exception {
        byte[] orig = "GumbyAndPokey".getBytes("UTF-8");
        
        String encoded = Base64.byteArrayToBase64(orig);
        
        byte[] decoded = Base64.base64ToByteArray(encoded);
        
        assertByteArrayEquals(orig, decoded);          
    }
    
    public void testAlternate() throws Exception {
        byte[] orig = "GumbyAndPokey".getBytes("UTF-8");
        
        String encoded = Base64.byteArrayToAltBase64(orig);
        
        byte[] decoded = Base64.altBase64ToByteArray(encoded);
        
        assertByteArrayEquals(orig, decoded);                  
    }
    
    private static void assertByteArrayEquals(byte[] a, byte[] b) {
        if ( a == null ) {
            assertTrue( b == null);
        }        
        
        assertEquals(a.length, b.length);
        
        for ( int i = 0  ; i < a.length ; i++ ) {
            assertEquals(a[i], b[i] );
        }
    }
        
}
