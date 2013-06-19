/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbverification.rules;

import java.io.IOException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.modules.j2ee.ejbverification.TestBase;
import static org.netbeans.modules.j2ee.ejbverification.TestBase.copyStringToFileObject;
import org.netbeans.modules.j2ee.ejbverification.TestEJBProblemFinder;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class BusinessMethodExposedTest extends TestBase {

    FileObject testBean;

    public BusinessMethodExposedTest(String name) {
        super(name);
    }

    public void createTestBeanWithMethod(TestBase.TestModule testModule) throws Exception {
        String testBeanContent = "package pkg;\n"
                + "@javax.ejb.Stateless\n"
                + "@javax.ejb.LocalBean\n"
                + "public class TestBean {\n"
                + "  public void businessMethod() {}\n"
                + "}";
        testBean = FileUtil.createData(testModule.getSources()[0], "pkg/TestBean.java");
        copyStringToFileObject(testBean, testBeanContent);
        RepositoryUpdater.getDefault().refreshAll(true, true, true, null, (Object[]) testModule.getSources());
    }

    public void testBussinessMethodExposedEE5() throws Exception {
        TestBase.TestModule testModule = createEjb30Module();
        assertNotNull(testModule);
        createTestBeanWithMethod(testModule);
        checkBussinessMethodExposedHint(true);
    }

    public void testBussinessMethodExposedEE6Lite() throws Exception {
        TestBase.TestModule testModule = createWeb30Module();
        assertNotNull(testModule);
        createTestBeanWithMethod(testModule);
        checkBussinessMethodExposedHint(false);
    }

    public void testBussinessMethodExposedEE7Lite() throws Exception {
        TestBase.TestModule testModule = createWeb31Module();
        assertNotNull(testModule);
        createTestBeanWithMethod(testModule);
        checkBussinessMethodExposedHint(false);
    }

    public void testBussinessMethodExposedEE6Full() throws Exception {
        TestBase.TestModule testModule = createEjb31Module();
        assertNotNull(testModule);
        createTestBeanWithMethod(testModule);
        checkBussinessMethodExposedHint(false);
    }

    public void testBussinessMethodExposedEE7Full() throws Exception {
        TestBase.TestModule testModule = createEjb32Module();
        assertNotNull(testModule);
        createTestBeanWithMethod(testModule);
        checkBussinessMethodExposedHint(false);
    }

    private void checkBussinessMethodExposedHint(boolean ruleExists) throws IOException {
        TestEJBProblemFinder finder = new TestEJBProblemFinder(testBean, new BusinessMethodExposed());
        finder.run();
        if (ruleExists) {
            assertEquals("TestBean[line 4] (HINT) Method not exposed in any business interface",
                    errorDescriptionToString(finder.getProblemsFound()));
        } else {
            assertTrue("Errors/Hints of the file are not empty for given hint", finder.getProblemsFound().isEmpty());
        }
    }
}
