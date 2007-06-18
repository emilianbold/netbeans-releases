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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class CmFieldGenerator extends AbstractMethodGenerator {
    
    // available for tests
    static final Set<Modifier> PUBLIC_ABSTRACT = new HashSet<Modifier>(Arrays.asList(new Modifier[] {
        Modifier.PUBLIC,
        Modifier.ABSTRACT
    }));
    
    private CmFieldGenerator(String ejbClass, FileObject ejbClassFileObject) {
        super(ejbClass, ejbClassFileObject);
    }
    
    public static CmFieldGenerator create(String ejbClass, FileObject ejbClassFileObject) {
        return new CmFieldGenerator(ejbClass, ejbClassFileObject);
    }
    
    public void addCmpField(MethodModel.Variable field, boolean localGetter, boolean localSetter, 
            boolean remoteGetter, boolean remoteSetter, String description) throws IOException {
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(ejbModule.getDeploymentDescriptor()); // EJB 2.1
        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Entity entity = (Entity) enterpriseBeans.findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, ejbClass);
        if (!containsField(ejbClassFileObject, entity.getEjbClass(), field)) {
            addFieldToClass(field, localGetter, localSetter, remoteGetter, remoteSetter);
        }
        if (!containsField(entity, field)) {
            CmpField cmpField = entity.newCmpField();
            cmpField.setFieldName(field.getName());
            cmpField.setDescription(description);
            entity.addCmpField(cmpField);
            saveXml();
        }
    }
    
    public void addFieldToClass(final MethodModel.Variable variable,  final boolean localGetter, final boolean localSetter, 
            final boolean remoteGetter, final boolean remoteSetter) throws IOException {
        
        // ejb class
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(ejbClass);
                MethodTree getterTree = createGetter(workingCopy, variable, PUBLIC_ABSTRACT);
                MethodTree setterTree = createSetter(workingCopy, variable, PUBLIC_ABSTRACT);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, getterTree);
                newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, setterTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        
        Map<String, String> interfaces = getInterfaces();
        final String local = interfaces.get(EntityAndSession.LOCAL);
        final String remote = interfaces.get(EntityAndSession.REMOTE);

        // local interface
        if (localGetter || localSetter) {
            FileObject localFileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, local);
            javaSource = JavaSource.forFileObject(localFileObject);
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(local);
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    ClassTree newClassTree = classTree;
                    if (localGetter) {
                        MethodTree getterTree = createGetter(workingCopy, variable, Collections.<Modifier>emptySet());
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, getterTree);
                    }
                    if (localSetter) {
                        MethodTree setterTree = createSetter(workingCopy, variable, Collections.<Modifier>emptySet());
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, setterTree);
                    }
                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();
        }
        
        // remote interface
        if (remoteGetter || remoteSetter) {
            FileObject remoteFileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, remote);
            javaSource = JavaSource.forFileObject(remoteFileObject);
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws IOException {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(remote);
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    ClassTree newClassTree = classTree;
                    if (remoteGetter) {
                        MethodTree getterTree = createGetter(workingCopy, variable, Collections.<Modifier>emptySet());
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, getterTree);
                    }
                    if (remoteSetter) {
                        MethodTree setterTree = createSetter(workingCopy, variable, Collections.<Modifier>emptySet());
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, setterTree);
                    }
                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();
        }
        
    }
    
    /**
     * Returns true if typeElement contains method with name <code>get&lt;fieldName&gt;</code>
     */
    private static boolean containsField(FileObject fileObject, final String className, final MethodModel.Variable field) throws IOException {
        final boolean[] result = new boolean[] { false };
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                TypeElement typeElement = controller.getElements().getTypeElement(className);
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (method.getSimpleName().contentEquals(getterName)) {
                        result[0] = true;
                        return;
                    }
                }
            }
        }, true);
        return result[0];
    }
    
    /**
     * Returns true if entity contains CMP field with name of the field
     */
    private static boolean containsField(Entity entity, final MethodModel.Variable field) {
        for (CmpField cmpField : entity.getCmpField()) {
            if (cmpField.getFieldName().equals(field.getName())) {
                return true;
            }
        }
        return false;
    }
    
    private static MethodTree createGetter(WorkingCopy workingCopy, MethodModel.Variable field, Set<Modifier> modifiers) {
        MethodModel methodModel = MethodModel.create(
                "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1),
                field.getType(),
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                modifiers
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
    
    private static MethodTree createSetter(WorkingCopy workingCopy, MethodModel.Variable field, Set<Modifier> modifiers) {
        MethodModel methodModel = MethodModel.create(
                "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1),
                "void",
                null,
                Collections.singletonList(MethodModel.Variable.create(field.getType(), field.getName())),
                Collections.<String>emptyList(),
                modifiers
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
    
}
