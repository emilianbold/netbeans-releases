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
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * HTMLLanguage test
 *
 * @author Marek Fukala
 */
public class HTMLLanguageTest extends TestCase {

    private static final int IDS_SIZE = 10;
    
    public HTMLLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
        Language language = HTMLTokenId.language();

        // Check token categories
        Set testTids = language.tokenCategories();
        Collection tids = Arrays.asList(new String[] {
            "text", "script", "ws", "error", "tag", "tag", "argument",
            "operator", "value", "block-comment", "sgml-comment", "sgml-declaration", 
            "character", "text", "tag", "tag"
        });
        LexerTestUtilities.assertCollectionsEqual("Invalid token ids", tids, testTids);
                
    }

}
