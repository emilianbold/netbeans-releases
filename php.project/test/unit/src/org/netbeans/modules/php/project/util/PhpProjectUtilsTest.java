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

package org.netbeans.modules.php.project.util;

import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import static org.junit.Assert.*;

public class PhpProjectUtilsTest extends NbTestCase {

    public PhpProjectUtilsTest(String name) {
        super(name);
    }

    public void testImplode() {
        final List<String> items = Arrays.asList("one", "two");
        assertEquals("one" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "two", StringUtils.implode(items, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR));
    }

    public void testExplode() {
        final String[] items = {"one", "two"};
        String string = "one*two";
        string = "one" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "two";
        assertArrayEquals(items, StringUtils.explode(string, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR).toArray(new String[0]));

        // test for empty string (relative path ".")
        string = "one" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "" + PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR + "two";
        assertArrayEquals(new String[] {"one", "", "two"}, StringUtils.explode(string, PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR).toArray(new String[0]));
    }
}
