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

package org.netbeans.modules.websvc.registry.netbeans;
import java.net.URL;
import java.beans.PropertyChangeListener;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderLookup;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.websvc.api.registry.WebServicesRegistryView;
/**
 *
 * @author  ludo
 */
public class RegisterViewImplNetBeansSide implements WebServicesRegistryView/*, PropertyChangeListener*/ {
    
    WebServicesRegistryView delegate;

    /** Creates a new instance of RegisterViewImplNetBeansSide */
    public RegisterViewImplNetBeansSide() {
//      WebServiceModuleInstaller.findObject(WebServiceModuleInstaller.class).addPropertyChangeListener(this);
        try{
            delegate = (WebServicesRegistryView) WebServiceModuleInstaller.getExtensionClassLoader().loadClass("org.netbeans.modules.websvc.registry.RegistryViewImpl").newInstance();//NOI18N            
        } catch (Exception e) {
          //  System.out.println("----- lacking app server classes");
            delegate = null;
        }
    }
    public Node getRegistryRootNode() {
		if(delegate != null) {
			return delegate.getRegistryRootNode();
		}
		return null;
    }
    
    public Node[] getWebServiceNodes(FileObject wsdlFile) {
		if(delegate != null) {
			return delegate.getWebServiceNodes( wsdlFile);
		}
		return null;
    }
    
    public boolean isServiceRegistered(String serviceName) {
		if(delegate != null) {
			return delegate.isServiceRegistered(serviceName);
		}
		return false;
    }
    
    public boolean registerService(FileObject wsdlFile, boolean replaceService) {
		if(delegate != null) {
	        return delegate.registerService( wsdlFile,  replaceService) ;
		}
		return false;
    }
    
    public boolean registerService(URL wsdlUrl, boolean replaceService) {
		if(delegate != null) {
			return delegate.registerService( wsdlUrl,  replaceService);
		}
		return false;
    }
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if(delegate != null) {
			delegate.addPropertyChangeListener(listener);
		}
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if(delegate != null) {
			delegate.removePropertyChangeListener(listener);
		}
	}

//    public void propertyChange(java.beans.PropertyChangeEvent evt) {
//       // System.out.println("propertyChange RegisterViewImplNetBeansSide");
//        try {
//            delegate = (WebServicesRegistryView) WebServiceModuleInstaller.getExtensionClassLoader().loadClass("org.netbeans.modules.websvc.registry.RegistryViewImpl").newInstance();//NOI18N            
//        } catch (Exception e) {
//          //  System.out.println("----- lacking app server classes");
//            delegate = null;
//        }
//    }
	
}
