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
package org.netbeans.modules.xml.xpath.ext.spi;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;

/**
 * Intended to provide a set of XPath extension functions. 
 * Extension functions are specific to particular XPath usage. 
 * So it is the responsibility of an external module to provid full 
 * information about extension functions. 
 * 
 * @author nk160297
 */
public interface ExtensionFunctionResolver {

    String JAVA_PROTOCOL = "java://";

    /**
     * Returns a metadata for the specified function. 
     */ 
    ExtFunctionMetadata getFunctionMetadata(QName name);
    
    /**
     * Indicates if the function is implicit. Implicit function doesn't require 
     * to be declared with personal metadata. A good example of implicit functions
     * is java-based XPath extension functions. 
     * @param name - QName of the function.
     * WARNING! It is implied that the parameter is populated with full
     * namespace URI but not only the prefix. 
     * @return
     */
    boolean isImplicit(QName name);

    /**
     * Returns full collection of supported extension functions. 
     */ 
    Collection<QName> getSupportedExtFunctions();
    
    /**
     * If an external module requires a specific class for a function, 
     * it can use this method to create new instance of such class. 
     */ 
    XPathExtensionFunction newInstance(XPathModel model, QName name);
    
    /**
     * Performs specific validation of the specified function.
     */ 
    void validateFunction(XPathExtensionFunction function, 
            XPathValidationContext context);
}
