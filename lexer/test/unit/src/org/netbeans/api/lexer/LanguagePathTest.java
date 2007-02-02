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

package org.netbeans.api.lexer;

import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author vita
 */
public class LanguagePathTest extends NbTestCase {

    /** Creates a new instance of LanguagePathTest */
    public LanguagePathTest(String name) {
        super(name);
    }

    public void testMimePath() {
        Language<TestTokenId> jspLang = new Lang("text/x-jsp").language();
        Language<TestTokenId> javaLang = new Lang("text/x-java").language();
        Language<TestTokenId> javadocLang = new Lang("text/x-javadoc").language();
        
        LanguagePath jspPath = LanguagePath.get(jspLang);
        assertEquals("Invalid mime path for jspLang", "text/x-jsp", jspPath.mimePath());
        
        LanguagePath javaPath = LanguagePath.get(javaLang);
        assertEquals("Invalid mime path for javaLang", "text/x-java", javaPath.mimePath());
        
        LanguagePath jspJavaPath = LanguagePath.get(LanguagePath.get(jspLang), javaLang);
        assertEquals("Invalid mime path for jspLang",
                "text/x-jsp/text/x-java", jspJavaPath.mimePath());
        
        LanguagePath javaJavadocPath = LanguagePath.get(LanguagePath.get(javaLang), javadocLang);
        assertEquals("Invalid mime path for javaLang/javadocLang", 
                "text/x-java/text/x-javadoc", javaJavadocPath.mimePath());

        LanguagePath jspJavaJavadocPath = LanguagePath.get(LanguagePath.get(jspPath, javaLang), javadocLang);
        assertEquals("Invalid mime path for jspLang/javaLang/javadocLang", 
                "text/x-jsp/text/x-java/text/x-javadoc", jspJavaJavadocPath.mimePath());

//        LanguagePath jspJavaNullJavadocPath = LanguagePath.get(LanguagePath.get(LanguagePath.get(jspPath, javaLang), nullLang), javadocLang);
//        assertEquals("Invalid mime path for jspLang/javaLang/nullLang/javadocLang", 
//                "text/x-jsp/text/x-java/text/x-javadoc", jspJavaNullJavadocPath.mimePath());

//        LanguagePath nullPath = LanguagePath.get(nullLang);
//        assertEquals("Invalid mime path for nullLang", "", nullPath.mimePath());
        
        // Test endsWith()
        assertTrue(jspPath.endsWith(jspPath));
        assertTrue(jspJavaPath.endsWith(javaPath));
        assertFalse(jspJavaPath.endsWith(jspPath));
        assertFalse(javaPath.endsWith(jspJavaPath));
        assertTrue(jspJavaJavadocPath.endsWith(javaJavadocPath));
        
        assertTrue(jspPath.subPath(0) == jspPath);
        assertTrue(jspJavaPath.subPath(0) == jspJavaPath);
        assertTrue(jspJavaPath.subPath(1) == javaPath);
        assertTrue(jspJavaJavadocPath.subPath(1, 2) == javaPath);
    }
    
    private static enum TestTokenId implements TokenId {
        
        TOKEN_ID1,
        TOKEN_ID2;
        
        private TestTokenId() {
            
        }

        public String primaryCategory() {
            return null;
        }
    } // End of TestTokenId
    
    private static final class Lang extends LanguageHierarchy<TestTokenId> {
        private String mimeType;
        
        public Lang(String mimeType) {
            this.mimeType = mimeType;
        }
        
        protected Lexer<TestTokenId> createLexer(LexerRestartInfo<TestTokenId> info) {
            return null;
        }

        protected Collection<TestTokenId> createTokenIds() {
            return EnumSet.allOf(TestTokenId.class);
        }
        
        public String mimeType() {
            return mimeType;
        }
    } // End of Lang class
}
