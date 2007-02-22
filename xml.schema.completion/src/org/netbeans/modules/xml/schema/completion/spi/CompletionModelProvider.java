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
package org.netbeans.modules.xml.schema.completion.spi;

import java.util.List;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 * Schema aware code completion feature works, based on the "schemaLocation"
 * attribute of the root element of the XML document. If found, it looks up
 * the models of the schemas specified.
 *
 * CompletionModelProvider is a hook for XML documents that will not specify
 * "schemaLocation" attribute in their document but still want to use the schema
 * aware code completion feature. For example, if you want code completion in a
 * WSDL document based on WSDL's schema, all you need to do is, implement a
 * CompletionModelProvider and return the set of models for WSDL's schema(s).
 *
 *
 * @author Samaresh
 */
public abstract class CompletionModelProvider {
    
    /**
     * Returns a list of CompletionModels at a given context. Context may be
     * used in determining the list of CompletionModels. For example, it does
     * not make sense to return WSDL models while working on a non-WSDL file.
     */
    public abstract List<CompletionModel> getModels(CompletionContext context);
    
    /**
     * Class CompletionModel.
     */
    public static abstract class CompletionModel {
        
        /**
         * Returns the suggested prefix to be used for completion.
         */
        public abstract String getSuggestedPrefix();
        
        /**
         * Returns the target namespace for this schema model.
         */
        public abstract String getTargetNamespace();
        
        /**
         * Returns the schema model.
         */
        public abstract SchemaModel getSchemaModel();
    }
    
}
