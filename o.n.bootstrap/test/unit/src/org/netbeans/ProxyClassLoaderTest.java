/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import junit.framework.TestCase;

public class ProxyClassLoaderTest extends TestCase {

    public ProxyClassLoaderTest(String name) {
        super(name);
    }

    public void testAmbiguousDelegation() throws Exception {
        class CL extends ProxyClassLoader {
            final Class[] owned;
            final String name;
            CL(ClassLoader[] parents, String name, Class... owned) {
                super(parents, false);
                addCoveredPackages(Collections.singleton("org.netbeans"));
                this.name = name;
                this.owned = owned;
            }
            protected @Override Class doLoadClass(String pkg, String name) {
                for (Class c : owned) {
                    if (name.equals(c.getName())) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream is = CL.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
                        byte[] buf = new byte[4096];
                        int read;
                        try {
                            while ((read = is.read(buf)) != -1) {
                                baos.write(buf, 0, read);
                            }
                        } catch (IOException x) {
                            assert false : x;
                        }
                        return defineClass(name, baos.toByteArray(), 0, baos.size());
                    }
                }
                return null;
            }
            protected @Override boolean shouldDelegateResource(String pkg, ClassLoader parent) {
                return parent != null || !pkg.equals("org/netbeans/");
            }
            public @Override String toString() {
                return name;
            }
        }
        ClassLoader l1 = new CL(new ClassLoader[0], "l1", A.class);
        ClassLoader l2 = new CL(new ClassLoader[0], "l2", A.class);
        ClassLoader l3 = new CL(new ClassLoader[] {l1}, "l3", B.class);
        ClassLoader l4 = new CL(new ClassLoader[] {l1, l2}, "l4", B.class);
        assertEquals(l1, l1.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l2, l2.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l1, l3.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l3, l3.loadClass(B.class.getName()).getClassLoader());
        assertEquals(l1, l3.loadClass(B.class.getName()).getMethod("a").invoke(null).getClass().getClassLoader());
        try {
            Class c = l4.loadClass(A.class.getName());
            fail("arbitrarily loaded A from " + c.getClassLoader());
        } catch (ClassNotFoundException x) {/* OK */}
        try {
            ClassLoader delegate = l4.loadClass(B.class.getName()).getMethod("a").invoke(null).getClass().getClassLoader();
            fail("arbitrarily returned A instance from " + delegate);
        } catch (LinkageError x) {/* OK */}
        ClassLoader l5 = new CL(new ClassLoader[] {l1, l3}, "l5", C.class);
        assertEquals(l1, l5.loadClass(A.class.getName()).getClassLoader());
        assertEquals(l3, l5.loadClass(B.class.getName()).getClassLoader());
        assertEquals(l5, l5.loadClass(C.class.getName()).getClassLoader());
        assertEquals(l1, l5.loadClass(C.class.getName()).getMethod("a").invoke(null).getClass().getClassLoader());
    }

    public static class A {}
    public static class B {
        public static A a() {
            return new A();
        }
    }
    public static class C {
        public static A a() {
            return new A();
        }
    }

}
