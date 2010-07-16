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
 * This is nothing but a wrapper around an XPathExpression.
 * This helps distinguishing expression which are predicates.
 * (ie. expression which are inside [] in a location/expression path.
 *
 * @author Nikita Krjukov
 * @author radval
 *
 */
public interface XPathPredicateExpression 
        extends XPathExpression, XPathSchemaContextHolder {

    /**
     * This method details semantics of the same method defined in the base interface.
     * @see XPathSchemaContextHolder
     *
     * The schema context has special meaning here.
     * 2 schema contexts are associated with the predicated location step:
     * a predicated and base context. The base context doesn't have information
     * about predicate. In only refers to step component. The predicated context
     * is a wrapper context, which contains the base context and reference to
     * predicate expression. It is important to understand that the base
     * context should be specified here. This context is implied to be use
     * as base while resolving schema context of the predicate's expression.
     */
    void setSchemaContext(XPathSchemaContext newContext);

    XPathExpression getPredicate();
}
