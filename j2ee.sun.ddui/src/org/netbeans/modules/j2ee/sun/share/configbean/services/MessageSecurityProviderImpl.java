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

import java.io.File;
import java.io.IOException;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.services.MessageSecurityProvider;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
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
        
        try {
            if(sunDD.exists()) {
                EndpointFinder finder = createEndpointFinder(config, endpointName, portName, false);
                if(finder != null) {
                    WebserviceEndpoint endpoint = finder.getEndpoint(sunDD, config);
                    if(endpoint != null) {
                        MessageSecurityBinding binding = endpoint.getMessageSecurityBinding();
                        if(binding != null) {
                            // !PW FIXME Find a way to use cloneVersion here.  Not required until
                            // there are different versions of this object but there will be.
                            result = (MessageSecurityBinding) binding.clone();
                        }
                    }
                }
            }
        } catch (VersionNotSupportedException ex) {
            // return null for this case.
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
        
        try {
            EndpointFinder finder = createEndpointFinder(config, endpointName, portName, true);
            if(finder != null) {
                WebserviceEndpoint endpoint = finder.getEndpoint(sunDD, config);
                endpoint.setMessageSecurityBinding((MessageSecurityBinding) binding.clone());
                result = true;
            }
        } catch (VersionNotSupportedException ex) {
            // return false for this case.
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return result;
    }

    /* Retrieve current MessageSecurityBinding data for the specified webservice client.
     *
     * NOTE: Temporarily, this API does not allow the user to specify which port they
     *  want the binding for.  If the client defines multiple ports, then only the first
     *  MessageSecurityBinding will be returned.  Be sure to note corollary in set method.
     *
     * @deprecated
     */
    public MessageSecurityBinding getServiceRefBinding(File sunDD, String serviceRefName) {
        throw new UnsupportedOperationException("Deprecated, use getServiceRefBinding(File sunDD, " +
                "String serviceRefName, String namespaceURI, String localpart))");
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
        
        try {
            if(sunDD.exists()) {
                ServiceRefFinder finder = createServiceRefFinder(config, serviceRefName, namespaceURI, localpart, false);
                if(finder != null) {
                    PortInfo portInfo = finder.getClientPort(sunDD, config);
                    if(portInfo != null) {
                        MessageSecurityBinding binding = portInfo.getMessageSecurityBinding();
                        if(binding != null) {
                            // !PW FIXME Find a way to use cloneVersion here.  Not required until
                            // there are different versions of this object but there will be.
                            result = (MessageSecurityBinding) binding.clone();
                        }
                    }
                }
            }
        } catch (VersionNotSupportedException ex) {
            // return null for this case.
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
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
        throw new UnsupportedOperationException("Deprecated, use setServiceRefBinding(File sunDD, " +
                "String serviceRefName, String namespaceURI, String localpart, MessageSecurityBinding binding)");
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

        try {
            ServiceRefFinder finder = createServiceRefFinder(config, serviceRefName, namespaceURI, localpart, true);
            if(finder != null) {
                PortInfo portInfo = finder.getClientPort(sunDD, config);
                portInfo.setMessageSecurityBinding((MessageSecurityBinding) binding.clone());
                result = true;
            }
        } catch (VersionNotSupportedException ex) {
            // return false for this case.
        } catch(IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        
        return result;
    }
    
    /** Creates new MessageSecurityBinding instance of the proper version.
     */
    public MessageSecurityBinding newMessageSecurityBinding(File sunDD) {
        SunONEDeploymentConfiguration config = getConfiguration(sunDD);
        return config.getStorageFactory().createMessageSecurityBinding();
    }
    
    /** EndpointFinder is a helper class that abstracts reading the standard descriptor
     *  and/or metamodel to locate the correct linkname (servlet or ejb) and then
     *  locating the appropriate endpoint entry under the linked entry in the sun
     *  descriptor.
     */
    private static abstract class EndpointFinder {

        protected final String webServiceName;
        protected final String portName;
        protected final boolean create;
        
        private EndpointFinder(String wsName, String portName, boolean create) {
            this.webServiceName = wsName;
            this.portName = portName;
            this.create = create;
        }
        
        WebserviceEndpoint getEndpoint(File sunDD, SunONEDeploymentConfiguration config) throws IOException {
            WebserviceEndpoint result = null;
            
            String linkName = findLinkName(config);
            if(linkName != null && linkName.length() > 0) {
                RootInterface rootDD = config.getSunDDRoot(sunDD, create);
                result = findEndpoint(rootDD, linkName);
            }
            
            return result;
        }
        
        private WebserviceEndpoint findEndpoint(RootInterface rootDD, String linkName) throws IOException {
            return findEndpoint(getEndpoints(rootDD, linkName));
        }
        
        protected abstract WebserviceEndpoint [] getEndpoints(RootInterface rootDD, String linkName);
        
        private WebserviceEndpoint findEndpoint(WebserviceEndpoint [] endpoints) {
            if(endpoints != null && endpoints.length > 0) {
                for(WebserviceEndpoint endpoint: endpoints) {
                    if(portName.equals(endpoint.getPortComponentName())) {
                        return endpoint;
                    }
                }
            }
            // !PW FIXME need to create a new endpoint here if create = true!!!
            return null;
        }
        
        private String findLinkName(final SunONEDeploymentConfiguration config) throws IOException {
            // Search standard DD
            String linkName = findLinkName(config.getWebServicesRootDD());
            if(linkName != null) {
                // Search metadata
                MetadataModel<WebservicesMetadata> model = config.getMetadataModel(WebservicesMetadata.class);
                if(model.isReady()) {
                    linkName = model.runReadAction(new MetadataModelAction<WebservicesMetadata, String>() {
                        public String run(WebservicesMetadata metadata) throws Exception {
                            return findLinkName(metadata.getRoot());
                        }
                    });
                }
            }
            return linkName;
        }
        
        private String findLinkName(org.netbeans.modules.j2ee.dd.api.webservices.Webservices wsRoot) {
            org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription [] descs = wsRoot.getWebserviceDescription();
            if(descs != null && descs.length > 0) {
                for(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription desc: descs) {
                    if(webServiceName.equals(desc.getWebserviceDescriptionName())) {
                        return findLinkName(desc);
                    }
                }
            }
            return null;
        }
        
        private String findLinkName(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription desc) {
            org.netbeans.modules.j2ee.dd.api.webservices.PortComponent [] ports = desc.getPortComponent();
            if(ports != null && ports.length > 0) {
                for(org.netbeans.modules.j2ee.dd.api.webservices.PortComponent port: ports) {
                    if(portName.equals(port.getPortComponentName())) {
                        org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean serviceBean = port.getServiceImplBean();
                        return (serviceBean != null) ? getLinkName(serviceBean) : null;
                    }
                }
            }
            return null;
        }
        
        protected abstract String getLinkName(org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean serviceBean);
        
    }

    private static class ServletEndpointFinder extends EndpointFinder {
        
        private ServletEndpointFinder(String wsName, String portName, boolean create) {
            super(wsName, portName, create);
        }
        
        protected String getLinkName(org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean serviceBean) {
            return serviceBean.getServletLink();
        }
        
        protected WebserviceEndpoint [] getEndpoints(RootInterface rootDD, String linkName) {
            if(rootDD instanceof SunWebApp) {
                SunWebApp sunWebApp = (SunWebApp) rootDD;
                Servlet [] servlets = sunWebApp.getServlet();
                if(servlets != null) {
                    for(Servlet servlet: servlets) {
                        if(linkName.equals(servlet.getServletName())) {
                            return servlet.getWebserviceEndpoint();
                        }
                    }
                }
                
                if(create) {
                    Servlet newServlet = sunWebApp.newServlet();
                    newServlet.setServletName(linkName);
                    sunWebApp.addServlet(newServlet);
                    
                    WebserviceEndpoint endpoint = newServlet.newWebserviceEndpoint();
                    endpoint.setPortComponentName(portName);
                    newServlet.addWebserviceEndpoint(endpoint);
                    
                    return newServlet.getWebserviceEndpoint();
                }
            } else {
                if(create) {
                    throw new IllegalStateException("Unable to create or access ddapi of proper type, expected SunWebApp.");
                }
            }
            return null;
        }
        
    }
    
    private static class EjbEndpointFinder extends EndpointFinder {
        
        private EjbEndpointFinder(String wsName, String portName, boolean create) {
            super(wsName, portName, create);
        }
        
        protected String getLinkName(org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean serviceBean) {
            return serviceBean.getEjbLink();
        }
        
        protected WebserviceEndpoint [] getEndpoints(RootInterface rootDD, String linkName) {
            if(rootDD instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) rootDD;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if(eb != null) {
                    Ejb [] ejbs = eb.getEjb();
                    if(ejbs != null) {
                        for(Ejb ejb: ejbs) {
                            if(linkName.equals(ejb.getEjbName())) {
                                return ejb.getWebserviceEndpoint();
                            }
                        }
                    }
                } 
                
                if(create) {
                    if(eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }
                    
                    Ejb newEjb = eb.newEjb();
                    newEjb.setEjbName(linkName);
                    eb.addEjb(newEjb);
                    
                    WebserviceEndpoint endpoint = newEjb.newWebserviceEndpoint();
                    endpoint.setPortComponentName(portName);
                    newEjb.addWebserviceEndpoint(endpoint);
                    
                    return newEjb.getWebserviceEndpoint();
                }
            } else {
                if(create) {
                    throw new IllegalStateException("Unable to create or access ddapi of proper type, expected SunEjbJar.");
                }
            }
            return null;
        }
    }

    private EndpointFinder createEndpointFinder(SunONEDeploymentConfiguration config, 
            String wsName, String portName, boolean create) {
        EndpointFinder result = null;
        J2eeModule module = config.getJ2eeModule();
        if(module != null) {
            if(ModuleType.WAR.equals(module.getModuleType())) {
                result = new ServletEndpointFinder(wsName, portName, create);
            } else if(ModuleType.EJB.equals(module.getModuleType())) {
                result = new EjbEndpointFinder(wsName, portName, create);
            }
        }
        return result;
    }
    
    /** ServiceRefFinder is a helper class that abstracts reading the standard descriptor
     *  and/or metamodel to locate the servic-ref (and ejb-name if ejb project) and then
     *  locating the appropriate port-info entry under the linked service-ref entry in the sun
     *  descriptor.
     */
    private abstract static class ServiceRefFinder {

        protected final String serviceRefName;
        protected final String localpart;
        protected final String namespaceURI;
        protected final boolean create;
        
        private ServiceRefFinder(String srName, String namespaceURI, String localpart, boolean create) {
            this.serviceRefName = srName;
            this.localpart = localpart;
            this.namespaceURI = namespaceURI;
            this.create = create;
        }

        PortInfo getClientPort(File sunDD, SunONEDeploymentConfiguration config) throws IOException {
            RootInterface rootDD = config.getSunDDRoot(sunDD, create);
            return findPort(rootDD, config);
        }
        
        protected abstract PortInfo findPort(RootInterface rootDD, SunONEDeploymentConfiguration config) throws IOException;
        
        protected PortInfo findPort(ServiceRef serviceRef) {
            PortInfo portInfo = null;
            
            PortInfo [] ports = serviceRef.getPortInfo();
            if(ports != null && ports.length > 0) {
                for(PortInfo pi: ports) {
                    WsdlPort wsdlPort = pi.getWsdlPort();
                    if(wsdlPort != null) {
                        if(localpart.equals(wsdlPort.getLocalpart()) && namespaceURI.equals(wsdlPort.getNamespaceURI())) {
                            portInfo = pi;
                            break;
                        }
                    }
                }
            }
            
            if(portInfo == null && create) {
                portInfo = createPortInfo(serviceRef);
            }
            
            return portInfo;
        }
        
        protected PortInfo createPortInfo(ServiceRef serviceRef) {
            PortInfo portInfo = serviceRef.newPortInfo();
            WsdlPort wsdlPort = portInfo.newWsdlPort();
            wsdlPort.setLocalpart(localpart);
            wsdlPort.setNamespaceURI(localpart);
            portInfo.setWsdlPort(wsdlPort);
            serviceRef.addPortInfo(portInfo);
            return portInfo;
        }
        
    }
    
    private static class WebAppServiceRefFinder extends ServiceRefFinder {
        
        private WebAppServiceRefFinder(String srName, String namespaceURI, String localpart, boolean create) {
            super(srName, namespaceURI, localpart, create);
        }
        
        protected PortInfo findPort(RootInterface rootDD, SunONEDeploymentConfiguration config) throws IOException {
            if(rootDD instanceof SunWebApp) {
                SunWebApp sunWebApp = (SunWebApp) rootDD;
                ServiceRef [] serviceRefs = sunWebApp.getServiceRef();
                if(serviceRefs != null) {
                    for(ServiceRef serviceRef: serviceRefs) {
                        if(serviceRefName.equals(serviceRef.getServiceRefName())) {
                            return findPort(serviceRef);
                        }
                    }
                }
                
                if(create) {
                    ServiceRef newServiceRef = sunWebApp.newServiceRef();
                    newServiceRef.setServiceRefName(serviceRefName);
                    sunWebApp.addServiceRef(newServiceRef);
                    return createPortInfo(newServiceRef);
                }
            } else {
                if(create) {
                    throw new IllegalStateException("Unable to create or access ddapi of proper type, expected SunWebApp.");
                }
            }
            return null;
        }
    }
    
    private static class EjbJarServiceRefFinder extends ServiceRefFinder {
        
        private EjbJarServiceRefFinder(String srName, String namespaceURI, String localpart, boolean create) {
            super(srName, namespaceURI, localpart, create);
        }
        
        protected PortInfo findPort(RootInterface rootDD, SunONEDeploymentConfiguration config) throws IOException {
            if(rootDD instanceof SunEjbJar) {
                SunEjbJar sunEjbJar = (SunEjbJar) rootDD;
                EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                if(eb != null) {
                    Ejb [] ejbs = eb.getEjb();
                    if(ejbs != null && ejbs.length > 0) {
                        for(Ejb ejb: ejbs) {
                            ServiceRef [] serviceRefs = ejb.getServiceRef();
                            if(serviceRefs != null) {
                                for(ServiceRef serviceRef: serviceRefs) {
                                    if(serviceRefName.equals(serviceRef.getServiceRefName())) {
                                        return findPort(serviceRef);
                                    }
                                }
                            }
                        }
                    }
                }
                
                if(create) {
                    String ejbName = findEjbName(config);
                    if(ejbName != null && ejbName.length() > 0) {
                    } else {
                        throw new UnsupportedOperationException("Unable to create new service-ref entry in sun-ejb-jar.xml, no owning ejb found in standard dd.");
                    }
                }
            } else {
                if(create) {
                    throw new IllegalStateException("Unable to create or access ddapi of proper type, expected SunEjbJar.");
                }
            }
            return null;
        }
        
        private String findEjbName(final SunONEDeploymentConfiguration config) throws IOException {
            // Search standard DD
            String ejbName = findEjbName(config.getStandardRootDD());
            if(ejbName != null) {
                // Search metadata
                MetadataModel<EjbJarMetadata> model = config.getMetadataModel(EjbJarMetadata.class);
                if(model.isReady()) {
                    ejbName = model.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
                        public String run(EjbJarMetadata metadata) throws Exception {
                            return findEjbName(metadata.getRoot());
                        }
                    });
                }
            }
            return ejbName;
        }
        
        private String findEjbName(org.netbeans.modules.j2ee.dd.api.common.RootInterface rootDD) {
            try {
                if(rootDD instanceof org.netbeans.modules.j2ee.dd.api.ejb.EjbJar) {
                    org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = (org.netbeans.modules.j2ee.dd.api.ejb.EjbJar) rootDD;
                    org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans eb = ejbJar.getEnterpriseBeans();
                    if(eb != null) {
                        org.netbeans.modules.j2ee.dd.api.ejb.Session [] sbs = eb.getSession();
                        if(sbs != null && sbs.length > 0) {
                            for(org.netbeans.modules.j2ee.dd.api.ejb.Session session: sbs) {
                                org.netbeans.modules.j2ee.dd.api.common.ServiceRef [] refs = session.getServiceRef();
                                if(refs != null && refs.length > 0) {
                                    for(org.netbeans.modules.j2ee.dd.api.common.ServiceRef serviceRef: refs) {
                                        if(serviceRefName.equals(serviceRef.getServiceRefName())) {
                                            return session.getEjbName();
                                        }
                                    }
                                }
                            }
                        }

                        org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven [] mdbs = eb.getMessageDriven();
                        if(mdbs != null && mdbs.length > 0) {
                            for(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven mdb: mdbs) {
                                org.netbeans.modules.j2ee.dd.api.common.ServiceRef [] refs = mdb.getServiceRef();
                                if(refs != null && refs.length > 0) {
                                    for(org.netbeans.modules.j2ee.dd.api.common.ServiceRef serviceRef: refs) {
                                        if(serviceRefName.equals(serviceRef.getServiceRefName())) {
                                            return mdb.getEjbName();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch(org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            
            return null;
        }
    }

    private ServiceRefFinder createServiceRefFinder(SunONEDeploymentConfiguration config, 
            String srName, String namespaceURI, String localpart, boolean create) {
        ServiceRefFinder result = null;
        J2eeModule module = config.getJ2eeModule();
        if(module != null) {
            if(ModuleType.WAR.equals(module.getModuleType())) {
                result = new WebAppServiceRefFinder(srName, namespaceURI, localpart, create);
            } else if(ModuleType.EJB.equals(module.getModuleType())) {
                result = new EjbJarServiceRefFinder(srName, namespaceURI, localpart, create);
            }
        }
        return result;
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

}
