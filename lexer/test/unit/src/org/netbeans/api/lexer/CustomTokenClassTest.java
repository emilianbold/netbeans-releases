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

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class CustomTokenClassTest extends NbTestCase {

    public CustomTokenClassTest(String name) {
        super(name);
    }

    public void testCustomTokenClass() throws Exception {
        try {
            Token token = new CustomToken();
            fail("IllegalStateException expected from constructor of Token class.");
        } catch (IllegalStateException e) {
            // Expected ISE from Token's constructor.
        }
    }
    
    private static final class CustomToken extends Token {
        
        public TokenId id() {
            return null;
        }

        public CharSequence text() {
            return null;
        }

        public boolean isCustomText() {
            return false;
        }

        public int length() {
            return 0;
        }

        public int offset(TokenHierarchy tokenHierarchy) {
            return 0;
        }

        public boolean isFlyweight() {
            return false;
        }

        public boolean isPreprocessedText() {
            return false;
        }

        public CharSequence preprocessedText() {
            return null;
        }

        public String preprocessError() {
            return null;
        }

        public int preprocessErrorIndex() {
            return 0;
        }

        public boolean hasProperties() {
            return false;
        }

        public Object getProperty(Object key) {
            return false;
        }
        
        public PartType partType() {
            return null;
        }
        
    }

}
