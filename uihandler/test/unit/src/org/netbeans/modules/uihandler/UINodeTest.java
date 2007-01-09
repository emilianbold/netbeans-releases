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

import java.util.ResourceBundle;
import java.util.logging.Level;
import junit.framework.TestCase;
import java.util.logging.LogRecord;
import org.openide.nodes.Node;

/**
 *
 * @author jarda
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
        r.setResourceBundle(ResourceBundle.getBundle("org.netbeans.modules.uihandler.TestBundle"));
        r.setParameters(new Object[] { new Integer(1), "Ahoj" });
        
        Node n = UINode.create(r);
        assertEquals("Name is taken from the message", "test_msg", n.getName());
        
        if (!n.getDisplayName().matches(".*Ahoj.*1.*")) {
            fail("wrong display name, shall contain Ahoj and 1: " + n.getDisplayName());
        }
    }
    
    public void testSomeNPE() {
        LogRecord r = new LogRecord(Level.FINE, "UI_ACTION_EDITOR");
        Node n = UINode.create(r);
        assertNotNull(n);
        assertEquals("No name", "", n.getDisplayName());
    }
}
