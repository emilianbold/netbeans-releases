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
