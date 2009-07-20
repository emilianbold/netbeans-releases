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

package org.netbeans.api.java.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class SourceUtilsTest extends NbTestCase {       

    private JavaSource js;
    private CompilationInfo info;

    static {
        System.setProperty("org.openide.util.Lookup", SourceUtilsTestUtil.class.getName());
    }
    
    public SourceUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {SFBQImpl.getDefault()});
    }

    private void prepareTest() throws Exception {
        File work = TestUtil.createWorkFolder();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("sourceutils");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        String capitalizedName = "T" + getName().substring(1);
        
        TestUtil.copyFiles(FileUtil.toFile(sourceRoot), "sourceutils/" + capitalizedName + ".java");
        
        packageRoot.refresh();
        
        FileObject testSource = packageRoot.getFileObject(capitalizedName + ".java");
        
        assertNotNull(testSource);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    public void testGetEnclosingTypeElement() throws Exception {
        //only a scatch of the test, add testcases as needed:
        prepareTest();
        
        TypeElement test = info.getElements().getTypeElement("sourceutils.TestGetEnclosingTypeElement");
        
        assertNotNull(test);
        
        ExecutableElement testMethod = ElementFilter.methodsIn(test.getEnclosedElements()).get(0);
//        TypeElement classInMethod = ElementFilter.typesIn(testMethod.getEnclosedElements()).get(0);
//        ExecutableElement classInMethodMethod = ElementFilter.methodsIn(classInMethod.getEnclosedElements()).get(0);;
//        VariableElement classInMethodField = ElementFilter.fieldsIn(classInMethod.getEnclosedElements()).get(0);;
//        TypeElement classInMethodNestedClass = ElementFilter.typesIn(classInMethod.getEnclosedElements()).get(0);
        VariableElement testField = ElementFilter.fieldsIn(test.getEnclosedElements()).get(0);
        TypeElement nestedClass = ElementFilter.typesIn(test.getEnclosedElements()).get(0);
        ExecutableElement nestedClassMethod = ElementFilter.methodsIn(nestedClass.getEnclosedElements()).get(0);
        VariableElement nestedClassField = ElementFilter.fieldsIn(nestedClass.getEnclosedElements()).get(0);
        TypeElement nestedClassNestedClass = ElementFilter.typesIn(nestedClass.getEnclosedElements()).get(0);
        
        assertEquals("TestGetEnclosingTypeElement", test.getSimpleName().toString());
        assertEquals("testMethod", testMethod.getSimpleName().toString());
//        assertEquals("classInMethod", classInMethod.getSimpleName().toString());
//        assertEquals("classInMethodMethod", classInMethodMethod.getSimpleName().toString());
//        assertEquals("classInMethodField", classInMethodField.getSimpleName().toString());
//        assertEquals("classInMethodNestedClass", classInMethodNestedClass.getSimpleName().toString());
        assertEquals("testField", testField.getSimpleName().toString());
        assertEquals("NestedClass", nestedClass.getSimpleName().toString());
        assertEquals("nestedClassMethod", nestedClassMethod.getSimpleName().toString());
        assertEquals("nestedClassField", nestedClassField.getSimpleName().toString());
        assertEquals("NestedClassNestedClass", nestedClassNestedClass.getSimpleName().toString());
        
        assertEquals(null, SourceUtils.getEnclosingTypeElement(test));
        assertEquals(test, SourceUtils.getEnclosingTypeElement(testMethod));
        assertEquals(test, SourceUtils.getEnclosingTypeElement(testField));
        assertEquals(test, SourceUtils.getEnclosingTypeElement(nestedClass));
        assertEquals(nestedClass, SourceUtils.getEnclosingTypeElement(nestedClassMethod));
        assertEquals(nestedClass, SourceUtils.getEnclosingTypeElement(nestedClassField));
        assertEquals(nestedClass, SourceUtils.getEnclosingTypeElement(nestedClassNestedClass));
        
        try {
            SourceUtils.getEnclosingTypeElement(test.getEnclosingElement());
            fail();
        } catch (IllegalArgumentException e) {
            //good.
        }
    }
    
    public void testIsDeprecated1() throws Exception {
        prepareTest();
        
        TypeElement test = info.getElements().getTypeElement("sourceutils.TestIsDeprecated1");
        
        assertNotNull(test);
        
        ExecutableElement methodDeprecated = findElementBySimpleName("methodDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        ExecutableElement methodNotDeprecated = findElementBySimpleName("methodNotDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        VariableElement fieldDeprecated = findElementBySimpleName("fieldDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        VariableElement fieldNotDeprecated = findElementBySimpleName("fieldNotDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        TypeElement classDeprecated = findElementBySimpleName("classDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));
        TypeElement classNotDeprecated = findElementBySimpleName("classNotDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));
        
        assertFalse(info.getElements().isDeprecated(methodNotDeprecated));
        assertFalse(info.getElements().isDeprecated(fieldNotDeprecated));
        assertFalse(info.getElements().isDeprecated(classNotDeprecated));

        assertTrue(info.getElements().isDeprecated(methodDeprecated));
        assertTrue(info.getElements().isDeprecated(fieldDeprecated));
        assertTrue(info.getElements().isDeprecated(classDeprecated));
    }
    
    public void testIsDeprecated2() throws Exception {
        prepareTest();
        
        TypeElement test = info.getElements().getTypeElement("sourceutils.TestIsDeprecated2");
        
        assertNotNull(test);
        
        ExecutableElement methodDeprecated = findElementBySimpleName("methodDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        ExecutableElement methodNotDeprecated = findElementBySimpleName("methodNotDeprecated", ElementFilter.methodsIn(test.getEnclosedElements()));
        VariableElement fieldDeprecated = findElementBySimpleName("fieldDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        VariableElement fieldNotDeprecated = findElementBySimpleName("fieldNotDeprecated", ElementFilter.fieldsIn(test.getEnclosedElements()));
        TypeElement classDeprecated = findElementBySimpleName("classDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));
        TypeElement classNotDeprecated = findElementBySimpleName("classNotDeprecated", ElementFilter.typesIn(test.getEnclosedElements()));
        
        assertFalse(info.getElements().isDeprecated(methodNotDeprecated));
        assertFalse(info.getElements().isDeprecated(fieldNotDeprecated));
        assertFalse(info.getElements().isDeprecated(classNotDeprecated));

        assertTrue(info.getElements().isDeprecated(methodDeprecated));
        assertTrue(info.getElements().isDeprecated(fieldDeprecated));
        assertTrue(info.getElements().isDeprecated(classDeprecated));
    }
    
    public void testGetOutermostEnclosingTypeElement () throws Exception {
	prepareTest();
	TypeElement test = info.getElements().getTypeElement("sourceutils.TestGetOutermostEnclosingTypeElement");        
        assertNotNull(test);
	assertEquals("TestGetOutermostEnclosingTypeElement", test.getSimpleName().toString());
	
	ExecutableElement testMethod = ElementFilter.methodsIn(test.getEnclosedElements()).get(0);
//        TypeElement classInMethod = ElementFilter.typesIn(testMethod.getEnclosedElements()).get(0);
//        ExecutableElement classInMethodMethod = ElementFilter.methodsIn(classInMethod.getEnclosedElements()).get(0);;
//        VariableElement classInMethodField = ElementFilter.fieldsIn(classInMethod.getEnclosedElements()).get(0);;
//        TypeElement classInMethodNestedClass = ElementFilter.typesIn(classInMethod.getEnclosedElements()).get(0);
        VariableElement testField = ElementFilter.fieldsIn(test.getEnclosedElements()).get(0);
        TypeElement nestedClass = ElementFilter.typesIn(test.getEnclosedElements()).get(0);
        ExecutableElement nestedClassMethod = ElementFilter.methodsIn(nestedClass.getEnclosedElements()).get(0);
        VariableElement nestedClassField = ElementFilter.fieldsIn(nestedClass.getEnclosedElements()).get(0);
        TypeElement nestedClassNestedClass = ElementFilter.typesIn(nestedClass.getEnclosedElements()).get(0);
        
        
        assertEquals("testMethod", testMethod.getSimpleName().toString());
//        assertEquals("classInMethod", classInMethod.getSimpleName().toString());
//        assertEquals("classInMethodMethod", classInMethodMethod.getSimpleName().toString());
//        assertEquals("classInMethodField", classInMethodField.getSimpleName().toString());
//        assertEquals("classInMethodNestedClass", classInMethodNestedClass.getSimpleName().toString());
        assertEquals("testField", testField.getSimpleName().toString());
        assertEquals("NestedClass", nestedClass.getSimpleName().toString());
        assertEquals("nestedClassMethod", nestedClassMethod.getSimpleName().toString());
        assertEquals("nestedClassField", nestedClassField.getSimpleName().toString());
        assertEquals("NestedClassNestedClass", nestedClassNestedClass.getSimpleName().toString());
        
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(test));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(testMethod));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(testField));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClass));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClassMethod));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClassField));
        assertEquals(test, SourceUtils.getOutermostEnclosingTypeElement(nestedClassNestedClass));
        
        try {
            SourceUtils.getOutermostEnclosingTypeElement(test.getEnclosingElement());
            fail();
        } catch (IllegalArgumentException e) {
            //good.
        }	
    }
    
    
    public void testGetDependentRoots () throws Exception {
        final URL url0 = new URL ("file:///url0/");
        final URL url1 = new URL ("file:///url1/");
        final URL url2 = new URL ("file:///url2/");
        final URL url3 = new URL ("file:///url3/");
        final URL url4 = new URL ("file:///url4/");
        final URL url5 = new URL ("file:///url5/");
        
        final List<URL> deps0 = Arrays.asList (new URL[] {url0});
        final List<URL> deps1 = Arrays.asList (new URL[] {url1, url2});
        final List<URL> deps2 = Arrays.asList (new URL[] {url2});
        final List<URL> deps3 = Arrays.asList (new URL[] {url3, url4});

        final ClassPath cp1 = ClassPathSupport.createClassPath(new URL[] {url1});
        final ClassPath cp2 = ClassPathSupport.createClassPath(new URL[] {url2});
        final ClassPath cp3 = ClassPathSupport.createClassPath(new URL[] {url3});
        final ClassPath cp4 = ClassPathSupport.createClassPath(new URL[] {url4});
        final ClassPath cp5 = ClassPathSupport.createClassPath(new URL[] {url5});

        final ClassPath[] cps = new ClassPath[] {cp1,cp2,cp3,cp4,cp5};
        
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, cps);

        final Map<URL,List<URL>> deps = new HashMap<URL,List<URL>> ();
        deps.put (url1,deps0);
        deps.put (url3,deps1);
        deps.put (url4,deps2);
        deps.put (url5,deps3);
        
        Set<URL> result = SourceUtils.getDependentRootsImpl(url5, deps);
        assertEquals (1, result.size());
        assertEquals (url5,result.iterator().next());
        
        result = SourceUtils.getDependentRootsImpl(url4, deps);
        assertEquals (new URL[] {url4, url5}, result);
        
        result = SourceUtils.getDependentRootsImpl(url3, deps);
        assertEquals (new URL[] {url3, url5}, result);
        
        result = SourceUtils.getDependentRootsImpl(url2, deps);
        assertEquals (new URL[] {url2, url3, url4, url5}, result);
        
        result = SourceUtils.getDependentRootsImpl(url1, deps);
        assertEquals (new URL[] {url1, url3, url5}, result);
    }
    
    
    private void assertEquals (URL[] expected, Set<URL> result) {
        assertEquals (expected.length,result.size());
        for (URL eurl : expected) {
            assertTrue (result.remove(eurl));
        }
        assertTrue(result.isEmpty());
    }
    
    private <E extends Element> E findElementBySimpleName(String simpleName, List<E> elements) {
        for (E e : elements) {
            if (simpleName.contentEquals(e.getSimpleName()))
                return e;
        }
        
        fail("Not found element with simple name: " + simpleName);
        
        throw new Error("Should never be here!");
    }
    
    public void testGetFQNsForSimpleName() throws Exception {
// The method was removed,
// tzezula: I am going to create an replacement using lucene        
//        prepareTest();
//        
//        List<TypeElement> fqnTEs;
//        List<String> fqns;
//        
//        fqnTEs = SourceUtils.getFQNsForSimpleNamePrefix(info, "List", true);
//        fqns = new ArrayList<String>();
//        
//        for (TypeElement te : fqnTEs) {
//            fqns.add(te.getQualifiedName().toString());
//        }
//        
//        assertTrue(fqns.remove("java.util.List"));
//        assertTrue(fqns.remove("java.awt.List"));
//        
//        //JDK16 specific:
//        fqns.remove("com.sun.xml.bind.v2.schemagen.xmlschema.List");
//        
//        assertEquals(fqns.toString(), 0, fqns.size());
//
//        fqnTEs = SourceUtils.getFQNsForSimpleNamePrefix(info, "File", true);
//        fqns = new ArrayList<String>();
//        
//        for (TypeElement te : fqnTEs) {
//            fqns.add(te.getQualifiedName().toString());
//        }
//        
//        assertTrue(fqns.remove("java.io.File"));
//        
//        assertEquals(fqns.toString(), 0, fqns.size());
        
        //XXX: onlyExact
    }
    
    
    public void testGetFile () throws Exception {
        File workDir = getWorkDir();
        FileObject workFo = FileUtil.toFileObject(workDir);
        assertNotNull (workFo);
        FileObject src = workFo.createFolder("src");
        FileObject srcInDefPkg = src.createData("Foo","java");
        assertNotNull(srcInDefPkg);
        FileObject sourceFile = src.createFolder("org").createFolder("me").createData("Test", "java");
        assertNotNull(sourceFile);
        ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new FileObject[0]), ClassPathSupport.createClassPath(new FileObject[0]),
            ClassPathSupport.createClassPath(new FileObject[]{src}));
        FileObject cls = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo,PathKind.OUTPUT).getRoots()[0];
        FileObject classInDefPkg = cls.createData("Foo","class");
        assertNotNull(classInDefPkg);
        FileObject classPkg = cls.createFolder("org").createFolder("me");
        assertNotNull(classPkg);
        FileObject classFile = classPkg.createData("Test", "class");
        assertNotNull(classFile);
        FileObject classFileInnder = classPkg.createData("Test$Inner", "class");
        assertNotNull(classFileInnder);        
        SFBQImpl.getDefault().register(cls, src);
        ElementHandle<? extends Element> handle = ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, new String[] {"org.me.Test"});
        assertNotNull (handle);        
        FileObject result = SourceUtils.getFile(handle, cpInfo);
        assertNotNull(result);
        handle = ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, new String[] {"org.me.Test$Inner"});
        result = SourceUtils.getFile(handle,cpInfo);
        assertNotNull(result);
        handle = ElementHandleAccessor.INSTANCE.create(ElementKind.PACKAGE, new String[] {"org.me"});
        result = SourceUtils.getFile(handle,cpInfo);
        assertNotNull(result);
        handle = ElementHandleAccessor.INSTANCE.create(ElementKind.CLASS, new String[] {"Foo"});
        result = SourceUtils.getFile(handle,cpInfo);
        assertNotNull(result);
    }
    
    
    private static class SFBQImpl implements SourceForBinaryQueryImplementation {
        
        private static SFBQImpl instance;
        
        private final Map<URL, FileObject> map = new HashMap<URL, FileObject> ();
        
        private SFBQImpl () {
            
        }
        
        public void register (FileObject bin, FileObject src) throws IOException {
            map.put(bin.getURL(), src);
        }
            
        public Result findSourceRoots(URL binaryRoot) {
            final FileObject src = map.get (binaryRoot);
            if (src != null) {
                return new Result() {

                    public FileObject[] getRoots() {
                        return new FileObject[] {src};
                    }

                    public void addChangeListener(ChangeListener l) {                        
                    }

                    public void removeChangeListener(ChangeListener l) {                        
                    }
                };
            }
            return null;
        }
        
        public static synchronized SFBQImpl getDefault () {
            if (instance == null) {
                instance = new SFBQImpl ();
            }
            return instance;
        }
    }

    public void testGenerateReadableParameterName() throws Exception {
        System.out.println("testGenerateReadableParameterName");
        Match m = new Match("java.lang.Object", "o");
        m.match("java.lang.Runnable", "r")
        .match("java.awt.event.ActionListener,java.awt.event.ActionListener","al,al1")
        .match("java.io.InputStream", "in")
        .match("java.io.OutputStream","out")
        .match("java.io.ByteArrayOutputStream","stream")
        .match("missingthing.Foodbar", "fdbr")
        .match("somepackage.FillUpNoKnownMessageEverywhere", "funkme")
        .match("java.lang.Class","type")
        .match("java.lang.Class<T>", "type")
        .match("org.openide.util.Lookup","lkp")
        .match("sun.awt.KeyboardFocusManagerPeerImpl", "kfmpi")
        .match("com.foo.BigInterface", "bi")
        .match("java.util.concurrent.Callable<Runnable>", "clbl")
        .match("int[]", "ints")
        .match("short", "s")
        .match("java.lang.Integer...", "intgrs")
        .match("java.awt.Component[]", "cmpnts")
        .match("int,java.lang.Runnable", "i,r")
        .match("java.lang.Runnable[]", "rs")
        .match("com.foo.Classwithanannoyinglylongname", "c")
        .match("com.foo.Classwithanannoyinglylongname,foo.bar.Classwithanotherannoyinglylongname", "c,c1")
        .match("com.foo.Classwithanannoyinglylongname,foo.bar.ClasswithLongnameButshortAcronym", "c,clba")
        .match("com.foo.ClassWithAnAnnoyinglyLongNameThatGoesOnForever", "c");
        m.assertMatch();
    }

    private static final class Match {
        private String[] fqns;
        private String[] names;
        private final Set<String> used = new HashSet<String>();
        Match(String fqns, String names) {
            this.fqns = fqns.split(",");
            this.names = names.split(",");
            assertEquals ("Test is broken: " + fqns + " vs " + names + " do " +
                    "not have same number of elements", this.fqns.length, this.names.length);
        }

        public void assertMatch() {
            assertTrue (this.fqns.length > 0);
            assertTrue (this.names.length > 0);
            for (int i = 0; i < fqns.length; i++) {
                String fqn = fqns[i];
                String expected = names[i];
                String got = SourceUtils.generateReadableParameterName(fqn, used);
                String msg = "For " + Arrays.asList(fqns) + " expected " + Arrays.asList(names);
                assertEquals (msg, expected, got);
            }
            if (next != null) {
                next.assertMatch();
            }
        }

        private Match next;
        public Match match(String fqns, String names) {
            next = new Match (fqns, names);
            return next;
        }
    }
    
