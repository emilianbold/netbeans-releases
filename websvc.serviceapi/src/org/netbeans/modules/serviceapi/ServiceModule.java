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
import org.netbeans.api.project.Project;

/**
 * Represents a unit in designing and composing service-based application.
 *
 * @author Nam Nguyen
 * @author Chris Webster
 * @author Jiri Kopsa
 */
public abstract class ServiceModule {

    /**
     * Property name of change event on service components.
     */
    public static final String SERVICE_COMPONENT_ADDED_PROPERTY = "serviceComponentAdded";
    public static final String SERVICE_COMPONENT_REMOVED_PROPERTY = "serviceComponentRemoved";

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * Adds/removes listener from this service module.
     */
    public void addPropertyListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * @return name of the service module.
     */
    public abstract String getName();

    /**
     * Returns service components contained in this module.
     */
    public abstract Collection<ServiceComponent> getServiceComponents();
    
    /**
     * Add service component.
     */
    
    public abstract void addServiceComponent(ServiceComponent component);
    
    /**
     * Remove service component.
     */
    public abstract void removeServiceComponent(ServiceComponent component);

    /**
     * @return the project if applicable (not applicable for service modules from appserver).
     */
    public abstract Project getProject();
}
