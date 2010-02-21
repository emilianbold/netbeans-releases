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
import org.netbeans.junit.RandomlyFails;

public class ActivatorTest extends NbTestCase {

    public ActivatorTest(String n) {
        super(n);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testModuleInstall() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.bundle.ran\", \"true\");}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules",
                "OpenIDE-Module-Specification-Version: 1.0").run();
        assertTrue(Boolean.getBoolean("my.bundle.ran"));
    }

    public void testModuleInstallBackwards() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.bundle.ran.again\", \"true\");}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules",
                "OpenIDE-Module-Specification-Version: 1.0").backwards().run();
        assertTrue(Boolean.getBoolean("my.bundle.ran.again"));
    }

    public void testLayers() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.file\", ",
                "org.openide.filesystems.FileUtil.getConfigFile(\"whatever\").getPath());}",
                "}").sourceFile("custom/layer.xml", "<filesystem>",
                "<file name='whatever'/>",
                "</filesystem>").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems",
                "OpenIDE-Module-Specification-Version: 1.0").run();
        assertEquals("whatever", System.getProperty("my.file"));
    }

    public void testURLStreamHandler() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {try {",
                "System.setProperty(\"my.url.length\", ",
                "Integer.toString(new java.net.URL(\"nbres:/custom/stuff\").openConnection().getContentLength()));",
                "} catch (Exception x) {x.printStackTrace();}",
                "}",
                "}").sourceFile("custom/stuff", "some text").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules",
                "OpenIDE-Module-Specification-Version: 1.0").
                backwards(). // XXX will not pass otherwise
                run();
        assertEquals("10", System.getProperty("my.url.length"));
    }

    @RandomlyFails // XXX NB-Core-Build #4023, 4026: InstanceDataObject.resolveConvertor: "Invalid settings.providerPath ... for class custom.Install$Bean"
    public void testSettings() throws Exception {
        new OSGiProcess(getWorkDir()).manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.netbeans.modules.settings/1, org.openide.loaders, " +
                "org.openide.filesystems, org.openide.modules, org.openide.util, org.netbeans.core/2",
                "OpenIDE-Module-Specification-Version: 1.0").
                // XXX consider moving Environment.Provider part of FileEntityResolver to settings module to avoid core dep:
                module("org.netbeans.modules.settings").module("org.netbeans.core").sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {",
                "Bean b = new Bean(); b.setP(\"hello\");",
                "try {",
                "org.openide.loaders.InstanceDataObject.create(org.openide.loaders.DataFolder.findFolder(",
                "org.openide.filesystems.FileUtil.getConfigRoot().createFolder(\"d\")), \"x\", b, null);",
                "System.setProperty(\"my.settings\", org.openide.filesystems.FileUtil.getConfigFile(\"d/x.settings\").asText());",
                "} catch (Exception x) {x.printStackTrace();}",
                "}",
                "@org.netbeans.api.settings.ConvertAsJavaBean public static class Bean {",
                "private String p; public String getP() {return p;} public void setP(String p2) {p = p2;}",
                "public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}",
                "public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {}",
                "}",
                "}").
                run();
        String settings = System.getProperty("my.settings");
        assertNotNull(settings);
        assertTrue(settings, settings.contains("<string>hello</string>"));
    }

    public void testDynamicImport() throws Exception {
        new OSGiProcess(getWorkDir()).sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {",
                "new javax.swing.JOptionPane().setUI(new javax.swing.plaf.basic.BasicOptionPaneUI());",
                "System.setProperty(\"my.bundle.worked\", \"true\");",
                "}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules",
                "OpenIDE-Module-Specification-Version: 1.0").run();
        assertTrue(Boolean.getBoolean("my.bundle.worked"));
    }

}
