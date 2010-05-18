/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import java.text.MessageFormat;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathProblem;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationVisitor;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.spi.Validator.ResultType;
import org.openide.util.NbBundle;

/**
 * This is an auxiliary class which hold all context objects are required to 
 * validate a WSDL XPath expression. The primary usage target is property alias 
 * objects and theirs query subelement.
 * 
 * @author nk160297
 */
public class PathValidationContext implements XPathValidationContext {

    private XPathModel myXPathModel;
    private Validator myValidator;
    private ValidationVisitor myVVisitor;
    private WSDLComponent myWsdlComponent;
    private WSDLComponent myXpathContentElement;
    private NamespaceContext myNsContext;
    
    private transient SchemaComponent contextComponent;
    
    public PathValidationContext(XPathModel xPathModel, 
            Validator validator, ValidationVisitor vVisitor, 
            WSDLComponent component, WSDLComponent xpathContentElement, 
            NamespaceContext nsContext) {
        myXPathModel = xPathModel;
        myValidator = validator;
        myVVisitor = vVisitor;
        myWsdlComponent = component;
        myXpathContentElement = xpathContentElement;
        myNsContext = nsContext;
    }
    
    /**
     * Returns the Global WSLD component to which the validation is applied.
     */
    public WSDLComponent getWsdlContext() {
        return myWsdlComponent;
    }
    
    /**
     * Returns the immediate owner of the validated XPath expression.
     */
    public WSDLComponent getXpathContentElement() {
        return myXpathContentElement;
    }
    
    /**
     * This context is specific to validation of Property Alias with query. 
     * The schema component is a global element or global type of 
     * the massage part to which the query is implied to be applied. 
     * The query first step has to be consistent with the type of the message part. 
     * <p>
     * In case of the query has a form of relative location paths, its first step 
     * has to be associated with a child element of the context element (type).
     * <p>
     * In case of the query has a form of absolute location paths, its first step 
     * has to be associated with the same element as specified for the massage part. 
     * <p>
     * If the message part uses a global type, then only relative form is 
     * allowed. See issue #90323.
     */ 
    public void setSchemaContextComponent(SchemaComponent context) {
        contextComponent = context;
    }
    
    public SchemaComponent getSchemaContextComponent() {
        return contextComponent;
    }
    
    public Validator getValidator() {
        return myValidator;
    }
    
    public ValidationVisitor getVVisitor() {
        return myVVisitor;
    }
    
    public NamespaceContext getNsContext() {
        return myNsContext;
    }

    public void setXPathModel(XPathModel model) {
        myXPathModel = model;
    }

    public void addResultItem(String exprText, ResultType resultType, String str, Object... values) {
        addResultItemImpl(resultType, str, values);
    }

    public void addResultItem(ResultType resultType, String bundleKey, Object... values){
        //
        String str = NbBundle.getMessage(BPELExtensionXpathValidator.class, bundleKey);
        addResultItemImpl(resultType, str, values);
    }
    
    public void addResultItem(XPathExpression expr, ResultType resultType, XPathProblem problem, Object... values) {
        //
        addResultItemImpl(resultType, problem.getMsgTemplate(), values);
    }

    private void addResultItemImpl(ResultType resultType, String template,
            Object... values){
        //
        String str = template;
        if (values != null && values.length > 0) {
            str = MessageFormat.format(str, values);
        }
        //
        if (myXPathModel != null) {
            XPathExpression rootExpr = myXPathModel.getRootExpression();
            str = str + " Expression: \"" + rootExpr + "\"";
        }
        //
        ResultItem resultItem = new ResultItem(
                getValidator(), resultType, getXpathContentElement(), str);
        getVVisitor().getResultItems().add(resultItem);
    }
}
