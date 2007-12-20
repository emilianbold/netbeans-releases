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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.xpath.ext.spi.validation;

import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;

/**
 * Specifies the interface of validation context to inform about 
 * standard XPath problems. All standard problems are enumerated in 
 * the XPathProblem enum.
 * 
 * If a validation context is specified to the XPath model, it 
 * will be informed about all problems while the model resolve  
 * schema types. 
 * 
 * @author nk160297
 */
public interface XPathValidationContext {

    /**
     * The first argument can be null. 
     * It usually means that there are exception while parsing the expression.
     */  
    void addResultItem(XPathExpression expr, ResultType resultType, 
             XPathProblem problem, Object... values);
    
}
