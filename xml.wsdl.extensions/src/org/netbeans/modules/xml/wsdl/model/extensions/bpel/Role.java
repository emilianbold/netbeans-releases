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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author Nam Nguyen
 * 
 * changed by
 * @author ads 
 */

public interface Role extends BPELExtensibilityComponent, Nameable<WSDLComponent>, ReferenceableWSDLComponent {
    public static final String PORT_TYPE_PROPERTY = "portType";
    public static final String ROLE_DOCUMENTATION_PROPERTY = "documentation";
    NamedComponentReference<PortType> getPortType();
    void setPortType(NamedComponentReference<PortType> portType);
    
    void addRoleDocumentation(Documentation doc);
    void removeRoleDocumentation(Documentation doc);
    Collection<Documentation> getRoleDocumentations();
}
