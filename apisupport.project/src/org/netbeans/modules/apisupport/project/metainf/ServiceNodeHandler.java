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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.apisupport.project.metainf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Wraps the META-INF/services node in Important Files node 
 * @author pzajac
 */
public final class ServiceNodeHandler  {
     private static final String KEY_WAIT = "wait"; // NOI18N
    
    private static final String THIS_SERVICES = NbBundle.getMessage(ServiceNodeHandler.class,"LBL_this_services");
    private static final String THIS_SERVICES_IN_CONTEXT = NbBundle.getMessage(ServiceNodeHandler.class,"LBL_this_services_in_context");
    static final String ROOT_NODE_NAME = NbBundle.getMessage(ServiceNodeHandler.class,"LBL_META_INF_services");
    // All services in platform
    TreeMap /* service class -> List of classes */ allServicesMap ; 
    // services in module
    TreeMap /* service class -> List of classes */moduleServiceMap;
    
    int prevAllServicesCount = -1;
    int prevModuleServicesCount = -1;
    
    final Project project;
    final NbModuleProvider info;
    List /*Service*/ moduleServices;
    
    /** services in this module
     */
    ServiceRootChildren moduleChild ;
    /** services in context
     */
    ServiceRootChildren allInContextChild;
    boolean registeredListener;
    /** holds reference to META-INF/services folder 
     */
    private FileObject metaInfServicesFo;    
    /** cached codebase name
     */
    private String codeNameBase;
    /** Children for services list
     */
    class ServiceRootChildren extends Children.Keys {
        boolean fullyComputed = false;
        /** show services  of this project or platfrom
         */
        private final boolean bProjectServices ;
        
        ServiceRootChildren(boolean bProjectServices) {
            this.bProjectServices = bProjectServices;
        }
        protected Node[] createNodes(Object key) {
            // synchronize access to allServicesMap and moduleServices
            if (key == KEY_WAIT) {
                return new Node[] {new AbstractNode(Children.LEAF) {
                    public String getName() {
                        return KEY_WAIT;
                    }
                    public String getDisplayName() {
                        return NbBundle.getMessage(ServiceNodeHandler.class,"LBL_ServiceNode_please_wait");
                    }
                    public Action[] getActions(boolean context) {
                        return new Action[0];
                    } 
                }};
            } else if (key instanceof String ) {
                Node parent = getNode(); 
                String parentName = parent.getName();
                boolean isThisModule = parentName == THIS_SERVICES;
                ServiceNode node = new ServiceNode((String)key,isThisModule);
                return new Node[] {node};
            } else {
                throw new AssertionError(key);
            }
            
            
        }
        
