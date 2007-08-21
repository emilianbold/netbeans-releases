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
import org.netbeans.modules.j2ee.dd.api.webservices.Webservices;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


/**
 *
 * @author Peter Williams
 */
public class PortComponentMetadataReader extends CommonBeanReader {

    private String parentName;
    
    public PortComponentMetadataReader(final String parentName) {
        super(DDBinding.PROP_PORTCOMPONENT);
        this.parentName = parentName;
    }
    
    /** For normalizing data structures
     *    /webservices -> -> /webservices/webservice-description[webservice-description-name="xxx"]
     * 
     * TODO This mechanism will probably need optimization and caching to perform
     * for larger files.
     */
    @Override
    protected CommonDDBean normalizeParent(CommonDDBean parent) {
        if(parentName != null && parent instanceof Webservices) {
            parent = findWebServiceDescByName((Webservices) parent, parentName);
        }
        return parent;
    }
    
    private CommonDDBean findWebServiceDescByName(Webservices webservices, String parentName) {
        return findWebServiceDescByName(webservices.getWebserviceDescription(), parentName);
    }
    
    private CommonDDBean findWebServiceDescByName(WebserviceDescription [] descs, String wsDescName) {
        CommonDDBean match = null;
        if(descs != null) {
            for(WebserviceDescription ws: descs) {
                if(wsDescName.equals(ws.getWebserviceDescriptionName())) {
                    match = ws;
                    break;
                }
            }
        }
        return match;
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
    
    /** Maps interesting fields from port-component descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof PortComponent []) {
            PortComponent [] ports = (PortComponent []) beans;
            for(PortComponent port: ports) {
                String portName = port.getPortComponentName();
                if(Utils.notEmpty(portName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, Object> portMap = new HashMap<String, Object>();
                    result.put(portName, portMap);
                    portMap.put(DDBinding.PROP_NAME, portName);
                    
                    addMapString(portMap, DDBinding.PROP_SEI, port.getServiceEndpointInterface());

                    // Wsdl port is actually 3 fields wrapped in a QName.  Do we really need it?
//                    port.getWsdlPort();

                    ServiceImplBean serviceBean = port.getServiceImplBean();
                    if(serviceBean != null) {
                        addMapString(portMap, DDBinding.PROP_SERVLET_LINK, serviceBean.getServletLink());
                        addMapString(portMap, DDBinding.PROP_EJB_LINK, serviceBean.getEjbLink());
                    }
                }
            }
        }
        return result;
    }
}
