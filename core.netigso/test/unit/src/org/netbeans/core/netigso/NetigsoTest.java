/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.netigso;

import org.netbeans.core.startup.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.JarClassLoader;
import org.netbeans.Module;
import org.netbeans.ModuleFactory;
import org.netbeans.ModuleManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 * Basic tests to verify the basic interaction between NetBeans module
 * system and OSGi.
 *
 * @author Jaroslav Tulach
 */
public class NetigsoTest extends SetupHid {

    static {
        MockLookup.setInstances(new MyModuleFactory());
    }

    public NetigsoTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        Locale.setDefault(Locale.US);
        clearWorkDir();
        data = new File(getDataDir(), "jars");
        jars = new File(getWorkDir(), "jars");
        jars.mkdirs();
        File simpleModule = createTestJAR("simple-module", null);
        File dependsOnSimpleModule = createTestJAR("depends-on-simple-module", null, simpleModule);

        File ud = new File(getWorkDir(), "ud");
        ud.mkdirs();

        System.setProperty("netbeans.user", ud.getPath());
    }
    private File createTestJAR(String name, String srcdir, File... classpath) throws IOException {
        return createTestJAR(data, jars, name, srcdir, classpath);
    }

    public static int numberOfStandard = 0;
    public static int numberOfFixed = 0;
    public static boolean testingParentClassloaders = false;
    public static boolean testingDummyModule = false;
    
    public void testFactoryCreatesOurModules() throws Exception {
        // clear the counters before the test!
        numberOfStandard = 0;
        numberOfFixed = 0;
        
        FakeModuleInstaller installer = new FakeModuleInstaller();
        FakeEvents ev = new FakeEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        try {
            String mf = "Bundle-SymbolicName: org.foo\n" +
                "Bundle-Version: 1.1.0\n" +
                "Export-Package: org.foo";

            File j1 = changeManifest(new File(jars, "simple-module.jar"), mf);
            File j2 = new File(jars, "depends-on-simple-module.jar");
            File j3 = new File(jars, "dep-on-two-modules.jar");
            Module m1 = mgr.create(j1, null, false, false, false);
            Module m2 = mgr.create(j2, null, false, false, false);
            Module m3 = mgr.create(j3, null, false, false, false);
            mgr.enable(new HashSet<Module>(Arrays.asList(m1, m2, m3)));
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }
        assertEquals("Number of standard modules created by our factory ", 1, numberOfStandard);
        assertEquals("Number of fixed modules created by our factory ", 2, numberOfFixed);
    }

    
    private File changeManifest(File orig, String manifest) throws IOException {
        File f = new File(getWorkDir(), orig.getName());
        Manifest mf = new Manifest(new ByteArrayInputStream(manifest.getBytes("utf-8")));
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f), mf);
        JarFile jf = new JarFile(orig);
        Enumeration<JarEntry> en = jf.entries();
        InputStream is;
        while (en.hasMoreElements()) {
            JarEntry e = en.nextElement();
            if (e.getName().equals("META-INF/MANIFEST.MF")) {
                continue;
            }
            os.putNextEntry(e);
            is = jf.getInputStream(e);
            FileUtil.copy(is, os);
            is.close();
            os.closeEntry();
        }
        os.close();

        return f;
    }
    
    private static final class MyModuleFactory extends ModuleFactory {
        public @Override Module create(File jar, Object history, boolean reloadable, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws IOException {
            if (testingDummyModule || testingParentClassloaders) {
                return new DummyModule(mgr, ev, history, reloadable, autoload, eager);
            }
            numberOfStandard++;
            try {
                return super.create(jar, history, reloadable, autoload, eager, mgr, ev);
            } catch (InvalidException ex) {
                Manifest mani = ex.getManifest();
                if (mani != null) {
                    String name = mani.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
                    if (name == null) {
                        fail("Shall have OSGi manifest: ");
                    }
                    fail("Success OSGi!: " + name);
                    return null;
                }
                throw ex;
            }
        }
        
        public @Override Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager, ModuleManager mgr, Events ev) throws InvalidException {
            numberOfFixed++;
            return super.createFixed(mani, history, loader, autoload, eager, mgr, ev);
        }
        
        public @Override boolean removeBaseClassLoader() {
            if (testingParentClassloaders) {
                return true;
            }
            return super.removeBaseClassLoader();
        }
        public @Override ClassLoader getClasspathDelegateClassLoader(ModuleManager mgr, ClassLoader del) {
            if (testingParentClassloaders) {
                return new NoOpClassLoader();
            }
            return del;
        }
    }
    
    private static final class DummyModule extends Module {
        private final Manifest manifest;
        public DummyModule(ModuleManager mgr, Events ev, Object history, boolean reloadable, boolean autoload, boolean eager) throws IOException {
            super(mgr, ev, history, reloadable, autoload, eager);
            manifest = new Manifest();
            manifest.getMainAttributes().putValue("OpenIDE-Module", "boom");
            parseManifest();
        }
        @Override
        public List<File> getAllJars() {
            return Collections.emptyList();
        }
        @Override
        public void setReloadable(boolean r) {
        }
        @Override
        public void reload() throws IOException {
        }
        @Override
        protected void classLoaderUp(Set parents) throws IOException {
            classloader = new JarClassLoader(Collections.<File>emptyList(), new ClassLoader[] {new NoOpClassLoader()});
        }
        @Override
        protected void classLoaderDown() {
        }
        @Override
        protected void cleanup() {
        }
        @Override
        protected void destroy() {
        }
        @Override
        public boolean isFixed() {
            return true;
        }
        @Override
        public Object getLocalizedAttribute(String attr) {
            return null;
        }
        public @Override Manifest getManifest() {
            return manifest;
        }
    }
    
    private static final class NoOpClassLoader extends ClassLoader {
        NoOpClassLoader() {
	    super(ClassLoader.getSystemClassLoader());
	}
        protected @Override Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if ("java.lang.String".equals(name)) {
                throw new ClassNotFoundException("NoOpClassLoader cannot load " + name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
