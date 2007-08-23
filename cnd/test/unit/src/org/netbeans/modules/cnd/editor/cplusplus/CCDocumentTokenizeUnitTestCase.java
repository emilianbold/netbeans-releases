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

import org.netbeans.editor.TokenID;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CCDocumentTokenizeUnitTestCase extends CCTokenizeUnitTestCase {
    
    public CCDocumentTokenizeUnitTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    public void testBlockCommnetsAfterPtr() {
        doParse("const/*    */int*/*       */i = 0;    // const int* i = 0;",
                new TokenID[] {
                    CCTokenContext.CONST, CCTokenContext.BLOCK_COMMENT, 
                    CCTokenContext.INT, CCTokenContext.MUL,
                    CCTokenContext.BLOCK_COMMENT, CCTokenContext.IDENTIFIER, 
                    CCTokenContext.WHITESPACE, CCTokenContext.EQ,
                    CCTokenContext.WHITESPACE, CCTokenContext.INT_LITERAL,
                    CCTokenContext.SEMICOLON, CCTokenContext.WHITESPACE,
                    CCTokenContext.LINE_COMMENT
                },
                false);
        
    }
}
