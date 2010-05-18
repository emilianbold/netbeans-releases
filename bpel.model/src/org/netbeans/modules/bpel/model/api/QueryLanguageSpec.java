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
package org.netbeans.modules.bpel.model.api;

import org.netbeans.modules.bpel.model.api.events.VetoException;


/**
 * @author ads
 *
 */
public interface QueryLanguageSpec {

    /**
     * queryLanguage attribute name.
     */
    String QUERY_LANGUAGE = "queryLanguage";     // NOI18N

    /**
     * Gets the value of the queryLanguage property.
     * 
     * @return possible object is {@link String }
     */
    String getQueryLanguage();

    /**
     * Sets the value of the queryLanguage property.
     * 
     * @param value
     *            allowed object is {@link String }
     * @throws VetoException {@link VetoException}
     *             will be thrown if <code>value</code> if not acceptable as
     *             queryLanguage attribute here.
     */
    void setQueryLanguage( String value ) throws VetoException;
    
    /**
     * Removes queryLanguage attribute.
     */
    void removeQueryLanguage();
}
