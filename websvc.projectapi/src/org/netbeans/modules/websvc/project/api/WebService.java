/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.websvc.project.api;

import org.netbeans.modules.websvc.project.WebServiceAccessor;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;
import org.openide.nodes.Node;

/**
 * Encapsulation of a web service. 
 * 
 * Clients make calls to instances of this class which are then delegated to the
 * (@link WebServiceImplementation).
 * 
 * @author  mkuchtiak
 */
public final class WebService {

    private WebServiceImplementation serviceImpl;
    

    static {
        WebServiceAccessor.DEFAULT = new WebServiceAccessor() {

            @Override
            public WebService createWebService(WebServiceImplementation serviceImpl) {
                return new WebService(serviceImpl);
            }
        };
    }

    private WebService(WebServiceImplementation serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    /**
     * Returns an identifier for the service provider or consumer
     */
    public String getIdentifier(){
        return serviceImpl.getIdentifier();
    }
    
    /**
     * Says whether this is a service provider or service consumer
     * @return true if it is a service provider, false if consumer
     */
    public boolean isServiceProvider() {
        return serviceImpl.isServiceProvider();
    }

    /**
     * Returns the type of service (e.g., SOAP, REST, etc)
     * @return Type of service
     */
    public Type getServiceType() {
        return serviceImpl.getServiceType();
    }

    /**
     * Returns Netbeans node that representing the web service
     * @return Node representing the web service.
     */
    public Node getNode() {
        return serviceImpl.getNode();
    }

    /**
     * Returns the service descriptor for this web service implementation
     * @return ServiceDescriptor
     */
    public ServiceDescriptor getServiceDescriptor() {
        return serviceImpl.getServiceDescriptor();
    }

    /**
     * Enum that enumerates the web service types (SOAP, REST, etc) supported in Netbeans.
     */
    public static enum Type {
        /**
         * WSDL-based services
         */
        SOAP,
        /**
         * RESTful services
         */
        REST;
    }
}
