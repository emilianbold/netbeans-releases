/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.net.URLClassLoader;
import org.openide.util.test.AnnotationProcessorTestUtils;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbBundle.Messages;
import org.openide.util.test.TestFileUtils;
import static org.netbeans.modules.openide.util.Bundle.*;

@Messages("k3=value #3")
public class NbBundleProcessorTest extends NbTestCase {

    public NbBundleProcessorTest(String n) {
        super(n);
    }

    private File src;
    private File dest;
    protected @Override void setUp() throws Exception {
        clearWorkDir();
        src = new File(getWorkDir(), "src");
        dest = new File(getWorkDir(), "classes");
    }

    @Messages({
        "k1=value #1",
        "k2=value #2"
    })
    public void testBasicUsage() throws Exception {
        assertEquals("value #1", k1());
        assertEquals("value #2", k2());
        assertEquals("value #3", k3());
    }

    @Messages({
        "f1=problem with {0}",
        "# {0} - input file",
        "# {1} - pattern",
        "f2={0} did not match {1}",
        "LBL_BuildMainProjectAction_Name=&Build {0,choice,-1#Main Project|0#Project|1#Project ({1})|1<{0} Projects}"
    })
    public void testMessageFormats() throws Exception {
        assertEquals("problem with stuff", f1("stuff"));
        assertEquals("1 did not match 2", f2(1, 2));
        assertEquals("&Build Main Project", LBL_BuildMainProjectAction_Name(-1, "whatever"));
        assertEquals("&Build Project", LBL_BuildMainProjectAction_Name(0, "whatever"));
        assertEquals("&Build Project (whatever)", LBL_BuildMainProjectAction_Name(1, "whatever"));
        assertEquals("&Build 2 Projects", LBL_BuildMainProjectAction_Name(2, "whatever"));
    }

    @Messages({
        "s1=Don't worry",
        "s2=Don''t worry about {0}",
        "s3=@camera Say \"cheese\"",
        "s4=<bra&ket>",
        "s5=Operators: +-*/=",
        "s6=One thing.\nAnd another."
    })
    public void testSpecialCharacters() throws Exception {
        assertEquals("Don't worry", s1());
        assertEquals("Don't worry about me", s2("me"));
        assertEquals("@camera Say \"cheese\"", s3());
        assertEquals("<bra&ket>", s4());
        assertEquals("Operators: +-*/=", s5());
        assertEquals("One thing.\nAnd another.", s6());
    }

    @Messages({
        "some key=some value",
        "public=property",
        "2+2=4"
    })
    public void testNonIdentifierKeys() throws Exception {
        assertEquals("some value", some_key());
        assertEquals("property", _public());
        assertEquals("4", _2_2());
    }

    public void testPackageKeys() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.package-info", "@org.openide.util.NbBundle.Messages(\"k=v\")", "package p;");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {dest.toURI().toURL()});
        Method m = l.loadClass("p.Bundle").getDeclaredMethod("k");
        m.setAccessible(true);
        assertEquals("v", m.invoke(null));
    }

    public void testDupeErrorSimple() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages({\"k=v1\", \"k=v2\"})", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testDupeErrorByIdentifier() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages({\"k.=v1\", \"k,=v2\"})", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testDupeErrorAcrossClasses() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages({\"k=v\"})", "class C1 {}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages({\"k=v\"})", "class C2 {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
        assertTrue(err.toString(), err.toString().contains("C1.java"));
        assertTrue(err.toString(), err.toString().contains("C2.java"));
    }

    public void testDupeErrorAcrossClassesIncremental() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages({\"k=v1\"})", "class C1 {}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages({\"k=v2\"})", "class C2 {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, "C2.java", dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testNoEqualsError() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"whatever\")", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("="));
    }

    public void testWhitespaceError() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"key = value\")", "class C {}");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("="));
    }

    @Messages({
        "# {0} - in use", "# {1} - not in use", "unused_param_1=please remember {0}",
        "# {0} - not in use", "# {1} - in use", "unused_param_2=I will remember {1}"
    })
    public void testNonexistentParameter() throws Exception {
        assertEquals("please remember me", unused_param_1("me", "you"));
        assertEquals("I will remember you", unused_param_2("me", "you"));
    }

    public void testExistingBundle() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"k=v\")", "class C {}");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "# original comment\nold=stuff\n");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("k=v\n# original comment\nold=stuff\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
        // Also check that we can recompile:
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("k=v\n# original comment\nold=stuff\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
    }

    public void testDupeErrorWithExistingBundle() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages(\"k=v\")", "class C {}");
        TestFileUtils.writeFile(new File(src, "p/Bundle.properties"), "k=v\n");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertFalse(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, err));
        assertTrue(err.toString(), err.toString().contains("uplicate"));
    }

    public void testIncrementalCompilation() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v1\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages(\"k2=v2\")", "public class C2 {public @Override String toString() {return Bundle.k2();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {dest.toURI().toURL()});
        assertEquals("v1", l.loadClass("p.C1").newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").newInstance().toString());
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v3\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, null));
        l = new URLClassLoader(new URL[] {dest.toURI().toURL()});
        assertEquals("v3", l.loadClass("p.C1").newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").newInstance().toString());
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k3=v4\")", "public class C1 {public @Override String toString() {return Bundle.k3();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, null));
        l = new URLClassLoader(new URL[] {dest.toURI().toURL()});
        assertEquals("v4", l.loadClass("p.C1").newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").newInstance().toString());
    }

    public void testIncrementalCompilationWithBrokenClassFiles() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v1\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C2", "@org.openide.util.NbBundle.Messages(\"k2=v2\")", "public class C2 {public @Override String toString() {return Bundle.k2();}}");
        AnnotationProcessorTestUtils.makeSource(src, "p.C3", "class C3 {C3() {new Runnable() {public @Override void run() {new Runnable() {public @Override void run() {}};}};}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        ClassLoader l = new URLClassLoader(new URL[] {dest.toURI().toURL()});
        assertEquals("v1", l.loadClass("p.C1").newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").newInstance().toString());
        assertTrue(new File(dest, "p/C3.class").delete());
        assertTrue(new File(dest, "p/C3$1.class").delete());
        assertTrue(new File(dest, "p/C3$1$1.class").isFile());
        AnnotationProcessorTestUtils.makeSource(src, "p.C1", "@org.openide.util.NbBundle.Messages(\"k1=v3\")", "public class C1 {public @Override String toString() {return Bundle.k1();}}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, "C1.java", dest, null, null));
        l = new URLClassLoader(new URL[] {dest.toURI().toURL()});
        assertEquals("v3", l.loadClass("p.C1").newInstance().toString());
        assertEquals("v2", l.loadClass("p.C2").newInstance().toString());
    }

    public void testComments() throws Exception {
        AnnotationProcessorTestUtils.makeSource(src, "p.C", "@org.openide.util.NbBundle.Messages({\"# Something good to note.\", \"k=v\"})", "class C {}");
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("# Something good to note.\nk=v\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
        // Also check that we can recompile:
        assertTrue(AnnotationProcessorTestUtils.runJavac(src, null, dest, null, null));
        assertEquals("# Something good to note.\nk=v\n", TestFileUtils.readFile(new File(dest, "p/Bundle.properties")).replace("\r\n", "\n"));
        // XXX also check non-ASCII chars in comments; works locally but fails on deadlock
    }

}
