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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.compapp.javaee.util.ProjectUtil;
import org.netbeans.api.project.Project;

import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import java.util.Enumeration;
import java.util.List;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentStatus;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileSystem;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;


/**
 * 
 * This class creats a model that provides the jbi component information and the
 * associated wsdl extension plugin information. It reads the layer filesystem 
 * and creates the model. Also, it listens for the changes in the layer filesyste
 * correpsonding to the jbi component folders and reloads the model and notifies
 * the change to the listeners of this model using ChangeListner interface.
 * 
 * @author ???
 * @author chikkala
 * @version 
 */
public class JbiDefaultComponentInfo {
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_ID = "id"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_NAME = "name"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_DESC = "description"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_TYPE = "type"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_NAMESPACE = "namespace"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String COMP_ICON = "icon"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String PROJ_ICON = "projectIcon"; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    public static final String FILE_ICON = "fileIcon"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    private static final String OLD_NAME_PREFIX = "com.sun."; // NOI18N
    
    /**
     * DOCUMENT ME!
     */
    private static final String NEW_NAME_PREFIX = "sun-"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String SB_COMP_RESOURCE_NAME = "SeeBeyondJbiComponents"; // NOI18N
    public static final String COMP_RESOURCE_NAME = "JbiComponents"; // NOI18N
    public static final String WSDLEDITOR_NAME = "WSDLEditor"; // NOI18N
    public static final String WSDL_ICON_NAME = "SystemFileSystem.icon"; // NOI18N

    private static Logger sLogger = Logger.getLogger(JbiDefaultComponentInfo.class.getName());
    
    private static JbiDefaultComponentInfo singleton = null;
    /** change listner support */
    private static ChangeSupport changeSupport = null;
    /** listener for monitoring layer fs changes */
    private static LayerFSChangeListener lfsListener = null;
    
    // a list of SE and BCs known at design time
    private List<JBIComponentStatus> componentList = new ArrayList<JBIComponentStatus>();
    
    // mapping SE/BC's name to the component
    private Map<String, JBIComponentStatus> componentMap = new HashMap<String, JBIComponentStatus>();
    
    // mapping BC name to icon
    private Map<String, URL> bcIconMap = new HashMap<String, URL>();
    
    // mapping BC name to binding info
    private Map<String, JbiBindingInfo> bindingInfoHash = new HashMap<String, JbiBindingInfo>();
    private List<JbiBindingInfo> bindingInfoList = new ArrayList<JbiBindingInfo>();

