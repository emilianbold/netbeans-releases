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

import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Action that can always be invoked and work procedurally.
 * @author cwebster
 */
public class AddHomeMethodStrategy extends AbstractAddMethodStrategy {
    
    public AddHomeMethodStrategy(String name) {
        super (name);
    }
    public AddHomeMethodStrategy () {
        super(NbBundle.getMessage(AddHomeMethodStrategy.class, "LBL_AddHomeMethodAction"));
    }
    
    protected MethodModel getPrototypeMethod() {
        return MethodModel.create(
                "homeMethod",
                "void",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodModel methodModel) throws IOException{
        String className = _RetoucheUtil.getMainClassName(fileObject);
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        MethodsNode methodsNode = getMethodsNode();
        return MethodCustomizerFactory.homeMethod(
                getTitle(),
                methodModel, 
                ejbMethodController.hasRemote(), 
                ejbMethodController.hasLocal(),
                methodsNode == null ? ejbMethodController.hasLocal() : methodsNode.isLocal(),
                methodsNode == null ? ejbMethodController.hasRemote() : !methodsNode.isLocal(),
                Collections.<MethodModel>emptySet() //TODO: RETOUCHE collect all methods
                );
    }
    
    public MethodType.Kind getPrototypeMethodKind() {
        return MethodType.Kind.HOME;
    }
}
