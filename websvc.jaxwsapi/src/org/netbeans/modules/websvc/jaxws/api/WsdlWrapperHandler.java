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

package org.netbeans.modules.websvc.jaxws.api;

import java.util.HashMap;
import java.util.Map;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/** ConteHandler that gives information if wsdl wrapper need to be created
 *  This is the case when service element is missing
 *
 * @author mkuchtiak
 */
public class WsdlWrapperHandler extends DefaultHandler{
    
    public static final String WSDL_SOAP_URI = "http://schemas.xmlsoap.org/wsdl/"; //NOI18N
    public static final String SOAP_BINDING_PREFIX = "http://schemas.xmlsoap.org/wsdl/soap"; //NOI18N
    
    private boolean isService, isPortType, isBinding;
    private String tns;
    private Map<String, String> prefixes;
    private Map<String, BindingInfo> bindings;
    private Map<String, String> ports;
    private BindingInfo bindingInfo;
    private boolean insideBinding, insideService;
    
    /** Creates a new instance of WsdlWrapperHandler */
    public WsdlWrapperHandler() {
        prefixes = new HashMap<String, String>();
        bindings = new HashMap<String, BindingInfo>();
        ports = new HashMap<String, String>();
    }    

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (!prefixes.containsKey(uri)) prefixes.put(uri,prefix);
    }
    
    public void startElement(String uri, String localName, String qname, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if("portType".equals(localName)) { // NOI18N
            isPortType=true;
        } else if("binding".equals(localName)) { // NOI18N
            isBinding=true;
            if (WSDL_SOAP_URI.equals(uri)) {
                String bindingName=attributes.getValue("name"); // NOI18N
                insideBinding=true;
                if (bindingName!=null) {
                    bindingInfo = new BindingInfo(bindingName);
                    bindings.put(bindingName,bindingInfo);
                }
            } else if (insideBinding && bindingInfo!=null && uri.startsWith(SOAP_BINDING_PREFIX)) {
                bindingInfo.setBindingType(attributes.getValue("transport")); //NOI18N
            }
        } else if("service".equals(localName)) { // NOI18N
            isService=true;
            insideService=true;
        } else if("port".equals(localName) && insideService) { // NOI18N
            String portName = attributes.getValue("name"); // NOI18N
            if (portName!=null) ports.put(portName, attributes.getValue("binding")); // NOI18N
        } else if("definitions".equals(localName)) { // NOI18N
            tns=attributes.getValue("targetNamespace"); // NOI18N
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if("binding".equals(localName) && WSDL_SOAP_URI.equals(uri)) { // NOI18N
            bindingInfo=null;
            insideBinding=false;
        } else if ("service".equals(localName)) {
            insideService=false;
        }
    }
    
    public String getBindingTypeForPort(String name) {
        String fullBindingName = ports.get(name);
        if (fullBindingName!=null) {
            String bindingName = getLocalPart(fullBindingName);
            BindingInfo info = bindings.get(bindingName);
            if (info!=null) return info.getBindingType();
        }
        return null;
    }
    
    public boolean isServiceElement() {
        return isService;
    }
    
    public String getTargetNsPrefix() {
        return (prefixes == null) ? null : prefixes.get(tns);
    }

    public void endDocument() throws SAXException {
        // throw exception if service & binding & portType are missing 
        if (!isService && !isBinding && !isPortType) throw new SAXException("Missing wsdl elements (wsdl:service | wsdl:binding | wsdl:portType)"); //NOI18N
    }
    
    private class BindingInfo {
        private String bindingName;
        private String bindingType;
        
        BindingInfo(String bindingName) {
            this.bindingName=bindingName;
        }
        
        void setBindingType(String bindingType) {
            this.bindingType=bindingType;
        }
        
        String getBindingType() {
            return bindingType;
        }
    }
    
    private String getLocalPart(String fullName) {
        int index = fullName.indexOf(":"); //NOI18N
        return (index>=0?fullName.substring(index+1):fullName);
    }
}