//    //tests for SourceUtils.filterSupportedMIMETypes:
//
//    @SuppressWarnings("deprecation")
//    public void testFilter() throws Exception {
//        SourceUtilsTestUtil.setLookup(new Object[] {new JavaSourceProviderImpl(), new ResolverImpl()}, SourceUtilsTest.class.getClassLoader());
//        boolean registered = false;
//        
//        for (JavaSourceProvider p : Lookup.getDefault().lookupAll(JavaSourceProvider.class)) {
//            if (p instanceof JavaSourceProvider) {
//                registered = true;
//                break;
//            }
//        }
//        
//        assertTrue(registered);
//        
//        FileObject work = FileUtil.toFileObject(getWorkDir());
//        
//        FileObject file1 = FileUtil.createData(work, "test.ext1");
//        FileObject file2 = FileUtil.createData(work, "test.ext2");
//        FileObject file3 = FileUtil.createData(work, "test.ext3");
//        FileObject file4 = FileUtil.createData(work, "test.ext4");
//        FileObject file5 = FileUtil.createData(work, "test.txt");
//        FileObject file6 = FileUtil.createData(work, "test.ant");
//
//        assertEquals("text/x-java", FileUtil.getMIMEType(file1));
//        assertEquals("text/jsp", FileUtil.getMIMEType(file2));
//        assertEquals("text/plain", FileUtil.getMIMEType(file3));
//        assertEquals("text/test+x-java", FileUtil.getMIMEType(file4));
//        assertEquals("text/test+x-ant+xml", FileUtil.getMIMEType(file6));
//        
//        List<FileObject> files = Arrays.asList(file1, file2, file3, file4, file5, file6);
//        
//        assertEquals(Arrays.asList(file1, file2, file3, file4, file6), SourceUtils.filterSupportedMIMETypes(files, F1.class));
//        assertEquals(Arrays.asList(file1, file4), SourceUtils.filterSupportedMIMETypes(files, F2.class));
//        assertEquals(Arrays.asList(file4), SourceUtils.filterSupportedMIMETypes(files, F3.class));
//        assertEquals(Arrays.asList(file1, file4), SourceUtils.filterSupportedMIMETypes(files, F4.class));
//        assertEquals(Arrays.asList(file1, file2, file4), SourceUtils.filterSupportedMIMETypes(files, F5.class));
//    }
//    
//    public static class JavaSourceProviderImpl implements JavaSourceProvider {
//        public PositionTranslatingJavaFileFilterImplementation forFileObject(FileObject fo) {
//            if ("txt".equals(fo.getExt()))
//                return null;
//            
//            return new PositionTranslatingJavaFileFilterImplementation() {
//                public int getOriginalPosition(int javaSourcePosition) {
//                    return javaSourcePosition;
//                }
//                public int getJavaSourcePosition(int originalPosition) {
//                    return originalPosition;
//                }
//                public Reader filterReader(Reader r) {
//                    return r;
//                }
//                public CharSequence filterCharSequence(CharSequence charSequence) {
//                    return charSequence;
//                }
//                public Writer filterWriter(Writer w) {
//                    return w;
//                }
//                public void addChangeListener(ChangeListener listener) {}
//                public void removeChangeListener(ChangeListener listener) {}
//            };
//        }
//    }
//    
//    public static class ResolverImpl extends MIMEResolver {
//        public String findMIMEType(FileObject fo) {
//            String ext = fo.getExt();
//            
//            if ("ext1".contains(ext)) return "text/x-java";
//            if ("ext2".contains(ext)) return "text/jsp";
//            if ("ext3".contains(ext)) return "text/plain";
//            if ("ext4".contains(ext)) return "text/test+x-java";
//            if ("ant".contains(ext)) return "text/test+x-ant+xml";
//            
//            return null;
//        }
//    }
//    
//    @SupportedMimeTypes("*")
//    private static class F1 {}
//    
//    @SupportedMimeTypes("text/x-java")
//    private static class F2 {}
//    
//    @SupportedMimeTypes("text/test+x-java")
//    private static class F3 {}
//    
//    private static class F4 {}
//    
//    @SupportedMimeTypes({"text/x-java", "text/jsp"})
//    private static class F5 {}
}
