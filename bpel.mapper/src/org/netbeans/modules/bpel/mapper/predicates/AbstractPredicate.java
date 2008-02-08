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

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;

/**
 * The base class for different kind of Predicated Schema components.
 * @author nk160297
 */
public abstract class AbstractPredicate {

    public abstract XPathSchemaContext getContext();
    
    public abstract XPathPredicateExpression[] getPredicates();
    
    public abstract SchemaComponent getSComponent();
    
    public abstract void setPredicates(XPathPredicateExpression[] newPArr);
    
    public String getPredicatesText() {
        XPathPredicateExpression[] predArr = getPredicates();
        if (predArr != null && predArr.length != 0) {
            StringBuilder sb = new StringBuilder();
            for (XPathPredicateExpression predicate : predArr) {
                sb.append(predicate.getExpressionString());
            }
            return sb.toString();
        } else {
            return "";
        }
    }
    
    public boolean hasSameContext(XPathSchemaContext context) {
        XPathSchemaContext myContext = getContext();
        if (myContext == null) {
            return false;
        }
        return myContext.equalsChain(context);
    }
    
    @Override
    public boolean equals(Object obj) {
        // Compare class
        if (!(obj instanceof AbstractPredicate)) {
            return false;
        }
        //
        AbstractPredicate comp2 = (AbstractPredicate)obj;
        //
        XPathSchemaContext mySContext = getContext();
        if (mySContext == null || comp2.getContext() == null) {
            // 
            // Compare Schema component
            SchemaComponent mySchemaComp = getSComponent();
            if (mySchemaComp != comp2.getSComponent()) {
                return false;
            }
        } else {
            // Compare context
            if (!(comp2.getContext().equalsChain(mySContext))) {
                return false;
            }
        }
        //
        // Compare predicates 
        XPathPredicateExpression[] otherPredicateArr = comp2.getPredicates();
        return XPathUtils.samePredicatesArr(getPredicates(), otherPredicateArr);        
    }

    public String getDisplayName() {
        String sCompName = ((Named)getSComponent()).getName();
        return sCompName + " " + getPredicatesText();
    }
    
    @Override
    public String toString() {
        String contextText = null;
        XPathSchemaContext mySContext = getContext();
        if (mySContext != null) {
            contextText = mySContext.toString();
        }
        //
        if (contextText == null || contextText.length() == 0) {
            return getDisplayName();
        } else {
            return mySContext.toString() + " " + getPredicatesText();
        }
    }
    
} 