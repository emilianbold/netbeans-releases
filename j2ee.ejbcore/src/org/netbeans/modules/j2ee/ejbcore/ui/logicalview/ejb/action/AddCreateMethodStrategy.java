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

import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.common.method.MethodCustomizerFactory;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.action.CreateMethodGenerator;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.ErrorManager;
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
    
    protected MethodModel getPrototypeMethod() {
        return MethodModel.create(
                "create",
                "void",
                "",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.singletonList("javax.ejb.CreateException"),
                Collections.<Modifier>emptySet()
                );
    }

    protected MethodCustomizer createDialog(FileObject fileObject, final MethodModel methodModel) throws IOException{
        String className = _RetoucheUtil.getMainClassName(fileObject);
        EjbMethodController ejbMethodController = EjbMethodController.createFromClass(fileObject, className);
        MethodsNode methodsNode = getMethodsNode();
        return MethodCustomizerFactory.createMethod(
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
        return MethodType.Kind.CREATE;
    }
    
    protected void generateMethod(EntityAndSession entityAndSession, MethodModel method, boolean isOneReturn, boolean publishToLocal,
                                  boolean publishToRemote, String ejbql, FileObject ejbClassFO, String className) throws IOException {
        CreateMethodGenerator generator = CreateMethodGenerator.create(entityAndSession, ejbClassFO);
        generator.generate(method, publishToLocal, publishToRemote);
    }

    public boolean supportsEjb(FileObject fileObject, String className) {
        try {
            EntityAndSession ejb = getEntityAndSession(fileObject, className);
            if (ejb instanceof Entity) {
                return true;
            } else if (ejb instanceof Session) {
                return Session.SESSION_TYPE_STATEFUL.equals(((Session) ejb).getSessionType());
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return false;
    }

}
