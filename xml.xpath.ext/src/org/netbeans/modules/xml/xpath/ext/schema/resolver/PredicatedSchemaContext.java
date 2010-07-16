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

package org.netbeans.modules.xml.xpath.ext.schema.resolver;

import java.util.ArrayList;
import java.util.Set;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.schema.SchemaModelsStack;
import org.netbeans.modules.xml.xpath.ext.visitor.VariableVisibilityChecker;

/**
 * The schema context, which relates to a Predicated step. 
 * It wraps a base schema context, which represents the entity to which 
 * the predicated is applied. 
 * 
 * WARNING. The class hasn't intended to be used in HashSet yet. 
 * It doesn't have hashCode method redifined.
 * 
 * @author nk160297
 */
public class PredicatedSchemaContext implements WrappingSchemaContext {

    // The context, which this Predicate context based on.
    protected XPathSchemaContext mBaseContext;
    
    protected XPathPredicateExpression[] mXPathPredExprArr;
    
    protected boolean lastInChain = false;

    protected PredicatedSchemaContext(XPathSchemaContext baseContext) {
        mBaseContext = baseContext;
    }

    public PredicatedSchemaContext(XPathSchemaContext baseContext,
            XPathPredicateExpression xPathPredicateExpr) {
        assert xPathPredicateExpr != null;
        assert !(baseContext instanceof PredicatedSchemaContext);
        mBaseContext = baseContext;
        mXPathPredExprArr = new XPathPredicateExpression[] {xPathPredicateExpr};
    }

    public PredicatedSchemaContext(XPathSchemaContext baseContext, 
            XPathPredicateExpression[] xPathPredicateExprArr) {
        assert xPathPredicateExprArr != null;
        assert !(baseContext instanceof PredicatedSchemaContext);
        //
        mBaseContext = baseContext;
        mXPathPredExprArr = xPathPredicateExprArr;
    }

    /**
     * Be aware that shallow cloning is maid. 
     * @return
     */
    @Override
    public PredicatedSchemaContext clone() {
        if (mXPathPredExprArr != null) {
            return new PredicatedSchemaContext(mBaseContext, mXPathPredExprArr);
        }
        return null;
    }

    public XPathSchemaContext getBaseContext() {
        return mBaseContext;
    }
    
    public XPathPredicateExpression[] getPredicateExpressions() {
        return mXPathPredExprArr;
    }

    public void setPredicateExpressions(XPathPredicateExpression[] newExpr) {
        mXPathPredExprArr = newExpr;
    }

    private String[] getPredicateTexts(NamespaceContext nsContext) {
        XPathPredicateExpression[] exprArr = getPredicateExpressions();
        assert exprArr != null && exprArr.length != 0;
        //
        ArrayList<String> newPredTextList = new ArrayList<String>();
        for (XPathPredicateExpression predExpr : exprArr) {
            XPathExpression predExprContent = predExpr.getPredicate();
            String newPredText = null;
            if (nsContext != null) {
                newPredText = predExprContent.getExpressionString(nsContext);
            } else {
                newPredText = predExprContent.getExpressionString();
            }
            if (newPredText != null && newPredText.length() != 0) {
                newPredTextList.add(newPredText);
            }
        }
        String[] mPredTextArr = newPredTextList.toArray(new String[newPredTextList.size()]);
        return mPredTextArr;
    }

    public XPathSchemaContext getParentContext() {
        return mBaseContext.getParentContext();
    }

    public Set<SchemaCompPair> getSchemaCompPairs() {
        return mBaseContext.getSchemaCompPairs();
    }
    
    public Set<SchemaCompPair> getUsedSchemaCompPairs() {
        return mBaseContext.getUsedSchemaCompPairs();
    }

    public void setUsedSchemaCompH(Set<SchemaCompHolder> compSet) {
        mBaseContext.setUsedSchemaCompH(compSet);
    }

    public String toStringWithoutParent() {
        StringBuilder sb = new StringBuilder();
        sb.append(mBaseContext.toStringWithoutParent());
        for (String predicate : getPredicateTexts(null)) {
            sb.append("[").append(predicate).append("]");
        }
        return  sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mBaseContext.toString());
        for (String predicate : getPredicateTexts(null)) {
            sb.append("[").append(predicate).append("]");
        }
        return  sb.toString();
    }

    public String getExpressionString(NamespaceContext nsContext, SchemaModelsStack sms) {
        StringBuilder sb = new StringBuilder();
        sb.append(mBaseContext.getExpressionString(nsContext, sms));
        for (String predicate : getPredicateTexts(nsContext)) {
            sb.append("[").append(predicate).append("]");
        }
        return  sb.toString();
    }

    public String getPredicatesString(NamespaceContext nsContext) {
        StringBuilder sb = new StringBuilder();
        for (String predicate : getPredicateTexts(nsContext)) {
            sb.append("[").append(predicate).append("]");
        }
        return  sb.toString();
    }

    @Override
    public boolean equals(Object obj)  {
        if (obj instanceof PredicatedSchemaContext) {
            PredicatedSchemaContext other = (PredicatedSchemaContext)obj;
            if (XPathUtils.equal(this.mBaseContext, other.mBaseContext)) {
                //
                XPathPredicateExpression[] pExprArr1 = this.getPredicateExpressions();
                XPathPredicateExpression[] pExprArr2 = other.getPredicateExpressions();
                //
                return XPathUtils.samePredicatesArr(pExprArr1, pExprArr2);
            }
        }
        //
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.mBaseContext != null ? this.mBaseContext.hashCode() : 0);
        hash = 37 * hash + (this.mXPathPredExprArr != null ? toStringWithoutParent().hashCode() : 0);
        return hash;
    }
    
    public boolean equalsChain(XPathSchemaContext other) {
        return XPathSchemaContext.Utilities.equalsChain(this, other);
    }

    public boolean isLastInChain() {
        return lastInChain;
    }

    public void setLastInChain(boolean value) {
        lastInChain = value;
    }

    // It is implied that the method is called after full resolving of
    // the main expression.
    public boolean hasUnknownVariable() {
        XPathPredicateExpression[] exprArr = getPredicateExpressions();
        if (exprArr == null || exprArr.length == 0) {
            return false;
        }
        //
        boolean result = false;
        //
        for (XPathPredicateExpression predicate : exprArr) {
            XPathExpression predExpr = predicate.getPredicate();
            if (predExpr != null) {
                VariableVisibilityChecker checker =
                        new VariableVisibilityChecker();
                predExpr.accept(checker);
                if (checker.hasUnknownVariable()) {
                    result = true;
                    break;
                }
            }
        }
        //
        return result;
    }

}
