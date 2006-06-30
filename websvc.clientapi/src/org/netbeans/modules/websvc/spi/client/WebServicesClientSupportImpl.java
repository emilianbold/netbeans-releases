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

    public void addServiceClient(String serviceName, String packageName, 
        String sourceUrl, FileObject configFile, ClientStubDescriptor stubDescriptor, String[] wscompileFeatures);
    
    public void addServiceClientReference(String serviceName, String fqServiceName, String relativeWsdlPath, String mappingPath, String[] portSEIInfo);

    public void removeServiceClient(String serviceName);
		
    public FileObject getWsdlFolder(boolean create) throws IOException;

    public FileObject getDeploymentDescriptor();
	
    public List/*ClientStubDescriptor*/ getStubDescriptors();
    
    public List/*WsCompileClientEditorSupport.ServiceSettings*/ getServiceClients();
    
    public String getWsdlSource(String serviceName);
    
    public void setWsdlSource(String serviceName, String wsdlSource);
    
    public void setProxyJVMOptions(String proxyHost, String proxyPort);
    
}
