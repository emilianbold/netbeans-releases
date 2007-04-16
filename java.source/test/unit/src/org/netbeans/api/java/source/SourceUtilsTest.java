/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
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
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
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
        FileObject cls = ClasspathInfoAccessor.INSTANCE.getCachedClassPath(cpInfo,PathKind.OUTPUT).getRoots()[0];
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
    
}
