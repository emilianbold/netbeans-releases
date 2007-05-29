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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author mp
 */
public class RegexpMakerTest extends NbTestCase {

    public RegexpMakerTest() {
        super("SimpleRegexpParserTest");
    }

    public void testMakeRegexp() {
        assertEquals("", RegexpMaker.makeRegexp(""));
        assertEquals("a", RegexpMaker.makeRegexp("a"));
        assertEquals("ab", RegexpMaker.makeRegexp("ab"));
        assertEquals("abc", RegexpMaker.makeRegexp("abc"));
        assertEquals("a.", RegexpMaker.makeRegexp("a?"));
        assertEquals("a.*", RegexpMaker.makeRegexp("a*"));
        assertEquals(".a", RegexpMaker.makeRegexp("?a"));
        assertEquals(".*a", RegexpMaker.makeRegexp("*a"));
        assertEquals("a.b", RegexpMaker.makeRegexp("a?b"));
        assertEquals(".a.", RegexpMaker.makeRegexp("?a?"));
        assertEquals("a.*b.c", RegexpMaker.makeRegexp("a*b?c"));
        assertEquals("a...*b", RegexpMaker.makeRegexp("a?*?b"));
        assertEquals("a..*b", RegexpMaker.makeRegexp("a*?*b"));
        
        assertEquals("a b", RegexpMaker.makeRegexp("a b"));
        assertEquals("a\\!b", RegexpMaker.makeRegexp("a!b"));
        assertEquals("a\\\"b", RegexpMaker.makeRegexp("a\"b"));
        assertEquals("a\\#b", RegexpMaker.makeRegexp("a#b"));
        assertEquals("a\\$b", RegexpMaker.makeRegexp("a$b"));
        assertEquals("a\\%b", RegexpMaker.makeRegexp("a%b"));
        assertEquals("a\\&b", RegexpMaker.makeRegexp("a&b"));
        assertEquals("a\\'b", RegexpMaker.makeRegexp("a'b"));
        assertEquals("a\\(b", RegexpMaker.makeRegexp("a(b"));
        assertEquals("a\\)b", RegexpMaker.makeRegexp("a)b"));
        assertEquals("a\\+b", RegexpMaker.makeRegexp("a+b"));
        assertEquals("a\\,b", RegexpMaker.makeRegexp("a,b"));
        assertEquals("a\\-b", RegexpMaker.makeRegexp("a-b"));
        assertEquals("a\\.b", RegexpMaker.makeRegexp("a.b"));
        assertEquals("a\\/b", RegexpMaker.makeRegexp("a/b"));
        
        assertEquals("a0b", RegexpMaker.makeRegexp("a0b"));
        assertEquals("a1b", RegexpMaker.makeRegexp("a1b"));
        assertEquals("a2b", RegexpMaker.makeRegexp("a2b"));
        assertEquals("a3b", RegexpMaker.makeRegexp("a3b"));
        assertEquals("a4b", RegexpMaker.makeRegexp("a4b"));
        assertEquals("a5b", RegexpMaker.makeRegexp("a5b"));
        assertEquals("a6b", RegexpMaker.makeRegexp("a6b"));
        assertEquals("a7b", RegexpMaker.makeRegexp("a7b"));
        assertEquals("a8b", RegexpMaker.makeRegexp("a8b"));
        assertEquals("a9b", RegexpMaker.makeRegexp("a9b"));
        
        assertEquals("a\\:b", RegexpMaker.makeRegexp("a:b"));
        assertEquals("a\\;b", RegexpMaker.makeRegexp("a;b"));
        assertEquals("a\\<b", RegexpMaker.makeRegexp("a<b"));
        assertEquals("a\\=b", RegexpMaker.makeRegexp("a=b"));
        assertEquals("a\\>b", RegexpMaker.makeRegexp("a>b"));
        assertEquals("a\\@b", RegexpMaker.makeRegexp("a@b"));
        assertEquals("a\\[b", RegexpMaker.makeRegexp("a[b"));
        assertEquals("ab", RegexpMaker.makeRegexp("a\\b"));
        assertEquals("a\\]b", RegexpMaker.makeRegexp("a]b"));
        assertEquals("a\\^b", RegexpMaker.makeRegexp("a^b"));
        assertEquals("a\\_b", RegexpMaker.makeRegexp("a_b"));
        assertEquals("a\\`b", RegexpMaker.makeRegexp("a`b"));
        assertEquals("a\\{b", RegexpMaker.makeRegexp("a{b"));
        assertEquals("a\\|b", RegexpMaker.makeRegexp("a|b"));
        assertEquals("a\\}b", RegexpMaker.makeRegexp("a}b"));
        assertEquals("a\\~b", RegexpMaker.makeRegexp("a~b"));
        assertEquals("a\\\u007fb", RegexpMaker.makeRegexp("a\u007fb"));
        
        assertEquals("a\u0080b", RegexpMaker.makeRegexp("a\u0080b"));
        assertEquals("a\u00c1b", RegexpMaker.makeRegexp("a\u00c1b"));
        
        assertEquals("abc\\\\", RegexpMaker.makeRegexp("abc\\"));
    }
    
