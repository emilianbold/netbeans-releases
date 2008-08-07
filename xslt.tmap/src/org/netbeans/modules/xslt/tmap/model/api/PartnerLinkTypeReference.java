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
package org.netbeans.modules.xslt.tmap.model.api;

import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public interface PartnerLinkTypeReference extends ReferenceCollection {
    /**
     * Partner Link Type attribute name
     */
    String PARTNER_LINK_TYPE = "partnerLinkType"; // NOI18N
    
    /**
     * Role Name attribute name 
     */
    String ROLE_NAME = "roleName"; // NOI18N
    
    /**
     * 
     * @return 
     */
    WSDLReference<PartnerLinkType> getPartnerLinkType();
    /**
     * 
     * @param pltRef - reference to the partner link type
     */
    void setPartnerLinkType(WSDLReference<PartnerLinkType> pltRef);
    
     /**
      * 
      * @return 
      */
     WSDLReference<Role> getRole();

     /**
      * 
      * @param roleRef - reference to the role wsdl component
      */
     void setRole(WSDLReference<Role> roleRef);
    
}
