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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.junit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class NbModuleSuite extends NbTestSuite {
    private Class<?> clazz;

    public NbModuleSuite(Class<?> aClass) {
        super();
        clazz = aClass;
    }

    @Override
    public void run(TestResult result) {
        try {
            runInRuntimeContainer(result);
        } catch (Exception ex) {
            result.addError(this, ex);
        }
    }
    
    private void runInRuntimeContainer(TestResult result) throws Exception {
        File platform = findPlatform();
        File[] boot = new File(platform, "lib").listFiles();
        List<URL> bootCP = new ArrayList<URL>();
        for (int i = 0; i < boot.length; i++) {
            URL u = boot[i].toURL();
            if (u.toExternalForm().endsWith(".jar")) {
                bootCP.add(u);
            }
        }
        // loader that does not see our current classloader
        ClassLoader parent = ClassLoader.getSystemClassLoader().getParent();
        Assert.assertNotNull("Parent", parent);
        URLClassLoader loader = new URLClassLoader(bootCP.toArray(new URL[0]), parent);
        Class<?> main = loader.loadClass("org.netbeans.Main"); // NOI18N
        Assert.assertEquals("Loaded by our classloader", loader, main.getClassLoader());
        Method m = main.getDeclaredMethod("main", String[].class); // NOI18N
        
        System.setProperty("java.util.logging.config", "-");
        System.setProperty("netbeans.home", platform.getPath());
        
        File ud = new File(new File(Manager.getWorkDirPath()), "userdir");
        ud.mkdirs();
        NbTestCase.deleteSubFiles(ud);
        
        System.setProperty("netbeans.user", ud.getPath());
        
        List<String> args = new ArrayList<String>();
        args.add("--nosplash");
        m.invoke(null, (Object)args.toArray(new String[0]));
        
        ClassLoader global = Lookup.getDefault().lookup(ClassLoader.class);
        Assert.assertNotNull("Global classloader is initialized", global);
        
        URL[] testCP = preparePath(clazz);
        JunitLoader testLoader = new JunitLoader(testCP, global, NbTestSuite.class.getClassLoader());
        Class<?> sndClazz = testLoader.loadClass(clazz.getName());

        new NbTestSuite(sndClazz).run(result);
    }
    
    private URL[] preparePath(Class<?>... classes) {
        Collection<URL> cp = new LinkedHashSet<URL>();
        for (Class c : classes) {
            URL test = c.getProtectionDomain().getCodeSource().getLocation();
            Assert.assertNotNull("URL found for " + c, test);
            cp.add(test);
        }
        return cp.toArray(new URL[0]);
    }

    
    private File findPlatform() {
        try {
            File util = new File(Lookup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Assert.assertTrue("Util exists: " + util, util.exists());

            return util.getParentFile().getParentFile();
        } catch (URISyntaxException ex) {
            Assert.fail("Cannot find utilities JAR");
            return null;
        }
    }
    
    private static final class JunitLoader extends URLClassLoader {
        private final ClassLoader junit;

        public JunitLoader(URL[] urls, ClassLoader parent, ClassLoader junit) {
            super(urls, parent);
            this.junit = junit;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (isUnit(name)) {
                return junit.loadClass(name);
            }
            return super.findClass(name);
        }

        @Override
        public URL findResource(String name) {
            if (isUnit(name)) {
                return junit.getResource(name);
            }
            return super.findResource(name);
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            if (isUnit(name)) {
                return junit.getResources(name);
            }
            return super.findResources(name);
        }
        
        private final boolean isUnit(String res) {
            if (res.startsWith("junit")) {
                return true;
            }
            if (res.startsWith("org.junit") || res.startsWith("org/junit")) {
                return true;
            }
            if (res.startsWith("org.netbeans.junit") || res.startsWith("org/netbeans/junit")) {
                return true;
            }
            return false;
        }
    }
}
