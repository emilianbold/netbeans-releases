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

package org.netbeans.api.editor.mimelookup;

import org.netbeans.junit.NbTestCase;


/**
 * Testing basic functionality of MimePath
 *
 * @author Martin Roskanin
 */
public class MimePathTest extends NbTestCase {

    public MimePathTest(java.lang.String testName) {
        super(testName);
    }

    public void testParsing(){
        String path = "text/x-java/text/x-ant+xml/text/html/text/xml";
        MimePath mp = MimePath.parse(path);
        String parsedPath = mp.getPath();
        assertTrue(path.equals(parsedPath));

        int size = mp.size();
        assertTrue(size == 4);
        
        String one = mp.getMimeType(0);
        String two = mp.getMimeType(1);
        String three = mp.getMimeType(2);
        String four = mp.getMimeType(3);
        
        assertTrue("text/x-java".equals(one));
        assertTrue("text/x-ant+xml".equals(two));
        assertTrue("text/html".equals(three));
        assertTrue("text/xml".equals(four));
        
        MimePath mpPrefix = mp.getPrefix(2);
        assertTrue("text/x-java/text/x-ant+xml".equals(mpPrefix.getPath()));
    }
    
    public void testMimeTypeCorrectnessCheck() {
        String [] valid = new String [] {
            "text/plain",
            "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!#$&.+-^_/abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!#$&.+-^_"
        };
        String [] invalid = new String [] {
            "/",
            "text",
            "text//aaa",
            "text@aaa",
            "text/aaa/bb",
            "text/ aaa",
        };

        // Check an empty mime type.
        // This is an exception to the mime type specs, we allow empty mime types
        // and they denote MimePath.EMPTY
        {
            MimePath mimePath = MimePath.get("");
            assertNotNull("MimePath should not be null", mimePath);
            assertEquals("Wrong MimePath size", 0, mimePath.size());
            assertSame("Wrong empty MimePath", MimePath.EMPTY, mimePath);
        }
        
        // Check valid mime types
        for(String mimeType : valid) {
            MimePath mimePath = MimePath.get(mimeType);
            assertNotNull("MimePath should not be null", mimePath);
            assertEquals("Wrong MimePath size", 1, mimePath.size());
            assertEquals("Wrong mime type", mimeType, mimePath.getMimeType(0));
        }
        
        // Check invalid mime types
        for(String mimeType : invalid) {
            try {
                MimePath mimePath = MimePath.get(mimeType);
                fail("Should not create MimePath for an invalid mime type: '" + mimeType + "'");
            } catch (IllegalArgumentException iae) {
                // passed
            }
        }
    }
}
