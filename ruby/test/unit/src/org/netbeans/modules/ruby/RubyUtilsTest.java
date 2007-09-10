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

package org.netbeans.modules.ruby;

import junit.framework.TestCase;

/**
 *
 * @author Tor Norbye
 */
public class RubyUtilsTest extends TestCase {
    
    public RubyUtilsTest(String testName) {
        super(testName);
    }

    public void testIsInvalidMultibytechars() {
        assertTrue(RubyUtils.isSafeIdentifierName("foo_bar", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("FOO", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("foo_bar=", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("foo_bar?", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("foo_bar!", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("$foo", 1));
        assertTrue(RubyUtils.isSafeIdentifierName("@foo", 1));
        assertTrue(RubyUtils.isSafeIdentifierName("@@foo", 2));
        assertFalse(RubyUtils.isSafeIdentifierName("abc\\u1234", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("Torbjørn", 0));
    }
    
    public void testCamelToUnderlinedName() {
        assertEquals("foo", RubyUtils.camelToUnderlinedName("Foo"));
        assertEquals("foo", RubyUtils.camelToUnderlinedName("foo"));
        assertEquals("foo_bar", RubyUtils.camelToUnderlinedName("FooBar"));
        assertEquals("test_class", RubyUtils.camelToUnderlinedName("TestClass"));
        assertEquals("test_class", RubyUtils.camelToUnderlinedName("Test_Class"));
        assertEquals("_my_class", RubyUtils.camelToUnderlinedName("_MyClass"));
        assertEquals("t_n", RubyUtils.camelToUnderlinedName("TN"));
        assertEquals("t_n_t", RubyUtils.camelToUnderlinedName("TNT"));
    }
    
    public void testUnderlinedToCamelName() {
        assertEquals("Foo", RubyUtils.underlinedNameToCamel("foo"));
        assertEquals("FooBar", RubyUtils.underlinedNameToCamel("foo_bar"));
        assertEquals("Foo::BarBaz", RubyUtils.underlinedNameToCamel("foo/bar_baz"));
        assertEquals("JavaScriptMacrosHelper", RubyUtils.underlinedNameToCamel("java_script_macros_helper"));
    }

    public void testModuleStrings() {
        assertTrue(RubyUtils.isValidRubyModuleName("A"));
        assertTrue(RubyUtils.isValidRubyModuleName("AB::B::C"));
        assertTrue(RubyUtils.isValidRubyModuleName("Abc::D1_3"));

        assertFalse(RubyUtils.isValidRubyModuleName("Abc::1_3"));
        assertFalse(RubyUtils.isValidRubyModuleName("Abc:D1_3"));
        assertFalse(RubyUtils.isValidRubyModuleName(""));
        assertFalse(RubyUtils.isValidRubyModuleName("@foo"));
        assertFalse(RubyUtils.isValidRubyModuleName("1::B"));
    }
    
    public void testClassIdentifiers() {
        assertTrue(RubyUtils.isValidRubyClassName("A"));
        assertTrue(RubyUtils.isValidRubyClassName("AB"));
        assertTrue(RubyUtils.isValidRubyClassName("Abc"));
        assertTrue(!RubyUtils.isValidRubyClassName(""));
        assertTrue(!RubyUtils.isValidRubyClassName("abc"));
        assertTrue(!RubyUtils.isValidRubyClassName(" Abc"));
        assertTrue(!RubyUtils.isValidRubyClassName(":Abc"));
        assertTrue(!RubyUtils.isValidRubyClassName("def"));
    }

    public void testMethodIdentifiers() {
        assertTrue(RubyUtils.isValidRubyMethodName("a"));
        assertTrue(RubyUtils.isValidRubyMethodName("ab"));
        assertTrue(RubyUtils.isValidRubyMethodName("ab_"));
        assertTrue(RubyUtils.isValidRubyMethodName("cde?"));
        assertTrue(RubyUtils.isValidRubyMethodName("[]"));
        assertTrue(RubyUtils.isValidRubyMethodName("[]="));
        assertTrue(RubyUtils.isValidRubyMethodName("<=>"));
        assertTrue(RubyUtils.isValidRubyMethodName("<="));
        assertTrue(RubyUtils.isValidRubyMethodName("`"));

        assertTrue(!RubyUtils.isValidRubyMethodName("Abc"));
        assertTrue(!RubyUtils.isValidRubyMethodName(" def"));
        assertTrue(!RubyUtils.isValidRubyMethodName(""));
        assertTrue(!RubyUtils.isValidRubyMethodName("ijk "));
        assertTrue(!RubyUtils.isValidRubyMethodName("=>"));
        assertTrue(!RubyUtils.isValidRubyMethodName("^^"));
        assertTrue(!RubyUtils.isValidRubyMethodName("***"));
        assertTrue(!RubyUtils.isValidRubyMethodName(".."));
        assertTrue(!RubyUtils.isValidRubyMethodName("["));

        assertTrue(RubyUtils.isValidRubyMethodName("abc?"));
        assertTrue(RubyUtils.isValidRubyMethodName("abc="));
        assertTrue(RubyUtils.isValidRubyMethodName("abc!"));
        assertTrue(!RubyUtils.isValidRubyMethodName("ab!c"));
        assertTrue(!RubyUtils.isValidRubyMethodName("ab?c"));
        assertTrue(!RubyUtils.isValidRubyMethodName("ab=c"));

        
        assertTrue(RubyUtils.isValidRubyMethodName("abc"));
        assertTrue(RubyUtils.isValidRubyMethodName("ab_c"));
        assertTrue(RubyUtils.isValidRubyMethodName("_abc"));
        assertTrue(RubyUtils.isValidRubyMethodName("abc3"));
        assertTrue(RubyUtils.isValidRubyMethodName("abcDef"));
        // keywords
        
        for (String s : RUBY_BUILTINS) {
            assertTrue(!RubyUtils.isValidRubyMethodName(s));
            assertTrue(!RubyUtils.isValidRubyClassName(s));
        }
    }

    public void testLocalVars() {
        assertTrue(!RubyUtils.isValidRubyLocalVarName("Abc"));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("abc "));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("ab!c"));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("ab?c"));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("ab=c"));

        assertTrue(RubyUtils.isValidRubyLocalVarName("abc"));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("abc?"));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("abc="));
        assertTrue(!RubyUtils.isValidRubyLocalVarName("abc!"));
        assertTrue(RubyUtils.isValidRubyLocalVarName("ab_c"));
        assertTrue(RubyUtils.isValidRubyLocalVarName("_abc"));
        assertTrue(RubyUtils.isValidRubyLocalVarName("abc3"));
        assertTrue(RubyUtils.isValidRubyLocalVarName("abcDef"));
    
    }
    
    private static final String[] RUBY_BUILTINS =
        new String[] {
            // Keywords
            "alias", "and", "BEGIN", "begin", "break", "case", "class", "def", "defined?", "do",
            "else", "elsif", "END", "end", "ensure", "false", "for", "if", "in", "module", "next",
            "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then", "true",
            "undef", "unless", "until", "when", "while", "yield",
        };
}
