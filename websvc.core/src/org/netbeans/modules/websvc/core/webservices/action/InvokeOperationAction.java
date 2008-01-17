/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.core.webservices.action;

import java.awt.Dialog;
import org.netbeans.modules.websvc.core.InvokeOperationCookie;
import org.netbeans.modules.websvc.core.WebServiceActionProvider;
import org.netbeans.modules.websvc.core.webservices.ui.panels.ClientExplorerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


import org.openide.util.actions.NodeAction;


/**
 *
 * @author Peter Williams
 */
public class InvokeOperationAction extends NodeAction {
    public String getName() {
        return NbBundle.getMessage(InvokeOperationAction.class, "LBL_CallWebServiceOperation"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        // If you will provide context help then use:
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;
        if (activatedNodes != null && activatedNodes.length == 1 && activatedNodes[0] != null) {
            if (InvokeOperationCookie.TARGET_SOURCE_UNKNOWN != getTargetSourceType(activatedNodes[0]))
                return true;
        }
        return result;
    }

    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes != null && activatedNodes[0] != null) {
            FileObject currentFO = getCurrentFileObject(activatedNodes[0]);
            if(currentFO != null) {
                // !PW I wrote this code before I knew about NodeOperation.  Anyway, this
                // behaves a bit nicer in that the root node is hidden and the tree opens
                // up expanded.  Both improve usability for this use case I think.
                ClientExplorerPanel serviceExplorer = new ClientExplorerPanel(currentFO);
                DialogDescriptor descriptor = new DialogDescriptor(serviceExplorer, 
                        NbBundle.getMessage(InvokeOperationAction.class,"TTL_SelectOperation"));
                serviceExplorer.setDescriptor(descriptor);
                // !PW FIXME put help context here when known to get a displayed help button on the panel.
//                descriptor.setHelpCtx(new HelpCtx("HelpCtx_J2eePlatformInstallRootQuery"));
                Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                dlg.getAccessibleContext().setAccessibleDescription(dlg.getTitle());
                dlg.setVisible(true);
                if(descriptor.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                    // !PW FIXME refactor this as a method implemented in a cookie
                    // on the method node.
                    InvokeOperationCookie invokeCookie = WebServiceActionProvider.getInvokeOperationAction(currentFO,serviceExplorer.getSelectedMethod());
                    if (invokeCookie!=null)
                        invokeCookie.invokeOperation(getTargetSourceType(activatedNodes[0]), activatedNodes[0], serviceExplorer.getSelectedMethod());
                }
            }
        }
    }
    
    private FileObject getCurrentFileObject(Node n) {
        FileObject result = null;
        DataObject dobj = (DataObject) n.getCookie(DataObject.class);
        if(dobj != null) {
            result = dobj.getPrimaryFile();
        }
        return result;
    }
    
    private int getTargetSourceType(Node node) {
        EditorCookie cookie = (EditorCookie)node.getCookie(EditorCookie.class);
        if (cookie!=null && "text/x-jsp".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return InvokeOperationCookie.TARGET_SOURCE_JSP;
        } else if (cookie!=null && "text/x-java".equals(cookie.getDocument().getProperty("mimeType"))) { //NOI18N
            return InvokeOperationCookie.TARGET_SOURCE_JAVA;
        }
        return InvokeOperationCookie.TARGET_SOURCE_UNKNOWN;
    }
    
}
