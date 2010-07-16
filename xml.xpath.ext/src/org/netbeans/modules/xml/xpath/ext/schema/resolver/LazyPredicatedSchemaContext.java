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
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathModelConstructor;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.openide.ErrorManager;

/**
 * The special extension of Predicated schema context.
 * It implies lazy construction of predicates' expressions. 
 * 
 * WARNING. The class hasn't intended to be used in HashSet yet. 
 * It doesn't have hashCode method redifined.
 * 
 * @author nk160297
 */
public class LazyPredicatedSchemaContext extends PredicatedSchemaContext {

    private String[] mPredTextArr;
    private XPathModelConstructor mXPathModelConstr;
    
    public LazyPredicatedSchemaContext(XPathSchemaContext baseContext,
            String predicateText, XPathModelConstructor xPathModelConstr) {
        super(baseContext);
        //
        assert predicateText != null && predicateText.length() != 0 &&
                xPathModelConstr != null;
        assert !(baseContext instanceof LazyPredicatedSchemaContext);
        //
        mPredTextArr = new String[] {predicateText};
        mXPathModelConstr = xPathModelConstr;
    }

    public LazyPredicatedSchemaContext(XPathSchemaContext baseContext, 
            String[] predicateTextArr, XPathModelConstructor xPathModelConstr) {
        super(baseContext);
        //
        assert predicateTextArr != null && predicateTextArr.length != 0 && 
                xPathModelConstr != null;
        assert !(baseContext instanceof LazyPredicatedSchemaContext);
        //
        mPredTextArr = predicateTextArr;
        mXPathModelConstr = xPathModelConstr;
    }

    @Override
    public PredicatedSchemaContext clone() {
        PredicatedSchemaContext result = super.clone();
        if (result != null) {
            return result;
        } else if (mPredTextArr != null) {
            return new LazyPredicatedSchemaContext(mBaseContext, mPredTextArr, mXPathModelConstr);
        } else if (mXPathModelConstr != null) {
            return new LazyPredicatedSchemaContext(mBaseContext, mPredTextArr, mXPathModelConstr);
        }
        return null;
    }

    @Override
    public XPathPredicateExpression[] getPredicateExpressions() {
        if (mXPathPredExprArr == null) {
            XPathModel newModel = mXPathModelConstr.getModel();
            ArrayList<XPathPredicateExpression> newPredExprList = 
                    new ArrayList<XPathPredicateExpression>();
            for (String xPredText : mPredTextArr) {
                try {
                    XPathExpression expr = newModel.parseExpression(xPredText);
                    XPathPredicateExpression newPredExpr = newModel.getFactory().
                            newXPathPredicateExpression(expr);
                    newPredExprList.add(newPredExpr);
                } catch (XPathException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
            mXPathPredExprArr = newPredExprList.toArray(
                    new XPathPredicateExpression[newPredExprList.size()]);
        }
        return mXPathPredExprArr;
    }

    @Override
    public void setPredicateExpressions(XPathPredicateExpression[] newExpr) {
        mPredTextArr = null;
        super.setPredicateExpressions(newExpr);
    }

    /**
     * Be carefull to use this method. It requires that the lazy initialization
     * approach is used while constructing this context. See the constuctor,
     * which takes the XPathModelConstructor as argument.
     * @param newPredicates
     */
    public void setPredicateTexts(String[] newPredicates) {
        mPredTextArr = newPredicates;
        mXPathPredExprArr = null;
    }
}