    private JbiDefaultComponentInfo() {
    }
    /** 
     * Initializes the class and loads the data from the layer filesystem. This can be
     * used to initialize this object after creation or to reload the data when the 
     * layer filesystem changes.
     */
    protected void init() {
        
        componentList = new ArrayList<JBIComponentStatus>();
        componentMap = new HashMap<String, JBIComponentStatus>();
        bcIconMap = new HashMap<String, URL>();
        bindingInfoHash = new HashMap<String, JbiBindingInfo>();
        bindingInfoList = new ArrayList<JbiBindingInfo>();
        
        try {
            FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();

            // load new container first
            FileObject wsdlEditorFolderFO = fileSystem.findResource(WSDLEDITOR_NAME);
            loadJbiDefaultComponentInfoForSDLEditor(wsdlEditorFolderFO);

            // load new container first
            FileObject compFolderFO = fileSystem.findResource(COMP_RESOURCE_NAME);
            loadJbiDefaultComponentInfoFromFileObject(compFolderFO);

            // backward compatibility
            FileObject sbCompFolderFO = fileSystem.findResource(SB_COMP_RESOURCE_NAME);
            loadJbiDefaultComponentInfoFromFileObject(sbCompFolderFO);

        } catch (Exception ex) {
            sLogger.log(Level.FINE, ex.getMessage(), ex);
        }        
    }
    /**
     * Factory method for the default component list object
     *
     * @return the default component list object
     */
    public static JbiDefaultComponentInfo getJbiDefaultComponentInfo() {
        if (singleton == null) {
            try {
                singleton = new JbiDefaultComponentInfo();
                singleton.init(); // don't fire change event until change support is initialized.
                changeSupport = new ChangeSupport(singleton);
                lfsListener = new LayerFSChangeListener();
            } catch (Exception ex) {
                // failed... return withopt changing the selector content.
                ex.printStackTrace();
            }
        }       
        return singleton;
    }

    
    private static void loadJbiDefaultComponentInfoForSDLEditor(FileObject fo) { // Register Binding Component Icon: WSDLEditor/Binding/{FileBinding, ...}
        if (fo != null) {
            DataFolder df = DataFolder.findFolder(fo);
            DataObject[] bs = df.getChildren();

            for (int i = 0; i < bs.length; i++) {
                String name = bs[i].getName();
                if (name.equalsIgnoreCase("binding")) { //NOI18N
                    DataObject[] ms = DataFolder.findFolder(bs[i].getPrimaryFile()).getChildren();
                    for (int j = 0; j < ms.length; j++) {
                        String bname = ms[j].getName().toLowerCase(); // e.x., filebinding
                        FileObject msfo = ms[i].getPrimaryFile(); 
                        URL icon = (URL) msfo.getAttribute(WSDL_ICON_NAME);
                        if (icon != null) {
                            singleton.bcIconMap.put(bname, icon);
                            // System.out.println("Add ICON: "+bname+", "+ ((URL) icon).toString());
                        }
                    }
                }
            }
        }
    }

    private static void loadJbiDefaultComponentInfoFromFileObject(FileObject fo) { // JbiComponents or SeeBeyondJbiComponents
        if (fo != null) {
            DataFolder df = DataFolder.findFolder(fo);            
            for (DataObject compDO : df.getChildren()) {
                String name = compDO.getName();
                String id = ""; // NOI18N
                String desc = ""; // NOI18N
                String type = ""; // NOI18N
                String state = "Installed"; // NOI18N
                URL projectIconURL = null; 
                URL fileIconURL = null; 
                List<String> nsList = new ArrayList<String>();
                
                FileObject compFO = compDO.getPrimaryFile();  // e.x., SeeBeyondJbiComponents/sun-file-binding
                for (Enumeration<String> e = compFO.getAttributes(); e.hasMoreElements();) {
                    String attrName = e.nextElement();
                    Object attrValue = compFO.getAttribute(attrName);
                    
                    if (attrName.equals(COMP_ID)) {
                        id = (String) attrValue;
                    } else if (attrName.equals(COMP_DESC)) {
                        desc = (String) attrValue;
                    } else if (attrName.equals(COMP_TYPE)) {
                        type = (String) attrValue;
                    } else if (attrName.equals(COMP_NAMESPACE)) {
                        nsList.add((String) attrValue);  
                    } else if (attrName.equals(PROJ_ICON)) {
                        projectIconURL = (URL) attrValue;
                    } else if (attrName.equals(FILE_ICON)) {
                        fileIconURL = (URL) attrValue;
                    }
                }
                        
                if (JBIComponentStatus.BINDING.equals(type) && compDO instanceof DataFolder) {
                    for (DataObject bindingTypeDO : ((DataFolder)compDO).getChildren()) { 
                        FileObject bindingTypeFO = bindingTypeDO.getPrimaryFile(); // e.x., SeeBeyondJbiComponents/sun-file-binding/file.binding-1.0
                        
                        String bindingType = bindingTypeFO.getName(); // e.x., file.binding-1    // for http soap, there are two files: http.binding-1 and soap.binding-1
                        int idx = bindingType.indexOf('.');
                        if (idx > 0) {
                            bindingType = bindingType.substring(0,idx).toLowerCase();
                        }
                        
                        String ns = (String) bindingTypeFO.getAttribute(COMP_NAMESPACE);
                        if (ns != null) {
                            nsList.add(ns);
                            addBindingInfo(id, bindingType, desc, ns);
                        }
                    }
                }
                
                // check for duplicates first..
                if (id.length() > 0 && !singleton.componentMap.containsKey(id)) {
                    JBIComponentStatus jcs = 
                            new JBIComponentStatus(id, desc, type, state, nsList);
                    jcs.setProjectIconURL(projectIconURL);
                    jcs.setFileIconURL(fileIconURL);
                    singleton.componentList.add(jcs);
                    singleton.componentMap.put(id, jcs);
                }
            }
        }
    }
    
