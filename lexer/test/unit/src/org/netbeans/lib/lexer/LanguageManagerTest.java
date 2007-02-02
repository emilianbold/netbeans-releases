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

package org.netbeans.lib.lexer;

import java.lang.ref.WeakReference;
import javax.swing.text.PlainDocument;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.simple.SimpleCharTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleLanguageProvider;
import org.netbeans.lib.lexer.test.simple.SimplePlainTokenId;

/**
 *
 * @author vita
 */
public class LanguageManagerTest extends NbTestCase {
    
    public static final String MIME_TYPE_UNKNOWN = "text/x-unknown";
    public static final String MIME_TYPE_KNOWN = "text/x-known";
    
    
    /** Creates a new instance of LanguageManagerTest */
    public LanguageManagerTest(String name) {
        super(name);
    }

    public void testBasic() {
        Language lang = LanguageManager.getInstance().findLanguage(null);
        assertNull("There should be no language for null mime type", lang);

        lang = LanguageManager.getInstance().findLanguage("");
        assertNull("There should be no language for '' mime type", lang);
    }
    
    public void testUnknownMimeType() {
        Language lang = LanguageManager.getInstance().findLanguage(MIME_TYPE_UNKNOWN);
        assertNull("There should be no language for " + MIME_TYPE_UNKNOWN, lang);
    }

    public void testKnownMimeType() {
        PlainDocument doc = new PlainDocument();
        doc.putProperty("mimeType", MIME_TYPE_KNOWN);
        
        TokenHierarchy th = TokenHierarchy.get(doc);
        Language lang = th.tokenSequence().language();
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, lang);
        
        assertNotNull("Invalid mime type", lang.mimeType());
        assertEquals("Wrong language's mime type", MIME_TYPE_KNOWN, lang.mimeType());
    }
    
    public void testCachingMT() {
        Language langA = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langA);
        
        Language langB = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langB);
        
        assertSame("The Language is not cached", langA, langB);
    }
    
    public void testGCedMT() {
        Language lang = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, lang);
        
        WeakReference<Language> ref = new WeakReference<Language>(lang);
        lang = null;
        
        assertGC("Language has not been GCed", ref);
    }

    public void testCacheRefreshMT() {
        Language langA = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langA);
        
        SimpleLanguageProvider.fireLanguageChange();
        
        Language langB = LanguageManager.getInstance().findLanguage(MIME_TYPE_KNOWN);
        assertNotNull("There should be language for " + MIME_TYPE_KNOWN, langB);
        
        assertNotSame("The cache has not been refreshed", langA, langB);
    }

    /*
     * SimplePlainLanguage does not define any embedding. The SimpleLanguageProvider
     * however defines the SimpleCharLanguage as an embedded language for the SimplePlainTokenId.WORD.
     * Therefore SimplePlainTokenId.WHITESPACE should not have any embedded language and
     * SimplePlainTokenId.WORD should have the SimpleCharLanguage.
     */
    public void testEmbedding() {
        TokenHierarchy th = TokenHierarchy.create("abc xyz 012 0xFF00 0-1-2-3-4-5-6-7-8-9", SimplePlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        
        for( ; tokens.moveNext(); ) {
            TokenId id = tokens.token().id();
            TokenSequence embedded = tokens.embedded();
            
            if (id == SimplePlainTokenId.WHITESPACE) {
                assertNull("Whitespace should not have any embedded language", embedded);
            } else if (id == SimplePlainTokenId.WORD) {
                assertNotNull("Word should have an embedded token sequence", embedded);
                assertNotNull("Word should have an embedded language", embedded.language());
                assertEquals("Wrong embedded language", SimpleCharTokenId.MIME_TYPE, embedded.language().mimeType());
            }
        }
    }
    
    public void testCachingE() {
        TokenHierarchy th = TokenHierarchy.create("abc", SimplePlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        tokens.moveStart();
        assertEquals(true, tokens.moveNext());
        
        TokenSequence embeddedA = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedA);
        
        TokenSequence embeddedB = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedB);
        
        assertSame("The embedded language is not cached", embeddedA.language(), embeddedB.language());
    }

    public void testGCedE() {
        TokenHierarchy th = TokenHierarchy.create("abc", SimplePlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        tokens.moveStart();
        assertEquals(true, tokens.moveNext());
        
        TokenSequence embedded = tokens.embedded();
        assertNotNull("There should be an embedded language", embedded);
        
        WeakReference<Language> refLang = new WeakReference<Language>(embedded.language());
        embedded = null;

        WeakReference<Token> refToken = new WeakReference<Token>(tokens.token());
        tokens = null;
        th = null;
        
        // This no longer works after the language is statically held in the xxTokenId by the new convention
        //assertGC("The embedded language has not been GCed", refLang);
        assertGC("The token with embedded language has not been GCed", refToken);
    }
    
    public void testCacheRefreshedE() {
        TokenHierarchy th = TokenHierarchy.create("abc", SimplePlainTokenId.language());
        TokenSequence tokens = th.tokenSequence();
        tokens.moveStart();
        assertEquals(true, tokens.moveNext());
        
        TokenSequence embeddedA = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedA);
        
        SimpleLanguageProvider.fireTokenLanguageChange();
        
        TokenSequence embeddedB = tokens.embedded();
        assertNotNull("There should be an embedded language", embeddedB);
        
        assertNotSame("The token language cache has not been refreshed", embeddedA, embeddedB);
    }
    
}
