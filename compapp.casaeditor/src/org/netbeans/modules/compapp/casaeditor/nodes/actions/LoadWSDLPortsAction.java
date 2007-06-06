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
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Port;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Check WSDL files within a SU and list available ports for loading
 * into CASA
 *
 * @author  tli
 */
public class LoadWSDLPortsAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(LoadWSDLPortsAction.class, "LBL_LoadWSDLPortsAction_Name"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length > 0 && activatedNodes[0] instanceof ServiceUnitNode) {
            final ServiceUnitNode node = ((ServiceUnitNode) activatedNodes[0]);
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    showDialog(node);
                }
            });
        }
    }
    
    private void visitAllWsdlFiles(File file, List<File> fs) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i=0; i<children.length; i++) {
                visitAllWsdlFiles(new File(file, children[i]), fs);
            }
        } else if (file.getName().toLowerCase().endsWith(".wsdl")) { // NOI18N
            fs.add(file);
        }
    }
    
    private void showDialog(ServiceUnitNode node) {
        final CasaWrapperModel model = node.getModel();
        CasaServiceEngineServiceUnit csu = (CasaServiceEngineServiceUnit) node.getData();
        final String suname = csu.getUnitName();
        ModelSource ms = model.getModelSource();
        Lookup lookup = ms.getLookup();
        CatalogModel catalogModel = (CatalogModel) lookup.lookup(CatalogModel.class);
        String casaPath = ((FileObject) lookup.lookup(FileObject.class)).getPath();
        File suRoot = new File(casaPath + "/../../jbiServiceUnits/"+suname); // NOI18N
        List<File> fs = new ArrayList<File>();
        List<Port> portList = new ArrayList<Port>();
        HashMap fileMap = new HashMap();
        visitAllWsdlFiles(suRoot, fs);
        
        for (File f : fs) {
            try {
                ModelSource ms2 = catalogModel.getModelSource(f.toURI(), ms);
                WSDLModel wm = WSDLModelFactory.getDefault().getModel(ms2);
                Collection<Service> cs = wm.getDefinitions().getServices();
                for (Service s : cs) {
                    Collection<Port> ps = s.getPorts();
                    for (Port p : ps) {
                        portList.add(p);
                        fileMap.put(p, f);
                        // System.out.println(" WSDL port: "+p.getName() + ", "+f.getPath());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // get SU..
        List<CasaPort> ps = model.getCasaPorts();
        for (CasaPort p : ps) {
            Port wp = model.getLinkedWSDLPort(p);
            if (portList.contains(wp)) {
                portList.remove(wp);
            }
        }
        
        final List<Port> plist = portList;
        final HashMap fmap = fileMap;
        if (plist.size() > 0) {
            final String[] slist = new String[portList.size()];
            for (int i=0; i < portList.size(); i++) {
                Port p = portList.get(i);
                String sName = ((Service) p.getParent()).getName();
                slist[i] = "Service=" + sName + ", Port=" + p.getName();
            }
            
            final LoadWsdlPortPanel panel = new LoadWsdlPortPanel(
                    NbBundle.getMessage(getClass(), "LBL_AvailableWSDLPortsFor", suname),
                    slist);
            DialogDescriptor descriptor = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(LoadWsdlPortPanel.class, "LBL_WsdlPort_Selection_Panel"),   // NOI18N
                    true,
                    new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                        int sel = panel.getSelectedIndex();
                        
                        Port port = plist.get(sel);
                        File f = (File) fmap.get(port);
                        model.addCasaPortFromWsdlPort(port, f);
                    }
                }
            }
            );
            
            // enable/disable the dlg ok button depending selection
            /*
            panel.addPropertyChangeListener( new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(LoadWsdlPortPanel.PROP_VALID_SELECTION)) {
                        descriptor.setValid(((Boolean)evt.getNewValue()).booleanValue());
                    }
                }
            });
            panel.checkValidity();
             */
            
            Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setPreferredSize(new Dimension(400, 400));
            dlg.setVisible(true);
            
        } else {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(LoadWSDLPortsAction.class, "MSG_NoPortFound"), // NOI18N
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        
    }
}