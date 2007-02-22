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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.impl;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELExtensibilityComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.BPELQName;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Documentation;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.spi.NamedExtensibilityElementBase;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 * 
 * changed by 
 * @author ads
 */
public class RoleImpl extends NamedExtensibilityElementBase implements Role {
    
    /** Creates a new instance of RoleImpl */
    public RoleImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public RoleImpl(WSDLModel model){
        this(model, createPrefixedElement(BPELQName.ROLE.getQName(), model));
    }
    
    protected String getNamespaceURI() {
        return BPELQName.PLNK_NS;
    }
    
    public NamedComponentReference<PortType> getPortType() {
        return resolveGlobalReference(PortType.class, BPELAttribute.PORT_TYPE);
    }

    public void setPortType(NamedComponentReference<PortType> portType) {
        setAttribute(PORT_TYPE_PROPERTY, BPELAttribute.PORT_TYPE, portType);
    }

    public void accept(BPELExtensibilityComponent.Visitor v) {
        v.visit(this);
    }

    @Override
    public void addExtensibilityElement(ExtensibilityElement ee) {
        if (ee instanceof Documentation) {
            addRoleDocumentation((Documentation) ee);
        } else {
            super.addExtensibilityElement(ee);
        }
    }

    public void addRoleDocumentation(Documentation doc) {
        if (doc == null) return;
        appendChild(ROLE_DOCUMENTATION_PROPERTY, doc);
    }

    public void removeRoleDocumentation(Documentation doc) {
        removeChild(ROLE_DOCUMENTATION_PROPERTY, doc);
        
    }

    public Collection<Documentation> getRoleDocumentations() {
        return getChildren(Documentation.class);
    }
}
