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
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
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
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
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
    
}
