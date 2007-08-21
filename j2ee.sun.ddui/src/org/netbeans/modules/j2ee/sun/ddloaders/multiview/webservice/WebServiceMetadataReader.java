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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.schema2beans.QName;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Peter Williams
 */
public class WebServiceMetadataReader extends CommonBeanReader {

    public WebServiceMetadataReader() {
        super(DDBinding.PROP_WEBSERVICE_DESC);
    }
    
    @Override
    public Map<String, Object> readAnnotations(DataObject dObj) {
        Map<String, Object> result = null;
        try {
            File key = FileUtil.toFile(dObj.getPrimaryFile());
            SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(key);
            if(dc != null) {
                J2eeModule module = dc.getJ2eeModule();
                if(module != null) {
                    if(J2eeModule.WAR.equals(module.getModuleType()) || J2eeModule.EJB.equals(module.getModuleType())) {
                        result = readWebservicesMetadata(module.getMetadataModel(WebservicesMetadata.class));
                    }
                }
            }
        } catch(MetadataModelException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return result;
    }
    
    /** Maps interesting fields from service-ref descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof WebserviceDescription []) {
            WebserviceDescription [] webServices = (WebserviceDescription []) beans;
            for(WebserviceDescription webServiceDesc: webServices) {
                String webServiceDescName = webServiceDesc.getWebserviceDescriptionName();
                if(Utils.notEmpty(webServiceDescName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, Object> webServiceDescMap = new HashMap<String, Object>();
                    result.put(webServiceDescName, webServiceDescMap);
                    webServiceDescMap.put(DDBinding.PROP_NAME, webServiceDescName);
                    
                    PortComponent [] ports = webServiceDesc.getPortComponent();
                    if(ports != null && ports.length > 0) {
                        Map<String, Object> portGroupMap = new HashMap<String, Object>();
                        webServiceDescMap.put(DDBinding.PROP_PORTCOMPONENT, portGroupMap);
                        for(PortComponent port: ports) {
                            String portName = port.getPortComponentName();
                            if(Utils.notEmpty(portName)) {
                                Map<String, Object> portMap = new HashMap<String, Object>(7);
                                portMap.put(DDBinding.PROP_NAME, portName);
                                portGroupMap.put(portName, portMap);
                                
                                addMapString(portMap, DDBinding.PROP_SEI, port.getServiceEndpointInterface());

                                // Wsdl port is actually 3 fields wrapped in a QName.  Do we really need it?
//                                port.getWsdlPort();
                                
                                ServiceImplBean serviceBean = port.getServiceImplBean();
                                if(serviceBean != null) {
                                    addMapString(portMap, DDBinding.PROP_SERVLET_LINK, serviceBean.getServletLink());
                                    addMapString(portMap, DDBinding.PROP_EJB_LINK, serviceBean.getEjbLink());
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
