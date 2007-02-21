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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.common.method.FieldCustomizer;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EjbMethodController;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action that can always be invoked and work procedurally.
 * @author Chris Webster
 */
public class AddCmpFieldAction extends NodeAction {

    private static final String NAME = NbBundle.getMessage(AddCmpFieldAction.class, "LBL_AddCmpFieldAction");
    
    public String getName() {
        return NAME;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length < 1) {
            return false;
        }
        EjbMethodController ejbMethodController;
        ElementHandle<TypeElement> elementHandle = null;
        try {
             elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
            if (elementHandle == null) {
                return false;
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
            return false;
        }
        FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (fileObject == null) {
            return false;
        }
        return activatedNodes.length == 1 &&
               (ejbMethodController = EjbMethodController.createFromClass(fileObject, elementHandle.getQualifiedName())) != null &&
               ejbMethodController instanceof EntityMethodController &&
               ((EntityMethodController) ejbMethodController).isCMP();
    }

    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length < 1) {
            return;
        }
        try {
            ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(activatedNodes[0]);
            if (elementHandle != null) {
                FileObject fileObject = activatedNodes[0].getLookup().lookup(FileObject.class);
                FileObject ddFile = EjbJar.getEjbJar(fileObject).getDeploymentDescriptor();
                EntityMethodController emc = (EntityMethodController) EjbMethodController.createFromClass(fileObject, elementHandle.getQualifiedName());
                MethodModel.Variable field = MethodModel.Variable.create("java.lang.String", "cmpField");
                addCmpField(emc, ddFile, field);
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }

    public static boolean addCmpField(FileObject fileObject, String className, FileObject ddFile) {
        MethodModel.Variable field = MethodModel.Variable.create("java.lang.String", "cmpField");
        EntityMethodController emc = (EntityMethodController) EntityMethodController.createFromClass(fileObject, className);
        try {
            return addCmpField(emc, ddFile, field);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        return false;
    }

    private static boolean addCmpField(EntityMethodController emc, FileObject ddFile, MethodModel.Variable field) throws IOException {
        FieldCustomizer customizer = new FieldCustomizer(emc.getModelCopy(), field, "", 
                emc.getLocal() != null, emc.getRemote() != null, true, true, false, false);
        if (customizer.customizeField()) {
            MethodModel.Variable customizedField = customizer.getField();
            emc.addField(customizedField, ddFile, customizer.isLocalGetter(), customizer.isLocalSetter(),
                    customizer.isRemoteGetter(), customizer.isRemoteSetter(), customizer.getDescription());
            return true;
        }
        return false;
    }

    public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? super.createContextAwareInstance(actionContext) : null;
    }

}
