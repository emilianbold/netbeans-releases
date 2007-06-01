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
    
    public WSDLComponent getWsdlContext() {
        return myWsdlComponent;
    }
    
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
