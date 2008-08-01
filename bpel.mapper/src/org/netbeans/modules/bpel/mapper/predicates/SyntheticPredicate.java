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

package org.netbeans.modules.bpel.mapper.predicates;

import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;

/**
 * This predicate isn't related with any XPath model
 * Usually the new predicates which haven't attached to any XPath 
 * expression can use this class. 
 * @author nk160297
 */
public class SyntheticPredicate extends AbstractPredicate {

    // private RestartableIterator<Object> mPathItr;
    
    private XPathSchemaContext mSContext;
    private XPathPredicateExpression[] mPredicateArr;
    
    public SyntheticPredicate(XPathSchemaContext sContext, 
            XPathPredicateExpression[] predicateArr) {
        mSContext = sContext;
        mPredicateArr = predicateArr;
    }
    
    public XPathSchemaContext getSchemaContext() {
        return mSContext;
    }
    
    public XPathPredicateExpression[] getPredicates() {
        return mPredicateArr;
    }
    
    public SchemaCompHolder getSCompHolder() {
        return XPathSchemaContext.Utilities.getSchemaCompHolder(mSContext);
    }
    
    public void setPredicates(XPathPredicateExpression[] newPArr) {
        mPredicateArr = newPArr;
    }
    
} 