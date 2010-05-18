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
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitNode;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointsNode;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.*;
import org.netbeans.modules.compapp.casaeditor.CasaDataNode;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Port;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import org.openide.filesystems.FileUtil;

/**
 * Check WSDL files within a SU and list available ports for loading
 * into CASA
 *
 * @author  tli
 */
public class LoadWSDLPortsAction extends NodeAction {

    private static final String JBI_SOURCE_DIR = "jbiasa";      // NOI18N
    private static final String JBI_SU_JAR_DIR = "jbiServiceUnits";      // NOI18N

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
        if (activatedNodes.length > 0) {
            CasaNode casaNode = null;
            CasaWrapperModel casaModel = null;
            if (activatedNodes[0] instanceof ServiceUnitNode) {
                casaNode = ((ServiceUnitNode) activatedNodes[0]);
                casaModel = casaNode.getModel();
            } else if (activatedNodes[0] instanceof CasaNode) {
                casaNode = ((CasaNode) activatedNodes[0]);
                casaModel = casaNode.getModel();
            } else if (activatedNodes[0] instanceof CasaDataNode) {
                casaModel = ((CasaDataObject) ((CasaDataNode) activatedNodes[0])
                        .getDataObject()).getEditorSupport().getModel();
            }
            if (casaModel != null) {
                final CasaNode node = casaNode;
                final CasaWrapperModel model = casaModel;
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        showDialog(node, model);
                    }
                });
            }
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

    private String getWsdlFilename(File f, String suName) {
        String fpath = "";
        try {
            fpath = f.getCanonicalPath();
            if ((suName != null) && (suName.length() > 0)) { // from an SU
                fpath = fpath.substring(fpath.indexOf(JBI_SU_JAR_DIR)
                        + JBI_SU_JAR_DIR.length() + 1 + suName.length() + 1);
            } else if (fpath.indexOf(JBI_SOURCE_DIR) > 0) { // from CompApp src
                fpath = fpath.substring(fpath.indexOf(JBI_SOURCE_DIR)
                        + JBI_SOURCE_DIR.length() + 1);
            }
            if (fpath.length() > 0) {
                return " (" + fpath.replace('\\', '/') + ")";  // NOI18N
            }
        } catch (IOException ex) {
            // a bad file path
        }
        return fpath;
    }
    
    private void showDialog(CasaNode node, final CasaWrapperModel model) {
        // final CasaWrapperModel model = node.getModel();
        ModelSource ms = model.getModelSource();
        Lookup lookup = ms.getLookup();
        CatalogModel catalogModel = lookup.lookup(CatalogModel.class);
        FileObject casaFO = lookup.lookup(FileObject.class);
        File srcDir = FileUtil.toFile(casaFO).getParentFile().getParentFile();
        
        String suName;
        String rootPath;
        if ((node != null) && (node instanceof ServiceUnitNode)) { // load WSDL ports defined in some JBI Module
            CasaServiceEngineServiceUnit sesu = (CasaServiceEngineServiceUnit) node.getData();
            suName = sesu.getUnitName();
            rootPath = File.separator + JBI_SU_JAR_DIR + File.separator + suName;
        } else { // load WSDL ports defined in CompApp
            suName = null;
            rootPath = File.separator + JBI_SOURCE_DIR + File.separator;
        }

        File root = new File(srcDir.getAbsolutePath() + rootPath); // NOI18N
        List<File> fs = new ArrayList<File>();
        List<Port> portList = new ArrayList<Port>();
        Map<Port, File> fileMap = new HashMap<Port, File>();
        visitAllWsdlFiles(root, fs);
        
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
        final Map<Port, File> fmap = fileMap;
        if (plist.size() > 0) {
            final String[] slist = new String[portList.size()];
            for (int i=0; i < portList.size(); i++) {
                Port p = portList.get(i);
                String sName = ((Service) p.getParent()).getName();
                File f = fmap.get(p);
                String fPath = getWsdlFilename(f, suName);
                slist[i] = "Service=" + sName + ", Port=" + p.getName() + fPath;
            }

            String title = (suName == null) ?
                NbBundle.getMessage(getClass(), "LBL_AvailableWSDLPortsInCompApp") :
                NbBundle.getMessage(getClass(), "LBL_AvailableWSDLPortsInJBIModule", suName);
            final LoadWsdlPortPanel panel = new LoadWsdlPortPanel(title, slist);

            DialogDescriptor descriptor = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(LoadWsdlPortPanel.class, "LBL_WsdlPort_Selection_Panel"),   // NOI18N
                    true,
                    new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    if (evt.getSource().equals(DialogDescriptor.OK_OPTION)) {
                        for (int index : panel.getSelectedIndices()) {                        
                            Port port = plist.get(index);
                            File f = fmap.get(port);
                            model.addCasaPortFromWsdlPort(port, f);
                        }
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
            String message = (suName == null) ?
                NbBundle.getMessage(getClass(), "MSG_NoPortFoundInCompApp") : // NOI18N
                NbBundle.getMessage(getClass(), "MSG_NoPortFoundInJBIModule", suName); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        
    }
}