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

package org.netbeans.modules.websvc.rest.codegen.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.support.JaxWsUtils;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlParameter;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.util.WebServiceLibReferenceHelper;
import org.netbeans.modules.websvc.rest.codegen.Constants;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nam
 */
public class JaxwsOperationInfo {

    private String categoryName;
    private String serviceName;
    private String portName;
    private String operationName;
    private String wsdlUrl;
    private Project project;
    private WebServiceData webServiceData;
    private WsdlService service;
    private WsdlOperation operation;
    private WsdlPort port;

    public JaxwsOperationInfo(String categoryName, String serviceName, String portName, String operationName, String wsdlURL, Project project) {
        this.categoryName = categoryName;
        this.serviceName = serviceName;
        this.portName = portName;
        this.operationName = operationName;
        this.wsdlUrl = wsdlURL;
        this.project = project;
    }

    public String getCategoryName() {
        return categoryName;
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

    public String getWsdlLocation() {
        initWsdlModelInfo();
        return webServiceData.getURL();
    }

    public void initWsdlModelInfo() {
        if (webServiceData != null) return;
        
        webServiceData = WebServiceListModel.getInstance().getWebServiceData(wsdlUrl, serviceName);
        if (webServiceData == null) {
            throw new IllegalStateException("There might have been earlier errors in initializing Web Services");
        }
        
        service = webServiceData.getWsdlService();
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
        WebServiceLibReferenceHelper.addDefaultJaxWsClientJar(project, webServiceData);
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
    
    public boolean needsSoapHandler() {
        return getSoapHeaderParameters().size() > 0;
    }
    
    private List<ParameterInfo> headerParams;
    public List<ParameterInfo> getSoapHeaderParameters() {
        if (headerParams == null) {
            headerParams = new java.util.ArrayList<ParameterInfo>();

            Map<QName,String> params = JaxWsUtils.getSoapHandlerParameters(
                    getXamWsdlModel(), getPort(), getOperation());
            for (Map.Entry<QName,String> entry : params.entrySet()) {
                Class type = Util.getType(project, entry.getValue());
                ParameterInfo info = new ParameterInfo(entry.getKey(), type, entry.getValue());
                info.setIsQueryParam(false);
                headerParams.add(info);
            }
        }
        return headerParams;
    }

    public WSDLModel getXamWsdlModel() {
        try {
            FileObject wsdlFO = FileUtil.toFileObject(new File(webServiceData.getURL()));;
            return WSDLModelFactory.getDefault().getModel(Utilities.createModelSource(wsdlFO, true));
        } catch(CatalogModelException ex) {
            Logger.global.log(Level.INFO, "", ex);
        }
        return null;
    }
    
    
}