        protected void addNotify() {
            SUtil.log(SUtil.LOG_SERVICE_NODE_HANDLER_ADD_NOTIFY);
            super.addNotify();
            if (fullyComputed) {
                Object keys[] = null;
                if (bProjectServices) {
                    // only services from this project
                    keys = moduleServiceMap.keySet().toArray();
                    prevModuleServicesCount = moduleServiceMap.keySet().size();
                    SUtil.log(SUtil.LOG_SET_KEYS);
                    setKeys(keys);
                } else {
                    keys = allServicesMap.keySet().toArray();
                    prevAllServicesCount = allServicesMap.keySet().size();
                    setKeys(keys);
                }
            } else {
                SUtil.log(SUtil.LOG_COMPUTE_KEYS);
                setKeys(new Object[] {KEY_WAIT});
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            Object keys[] = null;
                            // synchronize access to TreeMaps
                            // categorize services
                            synchronized(ServiceNodeHandler.this) {
                                if (moduleServiceMap == null) {
                                    moduleServiceMap = new TreeMap();
                                    moduleServices = Service.getOnlyProjectServices(project);
                                    sortServices(moduleServiceMap, moduleServices);
                                }                                
                                if (bProjectServices) {
                                    // only services from this project
                                    keys = moduleServiceMap.keySet().toArray();
                                    prevModuleServicesCount = moduleServiceMap.keySet().size();
                                } else {
                                    if (allServicesMap == null) {
                                        allServicesMap = new TreeMap();
                                        List /*Service*/ services = ServiceViewUpdater.getAllServices(ServiceNodeHandler.this);
                                        if (services != null) {
                                            assert moduleServiceMap!=null;
                                            sortServices(allServicesMap, services);
                                        }
                                    }
                                    prevAllServicesCount = allServicesMap.keySet().size();
                                    keys = allServicesMap.keySet().toArray();
                                    if (keys.length > 0) {
                                        SUtil.log(keys[0].toString());
                                    }
                                }
                                
                            }
                            setKeys(keys);
                            synchronized (ServiceNodeHandler.this) {
                                // tell the test that it is initialized
                                fullyComputed = true;
                                SUtil.log(SUtil.LOG_END_COMPUTE_KEYS);
                            }
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    } // run
                    
                    
                    
                    private void sortServices(final TreeMap map, final List services) {
                        //               sortServices(map,services);
                        for (Iterator it = services.iterator() ; it.hasNext();) {
                            Service service = (Service) it.next();
                            assert map != null;
                            List theSameServices = (List) map.get(service.getFileName());
                            if (theSameServices == null) {
                                theSameServices = new ArrayList();
                                map.put(service.getFileName(),theSameServices);
                            }
                            theSameServices.add(service);
                        }
                    }
                }); // runnable
            } // else
        }
        
        void refreshKeys() {
            if (bProjectServices) {
                setKeys(moduleServiceMap.keySet());
                prevModuleServicesCount = moduleServiceMap.size();
            } else {
                setKeys(allServicesMap.keySet());
                prevAllServicesCount = allServicesMap.size();
            }
            
        }
        
        public void updateNode(String keyName) {
             Node nodes[] = this.getNodes() ;
             for (int nIt = 0 ; nIt < nodes.length; nIt++) {
                ServiceNode n = (ServiceNode)nodes[nIt];
                if (n.getName().equals(keyName)) {   
                    n.refreshName();
                    ((ServiceNodeChildren)n.getChildren()).nodesChanged();
                } 
             }
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
    }
    class ServiceClassKey {
        String name;
        boolean bRemoved;
        ServiceClassKey(String name,boolean bRemoved) {
            this.name = name;
            this.bRemoved = bRemoved;
        }

        public int hashCode() {
            return name.hashCode();
        }
        
        public boolean equals(Object obj) {
            return  (obj instanceof ServiceClassKey) && ((ServiceClassKey)obj).name.equals(name);
        }

    }
    
    class  ServiceNodeChildren extends Children.Keys  {
        private boolean isThisModule;
        /** className -> ServiceClassKey 
         */
        private TreeMap keys;
        boolean initialized = true;
        
        ServiceNodeChildren(boolean isThisModule) {
            this.isThisModule = isThisModule;
//            setKeys(new Object[]{KEY_WAIT});
        }
        private TreeMap getKeysMap () {
            if (keys == null) {
                keys = new TreeMap(); 
            }
            return keys;
        }
        
        private ServiceClassKey addKey(ServiceClassKey key,TreeMap newKeyMap) {
              TreeMap keys = getKeysMap();
              ServiceClassKey oldKey = (ServiceClassKey)keys.get(key.name); 
              if (oldKey == null) {
                  oldKey = key;
              } else if (oldKey.bRemoved != key.bRemoved) {
                  oldKey.bRemoved = key.bRemoved;
                  refreshKey(oldKey);
              }
              newKeyMap.put(key.name,oldKey); 
              return oldKey;
        }
        
         protected void addNotify() {
            ServiceNode serviceNode = (ServiceNode) getNode();
            isThisModule = serviceNode.isThisModule();
            List servicesGroup  = (List) ((isThisModule) ? moduleServiceMap.get(serviceNode.getName()) : 
                                                                  allServicesMap.get(serviceNode.getName()));
           
            List classes  = new ArrayList();
            List maskedClasses = new ArrayList();
            
            Service service = null;
            TreeMap newKeyMap = new TreeMap();
            // creates two groups - classes and masked class
            //
            for (Iterator sIt = servicesGroup.iterator(); sIt.hasNext() ; ) {
                service = (Service) sIt.next();
                for (Iterator ssIt = service.getClasses().iterator() ; ssIt.hasNext() ; ) {
                    String name = (String)ssIt.next();
                    if (name.charAt(0) == '-') {
                        maskedClasses.add(name);
                    } else {
                        classes.add(name);
                    }
                }
            }
            
            // create nodes from classes
            //
            int i;
            for (i = 0 ; i < classes.size() ; i++) {
                String name = (String)classes.get(i);
                ServiceClassKey key = new ServiceClassKey(name,false);
                if (!isThisModule) {
                    String filteredName = '-' + name;
                    // register class like masked
                    for (int fIt = 0 ; fIt < maskedClasses.size() ; fIt++) {
                        if (maskedClasses.get(fIt).equals(filteredName)) {
                            key.bRemoved = true;
                        }
                    }
                }
                key = addKey(key,newKeyMap);

            }
            // show element which masks services in this module view
            //
            if (isThisModule) {
                for ( int j = 0; j < maskedClasses.size() ; j++ ) {
                    addKey(new ServiceClassKey((String)maskedClasses.get(j),false),newKeyMap);
                }
            }
            this.keys = newKeyMap;
            setKeys(getKeysMap().values());
            initialized = true;
        }
        protected Node[] createNodes(Object key) {
            ServiceClassKey classKey = (ServiceClassKey)key;
            ServiceClassNode node = new ServiceClassNode(classKey.name,classKey.bRemoved);
            return new Node[] {node};
        } 

        synchronized  void nodesChanged() {
            if (initialized) {
                addNotify();
            }
            
        }
    }
    /*** the service super class node
     */
    public final  class ServiceNode extends AbstractNode {
        ServiceNode(String name,boolean isThisModule) {
            super(new ServiceNodeChildren(isThisModule));
            setName(name);
            setIconBaseWithExtension("org/netbeans/modules/apisupport/project/metainf/interface.png");
        }
        public void updateChildren() {
              ((ServiceNodeChildren) getChildren()).nodesChanged();
              fireDisplayNameChange(null,null);
        }
        boolean isThisModule() {
            return (getParentNode() == null) ? false : ( getParentNode().getName() == THIS_SERVICES);
        }
        public String getHtmlDisplayName() {
            List services = (List) moduleServiceMap.get(getName());
            
            return  (services != null && !isThisModule()) ? "<b>" + getName() + "</b>" : getName(); //NOI18N
        }

        public Action[] getActions(boolean context) {
            return new Action[] {AddService.getInstance()};
        }
        
        /** create new service (also creates new file)
         */
        void addService(String serviceName, String classServiceName) {
            List services = (List)allServicesMap.get(serviceName);
            boolean exists = false;
            for (int sIt = 0 ; sIt < services.size() ; sIt++ ) {
                Service service = (Service) services.get(sIt);
                List classes = service.getClasses();
                for (int cIt = 0 ; cIt < classes.size() ; cIt++) {
                    String className = (String) classes.get(cIt);
                    if (classServiceName.equals(className)) {
                        // already exist
                        exists = true;
                        NotifyDescriptor.Message msgDesc = 
                                new NotifyDescriptor.Message(NbBundle.getMessage(ServiceNodeHandler.class,"MSG_ServiceExist",className));
                        DialogDisplayer.getDefault().notify(msgDesc);
                    }
                }
            }
            if (!exists) {
               services = (List)moduleServiceMap.get(serviceName);
               Service service = null;
               if (services != null && services.size() > 0) {
                   service = (Service) services.get(0);
               } else {
                   service = new Service(getInfo().getCodeNameBase(),serviceName,new ArrayList());
               }
               service.getClasses().add(classServiceName);
               service.write(project);  
            }
        }

        void refreshName() {
            fireDisplayNameChange(null,null);
        }
        
        Project getProject() {
            return project;
        }
        
        NbModuleProvider getInfo() {
            return project.getLookup().lookup(NbModuleProvider.class);
        }
        
    }
    
    /** leaf of services - a node for a class  
     */
    public final class ServiceClassNode extends AbstractNode {
        /** is the classs masked in other module?
         */
        boolean bRemoved ;
        ServiceClassNode(String className, boolean bRemoved) {
            super(Children.LEAF);
            setName(className);
            this.bRemoved = bRemoved;
            if (className.startsWith("-")) { // NOI18N
                setIconBaseWithExtension("org/netbeans/modules/apisupport/project/metainf/noinstance.png");
            } else {
                setIconBaseWithExtension("org/netbeans/modules/apisupport/project/metainf/instance.png");
            }
        }
        Service getService() {
            // The parent node can be null
            if (getParentNode() != null) {
                List services = (List) allServicesMap.get(getParentNode().getName() );
                String name = getName();
                if (services != null) {
                    for (Iterator it = services.iterator() ; it.hasNext() ; ) {
                        Service service = (Service) it.next();
                        for (Iterator cIt = service.getClasses().iterator() ; cIt.hasNext() ; ) {
                            if (name.equals(cIt.next())) { 
                                return service;
                            }
                        }
                    }
                }
            }
            return null;
        }
        public Action[] getActions(boolean context) {
            return new Action[] {DeleteAction.get(DeleteAction.class)}; 
        }
        
        public String getHtmlDisplayName() {
            List services = (List) moduleServiceMap.get(getParentNode().getName() );
            String name = getName();
            boolean bFound = false;
            if (services != null) {
                for (Iterator it = services.iterator() ; it.hasNext() ; ) {
                    Service service = (Service) it.next();
                    for (Iterator cIt = service.getClasses().iterator() ; cIt.hasNext() ; ) {
                        if (name.equals(cIt.next())) {
                            bFound = true;
                            break;
                        }
                    }
                }
            }
            String dispName = (bFound && getParentNode().getParentNode().getName() != THIS_SERVICES) ? "<b>" + name + "</b>" : name; //NOI18N
            if (bRemoved) {
                dispName = "<s>" + dispName + "</s>"; //NOI18N
            }
            return dispName;
        }
        public boolean canDestroy() {
            return true;
        }
        public boolean canCopy() {
            return false;
        }
        public void destroy() throws IOException {
            Service service = getService();
            // exists the service?
            if (service != null) {
                Service moduleService = null;
                List moduleServices = (List) moduleServiceMap.get(service.getFileName());
                if (moduleServices == null || moduleServices.size() == 0) {
                    // create service in this modulu if the service doesn't exist
                    ArrayList classes = new ArrayList();
                    moduleService = new Service(service.getCodebase(),
                                                        service.getFileName(),
                                                        classes);
                } else {
                    moduleService = (Service) moduleServices.get(0);
                }
                moduleService.removeClass(getName(),project);
            }
        }
    }
    
    public ServiceNodeHandler(Project project, NbModuleProvider provider) {
        this.project = project;
        this.info = provider;
        if (!registeredListener) {
            // #87269 deadlock when file is modified externally on project initialization
            // for example cvs update can cause it 
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                public void run() {
                  registerFileObjectListener();
                }
            });
        }
        
    }
    
    /** creates root node which will be placed into important files node
     */
    public  Node createServiceRootNode() {
        return new ServiceRootNode();
    }
    
    class ServiceRootNode extends AbstractNode {
        ServiceRootNode () {
            super (new Children.Array());
            setDisplayName(ROOT_NODE_NAME);
            setName(ROOT_NODE_NAME);
            Children.Array childs = (Children.Array)getChildren();
            childs.add(new Node[]{createServiceFolderNode(true),
                createServiceFolderNode(false)});
            setIconBaseWithExtension("org/netbeans/modules/apisupport/project/metainf/services.png");
        }
        ServiceNodeHandler getNodeHandler() {
            return ServiceNodeHandler.this;
        }

        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }
    
    static class ServiceFolderNode extends AbstractNode {
        ServiceFolderNode(Children children) {
            super(children);
        }
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }
    private Node createServiceFolderNode(boolean bProjectServices) {
        ServiceRootChildren children = new ServiceRootChildren(bProjectServices);
        AbstractNode node = new ServiceFolderNode(children);
        if (bProjectServices) {
            node.setDisplayName(THIS_SERVICES);
            node.setName(THIS_SERVICES);
            node.setIconBaseWithExtension("org/netbeans/modules/apisupport/project/metainf/export.png");
            moduleChild = children;
        } else {
            node.setDisplayName(THIS_SERVICES_IN_CONTEXT);
            node.setName(THIS_SERVICES_IN_CONTEXT);
            node.setIconBaseWithExtension("org/netbeans/modules/apisupport/project/metainf/services.png");
            allInContextChild = children;
        }
        return node;
    }
    
    ////////////////////////////////////////////////////////////////
    // Filechange listener
    /////////////////////////////////////////////////////
    
    /** listen on :
     *  META-INF/service | META-INF | project/src
     */
    public  void registerFileObjectListener() {
        FileObject srcDir = project.getProjectDirectory().getFileObject(
                                info.getResourceDirectoryPath(false));
        
        // srcDir is sometimes null, is it bug?
        if (srcDir != null) {
            if (!registeredListener) {
                registeredListener = true;
                FileObject fo = srcDir.getFileObject("META-INF"); //NOI18N 
                FileChangeListener listener = ServicesFileListener.getInstance();
                if (fo != null) {
                    fo.removeFileChangeListener(listener);
                    fo.addFileChangeListener(listener);
                    metaInfServicesFo = fo;
                    fo = fo.getFileObject("services"); //NOI18N
                    if (fo != null) {
                        fo.removeFileChangeListener(listener);
                        fo.addFileChangeListener(listener);
                        metaInfServicesFo = fo;
                    }
                } else {
                    srcDir.removeFileChangeListener(listener);
                    srcDir.addFileChangeListener(listener);
                    metaInfServicesFo = srcDir;
                }
            }
        } else {
            // Error - logging 
            ErrorManager em = ErrorManager.getDefault();
            em.log(" project.getSourceDirectory() = null");
            em.log("codenamebase = " + info.getCodeNameBase() );
            em.log("projectroot = " + project.getProjectDirectory().getPath());
        }
    }
    
    void updateFile(FileObject fo) {
        try {
            if (fo.getParent() == SUtil.getServicesFolder(project,false) ) {
                InputStream is = fo.getInputStream();
                Service service = Service.createService(info.getCodeNameBase(),fo.getNameExt(), is );
                is.close();
                ServiceViewUpdater.serviceUpdated(service,this);
                // merge or add service
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
        
    }
    
    
    /** remove nodes ...
     */
    void removeFile(FileObject fileObject) throws IOException {
        if (fileObject.getParent() == SUtil.getServicesFolder(project,false) ) {
            String name = fileObject.getNameExt();
            if (moduleServiceMap != null) {
                Service service = null;
                synchronized (this) {
                    List services = (List)moduleServiceMap.get(name);
                    if (services != null && services.size() > 0 ) {
                        service = (Service)services.get(0);
                    }
                    moduleServiceMap.remove(name);
                    
                    if (allServicesMap != null ) {
                        services = (List)allServicesMap.get(name);
                        if (services != null) {
                            services.remove(service);
                            
                            if (services.isEmpty()) {
                                allServicesMap.remove(name);
                            }
                        }
                    }
                }
                updateNode(service);
            }
        }
    }
    
    /** update node of services list
     * - add node
     * - modify list of classes
     * - remove node
     */
    private void updateNode(Service service) {
        String name = (service == null) ? null : service.getFileName();
        if (moduleChild != null && moduleChild.fullyComputed &&moduleServiceMap != null ) {
            if (prevModuleServicesCount == moduleServiceMap.size() && name != null ) {
                // update only key
                moduleChild.updateNode(name);
            } else {
                moduleChild.refreshKeys();
                prevModuleServicesCount = moduleServiceMap.size();
            }
        }
        if (allInContextChild != null && allInContextChild.fullyComputed && allServicesMap != null) {
            if (prevAllServicesCount == allServicesMap.size() && name != null) {
                allInContextChild.updateNode(name);
            } else {
                allInContextChild.refreshKeys();
                prevAllServicesCount = allServicesMap.size();
            }
        }
    }

    public int hashCode() {
        return getCodeNameBase().hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof ServiceNodeHandler && 
          getCodeNameBase().equals(((ServiceNodeHandler)obj).getCodeNameBase());
    }

    void updateService(Service service) {
      //  throw new UnsupportedOperationException("Not yet implemented");
        synchronized (this) {
            if (moduleServiceMap != null) {
                List services = (List) moduleServiceMap.get(service.getFileName()) ;
                if (service.getCodebase().equals(info.getCodeNameBase())) {
                    if (services != null && services.size() > 0 ) {
                        services.remove(0);
                    } else {
                        services = new ArrayList();
                        moduleServiceMap.put(service.getFileName(),services);
                    }
                   services.add(service);
                }

                if (allServicesMap != null) {
                    services = (List) allServicesMap.get(service.getFileName()) ;
                    if (services != null && services.size() > 0 ) {
                        // find service
                        for (int sIt = 0 ; sIt < services.size() ; sIt++ ) {
                            if (((Service)services.get(sIt)).getCodebase().equals(service.getCodebase())) {
                                services.remove(sIt);
                                break;
                            }
                        }
                    } else {
                        services = new ArrayList();
                        allServicesMap.put(service.getFileName(),services);
                    }
                    services.add(service);
                }
               updateNode(service);
            }
        }
    }

    Project getProject() {
        return project;
    }

    private String getCodeNameBase() {
         String cnb = info.getCodeNameBase();
          // cnb will be null if project is deleted
         if (cnb == null) {
             cnb = codeNameBase;
         } else {
             codeNameBase = cnb;
         }
        if (cnb == null) {
          cnb = "unknown"; // NOI18N  
        }
        return cnb;
    }
}
