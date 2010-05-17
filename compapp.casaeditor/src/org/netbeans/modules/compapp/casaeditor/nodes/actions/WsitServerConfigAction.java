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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.BindingNode;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;

import java.util.Collection;
import java.util.HashSet;
import java.awt.Dialog;
import javax.swing.*;
import javax.swing.undo.UndoManager;

/**
 * DOCUMENT ME!
 *
 * @author tli
 * @version
 */
public class WsitServerConfigAction extends NodeAction {

    private static String helpID = "org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookieImpl"; // NOI18N
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean asynchronous() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return NbBundle.getMessage(WsitServerConfigAction.class, "LBL_WsitServerConfigAction_Name"); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(AddModuleAction.class);
    }

    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     */
    protected void performAction(Node[] activatedNodes) {
        String mName = activatedNodes[0].getDisplayName();
        final WSDLEndpointNode node = ((WSDLEndpointNode) activatedNodes[0]);
        final CasaPort cp = (CasaPort) node.getData();
        String ptn = ((CasaWrapperModel) cp.getModel()).getCasaPortType(cp).toString();

        Node[] ns = node.getChildren().getNodes();
        Port p = null;
        Service s = null;
        Binding b = null;
        PortType pt = null;
        for (int i=0; i<ns.length; i++) {
            Node n = ns[i];
            if (n instanceof PortNode) {
                p = (Port) ((PortNode) n).getWSDLComponent();
                s = (Service) p.getParent();
            } else if (n instanceof BindingNode) {
                b = (Binding) ((BindingNode) n).getWSDLComponent();
            }
        }
        pt = ((CasaWrapperModel) cp.getModel()).getCasaPortType(cp);

        final WSDLModel wsdlModel = (WSDLModel) b.getModel();
        Collection<Binding> bindings = new HashSet<Binding>();
        bindings.add(b);

        // todo: 08/27/07, add undo manager...
        final UndoManager undoManager = new UndoManager();
        wsdlModel.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
        final JComponent stc = WSITConfigProvider.getDefault().getWSITServiceConfig(wsdlModel, undoManager, bindings, node);

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                DialogDescriptor dialogDesc = new DialogDescriptor(stc, "WS-Policy Attachment: "+node.getName());  // NOI18N
                dialogDesc.setHelpCtx(new HelpCtx(helpID));
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
                dialog.setVisible(true);

                // todo: 08/24, we need to decide how to back out changes when CANCEL is selected..
                if(dialogDesc.getValue() == NotifyDescriptor.OK_OPTION){
                    // refresh wsit badge..
                    // 02/22/08, disabled upon request
                    // ((CasaWrapperModel) cp.getModel()).refershWsitStatus(cp);
                } else { // cancle
                    try {
                        if (undoManager != null) {
                            while (undoManager.canUndo()) {
                                undoManager.undo();
                            }
                            wsdlModel.sync();
                        }
                    } catch (Exception e){
                        // System.out.println("Got Error: "+e);
                    }

                }
            }
        });

    }
}
