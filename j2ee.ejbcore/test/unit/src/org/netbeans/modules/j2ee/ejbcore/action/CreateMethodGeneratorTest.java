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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class CreateMethodGeneratorTest extends TestBase {
    
    public CreateMethodGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateSession() throws IOException {
        TestModule testModule = ejb14();
        
        // add create method into local and remote interfaces of Stateful session EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("statefullr/StatefulLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        Session session = (Session) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.SESSION, Session.EJB_CLASS, "statefullr.StatefulLRBean");
        CreateMethodGenerator generator = CreateMethodGenerator.create(session, beanClass);
        final MethodModel methodModel = MethodModel.create(
                "createTest",
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        generator.generate(methodModel, true, true);

        // ejb class, EJB 2.1 spec 7.11.3
        final boolean[] found = new boolean[] { false };
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("ejbCreateTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNotNull(methodTree.getBody());
                        assertTrue(executableElement.getModifiers().contains(Modifier.PUBLIC));
                        assertFalse(executableElement.getModifiers().contains(Modifier.FINAL));
                        assertFalse(executableElement.getModifiers().contains(Modifier.STATIC));
                        assertSame(TypeKind.VOID, executableElement.getReturnType().getKind());
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // local interface, EJB 2.1 spec 7.11.8
        found[0] = false;
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("statefullr/StatefulLRLocalHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRLocalHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertFalse(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement localTypeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRLocal");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), localTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface, EJB 2.1 spec 7.11.6
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("statefullr/StatefulLRRemoteHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRRemoteHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertTrue(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement remoteTypeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRRemote");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), remoteTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
    }
    
    public void testGenerateEntity() throws IOException {
        TestModule testModule = ejb14();
        
        // add create method into local and remote interfaces of CMP Entity EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        Entity entity = (Entity) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, "cmplr.CmpLRBean");
        CreateMethodGenerator generator = CreateMethodGenerator.create(entity, beanClass);
        final MethodModel methodModel = MethodModel.create(
                "createTest",
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        generator.generate(methodModel, true, true);

        // ejb class, EJB 2.1 spec 10.6.4, 10.6.5
        final boolean[] found = new boolean[] { false, false }; // ejbCreate, ejbPostCreate
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("ejbCreateTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNotNull(methodTree.getBody());
                        assertTrue(executableElement.getModifiers().contains(Modifier.PUBLIC));
                        assertFalse(executableElement.getModifiers().contains(Modifier.FINAL));
                        assertFalse(executableElement.getModifiers().contains(Modifier.STATIC));
                        TypeElement longTypeElement = workingCopy.getElements().getTypeElement("java.lang.Long");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), longTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                    if (executableElement.getSimpleName().contentEquals("ejbPostCreateTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNotNull(methodTree.getBody());
                        assertTrue(executableElement.getModifiers().contains(Modifier.PUBLIC));
                        assertFalse(executableElement.getModifiers().contains(Modifier.FINAL));
                        assertFalse(executableElement.getModifiers().contains(Modifier.STATIC));
                        assertSame(TypeKind.VOID, executableElement.getReturnType().getKind());
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[1] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        assertTrue(found[1]);
        
        // local interface, EJB 2.1 spec 10.6.12
        found[0] = false;
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRLocalHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocalHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertFalse(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement localTypeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocal");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), localTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface, EJB 2.1 spec 10.6.10
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRRemoteHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemoteHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertTrue(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement remoteTypeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemote");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), remoteTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
    }

}
