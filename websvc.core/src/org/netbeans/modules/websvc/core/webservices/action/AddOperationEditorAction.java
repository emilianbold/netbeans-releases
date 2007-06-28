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
package org.netbeans.modules.websvc.core.webservices.action;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.core.AddOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

/** Editor action that opens the Add Operation dialog and adds new methods to
 *  the service
 */
public class AddOperationEditorAction extends NodeAction {
    private Service service;
    AddOperationCookie cookie;
    FileObject fo;
    
    public String getName() {
        return NbBundle.getMessage(AddOperationEditorAction.class, "LBL_AddOperationEditorAction");
    }
    
    public HelpCtx getHelpCtx() {
        // If you will provide context help then use:
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            fo = getFileObjectFromNode(activatedNodes[0]);
            if (fo!=null) {
                cookie = WebServiceActionProvider.getAddOperationAction(fo);
                return cookie!=null && activatedNodes.length == 1 && cookie.isEnabledInEditor(fo);
            }
        }
        return false;
    }
    
    
    private FileObject getFileObjectFromNode(Node n) {
        DataObject dObj = (DataObject)n.getCookie(DataObject.class);
        if (dObj!=null) return dObj.getPrimaryFile();
        else return null;
    }
    
    protected void performAction(Node[] activatedNodes) {
        if(fo == null){
            fo = getFileObjectFromNode(activatedNodes[0]);
        }
        if (fo == null) return;
        if(cookie == null) {
            cookie = WebServiceActionProvider.getAddOperationAction(fo);
        }
        if(cookie == null) return;
        cookie.addOperation(fo);
    }
}
