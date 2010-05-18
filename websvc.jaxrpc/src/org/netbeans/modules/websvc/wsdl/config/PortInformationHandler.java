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

package org.netbeans.modules.websvc.wsdl.config;

import java.util.*;
import org.netbeans.modules.websvc.jaxrpc.PortInformation;
import org.netbeans.modules.websvc.jaxrpc.Utilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Peter Williams
 */
public class PortInformationHandler extends DefaultHandler implements PortInformation {

    private static final String W3C_WSDL_SCHEMA = "http://schemas.xmlsoap.org/wsdl";
    private static final String W3C_WSDL_SCHEMA_SLASH = "http://schemas.xmlsoap.org/wsdl/";
    private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    
    /* Currently, binding types are determined by the URI used for the binding subnode of
     * the wsdl:binding element.  E.g. soap:binding, http:binding, etc.
     *
     * Use the following constants to interpret the result of PortInfo.getBindingType()
     */
    private static final String SOAP_BINDING = "http://schemas.xmlsoap.org/wsdl/soap";
    private static final String SOAP_BINDING_SLASH = "http://schemas.xmlsoap.org/wsdl/soap/";
    
    // Data collection results
    private Map/*serviceName, ServiceInfo*/ serviceMap;
    private String targetNamespace;
    
    // Intermediate storage
    private List/*PortInfo*/ entirePortList;
    private List/*PortInfo*/ bindingPortList;
    private List/*String*/ wsdlImports;
    private String currentServiceName;
    private PortInfo bindingPort;
    private Set wscompileFeatures;
    private boolean serviceNameConflict;
    
    public PortInformationHandler() {
        serviceMap = new LinkedHashMap(5);
        entirePortList = new ArrayList();
        bindingPortList = new ArrayList();
        wsdlImports = new ArrayList();
        initWscompileFeatures();
    }
    
    public PortInformationHandler(String targetNamespace, Map serviceMap, List entirePortList, List bindingPortList, List wsdlImports) {
        this.targetNamespace=targetNamespace;
        this.serviceMap=serviceMap;
        this.entirePortList=entirePortList;
        this.bindingPortList=bindingPortList;
        this.wsdlImports=wsdlImports;
        initWscompileFeatures();
    }
    
    private void initWscompileFeatures() {
        wscompileFeatures = new HashSet(5);
        wscompileFeatures.add("wsi");wscompileFeatures.add("strict");
    }
    
