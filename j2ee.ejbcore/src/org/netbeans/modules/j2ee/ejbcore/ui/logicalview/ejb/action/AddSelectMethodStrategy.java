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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 */
public class AddSelectMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddSelectMethodStrategy() {
        super(NbBundle.getMessage(AddSelectMethodStrategy.class, "LBL_AddSelectMethodAction"));
    }
    
    public AddSelectMethodStrategy(String name) {
        super(name);
    }
    
    public MethodType getPrototypeMethod(FileObject fileObject, ElementHandle<TypeElement> classHandle) throws IOException {
        final MethodType[] result = new MethodType[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = AbstractMethodController.createMethod(workingCopy, "ejbSelectBy");
                TypeElement exceptionElement = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                ExpressionTree throwsClause = workingCopy.getTreeMaker().QualIdent(exceptionElement);
                Set<Modifier> modifiers = new HashSet();
                modifiers.add(Modifier.PUBLIC);
                modifiers.add(Modifier.ABSTRACT);
                MethodTree methodTree = AbstractMethodController.modifyMethod(workingCopy, method,
                        modifiers,
                        null,
                        workingCopy.getTreeMaker().PrimitiveType(TypeKind.INT),
                        null,
                        Collections.<ExpressionTree>singletonList(throwsClause),
                        null);
                Trees trees = workingCopy.getTrees();
                TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), methodTree);
                ExecutableElement prototypeMethod = (ExecutableElement) trees.getElement(treePath);
                ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(prototypeMethod);
                result[0] = new MethodType.SelectMethodType(methodHandle);
            }
        });
        return result[0];
    }
    
    protected MethodCustomizer createDialog(FileObject fileObject, final MethodType pType) {
        return MethodCollectorFactory.selectCollector(pType.getMethodElement());
    }
    
    protected void okButtonPressed(final MethodCustomizer methodCustomizer, final MethodType methodType, 
            final FileObject fileObject, final ElementHandle<TypeElement> classHandle) throws java.io.IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
                try {
                    handle.start(100);
                    TypeElement clazz = classHandle.resolve(workingCopy);
                    EjbMethodController ejbMethodController = EjbMethodController.createFromClass(workingCopy, clazz);
                    ExecutableElement method = methodType.getMethodElement().resolve(workingCopy);
                    EntityMethodController entityMethodController = (EntityMethodController) ejbMethodController;
                    entityMethodController.addSelectMethod(method, methodCustomizer.getEjbQL(), getDDFile(fileObject));
                    handle.progress(99);
                } finally {
                    handle.finish();
                }
            }
        });
    }
    
    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.SELECT;
    }
}
