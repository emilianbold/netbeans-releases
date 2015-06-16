/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.classfile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Tomas Zezula
 */
public class ModuleTest extends TestCase {
    private static final String JDK9_HOME = null;
    private static final Logger LOG = Logger.getLogger(ModuleTest.class.getName());

    public void testJavaBaseModule() throws IOException {
        doTest("java.base");    //NOI18N
    }

    public void testJavaDesktopModule() throws IOException {
        doTest("java.desktop");    //NOI18N
    }

    private void doTest(String moduleName) throws IOException {
        System.out.println(moduleName);
        final Path modulesRoot = getModulesRoot();
        if (modulesRoot != null) {
            final Path javaBase = modulesRoot.resolve(String.format("%s/module-info.class", moduleName));   //NOI18N
            assertTrue(Files.exists(javaBase));
            try (InputStream in = Files.newInputStream(javaBase)) {
                final ClassFile cf = new ClassFile(in, true);
                assertNotNull(cf);
                final Module mod = cf.getModule();
                assertNotNull(mod);

                for (Module.RequiresEntry req : mod.getRequiresEntries()) {
                    System.out.printf("%s%n", req);
                }
                for (Module.ExportsEntry exp : mod.getExportsEntries()) {
                    System.out.printf("%s%n", exp);
                }
                for (CPClassInfo u : mod.getUses()) {
                    System.out.printf("uses: %s%n", u.getClassName());
                }
                for (Module.ProvidesEntry p : mod.getProvidesEntries()) {
                    System.out.printf("%s%n", p);
                }
            }
        }
    }

    private static Path getModulesRoot() {
        try {
            final File javaHome = new File(JDK9_HOME == null ? System.getProperty("java.home") : JDK9_HOME);    //NOI18N
            final File jrtProvider = new File(javaHome, "jrt-fs.jar");  //NOI18N
            if (!jrtProvider.exists()) {
                return null;
            }
            final ClassLoader cl = new URLClassLoader(
                    new URL[]{jrtProvider.toURI().toURL()},
                    ModuleTest.class.getClassLoader());
            FileSystemProvider provider = null;
            for (FileSystemProvider p : ServiceLoader.load(FileSystemProvider.class, cl)) {
                if ("jrt".equals(p.getScheme())) {  //NOI18N
                    provider = p;
                    break;
                }
            }
            if (provider == null) {
                return null;
            }
            final Path jimageRoot = provider.getPath(URI.create("jrt:///"));    //NOI18N
            final Path modules = jimageRoot.resolve("modules");
            return Files.exists(modules) ? modules : jimageRoot;
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, "Cannot load jrt nio provider.", ioe);   //NOI18N
            return null;
        }
    }
}
