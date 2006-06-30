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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * TypeReferenceContainer.java
 *
 * Created on January 19, 2006, 6:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * A type container defines a schema element which can either
 * reference or include a local type definition. 
 * @author Chris Webster
 */
public interface TypeContainer {
    public static final String TYPE_PROPERTY = "type";
    public static final String INLINE_TYPE_PROPERTY = "inlineType";
    
    GlobalReference<? extends GlobalType> getType();
    void setType(GlobalReference<? extends GlobalType>t);
    
    LocalType getInlineType();
    void setInlineType(LocalType t);
}
