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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 * Provide action for calling another EJB
 * @author Chris Webster
 * @author Martin Adamek
 */
public class CallEjbAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        try {
            ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
            CallEjbDialog callEjbDialog = new CallEjbDialog();
            callEjbDialog.open(fileObject, elementHandle.getQualifiedName(), NbBundle.getMessage(CallEjbAction.class, "LBL_CallEjbAction")); //NOI18N
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public String getName() {
        return NbBundle.getMessage(CallEjbAction.class, "LBL_CallEjbAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        ElementHandle<TypeElement> elementHandle = null;
        try {
            elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
        if (elementHandle == null) {
            return false;
        }
        FileObject srcFile = nodes[0].getLookup().lookup(FileObject.class);
        Project project = FileOwnerQuery.getOwner(srcFile);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup ().lookup (J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            if (serverInstanceId == null) {
                return true;
            }
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
            if (platform == null) {
                return true;
            }
            if (!platform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
                return false;
            }
        }
        try {
            return !_RetoucheUtil.isInterface(srcFile, elementHandle);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CallEjbAction.class, "HINT_Action"));
     * }
     */

    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
}
