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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans;

import junit.framework.AssertionFailedError;
import junit.textui.TestRunner;
import org.netbeans.junit.*;
import java.io.InputStream;
import java.lang.reflect.*;
import java.util.*;

/** Test patching of openide.jar byte code for compatibility.
 * @author Jaroslav Tulach
 */
public class PatchByteCodeTest extends NbTestCase {

    public PatchByteCodeTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PatchByteCodeTest.class));
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testBeanTreeViewLoad () throws Exception {
        checkPatching (
            "org.openide.explorer.view.BeanTreeView", 
            "data/BeanTreeView.clazz",
            null, null
        , null);
    }

    /* XXX This module is obsolete. Does the test do anything else useful?
    public void testCompilerGroupLoad () throws Exception {
        checkPatching (
            "org.openide.compiler.CompilerGroup", 
            "data/CompilerGroup.clazz",
            null, null
        );
        
        InputStream is = getClass ().getResourceAsStream ("data/CompilerGroup.clazz");
        assertNotNull ("Class has not been found", is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);
        

        HashMap args = new HashMap ();
        args.put ("netbeans.public", Arrays.asList(new String[] { "addCompilerListener", "removeCompilerListener" }) );
        byte[] res = PatchByteCode.enhanceClass(arr, args);
        PatchClassLoader loader = new PatchClassLoader ("org.openide.compiler.CompilerGroup", res, ClassLoader.getSystemClassLoader());
        
        Class c = loader.loadClass ("org.openide.compiler.CompilerGroup");
        
        Method m = c.getDeclaredMethod("addCompilerListener", new Class[] { org.openide.compiler.CompilerListener.class });
        assertTrue ("Is not final", !Modifier.isFinal (m.getModifiers ()));
        
        m = c.getDeclaredMethod("removeCompilerListener", new Class[] { org.openide.compiler.CompilerListener.class });
        assertTrue ("Is not final", !Modifier.isFinal (m.getModifiers ()));
    }
    */
    
    public void testClassCanBeAlsoInstantiated () throws Exception {
        Class c = checkPatching (
            Sample.class.getName (),
            "Sample.class",
            "java.lang.Throwable", 
            null,
            null
        );
        
        c.newInstance ();
    }

    public void testConstructorCanBeAlsoInstantiated () throws Exception {
        Class c = checkPatching (
            Sample2.class.getName (),
            "Sample2.class",
            "java.lang.Throwable", 
            null,
            new String[] { "<init>" }
        );
        
        c.newInstance ();
    }
    
    public void testChangingSetOfSuperInterfaces () throws Exception {
        Class c = checkPatching (
            Sample.class.getName (),
            "Sample.class",
            "java.lang.Throwable", 
            new String[] { "org.openide.nodes.Node$Cookie", "java.lang.Cloneable" }
        , null);
        
        assertEquals ("Super class is throwable", Throwable.class, c.getSuperclass());
        Class[] ifaces = c.getInterfaces();
        assertEquals ("Two of them", 2, ifaces.length);

        
        Object obj = c.newInstance ();
        assertTrue ("Is instance of Cookie", obj instanceof org.openide.nodes.Node.Cookie);
        assertTrue ("Is instance of Cloneable", obj instanceof Cloneable);
    }
    
    
    public void testPatchingOfFieldsAndMethodsToPublicAndNonFinal () throws Exception {
        InputStream is = getClass ().getResourceAsStream ("Sample.class");
        assertNotNull ("Class has not been found", is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);

        
        
        HashMap args = new HashMap ();
        args.put ("netbeans.public", Arrays.asList(new String[] { "member", "field", "method", "staticmethod" }) );
        byte[] res = PatchByteCode.enhanceClass(arr, args);
        PatchClassLoader loader = new PatchClassLoader (Sample.class.getName (), res, ClassLoader.getSystemClassLoader());
        
        Class c = loader.loadClass (Sample.class.getName ());

        assertTrue ("Class should be public", Modifier.isPublic (c.getModifiers()));

        Method m = c.getDeclaredMethod("method", new Class[0]);
        assertNotNull ("Mehtod method is there", m);
        assertTrue ("And is public", Modifier.isPublic (m.getModifiers()));
        assertTrue ("And is not final", !Modifier.isFinal(m.getModifiers ()));
        assertTrue ("And is not static", !Modifier.isStatic(m.getModifiers()));
        assertTrue ("And is not synchronzied", !Modifier.isSynchronized(m.getModifiers()));
        
        m = c.getDeclaredMethod("member", new Class[] { Object.class });
        assertNotNull ("Member method is there", m);
        assertTrue ("And is public", Modifier.isPublic (m.getModifiers()));        
        assertTrue ("And is not final", !Modifier.isFinal(m.getModifiers ()));
        assertTrue ("And is not static", !Modifier.isStatic(m.getModifiers()));
        assertTrue ("And is synchronzied", Modifier.isSynchronized(m.getModifiers()));
        
        m = c.getDeclaredMethod("staticmethod", new Class[] { });
        assertNotNull ("Member method is there", m);
        assertTrue ("And is public", Modifier.isPublic (m.getModifiers()));        
        assertTrue ("And is not final", !Modifier.isFinal(m.getModifiers ()));
        assertTrue ("And is not static", Modifier.isStatic(m.getModifiers()));
        assertTrue ("And is not synchronzied", !Modifier.isSynchronized(m.getModifiers()));
        
        java.lang.reflect.Field f;
        
        f = c.getDeclaredField("member");
        assertNotNull ("Really exists", f);
        assertTrue ("Is public", Modifier.isPublic (f.getModifiers ()));
        assertTrue ("Is not final", !Modifier.isFinal (f.getModifiers ()));
        assertTrue ("Is static", Modifier.isStatic (f.getModifiers ()));
        
        f = c.getDeclaredField("field");
        assertNotNull ("Really exists", f);
        assertTrue ("Is public", Modifier.isPublic (f.getModifiers ()));
        assertTrue ("Is not final", !Modifier.isFinal (f.getModifiers ()));
        assertTrue ("Is static", !Modifier.isStatic (f.getModifiers ()));
        
    }

    public void testRenameOfAMember () throws Exception {
        InputStream is = getClass ().getResourceAsStream ("Sample.class");
        assertNotNull ("Class has not been found", is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);

        
        
        HashMap args = new HashMap ();
        args.put ("netbeans.rename", Arrays.asList(new String[] { "staticmethod", "StaticMethod" }) );
        byte[] res = PatchByteCode.enhanceClass(arr, args);

        PatchClassLoader loader = new PatchClassLoader (Sample.class.getName (), res, ClassLoader.getSystemClassLoader());
        
        Class c = loader.loadClass (Sample.class.getName ());

        try {
            c.getDeclaredMethod("staticmethod", new Class[] { });
            fail ("The old method is still present");
        } catch (NoSuchMethodException ex) {
            // ok, should not be there
        }
        
        java.lang.reflect.Method m = c.getDeclaredMethod("StaticMethod", new Class[] { });
        assertNotNull ("Renamed method found", m);
    }
        
    private Class checkPatching (
        String className, 
        String resource, 
        String superclass, 
        String[] interfaces, 
        String[] publc
    ) throws Exception {
        if (superclass == null) {
            superclass = PatchByteCodeTest.class.getName ();
        }
        
        InputStream is = getClass ().getResourceAsStream (resource);
        assertNotNull ("Resource has been found " + resource, is);
        
        byte[] arr = new byte[is.available ()];
        int l = is.read (arr);
        assertEquals ("Read exactly as much as expected", l, arr.length);
        
        HashMap args = new HashMap ();
        args.put ("netbeans.superclass", superclass.replace ('.', '/'));
        
        if (interfaces != null) {
            StringBuffer sb = new StringBuffer ();
            String ap = "";
            for (int i = 0; i < interfaces.length; i++) {
                sb.append (ap);
                sb.append (interfaces[i]);
                ap = ",";
            }
            args.put ("netbeans.interfaces", sb.toString().replace('.', '/'));
        }
        
        if (publc != null) {
            args.put ("netbeans.public", Arrays.asList(publc));
        }
        
        byte[] res = PatchByteCode.enhanceClass(arr, args);
        PatchClassLoader loader = new PatchClassLoader (className, res);

        Class c = loader.loadClass (className);
        
        assertEquals (
            "Superclass changed appropriately", 
            superclass,
            c.getSuperclass().getName ()
        );
        
        return c;
    }        
    
    
    private static final class PatchClassLoader extends ClassLoader {
        private String res;
        private byte[] arr;
        
        public PatchClassLoader (String res, byte[] arr) {
            this (res, arr, PatchClassLoader.class.getClassLoader ());
        }
        
        public PatchClassLoader (String res, byte[] arr, ClassLoader c) {
            super (c);
            
            this.res = res;
            this.arr = arr;
        }
        
        protected synchronized Class loadClass(String name, boolean resolve) 
        throws ClassNotFoundException {
            if (res.equals (name)) {
                byte[] patch = PatchByteCode.patch(arr, name);
                
                return defineClass (name, patch, 0, patch.length);
            } else {
                return super.loadClass (name, resolve);
            }
        }
    }
}
