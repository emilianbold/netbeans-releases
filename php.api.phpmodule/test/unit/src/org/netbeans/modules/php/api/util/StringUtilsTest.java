/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.api.util;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

public class StringUtilsTest extends NbTestCase {
    private static final String TESTING_SEPARATOR = "??NB??";

    public StringUtilsTest(String name) {
        super(name);
    }

    public void testHasText() {
        assertTrue(StringUtils.hasText("a"));

        assertFalse(StringUtils.hasText(null));
        assertFalse(StringUtils.hasText(""));
        assertFalse(StringUtils.hasText(" "));
    }

    public void testImplode() {
        final List<String> items = Arrays.asList("one", "two");
        assertEquals("one*two", StringUtils.implode(items, "*"));
        assertEquals("oneonetwo", StringUtils.implode(items, "one"));
        assertEquals("one" + TESTING_SEPARATOR + "two", StringUtils.implode(items, TESTING_SEPARATOR));
    }

    public void testExplode() {
        final String[] items = {"one", "two"};
        String string = "one*two";
        assertArrayEquals(items, StringUtils.explode(string, "*").toArray(new String[0]));
        string = "one" + TESTING_SEPARATOR + "two";
        assertArrayEquals(items, StringUtils.explode(string, TESTING_SEPARATOR).toArray(new String[0]));

        // test for empty string (relative path ".")
        string = "one" + TESTING_SEPARATOR + "" + TESTING_SEPARATOR + "two";
        assertArrayEquals(new String[] {"one", "", "two"}, StringUtils.explode(string, TESTING_SEPARATOR).toArray(new String[0]));
    }
}
