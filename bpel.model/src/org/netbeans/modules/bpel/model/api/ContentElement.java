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

import org.netbeans.modules.bpel.model.api.events.VetoException;

/**
 * @author ads
 */
public interface ContentElement {

    /**
     * This is not attribute name. This is identifier for
     * text in mixed entity. No attribute can have such name
     * so TEXT will be identified by this property.
     */
    String CONTENT_PROPERTY = "<content>";          // NOI18N

    /**
     * @return text representation of the element content.
     */
    String getContent();

    /**
     * Set the content to a text node with the given
     * string value.
     * @param content New text value.
     * @throws VetoException {@link VetoException}
     * will be thrown if content is not acceptable here.
     */
    void setContent( String content ) throws VetoException;
}
