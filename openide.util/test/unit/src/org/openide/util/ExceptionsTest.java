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

package org.openide.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import junit.framework.TestCase;
import org.netbeans.junit.Log;

/**
 * @author Jaroslav Tulach
 */
public class ExceptionsTest extends TestCase {

    public ExceptionsTest(String testName) {
        super(testName);
    }

    private void assertCleanStackTrace(Throwable t) {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        t.printStackTrace(pw);
        pw.flush();
        String m = w.toString();
        assertFalse(m.replace("\n", "\\n").replace("\t", "\\t"), m.contains("AnnException"));
        assertFalse(m.replace("\n", "\\n").replace("\t", "\\t"), m.contains("msg"));
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

        assertCleanStackTrace(e);
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

        assertCleanStackTrace(e);
    }

    public void testAttachLocalizedMessage() {
        Exception e = new Exception("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachLocalizedMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("The same msg", msg, fnd);

        assertCleanStackTrace(e);
    }

    public void testAttachLocalizedMessageForClassNFE() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachLocalizedMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("The same msg", msg, fnd);

        assertCleanStackTrace(e);
    }

    public void testAttachLocalizedMessageForClassNFEIfNoMsg() {
        Exception e = new ClassNotFoundException("Help");
        String msg = "me please";
        
        Exception expResult = e;
        Exception result = Exceptions.attachMessage(e, msg);
        assertEquals(expResult, result);

        String fnd = Exceptions.findLocalizedMessage(e);

        assertEquals("No localized msg found", null, fnd);

        assertCleanStackTrace(e);
    }
    
}
