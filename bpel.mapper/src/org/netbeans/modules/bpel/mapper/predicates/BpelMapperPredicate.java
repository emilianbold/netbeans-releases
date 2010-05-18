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

import org.netbeans.modules.bpel.mapper.cast.BpelMapperLsm;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.events.VetoException;
import org.netbeans.modules.bpel.model.api.support.BpelXPathModelFactory;
import org.netbeans.modules.bpel.model.ext.editor.api.LocationStepModifier;
import org.netbeans.modules.bpel.model.ext.editor.api.Predicate;
import org.netbeans.modules.bpel.model.ext.editor.api.Source;
import org.netbeans.modules.soa.xpath.mapper.lsm.MapperPredicate;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.PredicatedSchemaContext;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.openide.ErrorManager;

/**
 * Represents XPath Predicate in BPEL Mapper
 *
 * @author Nikita Krjukov
 */
public class BpelMapperPredicate extends MapperPredicate implements BpelMapperLsm {

    /**
     * Loads and creates a new XPathPredicate from the Predicate object.
     * The Predicate object is taken from the BPEL Editor extension.
     *
     * Such extension can be located either inside of a variable declaration
     * or in the owner of the XPath expression.
     *
     * @param predicate
     * @param castResolver is a resolver, which reflects a chain of parent
     * LocationStepModifier extensions in case the predicate is nested to
     * another extensions. It can be null and it means that there isn't
     * any parent LocationStepModifier extensions.
     * @param varContext points to a BpelEntity which is used to calculate
     * the set of visible variables. It usually sould be the entity,
     * for which the mapper is shown.
     *
     * @return
     */
    public static BpelMapperPredicate convert(Predicate predicate,
            XPathCastResolver castResolver, BpelEntity varContextEntity) {
        //
        if (predicate == null) {
            return null;
        }
        //
        XPathExpression xPathExpr =
                getExpression(predicate, varContextEntity, castResolver);
        if (xPathExpr == null || !(xPathExpr instanceof XPathExpressionPath)) {
            return null;
        }
        //
        XPathExpressionPath exprPath = (XPathExpressionPath) xPathExpr;
        XPathSchemaContext sContext = exprPath.getSchemaContext();
        if (sContext == null || !(sContext instanceof PredicatedSchemaContext)) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "Predicate path \"" + xPathExpr.getExpressionString() +
                    "\". " + "It has to be a valid path expression!"); // NOI18N
            return null;
        }
        //
        BpelMapperPredicate result = new BpelMapperPredicate(
                PredicatedSchemaContext.class.cast(sContext));
        return result;
    }

    public static XPathExpression getExpression(Predicate predicate,
            BpelEntity varContext, XPathCastResolver castResolver) {
        String pathText = predicate.getPath();
        if (pathText == null || pathText.length() == 0) {
            return null;
        }
        //
        XPathModel xPathModel = BpelXPathModelFactory.create(
                predicate, varContext, castResolver);
        XPathExpression xPathExpr = null;
        try {
            xPathExpr = xPathModel.parseExpression(pathText);
        } catch (XPathException ex) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                    "Unresolved XPath: " + pathText); // NOI18N
        }
        return xPathExpr;
    }

    public BpelMapperPredicate(PredicatedSchemaContext predSContext) {
        super(predSContext);
    }
    
    /**
     * Fill in the target BPEL model's Predicate object by the content of 
     * this predicate. 
     * @param target
     * @param destination
     * @param inLeftMapperTree
     * @return
     */
    public boolean populatePredicate(Predicate target,
            BpelEntity destination, boolean inLeftMapperTree) {
        //
        String xPathText = getSchemaContext().getExpressionString(
                destination.getNamespaceContext(), null);
        //
        if (xPathText != null && xPathText.length() != 0) {
            try {
                target.setPath(xPathText);
            } catch (VetoException ex) {
                return false;
            }
        } else {
            return false;
        }
        //
        if (inLeftMapperTree) {
            target.setSource(Source.FROM);
        } else {
            target.setSource(Source.TO);
        }
        //
        return true;
    }

    @Override
    public BpelMapperPredicate clone() {
        XPathSchemaContext sContext = getSchemaContext();
        assert sContext instanceof PredicatedSchemaContext;
        if (sContext == null || !(sContext instanceof PredicatedSchemaContext)) {
            return null;
        }
        //
        PredicatedSchemaContext contextClone =
                ((PredicatedSchemaContext)sContext).clone();
        //
        return new BpelMapperPredicate(contextClone);
    }


    public boolean equalsIgnoreLocation(LocationStepModifier lsm) {
        if (lsm instanceof Predicate) {
            return equalsIgnoreLocation((Predicate)lsm);
        }
        return false;
    }

    protected boolean equalsIgnoreLocation(Predicate predicate) {
        if (predicate == null) {
            return false;
        }
        //
        String myPath = this.getPredicatesText();
        String otherPath = predicate.getPath();
        //
        // This way can be vulnerable
        return otherPath.endsWith(myPath);
    }

    public VariableDeclaration getBaseBpelVariable() {
        return BpelMapperUtils.getBaseVariable(getSchemaContext());
    }

} 
