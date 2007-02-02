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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.lexer.test.simple;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.test.LexerTestUtilities;


/**
 * Test several simple lexer impls.
 *
 * @author mmetelka
 */
public class SimpleLanguageTest extends TestCase {

    private static final int IDS_SIZE = 18;

    public SimpleLanguageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public void testTokenIds() {
        // Check that token ids are all present and correctly ordered
        Language<SimpleTokenId> language  = SimpleTokenId.language();
        Set ids = language.tokenIds();
        assertEquals("Invalid ids.size()", IDS_SIZE, ids.size());
        
        TokenId[] idArray = {
            SimpleTokenId.IDENTIFIER,
            SimpleTokenId.PLUS,
            SimpleTokenId.MINUS,
            SimpleTokenId.PLUS_MINUS_PLUS,
            SimpleTokenId.DIV,
            SimpleTokenId.STAR,
            SimpleTokenId.BLOCK_COMMENT,
            SimpleTokenId.BLOCK_COMMENT_INCOMPLETE,
            SimpleTokenId.WHITESPACE,
            SimpleTokenId.LINE_COMMENT,
            SimpleTokenId.ERROR,
            SimpleTokenId.PUBLIC,
            SimpleTokenId.PRIVATE,
            SimpleTokenId.STATIC,
            SimpleTokenId.JAVADOC_COMMENT,
            SimpleTokenId.JAVADOC_COMMENT_INCOMPLETE,
            SimpleTokenId.STRING_LITERAL,
            SimpleTokenId.STRING_LITERAL_INCOMPLETE
            
        };

        // Check operations with ids
        Collection testIds = Arrays.asList(idArray);
        LexerTestUtilities.assertCollectionsEqual("Ids do not match with test ones",
                ids, testIds);
        
        // Check that ids.iterator() is ordered by ordinal
        int ind = 0;
        int lastOrdinal = -1;
        for (Iterator it = ids.iterator(); it.hasNext();) {
            TokenId id = (TokenId) it.next();
            if (id.ordinal() == lastOrdinal) {
                fail("Duplicate ordinal for " + id);
            }
            if (id.ordinal() <= lastOrdinal) {
                fail("Token ids not sorted by ordinal: " + id);
            }
            lastOrdinal = id.ordinal();
        }
        
        try {
            language.validTokenId("invalid-name");
            fail("Error: exception not thrown");
        } catch (IllegalArgumentException e) {
            // OK
        }
        
        try {
            language.validTokenId(-1);
            fail("Error: exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // OK
        }
        
        try {
            language.validTokenId(20);
            fail("Error: exception not thrown");
        } catch (IndexOutOfBoundsException e) {
            // OK
        }
        
        assertEquals(17, language.maxOrdinal());
        
        // Check token categories
        Set cats = language.tokenCategories();
        Collection testCats = Arrays.asList(new String[] {
            "operator", "test-category", "whitespace",
            "incomplete", "error", "comment", "keyword", "string"
        });
        LexerTestUtilities.assertCollectionsEqual("Invalid token categories",
                cats, testCats);
        
        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("operator"),
                Arrays.asList(new TokenId[] {
                    SimpleTokenId.PLUS,
                    SimpleTokenId.MINUS,
                    SimpleTokenId.PLUS_MINUS_PLUS,
                    SimpleTokenId.STAR,
                    SimpleTokenId.DIV,
                })
        );
        
        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("test-category"),
                Arrays.asList(new TokenId[] {
                    SimpleTokenId.PLUS,
                    SimpleTokenId.MINUS,
                    SimpleTokenId.IDENTIFIER,
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("whitespace"),
                Arrays.asList(new TokenId[] {
                    SimpleTokenId.WHITESPACE,
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("error"),
                Arrays.asList(new TokenId[] {
                    SimpleTokenId.ERROR,
                    SimpleTokenId.BLOCK_COMMENT_INCOMPLETE,
                    SimpleTokenId.JAVADOC_COMMENT_INCOMPLETE,
                    SimpleTokenId.STRING_LITERAL_INCOMPLETE,
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategoryMembers("comment"),
                Arrays.asList(new TokenId[] {
                    SimpleTokenId.LINE_COMMENT,
                    SimpleTokenId.BLOCK_COMMENT,
                    SimpleTokenId.JAVADOC_COMMENT,
                    SimpleTokenId.BLOCK_COMMENT_INCOMPLETE,
                    SimpleTokenId.JAVADOC_COMMENT_INCOMPLETE
                })
        );
                
        LexerTestUtilities.assertCollectionsEqual(language.tokenCategories(SimpleTokenId.IDENTIFIER),
                Arrays.asList(new String[] {
                    "test-category",
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategories(SimpleTokenId.PLUS),
                Arrays.asList(new String[] {
                    "test-category",
                    "operator",
                })
        );

        LexerTestUtilities.assertCollectionsEqual(language.tokenCategories(SimpleTokenId.BLOCK_COMMENT_INCOMPLETE),
                Arrays.asList(new String[] {
                    "error",
                    "incomplete",
                    "comment",
                })
        );

        // Check indexedIds()
        LexerTestUtilities.assertCollectionsEqual("Invalid language.indexedIds()",
                language.merge(
                    EnumSet.of(SimpleTokenId.IDENTIFIER),
                    language.merge(language.tokenCategoryMembers("comment"),
                        language.tokenCategoryMembers("error"))
                ),
                Arrays.asList(new TokenId[] {
                    SimpleTokenId.LINE_COMMENT,
                    SimpleTokenId.BLOCK_COMMENT,
                    SimpleTokenId.JAVADOC_COMMENT,
                    SimpleTokenId.BLOCK_COMMENT_INCOMPLETE,
                    SimpleTokenId.JAVADOC_COMMENT_INCOMPLETE,
                    SimpleTokenId.STRING_LITERAL_INCOMPLETE,
                    SimpleTokenId.ERROR,
                    SimpleTokenId.IDENTIFIER,
                })
        );

    }

}
