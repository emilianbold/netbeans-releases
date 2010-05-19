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

package org.netbeans.modules.bpel.debugger.ui.plinks.models;

import org.netbeans.modules.bpel.debugger.api.RuntimePartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;

/**
 *
 * @author Kirill Sorokin
 */
public class PartnerLinkWrapper {
    
    private PartnerLink pLink;
    private RuntimePartnerLink rLink;
    
    public PartnerLinkWrapper(
            final PartnerLink pLink,
            final RuntimePartnerLink rLink) {
        this.pLink = pLink;
        this.rLink = rLink;
    }
    
    public String getName() {
        return pLink.getName();
    }
    
    public boolean isDynamic() {
        return rLink != null;
    }
    
    public WSDLReference<PartnerLinkType> getPartnerLinkTypeRef() {
        return pLink.getPartnerLinkType();
    }
    
    public WSDLReference<Role> getMyRoleRef() {
        return pLink.getMyRole();
    }
    
    public WSDLReference<Role> getPartnerRoleRef() {
        return pLink.getPartnerRole();
    }
    
    public String getEndpointSerializedValue() {
        if (rLink != null) {
            return rLink.getSerializedValue();
        }
        
        return null;
    }
}
