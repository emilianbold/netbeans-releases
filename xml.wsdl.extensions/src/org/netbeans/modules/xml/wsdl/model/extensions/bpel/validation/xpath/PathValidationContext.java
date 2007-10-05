/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.ValidationVisitor;
import org.netbeans.modules.xml.xam.spi.Validator;
import org.netbeans.modules.xml.xpath.visitor.AbstractXPathVisitor;

/**
 * This is an auxiliary class which hold all context objects are required to 
 * validate a WSDL XPath expression. The primary usage target is property alias 
 * objects and theirs query subelement.
 * 
 * @author nk160297
 */
public class PathValidationContext extends AbstractXPathVisitor {
    private Validator myValidator;
    private ValidationVisitor myVVisitor;
    private WSDLComponent myWsdlComponent;
    private WSDLComponent myXpathContentElement;
    
    private transient SchemaModel contextModel;
    private transient SchemaComponent contextComponent;
    
    public PathValidationContext(Validator validator, ValidationVisitor vVisitor, 
            WSDLComponent component, WSDLComponent xpathContentElement) {
        myValidator = validator;
        myVVisitor = vVisitor;
        myWsdlComponent = component;
        myXpathContentElement = xpathContentElement;
    }
    
    public PathValidationContext clone() {
        PathValidationContext newContext = new PathValidationContext(
                myValidator, myVVisitor, 
                myWsdlComponent, myXpathContentElement);
        newContext.setSchemaContextModel(contextModel);
        newContext.setSchemaContextComponent(contextComponent);
        return newContext;
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
    
    public ValidationVisitor getVVisitor() {
        return myVVisitor;
    }

}
