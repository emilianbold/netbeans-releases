/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
    private static String CALLBACK_PROJECT_NAMESPACE = "http://www.sun.com/jbi/wsit/callbackproject"; // NOI18N
    private static String CALLBACK_PROJECT_ATTRIBUTE = "CallbackProject";                             // NOI18N

    private static final String EMPTY_WSDL = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" + // NOI18N
            "<definitions \n" + // NOI18N
            "    xmlns=\"http://schemas.xmlsoap.org/wsdl/\" \n" + // NOI18N
            "    xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" \n" + // NOI18N
            "    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" \n" + // NOI18N
            "    xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" \n" + // NOI18N
            ">\n\n\n</definitions>\n"; // NOI18N

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(WsitClientConfigAction.class, "LBL_WsitClientConfigAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(AddModuleAction.class);
    }

    protected void performAction(Node[] activatedNodes) {
        final WSDLEndpointNode node = ((WSDLEndpointNode) activatedNodes[0]); 
        final CasaPort casaPort = (CasaPort) node.getData();

        Port port = null;
        Service service = null;
        Binding binding = null;

        for (Node childNode : node.getChildren().getNodes()) {
            if (childNode instanceof PortNode) {
                port = (Port) ((PortNode) childNode).getWSDLComponent();
                service = (Service) port.getParent();
            } else if (childNode instanceof BindingNode) {
                binding = (Binding) ((BindingNode) childNode).getWSDLComponent();
            }
        }

        // Usability enhancement (#124850)
        if (binding.getExtensibilityElements(Policy.class).size() == 0 &&
                binding.getExtensibilityElements(PolicyReference.class).size() == 0) {
            String title = NbBundle.getMessage(
                    WsitClientConfigAction.class,
                    "TTL_BINDING_CONTAINS_NO_WS_POLICY"); // NOI18N
            String msg = NbBundle.getMessage(
                    WsitClientConfigAction.class,
                    "MSG_BINDING_CONTAINS_NO_WS_POLICY", // NOI18N
                    port.getName(),
                    binding.getName());
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    msg, title, NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return;
            }
        }

        final WSDLModel wsdlModel = (WSDLModel) binding.getModel();
        Collection<Binding> bindings = new HashSet<Binding>();
        bindings.add(binding);

        Collection<FileObject> createdFiles = new LinkedList<FileObject>();
        Project proj = ((CasaWrapperModel) casaPort.getModel()).getJBIProject();
        final WSDLModel clientModel = getModelForClient(proj, wsdlModel, true, createdFiles);
        if (clientModel == null) return;

        // todo: add callback project to lookup...
        // replace the file object in lookup
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo != null) {
            node.removeContent(fo);
        }

        for (CasaExtensibilityElement ee : casaPort.getExtensibilityElements()) {
            QName eeQName = ee.getQName();
            if (eeQName.getNamespaceURI().equals(CALLBACK_PROJECT_NAMESPACE)) {
                String callbackProjName = ee.getAttribute(CALLBACK_PROJECT_ATTRIBUTE);
                if (callbackProjName != null && !callbackProjName.equals("")) { // NOI18N
                    File f = new File(callbackProjName);
                    if (f.exists()) {
                        fo = FileUtil.toFileObject(f);
                        node.addContent(fo);
                    }
                }
            }
        }

        // (See #174952)
        // Not sure how WSIT Config uses the above Callback Project FileObject
        // in the lookup...
        //
        // TransportPanelClient (in websvc.wsitconf) requires the node
        // (WSDLEndpointNode) to have a FileObject or a JAXWSLightSupport
        // in the lookup. TransportPanelClient uses it to find a Project to
        // check for Metro Library.
        //
        // The following temp fix is to make sure some arbitrary FileObject
        // (CASA file in particular) gets put into the node's lookup (if there
        // is no callback project configured for the endpoint) so that at least
        // no NPE occurs when expanding the WSIT Config's Transport panel.
        //          -- Jun 11/12/2009
        fo = node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            FileObject casaFO = node.getModel().getModelSource().getLookup().lookup(FileObject.class);
            node.addContent(casaFO);
        }

        // todo: 08/27/07, add undo manager...
        final UndoManager undoManager = new UndoManager();
        clientModel.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
        final JComponent stc = WSITConfigProvider.getDefault().getWSITClientConfig(service, clientModel, wsdlModel, node);
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

    public WSDLModel getModelForClient(Project p, WSDLModel originalwsdlmodel,
            boolean create, Collection<FileObject> createdFiles) {
        //System.out.println("Calling getModelForClient...");
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
            FileObject configFO = srcFolder.getFileObject(originalWsdlFO.getName(),
                    WSITModelSupport.CONFIG_WSDL_EXTENSION);
            if ((configFO != null) && (configFO.isValid())) {
                return getModelFromFO(configFO, true);
            }

            if (create) {
                // check whether main config file exists
                FileObject mainConfigFO = srcFolder.getFileObject(
                        WSITModelSupport.CONFIG_WSDL_CLIENT_PREFIX,
                        WSITModelSupport.MAIN_CONFIG_EXTENSION);
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

    private static FileObject createMainConfig(FileObject folder,
            Collection<FileObject> createdFiles) {
        FileObject mainConfig = null;
        try {
            mainConfig = FileUtil.createData(folder,
                    WSITModelSupport.CONFIG_WSDL_CLIENT_PREFIX +
                    "." + WSITModelSupport.MAIN_CONFIG_EXTENSION); // NOI18N
            if ((mainConfig != null) && (mainConfig.isValid()) && !(mainConfig.isVirtual())) {
                if (createdFiles != null) {
                    createdFiles.add(mainConfig);
                }
                FileWriter fw = new FileWriter(FileUtil.toFile(mainConfig));
                fw.write(EMPTY_WSDL);
                fw.close();
                mainConfig.refresh(true);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return mainConfig;
    }

    private void copyImports(final WSDLModel model, final FileObject srcFolder,
            Collection<FileObject> createdFiles) throws CatalogModelException {

        // IZ#129853 can not copy from file, when the project wsdl is not yet saved..
        // ...needed to force a save of project wsdl!!!
        FileObject modelFO = Utilities.getFileObject(model.getModelSource());

        try {
            FileObject configFO = FileUtil.copyFile(modelFO, srcFolder,
                    modelFO.getName(), WSITModelSupport.CONFIG_WSDL_EXTENSION);
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
                        newImportsIt.next().setLocation(oldImportFO.getName() + 
                                "." + WSITModelSupport.CONFIG_WSDL_EXTENSION); // NOI18N
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
