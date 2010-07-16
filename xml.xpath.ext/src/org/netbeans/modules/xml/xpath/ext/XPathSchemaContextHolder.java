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

import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * A context holder is a component, which can provide type information in term 
 * of the Schema model component(s). Such information is held in the special 
 * container - XPathSchemaContext. The schema context can point either 
 * to the specific schema component or to a set of schema components. 
 * See more detailed description in the XPathSchemaContext.
 * 
 * The schema context can has a bit different meaning for different components.
 * - for Location Steps it means the schema type of the step. 
 * - for Location Paths or Expression Paths it means the schema type of the last Step.
 * - for XPath model it means the context in which the model is defined as a whole.
 * - for Predicates it means the context in which the predicate is applied.
 *
 * TODO: Two new interfaces should be derived: XPathSchemaContextOwner and
 * BaseSchemaContextHolder. They should be empty because they will derive
 * the methods from this one. In most places where this interface is used it
 * has to be replaced with XPathSchemaContextOwner. The XPathModel and
 * XpathPredicateExpression has to extend BaseSchemaContextHolder.
 * The meaning of such changes is to prevent mess up with this 2 notions.
 * The changes affects many modules so it worth doing it when code will be stable. 
 *
 * @author nk160297
 */
public interface XPathSchemaContextHolder {

    /**
     * Returnes the calculated or assigned Schema context.
     * The null will be returned if the schema context can't 
     * be resolved or it is not assigned.
     *
     * WARNING!!! Always check the result to null!
     * It is very common mistake when the check to null is missed. 
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
