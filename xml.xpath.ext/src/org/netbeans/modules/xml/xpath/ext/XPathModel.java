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

import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.netbeans.modules.xml.xpath.ext.spi.ExternalModelResolver;
import org.netbeans.modules.xml.xpath.ext.spi.VariableResolver;
import org.netbeans.modules.xml.xpath.ext.spi.XPathCastResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;

/**
 * Interface for an XPath parser wrapper.
 *
 * @author Enrico Lelina
 * @version 
 */
public interface XPathModel extends XPathSchemaContextHolder {

    /**
     * Returns current root XPath expression of the model. 
     * It is the result of latest parseExpression call.
     */ 
    XPathExpression getRootExpression();
    
    /**
     * This method can be used when an XPath model is constructed 
     * from expressions but not by parsing a text. 
     * @param newExpr
     */
    void setRootExpression(XPathExpression newExpr);
    
    /**
     * Parses an XPath expression.
     * @param expression the XPath expression to parse
     * @return an instance of XPathExpression
     * @throws XPathException for any parsing errors
     */
    XPathExpression parseExpression(String expression)
        throws XPathException;

    /**
     * Returns a model factory.
     */ 
    XPathModelFactory getFactory();

    /**
     * After calling this method the model is resolved again when 
     * a schema information is requested.
     */
    void discardResolvedStatus();
    
    /**
     * Tries resolving all external references: variable, schema elements 
     * and attributes. Assigns schema contexts for all context holders.
     * 
     * Only the model's root expression is processed here. 
     * If you create a new XPath expression with the help of the Factory, 
     * then it is not a part of the root expression and has to be processed 
     * separately with the help of resolveExpressionExtReferences() method.
     * 
     * If the again attribute is true, resolve will be retried.
     * Otherwise it will be executed only if it hasn't been done before.
     * 
     * It does nothing and returns if the model in in resolve mode already.
     * Be aware that the implementation method is synchronized. So the 
     * second thread will be locked until the method is not finished in 
     * the first thread.
     * @return resolution success flag.
     */ 
    boolean resolveExtReferences(boolean again);
    
    /**
     * This method does almost the same as the resolveExtReferences but 
     * it is intended to process expressions, which aren't connected to
     * the model root. 
     * The model root is assigned automatically when the parseExpression() 
     * is used. But it is not if you create an expression with the help of 
     * the factory methods. So there are two choise: 
     *  - specify the expression as a root (method setRootExpression) and 
     * call the resolveExtReferences().
     *  - call this method (without specifying the expression as a root).
     * 
     * Be aware that the implementation method is synchronized. So the 
     * second thread will be locked until the method is not finished in 
     * the first thread.
     * @param expr
     * @return resolution success flag.
     */
    boolean resolveExpressionExtReferences(XPathExpression expr);
    
    /**
     * Adds special replacing function stub() to all places where required 
     * components are skipped. At first it relates to skipped arguments of 
     * functions or operations. Actually all operations require the operands 
     * are specified. Otherwise the generated text representation will be 
     * corrupted. 
     */
    void fillInStubs(XPathExpression expr);
    
    //--------------------------------------------------------------------------
    // SPI methods
    //--------------------------------------------------------------------------
    
    VariableResolver getVariableResolver();
    
    void setVariableResolver(VariableResolver resolver);

    /**
     * An XPath can contain elements from various namespaces. 
     * The method provides an object which helps to resolve namespaces to models.
     */  
    ExternalModelResolver getExternalModelResolver();

    void setExternalModelResolver(ExternalModelResolver resolver);
    
    /**
     * It is necessary to convert prefixes to full namespace URIs and back. 
     */  
    NamespaceContext getNamespaceContext();
    
    /**
     * Specifies a namespace context to the model. 
     * 
     * Namespace prefixes will be declared automatically if ExNamespaceContext 
     * is specified instead of simple NamespaceContext.
     * 
     * @param newContext
     */
    void setNamespaceContext(NamespaceContext newContext);
    
    /**
     * It's intended to collect validation messages. 
     */ 
    XPathValidationContext getValidationContext();
    
    void setValidationContext(XPathValidationContext vContext);
    
    /**
     * Helps to resolve extended functions. The set of such functins is 
     * specific to the particula case. 
     */ 
    ExtensionFunctionResolver getExtensionFunctionResolver();
    
    void setExtensionFunctionResolver(ExtensionFunctionResolver extFuncResolver);

    /**
     * The cast resolver provides the model with information about 
     * available type casts. The model can use it while resolving 
     * location paths. 
     */
    XPathCastResolver getXPathCastResolver();
    
    void setXPathCastResolver(XPathCastResolver xpathCastResolver);

}
