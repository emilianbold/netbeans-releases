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

package org.netbeans.modules.j2ee.ejbcore;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.ejbcore.test.TestUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class _RetoucheUtilTest extends TestBase{
    
    public _RetoucheUtilTest(String testName) {
        super(testName);
    }

    public void testGenerateInjectedField() throws IOException {
        // creates and tests following field:
        // @javax.annotation.Resource(name="MyJndiName")
        // javax.sql.DataSource myResource;
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        _RetoucheUtil.generateAnnotatedField(
                testFO,
                "foo.TestClass",
                "javax.annotation.Resource", 
                "myResource", 
                "javax.sql.DataSource", 
                Collections.singletonMap("name", "MyJndiName"), 
                false);
        testAddedField(testFO, false);

        // creates and tests following field:
        // @javax.annotation.Resource(name="MyJndiName")
        // static javax.sql.DataSource myResource;
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        _RetoucheUtil.generateAnnotatedField(
                testFO,
                "foo.TestClass",
                "javax.annotation.Resource", 
                "myResource", 
                "javax.sql.DataSource", 
                Collections.singletonMap("name", "MyJndiName"), 
                true);
        testAddedField(testFO, true);
    }
    
    private void testAddedField(FileObject fileObject, final boolean isStatic) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.newInstance(controller).getTypeElement();
                List<VariableElement> elements = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
                VariableElement variableElement = (VariableElement) elements.get(0);
                assertEquals(isStatic, variableElement.getModifiers().contains(Modifier.STATIC));
                assertTrue(variableElement.getSimpleName().contentEquals("myResource")); // field name
                DeclaredType declaredType = (DeclaredType) variableElement.asType();
                TypeElement returnTypeElement = (TypeElement) declaredType.asElement();
                assertTrue(returnTypeElement.getQualifiedName().contentEquals("javax.sql.DataSource")); // field type
                AnnotationMirror annotationMirror = variableElement.getAnnotationMirrors().get(0);
                DeclaredType annotationDeclaredType = annotationMirror.getAnnotationType();
                TypeElement annotationTypeElement = (TypeElement) annotationDeclaredType.asElement();
                assertTrue(annotationTypeElement.getQualifiedName().contentEquals("javax.annotation.Resource")); // annotation type
                Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = annotationMirror.getElementValues().entrySet().iterator().next();
                String attributeName = entry.getKey().getSimpleName().toString();
                String attributeValue = (String) entry.getValue().getValue();
                assertEquals("name", attributeName); // attributes
                assertEquals("MyJndiName", attributeValue);
            }
        }, true);
    }

}
