/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor.hints;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tor Norbye
 */
public class NameStyleTest extends NbTestCase {
    public NameStyleTest(String name) {
        super(name);
    }

    public void testComplies() {
        assertTrue(NameStyle.NO_PREFERENCE.complies("foo"));
        assertTrue(NameStyle.NO_PREFERENCE.complies("s_43"));
        assertTrue(NameStyle.NO_PREFERENCE.complies("Ffoo"));

        assertTrue(NameStyle.CAPITALIZED_WITH_UNDERSCORES.complies("FOO"));
        assertTrue(NameStyle.CAPITALIZED_WITH_UNDERSCORES.complies("FOO_BAR"));
        assertTrue(NameStyle.CAPITALIZED_WITH_UNDERSCORES.complies("_FOO_BAR"));
        assertTrue(NameStyle.CAPITALIZED_WITH_UNDERSCORES.complies("__FOO_BAR__"));
        assertTrue(NameStyle.CAPITALIZED_WITH_UNDERSCORES.complies("FOO1"));
        assertTrue(!NameStyle.CAPITALIZED_WITH_UNDERSCORES.complies("foo"));

        assertTrue(NameStyle.CAPITALIZED_WORDS.complies("FooF"));
        assertTrue(NameStyle.CAPITALIZED_WORDS.complies("FooBar"));
        assertTrue(!NameStyle.CAPITALIZED_WORDS.complies("fooBar"));
        assertTrue(!NameStyle.CAPITALIZED_WORDS.complies("foobar"));

        assertTrue(NameStyle.LOWERCASE.complies("foobar"));
        assertTrue(NameStyle.LOWERCASE.complies("__foobar"));
        assertTrue(!NameStyle.LOWERCASE.complies("__Foobar"));
        assertTrue(!NameStyle.LOWERCASE.complies("__fooBar"));

        assertTrue(NameStyle.LOWERCASE_WITH_UNDERSCORES.complies("foo"));
        assertTrue(NameStyle.LOWERCASE_WITH_UNDERSCORES.complies("foo_bar"));
        assertTrue(!NameStyle.LOWERCASE_WITH_UNDERSCORES.complies("foo_Bar"));

        assertTrue(NameStyle.MIXED_CASE.complies("foobar"));
        assertTrue(NameStyle.MIXED_CASE.complies("fooBar"));
        assertTrue(!NameStyle.MIXED_CASE.complies("FooBar"));
        assertTrue(!NameStyle.MIXED_CASE.complies("foo_bar"));
        assertTrue(!NameStyle.MIXED_CASE.complies("Foobar"));

        assertTrue(NameStyle.UPPERCASE.complies("FOOBAR"));
        assertTrue(NameStyle.UPPERCASE.complies("F"));
        assertTrue(NameStyle.UPPERCASE.complies("__FOOBAR__"));
        assertTrue(NameStyle.UPPERCASE.complies("_FOOBAR"));
        assertTrue(!NameStyle.UPPERCASE.complies("FOO_BAR"));
        assertTrue(!NameStyle.UPPERCASE.complies("FooBar"));

        assertTrue(NameStyle.UPPERCASE_WITH_UNDERSCORES.complies("FOOBAR"));
        assertTrue(NameStyle.UPPERCASE_WITH_UNDERSCORES.complies("FOO_BAR"));
        assertTrue(NameStyle.UPPERCASE_WITH_UNDERSCORES.complies("__FOO_BAR__"));
        assertTrue(!NameStyle.UPPERCASE_WITH_UNDERSCORES.complies("FooBar"));
    }
}
