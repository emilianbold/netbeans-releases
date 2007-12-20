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

package org.netbeans.modules.xml.xpath.ext.spi;

import java.util.Collection;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * The interface if intended to be implemented by other modules, 
 * which are going to use the XPath model. With the help of 
 * the interface the XPath model can obtaing an external Schema 
 * model by the specified namespace URI. It is required when 
 * there are absolute location pathes in the XPath expression. 
 * 
 * @author nk160297
 */
public interface ExternalModelResolver {
    
    /**
     * Get a list of object schema model by the specified namespace URI.
     */ 
    Collection<SchemaModel> getModels(String schemaNamespaceUri);

    /**
     * Get a list of all schema models is visible at current position.
     * 
     * This method is intended to check the first step of an absolute 
     * location path in case when it is written without a namespace 
     * prefix. In such case the resolver tries to look the item 
     * in all available models. 
     */ 
    Collection<SchemaModel> getVisibleModels();
    
    /**
     * Determine if a schema with the specified namspace is visible at current position.
     * 
     * This method is used by XPath validator to check if a schema with 
     * the specified name is imported or not. You can return any value 
     * if the XPath validation is not required. 
     */ 
    boolean isSchemaVisible(String schemaNamespaceUri);
}
