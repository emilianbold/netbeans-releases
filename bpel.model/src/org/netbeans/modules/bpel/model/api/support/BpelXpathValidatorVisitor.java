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
package org.netbeans.modules.bpel.model.api.support;

import java.util.Collection;
import org.netbeans.modules.bpel.model.api.Activity;
import org.netbeans.modules.bpel.model.api.BooleanExpr;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Branches;
import org.netbeans.modules.bpel.model.api.Condition;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.bpel.model.api.DeadlineExpression;
import org.netbeans.modules.bpel.model.api.ExpressionLanguageSpec;
import org.netbeans.modules.bpel.model.api.FinalCounterValue;
import org.netbeans.modules.bpel.model.api.For;
import org.netbeans.modules.bpel.model.api.From;
import org.netbeans.modules.bpel.model.api.OnAlarmEvent;
import org.netbeans.modules.bpel.model.api.Query;
import org.netbeans.modules.bpel.model.api.RepeatEvery;
import org.netbeans.modules.bpel.model.api.StartCounterValue;
import org.netbeans.modules.bpel.model.api.To;
import org.netbeans.modules.bpel.model.api.support.ExNamespaceContext;
import org.netbeans.modules.bpel.model.api.support.XPathModelFactory;
import org.netbeans.modules.bpel.model.impl.references.SchemaReferenceBuilder;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xpath.ext.XPathModelHelper;
import org.netbeans.modules.xml.xpath.ext.XPathException;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import java.util.HashSet;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

/**
 * Validates expressions in TEXT child of DOM elements against
 * XPath expression.
 *
 * @author ads
 * @author nk160297
 *
 */
class BpelXpathValidatorVisitor extends SimpleBpelModelVisitorAdaptor implements ValidationVisitor {
    
    private Validator myValidator;
    private BpelEntity validatedActivity; 
    
    BpelXpathValidatorVisitor(Validator validator) {
        myValidator = validator;
        init();
    }
    
    public HashSet<ResultItem> getResultItems() {
      return myResultItems;
    }
    
    protected void init(){
      myResultItems = new HashSet<ResultItem>();
    }

    private HashSet<ResultItem> myResultItems;

    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.BooleanExpr)
     */
    @Override
    public void visit( BooleanExpr expr ) {
        checkXPathExpression( expr );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.Branches)
     */
    @Override
    public void visit( Branches branches ) {
        checkXPathExpression( branches );
    }
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.Condition)
     */
    @Override
    public void visit( Condition condition ){
        checkXPathExpression( condition );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.DeadlineExpression)
     */
    @Override
    public void visit( DeadlineExpression expression ) {
        checkXPathExpression( expression );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.FinalCounterValue)
     */
    @Override
    public void visit( FinalCounterValue value ) {
        checkXPathExpression( value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.For)
     */
    @Override
    public void visit( For fo ) {
        checkXPathExpression( fo );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.From)
     */
    @Override
    public void visit( From from ) {
        checkXPathExpression( from );
    }
    
    @Override
    public void visit ( Query query ) {
        // TODO additional validation is required for Copy-->From/To-->Query
        // It has to work similar to PropertyAlias-->Query
        checkXPathExpression( query );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.RepeatEvery)
     */
    @Override
    public void visit( RepeatEvery repeatEvery ) {
        checkXPathExpression( repeatEvery );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.StartCounterValue)
     */
    @Override
    public void visit( StartCounterValue value ) {
        checkXPathExpression( value );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.bpel.model.api.support.BpelModelVisitorAdaptor#visit(org.netbeans.modules.bpel.model.api.To)
     */
    @Override
    public void visit( To to ) {
        checkXPathExpression( to );
    }
    
    @Override
    public void visit( OnAlarmEvent event) {
        validatedActivity = event;
    }
    
    @Override
    protected void visit( Activity activity ) {
        validatedActivity = activity;
    }
    
    private void checkXPathExpression( ContentElement element ){
        String content = element.getContent();
        if ( content == null ){
            return;
        }
        content = content.trim();
        if ( content.length() == 0 ) {
            return;
        }
        String expressionLang = null;
        if ( element instanceof ExpressionLanguageSpec ) {
            expressionLang = ((ExpressionLanguageSpec) element).
                    getExpressionLanguage();
        }
        //
        checkExpression( expressionLang, content, element);
    }
    
    public void checkExpression(String exprLang, String exprText, 
            final ContentElement element) {
        boolean isXPathExpr = (exprLang == null ||
                XPathModelFactory.DEFAULT_EXPR_LANGUAGE.equals(exprLang));
        //
        // we can handle only xpath expressions.
        if (!isXPathExpr) {
            return;
        }
        //
        XPathModelHelper helper= XPathModelHelper.getInstance();
        XPathModel model = helper.newXPathModel();
        //
        assert validatedActivity != null;
        final PathValidationContext context = new PathValidationContext(
                model, myValidator, this, validatedActivity, element);
        model.setValidationContext(context);
        //
        ExNamespaceContext nsContext = ((BpelEntity)element).getNamespaceContext();
        model.setNamespaceContext(new BpelXPathNamespaceContext(nsContext));
        //
        model.setVariableResolver(new BpelVariableResolver(
                context, validatedActivity));
        model.setExtensionFunctionResolver(new BpelXpathExtFunctionResolver());
        //
        model.setExternalModelResolver(new ExternalModelResolver() {
            public Collection<SchemaModel> getModels(String modelNsUri) {
                BpelModel bpelModel = ((BpelEntity)element).getBpelModel();
                return SchemaReferenceBuilder.getSchemaModels(bpelModel, modelNsUri);
            }

            public Collection<SchemaModel> getVisibleModels() {
                // Implementation of the method is not necessary here 
                // because it is used only to resolve the first step 
                // of an absolute location path. The absolute pathes 
                // are not supported in the BPEL. 
                context.addResultItem(Validator.ResultType.ERROR, 
                        "ABSOLUTE_PATH_DISALLOWED"); // NOI18N
                return null;
            }

            public boolean isSchemaVisible(String schemaNamespaceUri) {
                return context.isSchemaImported(schemaNamespaceUri);
            }
        });
        //
        // Checks if the expression contains ";". 
        // If it does, then split it to parts and verifies them separately.
        if (exprText.contains(XPathModelFactory.XPATH_EXPR_DELIMITER)) {
            //
            // Notify the user that the expression is not completed
            context.addResultItem(exprText, Validator.ResultType.ERROR, 
                    "INCOMPLETE_XPATH"); // NOI18N
            //
            String[] partsArr = exprText.split(
                    XPathModelFactory.XPATH_EXPR_DELIMITER);
            for (String anExprText : partsArr) {
                if (anExprText != null && anExprText.length() != 0) {
                    //
                    // Only the first expression graph has to be connected 
                    // to the right tree! The isFirst flag is used for it. 
                    checkSingleExpr(model, anExprText);
                }
            }
        } else {
            checkSingleExpr(model, exprText);
        }
    }

    private void checkSingleExpr(XPathModel model, String exprText) {
        try {
            XPathExpression xpath = model.parseExpression(exprText);
            //
            // Common validation will be made here!
            model.resolveExtReferences(true);
        } catch (XPathException e) {
            // Nothing to do here because of the validation context 
            // was specified before and it has to be populated 
            // with a set of problems.
        }
        //
        // TODO additional validation is required for Copy-->From/To-->Query
        // It has to work similar to PropertyAlias-->Query
    }
}
