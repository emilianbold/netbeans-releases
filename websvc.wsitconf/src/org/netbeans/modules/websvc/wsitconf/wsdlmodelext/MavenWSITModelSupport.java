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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.UndoManager;
import javax.xml.namespace.QName;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.jaxwsruntimemodel.JavaWsdlMapper;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.WSITEditor;
import org.netbeans.modules.websvc.wsitconf.api.DesignerListenerProvider;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfileRegistry;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.security.listmodels.ServiceProviderElement;
import org.netbeans.modules.websvc.wsitconf.util.AbstractTask;
import org.netbeans.modules.websvc.wsitconf.util.SourceUtils;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ServiceProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class MavenWSITModelSupport {
    
    private static final Logger logger = Logger.getLogger(MavenWSITModelSupport.class.getName());
    
    /** Creates a new instance of MavenWSITModelSupport */
    public MavenWSITModelSupport() { }
    
    public static WSDLModel getModel(Node node, Project project, JAXWSLightSupport jaxWsSupport, JaxWsService jaxService, UndoManagerHolder umHolder, boolean create, Collection<FileObject> createdFiles) throws MalformedURLException, Exception {

        WSDLModel model = null;
        boolean isClient = !jaxService.isServiceProvider();

        if (isClient) {
            model = getModelForClient(project, jaxWsSupport, jaxService, create, createdFiles);
        } else {  //it is a service
            FileObject implClass = node.getLookup().lookup(FileObject.class);
            try {
                String wsdlUrl = jaxService.getWsdlUrl();
                if (wsdlUrl == null) { // WS from Java
                    if ((implClass == null) || (!implClass.isValid() || implClass.isVirtual())) {
                        logger.log(Level.INFO, "Implementation class is null or not valid, or just virtual: " + implClass + ", service: " + jaxService);
                        return null;
                    }
                    return getModelForServiceFromJava(implClass, jaxWsSupport, create, createdFiles);
                } else {
                    if (project == null) return null;
                    return getModelForServiceFromWsdl(jaxWsSupport, jaxService);
                }
            } catch (Exception e) {
                logger.log(Level.INFO, null, e);
            }
        }

        if ((model != null) && (umHolder != null) && (umHolder.getUndoManager() == null)) {
            UndoManager undoManager = new UndoManager();
            model.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
            umHolder.setUndoManager(undoManager);
        }
        return model;
    }

    /* Retrieves WSDL model for a WS client - always has a wsdl
     */
    public static WSDLModel getModelForClient(Project p, JAXWSLightSupport jaxWsSupport, JaxWsService jaxWsService, boolean create, Collection<FileObject> createdFiles) throws IOException {
        
        WSDLModel model = null;
        FileObject srcFolder = WSITEditor.getClientConfigFolder(p);
        FileObject catalogfo = Utilities.getProjectCatalogFileObject(p);
        ModelSource catalogms = Utilities.getModelSource(catalogfo, true);
        
//        try {
//            CatalogModel cm = Utilities.getCatalogModel(catalogms);
//            ModelSource originalms = cm.getModelSource(URI.create(jaxWsService.getWsdlUrl()));
//            FileObject originalWsdlFO = Utilities.getFileObject(originalms);
//            WSDLModel originalwsdlmodel = getModelFromFO(originalWsdlFO, true);
//
//            // check whether config file already exists
//            FileObject configFO = srcFolder.getFileObject(originalWsdlFO.getName(), CONFIG_WSDL_EXTENSION);
//            if ((configFO != null) && (configFO.isValid())) {
//                return getModelFromFO(configFO, true);
//            }
//
//            if (create) {
//                // check whether main config file exists
//                FileObject mainConfigFO = srcFolder.getFileObject(CONFIG_WSDL_CLIENT_PREFIX, MAIN_CONFIG_EXTENSION);
//                if (mainConfigFO == null) {
//                    mainConfigFO = createMainConfig(srcFolder, createdFiles);
//                }
//
//                copyImports(originalwsdlmodel, srcFolder, createdFiles);
//
//                // import the model from client model
//                WSDLModel mainModel = getModelFromFO(mainConfigFO, true);
//                mainModel.startTransaction();
//                try {
//                    WSDLComponentFactory wcf = mainModel.getFactory();
//
//                    FileObject configName = Utilities.getFileObject(originalwsdlmodel.getModelSource());
//                    configFO = srcFolder.getFileObject(configName.getName(), CONFIG_WSDL_EXTENSION);
//
//                    boolean importFound = false;
//                    Collection<Import> imports = mainModel.getDefinitions().getImports();
//                    for (Import i : imports) {
//                        if (i.getLocation().equals(configFO.getNameExt())) {
//                            importFound = true;
//                            break;
//                        }
//                    }
//                    model = getModelFromFO(configFO, true);
//                    if (!importFound) {
//                        org.netbeans.modules.xml.wsdl.model.Import imp = wcf.createImport();
//                        imp.setLocation((configFO).getNameExt());
//                        imp.setNamespace(model.getDefinitions().getTargetNamespace());
//                        Definitions def = mainModel.getDefinitions();
//                        def.setName("mainclientconfig"); //NOI18N
//                        def.addImport(imp);
//                    }
//                } finally {
//                    mainModel.endTransaction();
//                }
//
//                DataObject mainConfigDO = DataObject.find(mainConfigFO);
//                if ((mainConfigDO != null) && (mainConfigDO.isModified())) {
//                    SaveCookie wsdlSaveCookie = mainConfigDO.getCookie(SaveCookie.class);
//                    if(wsdlSaveCookie != null){
//                        wsdlSaveCookie.save();
//                    }
//                    mainConfigDO.setModified(false);
//                }
//
//                DataObject configDO = DataObject.find(configFO);
//                if ((configDO != null) && (configDO.isModified())) {
//                    SaveCookie wsdlSaveCookie = configDO.getCookie(SaveCookie.class);
//                    if(wsdlSaveCookie != null){
//                        wsdlSaveCookie.save();
//                    }
//                    configDO.setModified(false);
//                }
//            }
//
//        } catch (CatalogModelException ex) {
//            logger.log(Level.INFO, null, ex);
//        }
        
        return model;
    }
    
//    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
//     */
//    public static WSDLModel getServiceModelForClient(JAXWSClientSupport supp, Client client) throws IOException, Exception {
//        FileObject originalWsdlFolder = supp.getLocalWsdlFolderForClient(client.getName(), false);
//        FileObject originalWsdlFO = originalWsdlFolder.getFileObject(client.getLocalWsdlFile());
//
//        if ((originalWsdlFO != null) && (originalWsdlFO.isValid())) {   //NOI18N
//            return getModelFromFO(originalWsdlFO, true);
//        }
//        return null;
//    }
    
    private static WSDLModel getModelForServiceFromWsdl(JAXWSLightSupport supp, JaxWsService service) throws IOException, Exception {
        String wsdlLocation = service.getLocalWsdl();
        FileObject wsdlFO = supp.getWsdlFolder(false).getFileObject(wsdlLocation);
        return WSITModelSupport.getModelFromFO(wsdlFO, true);
    }
    
    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */
    public static WSDLModel getModelForServiceFromJava(FileObject jc, JAXWSLightSupport supp, boolean create, Collection<FileObject> createdFiles) throws IOException {
        
        WSDLModel model = null;
        String configWsdlName = WSITModelSupport.CONFIG_WSDL_SERVICE_PREFIX;
        
        try {
            if (jc == null) return null;
            final java.lang.String[] result = new java.lang.String[1];
            
            JavaSource js = JavaSource.forFileObject(jc);
            js.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws java.io.IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    SourceUtils sourceUtils = SourceUtils.newInstance(controller);
                    if (sourceUtils != null) {
                        result[0] = sourceUtils.getTypeElement().getQualifiedName().toString();
                    }
                }
            }, true);
            
            configWsdlName += result[0];
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        // check whether config file already exists
        FileObject wsdlFolder = supp.getWsdlFolder(create);
        if ((wsdlFolder != null) && (wsdlFolder.isValid())) {
            FileObject wsdlFO = wsdlFolder.getParent().getFileObject(configWsdlName, WSITModelSupport.CONFIG_WSDL_EXTENSION);  //NOI18N
            if ((wsdlFO != null) && (wsdlFO.isValid())) {   //NOI18N
                return WSITModelSupport.getModelFromFO(wsdlFO, true);
            }
        }
        
        if (create) {
            // config file doesn't exist - generate empty file
            FileObject wsdlFO = wsdlFolder.getParent().getFileObject(configWsdlName, WSITModelSupport.CONFIG_WSDL_EXTENSION);   //NOI18N
            if ((wsdlFO == null) || !(FileUtil.toFile(wsdlFO).exists())) {
                wsdlFO = wsdlFolder.getParent().createData(configWsdlName, WSITModelSupport.CONFIG_WSDL_EXTENSION);  //NOI18N
                if (createdFiles != null) {
                    createdFiles.add(wsdlFO);
                }
                FileWriter fw = new FileWriter(FileUtil.toFile(wsdlFO));
                fw.write(NbBundle.getMessage(WSITEditor.class, "EMPTY_WSDL"));       //NOI18N
                fw.close();
                wsdlFO.refresh(true);
            }
            
            // and fill it with values
            model = WSITModelSupport.createModelFromFO(wsdlFO, jc);
            wsdlFO.refresh(true);
            
            DesignerListenerProvider.configCreated();
        }
        
        return model;
    }
    
    public static Binding getBinding(JaxWsService service, FileObject implClass, Project project, boolean create, Collection<FileObject> createdFiles) {
        String portName = service.getPortName();
        String serviceName = service.getServiceName();
        if (serviceName == null) {
            QName serviceQ = JavaWsdlMapper.getServiceName(implClass);
            if (serviceQ != null) {
                serviceName = serviceQ.getLocalPart();
            }
        }
        if (serviceName == null) return null;
//        WSDLModel model = getModelForService(service, implClass, project, create, createdFiles);
//        if (model == null) return null;
//        Definitions definitions = model.getDefinitions();
//        if (definitions == null) return null;
//        Collection<Binding> bindings = definitions.getBindings();
//        if ((bindings == null) || (bindings.isEmpty())) return null;
//        if (bindings.size() == 1) return bindings.iterator().next();
//        Collection<org.netbeans.modules.xml.wsdl.model.Service> services = definitions.getServices();
//        for (org.netbeans.modules.xml.wsdl.model.Service s : services) {
//            if (s == null) continue;
//            if (serviceName.equals(s.getName())) {
//                Collection<Port> ports = s.getPorts();
//                if (portName == null) {
//                    if ((ports != null) && (!ports.isEmpty())) {
//                        Port p = ports.iterator().next();
//                        QName b = p.getBinding().getQName();
//                        return model.findComponentByName(b, Binding.class);
//                    }
//                } else {
//                    for (Port p : ports) {
//                        if (portName.equals(p.getName())) {
//                            QName b = p.getBinding().getQName();
//                            return model.findComponentByName(b, Binding.class);
//                        }
//                    }
//                }
//            }
//        }
        return null;
    }

}
