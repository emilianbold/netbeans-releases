/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.xpath.ext.visitor;

import java.util.List;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * Compares two XPath Expressions recursively.
 * The only static function is acccessible. 
 * So the constructor is private intentionally!
 *
 * TODO: All visit(xxx) methods must specify mEquals!
 *
 * @author Nikita Krjukov
 */
public class ExpressionComparatorVisitor implements XPathVisitor {

    private XPathExpression mCurrExpr = null;
    private boolean mEquals = false;

    private ExpressionComparatorVisitor() {
    }

    public static boolean equals(XPathExpression expr1, XPathExpression expr2) {
        if (expr1 == expr2) return true;
        if (expr1 == null || expr2 == null) {
            return false;
        }
        //
        ExpressionComparatorVisitor newVisitor = new ExpressionComparatorVisitor();
        boolean result = newVisitor.compare(expr1, expr2);
        return result;
    }

    public void visit(LocationStep ls1) {
        if (mCurrExpr instanceof LocationStep) {
            LocationStep ls2 = (LocationStep)mCurrExpr;
            //
            XPathSchemaContext sContext1 = ls1.getSchemaContext();
            XPathSchemaContext sContext2 = ls2.getSchemaContext();
            //
            if (sContext1 == sContext2) {
                mEquals = true;
                return;
            }
            if (sContext1 != null && sContext2 != null) {
                if (!sContext1.equals(sContext2)) {
                    mEquals = false;
                    return;
                }
            } else {
                if (!ls1.equals(ls2)) {
                    mEquals = false;
                    return;
                }
            }
            mEquals = true;
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathStringLiteral stringLiteral) {
        if (mCurrExpr instanceof XPathStringLiteral) {
            String value1 = stringLiteral.getValue();
            String value2 = ((XPathStringLiteral)mCurrExpr).getValue();
            mEquals = XPathUtils.equal(value1, value2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathNumericLiteral numericLiteral) {
        if (mCurrExpr instanceof XPathNumericLiteral) {
            Number value1 = numericLiteral.getValue();
            Number value2 = ((XPathNumericLiteral)mCurrExpr).getValue();
            mEquals = XPathUtils.equal(value1, value2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathLocationPath locationPath) {
        if (mCurrExpr instanceof XPathLocationPath) {
            LocationStep[] lsArr1 = locationPath.getSteps();
            LocationStep[] lsArr2 = ((XPathLocationPath)mCurrExpr).getSteps();
            //
            mEquals = compare(lsArr1, lsArr2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathExpressionPath expressionPath) {
        if (mCurrExpr instanceof XPathExpressionPath) {
            //
            XPathExpression root1 = expressionPath.getRootExpression();
            XPathExpression root2 = ((XPathExpressionPath)mCurrExpr).getRootExpression();
            //
            mEquals = compare(root1, root2);
            //
            if (!mEquals) {
                return;
            }
            //
            LocationStep[] lsArr1 = expressionPath.getSteps();
            LocationStep[] lsArr2 = ((XPathExpressionPath)mCurrExpr).getSteps();
            //
            mEquals = compare(lsArr1, lsArr2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathCoreOperation coreOperation) {
        if (mCurrExpr instanceof XPathCoreOperation) {
            CoreOperationType opType1 = coreOperation.getOperationType();
            CoreOperationType opType2 = ((XPathCoreOperation)mCurrExpr).getOperationType();
            if (opType1 != opType2) {
                mEquals = false;
                return;
            }
            //
            List<XPathExpression> children1 = coreOperation.getChildren();
            List<XPathExpression> children2 = ((XPathCoreOperation)mCurrExpr).getChildren();
            //
            mEquals = compare(children1, children2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathCoreFunction coreFunction) {
        if (mCurrExpr instanceof XPathCoreFunction) {
            CoreFunctionType fType1 = coreFunction.getFunctionType();
            CoreFunctionType fType2 = ((XPathCoreFunction)mCurrExpr).getFunctionType();
            if (fType1 != fType2) {
                mEquals = false;
                return;
            }
            //
            List<XPathExpression> children1 = coreFunction.getChildren();
            List<XPathExpression> children2 = ((XPathCoreFunction)mCurrExpr).getChildren();
            //
            mEquals = compare(children1, children2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathExtensionFunction extFunction) {
        if (mCurrExpr instanceof XPathExtensionFunction) {
            ExtFunctionMetadata fmd1 = extFunction.getMetadata();
            ExtFunctionMetadata fmd2 = ((XPathExtensionFunction)mCurrExpr).getMetadata();
            //
            if (!XPathUtils.equal(fmd1, fmd2)) {
                mEquals = false;
                return;
            }
            //
            List<XPathExpression> children1 = extFunction.getChildren();
            List<XPathExpression> children2 = ((XPathExtensionFunction)mCurrExpr).getChildren();
            //
            mEquals = compare(children1, children2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathVariableReference vReference) {
        if (mCurrExpr instanceof XPathVariableReference) {
            XPathVariable var1 = vReference.getVariable();
            XPathVariable var2 = ((XPathVariableReference)mCurrExpr).getVariable();
            mEquals = XPathUtils.equal(var1, var2);
        } else {
            mEquals = false;
        }
    }

    public void visit(XPathPredicateExpression predicate) {
        if (mCurrExpr instanceof XPathPredicateExpression) {
            XPathExpression prExpr1 = predicate.getPredicate();
            XPathExpression prExpr2 = ((XPathPredicateExpression)mCurrExpr).getPredicate();
            mEquals = compare(prExpr1, prExpr2);
        } else {
            mEquals = false;
        }
    }

    //--------------------------------------------------------------------------
    // Auxilary functions

    private boolean compare(List<XPathExpression> children1, List<XPathExpression> children2) {
        //
        if (children1 == children2) {
            return true;
        }
        if (children1 == null || children2 == null) {
            return false;
        }
        if (children1.size() != children2.size()) {
            return false;
        } 
        XPathExpression oldExpression = mCurrExpr;
        try {
            for (int index = 0; index < children1.size(); index++) {
                XPathExpression expr1 = children1.get(index);
                XPathExpression expr2 = children2.get(index);
                //
                if (!compare(expr1, expr2)) {
                    return false;
                }
            }
            return true;
        } finally {
            mCurrExpr = oldExpression;
        }
    }

    private boolean compare(LocationStep[] lsArr1, LocationStep[] lsArr2) {
        //
        if (lsArr1 == lsArr2) {
            return true;
        }
        if (lsArr1 == null || lsArr2 == null) {
            return false;
        }
        if (lsArr1.length != lsArr2.length) {
            return false;
        }
        XPathExpression oldExpression = mCurrExpr;
        try {
            for (int index = 0; index < lsArr1.length; index++) {
                LocationStep ls1 = lsArr1[index];
                LocationStep ls2 = lsArr2[index];
                mCurrExpr = ls1;
                mEquals = false;
                visit(ls2);
                if (!mEquals) {
                    return false;
                }
            }
            return true;
        } finally {
            mCurrExpr = oldExpression;
        }
    }

    private boolean compare(XPathExpression expr1, XPathExpression expr2) {
        if (expr1 == expr2) {
            return true;
        }
        if (expr1 == null || expr2 == null) {
            return false;
        }
        //
        XPathExpression oldExpr = mCurrExpr;
        try {
            mCurrExpr = expr1;
            mEquals = false;
            expr2.accept(this);
            return mEquals;
        } finally {
            mCurrExpr = oldExpr;
        }
    }

}