    /** 
     * @param id    binding component identifier, e.x., "sun-http-binding"
     * @param bindingType   binding type, e.x., "http", or "soap"
     * @param desc  binding component description 
     * @param ns    namespace for the binding type
     */
    private static void addBindingInfo(String id, String bindingType, String desc, String ns) {
        URL icon = null;
        
        for (String name : singleton.bcIconMap.keySet()) {
            if (name.startsWith(bindingType)) { // e.x., name: filebinding; bid: file
                icon = singleton.bcIconMap.get(name);
                break;
            }
        }
        
        if (icon != null) {
            JbiBindingInfo biinfo = new JbiBindingInfo(id, bindingType, icon, desc, ns);
            singleton.bindingInfoHash.put(id, biinfo);
            singleton.bindingInfoList.add(biinfo);
        } else {
            System.err.println("WARNING: missing icon for JBI binding component " + id);
        }
    }
    /**
     * reloads the model from the layer filesystem and notifies the change listeners
     * of this model about the change.
     */
    public void reload() {
        sLogger.fine("in reloading JbiDefaultComponentInfo......"); //NOI18N
        init();
        // fire change event until change support is initialized.
        if ( changeSupport != null ) {
            fireChangeEvent();
        }
    }
    /**
     * add change listener
     * @param listener
     */
    public void addChangeListener (ChangeListener listener) {
        // remove the listener if it is previously added
        changeSupport.removeChangeListener(listener);
        // add it now. 
        changeSupport.addChangeListener(listener);
    }
    /**
     * removes the change listener
     * @param listener
     */
    public void removeChangeListener (ChangeListener listener) {
        sLogger.fine("removing the jbi def comp info change listener..."); //NOI18N
        changeSupport.removeChangeListener(listener);
    }
    /**
     * fires the change event 
     */
    protected void fireChangeEvent() {
        changeSupport.fireChange();        
    }
    
    /**
     * getter for the default component list
     *
     * @return the default componet list
     */
    public List<JBIComponentStatus> getComponentList() {
        return componentList;
    }
    
    /**
     * Getter for the default component list hashtable
     *
     * @return the default component list hashtable
     */
    public Map<String, JBIComponentStatus> getComponentHash() {
        return componentMap;
    }

    /**
     * Getter for the default binding info list
     *
     * @return the default binding info list
     */
    public List<JbiBindingInfo> getBindingInfoList() {
        return bindingInfoList;
    }

    /**
     * Getter for the specific binding info
     *
     * @param  id  binding component identifier 
     * @return the specific binding info
     */
    public JbiBindingInfo getBindingInfo(String id) {
        return bindingInfoHash.get(id);
    }

    /**
     * Parse the compoent name for a short display name. Only handle
     * two formats: com.sun.xxx-1.2-3 and sun-xxx-engine
     *
     * @param id component identifier
     * @return display name
     */
    public static String getDisplayName(String id) {
        int idx;
        if (id.startsWith(OLD_NAME_PREFIX)) {
            idx = id.indexOf('-');
            if (idx > 0) {
                return id.substring(OLD_NAME_PREFIX.length(), idx);
            }

        } else if (id.startsWith(NEW_NAME_PREFIX)) {
            idx = id.lastIndexOf('-');
            if (idx > 0) {
                return id.substring(NEW_NAME_PREFIX.length(), idx);
            }
        }
        return id;
    }

