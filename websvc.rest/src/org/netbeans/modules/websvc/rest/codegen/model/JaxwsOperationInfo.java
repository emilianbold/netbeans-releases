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
package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nam
 */
public class JaxwsOperationInfo {
    String serviceName;
    String portName = "zipinfoSoap";
    String operationName = "GetCityState";
    String wsdlUrl;
    Project project;
    JAXWSClientSupport support;
    Client client;

    WsdlService service;
    WsdlOperation operation;
    WsdlPort port;
    
    public JaxwsOperationInfo(String serviceName, String portName, String operationName, String wsdlURL, Project project) {
        this.serviceName = serviceName;
        this.portName = portName;
        this.operationName = operationName;
        this.wsdlUrl = wsdlURL;
        this.project = project;
        support = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        if (support == null) {
            throw new IllegalArgumentException("Project "+project.getProjectDirectory()+" does not support JAX-WS client"); //NOI18N
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPortName() {
        return portName;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getWsdlURL() {
        return wsdlUrl;
    }

    public URL getWsdlLocation() {
        try {
            String clientName = getServiceClient().getName();
            String localWsdlFilePath = getServiceClient().getLocalWsdlFile();
            File folder = FileUtil.toFile(support.getLocalWsdlFolderForClient(clientName, false));
            return new File(folder, localWsdlFilePath).toURL();
        } catch(MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "getWsdlModel", ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public Client getServiceClient() {
        if (client != null) {
            return client;
        }
        List clients = support.getServiceClients();
        for (Object o : clients) {
            if (o instanceof Client) {
                Client c = (Client) o;
                if (serviceName.equals(c.getName()) && wsdlUrl.equals(c.getWsdlUrl())) {
                    return c;
                }
            }
        }
        return null;
    }

    public void initWsdlModelInfo() {
        if (service != null) {
            return;
        }
        
        service = getWsdlModel().getServiceByName(serviceName);
        if (service == null) {
            throw new IllegalArgumentException("Service "+serviceName+" does not exists");
        }
        port = service.getPortByName(portName);
        if (port == null) {
            throw new IllegalArgumentException("Port "+portName+" does not exists");
        }
        operation = findOperationByName(port, operationName);
        if (operation == null) {
            throw new IllegalArgumentException("Operation "+operationName+" does not exists");
        }
    }
    
    public void setupWebServiceClient() {
        if (getServiceClient() == null) {
            support.addServiceClient(serviceName, wsdlUrl, null, true);
        }
    }
    
    private WsdlModel getWsdlModel() {
        return WsdlModelerFactory.getDefault().getWsdlModeler(getWsdlLocation()).getAndWaitForWsdlModel();
    }
    
    public static WsdlOperation findOperationByName(WsdlPort port, String name) {
        for (WsdlOperation o : port.getOperations()) {
            if (name.equals(o.getName())) {
                return o;
            }
        }
        return null;
    }
    
    public String getOutputJAXBClass() {
        initWsdlModelInfo();
        return operation.getReturnTypeName();
    }

    public WsdlPort getPort() {
        return port;
    }
    
    public WsdlOperation getOperation() {
        return operation;
    }
    
    public WsdlService getService() {
        return service;
    }
}
