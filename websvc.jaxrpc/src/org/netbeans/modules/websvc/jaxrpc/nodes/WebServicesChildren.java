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
package org.netbeans.modules.websvc.jaxrpc.nodes;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.j2ee.dd.api.webservices.DDProvider;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileChangeAdapter;
import java.beans.PropertyChangeListener;

public class WebServicesChildren extends Children.Keys implements PropertyChangeListener{
    Webservices webServices;
    WebServicesSupport wsSupport;
    FileObject srcRoot;
    DDProvider wsDDProvider;
    FileObject ddFolder;
    FileChangeListener ddFolderListener;
    
    public WebServicesChildren(FileObject srcRoot) {
        this.srcRoot = srcRoot;
        Project project = FileOwnerQuery.getOwner(srcRoot);
        this.wsSupport = WebServicesSupport.
        getWebServicesSupport(project.getProjectDirectory());
        this.wsDDProvider = DDProvider.getDefault();
        ddFolderListener = new DDFolderFileChangeListener();
    }
    
    protected void addNotify() {
        super.addNotify();
        ddFolder = wsSupport.getWsDDFolder();
        //always listen to folder containing webservices.xml
        //for the case where the webservices.xml is deleted and recreated again
        // IZ 55633: if no WEB-INF/META-INF, then ddFolder can return null.
        if(ddFolder != null) {
            ddFolder.addFileChangeListener(ddFolderListener);
            FileObject wsDD = ddFolder.getFileObject("webservices", "xml");

            if(wsDD != null){
                try {
                    webServices = wsDDProvider.getDDRoot(wsSupport.getWebservicesDD());
                    webServices.addPropertyChangeListener(this);
                }
                catch(java.io.IOException e) {
                    webServices = null;
                    //Don't do anything
                    //FIX-ME: Log
                }
            }
            
            updateKeys();
        }
    }
    
    protected void removeNotify() {
        setKeys(Collections.EMPTY_SET);
        if(ddFolder != null){
            ddFolder.removeFileChangeListener(ddFolderListener);
        }
        super.removeNotify();
    }
    
    private void updateKeys() {
        List keys = new ArrayList();
        try {
            webServices = wsDDProvider.getDDRoot(wsSupport.getWebservicesDD());
        }
        catch(java.io.IOException e) {
            webServices = null;
            //Don't do anything
            //FIX-ME: Log
        }
        if (webServices != null) {
            WebserviceDescription[] webServiceDescriptions
            = webServices.getWebserviceDescription();
            for (int i = 0; i < webServiceDescriptions.length; i++) {
                WebserviceDescription webServiceDescription = webServiceDescriptions[i];
                WebServiceWrapper key = new WebServiceWrapper(webServiceDescription);
                keys.add(key);
            }
        }
        setKeys(keys);
    }
    
    static class WebServiceWrapper {
        WebserviceDescription webServiceDescription;
        
        public WebServiceWrapper(WebserviceDescription webServiceDescription) {
            this.webServiceDescription = webServiceDescription;
        }
        public WebserviceDescription getWebServiceDescription() {
            return webServiceDescription;
        }
        
        public boolean equals(Object obj) {
            if(obj instanceof WebServiceWrapper) {
                WebServiceWrapper otherObject = (WebServiceWrapper)obj;
                return this.getWebServiceDescription().getWebserviceDescriptionName().
                equals(otherObject.getWebServiceDescription().getWebserviceDescriptionName());
            }
            return false;
        }
        
        public int hashCode() {
            return getWebServiceDescription().getWebserviceDescriptionName().hashCode();
        }
        
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof WebServiceWrapper) {
            WebServiceWrapper wrapper =
            (WebServiceWrapper)key;            
            return new Node[] {new WebServiceNode(webServices, wrapper.getWebServiceDescription(), srcRoot)};
        }
        return new Node[0];
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        updateKeys();
    }
    
    class DDFolderFileChangeListener extends FileChangeAdapter{
        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
            FileObject fo = fe.getFile();
            if(fo.getNameExt().equals("webservices.xml")){
                try {
                    webServices = wsDDProvider.getDDRoot(wsSupport.getWebservicesDD());
                    //FIX-ME: change to WeakListener
                    webServices.addPropertyChangeListener(WebServicesChildren.this);
                }
                catch(java.io.IOException e) {
                    webServices = null;
                    //Don't do anything
                    //FIX-ME: Log
                }

            }
        }
    }

}
