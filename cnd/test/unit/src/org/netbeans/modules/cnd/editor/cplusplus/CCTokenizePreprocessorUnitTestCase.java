/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    public void testIncludeDirectiveNoSpace() {
        doParse("#include<file.h>\n" +
                "#include\"file.h\"\n",
                new TokenID[] {
                    CCTokenContext.CPPINCLUDE, 
                    CCTokenContext.SYS_INCLUDE, CCTokenContext.WHITESPACE,
                    CCTokenContext.CPPINCLUDE, 
                    CCTokenContext.USR_INCLUDE, CCTokenContext.WHITESPACE
                },
                true);        
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
