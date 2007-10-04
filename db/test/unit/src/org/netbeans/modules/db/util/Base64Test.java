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

    public void testEncodeObject() {
        SerializableObject orig = new SerializableObject(0, "foo", true);
        
        String encoded = Base64.encodeObject(orig);
                
        SerializableObject decoded = 
                (SerializableObject)Base64.decodeToObject(encoded);
        
        assertTrue(decoded.equals(orig));        
    }
    
    public void testEncodeObjectWithGzip() {
        SerializableObject orig = new SerializableObject(0, "foo", true);
        
        String encoded = Base64.encodeObject(orig, Base64.GZIP);
                
        SerializableObject decoded = 
                (SerializableObject)Base64.decodeToObject(encoded);
        
        assertTrue(decoded.equals(orig));                
    }
    
    public void testEncodeBytes() {
        byte[] orig = "GumbyAndPokey".getBytes();
        
        String encoded = Base64.encodeBytes(orig);
        
        byte[] decoded = Base64.decode(encoded, Base64.NO_OPTIONS);
        
        assertByteArrayEquals(orig, decoded);          
    }
    
    public void testEncodeURLSafeObject() {
        // Can't guarantee that there aren't some cases where it's
        // not URL safe, but this is just a santy test
        String orig = "GumbyAndPokey have a ball";
        
        String encoded = Base64.encodeObject(orig, Base64.URL_SAFE);

        try {
            URL url = new URL("http://www.netbeans.org/" + encoded);
        } catch ( MalformedURLException mfue ) {
            mfue.printStackTrace();
            fail("Encoded string should have been URL safe");
        }
        
        String decoded = (String)Base64.decodeToObject(encoded);
        
        assertEquals(decoded, orig);
    }    
    
    public void testEncodeString() {
        String orig = "Boris and Natasha";
        String encoded = Base64.encodeObject(orig);
        String decoded = (String)Base64.decodeToObject(encoded);
        
        assertEquals(decoded, orig);
    }
    
    public void testEncodeWithAllOptions() {
        String orig = "Rocky and Bullwinkle";
        int options = Base64.GZIP | Base64.URL_SAFE | Base64.DONT_BREAK_LINES;
        String encoded = Base64.encodeObject(orig, options);
        
        String decoded = (String)Base64.decodeToObject(encoded, options);
        
        assertEquals(decoded, orig);        
    }
    
    public void testEncodeToFile() {
        String orig = "That trick never works";
        String filename = "base64TestFile";
        
        File file = new File(filename);
        file.deleteOnExit();
        
        if ( file.exists() ) {
            file.delete();
        }
        
        byte[] origBytes = orig.getBytes();
        
        Base64.encodeToFile(origBytes, filename);
        
        assertTrue(file.exists());
        
        byte[] decoded = Base64.decodeFromFile(filename);
        
        assertByteArrayEquals(origBytes, decoded); 
        
        file.delete();
    }
    
    public void testEncodeFromFile() throws Exception {
        String filename = "base64TestFile";
        String encodedFileName = "base64TestFile.encoded";
        String orig = "Hey, Rocky, watch me pull a rabbit out of the hat!";
        
        File file = new File(filename);
        file.deleteOnExit();
        
        if ( file.exists() ) {
            file.delete();
        }
        
        File encodedFile = new File(encodedFileName);
        encodedFile.deleteOnExit();
        
        if ( encodedFile.exists() ) {
            encodedFile.delete();
        }
        
        PrintWriter writer = new PrintWriter(new FileOutputStream(filename));
        writer.print(orig);
        writer.close();
                
        Base64.encodeFileToFile(filename, encodedFileName);
        
        assertTrue(encodedFile.exists());
        
        byte[] decoded = Base64.decodeFromFile(encodedFileName);
        
        assertByteArrayEquals(orig.getBytes(), decoded);
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
        
    private static class SerializableObject implements Serializable {
        public int intval;
        public String stringval;
        public boolean boolval;
        
        public SerializableObject(int intval, String stringval, boolean boolval) {
            this.intval = intval;
            this.stringval = stringval;
            this.boolval = boolval;
        }
        
        public boolean equals(SerializableObject other) {
            if ( other == null ) return false;
            
            return intval == other.intval &&
                    stringval.equals(other.stringval) &&
                    boolval == other.boolval;
        }
    }

}
