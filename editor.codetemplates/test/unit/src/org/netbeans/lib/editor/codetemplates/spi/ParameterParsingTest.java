/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates.spi;

import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.editor.codetemplates.CTManagerOperationBridge;
import org.netbeans.lib.editor.codetemplates.CTProcessor;


/**
 * Testing correctness of the code template parameter parsing.
 *
 * @author mmetelka
 */
public class ParameterParsingTest extends NbTestCase {
    
    public ParameterParsingTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
    }

    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
    }

    public void testEmpty() {
        CTManagerOperationBridge.test("", new CTProcessor() {
            public void updateDefaultValues() {
                String insertText = getRequest().getInsertText();
                assertEquals(insertText, "");
            }
        });
    }
    
}
