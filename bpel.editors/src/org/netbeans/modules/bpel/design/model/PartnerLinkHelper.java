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


package org.netbeans.modules.bpel.design.model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import org.netbeans.modules.bpel.design.model.patterns.CollapsedPattern;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.connections.MessageConnection;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.schema.model.Schema;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.ErrorManager;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author Alexey Yarmolenko
 */
public class PartnerLinkHelper {
    
    
    private DiagramModel model;
    /**
     * Creates a new instance of PartnerlinkHelper
     */
    
    public PartnerLinkHelper(DiagramModel model) {
        this.model = model;
    }
    
    
    public void updateMessageFlowLinks(CollapsedPattern collapsedPattern) {
        VisualElement element = collapsedPattern.getFirstElement();
        
        List<Connection> connections = element.getAllConnections();
        
        for (Connection c : connections){
            if (c instanceof MessageConnection){
                c.remove();
            }
        }
        
        updateMessageFlowLinks(collapsedPattern.getOMReference(), element);
    }
    
    
    private void updateMessageFlowLinks(BpelEntity entity, VisualElement element) 
    {
        if (entity instanceof OperationReference &&
                entity instanceof PartnerLinkReference) {
            List<Connection> conns = element.getAllConnections();
            
            VisualElement op = getOperation(entity);
            
            if ( op != null ){
                Connection c = new MessageConnection(element.getPattern());
                if (entity instanceof OnMessage ||
                        entity instanceof OnEvent ||
                        entity instanceof Receive) {
                    c.connect(op, Direction.RIGHT,
                            element, Direction.LEFT);
                } else if (entity instanceof Invoke
                        || entity instanceof Reply) {
                    c.connect(element, Direction.LEFT,
                            op, Direction.RIGHT);
                }
            }            
        } else {
            List<BpelEntity> children = entity.getChildren();
            
            if (children != null) {
                for (BpelEntity child : children) {
                    updateMessageFlowLinks(child, element);
                }
            }
        }
    }
    
    
    /**
     *Iterates over diagram subtree and adds message flow links to diagram elements
     *@pattern is the root of pattern subtree to process
     **/
    public void updateMessageFlowLinks(Pattern pattern){
        Object omRef = pattern.getOMReference();
        
        if ( omRef instanceof OperationReference &&
                omRef instanceof PartnerLinkReference){
            //remove all old links
            VisualElement elem = pattern.getFirstElement();
            List<Connection> conns = elem.getAllConnections();
            for (Connection c: conns){
                if (c instanceof MessageConnection){
                    c.remove();
                }
            }
            
            //establish new link
            VisualElement op = getOperation((BpelEntity) omRef);
            
            if ( op != null ){
                
                Connection c = new MessageConnection(pattern);
                if (omRef instanceof OnMessage ||
                        omRef instanceof OnEvent ||
                        omRef instanceof Receive) {
                    c.connect(op, Direction.RIGHT,
                            pattern.getFirstElement(), Direction.LEFT);
                } else if (omRef instanceof Invoke
                        || omRef instanceof Reply) {
                    c.connect(pattern.getFirstElement(), Direction.LEFT,
                            op, Direction.RIGHT);
                    
                }
            }
            
        }
        
    }
    
