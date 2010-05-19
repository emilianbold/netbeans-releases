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

package org.netbeans.modules.soa.xpath.mapper.lsm;

import org.netbeans.modules.soa.xpath.mapper.utils.XPathMapperUtils;
import org.netbeans.modules.soa.xpath.mapper.tree.DirectedList;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathSchemaContextHolder;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.SchemaCompHolder;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * Represents XPath Predicate in BPEL Mapper
 *
 * @author Nikita Krjukov
 *
 * TODO: Rename to AbstractMapperPredicate
 */
public class MapperPredicate implements MapperLsm {

    public interface Calculator {
        MapperPredicate calculatePredicate();
        XPathPredicateExpression[] calculatePredicateExperArr();
    }

    private PredicatedSchemaContext mSContext;

    /**
     * Constructor is protected because the object has to be created either by
     * a derived class or by PredicateManager.
     * @param predSContext
     */
    protected MapperPredicate(PredicatedSchemaContext predSContext) {
        assert predSContext != null;
        mSContext = predSContext;
    }
    
    /**
     * This method details semantics of the same method defined in the base interface.
     * @see XPathSchemaContextHolder
     *
     * The result schema context is specially designed for Predicates.
     * It holds full information about the predicated component and
     * the predicate expression itself.
     * @return
     */
    public PredicatedSchemaContext getSchemaContext() {
        return mSContext;
    }

    /**
     * @see XPathSchemaContext.Utilities#getMatryoshkaCore(WrappingSchemaContext)
     * @param lookForMatryoshkaCore
     * @return
     */
    public SchemaCompHolder getSCompHolder(boolean lookForMatryoshkaCore) {
        return XPathSchemaContext.Utilities.getSchemaCompHolder(
                mSContext, lookForMatryoshkaCore);
    }

    public XPathPredicateExpression[] getPredicates() {
        return mSContext.getPredicateExpressions();
    }

    public void setPredicates(XPathPredicateExpression[] newPArr) {
        mSContext.setPredicateExpressions(newPArr);
    }

//    @Override
//    public MapperPredicate clone() {
//        XPathSchemaContext sContext = getSchemaContext();
//        assert sContext instanceof PredicatedSchemaContext;
//        if (sContext == null || !(sContext instanceof PredicatedSchemaContext)) {
//            return null;
//        }
//        //
//        PredicatedSchemaContext contextClone =
//                ((PredicatedSchemaContext)sContext).clone();
//        //
//        return new MapperPredicate(contextClone);
//    }

    /**
     * Returns predicates in string form. Example: [aaa][2]
     * @return
     */
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

    @Override
    public boolean equals(Object obj) {
        boolean equals = false;
        //
        // Compare class
        if (!(obj instanceof MapperPredicate)) {
            return false;
        }
        //
        MapperPredicate pred2 = (MapperPredicate)obj;
        //
        PredicatedSchemaContext sCont1 = this.getSchemaContext();
        PredicatedSchemaContext sCont2 = pred2.getSchemaContext();
        if (!XPathSchemaContext.Utilities.equalsChain(sCont1, sCont2)) {
            return false;
        }
        //
        // Compare predicates 
        XPathPredicateExpression[] otherPredicateArr = pred2.getPredicates();
        equals = XPathUtils.samePredicatesArr(getPredicates(), otherPredicateArr);        
        if (!equals) {
            return false;
        }
        //
        //
        return true;
    }

    public String getDisplayName() {
        XPathSchemaContext sContext = getSchemaContext();
        return sContext == null ? null : sContext.toStringWithoutParent();
    }

    @Override
    public String toString() {
        return getSchemaContext().toString();
    }
    
    public void setSchemaContext(XPathSchemaContext newContext) {
        throw new UnsupportedOperationException("Not supported"); // NOI18N
    }

    public XPathVariable getBaseVariable() {
        return XPathMapperUtils.getBaseVariable(getSchemaContext());
    }

    public void modifyPredicate(PredicatedSchemaContext template, 
            XPathPredicateExpression[] newExprArr) {
        //
        PredicateManager.modifyPredicateInSContext(
                mSContext, template, newExprArr);
    }

} 
