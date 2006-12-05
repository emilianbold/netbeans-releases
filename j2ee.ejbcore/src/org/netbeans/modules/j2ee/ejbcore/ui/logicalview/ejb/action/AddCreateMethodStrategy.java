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

import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModelSupport;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.common.method.MethodCollectorFactory;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * @author Pavel Buzek
 */
public class AddCreateMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddCreateMethodStrategy(String name) {
        super (name);
    }
    
    public AddCreateMethodStrategy() {
        super(NbBundle.getMessage(AddCreateMethodStrategy.class, "LBL_AddCreateMethodAction"));
    }
    
    protected MethodType getPrototypeMethod(FileObject fileObject, String classHandle) throws IOException {
        MethodModel method = MethodModelSupport.createMethodModel(
                "create",
                "void",
                "",
                null,
                Collections.<MethodModel.VariableModel>emptyList(),
                Collections.singletonList("javax.ejb.CreateException"),
                Collections.<Modifier>emptySet()
                );
        return new MethodType.CreateMethodType(method);
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodType pType) throws IOException{
        String className = _RetoucheUtil.getMainClassName(fileObject);
        MethodsNode methodsNode = getMethodsNode();
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        boolean local = methodsNode == null ? ejbMethodController.hasLocal() : (methodsNode.isLocal() && ejbMethodController.hasLocal());
        boolean remote = methodsNode == null ? ejbMethodController.hasRemote() : (!methodsNode.isLocal() && ejbMethodController.hasRemote());
        return MethodCollectorFactory.createCollector(pType.getMethodElement(), ejbMethodController.hasRemote(), ejbMethodController.hasLocal(), remote, local);
    }

    protected TypeMirror remoteReturnType(WorkingCopy workingCopy, EjbMethodController ejbMethodController, TypeMirror typeMirror, boolean isOneReturn) {
        return workingCopy.getElements().getTypeElement(ejbMethodController.getRemote()).asType();
    }

    protected TypeMirror localReturnType(WorkingCopy workingCopy, EjbMethodController ejbMethodController, TypeMirror typeMirror, boolean isOneReturn) {
        return workingCopy.getElements().getTypeElement(ejbMethodController.getLocal()).asType();
    }
    
    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.CREATE;
    }
    
}
