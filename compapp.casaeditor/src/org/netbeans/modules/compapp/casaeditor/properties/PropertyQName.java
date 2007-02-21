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

/*
 * PropertyQName.java
 *
 * Created on February 7, 2007, 1:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.properties;

import java.beans.PropertyEditor;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;

/**
 *
 * @author Josh Sandusky
 */
public abstract class PropertyQName extends BaseCasaProperty {
    
    
    public PropertyQName(
            CasaNode node,
            CasaEndpointRef component, 
            String propertyType, 
            String property,
            String propDispName, 
            String propDesc)
    {
        super(node, component, propertyType, QName.class, property, propDispName, propDesc);
    }
    
    protected abstract QName getCurrentQName();
    
    @Override
    public PropertyEditor getPropertyEditor() {
        return new NamespaceEditor(
                this, 
                getModel(),
                getCurrentQName(),
                getDisplayName());
    }
}
