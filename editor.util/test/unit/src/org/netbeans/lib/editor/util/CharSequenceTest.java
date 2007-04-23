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

package org.netbeans.lib.editor.util;

import junit.framework.TestCase;

public class CharSequenceTest extends TestCase {

    public CharSequenceTest(String testName) {
        super(testName);
    }

    public void testCharSequenceStringLike() {
        CharSequence text = new DelegateCharSequence("abcde");
        assertTrue(text.equals("abcde"));
        CharSequence subText = text.subSequence(1, 3);
        assertTrue(subText.equals("bc"));
    }
    
    private static final class DelegateCharSequence extends AbstractCharSequence.StringLike {
        
        private final String text;
        
        DelegateCharSequence(String text) {
            this.text = text;
        }
        
        public char charAt(int index) {
            return text.charAt(index);
        }
        
        public int length() {
            return text.length();
        }

    }

}
