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

package org.netbeans.modules.properties;

import java.lang.reflect.Method;
import junit.framework.TestCase;

/**
 *
 * @author  Marian Petras
 */
public class ElementTest extends TestCase {
    
    private Method escaperMethod;

    public ElementTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() {
        escaperMethod = null;
    }

    public void testEscapeSpecialCharsInKey() throws Exception {
        initMethod();

        /* spaces: */
        checkTranslation("", "");
        checkTranslation(" ", "\\ ");
        checkTranslation("  ", "\\ \\ ");
        checkTranslation("   ", "\\ \\ \\ ");
        checkTranslation("a ", "a\\ ");
        checkTranslation(" a", "\\ a");
        checkTranslation(" a ", "\\ a\\ ");
        checkTranslation("a b", "a\\ b");
        checkTranslation("a  b", "a\\ \\ b");
        checkTranslation("a   b", "a\\ \\ \\ b");
        checkTranslation("a    b", "a\\ \\ \\ \\ b");
        checkTranslation("a b c", "a\\ b\\ c");
        checkTranslation("a b  c", "a\\ b\\ \\ c");
        checkTranslation("alpha beta gamma", "alpha\\ beta\\ gamma");
        checkTranslation(" alpha  beta   gamma    ",
                         "\\ alpha\\ \\ beta\\ \\ \\ gamma\\ \\ \\ \\ ");

        /* equal signs: */
        checkTranslation("", "");
        checkTranslation("=", "\\=");
        checkTranslation("==", "\\=\\=");
        checkTranslation("===", "\\=\\=\\=");
        checkTranslation("a=", "a\\=");
        checkTranslation("=a", "\\=a");
        checkTranslation("=a=", "\\=a\\=");
        checkTranslation("a=b", "a\\=b");
        checkTranslation("a==b", "a\\=\\=b");
        checkTranslation("a===b", "a\\=\\=\\=b");
        checkTranslation("a====b", "a\\=\\=\\=\\=b");
        checkTranslation("a=b=c", "a\\=b\\=c");
        checkTranslation("a=b==c", "a\\=b\\=\\=c");
        checkTranslation("alpha=beta=gamma", "alpha\\=beta\\=gamma");
        checkTranslation("=alpha==beta===gamma====",
                         "\\=alpha\\=\\=beta\\=\\=\\=gamma\\=\\=\\=\\=");

        /* double quotes: */
        checkTranslation("", "");
        checkTranslation(":", "\\:");
        checkTranslation("::", "\\:\\:");
        checkTranslation(":::", "\\:\\:\\:");
        checkTranslation("a:", "a\\:");
        checkTranslation(":a", "\\:a");
        checkTranslation(":a:", "\\:a\\:");
        checkTranslation("a:b", "a\\:b");
        checkTranslation("a::b", "a\\:\\:b");
        checkTranslation("a:::b", "a\\:\\:\\:b");
        checkTranslation("a::::b", "a\\:\\:\\:\\:b");
        checkTranslation("a:b:c", "a\\:b\\:c");
        checkTranslation("a:b::c", "a\\:b\\:\\:c");
        checkTranslation("alpha:beta:gamma", "alpha\\:beta\\:gamma");
        checkTranslation(":alpha::beta:::gamma::::",
                         "\\:alpha\\:\\:beta\\:\\:\\:gamma\\:\\:\\:\\:");

        /* BS, tab, LF, FF, CR and other ISO control chars: */
        checkTranslation("\u0000", "\\u0000");
        checkTranslation("\u0001", "\\u0001");
        checkTranslation("\u0002", "\\u0002");
        checkTranslation("\u0003", "\\u0003");
        checkTranslation("\u0004", "\\u0004");
        checkTranslation("\u0005", "\\u0005");
        checkTranslation("\u0006", "\\u0006");
        checkTranslation("\u0007", "\\u0007");
        checkTranslation("\b",     "\\b");
        checkTranslation("\t",     "\\t");
        checkTranslation("\n",     "\\n");
        checkTranslation("\u000b", "\\u000b");
        checkTranslation("\f",     "\\f");
        checkTranslation("\r",     "\\r");
        checkTranslation("\u000e", "\\u000e");
        checkTranslation("\u000f", "\\u000f");
        checkTranslation("\u0010", "\\u0010");
        checkTranslation("\u0011", "\\u0011");
        checkTranslation("\u0012", "\\u0012");
        checkTranslation("\u0013", "\\u0013");
        checkTranslation("\u0014", "\\u0014");
        checkTranslation("\u0015", "\\u0015");
        checkTranslation("\u0016", "\\u0016");
        checkTranslation("\u0017", "\\u0017");
        checkTranslation("\u0018", "\\u0018");
        checkTranslation("\u0019", "\\u0019");
        checkTranslation("\u001a", "\\u001a");
        checkTranslation("\u001b", "\\u001b");
        checkTranslation("\u001c", "\\u001c");
        checkTranslation("\u001d", "\\u001d");
        checkTranslation("\u001e", "\\u001e");
        checkTranslation("\u001f", "\\u001f");

        /* comment chars (#, !): */
        checkTranslation("#", "\\#");
        checkTranslation("#abc", "\\#abc");
        checkTranslation("##", "\\##");
        checkTranslation("##abc", "\\##abc");
        checkTranslation("##abc#def", "\\##abc#def");
        checkTranslation("!", "\\!");
        checkTranslation("!abc", "\\!abc");
        checkTranslation("!!", "\\!!");
        checkTranslation("!!abc", "\\!!abc");
        checkTranslation("!!abc!def", "\\!!abc!def");
        checkTranslation(" #", "\\ #");
        checkTranslation(" !", "\\ !");
        checkTranslation("#!", "\\#!");
        checkTranslation("!#", "\\!#");
        checkTranslation("# !", "\\#\\ !");
        checkTranslation("! #", "\\!\\ #");
        checkTranslation(" #!", "\\ #!");
        checkTranslation(" !#", "\\ !#");
    }

    private void checkTranslation(String origText,
                                  String expectedTranslation) throws Exception {
        String actualTranslation = (String) escaperMethod.invoke(null, origText);
        assertEquals(expectedTranslation, actualTranslation);
    }

    private void initMethod() throws Exception {
        Class<Element.KeyElem> cls = Element.KeyElem.class;
        escaperMethod = cls.getDeclaredMethod("escapeSpecialChars", String.class);
        escaperMethod.setAccessible(true);
    }

}
