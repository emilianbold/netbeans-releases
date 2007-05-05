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

import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import junit.framework.TestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class SubmitPanelTest extends TestCase {
    SubmitPanel instance;
    
    public SubmitPanelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        instance = new SubmitPanel();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddRecord() throws Exception {
        LogRecord r = new LogRecord(Level.FINE, "Skákal pes, přes oves, přes zelenou louku");
        instance.addRecord(r);
        
        String res = instance.text.getDocument().getText(0, instance.text.getDocument().getLength());
        
        
        if (res.indexOf(r.getMessage()) == -1) {
            fail("Localized message is not distorted:\n"+res);
        }
    }
    
}
