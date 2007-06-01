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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.dd.impl.common.annotation;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 * Test for {@link ResourceImpl}.
 * @author Tomas Mysik
 */
public class ResourceImplTest extends CommonTestCase {
    
    public ResourceImplTest(String testName) {
        super(testName);
    }

    public void testSimplyAnnotatedField() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource" +
                "   private javax.sql.DataSource myResource;" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<VariableElement> fields = ElementFilter.fieldsIn(myClass.getEnclosedElements());
                VariableElement annotatedField = fields.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedField, myClass, annotationModelHelper);
                assertEquals("myResource", resource.getName());
                assertEquals("javax.sql.DataSource", resource.getType());
                assertEquals(ResourceImpl.DEFAULT_AUTHENTICATION_TYPE, resource.getAuthenticationType());
                assertEquals(ResourceImpl.DEFAULT_SHAREABLE, resource.getShareable());
                assertEquals(ResourceImpl.DEFAULT_MAPPED_NAME, resource.getMappedName());
                assertEquals(ResourceImpl.DEFAULT_DESCRIPTION, resource.getDescription());
            }
        });
    }
    
    public void testFullyAnnotatedField() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource(" +
                "       name=\"myName\", type=Object.class, shareable=false," +
                "       authenticationType=javax.annotation.Resource.AuthenticationType.APPLICATION," +
                "       mappedName=\"myMappedName\", description=\"myDescription\")" +
                "   private javax.sql.DataSource myResource;" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<VariableElement> fields = ElementFilter.fieldsIn(myClass.getEnclosedElements());
                VariableElement annotatedField = fields.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedField, myClass, annotationModelHelper);
                assertEquals("myName", resource.getName());
                assertEquals("java.lang.Object", resource.getType());
                assertEquals("APPLICATION", resource.getAuthenticationType());
                assertEquals("false", resource.getShareable());
                assertEquals("myMappedName", resource.getMappedName());
                assertEquals("myDescription", resource.getDescription());
            }
        });
    }
    
    public void testSimplyAnnotatedMethod() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource" +
                "   private void setMyResource(javax.sql.DataSource dataSource) {" +
                "   }" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<ExecutableElement> methods = ElementFilter.methodsIn(myClass.getEnclosedElements());
                ExecutableElement annotatedMethod = methods.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedMethod, myClass, annotationModelHelper);
                assertEquals("myResource", resource.getName());
                assertEquals("javax.sql.DataSource", resource.getType());
                assertEquals(ResourceImpl.DEFAULT_AUTHENTICATION_TYPE, resource.getAuthenticationType());
                assertEquals(ResourceImpl.DEFAULT_SHAREABLE, resource.getShareable());
                assertEquals(ResourceImpl.DEFAULT_MAPPED_NAME, resource.getMappedName());
                assertEquals(ResourceImpl.DEFAULT_DESCRIPTION, resource.getDescription());
            }
        });
    }
    
    public void testFullyAnnotatedMethod() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "public class MyClass {" +
                "   @javax.annotation.Resource(" +
                "       name=\"myName\", type=Object.class, shareable=false," +
                "       authenticationType=javax.annotation.Resource.AuthenticationType.APPLICATION," +
                "       mappedName=\"myMappedName\", description=\"myDescription\")" +
                "   private void setMyResource(javax.sql.DataSource dataSource) {" +
                "   }" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                List<ExecutableElement> methods = ElementFilter.methodsIn(myClass.getEnclosedElements());
                ExecutableElement annotatedMethod = methods.get(0);
                
                ResourceImpl resource = new ResourceImpl(annotatedMethod, myClass, annotationModelHelper);
                assertEquals("myName", resource.getName());
                assertEquals("java.lang.Object", resource.getType());
                assertEquals("APPLICATION", resource.getAuthenticationType());
                assertEquals("false", resource.getShareable());
                assertEquals("myMappedName", resource.getMappedName());
                assertEquals("myDescription", resource.getDescription());
            }
        });
    }
    
    public void testSimplyAnnotatedClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "@javax.annotation.Resource(name=\"myAnnotatedClass\")" +
                "public class MyClass {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                
                ResourceImpl resource = new ResourceImpl(myClass, myClass, annotationModelHelper);
                assertEquals("myAnnotatedClass", resource.getName());
                assertEquals("MyClass", resource.getType());
                assertEquals(ResourceImpl.DEFAULT_AUTHENTICATION_TYPE, resource.getAuthenticationType());
                assertEquals(ResourceImpl.DEFAULT_SHAREABLE, resource.getShareable());
                assertEquals(ResourceImpl.DEFAULT_MAPPED_NAME, resource.getMappedName());
                assertEquals(ResourceImpl.DEFAULT_DESCRIPTION, resource.getDescription());
            }
        });
    }
    
    public void testFullyAnnotatedClass() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "MyClass.java",
                "@javax.annotation.Resource(name=\"myAnnotatedClass\", type=Object.class, shareable=false," +
                "   authenticationType=javax.annotation.Resource.AuthenticationType.APPLICATION," +
                "   mappedName=\"myMappedName\", description=\"myDescription\")" +
                "public class MyClass {" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper annotationModelHelper = AnnotationModelHelper.create(cpi);
        annotationModelHelper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement myClass = annotationModelHelper.getCompilationController().getElements().getTypeElement("MyClass");
                
                ResourceImpl resource = new ResourceImpl(myClass, myClass, annotationModelHelper);
                assertEquals("myAnnotatedClass", resource.getName());
                assertEquals("java.lang.Object", resource.getType());
                assertEquals("APPLICATION", resource.getAuthenticationType());
                assertEquals("false", resource.getShareable());
                assertEquals("myMappedName", resource.getMappedName());
                assertEquals("myDescription", resource.getDescription());
            }
        });
    }
}
