/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.spi.client;

import java.io.IOException;
import java.util.List;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;

/**
 *
 * @author Peter Williams
 */
public interface WebServicesClientSupportImpl {
	
    public void addServiceClient(String serviceName, String packageName, 
        String sourceUrl, FileObject configFile, ClientStubDescriptor stubDescriptor);
	
    public void addServiceClientReference(String serviceName, String fqServiceName, String relativeWsdlPath, String mappingPath, String[] portSEIInfo);

    public void removeServiceClient(String serviceName);
		
    public FileObject getWsdlFolder(boolean create) throws IOException;

    public FileObject getDeploymentDescriptor();
	
    public List/*ClientStubDescriptor*/ getStubDescriptors();
    
    public List/*WsCompileClientEditorSupport.ServiceSettings*/ getServiceClients();
    
    public String getWsdlSource(String serviceName);
    
    public void setWsdlSource(String serviceName, String wsdlSource);
    
}
