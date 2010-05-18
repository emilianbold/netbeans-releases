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
package org.netbeans.modules.websvc.jaxrpc.nodes;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
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
    FileObject[] srcRoots;
    DDProvider wsDDProvider;
    FileObject ddFolder;
    FileChangeListener ddFolderListener;
    
    public WebServicesChildren(FileObject[] srcRoots) {
        this.srcRoots = srcRoots;
        assert srcRoots!=null&&srcRoots.length>0;
        Project project = FileOwnerQuery.getOwner(srcRoots[0]);
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
                } catch(java.io.IOException e) {
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
        } catch(java.io.IOException e) {
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
    
    private FileObject getImplBeanClass(FileObject srcRoot, WebserviceDescription webServiceDescription) {
        PortComponent portComponent = webServiceDescription.getPortComponent(0); //assume one port per ws
        ServiceImplBean serviceImplBean = portComponent.getServiceImplBean();
        String link =serviceImplBean.getServletLink();
        if(link == null) {
            link = serviceImplBean.getEjbLink();
        }
        WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(srcRoot);
        String implBean = wsSupport.getImplementationBean(link);
        return srcRoot.getFileObject(implBean.replace('.','/').concat(".java"));
    }
    
    protected Node[] createNodes(Object key) {
        if(key instanceof WebServiceWrapper) {
            WebServiceWrapper wrapper = (WebServiceWrapper)key;
            WebserviceDescription description = wrapper.getWebServiceDescription();
            for(FileObject srcRoot:srcRoots){
                FileObject implBean = getImplBeanClass(srcRoot,description);
                if(implBean!=null) {
                    return new Node[] {new WebServiceNode(webServices,
                            wrapper.getWebServiceDescription(), srcRoot, implBean)};
                }
            }
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
                } catch(java.io.IOException e) {
                    webServices = null;
                    //Don't do anything
                    //FIX-ME: Log
                }
                
            }
        }
    }
    
}
