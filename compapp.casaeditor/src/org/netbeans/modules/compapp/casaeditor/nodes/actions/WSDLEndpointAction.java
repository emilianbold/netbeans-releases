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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.BindingNode;
import org.netbeans.modules.xml.wsdl.ui.wizard.WizardBindingConfigurationStep;
import org.netbeans.modules.xml.wsdl.ui.wizard.BindingGenerator;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;

import java.util.Collection;
import java.util.Vector;
import java.util.Map;
import java.util.HashSet;
import java.awt.Dialog;
import javax.swing.SwingUtilities;
import javax.swing.JComponent;

/**
 * DOCUMENT ME!
 *
 * @author 
 * @version 
 */
public class WSDLEndpointAction extends NodeAction {
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
        return NbBundle.getMessage(WSDLEndpointAction.class, "LBL_WSDLEndpointAction_Name"); // NOI18N
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
        final WSDLEndpointNode node = ((WSDLEndpointNode) activatedNodes[0]); // .getData(); // CasaPortImpl
        CasaPort cp = (CasaPort) node.getData();
        String ptn = ((CasaWrapperModel) cp.getModel()).getCasaPortType(cp).toString();
//        String ptn = cp.getPortType();
        //port.getConsumes(true).setInterfaceQName(qName);

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
                //System.out.println("portNode: "+n.getName()+", "+s.getName() + "." + p.getName());  
            } else if (n instanceof BindingNode) {
                b = (Binding) ((BindingNode) n).getWSDLComponent();
                //System.out.println("bindingNode: "+n.getName()+", "+b.getName());
            }
        }
        //System.out.println("Got WSDLEndpoint Action.. PtN: "+ptn);
        pt = ((CasaWrapperModel) cp.getModel()).getCasaPortType(cp);
        //System.out.println("Got WSDLEndpoint Action.. Pt: "+pt);

        WSDLModel wsdlModel = (WSDLModel) b.getModel();
        Collection<Binding> bindings = new HashSet<Binding>();
        bindings.add(b);
        final JComponent stc = WSITConfigProvider.getDefault().getWSITServiceConfig(wsdlModel, null, bindings, node);
        // final ServiceTopComponent stc = new ServiceTopComponent(null, null, wsdlModel, node, null, bindings);
        //System.out.println("Got WsitTC: "+stc);

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                // tc.addTabs(editors, node, jaxWsModel );
                DialogDescriptor dialogDesc = new DialogDescriptor(stc, "WS-Policy Attachement: "+node.getName());  // NOI18N
                //dialogDesc.setHelpCtx(new HelpCtx(EditWSAttributesCookieImpl.class));
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDesc);
                dialog.setVisible(true);

                // todo: 01/24, we need to decide how to back out changes when CANCEL is selected..

                /*
                if(dialogDesc.getValue() == NotifyDescriptor.OK_OPTION){
                    for(WSEditor editor : editors){
                        editor.save(node, jaxWsModel);
                    }
                } else{
                    for(WSEditor editor: editors){
                        editor.cancel(node, jaxWsModel);
                    }
                }
                */
            }
        });

    }
}