    /**
     *Returns the VisualElement representing Operation used by given activity
     */
    private VisualElement getOperation(BpelEntity entity){
        
        assert entity instanceof OperationReference;
        assert entity instanceof PartnerLinkReference;
        
        WSDLReference<Operation> op_ref = ((OperationReference) entity).getOperation();
        if (op_ref == null){
            return null;
        }
        
        BpelReference<PartnerLink> pl_ref = ((PartnerLinkReference) entity).getPartnerLink();
        if (pl_ref == null){
            return null;
        }
        
        PartnerLink pl = pl_ref.get();
        if (pl == null){
            return null;
        }
        
        Pattern pattern = model.getPattern(pl);

        if (pattern == null) {
            return null;
        } else if (pattern instanceof CollapsedPattern) {
            return pattern.getFirstElement();
        } 
        
        return ((PartnerlinkPattern) pattern).getElement(op_ref);
    }
    /** That method is invoked to retrieve wsdl and
     * related resources hby given URL.
     *
     * WSDL is expected to belong to J2EE WS project having
     * one (and only one) PortType.
     *Partner link will be created for that portType
     **/
    
//    public void createPartnerLink(final URL wsdlURL){
//        if (wsdlURL == null) {
//            return;
//        }
//        RequestProcessor.getDefault().post(new Runnable() {
//            public void run() {
//                final String name = getServiceName(wsdlURL);
//
//                final FileObject wsdl_fo = retrieveWSDL(wsdlURL, name);
//
//                if (wsdl_fo == null){
//                    return;
//                }
//
//                WSDLModel wsdl_model = null;
//
//                try {
//                    wsdl_model = model.getView().getProcessHelper().getWSDLModelFromUri(wsdl_fo.getURL().toURI());
//                } catch (FileStateInvalidException ex) {
//                    ex.printStackTrace();
//                } catch (URISyntaxException ex) {
//                    ex.printStackTrace();
//                }
//
//
//                if (wsdl_model == null || wsdl_model.getDefinitions() == null){
//                    return;
//                }
//
//
//
//                wsdl_model.startTransaction();
//                try{
//                    Definitions defs = wsdl_model.getDefinitions();
//
//                    Collection<PortType> pts = defs.getPortTypes();
//
//                    if (pts == null || pts.size() != 1){
//                        return;
//                    }
//
//                    PortType pt = pts.iterator().next();
//
//                    BPELComponentFactory factory = new BPELComponentFactory(wsdl_model);
//
//                    final PartnerLinkType plt = factory.createPartnerLinkType(defs);
//
//
//                    defs.addExtensibilityElement(plt);
//
//                    plt.setName(name);
//
//                    final Role role = factory.createRole(defs);
//                    plt.setRole1(role);
//
//                    role.setName(name + "Provider");
//                    role.setPortType(role.createReferenceTo(pt, PortType.class));
//
//                    final BpelModel bpelModel = model.getView().getBPELModel();
//                    try {
//
//                        bpelModel.invoke( new Callable() {
//                            public Object call() throws Exception {
//                                PartnerLink pl = bpelModel
//                                        .getBuilder()
//                                        .createPartnerLink();
//
//
//                                try {
//                                    pl.setName(name + "Link");
//
//                                } catch (VetoException ex) {
//                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
//                                }
//                                pl.setPartnerLinkType(pl.createWSDLReference(plt, PartnerLinkType.class));
//
//                                pl.setPartnerRole(pl.createWSDLReference(role, Role.class));
//                                PartnerLinkContainer plc = bpelModel.getProcess().getPartnerLinkContainer();
//                                if (plc == null){
//                                    plc = bpelModel.getBuilder().createPartnerLinkContainer();
//                                    bpelModel.getProcess().setPartnerLinkContainer(plc);
//                                }
//                                plc.insertPartnerLink(pl, 0);
//
//
//                                new ImportRegistrationHelper(bpelModel).addImport(wsdl_fo);
//                                return pl;
//                            }
//                        }, null);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//
//
//                } finally {
//                    wsdl_model.endTransaction();
//                }
//            }
//        });
//
//
//    }
    public  PartnerLink createPartnerLink(DataObject dataObj){
        try {
            
            FileObject fo = dataObj.getPrimaryFile();
            if (fo == null){
                return null;
            }
            URI uri = fo.getURL().toURI();
            final String name = fo.getName();
            
            
            // Start Change to use BusinessProcessHelper
            WSDLModel wsdlModel = model.getView().getProcessHelper().
                    getWSDLModelFromUri(uri);
            
            
            
            if (wsdlModel == null){
                return null;
            }
            
            final List<PartnerLinkType> pltList = wsdlModel.getDefinitions().
                    getExtensibilityElements(PartnerLinkType.class);
            
            
            if (pltList == null || pltList.size() == 0){
                return null;
            }
            
            final BpelModel bpelModel = model.getView().getBPELModel();
            assert (bpelModel != null):"Broken WSDL model"; //NOI18N
            
            
            //this callable contains default befavior(use first PLT)
            //it will be used if no valid plt will be found
            Callable<PartnerLink> createPLCallable = new Callable<PartnerLink>() {
                public PartnerLink call() throws Exception {
                    PartnerLink pl = bpelModel.getBuilder().createPartnerLink();
                    pl.setName(name);
                    pl.setPartnerLinkType(pl.createWSDLReference(pltList.get(0),PartnerLinkType.class));
                    return pl;
                }
            };
            
            return bpelModel.invoke(createPLCallable, this);
        } catch( Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    
    /**
     * Detects if given PortType is mentioned in at least one service service element
     **/
    public PartnerLink createPartnerLink(){
        
        return model
                .getView()
                .getBPELModel()
                .getBuilder()
                .createPartnerLink();
        
    }
    
    private boolean isServiceRole(Role role){
        
        if (role == null || role.getPortType() == null){
            return false;
        }
        
        PortType pt = role.getPortType().get();
        if (pt == null) {
            return false;
        }
        
        WSDLModel wsdlModel = pt.getModel();
        
        
        Collection<Service> services = wsdlModel.getDefinitions().getServices();
        
        if (services == null){
            return false;
        }
        
        for (Service service : services) {
            for (Port port : service.getPorts()) {
                Binding b = port.getBinding().get();
                if (b == null){
                    continue;
                }
                PortType t = b.getType().get();
                if (t != null && pt.equals(t)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static void saveModel(Model model){
        DataObject model_do = (DataObject) model
                .getModelSource()
                .getLookup()
                .lookup(DataObject.class);
        
        if (model_do != null){
            SaveCookie sc = (SaveCookie) model_do.getCookie(SaveCookie.class);
            
            if (sc != null){
                try {
                    sc.save();
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
    public FileObject retrieveWSDL(URL url, String name, boolean retrieveToFlat ){
        // String name = getServiceName(url);
        FileObject bpel_fo = (FileObject) model
                .getView()
                .getBPELModel()
                .getModelSource()
                .getLookup()
                .lookup(FileObject.class);
        
        if (bpel_fo == null) {
            return null;
        }
        FileObject parent = bpel_fo.getParent();
        
        
        FileObject partners_folder = parent.getFileObject(PARTNERS_FOLDER);
        
        if (partners_folder  == null) {
            try {
                partners_folder = parent.createFolder(PARTNERS_FOLDER);
            } catch (IOException ex) {
                return null;
            }
        }
        
        FileObject service_folder = partners_folder.getFileObject(name);
        
        if (service_folder == null){
            try {
                
                service_folder = partners_folder.createFolder(name);
            } catch (IOException ex) {
                return null;
            }
        }
        
        FileObject wsdl_fo = null;
        WSDLModel wsdl_model = null;
        try {
            Retriever retr = Retriever.getDefault();
            retr.setOverwriteFilesWithSameName(true);
            wsdl_fo  = (retrieveToFlat)?
                retr.retrieveResourceClosureIntoSingleDirectory(service_folder, url.toURI())
                : retr.retrieveResource(service_folder, url.toURI());
            
            
            if (wsdl_fo == null){
                return null;
            }
            
            WSDLModel model = getWSDLModel(wsdl_fo);
            
            model.sync();
            
            if (model != null){
                fixSoapAddress(model, retrieveToFlat);
            }
        } catch (UnknownHostException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (URISyntaxException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return wsdl_fo;
        
    }
    /**
     */
    private void fixSoapAddress(WSDLModel model, boolean retrieveToFlat) {
        
        Definitions defs = model.getDefinitions();
        if (defs == null){
            return;
        }
        
        model.startTransaction();
        try {
            
            
            Collection<Service> svcs = defs.getServices();
            if (svcs == null){
                return;
            }
            for (Service svc: svcs){
                Collection<Port> ports = svc.getPorts();
                if (ports == null){
                    continue;
                }
                for (Port port: ports){
                    Collection<SOAPAddress> addrs =
                            port.getExtensibilityElements(SOAPAddress.class);
                    
                    for (SOAPAddress addr: addrs){
                        URL url;
                        try {
                            url = new URL(addr.getLocation());
                            
                            if(!isExternalResource(url)){
                                url = new URL( url.getProtocol(),
                                        "localhost",
                                        url.getPort(),
                                        url.getFile());
                                
                                addr.setLocation(url.toString());
                            }
                            
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                        }
                        
                    }
                }
            }
            if (retrieveToFlat) {
                //fix schema imports
                Types types = defs.getTypes();
                if (types != null){
                    Collection<Schema> schemas = types.getSchemas();
                    if (schemas != null){
                        for(Schema s: schemas){
                            Collection<org.netbeans.modules.xml.schema.model.Import> imps =
                                    s.getImports();
                            for(org.netbeans.modules.xml.schema.model.Import imp: imps){
                                String location = imp.getSchemaLocation();

                                if(location == null){
                                    continue;
                                }
                                if (isExternalResource(location)){
                                    continue;
                                }
                                String new_location = fixLocation(model, location);//getFileName(location);

                                if (new_location != null) {
                                    imp.setSchemaLocation(new_location);
                                }
                            }
                        }
                    }
                }
                //fix other WSDL imports
                Collection<Import> imports = defs.getImports();
                for (Import imp: imports){
                    String location = imp.getLocation();
                    if (location == null){
                        continue;
                    }
                    
                    if (isExternalResource(location)) {
                        continue;
                    }
                    
                    String new_location = getFileName(location);
                    if ( new_location != null){
                        imp.setLocation(new_location);
                    }
                    
                }
            }
        } finally {
            model.endTransaction();
        }
        saveModel(model);
        
        Collection<Import> imports = defs.getImports();
        for (Import imp: imports){
            WSDLModel m = null;
            try {
                String location = imp.getLocation();
                
                if (location != null && !isExternalResource(location)){
                    m = imp.getImportedWSDLModel();
                }
                
            } catch (CatalogModelException ex) {
            }
            if (m != null){
                fixSoapAddress(m, retrieveToFlat);
            }
        }
    }

    // vlv
    private String fixLocation(WSDLModel model, String location) {
      FileObject file = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
      CatalogWriteModel catalog;
      
      try {
        catalog = CatalogWriteModelFactory.getInstance().getCatalogWriteModelForProject(file);
      }
      catch (CatalogModelException e) {
        return null;
      }
      Collection<CatalogEntry> entries = catalog.getCatalogEntries();

      for (CatalogEntry entry : entries) {
        if (location.equals(entry.getSource())) {
          String value = entry.getTarget();
          int k = value.lastIndexOf('/');
  
          if (k != -1) {
            value = value.substring(k + 1);
          }
          return value;
        }
      }
      return null;
    }

    public static String getFileName(String  filepath){
        
        if (filepath == null) {
            return null;
        }
        int pos = filepath.lastIndexOf('/');
        if (pos >= 0){
            filepath = filepath.substring(pos + 1);
        }
        
        pos = filepath.indexOf('?');
        if (pos >=0){
            filepath = filepath.substring(0, pos);
        }
        return filepath;
    }
    
    public static boolean isExternalResource(String url){
        try {
            return isExternalResource(new URL(url));
        } catch (MalformedURLException ex) {
            
        }
        return false;
        
    }
    public static boolean isExternalResource(URL url){
        if (url == null || url.getHost() == null){
            return false;
        }
        InetAddress inet_addr;
        try {
            inet_addr = InetAddress.getByName(url.getHost());
            
            if (inet_addr == null){
                return false;
            }
            
            return (NetworkInterface.getByInetAddress(inet_addr) == null);
            
        } catch (SocketException ex) {
        } catch (UnknownHostException ex) {
        }
        return false;
    }
    
    private WSDLModel getWSDLModel(FileObject fo){
        try {
            return model.getView().getProcessHelper().getWSDLModelFromUri(fo.getURL().toURI());
        } catch (FileStateInvalidException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch (URISyntaxException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return null;
    }
    private String getServiceName(URL url){
        
//        final String SUFFIX = "Service";
        String name = url.getPath();
        int pos = name.lastIndexOf('/');
        if (pos > -1){
            name =  name.substring(pos + 1);
        }
        
        
//        if (!name.toLowerCase().endsWith(SUFFIX.toLowerCase())){
//            name = name + SUFFIX;
//        }
//
        return name;
        
    }
    
    
    private static final String PARTNERS_FOLDER = "Partners";
//    /**
//     * Calculates the QName for any referencable WSDL Component
//     */
//    public static QName getQName(WSDLComponent component) {
//        String namespace =
//                component.getModel().getDefinitions().getTargetNamespace();
//        if (component instanceof Named) {
//            QName pltQName = new QName(namespace, ((Named)component).getName());
//            return pltQName;
//        }
//        return null;
//    }
//
//
    
}
