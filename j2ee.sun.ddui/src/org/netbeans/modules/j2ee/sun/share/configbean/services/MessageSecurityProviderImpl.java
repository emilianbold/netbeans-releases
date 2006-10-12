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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.share.configbean.services;

import java.beans.PropertyVetoException;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.dd.api.services.MessageSecurityProvider;
import org.netbeans.modules.j2ee.sun.share.configbean.ServiceRef;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.WebServiceDescriptor;
import org.netbeans.modules.j2ee.sun.share.configbean.WebServices;
import org.openide.ErrorManager;


/** Implementation of MessageSecurityProvider interface
 * 
 * @author Peter Williams
 */
public class MessageSecurityProviderImpl implements MessageSecurityProvider {
    
    public MessageSecurityProviderImpl() {
        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "MessageSecurityProvider implementation created.");
    }
    
    /* Retrieve current MessageSecurityBinding data for the specified endpoint.
     *
     * FIXME How to differentiate the errors "service not found", "port not found", and 
     *   "no binding" from each other?
     */
    public MessageSecurityBinding getEndpointBinding(File sunDD, String endpointName, String portName) {
        MessageSecurityBinding result = null;

        // Validate input parameters
        validateEndpointParams(endpointName, portName);

        // get configuration (also validates sunDD parameter).
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        
        // locate endpoint DConfigBean
        WebServices wsRoot = config.getWebServicesRoot();
        if(wsRoot != null) {
            WebServiceDescriptor wsBean = wsRoot.getWebServiceDescriptor(endpointName);
            if(wsBean != null) {
                // found endpoint, now locate port component reference
                WebserviceEndpoint endpoint = wsBean.getWebServiceEndpoint(portName);
                if(endpoint != null) {
                    MessageSecurityBinding binding;
                    try {
                        binding = endpoint.getMessageSecurityBinding();
                        if(binding != null) {
                            // !PW FIXME Find a way to use cloneVersion here.  Not required until
                            // there are different versions of this object but there will be.
                            result = (MessageSecurityBinding) binding.clone();
                        }
                    } catch (VersionNotSupportedException ex) {
                        // return null for this case.
                    }
                }
            }
        }
        
        return result;
    }

    /* Set new MessageSecurityBinding data for the specified endpoint.
     */
    public boolean setEndpointBinding(File sunDD, String endpointName, String portName, MessageSecurityBinding binding) {
        boolean result = false;
        
        // Validate input parameters
        validateEndpointParams(endpointName, portName);

        // get configuration (also validates sunDD parameter).
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        
        // locate endpoint DConfigBean
        WebServices wsRoot = config.getWebServicesRoot();
        if(wsRoot != null) {
            WebServiceDescriptor wsBean = wsRoot.getWebServiceDescriptor(endpointName);
            if(wsBean != null) {
                try {
                    // found web service DConfigBean.  Now set the binding on the specified endpoint.
                    wsBean.setMessageSecurityBinding(portName, binding);
                    result = true;
                } catch (VersionNotSupportedException ex) {
                    // How to notify caller here?  Endpoint does not support Message Security Bindings.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                } catch (PropertyVetoException ex) {
                    // Suppress this.  Shouldn't happen anyway.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        
        return result;
    }

    /* Retrieve current MessageSecurityBinding data for the specified webservice client.
     *
     * NOTE: Temporarily, this API does not allow the user to specify which port they
     *  want the binding for.  If the client defines multiple ports, then only the first
     *  MessageSecurityBinding will be returned.  Be sure to note corollary in set method.
     *
     * FIXME: How to report failures?
     *
     * @deprecated
     */
    public MessageSecurityBinding getServiceRefBinding(File sunDD, String serviceRefName) {
        MessageSecurityBinding result = null;

        // Validate input parameters
        validateServiceRefParams(serviceRefName);

        // get configuration (also validates sunDD parameter).
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        
        // locate all service-ref DDBeans
        ServiceRef serviceRef = Utils.findServiceRef(config, serviceRefName);
        if(serviceRef != null) {
            // !PW FIXME for now, just return the first port binding.  Need to work out
            // how to return multiple bindings in a way that the caller knows which bindings
            // apply to which port.
            List portInfoList = serviceRef.getPortInfos();
            if(portInfoList.size() > 0) {
                PortInfo portInfo = (PortInfo) portInfoList.get(0);
                MessageSecurityBinding binding;
                try {
                    binding = portInfo.getMessageSecurityBinding();
                    if(binding != null) {
                        result = binding;
                    }
                } catch (VersionNotSupportedException ex) {
                    // return null for this case.
                }
            }
        }
        
        return result;
    }
    
    /* Retrieve current MessageSecurityBinding data for the specified wsdl-port of the
     * specified webservice client.
     */
    public MessageSecurityBinding getServiceRefBinding(File sunDD, String serviceRefName, 
            String namespaceURI, String localpart) {
        MessageSecurityBinding result = null;

        // Validate input parameters
        validateServiceRefParams(serviceRefName, namespaceURI, localpart);

        // get configuration (also validates sunDD parameter).
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        
        // locate all service-ref DDBeans
        ServiceRef serviceRef = Utils.findServiceRef(config, serviceRefName);
        if(serviceRef != null) {
            List portInfoList = serviceRef.getPortInfos();
            Iterator iter = portInfoList.iterator();
            while(iter.hasNext()) {
                PortInfo portInfo = (PortInfo) iter.next();
                WsdlPort port = portInfo.getWsdlPort();
                if(port != null && namespaceURI.equals(port.getNamespaceURI()) && localpart.equals(port.getLocalpart())) {
                    MessageSecurityBinding binding;
                    try {
                        binding = portInfo.getMessageSecurityBinding();
                        if(binding != null) {
                            result = binding;
                        }
                    } catch (VersionNotSupportedException ex) {
                        // return null for this case.
                    }
                    break;
                }
            }
        }
        
        return result;
    }

    /* Set the MessageSecurityBinding data for the specified webservice client.  The
     * current implementation applies this binding data to all configured ports on this
     * client.
     *
     * Note that the binding instance passed in is cloned for all ports it is configured to
     * so subsequent modification of that instance after this call returns will not affect
     * the data that was configured by this call.  A subsequent call to setServiceRefBinding()
     * would be required to apply new binding data.
     *
     * @deprecated
     */
    public boolean setServiceRefBinding(File sunDD, String serviceRefName, MessageSecurityBinding binding) {
        boolean result = false;
        
        // Validate input parameters
        validateServiceRefParams(serviceRefName);

        // get configuration (also validates sunDD parameter).
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        
        // locate all service-ref DDBeans
        ServiceRef serviceRef = Utils.findServiceRef(config, serviceRefName);
        if(serviceRef != null) {
            try {
                // !PW FIXME For now, set this message security binding data to all ports defined
                // on this client.  We need a definition that allows configuring a specific port.
                serviceRef.setMessageSecurityBinding(binding);
                result = true;
            } catch (VersionNotSupportedException ex) {
                // How to notify caller here?  Endpoint does not support Message Security Bindings.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (PropertyVetoException ex) {
                // Suppress this.  Shouldn't happen anyway.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return result;
    }

    /* Set the MessageSecurityBinding data for the specified wsdl-port of the specified 
     * webservice client.
     *
     * Note that the binding instance passed in is cloned for all ports it is configured to
     * so subsequent modification of that instance after this call returns will not affect
     * the data that was configured by this call.  A subsequent call to setServiceRefBinding()
     * would be required to apply new binding data.
     */
    public boolean setServiceRefBinding(File sunDD, String serviceRefName, String namespaceURI, 
            String localpart, MessageSecurityBinding binding) {
        boolean result = false;
        
        // Validate input parameters
        validateServiceRefParams(serviceRefName, namespaceURI, localpart);

        // get configuration (also validates sunDD parameter).
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        
        // locate all service-ref DDBeans
        ServiceRef serviceRef = Utils.findServiceRef(config, serviceRefName);
        if(serviceRef != null) {
            try {
                serviceRef.setMessageSecurityBinding(namespaceURI, localpart, binding);
                result = true;
            } catch (VersionNotSupportedException ex) {
                // How to notify caller here?  Endpoint does not support Message Security Bindings.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } catch (PropertyVetoException ex) {
                // Suppress this.  Shouldn't happen anyway.
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        
        return result;
    }
    
    /** Creates new MessageSecurityBinding instance of the proper version.
     */
    public MessageSecurityBinding newMessageSecurityBinding(File sunDD) {
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        return config.getStorageFactory().createMessageSecurityBinding();
    }
    
// ------------------- Proof of Concept methods... not implemented yet.-----------------------------
    
//    /**
//     * !PW Could we use a DDBean from the DDAPI for the standard descriptor (or the corresponding
//     *  merged node from the proposed annotation-ddbean merged provider) that refers to the
//     *  appropriate webservice endpoint or port-info DD?  Then on plugin side, we can translate
//     *  that node to our tree and lookup the bound endpoint or port-info structure, eliminating
//     *  possible name contention (and being able to differentiate between servlet/endpoint and service/port,
//     *  which for string based api requires an extra redundant argument to clarify).  For example...
//     *
//     *  sunddapi only depends on schema2beans and xerces (and now filesystems)  Adding j2eeserver
//     *  to get access to DDBean (from JSR-88) is probably a bad idea.  Can use BaseBean for now, 
//     *  but this is also a bad idea.
//     *
//     *  Remaining options are add a new module that _can_ depend on both j2eeserver and sunddapi
//     *  or find another type.  We could hack this by declaring type Object for api purposes
//     *  since both consumer (some highlevel project module) and implementor (sunddui) can depend
//     *  on j2ee/ddapi to get access to the real DDBean.  Once solved, we can resolve ambiguities
//     *  in setMessageSecurityBinding() the same way.
//     *
//     * @param sunDD File refering to the sun descriptor file containing the interesting
//     *  information.  This must be _the_ descriptor file for the specified j2eemodule or
//     *  an IllegalArgumentException will be thrown.
//     * @param refBean Instance of object implementing javax.enterprise.deploy.model.DDBean that
//     *  represents the descriptor node from which to get service endpoint/port information and thus
//     *  the corresponding binding information?
//     * 
//     * We need more info here too.  What, exactly?
//     */
//    public MessageSecurityBinding getMessageSecurityBinding(File sunDD, Object refBean) {
//        System.out.println("MSP.getMSB: " + sunDD.getName() + ", " + refBean.toString());
//        return null;
//    }

    /** Implementation details
     */
    private SunONEDeploymentConfiguration getConfiguration(File sunDD) {
        SunONEDeploymentConfiguration cachedDC = SunONEDeploymentConfiguration.getConfiguration(sunDD);
        
        if(sunDD == null) {
            throw new IllegalArgumentException("Deployment descriptor file reference cannot be null."); // NOI18N
        }
        
        // handle null configuration here.  Do we want to use checked exceptions for this case?
        if(cachedDC == null) {
            throw new IllegalStateException("No Sun deployment configuration found for descriptor " + sunDD.getPath()); // NOI18N
        }

        return cachedDC;
    }
    
    private void validateEndpointParams(String endpointName, String portName) {
        if(!Utils.notEmpty(endpointName)) {
            throw new IllegalArgumentException("Web service description name cannot be empty or null."); // NOI18N
        }
        if(!Utils.notEmpty(portName)) {
            throw new IllegalArgumentException("Web service port name cannot be empty or null."); // NOI18N
        }
    }
    
    private void validateServiceRefParams(String serviceRefName) {
        if(!Utils.notEmpty(serviceRefName)) {
            throw new IllegalArgumentException("Web service reference name cannot be empty or null."); // NOI18N
        }
    }

    private void validateServiceRefParams(String serviceRefName, String namespaceURI, String localpart) {
        if(!Utils.notEmpty(serviceRefName)) {
            throw new IllegalArgumentException("Web service reference name cannot be empty or null."); // NOI18N
        }
        if(!Utils.notEmpty(namespaceURI)) {
            throw new IllegalArgumentException("Wsdl-port namespaceURI for service-ref cannot be empty or null."); // NOI18N
        }
        if(!Utils.notEmpty(localpart)) {
            throw new IllegalArgumentException("Wsdl-port localpart for service-ref cannot be empty or null."); // NOI18N
        }
    }
    
    private WebserviceEndpoint getEndpoint(SunONEDeploymentConfiguration config, String endpointName, String portName) {
        WebserviceEndpoint endpoint = null;
        
        WebServices wsRoot = config.getWebServicesRoot();
        if(wsRoot != null) {
            WebServiceDescriptor wsBean = wsRoot.getWebServiceDescriptor(endpointName);
            if(wsBean != null) {
                // found endpoint, now locate port component reference
                endpoint = wsBean.getWebServiceEndpoint(portName);
            }
        }
        
        return endpoint;
    }

}