    public void testMakeMultiRegexp() {
        assertEquals("", RegexpMaker.makeMultiRegexp(""));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a"));
        assertEquals("ab", RegexpMaker.makeMultiRegexp("ab"));
        assertEquals("abc", RegexpMaker.makeMultiRegexp("abc"));
        assertEquals("a.", RegexpMaker.makeMultiRegexp("a?"));
        assertEquals("a.*", RegexpMaker.makeMultiRegexp("a*"));
        assertEquals(".a", RegexpMaker.makeMultiRegexp("?a"));
        assertEquals(".*a", RegexpMaker.makeMultiRegexp("*a"));
        assertEquals("a.b", RegexpMaker.makeMultiRegexp("a?b"));
        assertEquals(".a.", RegexpMaker.makeMultiRegexp("?a?"));
        assertEquals("a.*b.c", RegexpMaker.makeMultiRegexp("a*b?c"));
        assertEquals("a...*b", RegexpMaker.makeMultiRegexp("a?*?b"));
        assertEquals("a..*b", RegexpMaker.makeMultiRegexp("a*?*b"));
        
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a b"));
        assertEquals("a\\!b", RegexpMaker.makeMultiRegexp("a!b"));
        assertEquals("a\\\"b", RegexpMaker.makeMultiRegexp("a\"b"));
        assertEquals("a\\#b", RegexpMaker.makeMultiRegexp("a#b"));
        assertEquals("a\\$b", RegexpMaker.makeMultiRegexp("a$b"));
        assertEquals("a\\%b", RegexpMaker.makeMultiRegexp("a%b"));
        assertEquals("a\\&b", RegexpMaker.makeMultiRegexp("a&b"));
        assertEquals("a\\'b", RegexpMaker.makeMultiRegexp("a'b"));
        assertEquals("a\\(b", RegexpMaker.makeMultiRegexp("a(b"));
        assertEquals("a\\)b", RegexpMaker.makeMultiRegexp("a)b"));
        assertEquals("a\\+b", RegexpMaker.makeMultiRegexp("a+b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a,b"));
        assertEquals("a\\-b", RegexpMaker.makeMultiRegexp("a-b"));
        assertEquals("a\\.b", RegexpMaker.makeMultiRegexp("a.b"));
        assertEquals("a\\/b", RegexpMaker.makeMultiRegexp("a/b"));
        
        assertEquals("a0b", RegexpMaker.makeMultiRegexp("a0b"));
        assertEquals("a1b", RegexpMaker.makeMultiRegexp("a1b"));
        assertEquals("a2b", RegexpMaker.makeMultiRegexp("a2b"));
        assertEquals("a3b", RegexpMaker.makeMultiRegexp("a3b"));
        assertEquals("a4b", RegexpMaker.makeMultiRegexp("a4b"));
        assertEquals("a5b", RegexpMaker.makeMultiRegexp("a5b"));
        assertEquals("a6b", RegexpMaker.makeMultiRegexp("a6b"));
        assertEquals("a7b", RegexpMaker.makeMultiRegexp("a7b"));
        assertEquals("a8b", RegexpMaker.makeMultiRegexp("a8b"));
        assertEquals("a9b", RegexpMaker.makeMultiRegexp("a9b"));
        
        assertEquals("a\\:b", RegexpMaker.makeMultiRegexp("a:b"));
        assertEquals("a\\;b", RegexpMaker.makeMultiRegexp("a;b"));
        assertEquals("a\\<b", RegexpMaker.makeMultiRegexp("a<b"));
        assertEquals("a\\=b", RegexpMaker.makeMultiRegexp("a=b"));
        assertEquals("a\\>b", RegexpMaker.makeMultiRegexp("a>b"));
        assertEquals("a\\@b", RegexpMaker.makeMultiRegexp("a@b"));
        assertEquals("a\\[b", RegexpMaker.makeMultiRegexp("a[b"));
        assertEquals("ab", RegexpMaker.makeMultiRegexp("a\\b"));
        assertEquals("a\\]b", RegexpMaker.makeMultiRegexp("a]b"));
        assertEquals("a\\^b", RegexpMaker.makeMultiRegexp("a^b"));
        assertEquals("a\\_b", RegexpMaker.makeMultiRegexp("a_b"));
        assertEquals("a\\`b", RegexpMaker.makeMultiRegexp("a`b"));
        assertEquals("a\\{b", RegexpMaker.makeMultiRegexp("a{b"));
        assertEquals("a\\|b", RegexpMaker.makeMultiRegexp("a|b"));
        assertEquals("a\\}b", RegexpMaker.makeMultiRegexp("a}b"));
        assertEquals("a\\~b", RegexpMaker.makeMultiRegexp("a~b"));
        assertEquals("a\\\u007fb", RegexpMaker.makeMultiRegexp("a\u007fb"));
        
        assertEquals("a\u0080b", RegexpMaker.makeMultiRegexp("a\u0080b"));
        assertEquals("a\u00c1b", RegexpMaker.makeMultiRegexp("a\u00c1b"));
        
        assertEquals("abc\\\\", RegexpMaker.makeRegexp("abc\\"));
        
        assertEquals("", RegexpMaker.makeMultiRegexp(""));
        assertEquals("", RegexpMaker.makeMultiRegexp(" "));
        assertEquals("", RegexpMaker.makeMultiRegexp(","));
        assertEquals("", RegexpMaker.makeMultiRegexp(", "));
        assertEquals("", RegexpMaker.makeMultiRegexp(" ,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a "));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a, "));
        assertEquals("a", RegexpMaker.makeMultiRegexp("a ,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(",a"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(" a"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(", a"));
        assertEquals("a", RegexpMaker.makeMultiRegexp(" ,a"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a,b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a, b"));
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a ,b"));
        assertEquals(" ", RegexpMaker.makeMultiRegexp("\\ "));
        assertEquals("\\,", RegexpMaker.makeMultiRegexp("\\,"));
        assertEquals("\\,", RegexpMaker.makeMultiRegexp("\\, "));
        assertEquals(" ", RegexpMaker.makeMultiRegexp(",\\ "));
        assertEquals("\\, ", RegexpMaker.makeMultiRegexp("\\,\\ "));
        assertEquals(" ", RegexpMaker.makeMultiRegexp("\\ ,"));
        assertEquals("\\,", RegexpMaker.makeMultiRegexp(" \\,"));
        assertEquals(" \\,", RegexpMaker.makeMultiRegexp("\\ \\,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a,"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("a\\,"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("\\a\\,"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a "));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("a\\ "));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("\\a\\ "));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("a, \\"));
        assertEquals("a| ", RegexpMaker.makeMultiRegexp("a,\\ "));
        assertEquals("a| \\\\", RegexpMaker.makeMultiRegexp("a,\\ \\"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("a\\, "));
        assertEquals("a\\,|\\\\", RegexpMaker.makeMultiRegexp("a\\, \\"));
        assertEquals("a\\, ", RegexpMaker.makeMultiRegexp("a\\,\\ "));
        assertEquals("a\\, \\\\", RegexpMaker.makeMultiRegexp("a\\,\\ \\"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a, "));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("\\a, \\"));
        assertEquals("a| ", RegexpMaker.makeMultiRegexp("\\a,\\ "));
        assertEquals("a| \\\\", RegexpMaker.makeMultiRegexp("\\a,\\ \\"));
        assertEquals("a\\,", RegexpMaker.makeMultiRegexp("\\a\\, "));
        assertEquals("a\\,|\\\\", RegexpMaker.makeMultiRegexp("\\a\\, \\"));
        assertEquals("a\\, ", RegexpMaker.makeMultiRegexp("\\a\\,\\ "));
        assertEquals("a\\, \\\\", RegexpMaker.makeMultiRegexp("\\a\\,\\ \\"));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("a ,\\"));
        assertEquals("a|\\,", RegexpMaker.makeMultiRegexp("a \\,"));
        assertEquals("a|\\,\\\\", RegexpMaker.makeMultiRegexp("a \\,\\"));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("a\\ ,"));
        assertEquals("a |\\\\", RegexpMaker.makeMultiRegexp("a\\ ,\\"));
        assertEquals("a \\,", RegexpMaker.makeMultiRegexp("a\\ \\,"));
        assertEquals("a \\,\\\\", RegexpMaker.makeMultiRegexp("a\\ \\,\\"));
        assertEquals("a", RegexpMaker.makeMultiRegexp("\\a ,"));
        assertEquals("a|\\\\", RegexpMaker.makeMultiRegexp("\\a ,\\"));
        assertEquals("a|\\,", RegexpMaker.makeMultiRegexp("\\a \\,"));
        assertEquals("a|\\,\\\\", RegexpMaker.makeMultiRegexp("\\a \\,\\"));
        assertEquals("a ", RegexpMaker.makeMultiRegexp("\\a\\ ,"));
        assertEquals("a |\\\\", RegexpMaker.makeMultiRegexp("\\a\\ ,\\"));
        assertEquals("a \\,", RegexpMaker.makeMultiRegexp("\\a\\ \\,"));
        assertEquals("a \\,\\\\", RegexpMaker.makeMultiRegexp("\\a\\ \\,\\"));
        
        assertEquals("a|b", RegexpMaker.makeMultiRegexp("a, b"));
        assertEquals("a|.*b.", RegexpMaker.makeMultiRegexp("a,*b?"));
        assertEquals("a|\\*b.", RegexpMaker.makeMultiRegexp("a,\\*b?"));
        assertEquals("a|.*b\\?", RegexpMaker.makeMultiRegexp("a,*b\\?"));
    }
    
}
