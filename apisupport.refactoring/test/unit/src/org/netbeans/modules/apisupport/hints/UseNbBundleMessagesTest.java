/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.hints;

import java.net.URL;
import org.junit.Test;
import static org.netbeans.modules.apisupport.hints.Bundle.*;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class UseNbBundleMessagesTest {

    @Test public void regularWarning() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    String m() {\n" +
                       "        return org.openide.util.NbBundle.getMessage(Test.class, \"somekey\");\n" +
                       "    }\n" +
                       "}\n").
                input("test/Bundle.properties", "somekey=text\n", false).
                run(UseNbBundleMessages.class).
                assertWarnings("3:41-3:51:warning:" + UseNbBundleMessages_error_text());
    }

    @Test public void simpleFix() throws Exception {
        HintTest.create().classpath(cp()).
                input("package test;\n" +
                       "class Test {\n" +
                       "    String m() {\n" +
                       "        return org.openide.util.NbBundle.getMessage(Test.class, \"somekey\");\n" +
                       "    }\n" +
                       "}\n").
                input("test/Bundle.properties", "somekey=text\n", false).
                run(UseNbBundleMessages.class).
                findWarning("3:41-3:51:warning:" + UseNbBundleMessages_error_text()).
                applyFix().
                /* XXX does not work because Bundle not yet generated & parsed:
                assertCompilable().
                */
                assertVerbatimOutput("test/Bundle.properties", "").
                assertOutput("package test;\n" +
                       "import org.openide.util.NbBundle.Messages;\n" +
                       "import static test.Bundle.*;\n" +
                       "class Test {\n" +
                       "    @Messages(\"somekey=text\")\n" +
                       "    String m() {\n" +
                       "        return somekey();\n" +
                       "    }\n" +
                       "}\n");
    }

    private URL cp() {
        URL cp = NbBundle.class.getProtectionDomain().getCodeSource().getLocation();
        return cp.toString().endsWith("/") ? cp : FileUtil.getArchiveRoot(cp);
    }

}
