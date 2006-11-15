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

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.ui.nodes.MethodCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.AbstractMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Strategy for visual support for adding various methods into an EJB.
 * @author Pavel Buzek
 */
public abstract class AbstractAddMethodStrategy {
    
    private final String name;
    
    public AbstractAddMethodStrategy(String name) {
        this.name = name;
    }
    
    protected abstract MethodType getPrototypeMethod(FileObject fileObject, ElementHandle<TypeElement> methodHandle) throws IOException;
    
    /** Describes method type handled by this action. */
    public abstract MethodType.Kind getPrototypeMethodKind();
    
    public String getTitle() {
        return name;
    }
    
    protected abstract MethodCustomizer createDialog(FileObject fileObject, final MethodType pType) throws IOException;
    
    protected TypeMirror localReturnType(WorkingCopy workingCopy, EjbMethodController ejbMethodController, 
            TypeMirror typeMirror, boolean isOneReturn) {
        return typeMirror;
    }
    
    protected TypeMirror remoteReturnType(WorkingCopy workingCopy, EjbMethodController ejbMethodController, 
            TypeMirror typeMirror, boolean isOneReturn) {
        return typeMirror;
    }
    
    public void addMethod(FileObject fileObject, final ElementHandle<TypeElement> classHandle) throws IOException {
        if (classHandle == null) {
            return;
        }
        final MethodType pType = getPrototypeMethod(fileObject, classHandle);
        MethodCustomizer methodCustomizer = createDialog(fileObject, pType);
        final String[] defaultQL = new String[1];
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                TypeElement clazz = classHandle.resolve(workingCopy);
                EjbMethodController ejbMethodController = EjbMethodController.createFromClass(workingCopy, clazz);
                defaultQL[0] = ejbMethodController.createDefaultQL(pType);
            }
        });
        methodCustomizer.setEjbQL(defaultQL[0]);
        final NotifyDescriptor notifyDescriptor = new NotifyDescriptor(methodCustomizer, getTitle(),
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null, null
                );
        methodCustomizer.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(MethodCustomizer.OK_ENABLED)) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        notifyDescriptor.setValid(((Boolean)newvalue).booleanValue());
                    }
                }
            }
        });
        Object resultValue = DialogDisplayer.getDefault().notify(notifyDescriptor);
        methodCustomizer.isOK(); // apply possible changes in dialog fields
        if (resultValue == NotifyDescriptor.OK_OPTION) {
            try {
                okButtonPressed(methodCustomizer, pType, fileObject, classHandle);
            } catch (IOException ioe) {
                NotifyDescriptor ndd =
                        new NotifyDescriptor.Message(ioe.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndd);
            }
        }
    }
    
    protected void okButtonPressed(final MethodCustomizer methodCustomizer, final MethodType methodType, 
            final FileObject fileObject, final ElementHandle<TypeElement> classHandle) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runModificationTask(new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                ProgressHandle handle = ProgressHandleFactory.createHandle("Adding method");
                try {
                    handle.start(100);
                    boolean isComponent = methodType.getKind() == MethodType.Kind.BUSINESS;
                    boolean isOneReturn = methodCustomizer.finderReturnIsSingle();
                    handle.progress(10);
                    TypeElement clazz = classHandle.resolve(workingCopy);
                    EjbMethodController ejbMethodController = EjbMethodController.createFromClass(workingCopy, clazz);
                    ExecutableElement method = methodType.getMethodElement().resolve(workingCopy);
                    Trees trees = workingCopy.getTrees();
                    if (methodCustomizer.publishToLocal()) {
                        TypeMirror localReturn = localReturnType(workingCopy, ejbMethodController, method.getReturnType(), isOneReturn);
                        Element localReturnElement = workingCopy.getTypes().asElement(localReturn);
                        Tree localReturnTree = trees.getTree(localReturnElement);
                        MethodTree methodTree = AbstractMethodController.modifyMethod(
                                workingCopy, method, 
                                null, null, 
                                localReturnTree, 
                                null, null, null);
                        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), methodTree);
                        ExecutableElement modifiedMethod = (ExecutableElement) trees.getElement(treePath);
                        ejbMethodController.createAndAdd(modifiedMethod, true, isComponent);
                    }
                    handle.progress(60);
                    if (methodCustomizer.publishToRemote()) {
                        TypeMirror remoteReturn = remoteReturnType(workingCopy, ejbMethodController, method.getReturnType(), isOneReturn);
                        Element remoteReturnElement = workingCopy.getTypes().asElement(remoteReturn);
                        Tree localReturnTree = trees.getTree(remoteReturnElement);
                        MethodTree methodTree = AbstractMethodController.modifyMethod(
                                workingCopy, method, 
                                null, null, 
                                localReturnTree, 
                                null, null, null);
                        TreePath treePath = trees.getPath(workingCopy.getCompilationUnit(), methodTree);
                        ExecutableElement modifiedMethod = (ExecutableElement) trees.getElement(treePath);
                        ejbMethodController.createAndAdd(modifiedMethod, false, isComponent);
                    }
                    handle.progress(80);
                    String ejbql = methodCustomizer.getEjbQL();
                    if (ejbql != null && ejbql.length() > 0) {
                        ejbMethodController.addEjbQl(method, ejbql, getDDFile(fileObject));
                    }
                    handle.progress(99);
                } finally {
                    handle.finish();
                }
            }
        });
    }
    
    protected FileObject getDDFile(FileObject fileObject) {
        return EjbJar.getEjbJar(fileObject).getDeploymentDescriptor();
    }
    
    protected static MethodsNode getMethodsNode() {
        Node[] nodes = (Node[])Utilities.actionsGlobalContext().lookup(new Lookup.Template(Node.class)).allInstances().toArray(new Node[0]);
        if (nodes.length != 1) {
            return null;
        }
        Object object = nodes[0].getLookup().lookup(MethodsNode.class);
        if (object instanceof MethodsNode) {
            return (MethodsNode) object;
        }
        return null;
    }
    
}
