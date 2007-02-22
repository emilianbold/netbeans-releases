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

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.ReferenceableExtensibilityElement;

/**
 *
 * @author rico
 * @author Nam Nguyen
 */
public interface PartnerLinkType extends ReferenceableExtensibilityElement, 
        ExtensibilityElement.UpdaterProvider, BPELExtensibilityComponent {
    
    public static final String ROLE_PROPERTY = "role";
    public static final String PARTNERLINKTYPE_DOCUMENTATION_PROPERTY = "documentation";
    
    Role getRole1();
    void setRole1(Role role);
    
    Role getRole2();
    void setRole2(Role role);
    
    void addPartnerLinkTypeDocumentation(Documentation doc);
    void removePartnerLinkTypeDocumentation(Documentation doc);
    Collection<Documentation> getPartnerLinkTypeDocumentations();
}
