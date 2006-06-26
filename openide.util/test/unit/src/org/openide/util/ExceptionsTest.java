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

package org.openide.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.junit.Log;

/**
 *
 * @author Jaroslav Tulach
 */
public class ExceptionsTest extends TestCase {
    
    public ExceptionsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testAttachMessage() {
        Exception e = new Exception("Help");
        String msg = "me please";
        
        Exception result = Exceptions.attachMessage(e, msg);

        assertSame(result, e);

        StringWriter w = new StringWriter();
        result.printStackTrace(new PrintWriter(w));

        String m = w.toString();

        if (m.indexOf(msg) == -1) {
            fail(msg + " shall be part of output:\n" + m);
        }
    }
    
    public void testAttachMessageForClassNotFound() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception result = Exceptions.attachMessage(e, msg);

        assertSame(result, e);

        CharSequence log = Log.enable("", Level.WARNING);
        Exceptions.printStackTrace(e);

        String m = log.toString();

        if (m.indexOf(msg) == -1) {
            fail(msg + " shall be part of output:\n" + m);
        }
    }

    public void testAttachLocalizedMessage() {
        Exception e = new Exception("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachLocalizedMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("The same msg", msg, fnd);
    }

    public void testAttachLocalizedMessageForClassNFE() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachLocalizedMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("The same msg", msg, fnd);
    }

    public void testAttachLocalizedMessageForClassNFEIfNoMsg() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("No localized msg found", null, fnd);
    }
    
}