    /**
     * Is a given project a JavaEE project
     *
     * @param proj given project
     * @return true if it is a JavaEE project
     */
    public static boolean isJavaEEProject(Project proj){
        return ProjectUtil.isJavaEEProject(proj);
    }
        
    /**
     * Utility method to get the JBI binding info for the given WSDL Port.
     * 
     * @param port given WSDL Port
     * @return the corresponding JBI binding info
     */
    public static JbiBindingInfo getBindingInfo(final Port port) {
        JbiDefaultComponentInfo bcinfo = getJbiDefaultComponentInfo();
        if (bcinfo == null) {
            return null;
        }

        List<ExtensibilityElement> xts = port.getExtensibilityElements();
        if (xts.size() > 0) {
            ExtensibilityElement ex = xts.get(0);
            String qns = ex.getQName().getNamespaceURI();
            if (qns != null) {
                for (JbiBindingInfo bi : bcinfo.getBindingInfoList()) {
                    String ns = bi.getNameSpace();
                    if (qns.equalsIgnoreCase(ns)) {
                        return bi;
                    }
                }
            }
        }
        return null;
    }
    /**
     * This class provides the implementation that can be used to monitor
     * the layer filesytem chagnes correpsonding to the jbi component folder
     * and reload the JbiDefaultComponent model.
     */
    private static class LayerFSChangeListener {

        private FileChangeListener mFileChangeListener = null;
        private RequestProcessor mReqProcessor = null;
        private Runnable mRunnable;
        /**
         * constructor
         */
        public LayerFSChangeListener() {
            initRequestProcessor();
            initFileChangeAdapter();
            registerFileChangeListener();
        }
        /**
         * creates a request processor that will reload the model in a separate thread.
         */
        private void initRequestProcessor() {
            if (mReqProcessor == null) {
                mReqProcessor = new RequestProcessor("NewJbiPluginInstalled", 1); //NOI18N
                mRunnable = new Runnable() {

                    public void run() {
                        sLogger.fine("Running NewJbiPluginInstalled RequestProcessor"); //NOI18N
                        if ( singleton != null ) {
                            singleton.reload();
                        }
                    }
                };
            }
        }
        /**
         * initializes the file change lisnter implementation for the layer
         * filesystem
         */
        private void initFileChangeAdapter() {
            if ( mFileChangeListener != null) {
                sLogger.fine("Layer FS FileChangeListener already created"); //NOI18N
                return;
            }
            mFileChangeListener = new FileChangeAdapter() {

                @Override
                public void fileFolderCreated(FileEvent fe) {
                    onFileFolderCreated(fe);
                }
            };
        }
        /**
         * reloads the model on a change in the jbi component folders
         * @param fe
         */
        private void onFileFolderCreated(FileEvent fe) {
            boolean refresh = false;
            FileObject fo = fe.getFile();
            FileObject parentFO = fo.getParent();
            String parentPath = null;
            if (parentFO != null) {
                parentPath = parentFO.getNameExt();
            }
            if (COMP_RESOURCE_NAME.equals(parentPath) ||
                    SB_COMP_RESOURCE_NAME.equals(parentPath)) {
                refresh = true;
            }
            if (refresh) {
                int timeToWait = 5000;
                sLogger.fine("in LayerFS file folder created. post reload. " + fe.getFile().getNameExt()); //NOI18N
                mReqProcessor.post(mRunnable, timeToWait);
            }
        }
        /**
         * registers the file change listener on the layer filesystem.
         */
        private void registerFileChangeListener() {
            FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
            // listen on filesystem for changes to the jbi component folder
            //TODO: use weak listener
            fileSystem.addFileChangeListener(mFileChangeListener);
        }
    }
}
