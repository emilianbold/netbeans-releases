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
package org.netbeans.modules.serviceapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.List;
import org.openide.nodes.Node;

/**
 * Represents an implementation unit corresponding of one or multiple 
 * service interfaces, co-locate in a single implementation unit such as a plain 
 * Java class, a BPEL process, an EJB or Servlet.
 *
 * @author Nam Nguyen
 * @author Chris Webster
 * @author Jiri Kopsa
 */
public abstract class ServiceComponent {

    public static final String SERVICE_INTERFACE_ADDED_PROPERTY = "serviceInterfaceAdded";
    public static final String SERVICE_INTERFACE_REMOVED_PROPERTY = "serviceInterfaceRemoved";
    
    private PropertyChangeSupport propSupport;
    /**
     * Add property change listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }
    /**
     * Remove property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    /**
     * Returns the service interfaces this component provides 
     * the implementation for.
     */
    public abstract List<ServiceInterface> getServiceProviders();
    /**
     * Returns the service interfaces this component consumes.
     */
    public abstract List<ServiceInterface> getServiceConsumers();
    
    /**
     * Returns the service coordination information if applicable.
     */
    public abstract Collection<ServiceLink> getServiceLinks();
    
    /**
     * Returns the visualization of the service component.  The node should provide
     * navigation to the component editors and access to its content.
     * @return visualization node for the service component.
     */
    public abstract Node getNode();
    
    //CR: where should Categorization and CategorizationProvider come from
    
    /** 
     * Ensures this component provide for or consume the given interface.
     *
     * @param description the interface to create consumer or provider service for.
     * @provider whether the service interface to create is provider or consumer.
     * @return the service interface object.
     */ 
    public abstract ServiceInterface createServiceInterface(InterfaceDescription description, boolean provider);
 
    /** 
     * Creates the counter-part service interface of the given service interface.
     */ 
    public abstract ServiceInterface createServiceInterface(ServiceInterface other);
    
    /**
     * Removes service interface from this component.  Will also remove 
     * any associated connections from the containing service module container.
     * Note that the related service interfaces and service links are not removed.
     */
    public abstract void removeServiceInterface(ServiceInterface serviceInterface);

}
