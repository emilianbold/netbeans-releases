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
package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.Arrays;
import java.util.Iterator;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.editor.NbEditorKit;

/**
 * Test case for tokenizing document
 * 
 * @author Vladimir Voskresensky
 */
public abstract class CCTokenizeUnitTestCase extends CCBaseDocumentUnitTestCase {
    
    public CCTokenizeUnitTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    protected void doParse(String m, TokenID[] expected, boolean traceOnly) {
        if (traceOnly) { System.err.println("TEXT:\n" + m); }
        NbEditorKit kit = (NbEditorKit) getEditorKit();
        Syntax syntax = kit.createSyntax(null);
        syntax.load(null, m.toCharArray(), 0, m.length(), true, m.length());
        TokenID token = null;
        Iterator i = Arrays.asList(expected).iterator();
        if (traceOnly) { System.err.println("RESULT:"); }
        do {
            token = syntax.nextToken();
            if (traceOnly) {
                System.err.println(token);
            } else {
                if (token != null) {
                    if (!i.hasNext()) {
                        fail("More tokens returned than expected.");
                    } else {
                        assertSame("Tokens differ", i.next(), token);
                    }
                } else {
                    assertFalse("More tokens expected than returned.", i.hasNext());
                }
            }
        } while (token != null);
    }  
}
