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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nam
 */
public class JaxwsOperationInfo {

    String serviceName;
    String portName;
    String operationName;
    String wsdlUrl;
    Project project;
    String packageName;
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
        this.packageName = derivePackageName(wsdlURL, serviceName);
        support = JAXWSClientSupport.getJaxWsClientSupport(project.getProjectDirectory());
        if (support == null) {
            throw new IllegalArgumentException("Project " + project.getProjectDirectory() + " does not support JAX-WS client"); //NOI18N
        }
    }

    private String derivePackageName(String wsdlURL, String subPackage) {
        if (wsdlURL.startsWith("file:")) {
            throw new IllegalArgumentException("URL to access WSDL could not be local");
        }
        int iStart = wsdlURL.indexOf("://") + 3;
        int iEnd = wsdlURL.indexOf('/', iStart);
        String pakName = wsdlURL.substring(iStart, iEnd);
        String[] segments = pakName.split("\\.");
        StringBuilder sb = new StringBuilder(segments[segments.length - 1]);
        sb.append('.');
        sb.append(segments[segments.length - 2]);
        if (subPackage != null) {
            sb.append(".");
            sb.append(subPackage.toLowerCase());
        }
        return sb.toString();
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
        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getLocalizedMessage(), ex);
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

        setupWebServiceClient();
        service = getWsdlModel().getServiceByName(serviceName);

        if (service == null) {
            throw new IllegalArgumentException("Service " + serviceName + " does not exists");
        }
        port = service.getPortByName(portName);
        if (port == null) {
            throw new IllegalArgumentException("Port " + portName + " does not exists");
        }
        operation = findOperationByName(port, operationName);
        if (operation == null) {
            throw new IllegalArgumentException("Operation " + operationName + " does not exists");
        }
    }

    public void setupWebServiceClient() {
        if (getServiceClient() == null) {
            support.addServiceClient(serviceName, wsdlUrl, packageName, true);
        }
    }

    private WsdlModel getWsdlModel() {
        WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(getWsdlLocation());
        wsdlModeler.setPackageName(packageName);

        return wsdlModeler.getAndWaitForWsdlModel();
    }

    public static WsdlOperation findOperationByName(WsdlPort port, String name) {
        for (WsdlOperation o : port.getOperations()) {
            if (name.equals(o.getName())) {
                return o;
            }
        }
        return null;
    }

    public WsdlPort getPort() {
        initWsdlModelInfo();
        return port;
    }

    public WsdlOperation getOperation() {
        initWsdlModelInfo();
        return operation;
    }

    public WsdlService getService() {
        initWsdlModelInfo();
        return service;
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public List<WsdlParameter> getOutputParameters() {
        ArrayList<WsdlParameter> params = new ArrayList<WsdlParameter>();
        for (WsdlParameter p : getOperation().getParameters()) {
            if (p.isHolder()) {
                params.add(p);
            }
        }
        return params;
    }

    public static String getParamType(WsdlParameter param) {
        if (param.isHolder()) {
            String outputType = param.getTypeName();
            int iLT = outputType.indexOf('<');
            int iGT = outputType.indexOf('>');
            if (iLT > 0 || iGT > 0) {
                outputType = outputType.substring(iLT + 1, iGT).trim();
            }
            return outputType;
        } else {
            return param.getTypeName();
        }
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public String getOutputType() {
        initWsdlModelInfo();
        String outputType = getOperation().getReturnTypeName();
        if (Constants.VOID.equals(outputType)) {
            for (WsdlParameter p : getOperation().getParameters()) {
                if (p.isHolder()) {
                    outputType = getParamType(p);
                    break;
                }
            }
        }
        return outputType;
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public String[] getInputParameterNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (WsdlParameter p : getOperation().getParameters()) {
            if (!p.isHolder()) {
                names.add(p.getName());
            }
        }

        return names.toArray(new String[names.size()]);
    }

    //TODO maybe parse SEI class (using Retouche) for @WebParam.Mode annotation
    public Class[] getInputParameterTypes() {
        ArrayList<Class> types = new ArrayList<Class>();

        for (WsdlParameter p : getOperation().getParameters()) {
            if (!p.isHolder()) {
                int repeatCount = 0;
                Class type = null;

                // This is a hack to wait for the complex type to become
                // available. We will give up after 120 seconds.
                synchronized (this) {
                    try {
                        while (repeatCount < 60) {
                            type = Util.getType(project, p.getTypeName());

                            if (type != null) {
                                break;
                            }

                            repeatCount++;
                            this.wait(2000);
                        }
                    } catch (InterruptedException ex) {
                    }
                }

                // RESOLVE:
                // Need to failure gracefully by displaying an error dialog.
                // For now, set it to Object.class.
                if (type == null) {
                    type = Object.class;
                }

                types.add(type);
            }
        }

        return types.toArray(new Class[types.size()]);
    }
}