/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Named;

/**
 *
 * @author rico
 * Represents a service port in the WSDL document
 */
public interface Port extends Named<WSDLComponent>, WSDLComponent {
    public static final String BINDING_PROPERTY = "binding";
    
    void setBinding(GlobalReference<Binding> binding);
    GlobalReference<Binding> getBinding();
}
