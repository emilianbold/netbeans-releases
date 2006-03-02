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

package org.netbeans.modules.xml.wsdl.model.spi;

import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.w3c.dom.Element;



/**
 * Factory for creating a WSDL component. This factory must be provided by 
 * ElementFactoryProvider to be able to plugin to the WSDL model.
 *
 * @author rico
 * @author Nam Nguyen
 * 
 */
public interface ElementFactory {
    /**
     * Returns the QName of the element this factory is for.
     */
    Set<QName> getElementQNames();
    
    /**
     * Creates the WSDLComponent to be added to the given container component.
     * @param container component requesting the creation
     * @type type of requested component
     * @return created component.
     */
    <C extends WSDLComponent> C create(WSDLComponent container, Class<C> type);
    
    /**
     * Creates WSDLComponent from a DOM element given container component.
     * @param container component requesting creation
     * @param element DOM element from which to create the component.
     */
    WSDLComponent create(WSDLComponent container, Element element);
}
