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
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.dd.api.ejb.CmpField;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class CmFieldGenerator extends AbstractMethodGenerator {
    
    private static final Set<Modifier> PUBLIC_ABSTRACT = new HashSet<Modifier>(Arrays.asList(new Modifier[] {
        Modifier.PUBLIC,
        Modifier.ABSTRACT
    }));
    
    private CmFieldGenerator(Entity entity, FileObject ejbClassFileObject, FileObject ddFileObject) {
        super(entity, ejbClassFileObject, ddFileObject);
    }
    
    public static CmFieldGenerator create(Entity entity, FileObject ejbClassFileObject, FileObject ddFileObject) {
        return new CmFieldGenerator(entity, ejbClassFileObject, ddFileObject);
    }
    
    public void addCmpField(MethodModel.Variable field, boolean localGetter, boolean localSetter, 
            boolean remoteGetter, boolean remoteSetter, String description) throws IOException {
        addFieldToClass(field, localGetter, localSetter, remoteGetter, remoteSetter);
        Entity entity = (Entity) ejb;
        CmpField cmpField = entity.newCmpField();
        cmpField.setFieldName(field.getName());
        cmpField.setDescription(description);
        entity.addCmpField(cmpField);
        saveXml();
    }
    
    public void addFieldToClass(final MethodModel.Variable variable,  final boolean localGetter, final boolean localSetter, 
            final boolean remoteGetter, final boolean remoteSetter) throws IOException {
        
        // ejb class
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement(ejb.getEjbClass());
                MethodTree getterTree = createGetter(workingCopy, variable);
                MethodTree setterTree = createSetter(workingCopy, variable);
                ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                ClassTree newClassTree = workingCopy.getTreeMaker().addClassMember(classTree, getterTree);
                newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, setterTree);
                workingCopy.rewrite(classTree, newClassTree);
            }
        }).commit();
        
        // local interface
        if (localGetter || localSetter) {
            FileObject localFileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getLocal());
            javaSource = JavaSource.forFileObject(localFileObject);
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(ejb.getLocal());
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    ClassTree newClassTree = classTree;
                    if (localGetter) {
                        MethodTree getterTree = createGetter(workingCopy, variable);
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, getterTree);
                    }
                    if (localSetter) {
                        MethodTree setterTree = createSetter(workingCopy, variable);
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, setterTree);
                    }
                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();
        }
        
        // remote interface
        if (remoteGetter || remoteSetter) {
            FileObject remoteFileObject = _RetoucheUtil.resolveFileObjectForClass(ejbClassFileObject, ejb.getRemote());
            javaSource = JavaSource.forFileObject(remoteFileObject);
            javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
                public void run(WorkingCopy workingCopy) throws Exception {
                    workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = workingCopy.getElements().getTypeElement(ejb.getRemote());
                    ClassTree classTree = workingCopy.getTrees().getTree(typeElement);
                    ClassTree newClassTree = classTree;
                    if (remoteGetter) {
                        MethodTree getterTree = createGetter(workingCopy, variable);
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, getterTree);
                    }
                    if (remoteSetter) {
                        MethodTree setterTree = createSetter(workingCopy, variable);
                        newClassTree = workingCopy.getTreeMaker().addClassMember(newClassTree, setterTree);
                    }
                    workingCopy.rewrite(classTree, newClassTree);
                }
            }).commit();
        }
        
    }
    
    private static MethodTree createGetter(WorkingCopy workingCopy, MethodModel.Variable field) {
        MethodModel methodModel = MethodModel.create(
                "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1),
                field.getType(),
                null,
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                PUBLIC_ABSTRACT
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
    
    private static MethodTree createSetter(WorkingCopy workingCopy, MethodModel.Variable field) {
        MethodModel methodModel = MethodModel.create(
                "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1),
                "void",
                null,
                Collections.singletonList(MethodModel.Variable.create(field.getType(), field.getName())),
                Collections.<String>emptyList(),
                PUBLIC_ABSTRACT
                );
        return MethodModelSupport.createMethodTree(workingCopy, methodModel);
    }
    
}
