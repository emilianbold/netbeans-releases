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

import javax.xml.namespace.QName;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * Represents an XPath variable. 
 * The variable can have quite wide interpretation.
 *   
 * For example, the BPEL has an extension which allows using variables 
 * of a WSDL Message type in XPath. Such variables are always used 
 * with a Message Part. In the XPath expression the variable name 
 * goes first and it should be followed with a part name. The part 
 * name is separated from the variable name with the dot character.
 * So in case of BPEL the method getVariableName() can return not just 
 * the name of variable, but its combination with part name. 
 * The method getVariable() can't simply return BPEL Variable object.
 * In order to support Message variables it has to return special container, 
 * which can hold a Message part as well. 
 *  
 * The method getType() doesn't depend on the kind of variable. 
 * It has always return Schema component which will be used as a 
 * context for subsequent elements of the XPath expression.
 * 
 * In case of a BPEL Message type varialbe, the method getType() should 
 * return the type of Message Part.
 * 
 * @author radval
 * @author nk160297
 */
public interface XPathVariableReference 
        extends XPathExpression, XPathSchemaContextHolder {

    QName getVariableName(); 

    /**
     * Returns the object which is provided by the VariableResolver.
     * The type is specific to a module which is used the XPath model.
     * See description of the interface above.
     */ 
    XPathVariable getVariable();
   
    /**
     * ReferenceableSchemaComponent is the base interface for many different 
     * elements of the schema model. But it is implied that only those, which 
     * can be used as a type of variable, can be returned: 
     *  - Global schema type (primitive, simple, complex) 
     *  - Global schema element
     * 
     * Unfortunately the Schema model doesn't have a special marker interface 
     * for such cases. 
     */ 
    ReferenceableSchemaComponent getType();
}
