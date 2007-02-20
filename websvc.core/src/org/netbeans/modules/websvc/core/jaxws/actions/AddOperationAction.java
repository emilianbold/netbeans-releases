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
package org.netbeans.modules.websvc.core.jaxws.actions;

import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class AddOperationAction extends NodeAction  {
    
    public String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_OperationAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1 &&
                // Retouche
                //( activatedNodes[0].getLookup().lookup(ClassMember.class) != null || JMIUtils.getClassMemberFromNode(activatedNodes[0])!=null ) &&
                //JMIUtils.getJavaClassFromNode(activatedNodes[0]) != null &&
                !isFromWSDL(activatedNodes[0]);
    }

    private boolean isFromWSDL(Node node) {
        Service service = (Service)node.getLookup().lookup(Service.class);
        if (service!=null) return (service.getWsdlUrl()!=null);
        return false;
    }
    
    protected void performAction(Node[] activatedNodes) {

        if (activatedNodes.length != 1) {
            return;
        }
        
        FileObject implClassFo = activatedNodes[0].getLookup().lookup(FileObject.class);
        if (implClassFo!=null) {
            AddOperationCookie addOperationCookie = WebServiceActionProvider.getAddOperationAction(implClassFo);
            if (addOperationCookie!=null) addOperationCookie.addOperation(implClassFo);
        }
// Retouche        
//        JavaMetamodel.getManager().waitScanFinished();
//
//        JaxWsClassesCookie cookie = JaxWsCookieFactory.getJaxWsClassesCookie((Service)activatedNodes[0].getLookup().lookup(Service.class),JMIUtils.getJavaClassFromNode(activatedNodes[0]));
//        if (cookie == null) return;
//        JavaClass javaClass = JMIUtils.getJavaClassFromNode(activatedNodes[0]);
//        Method m = WSUtils.addWsOperation(javaClass, NbBundle.getMessage(AddOperationAction.class, "TXT_DefaultOperationName"));
//        if (m!=null) JMIUtils.openInEditor(m);
    }
}

