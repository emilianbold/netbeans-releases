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

import java.text.MessageFormat;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.ContentElement;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathProblem;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.util.NbBundle;

/**
 * This is an auxiliary class which hold all context objects are required to 
 * validate a BPEL XPath expression. The primary usage target is property alias 
 * objects and theirs query subelement.
 * 
 * @author nk160297
 */
public class PathValidationContext implements XPathValidationContext {

    private static EntityTypeNameVisitor etnv = new EntityTypeNameVisitor();
    
    private XPathModel myXPathModel;
    private Validator myValidator;
    private ValidationVisitor myVVisitor;
    private BpelEntity myBpelContextActivity;
    private ContentElement myXpathContentElement;
    
    private transient SchemaModel contextModel;
    private transient SchemaComponent contextComponent;
    
    public PathValidationContext(Validator validator, ValidationVisitor vVisitor, ContentElement contentElement) {
        myValidator = validator;
        myVVisitor = vVisitor;
        myXpathContentElement = contentElement;
    }
    
    public XPathModel getXPathModel() {
        return myXPathModel;
    }

    public void setXPathModel(XPathModel model) {
        myXPathModel = model;
    }
    
    /**
     * Returns the immediate owner of the validated XPath expression.
     */
    public ContentElement getXPathContentElement() {
        return myXpathContentElement;
    }
    
    /**
     * Context is a Schema component which represents current context for 
     * the XPath expression. 
     * <p>
     * In case of relative location paths, it references to a parent component, 
     * which should be considered as a parent for the first location step element.
     * <p>
     * In case of absolute location paths, it references to a global component, 
     * which corresponds to the root location step. 
     */ 
    public void setSchemaContextComponent(SchemaComponent context) {
        contextComponent = context;
    }
    
    public SchemaComponent getSchemaContextComponent() {
        return contextComponent;
    }
    
    /**
     * Context model specifies the root schema model. 
     * It is intended to be used to check absolute location paths.
     */ 
    public void setSchemaContextModel(SchemaModel context) {
        contextModel = context;
    }
    
    public SchemaModel getSchemaContextModel() {
        return contextModel;
    }
    
    public Validator getValidator() {
        return myValidator;
    }

    private ValidationVisitor getVVisitor() {
        return myVVisitor;
    }

    /**
     * Adds validation result item in current context.
     */ 
    public void addResultItem(ResultType resultType, String str, Object... values){
        addResultItemImpl(null, resultType, str, values);
    }

    /**
     * Adds validation result item in current context.
     */ 
    public void addResultItem(String exprText, ResultType resultType, String str, Object... values) {
        addResultItemImpl(exprText, resultType, str, values);
    }

    public void addResultItem(XPathExpression expr, ResultType resultType, 
            XPathProblem problem, Object... values) {
        //
        String exprText = null;
        if (expr != null) {
            exprText = expr.getExpressionString();
        }
        //
        if (problem == XPathProblem.PREFIX_REQUIRED_FOR_EXT_FUNCTION) {
            String msg = NbBundle.getMessage(
                    PathValidationContext.class, problem.toString());
            addResultItemImpl(exprText, resultType, msg, values);
        } else {
            addResultItemImpl(exprText, resultType, problem.getMsgTemplate(), values);
        }
    }
    
    private void addResultItemImpl(String exprText, ResultType resultType, String template, Object... values){
        //
        String str = template;
        if (values != null && values.length > 0) {
            str = MessageFormat.format(str, values);
        }
        //
        ContentElement ce = getXPathContentElement();
        String ceTypeName = etnv.getTypeName((BpelEntity)ce);
        str = ceTypeName + ": " + str;
        //
        if (exprText == null || exprText.length() == 0) {
            if (myXPathModel != null) {
                XPathExpression rootExpr = myXPathModel.getRootExpression();

                if (rootExpr == null) {
                  return;
                }
                exprText = rootExpr.getExpressionString();
            }
        }
        //
        str = str + " Expression: \"" + exprText + "\"";
        //
        ResultItem resultItem = new ResultItem(
                getValidator(),
                resultType,
                (BpelEntity)ce, 
                str);
        if (getVVisitor() != null) {
            getVVisitor().getResultItems().add(resultItem);
        }
    }
}
