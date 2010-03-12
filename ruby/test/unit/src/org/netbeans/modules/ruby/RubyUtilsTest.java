/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.ruby;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        assertTrue(RubyUtils.isValidConstantFQN("A"));
        assertTrue(RubyUtils.isValidConstantFQN("AB::B::C"));
        assertTrue(RubyUtils.isValidConstantFQN("Abc::D1_3"));

        assertFalse(RubyUtils.isValidConstantFQN("Abc::1_3"));
        assertFalse(RubyUtils.isValidConstantFQN("Abc:D1_3"));
        assertFalse(RubyUtils.isValidConstantFQN(""));
        assertFalse(RubyUtils.isValidConstantFQN("@foo"));
        assertFalse(RubyUtils.isValidConstantFQN("1::B"));
    }
    
    public void testClassIdentifiers() {
        assertTrue(RubyUtils.isValidConstantName("A"));
        assertTrue(RubyUtils.isValidConstantName("AB"));
        assertTrue(RubyUtils.isValidConstantName("Abc"));
        assertTrue(!RubyUtils.isValidConstantName(""));
        assertTrue(!RubyUtils.isValidConstantName("abc"));
        assertTrue(!RubyUtils.isValidConstantName(" Abc"));
        assertTrue(!RubyUtils.isValidConstantName(":Abc"));
        assertTrue(!RubyUtils.isValidConstantName("def"));
    }

    public void testMethodIdentifiers() {
        assertTrue(RubyUtils.isValidRubyMethodName("a"));
        assertTrue(RubyUtils.isValidRubyMethodName("ab"));
        assertTrue(RubyUtils.isValidRubyMethodName("ab_"));
        assertTrue(RubyUtils.isValidRubyMethodName("cde?"));
        assertTrue(RubyUtils.isValidRubyMethodName("[]"));
        assertTrue(RubyUtils.isValidRubyMethodName("[]="));
        assertTrue(RubyUtils.isValidRubyMethodName("-"));
        assertTrue(RubyUtils.isValidRubyMethodName("+"));
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
            assertTrue(!RubyUtils.isValidConstantName(s));
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

    public void testNumericIdentifiers() {
        assertFalse(RubyUtils.isSafeIdentifierName("1", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("1a", 0));

        assertFalse(RubyUtils.isSafeIdentifierName("@1", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("@1a", 0));

        assertFalse(RubyUtils.isSafeIdentifierName("@@1", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("@@1a", 0));

        assertFalse(RubyUtils.isSafeIdentifierName("$1", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("$1a", 0));

        assertFalse(RubyUtils.isSafeIdentifierName(":1", 0));
        assertFalse(RubyUtils.isSafeIdentifierName(":1a", 0));

        assertTrue(RubyUtils.isSafeIdentifierName("a1", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("@a1", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("@@a1", 0));
        assertTrue(RubyUtils.isSafeIdentifierName(":a1", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("$a1", 0));
    }

    public void testVariableNameAndKind() {
        assertTrue(RubyUtils.isSafeIdentifierName("a", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("@a", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("@@a", 0));
        assertTrue(RubyUtils.isSafeIdentifierName(":a", 0));
        assertTrue(RubyUtils.isSafeIdentifierName("$a", 0));

        assertFalse(RubyUtils.isSafeIdentifierName("", 0));
        assertFalse(RubyUtils.isSafeIdentifierName(" ", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("@@", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("$", 0));
        assertFalse(RubyUtils.isSafeIdentifierName(":", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("@@@", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("@@@a", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("$$", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("$$a", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("::", 0));
        assertFalse(RubyUtils.isSafeIdentifierName("::a", 0));
    }

    private static final String[] RUBY_BUILTINS =
        new String[] {
            // Keywords
            "alias", "and", "BEGIN", "begin", "break", "case", "class", "def", "defined?", "do",
            "else", "elsif", "END", "end", "ensure", "false", "for", "if", "in", "module", "next",
            "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then", "true",
            "undef", "unless", "until", "when", "while", "yield",
        };
    
    public void testParseConstantName() {
        assertEquals(Arrays.asList("Kernel", "RED"), Arrays.asList(RubyUtils.parseConstantName("RED")));
        assertEquals(Arrays.asList("Colors", "RED"), Arrays.asList(RubyUtils.parseConstantName("Colors::RED")));
        assertEquals(Arrays.asList("HTML::Colors", "RED"), Arrays.asList(RubyUtils.parseConstantName("HTML::Colors::RED")));
    }

    public void testJoin() {
        assertEquals("one, two, three", RubyUtils.join(new String[]{"one", "two", "three"}, ", "));
        assertEquals("", RubyUtils.join(Collections.<String>emptySet(), ", "));
    }

    public void testIsRails23OrHigher() {
        String actionmailer210 = "/usr/lib/ruby/gems/1.8/gems/actionmailer-2.1.0/something";
        String actionmailer222 = "/usr/lib/ruby/gems/1.8/gems/actionmailer-2.2.2/something";
        String actionmailer232 = "/usr/lib/ruby/gems/1.8/gems/actionmailer-2.3.2/something";
        String activesupport21 = "/usr/lib/ruby/gems/1.8/gems/activesupport-2.1/something";
        String activesupport222 = "/usr/lib/ruby/gems/1.8/gems/activesupport-2.2.2/something";
        String activesupport232 = "/usr/lib/ruby/gems/1.8/gems/activesupport-2.3.2/something";
        String activesupport24 = "/usr/lib/ruby/gems/1.8/gems/activesupport-2.4/something";

        String activemodel300beta = "/usr/lib/ruby/gems/1.8/gems/activemodel-3.0.0.beta/something";
        String activemodel300 = "/usr/lib/ruby/gems/1.8/gems/activemodel-3.0.0/something";

        String notRails = "/usr/lib/ruby/gems/1.8/gems/gemmy-2.3.2/something";

        assertFalse(RubyUtils.isRails23OrHigher(actionmailer210));
        assertFalse(RubyUtils.isRails23OrHigher(actionmailer222));
        assertTrue(RubyUtils.isRails23OrHigher(actionmailer232));

        assertFalse(RubyUtils.isRails23OrHigher(activesupport21));
        assertFalse(RubyUtils.isRails23OrHigher(activesupport222));
        assertTrue(RubyUtils.isRails23OrHigher(activesupport232));
        assertTrue(RubyUtils.isRails23OrHigher(activesupport24));

        assertTrue(RubyUtils.isRails23OrHigher(activemodel300));
        assertTrue(RubyUtils.isRails23OrHigher(activemodel300beta));

        assertFalse(RubyUtils.isRails23OrHigher(notRails));
    }

    public void testGetParentModules() {
        List<String> result = RubyUtils.getParentModules("Foo::Bar::Baz::Qux");
        assertEquals(3, result.size());
        assertEquals("Foo::Bar::Baz", result.get(0));
        assertEquals("Foo::Bar", result.get(1));
        assertEquals("Foo", result.get(2));

        result = RubyUtils.getParentModules("Qux::");
        assertEquals(1, result.size());
        assertEquals("Qux", result.get(0));

        result = RubyUtils.getParentModules("Qux");
        assertTrue(result.isEmpty());

        result = RubyUtils.getParentModules("");
        assertTrue(result.isEmpty());

    }

    public void testBaseName() {
        assertEquals("Users", RubyUtils.baseName("UsersController"));
        assertEquals("Users", RubyUtils.baseName("Users"));
    }

    public void testControllerName() {
        assertEquals("UsersController", RubyUtils.controllerName("Users"));
        assertEquals("UsersController", RubyUtils.controllerName("UsersController"));
    }

    public void testHelperName() {
        assertEquals("UsersHelper", RubyUtils.helperName("UsersController"));
        assertEquals("UsersHelper", RubyUtils.helperName("Users"));
    }

    public void testAddToArray() {
        String[] arr = {"a", "b", "c"};
        assertEquals(arr, RubyUtils.addToArray(arr));
        String[] result = RubyUtils.addToArray(arr, "d", "e");
        assertEquals(5, result.length);
        assertEquals("a", result[0]);
        assertEquals("d", result[3]);
        assertEquals("e", result[4]);

    }
}
