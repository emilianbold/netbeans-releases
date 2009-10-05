/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointNode;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.PortNode;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.BindingNode;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.websvc.wsitconf.api.WSITConfigProvider;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;

import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.awt.*;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.xml.namespace.QName;

/**
 * DOCUMENT ME!
 *
 * @author tli
 * @version
 */
public class WsitClientConfigAction extends NodeAction {

    private static String helpID = "org.netbeans.modules.websvc.core.wseditor.support.EditWSAttributesCookieImpl"; // NOI18N
    private static String cbNamespace = "http://www.sun.com/jbi/wsit/callbackproject"; // NOI18N
    private static String cbAttribute = "CallbackProject";                             // NOI18N

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
        return NbBundle.getMessage(WsitClientConfigAction.class, "LBL_WsitClientConfigAction_Name"); // NOI18N
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

        Collection<FileObject> createdFiles = new LinkedList<FileObject>();
        Project proj = ((CasaWrapperModel) cp.getModel()).getJBIProject();
        final WSDLModel clientModel = getModelForClient(proj, wsdlModel, true, createdFiles);
        if (clientModel == null) return;

        // todo: add callback project to lookup...
        // replace the file object in lookup
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            node.removeContent(fo);
        }

        for (CasaExtensibilityElement ee : cp.getExtensibilityElements()) {
            QName eeQName = ee.getQName();
            if (eeQName.getNamespaceURI().equals(cbNamespace)) {
                String projName = ee.getAttribute(cbAttribute);
                File f = new File(projName);
                if (f.exists()) {
                    fo = FileUtil.toFileObject(f);
                    node.addContent(fo);
                }
            }
         }

        // todo: 08/27/07, add undo manager...
        final UndoManager undoManager = new UndoManager();
        clientModel.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
        final JComponent stc = WSITConfigProvider.getDefault().getWSITClientConfig(s, clientModel, wsdlModel, node);
        stc.setPreferredSize(new Dimension(450, 360)); // set a larger initial default size..

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
                } else { // click on cancle..
                    try {
                        if (undoManager != null) {
                            while (undoManager.canUndo()) {
                                undoManager.undo();
                            }
                            clientModel.sync();
                        }
                    } catch (Exception e){
                        // System.out.println("Got Error: "+e);
                    }

                }
            }
        });
    }

    public WSDLModel getModelForClient(Project p, WSDLModel originalwsdlmodel, boolean create, Collection<FileObject> createdFiles) {
        System.out.println("Calling getModelForClient...");
        WSDLModel model = null;

        try {
            Sources sources = ProjectUtils.getSources(p);
            if (sources == null) return null;
            SourceGroup[] sourceGroups = sources.getSourceGroups(JbiProjectConstants.SOURCES_TYPE_JBI);
            FileObject srcFolder = sourceGroups[0].getRootFolder();
            FileObject catalogfo = Utilities.getProjectCatalogFileObject(p);
            ModelSource catalogms = Utilities.getModelSource(catalogfo, true);
            CatalogModel cm = Utilities.getCatalogModel(catalogms);
            FileObject originalWsdlFO = originalwsdlmodel.getModelSource().getLookup().lookup(FileObject.class);

            // check whether config file already exists
            FileObject configFO = srcFolder.getFileObject(originalWsdlFO.getName(), WSITModelSupport.CONFIG_WSDL_EXTENSION);
            if ((configFO != null) && (configFO.isValid())) {
                return getModelFromFO(configFO, true);
            }

            if (create) {
                // check whether main config file exists
                FileObject mainConfigFO = srcFolder.getFileObject(WSITModelSupport.CONFIG_WSDL_CLIENT_PREFIX, WSITModelSupport.MAIN_CONFIG_EXTENSION);
                if (mainConfigFO == null) {
                    mainConfigFO = createMainConfig(srcFolder, createdFiles);
                }

                if (!originalwsdlmodel.inSync()) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(WsitClientConfigAction.class, "MSG_SaveClientWSDL"), // NOI18N
                            NbBundle.getMessage(WsitClientConfigAction.class, "TTL_SaveClientWSDL"), // NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                        return null;
                    }
                    try {
                        DataObject dataObject = DataObject.find(originalWsdlFO);
                        SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
                        if (saveCookie != null) {
                            saveCookie.save();
                        }
                    } catch (Exception ex) {
                    // failed to load casa...
                    }
                }

                copyImports(originalwsdlmodel, srcFolder, createdFiles);

                // import the model from client model
                WSDLModel mainModel = getModelFromFO(mainConfigFO, true);
                mainModel.startTransaction();
                try {
                    WSDLComponentFactory wcf = mainModel.getFactory();

                    FileObject configName = Utilities.getFileObject(originalwsdlmodel.getModelSource());
                    configFO = srcFolder.getFileObject(configName.getName(), WSITModelSupport.CONFIG_WSDL_EXTENSION);

                    boolean importFound = false;
                    Collection<Import> imports = mainModel.getDefinitions().getImports();
                    for (Import i : imports) {
                        if (i.getLocation().equals(configFO.getNameExt())) {
                            importFound = true;
                            break;
                        }
                    }
                    model = getModelFromFO(configFO, true);
                    if (!importFound) {
                        org.netbeans.modules.xml.wsdl.model.Import imp = wcf.createImport();
                        imp.setLocation((configFO).getNameExt());
                        imp.setNamespace(model.getDefinitions().getTargetNamespace());
                        Definitions def = mainModel.getDefinitions();
                        def.setName("mainclientconfig"); //NOI18N
                        def.addImport(imp);
                    }
                } finally {
                    mainModel.endTransaction();
                }

                DataObject mainConfigDO = DataObject.find(mainConfigFO);
                if ((mainConfigDO != null) && (mainConfigDO.isModified())) {
                    SaveCookie wsdlSaveCookie = mainConfigDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                    mainConfigDO.setModified(false);
                }

                DataObject configDO = DataObject.find(configFO);
                if ((configDO != null) && (configDO.isModified())) {
                    SaveCookie wsdlSaveCookie = configDO.getCookie(SaveCookie.class);
                    if(wsdlSaveCookie != null){
                        wsdlSaveCookie.save();
                    }
                    configDO.setModified(false);
                }
            }
        } catch (Exception ex) {
            // logger.log(Level.INFO, null, ex);
            ex.printStackTrace();
        }

        return model;
    }

    private static FileObject createMainConfig(FileObject folder, Collection<FileObject> createdFiles) {
        FileObject mainConfig = null;
        try {
            mainConfig = FileUtil.createData(folder, WSITModelSupport.CONFIG_WSDL_CLIENT_PREFIX + "." + WSITModelSupport.MAIN_CONFIG_EXTENSION); //NOI18N
            if ((mainConfig != null) && (mainConfig.isValid()) && !(mainConfig.isVirtual())) {
                if (createdFiles != null) {
                    createdFiles.add(mainConfig);
                }
                FileWriter fw = new FileWriter(FileUtil.toFile(mainConfig));
                fw.write(NbBundle.getMessage(WsitClientConfigAction.class, "EMPTY_WSDL"));       //NOI18N
                fw.close();
                mainConfig.refresh(true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return mainConfig;
    }

    private void copyImports(final WSDLModel model, final FileObject srcFolder, Collection<FileObject> createdFiles) throws CatalogModelException {

        // IZ#129853 can not copy from file, when the project wsdl is not yet saved..
        // ...needed to force a save of project wsdl!!!
        FileObject modelFO = Utilities.getFileObject(model.getModelSource());

        try {
            FileObject configFO = FileUtil.copyFile(modelFO, srcFolder, modelFO.getName(), WSITModelSupport.CONFIG_WSDL_EXTENSION);
            if (createdFiles != null) {
                createdFiles.add(configFO);
            }

            WSDLModel newModel = getModelFromFO(configFO, true);

            removePolicies(newModel);
            removeTypes(newModel);

            Collection<Import> oldImports = model.getDefinitions().getImports();
            Collection<Import> newImports = newModel.getDefinitions().getImports();
            Iterator<Import> newImportsIt = newImports.iterator();
            for (Import i : oldImports) {
                WSDLModel oldImportedModel = i.getImportedWSDLModel();
                FileObject oldImportFO = Utilities.getFileObject(oldImportedModel.getModelSource());
                newModel.startTransaction();
                try {
                    if (newImportsIt.next() != null) {
                        newImportsIt.next().setLocation(oldImportFO.getName() + "." + WSITModelSupport.CONFIG_WSDL_EXTENSION);
                    }
                } finally {
                    newModel.endTransaction();
                }
                copyImports(oldImportedModel, srcFolder, createdFiles);
            }
        } catch (Exception e) {
            // ignore - this happens when files are imported recursively
            // logger.log(Level.FINE, null, e);
        }
    }

    private WSDLModel getModelFromFO(FileObject wsdlFO, boolean editable) {
        WSDLModel model = null;
        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(wsdlFO, editable);
        try {
            model = WSDLModelFactory.getDefault().getModel(ms);
            if (model != null) {
                model.sync();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return model;
    }

    private void removeTypes(WSDLModel model) {
        model.startTransaction();
        try {
            Definitions d = model.getDefinitions();
            Types t = d.getTypes();
            if (t != null) {
                t.getSchemas().retainAll(new ArrayList());
            }
        } finally {
            model.endTransaction();
        }
    }

    private void removePolicies(WSDLModel model) {
        model.startTransaction();
        try {
            removePolicyElements(model.getDefinitions());
        } finally {
            model.endTransaction();
        }
    }

    private void removePolicyElements(WSDLComponent c) {
        List<Policy> policies = c.getExtensibilityElements(Policy.class);
        for (Policy p : policies) {
            c.removeExtensibilityElement(p);
        }
        List<PolicyReference> policyReferences = c.getExtensibilityElements(PolicyReference.class);
        for (PolicyReference pr : policyReferences) {
            c.removeExtensibilityElement(pr);
        }
        List<WSDLComponent> children = c.getChildren();
        for (WSDLComponent ch : children) {
            removePolicyElements(ch);
        }
    }
}
