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
