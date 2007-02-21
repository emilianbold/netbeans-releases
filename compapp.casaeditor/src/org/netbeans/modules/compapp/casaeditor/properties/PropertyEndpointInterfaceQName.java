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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.properties;

import java.lang.reflect.InvocationTargetException;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;

/**
 *
 * @author Josh Sandusky
 */
public class PropertyEndpointInterfaceQName extends PropertyQName {
    
    
    public PropertyEndpointInterfaceQName(
            CasaNode node,
            CasaEndpointRef component, 
            String propertyType, 
            String property,
            String propDispName, 
            String propDesc)
    {
        super(node, component, propertyType, property, propDispName, propDesc);
    }

    
    protected QName getCurrentQName() { 
        return getModel().getInterfaceQName((CasaEndpointRef) getComponent());
    }
    
    public Object getValue()
    throws IllegalAccessException, InvocationTargetException {
        return getModel().getInterfaceQName((CasaEndpointRef) getComponent());
    }

    public void setValue(Object object)
    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        getModel().setEndpointInterfaceQName(
                (CasaEndpointRef) getComponent(), 
                (QName) object);
    }
}
