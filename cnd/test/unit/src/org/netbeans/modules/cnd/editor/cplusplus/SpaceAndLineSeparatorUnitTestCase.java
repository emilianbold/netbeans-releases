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

import junit.framework.TestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class SpaceAndLineSeparatorUnitTestCase extends TestCase {
    
    public SpaceAndLineSeparatorUnitTestCase(String testName) {
        super(testName);
    }
    
    public void testIsSpace() {
        boolean res = Character.isSpaceChar(' ');
        assertTrue("Character.isSpaceChar for ' ' must be true", res);
        res = Character.isSpaceChar('\n');
        assertFalse("Character.isSpaceChar for '\\n' must be false ", res);
        res = Character.isSpaceChar('\t');
        assertFalse("Character.isSpaceChar for '\\t' must be false", res);
        res = Character.isSpaceChar('\r');
        assertFalse("Character.isSpaceChar for '\\r' must be false", res);
    }
    
    public void testIsWhitespace() {
        boolean res = Character.isWhitespace(' ');
        assertTrue("Character.isWhitespace for ' ' must be true", res);
        res = Character.isWhitespace('\n');
        assertTrue("Character.isWhitespace for '\\n' must be true ", res);
        res = Character.isWhitespace('\t');
        assertTrue("Character.isWhitespace for '\\t' must be true", res);
        res = Character.isWhitespace('\r');
        assertTrue("Character.isWhitespace for '\\r' must be true", res);
    }
    
    public void testSyntaxIsSpaceChar() {
        boolean res = CCSyntax.isSpaceChar(' ');
        assertTrue("CCSyntax.isSpaceChar for ' ' must be true", res);
        res = CCSyntax.isSpaceChar('\n');
        assertFalse("CCSyntax.isSpaceChar for '\\n' must be false ", res);
        res = CCSyntax.isSpaceChar('\t');
        assertTrue("CCSyntax.isSpaceChar for '\\t' must be true", res);
        res = CCSyntax.isSpaceChar('\r');
        assertFalse("CCSyntax.isSpaceChar for '\\r' must be false", res);
    }
    
    public void testSyntaxLineSeparator() {
        boolean res = CCSyntax.isLineSeparator(' ');
        assertFalse("CCSyntax.isLineSeparator for ' ' must be false", res);
        res = CCSyntax.isLineSeparator('\n');
        assertTrue("CCSyntax.isLineSeparator for '\\n' must be true ", res);
        res = CCSyntax.isLineSeparator('\t');
        assertFalse("CCSyntax.isLineSeparator for '\\t' must be false", res);
        res = CCSyntax.isLineSeparator('\r');
        assertTrue("CCSyntax.isLineSeparator for '\\r' must be true", res);
    }    
    
}
