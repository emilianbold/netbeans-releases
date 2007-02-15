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

import java.util.Collection;
import javax.xml.namespace.QName;
import org.openide.nodes.Node;

/**
 * Represents the service view of a web service interface.
 * The service view defines the provider or consumer side of the interface.
 *
 * @author Nam Nguyen
 * @author Chris Webster
 * @author Jiri Kopsa
 */
public interface ServiceInterface {
    // maybe needed for JBI or other underlying technology
    // <T extends Object> T getCookie(Class<T> type);
    
    /**
     * Returns the description of the service interface.
     */
    InterfaceDescription getInterfaceDescription();
    
    /**
     * Returns service component exposing this service interface.
     */
    ServiceComponent getServiceComponent();

    /**
     * Whether provider-consumer relationship is possible between this 
     * service interface and the given service interface.
     */
    boolean canConnect(ServiceInterface other);

    /**
     * @return true if this service interface is of providing nature; 
     * return false if it is of consuming nature.
     */
    boolean isProvider();

    /**
     * @return the visual representation of this service interface.
     */
    Node getNode();
}
