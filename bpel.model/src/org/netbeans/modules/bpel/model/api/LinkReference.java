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

import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;

/**
 * @author ads
 */
public interface LinkReference extends ReferenceCollection {

    /**
     * linkName attribute name.
     */
    String LINK_NAME = "linkName";      // NOI18N

    /**
     * Gets the value of the linkName property.
     *
     * @return possible object is reference to Link entity.
     */
    BpelReference<Link> getLink();

    /**
     * Sets the value of the linkName property.
     * 
     * @param link
     *            allowed object is reference to Link entity.
     */
    void setLink( BpelReference<Link> link );

}
