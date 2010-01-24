/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.jackpot.impl.TestBase;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixTest extends TestBase {

    public JavaFixTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }
    
    public void testSimpleDate() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5 (16 May 2005)\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testLongText() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.123.2.1 - branch propsheet_issue_29447\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.123.2.1")));
    }

    public void testModuleName() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since org.openide.filesystems 7.15\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("7.15")));
    }

    public void testModuleNameMajor() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since org.openide/1 4.42\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("4.42")));
    }

    public void testEnd() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5 */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testOpenAPI() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since OpenAPI version 2.12" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("2.12")));

    }

    private SpecificationVersion computeSpecVersion(String javadoc) throws Exception {
        prepareTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    javadoc +
                    "     public void test() {\n" +
                    "     }\n" +
                    "}\n");

        TypeElement te = info.getElements().getTypeElement("test.Test");
        ExecutableElement method = ElementFilter.methodsIn(te.getEnclosedElements()).iterator().next();

        return JavaFix.computeSpecVersion(info, method);
    }
}