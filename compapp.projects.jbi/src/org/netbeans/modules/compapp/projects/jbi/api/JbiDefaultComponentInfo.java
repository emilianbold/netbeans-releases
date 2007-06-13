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

package org.netbeans.modules.compapp.projects.jbi.api;

import java.util.ArrayList;
import org.netbeans.modules.compapp.projects.jbi.descriptor.componentInfo.model.JBIComponentStatus;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;

import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.net.URL;

import org.openide.filesystems.FileSystem;


/**
 * DOCUMENT ME!
 *
 * @author 
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
    public static final String OLD_NAME_PREFIX = "com.sun."; // NOI18N
    /**
     * DOCUMENT ME!
     */
    public static final String NEW_NAME_PREFIX = "sun-"; // NOI18N

    /**
     * DOCUMENT ME!
     */
    public static final String SB_COMP_RESOURCE_NAME = "SeeBeyondJbiComponents"; // NOI18N
    public static final String COMP_RESOURCE_NAME = "JbiComponents"; // NOI18N
    public static final String WSDLEDITOR_NAME = "WSDLEditor"; // NOI18N
    public static final String WSDL_ICON_NAME = "SystemFileSystem.icon"; // NOI18N

    private static JbiDefaultComponentInfo singleton = null;
    private Vector componentList = new java.util.Vector(1);
    private Hashtable componentHash = new Hashtable();
    private Hashtable defaultIconHash = new Hashtable();
    private Hashtable bindingInfoHash = new Hashtable();
    private List<JbiBindingInfo> bindingInfoList = new ArrayList<JbiBindingInfo>();

    private JbiDefaultComponentInfo() {
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
                
                FileSystem fileSystem = Repository.getDefault().getDefaultFileSystem();
                
                FileObject fo;

                // load new container first
                fo = fileSystem.findResource(WSDLEDITOR_NAME);
                loadJbiDefaultComponentInfoForSDLEditor(fo);

                // load new container first
                fo = fileSystem.findResource(COMP_RESOURCE_NAME);
                loadJbiDefaultComponentInfoFromFileObject(fo);
                
                // backward compatibility
                fo = fileSystem.findResource(SB_COMP_RESOURCE_NAME);
                loadJbiDefaultComponentInfoFromFileObject(fo);

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
                if (name.equalsIgnoreCase("binding")) {
                    DataObject[] ms = DataFolder.findFolder(bs[i].getPrimaryFile()).getChildren();
                    for (int j = 0; j < ms.length; j++) {
                        String bname = ms[j].getName().toLowerCase(); // e.x., filebinding
                        FileObject msfo = ms[i].getPrimaryFile(); 
                        Object icon = msfo.getAttribute(WSDL_ICON_NAME);
                        if (icon != null) {
                            singleton.defaultIconHash.put(bname, icon);
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
            DataObject[] ms = df.getChildren();
            
            for (int i = 0; i < ms.length; i++) {
                String name = ms[i].getName();
                String id = ""; // NOI18N
                String desc = ""; // NOI18N
                String type = ""; // NOI18N
                String state = "Installed"; // NOI18N
//                        String ns = ""; // NOI18N
//                        fo = ms[i].getPrimaryFile();
                FileObject msfo = ms[i].getPrimaryFile();  // e.x., SeeBeyondJbiComponents/sun-file-binding
                
                List nsList = new ArrayList();
                List<String> bids = new ArrayList<String>();
                
                for (Enumeration<String> e = msfo.getAttributes(); e.hasMoreElements();) {
                    String cmd = e.nextElement();
                    String attr = (String) msfo.getAttribute(cmd);
                    
                    if (cmd.equals(COMP_ID)) {
                        id = attr;
                    } else if (cmd.equals(COMP_DESC)) {
                        desc = attr;
                    } else if (cmd.equals(COMP_TYPE)) {
                        type = attr;
                    } else if (cmd.equals(COMP_NAMESPACE)) {
                        nsList.add(attr);
                    }
                }
                
                if (JBIComponentStatus.BINDING_TYPE.equals(type) && ms[i] instanceof DataFolder) {
                    DataObject[] mgs = ((DataFolder)ms[i]).getChildren();
                    for (int j = 0; j < mgs.length; j++) {
                        FileObject mgsfo = mgs[j].getPrimaryFile(); // e.x., SeeBeyondJbiComponents/sun-file-binding/file.binding-1.0
                        String attr = (String)mgsfo.getAttribute(COMP_NAMESPACE);
                        bids.add(mgsfo.getName()); // e.x., file.binding-1    // for http soap, there are two files: http.binding-1 and soap.binding-1
                        if (attr != null) {
                            nsList.add(attr);
                        }
                    }
                }
                String[] ns = (String[])nsList.toArray(new String[0]);
                
                // check for duplicates first..
                if ((id.length() > 0) && (singleton.componentHash.get(id) == null)) {
                            /*
                               String tname = id;
                               if (type.equalsIgnoreCase("Engine")) {
                                   tname = name + "-" + id;
                               }
                             */
                    JBIComponentStatus jcs = new JBIComponentStatus(
                            id, id, desc, type, state, ns
                            );
                    singleton.componentList.add(jcs);
                    singleton.componentHash.put(id, jcs);
                    addBindingInfo(id, bids, desc, ns);
                    //System.out.println("CompDisplayName: "+getDisplayName(id));
                }
            }
        }
    }
    /** 
     * @param id    binding component name, e.x., "sun-http-binding"
     * @param bids  binding types, e.x., "http", or "soap"
     * @param desc  binding component description 
     * @param ns    namespaces for the binding component
     */
    private static void addBindingInfo(String id, List<String> bids, String desc, String[] ns) {
        Object icon = null;
        if (bids == null) {
            return;
        }
        for (String bid : bids) {
            int idx = bid.indexOf('.');
            if (idx > 0) {
                bid = bid.substring(0,idx).toLowerCase();
            }

            for (Enumeration e = singleton.defaultIconHash.keys() ; e.hasMoreElements() ;) {
                String name = (String) e.nextElement();
                if (name.startsWith(bid)) { // e.x., name: filebinding; bid: file
                    icon = singleton.defaultIconHash.get(name);
                    break;
                }
            }

            if (icon != null) {
                JbiBindingInfo biinfo = new JbiBindingInfo(id, bid, (URL) icon, desc, ns);
                singleton.bindingInfoHash.put(id, biinfo);
                singleton.bindingInfoList.add(biinfo);
                // System.out.println("Add BiInfo: "+id+", "+bid+", "+ icon.toString());
            }
        }
    }

    /**
     * getter for the default component list
     *
     * @return the default componet list
     */
    public Vector getComponentList() {
        return componentList;
    }
    
    /**
     * Getter for the default component list hashtable
     *
     * @return the default component list hashtable
     */
    public Hashtable getComponentHash() {
        return componentHash;
    }

    /**
     * Getter for the default binding info hashtable
     *
     * @return the default binding info hashtable
     */
    public List<JbiBindingInfo> getBindingInfoList() {
        return bindingInfoList;
    }

    /**
     * Getter for the specific binding info
     *
     * @return the specific binding info
     */
    public JbiBindingInfo getBindingInfo(String id) {
        Object bi = bindingInfoHash.get(id);
        return (JbiBindingInfo) bi;
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
}
