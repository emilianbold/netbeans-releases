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

    public void testEmpty() {
        CTManagerOperationBridge.test("", new CTProcessor() {
            public void updateDefaultValues() {
                String insertText = getRequest().getInsertText();
                assertEquals(insertText, "");
            }
        });
    }
    
}
