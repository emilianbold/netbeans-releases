/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.editor.cplusplus;

import org.netbeans.editor.TokenID;

/**
 * Test case for tokenizing preprocessor directives in document
 *
 * @author Vladimir Voskresensky
 */
public class CCTokenizePreprocessorUnitTestCase extends CCTokenizeUnitTestCase {
    
    public CCTokenizePreprocessorUnitTestCase(String testMethodName) {
        super(testMethodName);
    }
    
    public void testIncludeDirective() {
        doParse("# include <folder/file.h>\n" +
                "# include \"folder/file.h\"\n",
                new TokenID[] {
                    CCTokenContext.CPPINCLUDE, CCTokenContext.WHITESPACE, 
                    CCTokenContext.SYS_INCLUDE, CCTokenContext.WHITESPACE,
                    CCTokenContext.CPPINCLUDE, CCTokenContext.WHITESPACE, 
                    CCTokenContext.USR_INCLUDE, CCTokenContext.WHITESPACE
                },
                false);
        
    }

    public void testAloneSharpDirective() {
        doParse("#\n",
                new TokenID[] {
                    CCTokenContext.HASH, CCTokenContext.WHITESPACE
                },
                false);
    }

    public void testAloneSharpDirectiveWithSpace() {
        doParse("# \n",
                new TokenID[] {
                    CCTokenContext.HASH, CCTokenContext.WHITESPACE
                },
                false);
    }
    
    public void testStructAfterSharpIdDirective() {
        doParse("# id\n" +
                "struct A {\n" +
                "};",
                new TokenID[] {
                    CCTokenContext.CPPIDENTIFIER, CCTokenContext.WHITESPACE, 
                    CCTokenContext.STRUCT, CCTokenContext.WHITESPACE, CCTokenContext.IDENTIFIER, 
                    CCTokenContext.WHITESPACE, CCTokenContext.LBRACE, CCTokenContext.WHITESPACE,
                    CCTokenContext.RBRACE, CCTokenContext.SEMICOLON
                },
                false);
    }
    
    public void testStructAfterSharpDirective() {
        doParse("# \n" +
                "struct A {\n" +
                "};",
                new TokenID[] {
                    CCTokenContext.HASH, CCTokenContext.WHITESPACE, 
                    CCTokenContext.STRUCT, CCTokenContext.WHITESPACE, CCTokenContext.IDENTIFIER, 
                    CCTokenContext.WHITESPACE, CCTokenContext.LBRACE, CCTokenContext.WHITESPACE,
                    CCTokenContext.RBRACE, CCTokenContext.SEMICOLON
                },
                false);
    }
    
    public void testStructAfterPPDirective() {
        doParse("# 1 id\n" +
                "struct A {\n" +
                "};",
                new TokenID[] {
                    CCTokenContext.HASH, CCTokenContext.INT_LITERAL, CCTokenContext.WHITESPACE, 
                    CCTokenContext.IDENTIFIER, CCTokenContext.WHITESPACE,
                    CCTokenContext.STRUCT, CCTokenContext.WHITESPACE, CCTokenContext.IDENTIFIER, 
                    CCTokenContext.WHITESPACE, CCTokenContext.LBRACE, CCTokenContext.WHITESPACE,
                    CCTokenContext.RBRACE, CCTokenContext.SEMICOLON
                },
                false);
    }
    
    public void testDefineDirectiveWithSpace() {
        doParse("# define A B\n",
                new TokenID[] {
                    CCTokenContext.CPPDEFINE, CCTokenContext.WHITESPACE,
                    CCTokenContext.IDENTIFIER, CCTokenContext.WHITESPACE,
                    CCTokenContext.IDENTIFIER, CCTokenContext.WHITESPACE
                },
                false);
    }   
}
