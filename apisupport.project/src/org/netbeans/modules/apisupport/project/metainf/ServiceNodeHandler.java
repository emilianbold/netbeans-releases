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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
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
    TreeMap<String,List<Service>> /* service class -> List of classes */ allServicesMap ;
    // services in module
    TreeMap <String,List<Service>>/* service class -> List of classes */moduleServiceMap;

    int prevAllServicesCount = -1;
    int prevModuleServicesCount = -1;

    final Project project;
    final NbModuleProvider info;
    List<Service> moduleServices;

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
    class ServiceRootChildren extends Children.Keys<String> {
        boolean fullyComputed = false;
        /** show services  of this project or platfrom
         */
        private final boolean bProjectServices ;

        ServiceRootChildren(boolean bProjectServices) {
            this.bProjectServices = bProjectServices;
        }
        protected Node[] createNodes(String key) {
            // synchronize access to allServicesMap and moduleServices
            if (key.equals(KEY_WAIT)) {
                return new Node[] {new AbstractNode(Children.LEAF) {
                    @Override
                    public String getName() {
                        return KEY_WAIT;
                    }
                    @Override
                    public String getDisplayName() {
                        return NbBundle.getMessage(ServiceNodeHandler.class,"LBL_ServiceNode_please_wait");
                    }
                    @Override
                    public Action[] getActions(boolean context) {
                        return new Action[0];
                    }
                }};
            } else {
                Node parent = getNode();
                String parentName = parent.getName();
                boolean isThisModule = parentName.equals(THIS_SERVICES);
                ServiceNode node = new ServiceNode(key,isThisModule);
                return new Node[] {node};
            }

        }

        @Override
        protected void addNotify() {
            SUtil.log(SUtil.LOG_SERVICE_NODE_HANDLER_ADD_NOTIFY);
            super.addNotify();
            if (fullyComputed) {
                if (bProjectServices) {
                    // only services from this project
                    prevModuleServicesCount = moduleServiceMap.keySet().size();
                    SUtil.log(SUtil.LOG_SET_KEYS);
                    setKeys(moduleServiceMap.keySet());
                } else {
                    prevAllServicesCount = allServicesMap.keySet().size();
                    setKeys(allServicesMap.keySet());
                }
            } else {
                SUtil.log(SUtil.LOG_COMPUTE_KEYS);
                setKeys(new String[] {KEY_WAIT});
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            Set<String> keys = null;
                            // synchronize access to TreeMaps
                            // categorize services
                            synchronized(ServiceNodeHandler.this) {
                                if (moduleServiceMap == null) {
                                    moduleServiceMap = new TreeMap<String,List<Service>>();
                                    moduleServices = Service.getOnlyProjectServices(project);
                                    sortServices(moduleServiceMap, moduleServices);
                                }
                                if (bProjectServices) {
                                    // only services from this project
                                    keys = moduleServiceMap.keySet();
                                    prevModuleServicesCount = moduleServiceMap.keySet().size();
                                } else {
                                    if (allServicesMap == null) {
                                        allServicesMap = new TreeMap<String,List<Service>>();
                                        List <Service> services = ServiceViewUpdater.getAllServices(ServiceNodeHandler.this);
                                        if (services != null) {
                                            assert moduleServiceMap!=null;
                                            sortServices(allServicesMap, services);
                                        }
                                    }
                                    prevAllServicesCount = allServicesMap.keySet().size();
                                    keys = allServicesMap.keySet();
                                    if (!keys.isEmpty()) {
                                        SUtil.log(keys.iterator().next().toString());
                                    }
                                }

                            }
                            setKeys(keys);
                            synchronized (ServiceNodeHandler.this) {
                                fullyComputed = true;
                                SUtil.log(SUtil.LOG_END_COMPUTE_KEYS);
                            }
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                    } // run



                    private void sortServices(final TreeMap<String,List<Service>> map, final List<Service> services) {
                        //               sortServices(map,services);
                        for (Service service : services) {
                            assert map != null;
                            List<Service> theSameServices =  map.get(service.getFileName());
                            if (theSameServices == null) {
                                theSameServices = new ArrayList<Service>();
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
            for (Node _n : getNodes()) {
                ServiceNode n = (ServiceNode) _n;
                if (n.getName().equals(keyName)) {
                    n.refreshName();
                    ((ServiceNodeChildren)n.getChildren()).nodesChanged();
                }
             }
        }

        @Override
        protected void removeNotify() {
            setKeys(new String[0]);
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

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return  (obj instanceof ServiceClassKey) && ((ServiceClassKey)obj).name.equals(name);
        }

    }

    class  ServiceNodeChildren extends Children.Keys<ServiceClassKey>  {
        private boolean isThisModule;
        /** className -> ServiceClassKey
         */
        private TreeMap<String,ServiceClassKey> keys;
        boolean initialized = true;

        ServiceNodeChildren(boolean isThisModule) {
            this.isThisModule = isThisModule;
//            setKeys(new Object[]{KEY_WAIT});
        }
        private TreeMap<String,ServiceClassKey> getKeysMap () {
            if (keys == null) {
                keys = new TreeMap<String,ServiceClassKey>();
            }
            return keys;
        }

        private ServiceClassKey addKey(ServiceClassKey key,TreeMap<String,ServiceClassKey> newKeyMap) {
            TreeMap<String, ServiceClassKey> ks = getKeysMap();
            ServiceClassKey oldKey = ks.get(key.name);
              if (oldKey == null) {
                  oldKey = key;
              } else if (oldKey.bRemoved != key.bRemoved) {
                  oldKey.bRemoved = key.bRemoved;
                  refreshKey(oldKey);
              }
              newKeyMap.put(key.name,oldKey);
              return oldKey;
        }

        @Override
        protected void addNotify() {
            ServiceNode serviceNode = (ServiceNode) getNode();
            isThisModule = serviceNode.isThisModule();
            List<Service> servicesGroup  =  ((isThisModule) ? moduleServiceMap.get(serviceNode.getName()) :
                                                                  allServicesMap.get(serviceNode.getName()));

            List<String> classes  = new ArrayList<String>();
            List<String> maskedClasses = new ArrayList<String>();

            TreeMap<String,ServiceClassKey> newKeyMap = new TreeMap<String,ServiceClassKey>();
            // creates two groups - classes and masked class
            //
            for (Service service : servicesGroup) {
                for (String name : service.getClasses()) {
                    if (name.charAt(0) == '-') {
                        maskedClasses.add(name);
                    } else {
                        classes.add(name);
                    }
                }
            }

            // create nodes from classes
            //
            for (String name : classes) {
                ServiceClassKey key = new ServiceClassKey(name,false);
                if (!isThisModule) {
                    String filteredName = '-' + name;
                    // register class like masked
                    for (String masked : maskedClasses) {
                        if (masked.equals(filteredName)) {
                            key.bRemoved = true;
                        }
                    }
                }
                key = addKey(key,newKeyMap);

            }
            // show element which masks services in this module view
            //
            if (isThisModule) {
                for (String masked : maskedClasses) {
                    addKey(new ServiceClassKey(masked, false), newKeyMap);
                }
            }
            this.keys = newKeyMap;
            setKeys(getKeysMap().values());
            initialized = true;
        }
        protected Node[] createNodes(ServiceClassKey key) {
            ServiceClassKey classKey = key;
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
            return (getParentNode() == null) ? false : getParentNode().getName().equals(THIS_SERVICES);
        }
        @Override
        public String getHtmlDisplayName() {
            List<Service> services = moduleServiceMap.get(getName());
            return (services != null && !isThisModule()) ? "<b>" + getName() + "</b>" : getName(); //NOI18N
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {AddService.getInstance()};
        }

        /** create new service (also creates new file)
         */
        void addService(String serviceName, String classServiceName) {
            // #139887: when allServicesMap is null then we're called from <exported services> node
            // and can safely use moduleServiceMap instead. As it offers only classes from this project, 
            // it is also consistent regarding duplicate services.
            TreeMap<String,List<Service>> usedMap = allServicesMap != null ? allServicesMap : moduleServiceMap;
            List<Service> services = usedMap.get(serviceName);
            boolean exists = false;
            for (Service service : services) {
                for (String className : service.getClasses()) {
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
               services = moduleServiceMap.get(serviceName);
               Service service = null;
               if (services != null && services.size() > 0) {
                   service = services.get(0);
               } else {
                   service = new Service(getInfo().getCodeNameBase(),serviceName,new ArrayList<String>());
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
        /* XXX #126577: not used
        Service getService() {
            // The parent node can be null
            if (getParentNode() != null) {
                List<Service> services = allServicesMap.get(getParentNode().getName() );
                String name = getName();
                if (services != null) {
                    for (Service service : services) {
                        for (String n : service.getClasses()) {
                            if (name.equals(n)) {
                                return service;
                            }
                        }
                    }
                }
            }
            return null;
        }*/
        @Override
        public Action[] getActions(boolean context) {
            // XXX cannot delete anyway, see ##126577: return new Action[] {DeleteAction.get(DeleteAction.class)};
            return new Action[0];
        }

        @Override
        public String getHtmlDisplayName() {
            List<Service> services = moduleServiceMap.get(getParentNode().getName() );
            String name = getName();
            boolean bFound = false;
            if (services != null) {
                for (Service service : services) {
                    for (String n : service.getClasses()) {
                        if (name.equals(n)) {
                            bFound = true;
                            break;
                        }
                    }
                }
            }
            String dispName = (bFound && !getParentNode().getParentNode().getName().equals(THIS_SERVICES)) ? "<b>" + name + "</b>" : name; //NOI18N
            if (bRemoved) {
                dispName = "<s>" + dispName + "</s>"; //NOI18N
            }
            return dispName;
        }
        /* XXX #126577: just throws NPE if you try
        @Override
        public boolean canDestroy() {
            return true;
        }
         */
        @Override
        public boolean canCopy() {
            return false;
        }
        /* XXX #126577: not used 
        @Override
        public void destroy() throws IOException {
            Service service = getService();
            // exists the service?
            if (service != null) {
                Service moduleService = null;
                List<Service> moduleServices = moduleServiceMap.get(service.getFileName());
                if (moduleServices == null || moduleServices.size() == 0) {
                    // create service in this modulu if the service doesn't exist
                    List<String> classes = new ArrayList<String>();
                    moduleService = new Service(service.getCodebase(),
                                                        service.getFileName(),
                                                        classes);
                } else {
                    moduleService = moduleServices.get(0);
                }
                moduleService.removeClass(getName(),project);
            }
        }*/
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

        @Override
        public Action[] getActions(boolean context) {
            return new Action[0];
        }
    }

    static class ServiceFolderNode extends AbstractNode {
        ServiceFolderNode(Children children) {
            super(children);
        }
        @Override
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
        if (moduleChild != null && moduleChild.fullyComputed && moduleServiceMap != null ) {
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

    @Override
    public int hashCode() {
        return getKeyName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ServiceNodeHandler &&
          getKeyName().equals(((ServiceNodeHandler)obj).getKeyName());
    }

    void updateService(Service service) {
      //  throw new UnsupportedOperationException("Not yet implemented");
        synchronized (this) {
            if (moduleServiceMap != null) {
                List<Service> services = moduleServiceMap.get(service.getFileName()) ;
                if (service.getCodebase().equals(info.getCodeNameBase())) {
                    if (services != null && services.size() > 0 ) {
                        services.remove(0);
                    } else {
                        services = new ArrayList<Service>();
                        moduleServiceMap.put(service.getFileName(),services);
                    }
                   services.add(service);
                }

                if (allServicesMap != null) {
                    services = allServicesMap.get(service.getFileName()) ;
                    if (services != null && services.size() > 0 ) {
                        // find service
                        Iterator<Service> sIt = services.iterator();
                        while (sIt.hasNext()) {
                            if (sIt.next().getCodebase().equals(service.getCodebase())) {
                                sIt.remove();
                                break;
                            }
                        }
                    } else {
                        services = new ArrayList<Service>();
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

    private String getKeyName() {
        // #103798 important files node hashCode problems.
        // probably on cnb change
        if (codeNameBase == null) {
            codeNameBase  = info.getCodeNameBase();
        }
          // cnb will be null if project is deleted
        if (codeNameBase == null) {
          codeNameBase = "unknown"; // NOI18N
        }
        return codeNameBase;
    }
}
