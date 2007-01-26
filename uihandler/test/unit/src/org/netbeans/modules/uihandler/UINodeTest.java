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

package org.netbeans.modules.uihandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ResourceBundle;
import java.util.logging.Handler;
import java.util.logging.Level;
import junit.framework.TestCase;
import java.util.logging.LogRecord;
import org.netbeans.lib.uihandler.LogRecords;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Tulach
 */
public class UINodeTest extends TestCase {
    
    public UINodeTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testDisplayNameOfTheNode() throws Exception {
        LogRecord r = new LogRecord(Level.INFO, "test_msg");
        r.setResourceBundleName("org.netbeans.modules.uihandler.TestBundle");
        r.setResourceBundle(ResourceBundle.getBundle("org.netbeans.modules.uihandler.TestBundle"));
        r.setParameters(new Object[] { new Integer(1), "Ahoj" });
        
        Node n = UINode.create(r);
        assertEquals("Name is taken from the message", "test_msg", n.getName());
        
        if (!n.getDisplayName().matches(".*Ahoj.*1.*")) {
            fail("wrong display name, shall contain Ahoj and 1: " + n.getDisplayName());
        }
        assertSerializedWell(n);
    }
    
    public void testSomeNPE() throws Exception {
        LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR");
        Node n = UINode.create(r);
        assertNotNull(n);
        assertEquals("No name", "", n.getDisplayName());
        assertNotNull(n.getName());
        assertSerializedWell(n);
    }
    
    public void testHasNonNullName() throws Exception {
        LogRecord r = new LogRecord(Level.WARNING, null);
        r.setThrown(new Exception());
        Node n = UINode.create(r);
        assertNotNull(n);
        assertNotNull(n.getName());
   //     assertSerializedWell(n);
    }
    
    private static void assertSerializedWell(Node n) throws Exception {
        LogRecord r = n.getLookup().lookup(LogRecord.class);
        assertNotNull("There is a log record", r);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        LogRecords.write(os, r);
        os.close();

        {
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            LogRecord nr = LogRecords.read(is);
            is.close();

            Node newNode = UINode.create(nr);

            assertEquals("name", n.getName(), newNode.getName());
            assertEquals("displayName", n.getDisplayName(), newNode.getDisplayName());
            assertEquals("htmlName", n.getHtmlDisplayName(), newNode.getHtmlDisplayName());
        }
        
        class H extends Handler {
            LogRecord one;
            
            public void publish(LogRecord a) {
                assertNull("This is first one: " + a, one);
                one = a;
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }
        
        H handler = new H();
        
        {
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            LogRecords.scan(is, handler);
            is.close();

            Node newNode = UINode.create(handler.one);

            assertEquals("name", n.getName(), newNode.getName());
            assertEquals("displayName", n.getDisplayName(), newNode.getDisplayName());
            assertEquals("htmlName", n.getHtmlDisplayName(), newNode.getHtmlDisplayName());
        }
    }
}
