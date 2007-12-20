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

package org.netbeans.modules.xml.xpath.ext;

/**
 *
 * @author nk160297
 */
public interface XPathSchemaContextHolder {

    /**
     * Returnes the calculated or assigned Schema context.
     * The null will be returned if the schema context can't 
     * be resolved or it is not assigned.
     */ 
    XPathSchemaContext getSchemaContext();
    
    /**
     * Assigns a new Schema context to the context holder.
     * This method isn't intended to be called manually. 
     * 
     * It is going to be called automatically, when the method 
     * XPathModel.resolveExtReferences() is called.
     */  
    void setSchemaContext(XPathSchemaContext newContext);
    
}
