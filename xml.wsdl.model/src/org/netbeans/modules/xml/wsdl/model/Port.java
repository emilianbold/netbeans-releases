/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
