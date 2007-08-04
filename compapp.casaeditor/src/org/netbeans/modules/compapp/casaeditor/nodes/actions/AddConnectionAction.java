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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action to add a new connection.
 * 
 * @author jqian
 */
public class AddConnectionAction extends NodeAction {

    public AddConnectionAction() {
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length < 1) {
            return;
        }

        if (activatedNodes[0] instanceof CasaNode) {
            final CasaNode node = (CasaNode) activatedNodes[0];
            final CasaEndpointRef endpoint = (CasaEndpointRef) node.getData();
            final CasaWrapperModel model = node.getModel();

            if (model == null || endpoint == null) {
                return;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showDialog(model, endpoint);
                }
            });
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length == 1;
    }

    public String getName() {
        return NbBundle.getMessage(AddConnectionAction.class,
                "LBL_AddConnectionAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

     private void showDialog(final CasaWrapperModel model, 
             final CasaEndpointRef endpoint) {

        final List<CasaEndpointRef> endpoints = new ArrayList<CasaEndpointRef>();
        
        for (CasaPort casaPort : model.getCasaPorts()) {  
            CasaEndpointRef consumes = casaPort.getConsumes();
            if (model.canConnect(consumes, endpoint)) {
                endpoints.add(consumes);
            }
            CasaEndpointRef provides = casaPort.getProvides();
            if (model.canConnect(provides, endpoint)) {
                endpoints.add(provides);
            }
        }
        
        for (CasaServiceEngineServiceUnit sesu : model.getServiceEngineServiceUnits()) {
            for (CasaEndpointRef ep : sesu.getEndpoints()) {
                if (model.canConnect(ep, endpoint)) {
                     endpoints.add(ep);
                }
            }
        }
       
        Collections.sort(endpoints, new Comparator<CasaEndpointRef>() {
            public int compare(CasaEndpointRef o1, CasaEndpointRef o2) {
                String endpointName1 = o1.getEndpointName();
                String endpointName2 = o2.getEndpointName();
                return endpointName1.compareTo(endpointName2);
            }
        });

        final EndpointSelectionPanel panel = new EndpointSelectionPanel(endpoints);
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                NbBundle.getMessage(EndpointSelectionPanel.class, "LBL_Endpoint_Selection_Panel"),   // NOI18N
                true,
                new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) {
                if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                    CasaEndpointRef ep = panel.getSelectedItem();
                    if (ep != null) {
                        if (ep instanceof CasaConsumes) {
                            model.addConnection(
                                    (CasaConsumes) ep, (CasaProvides) endpoint);
                        } else {
                            model.addConnection(
                                    (CasaConsumes) endpoint, (CasaProvides) ep);
                        }
                    }                    
                }
            }
        });

        Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
        dlg.setPreferredSize(new Dimension(400, 400));
        dlg.setVisible(true);
    }
}