    public void startElement(String uri, String localname, String qname, Attributes attributes) throws SAXException {
        if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
            if("portType".equals(localname)) { // NOI18N
                PortInfo key = new PortInfo();
                key.setPortType(attributes.getValue("name")); // NOI18N
                
                PortInfo pi = (PortInfo)getPortInfoByKey(key);
                if(pi == null) {
                    entirePortList.add(key);
                }
            } else if("binding".equals(localname)) { // NOI18N
                PortInfo key = new PortInfo();
                key.setBinding(attributes.getValue("name")); // NOI18N
                key.setPortType(getLocalPart(attributes.getValue("type"))); // NOI18N
                
                PortInfo pi = (PortInfo)getPortInfoByKey(key);
                if(pi == null) {
                    entirePortList.add(key);
                } else {
                    if(pi.getBinding() == null) {
                        pi.setBinding(key.getBinding());
                    }
                    if(pi.getPortType() == null) {
                        pi.setPortType(key.getPortType());
                    }
                }
                bindingPort = pi;
                bindingPortList.add(bindingPort);
            } else if("port".equals(localname)) { // NOI18N
                PortInfo key = new PortInfo();
                key.setPort(attributes.getValue("name")); // NOI18N
                key.setBinding(getLocalPart(attributes.getValue("binding"))); // NOI18N
                
                PortInfo pi = (PortInfo)getPortInfoByKey(key);
                if(pi == null) {
                    entirePortList.add(key);
                    pi = key;
                } else {
                    if(pi.getPort() == null) {
                        pi.setPort(key.getPort());
                    }
                    if(pi.getBinding() == null) {
                        pi.setBinding(key.getBinding());
                    }
                }
                
                assert currentServiceName != null;
                
                ServiceInfo serviceInfo = (ServiceInfo) serviceMap.get(currentServiceName);
                if(serviceInfo == null) {
                    serviceInfo = new ServiceInfo(currentServiceName);
                    serviceMap.put(currentServiceName, serviceInfo);
                }
                
                List servicePorts = serviceInfo.getPorts();
                servicePorts.add(pi);
            } else if("service".equals(localname)) { // NOI18N
                currentServiceName = attributes.getValue("name"); // NOI18N
                if (currentServiceName != null) {
                    currentServiceName = Utilities.removeSpacesFromServiceName(currentServiceName);
                }
            } else if("definitions".equals(localname)) { // NOI18N
                targetNamespace = attributes.getValue("targetNamespace"); //NOI18N
            } else if ("import".equals(localname)) { // NOI18N
                String location = attributes.getValue("location"); // NOI18N
                if (location!=null) wsdlImports.add(location);
            }
        } else if(bindingPort != null && "binding".equals(localname)) {
            bindingPort.setBindingType(normalizeUri(uri));
            if(SOAP_BINDING.equals(uri) || SOAP_BINDING_SLASH.equals(uri) ){
                String style = attributes.getValue("style"); //NOI18N
                if (style!=null && ("rpc".equals(style)|| style.endsWith(":rpc"))) { //NOI18N
                    wscompileFeatures.remove("strict"); //NOI18N
                    wscompileFeatures.add("rpcliteral"); //NOI18N
                }
            }
        } else if(W3C_XML_SCHEMA.equals(uri)) {
            if ("element".equals(localname)) {
                String elementType = attributes.getValue("type"); //NOI18N
                if (elementType!=null && ("anyType".equals(elementType) || elementType.endsWith(":anyType"))) { //NOI18N
                    wscompileFeatures.add("nodatabinding"); //NOI18N
                } 
            } else if ("import".equals(localname)) {
                if (attributes.getValue("schemaLocation")==null && attributes.getValue("namespace")!=null) { //NOI18N
                    wscompileFeatures.add("searchschema"); //NOI18N
                }
            }
        }
    }
    
    public Map getServices() {
        return serviceMap;
    }
    
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if(W3C_WSDL_SCHEMA.equals(uri) || W3C_WSDL_SCHEMA_SLASH.equals(uri)) {
            if("binding".equals(localname)) {
                bindingPort = null;
            } else if("service".equals(localname)) { //NOI18N
                if (currentServiceName!=null) {
                    PortInformation.ServiceInfo si = getServiceInfo(currentServiceName);
                    if (si!=null) {
                        List ports = si.getPorts();
                        for (Iterator it = ports.iterator();it.hasNext();) {
                            PortInformation.PortInfo pi = ( PortInformation.PortInfo)it.next();
                            if (currentServiceName.equals(pi.getPortType())) {
                                serviceNameConflict=true;
                                break;
                            }
                        }
                        
                    }
                }
                currentServiceName = null;
            }
        }
    }
    
    public boolean isServiceNameConflict() {
        return serviceNameConflict;
    }
    
    public String [] getServiceNames() {
        Set keys = serviceMap.keySet();
        return (String []) keys.toArray(new String[keys.size()]);
    }
    
    public PortInformation.ServiceInfo getServiceInfo(String serviceName) {
        // !PW FIXME this is another place where case sensitivity of service names
        // is biting me.  websvc/registry forces first character of service name
        // to uppercase (done inside JAXRPC/wscompile, so difficult to resolve).
        // That is likely where the key will come from.  However, we have stored
        // the true case sensitive service name here.  So to resolve matching
        // problems, for now, search on key, if not found, iterate service map,
        // doing case insensitive comparisons to find a match.
        //        return (ServiceInfo) serviceMap.get(serviceName);
        ServiceInfo result = (ServiceInfo) serviceMap.get(serviceName);
        if(result == null) {
            Iterator iter = serviceMap.values().iterator();
            while(iter.hasNext()) {
                ServiceInfo si = (ServiceInfo) iter.next();
                if(serviceName.equalsIgnoreCase(si.getServiceName())) {
                    result = si;
                    break;
                }
            }
        }
        return result;
    }
    
    public List getBindings() {
        return bindingPortList;
    }
    
    public List getImportedSchemas() {
        return wsdlImports;
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    public List getEntirePortList() {
        return entirePortList;
    }
    
    public Set getWscompileFeatures() {
        return wscompileFeatures;
    }
    
    /** If there is a trailing backslash on the uri, remove it.
     */
    private String normalizeUri(String uri) {
        String result = uri;
        
        if(uri.charAt(uri.length()-1) == '/') {
            result = uri.substring(0, uri.length()-1);
        }
        
        return result;
    }
    
    private PortInformation.PortInfo getPortInfoByKey(PortInfo key) {
        PortInfo result = null;
        Iterator iter = entirePortList.iterator();
        
        while(iter.hasNext()) {
            PortInfo pi = (PortInfo) iter.next();
            
            if(compareField(key.getPort(), pi.getPort())) {
                result = pi;
                break;
            } else if(compareField(key.getBinding(), pi.getBinding())) {
                result = pi;
                break;
            } else if(compareField(key.getPortType(), pi.getPortType())) {
                result = pi;
                break;
            }
        }
        
        return result;
    }
    
    private boolean compareField(final String key, final String match) {
        boolean result = false;
        
        if(match != null && match.equals(key)) {
            result = true;
        }
        
        return result;
    }
    
    private String getLocalPart(String uri) {
        String result = uri;
        int index = uri.lastIndexOf(':');
        if(index != -1) {
            result = uri.substring(index+1);
        }
        return result;
    }
    
    //    public void fatalError(SAXParseException exception) throws SAXException {
    //        super.fatalError(exception);
    //    }
    //
    //    public void error(SAXParseException exception) throws SAXException {
    //        super.error(exception);
    //    }
    //
    //    public void warning(SAXParseException exception) throws SAXException {
    //        super.warning(exception);
    //    }
    
    public static final class PortInfo implements PortInformation.PortInfo {
        private String portName;
        private String bindingName;
        private String bindingType;
        private String portTypeName;
        
        public PortInfo() {
        }
        
        public String getPortType() {
            return portTypeName;
        }
        
        void setPortType(String pt) {
            portTypeName = pt;
        }
        
        public String getBinding() {
            return bindingName;
        }
        
        void setBinding(String b) {
            bindingName = b;
        }
        
        public String getBindingType() {
            return bindingType;
        }
        
        void setBindingType(String bt) {
            bindingType = bt;
        }
        
        public String getPort() {
            return portName;
        }
        
        void setPort(String p) {
            portName = p;
        }
        
        public String toString() {
            return "(" + portName + ", " + bindingName + ", " + portTypeName + ")"; // NOI18N
        }
    }
    
    public static final class ServiceInfo implements PortInformation.ServiceInfo {
        
        private String serviceName;
        private List portList;
        
        public ServiceInfo(String name, List ports) {
            init(name, ports);
        }
        
        public ServiceInfo(String name) {
            init(name, new ArrayList());
        }
        
        private void init(String name, List ports) {
            this.serviceName = name;
            this.portList = ports;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public List/*PortInfo*/ getPorts() {
            return portList;
        }
        
    }
}
