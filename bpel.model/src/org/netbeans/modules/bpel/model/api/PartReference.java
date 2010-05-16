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

/**
 *
 */
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.Part;

/**
 * @author ads
 */
public interface PartReference {

    /**
     * name of "part" attribute.
     */
    String PART = "part";       // NOI18N

    /**
     * @return reference to Part object in WSDL model.
     */
    WSDLReference<Part> getPart();

    /**
     * Set new part reference.
     *
     * @param part
     *            New reference to Part object in WSDL model.
     */
    void setPart( WSDLReference<Part> part );
    
}
