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

import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.TreeSet;
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
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.bundle.ran\", \"true\");}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().run();
        assertTrue(Boolean.getBoolean("my.bundle.ran"));
    }

    public void testModuleInstallBackwards() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.bundle.ran.again\", \"true\");}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().backwards().run();
        assertTrue(Boolean.getBoolean("my.bundle.ran.again"));
    }

    public void testLayers() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"my.file\", ",
                "org.openide.filesystems.FileUtil.getConfigFile(\"whatever\").getPath());}",
                "}").sourceFile("custom/layer.xml", "<filesystem>",
                "<file name='whatever'/>",
                "</filesystem>").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.filesystems").done().run();
        assertEquals("whatever", System.getProperty("my.file"));
    }

    public void testURLStreamHandler() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {try {",
                "System.setProperty(\"my.url.length\", ",
                "Integer.toString(new java.net.URL(\"nbres:/custom/stuff\").openConnection().getContentLength()));",
                "} catch (Exception x) {x.printStackTrace();}",
                "}",
                "}").sourceFile("custom/stuff", "some text").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().
                backwards(). // XXX will not pass otherwise
                run();
        assertEquals("10", System.getProperty("my.url.length"));
    }

    @RandomlyFails // sometimes in NB-Core-Build:
    // FNFE: Invalid settings.providerPath=xml/lookups/NetBeans/DTD_XML_beans_1_0.instance under SFS/xml/memory/ for class custom.Install$Bean
    public void testSettings() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.netbeans.modules.settings/1, org.openide.loaders, " +
                "org.openide.filesystems, org.openide.modules, org.openide.util").
                sourceFile("custom/Install.java", "package custom;",
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
                "}").done().
                module("org.netbeans.modules.settings").
                module("org.netbeans.modules.editor.mimelookup.impl"). // indirect dep of editor.mimelookup, from openide.loaders
                run();
        String settings = System.getProperty("my.settings");
        assertNotNull(settings);
        assertTrue(settings, settings.contains("<string>hello</string>"));
    }

    public void testJREPackageImport() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {",
                "new javax.swing.JOptionPane().setUI(new javax.swing.plaf.basic.BasicOptionPaneUI());",
                "System.setProperty(\"my.bundle.worked\", \"true\");",
                "}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().run();
        assertTrue(Boolean.getBoolean("my.bundle.worked"));
    }

    public void testModuleInfo() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"number.of.modules\",",
                "String.valueOf(org.openide.util.Lookup.getDefault().lookupAll(org.openide.modules.ModuleInfo.class).size()));}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup").done().run();
        String numberOfModules = System.getProperty("number.of.modules");
        assertNotNull(numberOfModules);
        assertTrue(numberOfModules, Integer.parseInt(numberOfModules) > 2);
    }

    public void testProvidesRequiresNeedsParsing() throws Exception {
        Hashtable<String,String> headers = new Hashtable<String,String>();
        assertEquals(Collections.emptySet(), Activator.provides(headers));
        assertEquals(Collections.emptySet(), Activator.requires(headers));
        assertEquals(Collections.emptySet(), Activator.needs(headers));
        headers.put("Bundle-SymbolicName", "org.netbeans.modules.projectui");
        headers.put("OpenIDE-Module-Provides", "org.netbeans.modules.project.uiapi.ActionsFactory,   " +
                "org.netbeans.modules.project.uiapi.OpenProjectsTrampoline,  org.netbeans.modules.project.uiapi.ProjectChooserFactory");
        assertEquals(new TreeSet<String>(Arrays.asList(
                "org.netbeans.modules.projectui",
                "org.netbeans.modules.project.uiapi.ActionsFactory",
                "org.netbeans.modules.project.uiapi.OpenProjectsTrampoline",
                "org.netbeans.modules.project.uiapi.ProjectChooserFactory"
                )), Activator.provides(headers));
        assertEquals(Collections.emptySet(), Activator.requires(headers));
        assertEquals(Collections.emptySet(), Activator.needs(headers));
        headers.clear();
        headers.put("Require-Bundle", "org.netbeans.api.progress;bundle-version=\"[101.0.0,200)\", " +
                "org.netbeans.spi.quicksearch;bundle-version=\"[1.0.0,100)\"");
        headers.put("OpenIDE-Module-Requires", "org.openide.modules.InstalledFileLocator, org.openide.modules.ModuleFormat2, org.openide.modules.os.Windows");
        assertEquals(Collections.emptySet(), Activator.provides(headers));
        assertEquals(new TreeSet<String>(Arrays.asList(
                "org.netbeans.api.progress",
                "org.netbeans.spi.quicksearch",
                "org.openide.modules.InstalledFileLocator"
                )), Activator.requires(headers));
        assertEquals(Collections.emptySet(), Activator.needs(headers));
        headers.clear();
        headers.put("OpenIDE-Module-Needs", "org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl");
        assertEquals(Collections.emptySet(), Activator.provides(headers));
        assertEquals(Collections.emptySet(), Activator.requires(headers));
        assertEquals(Collections.singleton("org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl"), Activator.needs(headers));
        headers.clear();
    }

    public void testRequireToken() throws Exception {
        new OSGiProcess(getWorkDir()).
                newModule().manifest(
                "OpenIDE-Module: zz.api",
                "OpenIDE-Module-Public-Packages: api.*",
                "OpenIDE-Module-Needs: api.Interface").
                sourceFile("api/Interface.java", "package api;",
                "public interface Interface {}").done().
                newModule().manifest(
                "OpenIDE-Module: zz.impl",
                "OpenIDE-Module-Module-Dependencies: zz.api",
                "OpenIDE-Module-Provides: api.Interface").
                sourceFile("impl/Provider.java", "package impl;",
                "@org.openide.util.lookup.ServiceProvider(service=api.Interface.class)",
                "public class Provider implements api.Interface {}").done().
                newModule().manifest(
                "OpenIDE-Module: zz.client",
                "OpenIDE-Module-Install: client.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup, zz.api").
                sourceFile("client/Install.java", "package client;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"provider.name\",",
                "org.openide.util.Lookup.getDefault().lookup(api.Interface.class).getClass().getName());}",
                "}").done().
                run();
        assertEquals("impl.Provider", System.getProperty("provider.name"));
    }

    public void testClassPathExtensions() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {",
                "org.netbeans.api.javahelp.Help.class.hashCode();",
                "javax.help.HelpSet.class.hashCode();",
                "javax.help.event.HelpSetEvent.class.hashCode();",
                "System.setProperty(\"used.javahelp\", \"true\");}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.netbeans.modules.javahelp/1").done().
                module("org.netbeans.modules.javahelp").
                module("org.netbeans.modules.editor.mimelookup.impl"). // indirect dep of editor.mimelookup, from openide.loaders
                run();
        assertTrue(Boolean.getBoolean("used.javahelp"));
    }

    public void testComSunPackages() throws Exception {
        new OSGiProcess(getWorkDir()).
                newModule().sourceFile("com/sun/java/swing/Painter.java", "package com.sun.java.swing;",
                "public interface Painter extends Runnable {}").
                manifest("OpenIDE-Module: painter", "OpenIDE-Module-Public-Packages: com.sun.java.swing.*").done().
                newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"com.sun.available\"," +
                "String.valueOf(Runnable.class.isAssignableFrom(com.sun.java.swing.Painter.class)));}",
                "}").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, painter").done().run();
        assertTrue(Boolean.getBoolean("com.sun.available"));
    }

    public void testServicesFolder() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().
                sourceFile("custom/Interface.java", "package custom; public interface Interface {String result();}").
                sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {System.setProperty(\"custom.service.result\", ",
                "org.openide.util.Lookup.getDefault().lookup(Interface.class).result());}",
                "}").
                sourceFile("custom/Service.java", "package custom; public class Service implements Interface {",
                "public String result() {return \"ok\";}",
                "}").
                sourceFile("custom/layer.xml", "<filesystem>",
                "<folder name='Services'>",
                "<file name='custom-Service.instance'><attr name='instanceOf' stringvalue='custom.Interface'/></file>",
                "</folder>",
                "</filesystem>").manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Layer: custom/layer.xml",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules, org.openide.util.lookup, org.netbeans.core/2").done().
                module("org.netbeans.core").
                module("org.netbeans.modules.editor.mimelookup.impl"). // indirect dep of editor.mimelookup, from openide.loaders
                run();
        assertEquals("ok", System.getProperty("custom.service.result"));
    }

    public void testInstalledFileLocator() throws Exception {
        new OSGiProcess(getWorkDir()).newModule().sourceFile("custom/Install.java", "package custom;",
                "public class Install extends org.openide.modules.ModuleInstall {",
                "public @Override void restored() {try {",
                "System.setProperty(\"my.file.count\", ",
                "Integer.toString(org.openide.modules.InstalledFileLocator.getDefault().locate(" +
                "\"some\", null, false).list().length));",
                "System.setProperty(\"my.file.length\", ",
                "Long.toString(new java.io.File(org.openide.modules.InstalledFileLocator.getDefault().locate(" +
                "\"some/stuff\", null, false).getParentFile(), \"otherstuff\").length()));",
                "System.setProperty(\"my.url.length\", ",
                "Integer.toString(new java.net.URL(\"nbinst://custom/some/stuff\").openConnection().getContentLength()));",
                "} catch (Exception x) {x.printStackTrace();}",
                "}",
                "}").
                sourceFile("OSGI-INF/files/some/stuff", "some text").
                sourceFile("OSGI-INF/files/some/otherstuff", "hello").
                manifest(
                "OpenIDE-Module: custom",
                "OpenIDE-Module-Install: custom.Install",
                "OpenIDE-Module-Module-Dependencies: org.openide.modules").done().
                module("org.netbeans.modules.masterfs").
                backwards(). // XXX will not pass otherwise
                run();
        assertEquals("2", System.getProperty("my.file.count"));
        assertEquals("6", System.getProperty("my.file.length"));
        assertEquals("10", System.getProperty("my.url.length"));
    }

}
