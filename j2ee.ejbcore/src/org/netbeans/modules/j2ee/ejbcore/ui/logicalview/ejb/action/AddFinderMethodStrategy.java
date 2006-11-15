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
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCollectorFactory;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 */
public class AddFinderMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddFinderMethodStrategy (String name) {
        super(name);
    }
    public AddFinderMethodStrategy () {
        super (NbBundle.getMessage(AddFinderMethodStrategy.class, "LBL_AddFinderMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(FileObject fileObject, ElementHandle<TypeElement> classHandle) throws IOException {
        return getFinderPrototypeMethod(fileObject, classHandle);
    }

    public static MethodType getFinderPrototypeMethod(FileObject fileObject, ElementHandle<TypeElement> classHandle) throws IOException {
        final MethodType[] result = new MethodType[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                ExecutableElement method = AbstractMethodController.createMethod(workingCopy, "findBy");
                TypeElement exceptionElement = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                ExpressionTree throwsClause = workingCopy.getTreeMaker().QualIdent(exceptionElement);
                MethodTree methodTree = AbstractMethodController.modifyMethod(workingCopy, method, 
                        null, null, null, null, 
                        Collections.<ExpressionTree>singletonList(throwsClause),
                        null);
                Trees trees = workingCopy.getTrees();
                TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), methodTree);
                ExecutableElement prototypeMethod = (ExecutableElement) trees.getElement(treePath);
                ElementHandle<ExecutableElement> methodHandle = ElementHandle.create(prototypeMethod);
                result[0] = new MethodType.FinderMethodType(methodHandle);
            }
        });
        return result[0];
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodType pType) throws IOException {
        return createFinderDialog(fileObject, pType);
    }

    protected MethodCustomizer createFinderDialog(FileObject fileObject, final MethodType pType) throws IOException{
        final MethodCustomizer[] result = new MethodCustomizer[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                MethodsNode methodsNode = getMethodsNode();
                TypeElement clazz = SourceUtils.newInstance(workingCopy).getTypeElement();
                EjbMethodController ejbMethodController = EjbMethodController.createFromClass(workingCopy, clazz);
                boolean local = methodsNode == null ? ejbMethodController.hasLocal() : (methodsNode.isLocal() && ejbMethodController.hasLocal());
                boolean remote = methodsNode == null ? ejbMethodController.hasRemote() : (!methodsNode.isLocal() && ejbMethodController.hasRemote());
                boolean javaImpl = ejbMethodController.hasJavaImplementation(pType);
                result[0] = MethodCollectorFactory.finderCollector(
                        pType.getMethodElement(), ejbMethodController.hasRemote(), ejbMethodController.hasLocal(), !javaImpl, remote, local);
            }
        });
        return result[0];
    }

    protected TypeMirror remoteReturnType(WorkingCopy workingCopy, EjbMethodController ejbMethodController, TypeMirror typeMirror, boolean isOneReturn) {
        String fullName = isOneReturn? ejbMethodController.getRemote() : Collection.class.getName();
        return workingCopy.getElements().getTypeElement(fullName).asType();
    }

    protected TypeMirror localReturnType(WorkingCopy workingCopy, EjbMethodController ejbMethodController, TypeMirror typeMirror, boolean isOneReturn) {
        String fullName = isOneReturn?ejbMethodController.getLocal():Collection.class.getName();
        return workingCopy.getElements().getTypeElement(fullName).asType();
    }
    
    public int prototypeMethod() {
        return MethodType.METHOD_TYPE_FINDER;
    }
    
}
