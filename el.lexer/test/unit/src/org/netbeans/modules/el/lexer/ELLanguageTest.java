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

package org.netbeans.lib.html.lexer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.lib.lexer.test.LexerTestUtilities;
import org.netbeans.modules.el.lexer.api.ELTokenId;

/**
 * Expression Language test
 *
 * @author Marek Fukala
 */
public class ELLanguageTest extends TestCase {
    
    private static final int IDS_SIZE = 10;
    
    public ELLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
//        Language language = ELTokenId.language();
//        
//        // Check token categories
//        Set testTids = language.tokenCategories();
//        Collection tids = Arrays.asList(new String[] {
//                    "numeric-literals", "whitespace", "tag-lib-prefix",
//                    "double-literal", "operator", "invalid-octal-literal",
//                    "keyword", "int-literal", "octal-literal", "string",
//                    "char-literal", "hex-literal", "float-literal",
//                    "el-delimiter", "long-literal", "invalid-char", "identifier"
//        });
//        LexerTestUtilities.assertCollectionsEqual("Invalid token ids", tids, testTids);
        
    }
    
}
