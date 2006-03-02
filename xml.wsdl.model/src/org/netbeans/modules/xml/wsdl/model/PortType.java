/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * PortType.java
 *
 * Created on November 11, 2005, 1:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Referenceable;

/**
 *
 * @author rico
 * Represents a portType in the WSDL document
 */
public interface PortType extends ReferenceableWSDLComponent {
    public static final String OPERATION_PROPERTY = "operation";
    
    void addOperation(Operation operation);
    void removeOperation(Operation operation);
    Collection<Operation> getOperations();
}
