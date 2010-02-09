/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.core.osgi;

import org.netbeans.junit.NbTestCase;

public class ActivatorTest extends NbTestCase {

    public ActivatorTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testModuleInstall() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom; ",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.bundle.ran\", \"true\");}",
                "}").manifest("Manifest-Version: 1.0",
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules",
                "OpenIDE-Module-Specification-Version: 1.0").run();
        assertTrue(Boolean.getBoolean("my.bundle.ran"));
    }

    public void testLayers() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom; ",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.file\", ",
                "org.openide.filesystems.FileUtil.getConfigFile(\"whatever\").getPath());}",
                "}").sourceFile("custom/layer.xml", "<filesystem>",
                "<file name='whatever'/>",
                "</filesystem>").manifest("Manifest-Version: 1.0",
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems",
                "OpenIDE-Module-Specification-Version: 1.0").run();
        assertEquals("whatever", System.getProperty("my.file"));
    }

}
