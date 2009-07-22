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

package org.netbeans.spi.project.support.ant;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestResult;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import org.openide.util.test.MockChangeListener;
import org.openide.util.test.MockPropertyChangeListener;

/**
 * Test functionality of PropertyUtils.
 * @author Jesse Glick
 */
public class PropertyUtilsTest extends NbTestCase {
    
    public PropertyUtilsTest(String name) {
        super(name);
    }
    
    public void run(final TestResult result) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                PropertyUtilsTest.super.run(result);
                return null;
            }
        });
    }
    
    private static PropertyEvaluator evaluator(Map<String,String> predefs, List<Map<String,String>> defs) {
        PropertyProvider[] mainProviders = new PropertyProvider[defs.size()];
        int i = 0;
        for (Map<String,String> def : defs) {
            mainProviders[i++] = PropertyUtils.fixedPropertyProvider(def);
        }
        return PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.fixedPropertyProvider(predefs), mainProviders);
    }
    
    private static String evaluate(String prop, Map<String,String> predefs, List<Map<String,String>> defs) {
        return evaluator(predefs, defs).getProperty(prop);
    }
    
    private static Map<String,String> evaluateAll(Map<String,String> predefs, List<Map<String,String>> defs) {
        return evaluator(predefs, defs).getProperties();
    }
    
    private static String evaluateString(String text, Map<String,String> predefs, List<Map<String,String>> defs) {
        return evaluator(predefs, defs).evaluate(text);
    }
    
    public void testEvaluate() throws Exception {
        // XXX check override order, property name evaluation, $$ escaping, bare or final $,
        // cyclic errors, undef'd property substitution, no substs in predefs, etc.
        Map<String,String> m1 = Collections.singletonMap("y", "val");
        Map<String,String> m2 = new HashMap<String,String>();
        m2.put("x", "${y}");
        m2.put("y", "y-${x}");
        List<Map<String,String>> m1m2 = new ArrayList<Map<String,String>>();
        m1m2.add(m1);
        m1m2.add(m2);
        assertEquals("x evaluates to former y", "val", evaluate("x", Collections.<String,String>emptyMap(), m1m2));
        assertEquals("first y defines it", "val", evaluate("y", Collections.<String,String>emptyMap(), m1m2));
        assertEquals("circularity error", null, evaluate("x", Collections.<String,String>emptyMap(), Collections.singletonList(m2)));
        assertEquals("circularity error", null, evaluate("y", Collections.<String,String>emptyMap(), Collections.singletonList(m2)));
        m2.clear();
        m2.put("y", "yval_${z}");
        m2.put("x", "xval_${y}");
        m2.put("z", "zval");
        Map<String,String> all = evaluateAll(Collections.<String,String>emptyMap(), Collections.singletonList(m2));
        assertNotNull("no circularity error", all);
        assertEquals("have three properties", 3, all.size());
        assertEquals("double substitution", "xval_yval_zval", all.get("x"));
        assertEquals("single substitution", "yval_zval", all.get("y"));
        assertEquals("no substitution", "zval", all.get("z"));
        // Yuck. But it failed once, so check it now.
        Properties p = new Properties();
        p.load(new ByteArrayInputStream("project.mylib=../mylib\njavac.classpath=${project.mylib}/build/mylib.jar\nrun.classpath=${javac.classpath}:build/classes".getBytes("US-ASCII")));
        all = evaluateAll(Collections.<String,String>emptyMap(), Collections.singletonList(NbCollections.checkedMapByFilter(p, String.class, String.class, true)));
        assertNotNull("no circularity error", all);
        assertEquals("javac.classpath correctly substituted", "../mylib/build/mylib.jar", all.get("javac.classpath"));
        assertEquals("run.classpath correctly substituted", "../mylib/build/mylib.jar:build/classes", all.get("run.classpath"));
    }
    
    public void testTokenizePath() throws Exception {
        assertEquals("basic tokenization works on ':'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("foo:bar")));
            assertEquals("basic tokenization works on ';'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("foo;bar")));
            assertEquals("Unix paths work",
                Arrays.asList(new String[] {"/foo/bar", "baz/quux"}),
                Arrays.asList(PropertyUtils.tokenizePath("/foo/bar:baz/quux")));
            assertEquals("empty components are stripped with ':'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath(":foo::bar:")));
            assertEquals("empty components are stripped with ';'",
                Arrays.asList(new String[] {"foo", "bar"}),
                Arrays.asList(PropertyUtils.tokenizePath(";foo;;bar;")));
            assertEquals("DOS paths are recognized with ';'",
                Arrays.asList(new String[] {"c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:\\foo;D:\\\\bar")));
            assertEquals("DOS paths are recognized with ':'",
                Arrays.asList(new String[] {"c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:\\foo:D:\\\\bar")));
            assertEquals("a..z can be drive letters",
                Arrays.asList(new String[] {"a:\\foo", "z:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("a:\\foo:z:\\\\bar")));
            assertEquals("A..Z can be drive letters",
                Arrays.asList(new String[] {"A:\\foo", "Z:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("A:\\foo:Z:\\\\bar")));
            assertEquals("non-letters are not drives with ';'",
                Arrays.asList(new String[] {"1", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("1;\\foo;D:\\\\bar")));
            assertEquals("non-letters are not drives with ':'",
                Arrays.asList(new String[] {"1", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("1:\\foo:D:\\\\bar")));
            assertEquals(">1 letters are not drives with ';'",
                Arrays.asList(new String[] {"ab", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("ab;\\foo;D:\\\\bar")));
            assertEquals(">1 letters are not drives with ':'",
                Arrays.asList(new String[] {"ab", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("ab:\\foo:D:\\\\bar")));
            assertEquals("drives use ':'",
                Arrays.asList(new String[] {"c", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c;\\foo;D:\\\\bar")));
            assertEquals("drives use only one ':'",
                Arrays.asList(new String[] {"c", "\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c::\\foo;D:\\\\bar")));
            assertEquals("drives use only one drive letter",
                Arrays.asList(new String[] {"c", "c:\\foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:c:\\foo;D:\\\\bar")));
            assertEquals("DOS paths start with '\\'",
                Arrays.asList(new String[] {"c", "foo", "D:\\\\bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:foo;D:\\\\bar")));
            assertEquals("DOS paths start with '/'",
                Arrays.asList(new String[] {"c", "/foo", "D:/bar", "/path"}),
                Arrays.asList(PropertyUtils.tokenizePath("c;/foo;D:/bar:/path")));
            assertEquals("empty path handled",
                Collections.EMPTY_LIST,
                Arrays.asList(PropertyUtils.tokenizePath("")));
            assertEquals("effectively empty path handled",
                Collections.EMPTY_LIST,
                Arrays.asList(PropertyUtils.tokenizePath(":;:;")));
            assertEquals("one letter directories handled",
                Arrays.asList(new String[] {"c:/foo/c", "/foo/c/bar", "c", "/foo/c", "/bar"}),
                Arrays.asList(PropertyUtils.tokenizePath("c:/foo/c;/foo/c/bar;c;/foo/c:/bar")));
            assertEquals("one letter directories handled2",
                Arrays.asList(new String[] {"c"}),
                Arrays.asList(PropertyUtils.tokenizePath("c")));
    }
    
    public void testRelativizeFile() throws Exception {
        clearWorkDir();
        File tmp = getWorkDir();
        File d1 = new File(tmp, "d1");
        File d1f = new File(d1, "f");
        File d1s = new File(d1, "s p a c e");
        File d1sf = new File(d1s, "f");
        File d2 = new File(tmp, "d2");
        File d2f = new File(d2, "f");
        // Note that "/tmp/d11".startsWith("/tmp/d1"), hence this being interesting:
        File d11 = new File(tmp, "d11");
        // Note: none of these dirs/files exist yet.
        assertEquals("d1f from d1", "f", PropertyUtils.relativizeFile(d1, d1f));
        assertEquals("d1 from d1f", "..", PropertyUtils.relativizeFile(d1f, d1)); // #61687
        assertEquals("d2f from d1", "../d2/f", PropertyUtils.relativizeFile(d1, d2f));
        assertEquals("d1 from d1", ".", PropertyUtils.relativizeFile(d1, d1));
        assertEquals("d2 from d1", "../d2", PropertyUtils.relativizeFile(d1, d2));
        assertEquals("d1s from d1", "s p a c e", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("d1sf from d1", "s p a c e/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // Now make them and check that the results are the same.
        assertTrue("made d1s", d1s.mkdirs());
        assertTrue("made d1f", d1f.createNewFile());
        assertTrue("made d1sf", d1sf.createNewFile());
        assertTrue("made d2", d2.mkdirs());
        assertTrue("made d2f", d2f.createNewFile());
        assertEquals("existing d1f from d1", "f", PropertyUtils.relativizeFile(d1, d1f));
        assertEquals("existing d2f from d1", "../d2/f", PropertyUtils.relativizeFile(d1, d2f));
        assertEquals("existing d1 from d1", ".", PropertyUtils.relativizeFile(d1, d1));
        assertEquals("existing d2 from d1", "../d2", PropertyUtils.relativizeFile(d1, d2));
        assertEquals("existing d1s from d1", "s p a c e", PropertyUtils.relativizeFile(d1, d1s));
        assertEquals("existing d1sf from d1", "s p a c e/f", PropertyUtils.relativizeFile(d1, d1sf));
        assertEquals("existing d11 from d1", "../d11", PropertyUtils.relativizeFile(d1, d11));
        // XXX: the below code should pass on Unix too I guess.
        if (Utilities.isWindows()) {
            // test Windows drives:
            File f1 = new File("C:\\folder\\one");
            File f2 = new File("D:\\t e m p\\two");
            assertNull("different drives cannot be relative", PropertyUtils.relativizeFile(f1, f2));
            f1 = new File("D:\\folder\\one");
            f2 = new File("D:\\t e m p\\two");
            assertEquals("relativization failed for Windows absolute paths", "../../t e m p/two", PropertyUtils.relativizeFile(f1, f2));
        }
    }
    
    public void testGlobalProperties() throws Exception {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        File ubp = new File(getWorkDir(), "build.properties");
        assertFalse("no build.properties yet", ubp.exists());
        assertEquals("no properties to start", Collections.EMPTY_MAP, PropertyUtils.getGlobalProperties());
        EditableProperties p = new EditableProperties(false);
        p.setProperty("key1", "val1");
        p.setProperty("key2", "val2");
        PropertyUtils.putGlobalProperties(p);
        assertTrue("now have build.properties", ubp.isFile());
        p = PropertyUtils.getGlobalProperties();
        assertEquals("two definitions now", 2, p.size());
        assertEquals("key1 correct", "val1", p.getProperty("key1"));
        assertEquals("key2 correct", "val2", p.getProperty("key2"));
        Properties p2 = new Properties();
        InputStream is = new FileInputStream(ubp);
        try {
            p2.load(is);
        } finally {
            is.close();
        }
        assertEquals("two definitions now from disk", 2, p2.size());
        assertEquals("key1 correct from disk", "val1", p2.getProperty("key1"));
        assertEquals("key2 correct from disk", "val2", p2.getProperty("key2"));
        // Test the property provider too.
        PropertyProvider gpp = PropertyUtils.globalPropertyProvider();
        MockChangeListener l = new MockChangeListener();
        gpp.addChangeListener(l);
        p = PropertyUtils.getGlobalProperties();
        assertEquals("correct initial definitions", p, gpp.getProperties());
        p.setProperty("key3", "val3");
        assertEquals("still have 2 defs", 2, gpp.getProperties().size());
        l.assertNoEvents();
        PropertyUtils.putGlobalProperties(p);
        l.assertEvent();
        assertEquals("now have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val", "val3", gpp.getProperties().get("key3"));
        l.msg("no spurious changes").assertNoEvents();
        // Test changes made using Filesystems API.
        p.setProperty("key1", "val1a");
        FileObject fo = FileUtil.toFileObject(ubp);
        assertNotNull("there is USER_BUILD_PROPERTIES on disk", fo);
        FileLock lock = fo.lock();
        OutputStream os = fo.getOutputStream(lock);
        p.store(os);
        os.close();
        lock.releaseLock();
        l.msg("got a change from the Filesystems API").assertEvent();
        assertEquals("still have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val for key1", "val1a", gpp.getProperties().get("key1"));
        // XXX changes made on disk are not picked up... bad test, or something else?
        /*
        Thread.sleep(1000);
        p.setProperty("key2", "val2a");
        OutputStream os = new FileOutputStream(ubp);
        p.store(os);
        os.close();
        FileUtil.toFileObject(ubp).getFileSystem().refresh(false);
        Thread.sleep(1000);
        assertTrue("got a change from disk", l.expect());
        assertEquals("still have 3 defs", 3, gpp.getProperties().size());
        assertEquals("right val for key2", "val2a", gpp.getProperties().get("key2"));
         */
    }
    
    public void testEvaluateString() throws Exception {
        Map<String,String> predefs = new HashMap<String,String>();
        predefs.put("homedir", "/home/me");
        Map<String,String> defs1 = new HashMap<String,String>();
        defs1.put("outdirname", "foo");
        defs1.put("outdir", "${homedir}/${outdirname}");
        Map<String,String> defs2 = new HashMap<String,String>();
        defs2.put("outdir2", "${outdir}/subdir");
        List<Map<String,String>> defs12 = new ArrayList<Map<String,String>>();
        defs12.add(defs1);
        defs12.add(defs2);
        assertEquals("correct evaluated string",
            "/home/me/foo/subdir is in /home/me",
            evaluateString("${outdir2} is in ${homedir}", predefs, defs12));
    }
    
    public void testFixedPropertyProvider() throws Exception {
        Map<String,String> defs = new HashMap<String,String>();
        defs.put("key1", "val1");
        defs.put("key2", "val2");
        PropertyProvider pp = PropertyUtils.fixedPropertyProvider(defs);
        assertEquals(defs, pp.getProperties());
    }
    
    public void testPropertiesFilePropertyProvider() throws Exception {
        clearWorkDir();
        final FileObject scratch = FileUtil.toFileObject(getWorkDir());
        PropertyProvider pp = PropertyUtils.propertiesFilePropertyProvider(new File(FileUtil.toFile(scratch), "test.properties"));
        MockChangeListener l = new MockChangeListener();
        pp.addChangeListener(l);
        assertEquals("no defs yet (no file)", Collections.EMPTY_MAP, pp.getProperties());
        l.assertNoEvents();
        final FileObject[] testProperties = new FileObject[1];
        scratch.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                testProperties[0] = FileUtil.createData(scratch, "test.properties");
                FileLock lock = testProperties[0].lock();
                try {
                    OutputStream os = testProperties[0].getOutputStream(lock);
                    try {
                        
                        PrintWriter pw = new PrintWriter(os);
                        pw.println("a=aval");
                        pw.flush();
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        });
        l.msg("got a change when file was created").assertEvent();
        assertEquals("one key", Collections.singletonMap("a", "aval"), pp.getProperties());
        FileLock lock = testProperties[0].lock();
        try {
            OutputStream os = testProperties[0].getOutputStream(lock);
            try {
                PrintWriter pw = new PrintWriter(os);
                pw.println("a=aval");
                pw.println("b=bval");
                pw.flush();
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
        Map<String,String> m = new HashMap<String,String>();
        m.put("a", "aval");
        m.put("b", "bval");
        l.msg("got a change when file was changed").assertEvent();
        assertEquals("right properties", m, pp.getProperties());
        testProperties[0].delete();
        l.msg("got a change when file was deleted").assertEvent();
        assertEquals("no defs again (file deleted)", Collections.emptyMap(), pp.getProperties());
    }
    
    public void testSequentialEvaluatorBasic() throws Exception {
        Map<String,String> defs1 = new HashMap<String,String>();
        defs1.put("key1", "val1");
        defs1.put("key2", "val2");
        defs1.put("key5", "5=${key1}");
        defs1.put("key6", "6=${key3}");
        Map<String,String> defs2 = new HashMap<String,String>();
        defs2.put("key3", "val3");
        defs2.put("key4", "4=${key1}:${key3}");
        defs2.put("key7", "7=${undef}");
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(null,
            PropertyUtils.fixedPropertyProvider(defs1),
            PropertyUtils.fixedPropertyProvider(defs2));
        String[] vals = {
            "val1",
            "val2",
            "val3",
            "4=val1:val3",
            "5=val1",
            "6=${key3}",
            "7=${undef}",
        };
        Map<String,String> all = eval.getProperties();
        assertEquals("right # of props", vals.length, all.size());
        for (int i = 1; i <= vals.length; i++) {
            assertEquals("key" + i + " is correct", vals[i - 1], eval.getProperty("key" + i));
            assertEquals("key" + i + " is correct in all properties", vals[i - 1], all.get("key" + i));
        }
        assertEquals("evaluate works", "5=val1 x ${undef}", eval.evaluate("${key5} x ${undef}"));
        // And test the preprovider...
        Map<String,String> predefs = Collections.singletonMap("key3", "preval3");
        eval = PropertyUtils.sequentialPropertyEvaluator(PropertyUtils.fixedPropertyProvider(predefs),
            PropertyUtils.fixedPropertyProvider(defs1),
            PropertyUtils.fixedPropertyProvider(defs2));
        vals = new String[] {
            "val1",
            "val2",
            "preval3",
            "4=val1:preval3",
            "5=val1",
            "6=preval3",
            "7=${undef}",
        };
        all = eval.getProperties();
        assertEquals("right # of props", vals.length, all.size());
        for (int i = 1; i <= vals.length; i++) {
            assertEquals("key" + i + " is correct", vals[i - 1], eval.getProperty("key" + i));
            assertEquals("key" + i + " is correct in all properties", vals[i - 1], all.get("key" + i));
        }
        assertEquals("evaluate works", "4=val1:preval3 x ${undef} x preval3", eval.evaluate("${key4} x ${undef} x ${key3}"));
    }
    
    public void testSequentialEvaluatorChanges() throws Exception {
        AntBasedTestUtil.TestMutablePropertyProvider predefs = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        AntBasedTestUtil.TestMutablePropertyProvider defs1 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        AntBasedTestUtil.TestMutablePropertyProvider defs2 = new AntBasedTestUtil.TestMutablePropertyProvider(new HashMap<String,String>());
        predefs.defs.put("x", "xval1");
        predefs.defs.put("y", "yval1");
        defs1.defs.put("a", "aval1");
        defs1.defs.put("b", "bval1=${x}");
        defs1.defs.put("c", "cval1=${z}");
        defs2.defs.put("m", "mval1");
        defs2.defs.put("n", "nval1=${x}:${b}");
        defs2.defs.put("o", "oval1=${z}");
        PropertyEvaluator eval = PropertyUtils.sequentialPropertyEvaluator(predefs, defs1, defs2);
        MockPropertyChangeListener l = new MockPropertyChangeListener();
        eval.addPropertyChangeListener(l);
        Map<String,String> result = new HashMap<String,String>();
        result.put("x", "xval1");
        result.put("y", "yval1");
        result.put("a", "aval1");
        result.put("b", "bval1=xval1");
        result.put("c", "cval1=${z}");
        result.put("m", "mval1");
        result.put("n", "nval1=xval1:bval1=xval1");
        result.put("o", "oval1=${z}");
        assertEquals("correct initial vals", result, eval.getProperties());
        l.assertEvents();
        // Change predefs.
        predefs.defs.put("x", "xval2");
        predefs.mutated();
        Map<String,String> oldvals = new HashMap<String,String>();
        oldvals.put("x", result.get("x"));
        oldvals.put("b", result.get("b"));
        oldvals.put("n", result.get("n"));
        Map<String,String> newvals = new HashMap<String,String>();
        newvals.put("x", "xval2");
        newvals.put("b", "bval1=xval2");
        newvals.put("n", "nval1=xval2:bval1=xval2");
        result.putAll(newvals);
        l.assertEventsAndValues(oldvals, newvals);
        assertEquals("right total values now", result, eval.getProperties());
        // Change some other defs.
        defs1.defs.put("z", "zval1");
        defs1.defs.remove("b");
        defs1.mutated();
        defs2.defs.put("m", "mval2");
        defs2.mutated();
        oldvals.clear();
        oldvals.put("b", result.get("b"));
        oldvals.put("c", result.get("c"));
        oldvals.put("m", result.get("m"));
        oldvals.put("n", result.get("n"));
        oldvals.put("o", result.get("o"));
        oldvals.put("z", result.get("z"));
        newvals.clear();
        newvals.put("b", null);
        newvals.put("c", "cval1=zval1");
        newvals.put("m", "mval2");
        newvals.put("n", "nval1=xval2:${b}");
        newvals.put("o", "oval1=zval1");
        newvals.put("z", "zval1");
        result.putAll(newvals);
        result.remove("b");
        l.assertEventsAndValues(oldvals, newvals);
        assertEquals("right total values now", result, eval.getProperties());
    }
    
    private static final String ILLEGAL_CHARS = " !\"#$%&'()*+,/:;<=>?@[\\]^`{|}~";
    
    public void testIsUsablePropertyName() throws Exception {
        for (int i=0; i<ILLEGAL_CHARS.length(); i++) {
            String s = ILLEGAL_CHARS.substring(i, i+1);
            assertFalse("Not a valid property name: "+s, PropertyUtils.isUsablePropertyName(s));
        }
        for (int i=127; i<256; i++) {
            String s = ""+(char)i;
            assertFalse("Not a valid property name: "+s+" - "+i, PropertyUtils.isUsablePropertyName(s));
        }
        assertFalse("Not a valid property name", PropertyUtils.isUsablePropertyName(ILLEGAL_CHARS));
        for (int i=32; i<127; i++) {
            String s = ""+(char)i;
            if (ILLEGAL_CHARS.indexOf((char)i) == -1) {
                assertTrue("Valid property name: "+s, PropertyUtils.isUsablePropertyName(s));
            }
        }
        assertTrue("Valid property name: java.classpath", 
                PropertyUtils.isUsablePropertyName("java.classpath"));
        assertFalse("Invalid property name: java#classpath", 
                PropertyUtils.isUsablePropertyName("java#classpath"));
        assertFalse("Blank name is not valid property name", 
                PropertyUtils.isUsablePropertyName(""));
    }
    
    public void testGetUsablePropertyName() throws Exception {
        StringBuffer bad = new StringBuffer();
        StringBuffer good = new StringBuffer();
        for (int i=0; i<ILLEGAL_CHARS.length(); i++) {
            bad.append(ILLEGAL_CHARS.substring(i, i+1));
            bad.append("x");
            good.append("_");
            good.append("x");
        }
        assertEquals("Corrected property name does match", good.toString(), PropertyUtils.getUsablePropertyName(bad.toString()));
    }
    
    public void testSequentialPropertyEvaluatorStringAllocation() throws Exception {
        // #48449: too many String instances.
        // String constants used in the test are interned; make sure the results are the same.
        // Not necessary for the provider to intern strings, just to not copy them.
        Map<String,String> defs = new HashMap<String,String>();
        defs.put("pre-a", "pre-a-val");
        defs.put("pre-b", "pre-b-val");
        PropertyProvider preprovider = PropertyUtils.fixedPropertyProvider(defs);
        defs = new HashMap<String,String>();
        defs.put("main-1-a", "main-1-a-val");
        defs.put("main-1-b", "main-1-b-val+${pre-b}");
        PropertyProvider provider1 = PropertyUtils.fixedPropertyProvider(defs);
        defs = new HashMap<String,String>();
        defs.put("main-2-a", "main-2-a-val");
        defs.put("main-2-b", "main-2-b-val+${main-1-b}");
        PropertyProvider provider2 = PropertyUtils.fixedPropertyProvider(defs);
        PropertyEvaluator pp = PropertyUtils.sequentialPropertyEvaluator(preprovider, provider1, provider2);
        defs = pp.getProperties();
        assertSame("uncopied pre-a", "pre-a-val", defs.get("pre-a"));
        assertSame("uncopied pre-b", "pre-b-val", defs.get("pre-b"));
        assertSame("uncopied main-1-a", "main-1-a-val", defs.get("main-1-a"));
        assertEquals("right main-1-b", "main-1-b-val+pre-b-val", defs.get("main-1-b"));
        assertSame("uncopied main-2-a", "main-2-a-val", defs.get("main-2-a"));
        assertEquals("right main-2-b", "main-2-b-val+main-1-b-val+pre-b-val", defs.get("main-2-b"));
    }
    
}
