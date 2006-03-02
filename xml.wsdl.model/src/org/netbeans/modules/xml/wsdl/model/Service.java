/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author rico
 * Represents a service in the WSDL document
 */
public interface Service extends Named<WSDLComponent>, WSDLComponent  {
    public static final String PORT_PROPERTY = "port";
    
    void addPort(Port port);
    void removePort(Port port);
    Collection<Port> getPorts();
}
