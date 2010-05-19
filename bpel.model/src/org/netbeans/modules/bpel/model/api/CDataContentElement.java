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

import java.io.IOException;
import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * @author Vitaly Bychkov
 */
public interface CDataContentElement {

    /**
     * This is not attribute name. This is identifier for
     * CData content in mixed entity. No attribute can have such name
     * so CData content will be identified by this property.
     */
    String CDATA_CONTENT_PROPERTY = "<cdatacontent>";          // NOI18N

    /**
     * @return CData content representation of the element content.
     */
    String getCDataContent();

    /**
     * Set the CData content to a node with the given
     * value.
     * @param content New CData content value.
     * @throws VetoException {@link VetoException}
     * will be thrown if content is not acceptable here.
     */
    void setCDataContent( String xmlContent ) throws VetoException, IOException;
}
